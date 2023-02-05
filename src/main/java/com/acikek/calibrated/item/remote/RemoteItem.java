package com.acikek.calibrated.item.remote;

import com.acikek.calibrated.CalibratedAccess;
import com.acikek.calibrated.api.impl.CalibratedAccessAPIImpl;
import com.acikek.calibrated.gamerule.CAGameRules;
import com.acikek.calibrated.sound.CASoundEvents;
import com.acikek.calibrated.util.RemoteUser;
import com.acikek.datacriteria.api.DataCriteriaAPI;
import com.acikek.datacriteria.api.Parameters;
import net.fabricmc.fabric.api.item.v1.FabricItem;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.stat.Stats;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class RemoteItem extends Item implements FabricItem {

    public static final Identifier ACCESS = CalibratedAccess.id("remote");
    public static final Identifier CREATE_SESSION = CalibratedAccess.id("create_session");

    public static final int ACCESS_TICKS = 15 * 20;
    public static final int STATUS_TICKS = 3 * 20;

    public RemoteType remoteType;

    public RemoteItem(Settings settings, RemoteType remoteType) {
        super(settings);
        this.remoteType = remoteType;
    }

    public static NamedScreenHandlerFactory getScreen(World world, BlockPos pos, BlockState state) {
        if (world.getBlockEntity(pos) instanceof NamedScreenHandlerFactory screen) {
            return screen;
        }
        return state.createScreenHandlerFactory(world, pos);
    }

    public static void triggerRemoteUsed(ServerPlayerEntity player, ServerWorld world, BlockPos targetPos, boolean interdimensional) {
        DataCriteriaAPI.trigger(CalibratedAccess.id("remote_used"), player, Parameters.block(world, targetPos), interdimensional);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        BlockPos pos = context.getBlockPos();
        BlockState state = context.getWorld().getBlockState(pos);
        boolean hasListeners = CalibratedAccessAPIImpl.blockListeners.containsKey(state.getBlock());
        if (hasListeners || getScreen(context.getWorld(), pos, state) != null) {
            NbtCompound nbt = context.getStack().getOrCreateNbt();
            calibrate(nbt, context.getPlayer(), context.getWorld(), pos, state);
            return ActionResult.SUCCESS;
        }
        return super.useOnBlock(context);
    }

    public void calibrate(NbtCompound nbt, PlayerEntity player, World world, BlockPos pos, BlockState state) {
        nbt.putLong("SyncedPos", pos.asLong());
        nbt.putString("SyncedWorld", world.getRegistryKey().getValue().toString());
        syncBlock(nbt, state.getBlock());
        // Remove animations if any are present
        nbt.remove("VisualTicks");
        nbt.remove("CustomModelData");
        RemoteUser remoteUser = (RemoteUser) player;
        // If the remote has an old session, attempt to remove that
        if (nbt.contains("Session")) {
            remoteUser.removeSession(nbt.getUuid("Session"));
        }
        UUID session = UUID.randomUUID();
        nbt.putUuid("Session", session);
        remoteUser.addSession(session, pos, CAGameRules.getMaxSessions(world));
        if (!remoteType.unlimited()) {
            nbt.putInt("Accesses", remoteType.accesses());
        }
        playSound(CASoundEvents.REMOTE_SYNC, 0.85f + world.random.nextFloat() * 0.3f, player, world);
        player.incrementStat(CREATE_SESSION);
    }

    public static void syncBlock(NbtCompound nbt, Block block) {
        nbt.putString("SyncedId", Registry.BLOCK.getId(block).toString());
        Text text =  Text.translatable(block.getTranslationKey())
                .formatted(block.asItem().getRarity(block.asItem().getDefaultStack()).formatting);
        nbt.putString("SyncedText", Text.Serializer.toJson(text));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        // Run on server to validate world and guarantee proper block entity fetching.
        if (user instanceof ServerPlayerEntity player) {
            NbtCompound nbt = stack.getOrCreateNbt();
            RemoteUseResult result = use(stack, nbt, world, player);
            if (result.isError()) {
                fail(nbt, result.eraseInfo(), player, world);
                user.sendMessage(result.getErrorMessage(), true);
            }
            return TypedActionResult.pass(stack);
        }
        return super.use(world, user, hand);
    }

    public boolean canTryAccess(NbtCompound nbt) {
        return nbt.contains("SyncedPos") && (remoteType.unlimited() || nbt.getInt("Accesses") >= 1);
    }

    public RemoteUseResult use(ItemStack stack, NbtCompound nbt, World world, ServerPlayerEntity player) {
        RemoteUser remoteUser = (RemoteUser) player;
        if (!canTryAccess(nbt)) {
            return RemoteUseResult.CANNOT_ACCESS;
        }
        // Prevents a player from using a remote that isn't matched with the player
        if (!remoteUser.hasSession(nbt.getUuid("Session"))) {
            return RemoteUseResult.INVALID_SESSION;
        }
        // Prevents interdimensional accesses for remotes that do not have this ability
        // This is not considered irreversible as the player can move back to the proper dimension
        Identifier worldId = new Identifier(nbt.getString("SyncedWorld"));
        boolean interdimensional = !world.getRegistryKey().getValue().equals(worldId);
        if (interdimensional && !remoteType.interdimensional()) {
            return RemoteUseResult.INVALID_WORLD;
        }
        // Prevents accesses from invalid worlds
        ServerWorld serverWorld = (ServerWorld) world;
        ServerWorld targetWorld = interdimensional
                ? serverWorld.getServer().getWorld(RegistryKey.of(Registry.WORLD_KEY, worldId))
                : serverWorld;
        if (targetWorld == null) {
            return RemoteUseResult.INVALID_WORLD;
        }
        // Prevents accesses to target blovks with different IDs if the gamerule disallows it
        BlockPos pos = BlockPos.fromLong(nbt.getLong("SyncedPos"));
        BlockState state = targetWorld.getBlockState(pos);
        Identifier syncedId = new Identifier(nbt.getString("SyncedId"));
        boolean differentSyncedId = !Registry.BLOCK.getId(state.getBlock()).equals(syncedId);
        if (differentSyncedId && !world.getGameRules().getBoolean(CAGameRules.ALLOW_ID_MISMATCH)) {
            return RemoteUseResult.INVALID_ID;
        }
        // Most checks passed; now onto activation
        UUID session = nbt.getUuid("Session");
        boolean sessionActive = remoteUser.isSessionActive(session);
        // Invoke remote accessed event for custom block behavior, if any
        RemoteUseResult invokedResult = CalibratedAccessAPIImpl.REMOTE_ACCESSED.invoker()
                .onRemoteAccessed(targetWorld, player, pos, state, this, stack);
        if (invokedResult != null && invokedResult.isError()) {
            return invokedResult;
        }
        // If there were no invokers and the screen cannot be found, desync
        NamedScreenHandlerFactory screen = invokedResult == null ? getScreen(targetWorld, pos, state) : null;
        if (invokedResult == null && screen == null) {
            return RemoteUseResult.DESYNC;
        }
        if (!sessionActive) {
            RemoteUser.activateSession(player, session, ACCESS_TICKS);
        }
        if (screen != null) {
            player.openHandledScreen(screen);
        }
        // Effects and NBT
        activate(nbt, player, world, targetWorld, sessionActive, interdimensional, differentSyncedId, pos, state);
        return RemoteUseResult.SUCCESS;
    }

    public void activate(NbtCompound nbt, ServerPlayerEntity player, World world, ServerWorld targetWorld, boolean sessionActive, boolean interdimensional, boolean differentSyncedId, BlockPos pos, BlockState state) {
        // Players can remove an activated remote from their inventory, and this will stop inventoryTick calls,
        // but that's purely visual and the valuable ticking happens in the entity mixin.
        if (remoteType.unlimited() || !sessionActive) {
            if (!remoteType.unlimited()) {
                nbt.putInt("Accesses", nbt.getInt("Accesses") - 1);
            }
            nbt.putInt("VisualTicks", remoteType.unlimited() ? STATUS_TICKS : ACCESS_TICKS);
            nbt.putInt("CustomModelData", 1);
        }
        // Replace synced id/text in case this target is different from the original one calibrated. Rare but possible
        if (differentSyncedId) {
            syncBlock(nbt, state.getBlock());
        }
        playSound(CASoundEvents.REMOTE_OPEN, 1.0f, player, world);
        triggerRemoteUsed(player, targetWorld, pos, interdimensional);
        player.incrementStat(ACCESS);
    }

    public void fail(NbtCompound nbt, boolean eraseInfo, ServerPlayerEntity player, World world) {
        // If the remote needs to be recalibrated anyways
        if (eraseInfo) {
            nbt.remove("SyncedPos");
            nbt.remove("SyncedWorld");
            nbt.remove("SyncedId");
            nbt.remove("SyncedText");
            nbt.remove("Accesses");
            if (nbt.contains("Session")) {
                UUID session = nbt.getUuid("Session");
                nbt.remove("Session");
                RemoteUser.removeSession(player, session);
            }
        }
        nbt.putInt("VisualTicks", STATUS_TICKS);
        nbt.putInt("CustomModelData", 2);
        player.getItemCooldownManager().set(this, 40);
        playSound(CASoundEvents.REMOTE_FAIL, 1.0f, player, world);
    }

    public static void playSound(SoundEvent event, float pitch, PlayerEntity player, World world) {
        world.playSound(player, player.getX(), player.getY(), player.getZ(), event, SoundCategory.PLAYERS, 1.0f, pitch);
        player.playSound(event, SoundCategory.MASTER, 1.0f, pitch);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (!stack.hasNbt()) {
            return;
        }
        handleVisualTicks(stack.getOrCreateNbt());
    }

    public static void handleVisualTicks(NbtCompound nbt) {
        if (!nbt.contains("VisualTicks")) {
            return;
        }
        int ticks = nbt.getInt("VisualTicks");
        ticks--;
        if (ticks <= 0) {
            nbt.remove("VisualTicks");
            nbt.remove("CustomModelData");
            return;
        }
        nbt.putInt("VisualTicks", ticks);
    }

    @Override
    public boolean allowNbtUpdateAnimation(PlayerEntity player, Hand hand, ItemStack oldStack, ItemStack newStack) {
        return false;
    }

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        return !remoteType.unlimited() && stack.hasNbt() && stack.getOrCreateNbt().contains("SyncedPos");
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        if (!stack.hasNbt()) {
            return 0;
        }
        NbtCompound nbt = stack.getOrCreateNbt();
        if (!nbt.contains("Accesses")) {
            return 0;
        }
        return (int) (((double) nbt.getInt("Accesses") / remoteType.accesses()) * 13.0);
    }

    // TODO: pulse with sin when being used
    @Override
    public int getItemBarColor(ItemStack stack) {
        return 6743789;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        if (context.isAdvanced() && stack.hasNbt()) {
            NbtCompound nbt = stack.getOrCreateNbt();
            if (nbt.contains("SyncedText")) {
                Text text = Text.Serializer.fromJson(nbt.getString("SyncedText"));
                tooltip.add(Text.translatable("tooltip.calibrated.synced_name", text).formatted(Formatting.GRAY));
            }
            if (nbt.contains("Accesses")) {
                MutableText accesses = Text.translatable("tooltip.calibrated.accesses", nbt.getInt("Accesses"), remoteType.accesses());
                tooltip.add(accesses.formatted(Formatting.GRAY));
            }
        }
        super.appendTooltip(stack, world, tooltip, context);
    }

    public static void registerStat(Identifier id) {
        Registry.register(Registry.CUSTOM_STAT, id, id);
        Stats.CUSTOM.getOrCreateStat(id);
    }

    public static void registerStats() {
        registerStat(ACCESS);
        registerStat(CREATE_SESSION);
    }
}

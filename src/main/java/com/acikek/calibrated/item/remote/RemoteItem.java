package com.acikek.calibrated.item.remote;

import com.acikek.calibrated.sound.ModSoundEvents;
import com.acikek.calibrated.util.AccessTicker;
import com.acikek.calibrated.util.RemoteUser;
import net.fabricmc.fabric.api.item.v1.FabricItem;
import net.minecraft.block.BlockState;
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
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

import java.util.UUID;

public class RemoteItem extends Item implements FabricItem {

    public enum UseResult {

        SUCCESS(false),
        CANNOT_ACCESS(true),
        INVALID_SESSION(true),
        INVALID_WORLD(true),
        DESYNC(true);

        public final Text message;

        UseResult(boolean error) {
            message = !error ? null
                    : Text.translatable("error.calibrated." + name().toLowerCase())
                            .formatted(Formatting.RED);
        }
    }

    public static final int ACCESS_TICKS = 15 * 20;
    public static final int STATUS_TICKS = 3 * 20;

    public RemoteType remoteType;

    public RemoteItem(Settings settings, RemoteType remoteType) {
        super(settings);
        this.remoteType = remoteType;
    }

    public static NamedScreenHandlerFactory getScreen(World world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof NamedScreenHandlerFactory screen) {
            return screen;
        }
        return world.getBlockState(pos).createScreenHandlerFactory(world, pos);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        BlockPos pos = context.getBlockPos();
        NamedScreenHandlerFactory screen = getScreen(context.getWorld(), pos);
        if (screen != null) {
            NbtCompound nbt = context.getStack().getOrCreateNbt();
            calibrate(nbt, context.getPlayer(), context.getWorld(), pos, context.getWorld().getBlockState(pos));
            return ActionResult.SUCCESS;
        }
        return super.useOnBlock(context);
    }

    public void calibrate(NbtCompound nbt, PlayerEntity player, World world, BlockPos pos, BlockState state) {
        nbt.putLong("SyncedPos", pos.asLong());
        nbt.putString("SyncedWorld", world.getRegistryKey().getValue().toString());
        nbt.putString("SyncedNameKey", state.getBlock().asItem().getTranslationKey());
        UUID session = UUID.randomUUID();
        nbt.putUuid("Session", session);
        ((RemoteUser) player).setSession(session);
        if (!remoteType.unlimited()) {
            nbt.putInt("Accesses", remoteType.accesses());
        }
        playSound(ModSoundEvents.REMOTE_SYNC, 0.85f + world.random.nextFloat() * 0.3f, player, world);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        // Run on server to validate world and guarantee proper block entity fetching.
        if (!world.isClient()) {
            NbtCompound nbt = stack.getOrCreateNbt();
            boolean isDifferentRemote = isDifferentRemote(nbt, user);
            UseResult result = use(nbt, isDifferentRemote, world, user);
            if (result != UseResult.SUCCESS) {
                fail(nbt, result == UseResult.DESYNC, isDifferentRemote, user, world);
                user.sendMessage(result.message, true);
            }
            return TypedActionResult.pass(stack);
        }
        return super.use(world, user, hand);
    }

    public static boolean isDifferentRemote(NbtCompound nbt, PlayerEntity player) {
        RemoteUser remoteUser = (RemoteUser) player;
        return remoteUser.isUsingRemote() && !remoteUser.getUsingSession().equals(nbt.getUuid("Session"));
    }

    public boolean canTryAccess(NbtCompound nbt) {
        return nbt.contains("SyncedPos") && (remoteType.unlimited() || nbt.getInt("Accesses") >= 1);
    }

    public UseResult use(NbtCompound nbt, boolean isDifferentRemote, World world, PlayerEntity player) {
        if (!canTryAccess(nbt)) {
            return UseResult.CANNOT_ACCESS;
        }
        // Prevents a player from using more than one remote at once
        RemoteUser remoteUser = (RemoteUser) player;
        if (!remoteUser.hasSession() || !remoteUser.getSession().equals(nbt.getUuid("Session"))) {
            return UseResult.INVALID_SESSION;
        }
        // Prevents interdimensional accesses for remotes that do not have this ability
        Identifier worldId = new Identifier(nbt.getString("SyncedWorld"));
        if (!world.getRegistryKey().getValue().equals(worldId) && !remoteType.interdimensional()) {
            return UseResult.INVALID_WORLD;
        }
        // Prevents accesses from invalid worlds
        ServerWorld targetWorld = ((ServerWorld) world).getServer().getWorld(RegistryKey.of(Registry.WORLD_KEY, worldId));
        if (targetWorld == null) {
            return UseResult.INVALID_WORLD;
        }
        ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
        // Prevents accesses to non-screen blocks (if they have been broken)
        // This is considered irreversible and will desync the remote
        BlockPos pos = BlockPos.fromLong(nbt.getLong("SyncedPos"));
        NamedScreenHandlerFactory screen = getScreen(targetWorld, pos);
        if (screen != null) {
            activate(nbt, isDifferentRemote, serverPlayer, world, screen, pos);
            return UseResult.SUCCESS;
        }
        return UseResult.DESYNC;
    }

    public void activate(NbtCompound nbt, boolean isDifferentRemote, ServerPlayerEntity player, World world, NamedScreenHandlerFactory screen, BlockPos pos) {
        RemoteUser remoteUser = ((RemoteUser) player);
        if (!remoteUser.isUsingRemote() || isDifferentRemote) {
            RemoteUser.setUsingRemote(player, pos, remoteUser.getSession());
        }
        player.openHandledScreen(screen);
        AccessTicker accessPlayer = ((AccessTicker) player);
        // Players can remove an activated remote from their inventory, and this will stop inventoryTick calls,
        // but that's purely visual and the valuable ticking happens in the server player mixin.
        if (remoteType.unlimited() || !accessPlayer.isAccessing() || isDifferentRemote) {
            if (!remoteType.unlimited()) {
                nbt.putInt("Accesses", nbt.getInt("Accesses") - 1);
                accessPlayer.setAccessTicks(ACCESS_TICKS);
            }
            nbt.putInt("VisualTicks", remoteType.unlimited() ? STATUS_TICKS : ACCESS_TICKS);
            nbt.putInt("CustomModelData", 1);
        }
        playSound(ModSoundEvents.REMOTE_OPEN, 1.0f, player, world);
    }

    public void fail(NbtCompound nbt, boolean desync, boolean isDifferentRemote, PlayerEntity player, World world) {
        if (desync) {
            nbt.remove("SyncedPos");
            nbt.remove("SyncedWorld");
            nbt.remove("SyncedNameKey");
            nbt.remove("Session");
            ((RemoteUser) player).setSession(null);
        }
        nbt.putInt("VisualTicks", STATUS_TICKS);
        nbt.putInt("CustomModelData", 2);
        if (!isDifferentRemote && player instanceof ServerPlayerEntity serverPlayer) {
            RemoteUser.setUsingRemote(serverPlayer, null, null);
        }
        player.getItemCooldownManager().set(this, 40);
        playSound(ModSoundEvents.REMOTE_FAIL, 1.0f, player, world);
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
        if (ticks == 0) {
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
        return !remoteType.unlimited() && stack.hasNbt() && canTryAccess(stack.getOrCreateNbt());
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
}

package com.acikek.calibrated.item;

import com.acikek.calibrated.network.CalibratedAccessNetworking;
import com.acikek.calibrated.util.RemoteAccessPlayer;
import com.acikek.calibrated.util.RemoteScreenPlayer;
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
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

public class RemoteItem extends Item implements FabricItem {

    public enum UseResult {
        SUCCESS,
        FAIL,
        DESYNC
    }

    public static final int ACCESS_TICKS = 15 * 20;
    public static final int STATUS_TICKS = 3 * 20;

    public int accesses;
    public boolean interdimensional;
    public boolean unlimited;

    public RemoteItem(Settings settings, int accesses, boolean interdimensional, boolean unlimited) {
        super(settings);
        this.accesses = accesses;
        this.interdimensional = interdimensional;
        this.unlimited = unlimited;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        BlockPos pos = context.getBlockPos();
        if (context.getWorld().getBlockEntity(pos) instanceof NamedScreenHandlerFactory) {
            NbtCompound nbt = context.getStack().getOrCreateNbt();
            calibrate(nbt, context.getWorld(), pos, context.getWorld().getBlockState(pos));
            return ActionResult.SUCCESS;
        }
        return super.useOnBlock(context);
    }

    public void calibrate(NbtCompound nbt, World world, BlockPos pos, BlockState state) {
        nbt.putLong("SyncedPos", pos.asLong());
        nbt.putString("SyncedWorld", world.getRegistryKey().getValue().toString());
        nbt.putString("SyncedNameKey", state.getBlock().asItem().getTranslationKey());
        if (!unlimited) {
            nbt.putInt("Accesses", accesses);
        }
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (stack.hasNbt()) {
            NbtCompound nbt = stack.getOrCreateNbt();
            UseResult result = use(world, user, nbt);
            if (result != UseResult.SUCCESS) {
                fail(nbt, result == UseResult.DESYNC);
            }
            return TypedActionResult.pass(stack);
        }
        return super.use(world, user, hand);
    }

    public boolean canTryAccess(NbtCompound nbt) {
        return nbt.contains("SyncedPos") && (unlimited || nbt.getInt("Accesses") >= 1);
    }

    public UseResult use(World world, PlayerEntity player, NbtCompound nbt) {
        // Run on server to validate world and guarantee proper block entity fetching.
        if (world.isClient() || !canTryAccess(nbt)) {
            return UseResult.FAIL;
        }
        Identifier worldId = new Identifier(nbt.getString("SyncedWorld"));
        if (!world.getRegistryKey().getValue().equals(worldId) && !interdimensional) {
            return UseResult.FAIL;
        }
        ServerWorld targetWorld = ((ServerWorld) world).getServer().getWorld(RegistryKey.of(Registry.WORLD_KEY, worldId));
        if (targetWorld == null) {
            return UseResult.FAIL;
        }
        ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
        BlockPos pos = BlockPos.fromLong(nbt.getLong("SyncedPos"));
        if (targetWorld.getBlockEntity(pos) instanceof NamedScreenHandlerFactory screen) {
            activate(serverPlayer, screen, pos, nbt);
            return UseResult.SUCCESS;
        }
        return UseResult.DESYNC;
    }

    // TODO handle unlimited
    public void activate(ServerPlayerEntity player, NamedScreenHandlerFactory screen, BlockPos pos, NbtCompound nbt) {
        RemoteScreenPlayer screenPlayer = ((RemoteScreenPlayer) player);
        if (!screenPlayer.isUsingRemote()) {
            screenPlayer.setUsingRemote(pos);
            CalibratedAccessNetworking.s2cSetUsingRemote(player, pos);
        }
        player.openHandledScreen(screen);
        RemoteAccessPlayer accessPlayer = ((RemoteAccessPlayer) player);
        if (!unlimited && !accessPlayer.isAccessing()) {
            nbt.putInt("Accesses", nbt.getInt("Accesses") - 1);
            accessPlayer.setAccessTicks(ACCESS_TICKS);
            nbt.putInt("VisualTicks", ACCESS_TICKS);
        }
    }

    public static void fail(NbtCompound nbt, boolean desync) {
        if (desync) {
            nbt.remove("SyncedPos");
            nbt.remove("SyncedWorld");
            nbt.remove("SyncedNameKey");
        }
        nbt.putInt("VisualTicks", STATUS_TICKS);
        nbt.putInt("CustomModelData", 2);
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
        return false; /*oldStack.hasNbt() && newStack.hasNbt()
                && oldStack.getOrCreateNbt().getInt("AccessingTicks") == newStack.getOrCreateNbt().getInt("AccessingTicks");*/
    }
}

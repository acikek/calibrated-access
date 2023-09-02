package com.acikek.calibrated.api;

import com.acikek.calibrated.api.event.RemoteAccessed;
import com.acikek.calibrated.api.event.RemoteUseResults;
import com.acikek.calibrated.api.impl.CalibratedAccessAPIImpl;
import com.acikek.calibrated.api.session.SessionView;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * <h1>Calibrated Access</h1>
 *
 * <h2>Overview</h2>
 *
 * <p>
 * This is the main API class for <a href="https://github.com/acikek/calibrated-access">Calibrated Access</a>.
 * It provides utility methods for registering and handling listeners of player using remote items to access blocks.
 * </p>
 * <br>
 *
 * <p>
 * The public {@code event} API package contains:
 * <ul>
 *     <li>{@link RemoteAccessed} interface, which acts as a listener callback for the remote access event</li>
 *     <li>{@link RemoteUseResults} utility class for passing success or fail states to the aforementioned event via a listener</li>
 * </ul>
 * </p>
 *
 * <h2>Example Usage</h2>
 *
 * <pre>
 * {@code
 * CalibratedAccessAPI.registerListener(Blocks.GOLD_BLOCK, (world, player, pos, state, remote, remoteStack) -> {
 *      player.sendMessage(Text.literal("Ooh, shiny!"));
 *      return RemoteUseResults.success();
 * });
 * }
 * </pre>
 */
public class CalibratedAccessAPI {

    /**
     * Registers a listener that only applies if the accessed block passes the specified predicate test.
     * @param predicate the predicate to test the block against
     */
    public static void registerListener(Predicate<Block> predicate, Identifier phase, RemoteAccessed listener) {
        CalibratedAccessAPIImpl.registerListener(predicate, phase, listener);
    }

    /**
     * @see CalibratedAccessAPI#registerListener(Predicate, Identifier, RemoteAccessed)
     */
    public static void registerListener(Predicate<Block> predicate, RemoteAccessed listener) {
        registerListener(predicate, Event.DEFAULT_PHASE, listener);
    }

    /**
     * Registers a listener that applies when the accessed block is listed within a specified block tag.
     * @param tag the block tag to match against
     */
    public static void registerListener(TagKey<Block> tag, Identifier phase, RemoteAccessed listener) {
        registerListener(block -> block.getRegistryEntry().isIn(tag), phase, listener);
    }

    /**
     * @see CalibratedAccessAPI#registerListener(TagKey, Identifier, RemoteAccessed)
     */
    public static void registerListener(TagKey<Block> tag, RemoteAccessed listener) {
        registerListener(tag, Event.DEFAULT_PHASE, listener);
    }

    /**
     * Registers a listener that applies to any of the specified blocks.
     * @param blocks the collection of blocks to match against
     */
    public static void registerListener(Collection<Block> blocks, Identifier phase, RemoteAccessed listener) {
        registerListener(blocks::contains, phase, listener);
    }

    /**
     * @see CalibratedAccessAPI#registerListener(Collection, Identifier, RemoteAccessed)
     */
    public static void registerListener(Collection<Block> blocks, RemoteAccessed listener) {
        registerListener(blocks, Event.DEFAULT_PHASE, listener);
    }

    /**
     * Registers a listener that applies to one specific block being accessed.
     * @param block the single block to test against
     */
    public static void registerListener(Block block, Identifier phase, RemoteAccessed listener) {
        registerListener(Collections.singletonList(block), phase, listener);
    }

    /**
     * @see CalibratedAccessAPI#registerListener(Block, Identifier, RemoteAccessed)
     */
    public static void registerListener(Block block, RemoteAccessed listener) {
        registerListener(block, Event.DEFAULT_PHASE, listener);
    }

    /**
     * @return whether the specified block matches a registered {@link RemoteAccessed} listener
     */
    public static boolean hasListener(Block block) {
        return CalibratedAccessAPIImpl.hasListener(block);
    }

    /**
     * @return an <b>immutable</b> map of a player's session IDs to their {@link SessionView}s.
     * If the player has no sessions loaded, returns an empty map.
     */
    public static @NotNull Map<UUID, SessionView> getSessions(PlayerEntity player) {
        return CalibratedAccessAPIImpl.getSessions(player);
    }

    /**
     * @return whether a player has the specified session loaded.
     * @see CalibratedAccessAPI#getSession(PlayerEntity, UUID)
     * @see CalibratedAccessAPI#getSessions(PlayerEntity)
     */
    public static boolean hasSession(PlayerEntity player, UUID session) {
        return getSessions(player).containsKey(session);
    }

    /**
     * @return an immutable view of a player's specified session data.
     * If the player doesn't have this session loaded, returns {@code null}.
     * @see CalibratedAccessAPI#hasSession(PlayerEntity, UUID)
     * @see CalibratedAccessAPI#getSessions(PlayerEntity)
     */
    public static @Nullable SessionView getSession(PlayerEntity player, UUID session) {
        return getSessions(player).get(session);
    }
}

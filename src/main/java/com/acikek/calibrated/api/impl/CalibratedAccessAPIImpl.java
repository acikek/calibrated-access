package com.acikek.calibrated.api.impl;

import com.acikek.calibrated.api.event.RemoteAccessed;
import com.acikek.calibrated.api.event.RemoteUseResults;
import com.acikek.calibrated.api.session.SessionView;
import com.acikek.calibrated.item.remote.RemoteUseResult;
import com.acikek.calibrated.util.RemoteUser;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CalibratedAccessAPIImpl {

    private static final Map<Predicate<Block>, RemoteAccessed> BLOCK_LISTENERS = new HashMap<>();

    private static List<RemoteAccessed> getListenersForBlock(Block block) {
        return BLOCK_LISTENERS.entrySet().stream()
                .filter(pair -> pair.getKey().test(block))
                .map(Map.Entry::getValue)
                .toList();
    }

    public static final Event<RemoteAccessed> REMOTE_ACCESSED = EventFactory.createArrayBacked(RemoteAccessed.class,
            listeners -> (world, player, pos, state, remote, remoteStack) -> {
                List<RemoteAccessed> common = new ArrayList<>(getListenersForBlock(state.getBlock()));
                common.retainAll(Arrays.stream(listeners).toList());
                for (RemoteAccessed listener : common) {
                    RemoteUseResult result = listener.onRemoteAccessed(world, player, pos, state, remote, remoteStack);
                    if (result.isError()) {
                        return result;
                    }
                }
                return common.isEmpty() ? null : RemoteUseResults.SUCCESS;
            });

    public static void registerListener(Predicate<Block> predicate, Identifier phase, RemoteAccessed listener) {
        BLOCK_LISTENERS.putIfAbsent(predicate, listener);
        REMOTE_ACCESSED.register(phase, listener);
    }

    public static boolean hasListener(Block block) {
        for (var pair : BLOCK_LISTENERS.entrySet()) {
            if (pair.getKey().test(block)) {
                return true;
            }
        }
        return false;
    }

    public static Map<UUID, SessionView> getSessions(PlayerEntity player) {
        var user = ((RemoteUser) player);
        if (!user.calibrated$hasSessions()) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(user.calibrated$getSessions());
    }
}

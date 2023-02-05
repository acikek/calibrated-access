package com.acikek.calibrated.api.impl;

import com.acikek.calibrated.api.event.RemoteAccessed;
import com.acikek.calibrated.api.event.RemoteUseResults;
import com.acikek.calibrated.item.remote.RemoteUseResult;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.block.Block;

import java.util.*;

public class CalibratedAccessAPIImpl {

    public static Map<Block, List<RemoteAccessed>> blockListeners = new HashMap<>();

    public static final Event<RemoteAccessed> REMOTE_ACCESSED = EventFactory.createArrayBacked(RemoteAccessed.class,
            listeners -> (world, player, pos, state, remote, remoteStack) -> {
                if (!blockListeners.containsKey(state.getBlock())) {
                    return null;
                }
                List<RemoteAccessed> common = new ArrayList<>(blockListeners.get(state.getBlock()));
                common.retainAll(Arrays.stream(listeners).toList());
                for (RemoteAccessed listener : common) {
                    RemoteUseResult result = listener.onRemoteAccessed(world, player, pos, state, remote, remoteStack);
                    if (result.isError()) {
                        return result;
                    }
                }
                return common.isEmpty() ? null : RemoteUseResults.success();
            });

    public static void registerListener(Block block, RemoteAccessed listener) {
        if (blockListeners.containsKey(block)) {
            blockListeners.get(block).add(listener);
            return;
        }
        blockListeners.put(block, new ArrayList<>(List.of(listener)));
        REMOTE_ACCESSED.register(listener);
    }
}

package com.acikek.calibrated.api;

import com.acikek.calibrated.api.event.RemoteAccessed;
import com.acikek.calibrated.api.event.RemoteUseResults;
import com.acikek.calibrated.api.impl.CalibratedAccessAPIImpl;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.block.Block;
import net.minecraft.util.Identifier;

public class CalibratedAccessAPI {

    /**
     * To register a listener for a specified block, use {@link CalibratedAccessAPI#registerListener(Block, Identifier, RemoteAccessed)}.
     * @return the event associated with {@link RemoteAccessed}
     */
    public static Event<RemoteAccessed> getRemoteAccessedEvent() {
        return CalibratedAccessAPIImpl.REMOTE_ACCESSED;
    }

    /**
     * Registers a listener for a specific block being accessed.
     * Use {@link RemoteUseResults} to return the proper using result from the listener.
     */
    public static void registerListener(Block block, Identifier phase, RemoteAccessed listener) {
        CalibratedAccessAPIImpl.registerListener(block, phase, listener);
    }

    /**
     * @see CalibratedAccessAPI#registerListener(Block, Identifier, RemoteAccessed)
     */
    public static void registerListener(Block block, RemoteAccessed listener) {
        registerListener(block, Event.DEFAULT_PHASE, listener);
    }
}

package com.acikek.calibrated.api;

import com.acikek.calibrated.api.event.RemoteAccessed;
import com.acikek.calibrated.api.impl.CalibratedAccessAPIImpl;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.block.Block;
import net.minecraft.util.Identifier;

public class CalibratedAccessAPI {

    /**
     * Registers a listener for a specific block being accessed.
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

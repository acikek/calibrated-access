package com.acikek.calibrated.api;

import com.acikek.calibrated.api.event.RemoteAccessed;
import com.acikek.calibrated.api.impl.CalibratedAccessAPIImpl;
import net.minecraft.block.Block;

public class CalibratedAccessAPI {

    public static void registerListener(Block block, RemoteAccessed listener) {
        CalibratedAccessAPIImpl.registerListener(block, listener);
    }
}

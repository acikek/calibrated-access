package com.acikek.calibrated.client;

import com.acikek.calibrated.network.CalibratedAccessNetworking;
import net.fabricmc.api.ClientModInitializer;

public class CalibratedAccessClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        CalibratedAccessNetworking.registerClient();
    }
}

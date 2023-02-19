package com.acikek.calibrated.client;

import com.acikek.calibrated.network.CANetworking;
import net.fabricmc.api.ClientModInitializer;

public class CalibratedAccessClient implements ClientModInitializer {

    public static boolean allowAccess;
    public static int maxSessions;

    @Override
    public void onInitializeClient() {
        CANetworking.registerClient();
    }
}

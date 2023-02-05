package com.acikek.calibrated.api.event;

import com.acikek.calibrated.item.remote.RemoteUseResult;

public class RemoteUseResults {

    public static RemoteUseResult success() {
        return new RemoteUseResult(null, false, false);
    }

    public static RemoteUseResult softError(String name) {
        return new RemoteUseResult(name, true, false);
    }

    public static RemoteUseResult error(String name) {
        return new RemoteUseResult(name, true, true);
    }
}

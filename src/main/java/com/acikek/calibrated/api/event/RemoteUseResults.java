package com.acikek.calibrated.api.event;

import com.acikek.calibrated.item.remote.RemoteUseResult;

public class RemoteUseResults {

    /**
     * @return a success result. The activation will proceed with access calculations and visual effects.
     */
    public static RemoteUseResult success() {
        return new RemoteUseResult(null, false, false);
    }

    /**
     * @return an error that stops the activation but does not erase any of the calibration data.
     */
    public static RemoteUseResult softError(String name) {
        return new RemoteUseResult(name, true, false);
    }

    /**
     * @return an error that stops the activation and erases the calibrated data
     */
    public static RemoteUseResult error(String name) {
        return new RemoteUseResult(name, true, true);
    }
}

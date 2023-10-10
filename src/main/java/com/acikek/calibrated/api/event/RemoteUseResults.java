package com.acikek.calibrated.api.event;

import com.acikek.calibrated.gamerule.CAGameRules;
import com.acikek.calibrated.item.remote.RemoteUseResult;

public class RemoteUseResults {

    /**
     * A success result. The activation will proceed with access calculations and visual effects.
     */
    public static final RemoteUseResult SUCCESS = new RemoteUseResult(null, false, false);
    /**
     * The remote cannot synchronize to the target block.
     */
    public static final RemoteUseResult CANNOT_SYNC = RemoteUseResults.error("cannot_sync");
    /**
     * The remote cannot access its synced block.
     */
    public static final RemoteUseResult CANNOT_ACCESS = RemoteUseResults.error("cannot_access");
    /**
     * The player's bound sessions are not aligned with the remote.<br>
     * This occurs when a player attempts to use a remote synced by another player.
     */
    public static final RemoteUseResult INVALID_SESSION = RemoteUseResults.error("invalid_session");
    /**
     * The remote is incapable of accessing the target block interdimensionally.
     */
    public static final RemoteUseResult INVALID_WORLD = RemoteUseResults.softError("invalid_world");
    /**
     * The target position changed blocks from when it was synced.<br>
     * This only errors when {@link CAGameRules#ALLOW_ID_MISMATCH} is {@code false}.
     */
    public static final RemoteUseResult INVALID_ID = RemoteUseResults.softError("invalid_id");
    /**
     * Generic error; something went wrong!
     */
    public static final RemoteUseResult DESYNC = RemoteUseResults.error("desync");

    /**
     * @deprecated in favor of {@link RemoteUseResults#SUCCESS}
     */
    @Deprecated
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

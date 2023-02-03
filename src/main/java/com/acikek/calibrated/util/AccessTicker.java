package com.acikek.calibrated.util;

import java.util.UUID;

public interface AccessTicker {

    void setAccessTicks(UUID session, int accessTicks);

    int getAccessTicks(UUID session);

    default boolean isAccessing(UUID session) {
        return getAccessTicks(session) != 0;
    }
}

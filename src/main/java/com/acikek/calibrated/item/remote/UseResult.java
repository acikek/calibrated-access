package com.acikek.calibrated.item.remote;

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public enum UseResult {

    SUCCESS(false, false),
    CANNOT_ACCESS(true, true),
    INVALID_SESSION(true, true),
    INVALID_WORLD(true, false),
    INVALID_ID(true, false),
    DESYNC(true, true);

    public final Text message;
    public final boolean eraseInfo;

    UseResult(boolean error, boolean eraseInfo) {
        message = !error ? null
                : Text.translatable("error.calibrated." + name().toLowerCase())
                        .formatted(Formatting.RED);
        this.eraseInfo = eraseInfo;
    }
}

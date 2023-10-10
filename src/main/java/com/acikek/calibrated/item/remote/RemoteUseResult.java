package com.acikek.calibrated.item.remote;

import com.acikek.calibrated.api.event.RemoteUseResults;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public record RemoteUseResult(String errorKey, boolean isError, boolean eraseInfo)  {

    public Text getErrorMessage() {
        return Text.translatable("error.calibrated." + errorKey).formatted(Formatting.RED);
    }
}

package com.acikek.calibrated.item.remote;

import com.acikek.calibrated.api.event.RemoteUseResults;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

public record RemoteUseResult(String errorKey, boolean isError, boolean eraseInfo)  {

    public static final RemoteUseResult SUCCESS = RemoteUseResults.success();
    public static final RemoteUseResult CANNOT_ACCESS = RemoteUseResults.error("cannot_access");
    public static final RemoteUseResult INVALID_SESSION = RemoteUseResults.error("invalid_session");
    public static final RemoteUseResult INVALID_WORLD = RemoteUseResults.softError("invalid_world");
    public static final RemoteUseResult INVALID_ID = RemoteUseResults.softError("invalid_id");
    public static final RemoteUseResult DESYNC = RemoteUseResults.error("desync");

    public Text getErrorMessage() {
        return new TranslatableText("error.calibrated." + errorKey).formatted(Formatting.RED);
    }
}

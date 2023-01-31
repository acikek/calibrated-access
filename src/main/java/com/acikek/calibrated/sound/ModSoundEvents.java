package com.acikek.calibrated.sound;

import com.acikek.calibrated.CalibratedAccess;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.List;

public class ModSoundEvents {

    public static List<SoundEvent> sounds = new ArrayList<>();

    public static final SoundEvent REMOTE_SYNC = create("item.remote_accessor.sync");
    public static final SoundEvent REMOTE_OPEN = create("item.remote_accessor.open");
    public static final SoundEvent REMOTE_FAIL = create("item.remote_accessor.fail");

    public static SoundEvent create(String name) {
        SoundEvent event = new SoundEvent(CalibratedAccess.id(name));
        sounds.add(event);
        return event;
    }

    public static void register() {
        for (SoundEvent event : sounds) {
            Registry.register(Registry.SOUND_EVENT, event.getId(), event);
        }
    }
}

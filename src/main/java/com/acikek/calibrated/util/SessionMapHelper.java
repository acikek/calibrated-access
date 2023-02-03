package com.acikek.calibrated.util;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class SessionMapHelper {

    public static <T> NbtList toNbt(Map<UUID, T> map, BiConsumer<NbtCompound, T> writer) {
        NbtList sessionEntries = new NbtList();
        for (Map.Entry<UUID, T> entry : map.entrySet()) {
            NbtCompound entryNbt = new NbtCompound();
            entryNbt.putUuid("Session", entry.getKey());
            writer.accept(entryNbt, entry.getValue());
            sessionEntries.add(entryNbt);
        }
        return sessionEntries;
    }

    public static <T> Map<UUID, T> readNbt(NbtCompound nbt, String key, Function<NbtCompound, T> reader) {
        NbtList sessions = nbt.getList(key, NbtElement.COMPOUND_TYPE);
        Map<UUID, T> result = new HashMap<>();
        for (int i = 0; i < sessions.size(); i++) {
            NbtCompound entry = sessions.getCompound(i);
            result.put(entry.getUuid("Session"), reader.apply(entry));
        }
        return result;
    }
}

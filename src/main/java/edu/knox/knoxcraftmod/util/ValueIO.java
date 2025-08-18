package edu.knox.knoxcraftmod.util;

import java.util.Optional;

import com.mojang.serialization.Codec;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Shim class because Forge doesn't rename the official 
 * Minecraft method names.
 */
public final class ValueIO {
    private ValueIO() {}

    public static <T> void store(ValueOutput out, String key, Codec<T> codec, T value) {
        out.store(key, codec, value);
    }

    public static <T> Optional<T> read(ValueInput in, String key, Codec<T> codec) {
        return in.read(key, codec);
    }
}


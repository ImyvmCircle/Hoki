package com.imyvm.hoki.nbt;

import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;

public class NbtEnumValue<T extends Enum<T>> implements NbtPersistent {
    private final Class<T> type;
    private T value;

    public NbtEnumValue(Class<T> type) {
        this.type = type;
    }

    @Override
    @Nullable
    public Tag serialize() {
        return this.value == null ? null : StringTag.valueOf(this.value.toString());
    }

    @Override
    public void deserialize(@Nullable Tag element) {
        this.value = element == null ? null : Enum.valueOf(this.type, ((StringTag) element).value());
    }

    public T get() {
        return this.value;
    }

    public void set(T value) {
        this.value = value;
    }
}

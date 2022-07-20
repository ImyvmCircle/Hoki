package com.imyvm.hoki.nbt;

import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtString;
import org.jetbrains.annotations.Nullable;

public class NbtEnumValue<T extends Enum<T>> implements NbtPersistent {
    private final Class<T> type;
    private T value;

    public NbtEnumValue(Class<T> type) {
        this.type = type;
    }

    @Override
    @Nullable
    public NbtElement serialize() {
        return this.value == null ? null : NbtString.of(this.value.toString());
    }

    @Override
    public void deserialize(@Nullable NbtElement element) {
        this.value = element == null ? null : Enum.valueOf(this.type, element.asString());
    }

    public T get() {
        return this.value;
    }

    public void set(T value) {
        this.value = value;
    }
}

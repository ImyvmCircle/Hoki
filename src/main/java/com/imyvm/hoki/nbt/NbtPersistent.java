package com.imyvm.hoki.nbt;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import org.jetbrains.annotations.Nullable;

public interface NbtPersistent {
    @Nullable
    default NbtElement serialize() {
        return NbtPersistentHelper.serialize(this);
    }

    default void deserialize(@Nullable NbtElement element) {
        if (element != null) {
            assert element instanceof NbtCompound;
            NbtPersistentHelper.deserialize(this, (NbtCompound) element);
        }
    }
}

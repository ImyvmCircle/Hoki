package com.imyvm.hoki.nbt;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;

public interface NbtPersistent {
    @Nullable
    default Tag serialize() {
        return NbtPersistentHelper.serialize(this);
    }

    default void deserialize(@Nullable Tag element) {
        if (element != null) {
            assert element instanceof CompoundTag;
            NbtPersistentHelper.deserialize(this, (CompoundTag) element);
        }
    }
}

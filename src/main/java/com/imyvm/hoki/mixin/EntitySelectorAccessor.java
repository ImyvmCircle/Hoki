package com.imyvm.hoki.mixin;

import net.minecraft.commands.arguments.selector.EntitySelector;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntitySelector.class)
public interface EntitySelectorAccessor {
    @Accessor
    @Nullable
    String getPlayerName();
}

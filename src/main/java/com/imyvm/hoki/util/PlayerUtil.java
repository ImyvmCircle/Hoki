package com.imyvm.hoki.util;

import com.imyvm.hoki.mixin.EntitySelectorAccessor;
import com.imyvm.hoki.mixin.MinecraftServerAccessor;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.server.MinecraftServer;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class PlayerUtil {
    private static MinecraftServer SERVER;

    private PlayerUtil() {
    }

    @Nullable
    public static GameProfile lookupOfflinePlayerFromArgument(@NotNull CommandContext<CommandSourceStack> context,
                                                              @NotNull String name) {
        EntitySelector selector = context.getArgument(name, EntitySelector.class);
        String playerName = ((EntitySelectorAccessor) selector).getPlayerName();
        if (playerName == null)
            return null;

        try {
            return lookupOfflinePlayer(playerName).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public static CompletableFuture<GameProfile> lookupOfflinePlayer(@NotNull String name) {
        return CompletableFuture.supplyAsync(() -> {
            Optional<GameProfile> profile = ((MinecraftServerAccessor) SERVER).getServices()
                .profileResolver().fetchByName(name);
            return profile.orElse(null);
        });
    }

    public static void initialize(MinecraftServer server) {
        SERVER = server;
    }
}

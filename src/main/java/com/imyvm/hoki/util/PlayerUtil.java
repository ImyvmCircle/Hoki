package com.imyvm.hoki.util;

import com.imyvm.hoki.mixin.EntitySelectorAccessor;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.ProfileLookupCallback;
import com.mojang.authlib.yggdrasil.ProfileNotFoundException;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.EntitySelector;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class PlayerUtil {
    private static MinecraftServer SERVER;

    private PlayerUtil() {
    }

    @Nullable
    public static GameProfile lookupOfflinePlayerFromArgument(@NotNull CommandContext<ServerCommandSource> context,
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
        CompletableFuture<GameProfile> future = new CompletableFuture<>();

        SERVER.getGameProfileRepo().findProfilesByNames(new String[]{name},
            new ProfileLookupCallback() {
                @Override
                public void onProfileLookupSucceeded(GameProfile profile) {
                    future.complete(profile);
                }

                @Override
                public void onProfileLookupFailed(String profileName, Exception exception) {
                    if (exception instanceof ProfileNotFoundException)
                        future.complete(null);
                    else
                        future.completeExceptionally(exception);
                }
            });

        return future;
    }

    public static void initialize(MinecraftServer server) {
        SERVER = server;
    }
}

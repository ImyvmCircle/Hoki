package com.imyvm.hoki.i18n;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public abstract class HokiLanguage {
    private static final Gson GSON = new Gson();
    private static final HokiLanguage EMPTY = new HokiLanguage() {
        @Override
        public @NotNull String get(@NotNull String key) {
            return key;
        }

        @Override
        public boolean hasTranslation(@NotNull String key) {
            return false;
        }
    };

    @NotNull
    public static String getResourcePath(@NotNull String modId, @NotNull String languageId) {
        return String.format("/assets/%s/lang/%s.json", modId, languageId);
    }

    @NotNull
    public static HokiLanguage create(@Nullable InputStream inputStream) {
        if (inputStream == null)
            return EMPTY;

        ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();

        JsonObject json = GSON.fromJson(new InputStreamReader(inputStream, StandardCharsets.UTF_8), JsonObject.class);
        json.entrySet().forEach((entry) -> builder.put(entry.getKey(), entry.getValue().getAsString()));

        ImmutableMap<String, String> map = builder.build();
        return new HokiLanguage() {
            @Override
            @NotNull
            public String get(@NotNull String key) {
                return Objects.requireNonNullElse(map.get(key), key);
            }

            @Override
            public boolean hasTranslation(@NotNull String key) {
                return map.containsKey(key);
            }
        };
    }

    @NotNull
    public abstract String get(@NotNull String key);

    public abstract boolean hasTranslation(@NotNull String key);
}

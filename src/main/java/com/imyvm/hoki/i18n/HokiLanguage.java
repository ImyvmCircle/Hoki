package com.imyvm.hoki.i18n;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public abstract class HokiLanguage {
    private static final Gson GSON = new Gson();

    public static String getResourcePath(String modId, String languageId) {
        return String.format("/assets/%s/lang/%s.json", modId, languageId);
    }

    public static HokiLanguage create(InputStream inputStream) {
        ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();

        JsonObject json = GSON.fromJson(new InputStreamReader(inputStream, StandardCharsets.UTF_8), JsonObject.class);
        json.entrySet().forEach((entry) -> builder.put(entry.getKey(), entry.getValue().getAsString()));

        ImmutableMap<String, String> map = builder.build();
        return new HokiLanguage() {
            @Override
            public String get(String key) {
                return Objects.requireNonNullElse(map.get(key), key);
            }

            @Override
            public boolean hasTranslation(String key) {
                return map.containsKey(key);
            }
        };
    }

    public abstract String get(String key);

    public abstract boolean hasTranslation(String key);
}

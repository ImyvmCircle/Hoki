package com.imyvm.hoki.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigRenderOptions;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.stream.Stream;

public abstract class HokiConfig {
    private final String filename;
    private final File configFile;

    public HokiConfig(@NotNull String filename) {
        this.filename = filename;
        this.configFile = FabricLoader.getInstance().getConfigDir().resolve(this.filename).toFile();
    }

    public void loadAndSave() {
        boolean hasMissing = this.load();
        if (hasMissing)
            this.save();
    }

    public boolean load() {
        Config config = ConfigFactory.parseFile(this.configFile);

        return this.iterateOptions()
            .map(option -> !option.loadFromConfig(config))
            .reduce(false, (lhs, rhs) -> lhs || rhs);
    }

    public void save() {
        Config config = ConfigFactory.empty();
        for (Option<?> option : this.iterateOptions().toList())
            config = option.saveToConfig(config);

        ConfigRenderOptions options = ConfigRenderOptions.defaults().setJson(false).setOriginComments(false);
        try (PrintWriter writer = new PrintWriter(this.configFile)) {
            writer.write(config.root().render(options));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private Stream<Option<?>> iterateOptions() {
        return Arrays.stream(this.getClass().getDeclaredFields())
            .filter(field -> field.isAnnotationPresent(ConfigOption.class))
            .map(field -> {
                try {
                    return (Option<?>) field.get(this);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            });
    }
}

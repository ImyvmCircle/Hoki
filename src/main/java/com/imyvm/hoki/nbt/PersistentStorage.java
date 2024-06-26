package com.imyvm.hoki.nbt;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtSizeTracker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static com.imyvm.hoki.HokiMod.LOGGER;

public class PersistentStorage<T extends NbtPersistent> {
    private final Path basePath;
    private final Map<UUID, T> loadedData = new ConcurrentHashMap<>();
    private final Function<UUID, T> defaultConstructor;

    public PersistentStorage(String directoryName, Function<UUID, T> defaultConstructor) {
        this.basePath = getBasePath(directoryName);
        this.defaultConstructor = defaultConstructor;

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> this.saveAll());
    }

    public void saveAll() {
        this.loadedData.forEach((uuid, data) -> {
            NbtCompound nbt = (NbtCompound) data.serialize();
            Path path = this.getDataFile(uuid);

            try {
                NbtIo.writeCompressed(nbt, path);
            } catch (IOException e) {
                LOGGER.error("Error while saving data", e);
            }
        });
    }

    public T getOrCreate(UUID uuid) {
        T data;
        if ((data = this.loadedData.get(uuid)) != null)
            return data;

        data = this.loadDataFromDisk(uuid);
        if (data == null)
            data = this.defaultConstructor.apply(uuid);
        this.loadedData.put(uuid, data);

        return data;
    }

    private T loadDataFromDisk(UUID uuid) {
        Path path = this.getDataFile(uuid);
        if (!path.toFile().exists())
            return null;

        T data = this.defaultConstructor.apply(uuid);
        try {
            NbtCompound nbt = NbtIo.readCompressed(path, NbtSizeTracker.ofUnlimitedBytes());
            data.deserialize(nbt);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return data;
    }

    private Path getDataFile(UUID uuid) {
        return this.basePath.resolve(uuid.toString() + ".dat");
    }

    private static Path getBasePath(String directoryName) {
        try {
            Path basePath = FabricLoader.getInstance().getGameDir().resolve("world").resolve(directoryName);
            return Files.createDirectories(basePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

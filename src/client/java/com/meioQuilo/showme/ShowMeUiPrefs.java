package com.meioQuilo.showme;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ShowMeUiPrefs {
    public boolean compactMode = false;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "showme_ui.json";

    public static ShowMeUiPrefs load() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
        try {
            if (Files.exists(path)) {
                return GSON.fromJson(Files.readString(path), ShowMeUiPrefs.class);
            }
        } catch (Exception ignored) {}
        return new ShowMeUiPrefs();
    }

    public static void save(ShowMeUiPrefs prefs) {
        Path path = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, GSON.toJson(prefs));
        } catch (IOException ignored) {}
    }
}
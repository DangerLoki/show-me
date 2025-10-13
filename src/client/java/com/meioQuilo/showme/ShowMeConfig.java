package com.meioQuilo.showme;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ShowMeConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("show-me.json");

    // Posição customizada do HUD (relativa à tela) e flag para usar
    public boolean useCustomHudPos = false;
    public float hudPosXPct = 0.02f; // 2% da largura disponível
    public float hudPosYPct = 0.02f; // 2% da altura disponível

    // Enum de posição agora com CUSTOM
    public enum TogglePosition {
        // ...existing code...
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT,
        CUSTOM // novo
    }

    // Campo já existente
    public TogglePosition togglePosition = TogglePosition.TOP_LEFT;

    // Configurações do HUD
    public boolean showFps = true;
    public boolean showCoords = false;
    public boolean showBiome = false;
    public boolean showPing = false;
    public boolean showTime = false;
    public boolean showBrightness = false;
    public boolean showDays = false;
    public boolean showDebug = false;
    public boolean showMemory = false;
    public boolean showClock = false;
    public boolean showSeed = false;

    // Informações da GPU
    public String gpuName;
    public double gpuVram;

    public static ShowMeConfig load() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                String json = Files.readString(CONFIG_PATH);
                return GSON.fromJson(json, ShowMeConfig.class);
            } catch (IOException e) {
                System.err.println("Erro ao carregar configuração do Show Me: " + e.getMessage());
            }
        }
        return new ShowMeConfig();
    }

    public static void save(ShowMeConfig config) {
        // Implementação básica para salvar configuração
        try {
            String json = GSON.toJson(config);
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, json);
        } catch (IOException e) {
            System.err.println("Erro ao salvar configuração do Show Me: " + e.getMessage());
        }
    }
}

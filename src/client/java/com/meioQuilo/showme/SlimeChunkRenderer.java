package com.meioQuilo.showme;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.List;

public class SlimeChunkRenderer {
    
    public static void renderSlimeChunkInfo(DrawContext context, List<String> lines) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) return;
        
        double playerX = mc.player.getX();
        double playerY = mc.player.getY();
        double playerZ = mc.player.getZ();
        
        int chunkX = (int) Math.floor(playerX) >> 4;
        int chunkZ = (int) Math.floor(playerZ) >> 4;
        
        // Obter a seed real do mundo
        long seed = 0;
        boolean hasSeed = false;
        
        if (mc.getServer() != null) {
            // Singleplayer - temos acesso à seed real
            seed = mc.getServer().getOverworld().getSeed();
            hasSeed = true;
        } else {
            // Multiplayer - não temos acesso à seed
            lines.add("§c✗ Seed indisponível (MP)");
            return;
        }
        
        if (!hasSeed) {
            lines.add("§c✗ Seed não disponível");
            return;
        }
        
        SlimeChunkDetector.SlimeChunkType type = SlimeChunkDetector.getSlimeChunkType(
            seed, playerX, playerY, playerZ
        );
        
        // Renderizar apenas a informação básica do chunk atual
        String baseText = String.format("Chunk (%d,%d): ", chunkX, chunkZ);
        String statusText;
        String colorCode = "";
        String symbol = "";
        
        switch (type) {
            case NOT_SLIME:
                colorCode = "§c";
                symbol = "✗ ";
                statusText = "Normal";
                break;
            case SLIME_WRONG_HEIGHT:
                colorCode = "§6";
                symbol = "⚠ ";
                statusText = "Slime (Y≥40)";
                break;
            case SLIME_VALID:
                colorCode = "§a";
                symbol = "✓ ";
                statusText = "Slime Farm!";
                break;
            default:
                colorCode = "§7";
                symbol = "? ";
                statusText = "Desconhecido";
        }
        lines.add(colorCode + symbol + baseText + statusText);
    }
}
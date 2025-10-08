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
            // Mostrar uma mensagem indicando isso
            lines.add("Chunk: Seed desconhecida (MP)");
            return;
        }
        
        if (!hasSeed) {
            lines.add("Chunk: Seed não disponível");
            return;
        }
        
        SlimeChunkDetector.SlimeChunkType type = SlimeChunkDetector.getSlimeChunkType(
            seed, playerX, playerY, playerZ
        );
        
        String text;
        switch (type) {
            case NOT_SLIME:
                text = String.format("Chunk (%d,%d): Não é Slime", chunkX, chunkZ);
                break;
            case SLIME_WRONG_HEIGHT:
                text = String.format("Chunk (%d,%d): Slime (Y≥40)", chunkX, chunkZ);
                break;
            case SLIME_VALID:
                text = String.format("Chunk (%d,%d): Slime (Y<40)", chunkX, chunkZ);
                break;
            default:
                return;
        }
        
        lines.add(text);
        
        // Adicionar informação da seed para debug (apenas em singleplayer)
        if (mc.getServer() != null) {
            lines.add(String.format("Seed: %d", seed));
        }
    }
}
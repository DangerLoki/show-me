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
        
        // Adicionar informação extra para slime chunks
        if (type != SlimeChunkDetector.SlimeChunkType.NOT_SLIME) {
            lines.add("⚠ Slime Chunk Detectado!");
            
            // Mostrar chunks adjacentes também
            String adjacentInfo = getAdjacentSlimeChunks(seed, chunkX, chunkZ, playerY);
            if (!adjacentInfo.isEmpty()) {
                lines.add(adjacentInfo);
            }
            
            // Adicionar informações úteis sobre slime spawn
            if (type == SlimeChunkDetector.SlimeChunkType.SLIME_VALID) {
                lines.add("✓ Altura ideal para slimes!");
            } else {
                lines.add("⚠ Desça para Y < 40 para slimes");
            }
        }
        
        // Mostrar mapa visual 3x3 dos chunks ao redor
        if (type != SlimeChunkDetector.SlimeChunkType.NOT_SLIME) {
            String mapInfo = getChunkMap(seed, chunkX, chunkZ, playerY);
            if (!mapInfo.isEmpty()) {
                lines.add("Mapa 3x3:");
                lines.add(mapInfo);
            }
        }
    }
    
    private static String getAdjacentSlimeChunks(long seed, int centerX, int centerZ, double playerY) {
        int slimeCount = 0;
        
        // Verificar chunks 3x3 ao redor
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue; // Pular o chunk central
                
                int chunkX = centerX + dx;
                int chunkZ = centerZ + dz;
                
                SlimeChunkDetector.SlimeChunkType type = SlimeChunkDetector.getSlimeChunkType(
                    seed, chunkX * 16 + 8, playerY, chunkZ * 16 + 8
                );
                
                if (type != SlimeChunkDetector.SlimeChunkType.NOT_SLIME) {
                    slimeCount++;
                }
            }
        }
        
        if (slimeCount > 0) {
            return String.format("Adjacentes: %d slime chunks", slimeCount);
        }
        
        return "";
    }
    
    /**
     * Cria um mapa visual 3x3 dos chunks ao redor
     */
    private static String getChunkMap(long seed, int centerX, int centerZ, double playerY) {
        StringBuilder map = new StringBuilder();
        
        for (int dz = -1; dz <= 1; dz++) {
            for (int dx = -1; dx <= 1; dx++) {
                int chunkX = centerX + dx;
                int chunkZ = centerZ + dz;
                
                SlimeChunkDetector.SlimeChunkType type = SlimeChunkDetector.getSlimeChunkType(
                    seed, chunkX * 16 + 8, playerY, chunkZ * 16 + 8
                );
                
                String symbol;
                if (dx == 0 && dz == 0) {
                    // Chunk atual
                    switch (type) {
                        case NOT_SLIME:
                            symbol = "[X]";
                            break;
                        case SLIME_WRONG_HEIGHT:
                            symbol = "[S]";
                            break;
                        case SLIME_VALID:
                            symbol = "[S]";
                            break;
                        default:
                            symbol = "[?]";
                    }
                } else {
                    // Chunks adjacentes
                    switch (type) {
                        case NOT_SLIME:
                            symbol = " . ";
                            break;
                        case SLIME_WRONG_HEIGHT:
                            symbol = " s ";
                            break;
                        case SLIME_VALID:
                            symbol = " S ";
                            break;
                        default:
                            symbol = " ? ";
                    }
                }
                
                map.append(symbol);
            }
            if (dz < 1) map.append(" ");
        }
        
        return map.toString();
    }
}
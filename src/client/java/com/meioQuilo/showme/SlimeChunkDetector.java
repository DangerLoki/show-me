package com.meioQuilo.showme;

import java.util.Random;

public class SlimeChunkDetector {
    
    /**
     * Verifica se um chunk é um slime chunk usando o algoritmo EXATO do Minecraft
     * Este é o mesmo algoritmo usado pelo Chunkbase e outros sites
     */
    public static boolean isSlimeChunk(long worldSeed, int chunkX, int chunkZ) {
        Random random = new Random(
            worldSeed +
            (long)(chunkX * chunkX * 0x4c1906) +
            (long)(chunkX * 0x5ac0db) +
            (long)(chunkZ * chunkZ) * 0x4307a7L +
            (long)(chunkZ * 0x5f24f) ^ 0x3ad76c0L
        );
        
        return random.nextInt(10) == 0;
    }
    
    /**
     * Verifica se a altura é ideal para spawn de slimes
     * Slimes spawnam em Y < 40 em slime chunks (swamplands têm regras diferentes)
     */
    public static boolean isValidSlimeHeight(double y) {
        return y < 40;
    }
    
    /**
     * Determina a cor do bloco baseado na posição
     */
    public static SlimeChunkType getSlimeChunkType(long worldSeed, double x, double y, double z) {
        int chunkX = (int) Math.floor(x) >> 4;
        int chunkZ = (int) Math.floor(z) >> 4;
        
        boolean isSlimeChunk = isSlimeChunk(worldSeed, chunkX, chunkZ);
        boolean validHeight = isValidSlimeHeight(y);
        
        if (!isSlimeChunk) {
            return SlimeChunkType.NOT_SLIME; // Vermelho
        } else if (!validHeight) {
            return SlimeChunkType.SLIME_WRONG_HEIGHT; // Azul
        } else {
            return SlimeChunkType.SLIME_VALID; // Verde
        }
    }
    
    public enum SlimeChunkType {
        NOT_SLIME,           // Vermelho
        SLIME_WRONG_HEIGHT,  // Azul
        SLIME_VALID          // Verde
    }
}
package com.meioQuilo.showme.components;

public class SlimeCalculator {

    public static java.util.Random seedSlimeChunk(int pChunkX, int pChunkZ, long pLevelSeed) {
        return new java.util.Random(
            pLevelSeed +
            (long)(pChunkX * pChunkX * 4987142) +
            (long)(pChunkX * 5947611) +
            (long)(pChunkZ * pChunkZ) * 4392871L +
            (long)(pChunkZ * 389711) ^
            (long)987234911L
        );
    }

    public static boolean isSlimeChunk(long worldSeed, int chunkX, int chunkZ) {
        return seedSlimeChunk(chunkX, chunkZ, worldSeed).nextInt(10) == 0;
    }

    public static boolean isSlimeChunkFromBlock(long worldSeed, int blockX, int blockZ) {
        int chunkX = Math.floorDiv(blockX, 16);
        int chunkZ = Math.floorDiv(blockZ, 16);
        return isSlimeChunk(worldSeed, chunkX, chunkZ);
    }
}
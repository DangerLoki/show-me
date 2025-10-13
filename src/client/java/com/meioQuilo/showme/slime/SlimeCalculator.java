package com.meioQuilo.showme.slime;

public class SlimeCalculator {
    
    public static class SlimeChunkInfo {
        public final int chunkX, chunkZ;
        public final double distance;
        public final String direction;
        
        public SlimeChunkInfo(int chunkX, int chunkZ, int originChunkX, int originChunkZ) {
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
            this.distance = Math.sqrt(Math.pow(chunkX - originChunkX, 2) + Math.pow(chunkZ - originChunkZ, 2));
            this.direction = getDirection(originChunkX, originChunkZ, chunkX, chunkZ);
        }
        
        private String getDirection(int fromX, int fromZ, int toX, int toZ) {
            int dx = toX - fromX;
            int dz = toZ - fromZ;
            if (Math.abs(dx) > Math.abs(dz)) {
                return dx > 0 ? "Leste" : "Oeste";
            } else {
                return dz > 0 ? "Sul" : "Norte";
            }
        }
    }

    public static boolean isSlimeChunk(long worldSeed, int chunkX, int chunkZ) {
        long x = chunkX;
        long z = chunkZ;
        long seed = worldSeed
            + x * x * 4987142L
            + x * 5947611L
            + z * z * 4392871L
            + z * 389711L;
        seed ^= 987234911L;
        java.util.Random rand = new java.util.Random(seed);
        return rand.nextInt(10) == 0;
    }

    public static boolean isSlimeChunkFromBlock(long worldSeed, int blockX, int blockZ) {
        int chunkX = Math.floorDiv(blockX, 16);
        int chunkZ = Math.floorDiv(blockZ, 16);
        return isSlimeChunk(worldSeed, chunkX, chunkZ);
    }

    public static void debugAround(long seed, int blockX, int blockZ, int radiusChunks) {
        int cx = Math.floorDiv(blockX, 16);
        int cz = Math.floorDiv(blockZ, 16);
        System.out.printf("Seed: %d | Block: (%d,%d) -> Chunk: (%d,%d)%n", seed, blockX, blockZ, cx, cz);
        for (int z = cz - radiusChunks; z <= cz + radiusChunks; z++) {
            for (int x = cx - radiusChunks; x <= cx + radiusChunks; x++) {
                boolean slime = isSlimeChunk(seed, x, z);
                System.out.printf("%s ", slime ? "s" : ".");
            }
            System.out.println();
        }
    }

    public static java.util.List<SlimeChunkInfo> findNearestSlimeChunks(long worldSeed, int blockX, int blockZ, int maxRadius) {
        java.util.List<SlimeChunkInfo> slimeChunks = new java.util.ArrayList<>();
        int originChunkX = Math.floorDiv(blockX, 16);
        int originChunkZ = Math.floorDiv(blockZ, 16);
        
        for (int radius = 1; radius <= maxRadius; radius++) {
            for (int x = originChunkX - radius; x <= originChunkX + radius; x++) {
                for (int z = originChunkZ - radius; z <= originChunkZ + radius; z++) {
                    if (Math.abs(x - originChunkX) == radius || Math.abs(z - originChunkZ) == radius) {
                        if (isSlimeChunk(worldSeed, x, z)) {
                            slimeChunks.add(new SlimeChunkInfo(x, z, originChunkX, originChunkZ));
                        }
                    }
                }
            }
            if (!slimeChunks.isEmpty()) break;
        }
        
        slimeChunks.sort((a, b) -> Double.compare(a.distance, b.distance));
        return slimeChunks;
    }

    public static String getSlimeRadarReport(long worldSeed, int blockX, int blockZ, int maxRadius) {
        java.util.List<SlimeChunkInfo> nearestSlimes = findNearestSlimeChunks(worldSeed, blockX, blockZ, maxRadius);
        
        if (nearestSlimes.isEmpty()) {
            return String.format("§cNenhum slime chunk encontrado em %d chunks de raio", maxRadius);
        }
        
        StringBuilder report = new StringBuilder();
        report.append(String.format("§aSlime chunks próximos (Posição: %d, %d):\n", blockX, blockZ));
        
        for (int i = 0; i < Math.min(3, nearestSlimes.size()); i++) {
            SlimeChunkInfo info = nearestSlimes.get(i);
            int blockStartX = info.chunkX * 16;
            int blockStartZ = info.chunkZ * 16;
            report.append(String.format("§e%d. Chunk (%d,%d) - Blocos (%d,%d) - %.1f chunks %s\n", 
                i + 1, info.chunkX, info.chunkZ, blockStartX, blockStartZ, info.distance, info.direction));
        }
        
        return report.toString();
    }

    public static void main(String[] args) {
        // Exemplo usando valores do jogo (quando disponíveis)
        // Em vez de valores hardcoded, você pode:
        
        // 1. Passar como parâmetros do método main
        if (args.length >= 3) {
            long seed = Long.parseLong(args[0]);
            int blockX = Integer.parseInt(args[1]);
            int blockZ = Integer.parseInt(args[2]);
            
            boolean result = isSlimeChunkFromBlock(seed, blockX, blockZ);
            System.out.println("Chunk alvo é slime? " + result);
            debugAround(seed, blockX, blockZ, 2);
            System.out.println(getSlimeRadarReport(seed, blockX, blockZ, 10));
        }
    }
}
package com.meioQuilo.showme.components;

public class CustomSeedStorage {
    private static Long customSeed = null;
    
    public static void setCustomSeed(long seed) {
        customSeed = seed;
    }
    
    public static void clearCustomSeed() {
        customSeed = null;
    }
    
    public static Long getCustomSeed() {
        return customSeed;
    }
    
    public static boolean hasCustomSeed() {
        return customSeed != null;
    }
}
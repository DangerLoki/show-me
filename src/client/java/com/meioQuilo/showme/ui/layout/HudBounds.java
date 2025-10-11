package com.meioQuilo.showme.ui.layout;

import com.meioQuilo.showme.ShowMeConfigScreen;

public class HudBounds {
    private final int screenWidth;
    private final int screenHeight;
    private final int hudWidth;
    private final int hudHeight;
    private final ShowMeConfigScreen.WorkingConfig config;
    
    public HudBounds(int screenWidth, int screenHeight, ShowMeConfigScreen.PreviewSize size, ShowMeConfigScreen.WorkingConfig config) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.hudWidth = size.maxWidth();
        this.hudHeight = size.totalHeight();
        this.config = config;
    }
    
    public boolean contains(int mouseX, int mouseY) {
        int hudX = getX();
        int hudY = getY();
        
        return mouseX >= hudX && mouseX <= hudX + hudWidth &&
               mouseY >= hudY && mouseY <= hudY + hudHeight;
    }
    
    public int getX() {
        int availW = getAvailableWidth();
        return Math.round(config.hudPosXPct * availW);
    }
    
    public int getY() {
        int availH = getAvailableHeight();
        return Math.round(config.hudPosYPct * availH);
    }
    
    public int getAvailableWidth() {
        return Math.max(0, screenWidth - hudWidth);
    }
    
    public int getAvailableHeight() {
        return Math.max(0, screenHeight - hudHeight);
    }
    
    public int getHudWidth() { return hudWidth; }
    public int getHudHeight() { return hudHeight; }
}
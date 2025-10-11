package com.meioQuilo.showme.ui.input;

import com.meioQuilo.showme.ShowMeConfigScreen;
import com.meioQuilo.showme.ui.layout.HudBounds;

public class HudDragHandler {
    private boolean dragging = false;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;
    
    public void handleInput(MouseInputHandler mouse, HudBounds hudBounds, ShowMeConfigScreen.WorkingConfig config) {
        boolean mouseOverHud = hudBounds.contains(mouse.getMouseX(), mouse.getMouseY());
        
        if (mouse.wasJustPressed() && mouseOverHud && !dragging) {
            // Inicia arrasto
            dragging = true;
            dragOffsetX = mouse.getMouseX() - hudBounds.getX();
            dragOffsetY = mouse.getMouseY() - hudBounds.getY();
        } 
        else if (mouse.wasJustReleased() && dragging) {
            // Termina arrasto
            dragging = false;
        } 
        else if (dragging && mouse.isPressed()) {
            // Atualiza posição durante arrasto
            updateHudPosition(mouse, config, hudBounds);
        }
    }
    
    private void updateHudPosition(MouseInputHandler mouse, ShowMeConfigScreen.WorkingConfig config, HudBounds bounds) {
        int newX = mouse.getMouseX() - dragOffsetX;
        int newY = mouse.getMouseY() - dragOffsetY;
        
        int availW = bounds.getAvailableWidth();
        int availH = bounds.getAvailableHeight();
        
        newX = Math.max(0, Math.min(newX, availW));
        newY = Math.max(0, Math.min(newY, availH));
        
        config.hudPosXPct = availW == 0 ? 0f : (float)newX / (float)availW;
        config.hudPosYPct = availH == 0 ? 0f : (float)newY / (float)availH;
    }
    
    public boolean isDragging() { return dragging; }
}
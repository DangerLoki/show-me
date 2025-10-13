package com.meioQuilo.showme.ui.input;

import org.lwjgl.glfw.GLFW;
import net.minecraft.client.MinecraftClient;

public class MouseInputHandler {
    private boolean leftButtonPressed = false;
    private boolean wasLeftButtonPressed = false;
    private int mouseX, mouseY;
    
    public void update(int currentMouseX, int currentMouseY) {
        this.mouseX = currentMouseX;
        this.mouseY = currentMouseY;
        
        this.wasLeftButtonPressed = this.leftButtonPressed;
        
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.getWindow() != null) {
            long window = client.getWindow().getHandle();
            this.leftButtonPressed = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        }
    }
    
    public boolean isPressed() { return leftButtonPressed; }
    public boolean wasJustPressed() { return leftButtonPressed && !wasLeftButtonPressed; }
    public boolean wasJustReleased() { return !leftButtonPressed && wasLeftButtonPressed; }
    public int getMouseX() { return mouseX; }
    public int getMouseY() { return mouseY; }
}
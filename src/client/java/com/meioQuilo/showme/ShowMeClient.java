// ShowMeClient.java
package com.meioQuilo.showme;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
// import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
// import net.fabricmc.fabric.api.client.rendering.v1.InGameHudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;

import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class ShowMeClient implements ClientModInitializer {
    public static ShowMeConfig CONFIG = ShowMeConfig.load();

    private static boolean hudEnabled = true;
    private static KeyBinding toggleHudKey;
    private static KeyBinding openMenuKey;

    // Categoria para os keybinds (use MISC para compilar em 1.21.x)
    private static final KeyBinding.Category SHOW_ME_CATEGORY = KeyBinding.Category.MISC;

    private static void toast(MinecraftClient mc, String msg) {
        if (mc != null && mc.player != null) {
            mc.player.sendMessage(Text.literal("[Show Me] " + msg), true);
        }
    }

    @Override
    public void onInitializeClient() {
        System.out.println("[ShowMe] Inicializando mod - registrando teclas...");

        toggleHudKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.showme.toggle_hud",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                SHOW_ME_CATEGORY));

        openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.showme.open_menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_Z,
                SHOW_ME_CATEGORY));

        System.out.println("[ShowMe] Teclas registradas: H (toggle HUD) e Z (menu)");

        HudElementRegistry.addLast(Identifier.of("show_me", "show_me_hud"), (drawContext, tickCounter) -> {
                renderHud(drawContext);
        });

        //if (!nativeLoaded) {
            //      * try {
            //      * ShowMeNativeLoader.loadNative();
            //      * nativeLoaded = true;
            //      * } catch (Exception e) {
            //      * e.printStackTrace();
            //      * }
            //      * }

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleHudKey.wasPressed()) {
                hudEnabled = !hudEnabled;
                toast(client, "HUD "
                        + (hudEnabled ? Text.translatable("key.hud.visible").getString()
                                : Text.translatable("key.hud.hidden").getString()));
            }
            while (openMenuKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new ShowMeConfigScreen(client.currentScreen));
                } else {
                    System.out.println("[ShowMe] Não pode abrir - já há uma tela aberta: "
                            + client.currentScreen.getClass().getSimpleName());
                }
            }
        });
    }

    private static void renderHud(DrawContext ctx) {
        if (!hudEnabled)
            return;
        final var mc = MinecraftClient.getInstance();
        if (mc == null || mc.player == null || mc.world == null)
            return;

        var font = mc.textRenderer;
        List<String> lines = new ArrayList<>();
        // List<String> debugLines = new ArrayList<>();

        if (CONFIG.showFps) {
            lines.add("FPS: " + mc.getCurrentFps());
        }
        if (CONFIG.showCoords) {
            BlockPos pos = mc.player.getBlockPos();
            lines.add(String.format("XYZ: %d / %d / %d", pos.getX(), pos.getY(), pos.getZ()));
        }
        if (CONFIG.showBrightness) {
            BlockPos pos = mc.player.getBlockPos();
            int blockLight = mc.world.getLightLevel(LightType.BLOCK, pos);
            int skyLight = mc.world.getLightLevel(LightType.SKY, pos);
            lines.add(Text.translatable("key.hud.brightness", blockLight, skyLight).getString());
        }

        if (CONFIG.showDays) {
            var days = mc.world.getTimeOfDay() / 24000L;
            lines.add(Text.translatable("key.hud.day", days).getString());
        }

        if (CONFIG.showClock) {
            long t = mc.world.getTimeOfDay();
            long tod = t % 24000L;
            int hour = (int) ((tod / 1000L + 6) % 24);
            int minute = (int) ((tod % 1000L) * 60 / 1000L);
            lines.add(Text.translatable("key.hud.time").append(String.format("%02d:%02d", hour, minute)).getString());
        }

        if (CONFIG.showBiome) {
            var biome = mc.world.getBiome(mc.player.getBlockPos())
                    .getKey()
                    .map(key -> key.getValue().toString())
                    .orElse("Desconhecido");
            lines.add(Text.translatable("key.hud.biome", biome).getString());
        }

        if (CONFIG.showPing) {
            var handler = mc.getNetworkHandler();
            if (handler != null) {
                var entry = handler.getPlayerListEntry(mc.player.getUuid());
                if (entry != null) {
                    int latency = entry.getLatency();
                    lines.add(Text.translatable("key.hud.ping", latency).getString());
                }
            }
        }

        if (CONFIG.showMemory) {
            Runtime rt = Runtime.getRuntime();
            long max = rt.maxMemory();
            long total = rt.totalMemory();
            long free = rt.freeMemory();
            long used = total - free;
            long usedMB = used / (1024 * 1024);
            long maxMB = max / (1024 * 1024);
            long percent = max > 0 ? (used * 100 / max) : 0;
            lines.add(Text.translatable("key.hud.memory", usedMB, maxMB, percent).getString());
        }

        // exibir seed do mundo
        if (CONFIG.showSeed) {
            String seedText = "???";
            if (mc.getServer() != null) { // singleplayer (integrated server)
                long seed = mc.getServer().getOverworld().getSeed();
                seedText = String.valueOf(seed);
            }
            lines.add(Text.translatable("key.hud.seed", seedText).getString());
        }

        if (lines.isEmpty())
            return;

        int width = ctx.getScaledWindowWidth();
        // debugLines.add(String.format("Window Width: %d", width));
        int height = ctx.getScaledWindowHeight();
        // debugLines.add(String.format("Window Height: %d", height));

        int paddingX = 4;
        int paddingY = 3;
        int lineSpacing = 2;
        int margin = 3;

        var chatHud = mc.inGameHud.getChatHud();
        int chatLines = chatHud.getVisibleLineCount();
        // debugLines.add(String.format("Chat Line Count: %d", chatLines));
        double chatScale = chatHud.getChatScale();
        // debugLines.add(String.format("Chat Scale: %f", chatScale));
        int chatHeight = (int) ((chatLines * font.fontHeight) * chatScale);
        // debugLines.add(String.format("Calculated ChatHeight: %d", chatHeight));

        // if (CONFIG.gpuName == null) {
        // CONFIG.gpuName = GpuMonitor.getName();
        // }

        // debugLines.add(CONFIG.gpuName);

        // if (CONFIG.showDebug) {
        // lines.addAll(debugLines);
        // }

        // Calcula tamanho do overlay
        int maxWidth = 0;
        for (String l : lines) {
            int w = font.getWidth(l);
            if (w > maxWidth)
                maxWidth = w;
        }

        int totalHeight = lines.size() * font.fontHeight + (lines.size() - 1) * lineSpacing;

        // Calcula posição inicial com margin
        // width/height: tamanho da janela
        // maxWidth: largura do bloco do HUD
        // totalHeight: altura do bloco do HUD
        // paddingX/paddingY/margin: já existentes no seu código
        int x, y;

        if (CONFIG.useCustomHudPos) {
            int availW = Math.max(0, width - maxWidth);
            int availH = Math.max(0, height - totalHeight);
            x = Math.round(CONFIG.hudPosXPct * availW);
            y = Math.round(CONFIG.hudPosYPct * availH);
        } else {
            switch (CONFIG.togglePosition) {
                case TOP_RIGHT:
                    x = width - maxWidth - paddingX - margin;
                    y = margin + paddingY;
                    break;
                case BOTTOM_RIGHT:
                    x = width - maxWidth - paddingX - margin;
                    y = height - totalHeight - paddingY - margin;
                    break;
                case BOTTOM_LEFT:
                    x = margin + paddingX;
                    y = height - totalHeight - chatHeight - 50 - paddingY - margin;
                    break;
                case CUSTOM: {
                    int availW = Math.max(0, width - maxWidth);
                    int availH = Math.max(0, height - totalHeight);
                    int cx = Math.round(CONFIG.hudPosXPct * availW);
                    int cy = Math.round(CONFIG.hudPosYPct * availH);
                    x = cx;
                    y = cy;
                    break;
                }
                case TOP_LEFT:
                default:
                    x = margin + paddingX;
                    y = margin + paddingY;
            }
        }

        // debugLines.add(String.format("Calculated y: %d", y));
        // Desenha background
        ctx.fill(
                x - paddingX,
                y - paddingY,
                x + maxWidth + paddingX,
                y + totalHeight + paddingY,
                0x88000000);

        // Desenha cada linha
        int drawY = y;
        for (String l : lines) {
            ctx.drawTextWithShadow(font, l, x, drawY, 0xFFFFFFFF);
            drawY += font.fontHeight + lineSpacing;
        }
    }
}

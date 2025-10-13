// ShowMeConfigScreen.java
package com.meioQuilo.showme;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ShowMeConfigScreen extends Screen {
    private final Screen parent;

    // Layout/scroll
    private int contentLeft, contentTop, contentWidth, contentBottom;
    private int scrollY = 0;
    private int contentHeight = 0;

    // Margem horizontal percentual
    private static final double H_MARGIN_PCT_NORMAL = 0.08; // 8% nas laterais (modo normal)
    private static final double H_MARGIN_PCT_COMPACT = 0.01; // 1% nas laterais (modo compacto)

    // Barra de rolagem
    private static final int SCROLLBAR_W = 8;
    private boolean draggingScrollbar = false;
    private int dragStartMouseY = 0;
    private int dragStartScrollY = 0;

    // Top bar (botões fixos)
    private static final int TOP_BAR_H = 24;
    private static final int TOP_BTN_MARGIN = 8;

    // Estado
    private final List<Section> sections = new ArrayList<>();
    private WorkingConfig draft;
    private ShowMeUiPrefs uiPrefs;
    private boolean compactMode = false;

    // Mover HUD
    private boolean moveHudMode = false;
    private boolean draggingHud = false;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;

    public ShowMeConfigScreen(Screen parent) {
        super(Text.literal("Show Me - Config"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        if (this.draft == null) this.draft = new WorkingConfig(ShowMeClient.CONFIG);

        if (this.uiPrefs == null) this.uiPrefs = ShowMeUiPrefs.load();
        this.compactMode = uiPrefs != null && uiPrefs.compactMode;

        recomputeLayout();
        buildSectionsIfEmpty();
        rebuildWidgets();
    }

    private void buildSectionsIfEmpty() {
        if (!sections.isEmpty()) return;

        var cfg = this.draft;

        Section world = new Section("key.category.world");
        world.addOption("key.option.showBrightness", () -> cfg.showBrightness, v -> cfg.showBrightness = v);
        world.addOption("key.option.showBiome", () -> cfg.showBiome, v -> cfg.showBiome = v);
        world.addOption("key.option.showDays", () -> cfg.showDays, v -> cfg.showDays = v);
        world.addOption("key.option.showClock", () -> cfg.showClock, v -> cfg.showClock = v);
        world.addOption("key.option.showCoordinates", () -> cfg.showCoords, v -> cfg.showCoords = v);
        world.addOption("key.option.showSeed", () -> cfg.showSeed, v -> cfg.showSeed = v);

        Section net = new Section("key.category.multiplayer");
        net.addOption("key.option.showPing", () -> cfg.showPing, v -> cfg.showPing = v);

        Section system = new Section("key.category.system");
        system.addOption("key.option.showFps", () -> cfg.showFps, v -> cfg.showFps = v);
        system.addOption("key.option.showMemory", () -> cfg.showMemory, v -> cfg.showMemory = v);

        Section debug = new Section("key.category.diagnosis");
        debug.addOption("key.option.showDebug", () -> cfg.showDebug, v -> cfg.showDebug = v);

        sections.add(world);
        sections.add(net);
        sections.add(system);
        sections.add(debug);
    }

    private void rebuildWidgets() {
        this.clearChildren();

        // Botão topo: COMPACTO (fixo no canto superior direito)
        int topBtnSize = 20;
        int compactBtnX = this.width - TOP_BTN_MARGIN - topBtnSize;
        int topBtnY = 4;
        this.addDrawableChild(
            ButtonWidget.builder(Text.literal(compactIcon()), b -> {
                        compactMode = !compactMode;
                        if (uiPrefs != null) {
                            uiPrefs.compactMode = compactMode;
                            ShowMeUiPrefs.save(uiPrefs);
                        }
                        recomputeLayout();
                        b.setMessage(Text.literal(compactIcon()));
                        rebuildWidgets();
                    })
                    .dimensions(compactBtnX, topBtnY, topBtnSize, topBtnSize)
                    .build()
        );

        // Botão topo: MOVER HUD (à esquerda do compacto)
        int moveBtnX = compactBtnX - 4 - topBtnSize;
        this.addDrawableChild(
            ButtonWidget.builder(Text.literal(moveIcon()), b -> {
                        moveHudMode = !moveHudMode;
                        if (moveHudMode) {
                            // garante que o render use a posição customizada
                            draft.useCustomHudPos = true;
                        }
                        b.setMessage(Text.literal(moveIcon()));
                        rebuildWidgets(); // Reconstrói para adicionar/remover widget de drag
                    })
                    .dimensions(moveBtnX, topBtnY, topBtnSize, topBtnSize)
                    .build()
        );

        // Widget invisível para arrastar o HUD (apenas quando moveHudMode está ativo)
        if (moveHudMode) {
            // Não precisamos de widget - o mouseClicked do Screen trata
        }

        int headerH = 20;
        int lineGap = 6;
        int scrollReserve = SCROLLBAR_W + 3;

        // yDraw: posição na tela (com scroll)
        int yDraw = contentTop - scrollY;
        // yMeasure: altura “natural” (sem scroll)
        int yMeasure = contentTop;

        for (Section s : sections) {
            String arrow = s.expanded ? "▾ " : "▸ ";
            Text headerLabel = Text.literal(arrow).append(Text.translatable(s.title));

            int headerMaxW = contentWidth - scrollReserve;
            int headerW = compactMode
                    ? Math.min(headerMaxW, this.textRenderer.getWidth(headerLabel.asOrderedText()) + 12)
                    : headerMaxW;

            var headerBtn = ButtonWidget.builder(headerLabel, b -> {
                        s.expanded = !s.expanded;
                        rebuildWidgets();
                    })
                    .dimensions(contentLeft, yDraw, headerW, headerH)
                    .build();
            this.addDrawableChild(headerBtn);

            yDraw += headerH + lineGap;
            yMeasure += headerH + lineGap;

            if (s.expanded) {
                // Flow layout
                int innerPad = 6, hGap = 6, vGap = 6, optH = 20;

                int xCur = contentLeft + innerPad;
                int maxX = contentLeft + contentWidth - scrollReserve;

                int yCurDraw = yDraw;
                int yCurMeasure = yMeasure;

                for (Option o : s.options) {
                    boolean checked = o.get.get();
                    Text labelText = Text.literal(checked ? "☑ " : "☐ ").append(Text.translatable(o.label));

                    int w = compactMode
                            ? this.textRenderer.getWidth(labelText.asOrderedText()) + 12
                            : Math.max(90, this.textRenderer.getWidth(labelText.asOrderedText()) + 18);

                    if (xCur + w > maxX) {
                        xCur = contentLeft + innerPad;
                        yCurDraw += optH + vGap;
                        yCurMeasure += optH + vGap;
                    }

                    ButtonWidget toggle = createToggleButton(xCur, yCurDraw, w, o);
                    this.addDrawableChild(toggle);

                    xCur += w + hGap;
                }

                yDraw = yCurDraw + optH + lineGap;
                yMeasure = yCurMeasure + optH + lineGap;
            }
        }

        // Botões Salvar/Cancelar
        int naturalButtonsY = yMeasure + 8;
        int naturalEndY = naturalButtonsY + 20 + 6;

        int availW = contentWidth - SCROLLBAR_W - 3;
        int saveW = 120, cancelW = 120, gap = 8;
        int total = saveW + gap + cancelW;
        int baseX = contentLeft + Math.max(0, (availW - total) / 2);

        int viewportBottomY = contentBottom - 24;
        int buttonsY = Math.max(naturalButtonsY - scrollY, viewportBottomY);

        this.addDrawableChild(
            ButtonWidget.builder(Text.translatable("key.save"), b -> saveAndClose())
                .dimensions(baseX, buttonsY, saveW, 20)
                .build()
        );
        this.addDrawableChild(
            ButtonWidget.builder(Text.translatable("key.cancel"), b -> cancelAndClose())
                .dimensions(baseX + saveW + gap, buttonsY, cancelW, 20)
                .build()
        );

        // Altura total do conteúdo (natural)
        contentHeight = Math.max(0, naturalEndY - contentTop);
        clampScroll();
    }

    private ButtonWidget createToggleButton(int x, int y, int width, Option o) {
        boolean checked = o.get.get();
        Text labelText = Text.literal(checked ? "☑ " : "☐ ").append(Text.translatable(o.label));

        return ButtonWidget.builder(labelText, b -> {
                    boolean nv = !o.get.get();
                    o.set.accept(nv);
                    Text newLabel = Text.literal(nv ? "☑ " : "☐ ").append(Text.translatable(o.label));
                    b.setMessage(newLabel);
                })
                .dimensions(x, y, width, 20)
                .build();
    }

    private void drawScrollbar(DrawContext ctx) {
        int viewport = contentBottom - contentTop;
        if (contentHeight <= viewport) return;

        int trackX1 = contentLeft + contentWidth - SCROLLBAR_W;
        int trackX2 = trackX1 + SCROLLBAR_W;
        ctx.fill(trackX1, contentTop, trackX2, contentBottom, 0x40000000);

        int maxScroll = Math.max(0, contentHeight - viewport);
        int trackH = viewport;
        int thumbH = Math.max(20, (int)((double)viewport * viewport / contentHeight));
        int thumbTravel = Math.max(1, trackH - thumbH);
        int thumbY = contentTop + (maxScroll == 0 ? 0 : (int)((double)scrollY / maxScroll * thumbTravel));
        ctx.fill(trackX1 + 1, thumbY, trackX2 - 1, thumbY + thumbH, draggingScrollbar ? 0xCCFFFFFF : 0xAAFFFFFF);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int viewport = contentBottom - contentTop;
        if (contentHeight > viewport) {
            scrollY -= (int) (verticalAmount * 16);
            clampScroll();
            rebuildWidgets();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // NOTA: Este método não é chamado no Minecraft 1.21.9 devido a mudanças na API.
        // A detecção de cliques foi movida para o método render() usando GLFW diretamente.
        
        // PRIORIDADE 1: Se estamos no modo mover HUD, verificar se o clique é na área do HUD
        // Mas SOMENTE se não estiver sobre nenhum botão
        if (moveHudMode && button == 0) {
            // Primeiro verifica se NÃO está sobre nenhum widget (botão)
            boolean overWidget = false;
            for (var child : this.children()) {
                if (child.isMouseOver(mouseX, mouseY)) {
                    overWidget = true;
                    System.out.println("[ShowMe DEBUG] Mouse está sobre um widget");
                    break;
                }
            }

            // Se não está sobre um widget, verifica se está sobre a prévia do HUD
            if (!overWidget) {
                PreviewSize size = computePreviewSize();
                int width = this.width;
                int height = this.height;

                int availW = Math.max(0, width - size.maxWidth);
                int availH = Math.max(0, height - size.totalHeight);

                int hudX = Math.round(draft.hudPosXPct * availW);
                int hudY = Math.round(draft.hudPosYPct * availH);

                System.out.println("[ShowMe DEBUG] HUD area: x=" + hudX + "-" + (hudX + size.maxWidth) + ", y=" + hudY + "-" + (hudY + size.totalHeight));

                if (mouseX >= hudX && mouseX <= hudX + size.maxWidth &&
                    mouseY >= hudY && mouseY <= hudY + size.totalHeight) {
                    draggingHud = true;
                    dragOffsetX = (int)mouseX - hudX;
                    dragOffsetY = (int)mouseY - hudY;
                    System.out.println("[ShowMe DEBUG] Iniciando drag do HUD!");
                    return true;
                }
            }
        }

        int viewport = contentBottom - contentTop;

        // PRIORIDADE 2: Clique na barra de rolagem
        if (button == 0 && contentHeight > viewport) {
            int trackX1 = contentLeft + contentWidth - SCROLLBAR_W;
            int trackX2 = trackX1 + SCROLLBAR_W;
            if (mouseX >= trackX1 && mouseX <= trackX2 && mouseY >= contentTop && mouseY <= contentBottom) {
                int maxScroll = Math.max(0, contentHeight - viewport);
                int thumbH = Math.max(20, (int)((double)viewport * viewport / contentHeight));
                int thumbTravel = Math.max(1, viewport - thumbH);
                double pos = Math.max(0, Math.min(mouseY - contentTop - thumbH / 2.0, thumbTravel));
                scrollY = (int)Math.round(pos / thumbTravel * maxScroll);
                clampScroll();
                rebuildWidgets();

                draggingScrollbar = true;
                dragStartMouseY = (int)mouseY;
                dragStartScrollY = scrollY;
                return true;
            }
        }

        return false;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        // NOTA: Este método não é chamado no Minecraft 1.21.9 devido a mudanças na API.
        
        // Arraste da barra de rolagem (mantido para compatibilidade)
        if (draggingScrollbar) {
            int viewport = contentBottom - contentTop;
            int maxScroll = Math.max(0, contentHeight - viewport);
            int thumbH = Math.max(20, (int)((double)viewport * viewport / contentHeight));
            int thumbTravel = Math.max(1, viewport - thumbH);

            int dy = (int)mouseY - dragStartMouseY;
            int newScroll = dragStartScrollY + (int)Math.round((double)dy / thumbTravel * maxScroll);
            scrollY = Math.max(0, Math.min(newScroll, maxScroll));
            clampScroll();
            rebuildWidgets();
            return true;
        }

        // Arraste do HUD (mantido para compatibilidade com versões antigas)
        if (moveHudMode && button == 0) {
            PreviewSize size = computePreviewSize();
            int width = this.width;
            int height = this.height;

            int availW = Math.max(0, width - size.maxWidth);
            int availH = Math.max(0, height - size.totalHeight);

            // Se ainda não está arrastando, verifica se está sobre o HUD
            if (!draggingHud) {
                int hudX = Math.round(draft.hudPosXPct * availW);
                int hudY = Math.round(draft.hudPosYPct * availH);
                
                // Verifica se o mouse está sobre a área do HUD
                if (mouseX >= hudX && mouseX <= hudX + size.maxWidth &&
                    mouseY >= hudY && mouseY <= hudY + size.totalHeight) {
                    // Inicia o drag
                    draggingHud = true;
                    dragOffsetX = (int)mouseX - hudX;
                    dragOffsetY = (int)mouseY - hudY;
                }
            }
            
            // Se está arrastando, atualiza a posição
            if (draggingHud) {
                int newX = (int)Math.round(mouseX) - dragOffsetX;
                int newY = (int)Math.round(mouseY) - dragOffsetY;

                newX = Math.max(0, Math.min(newX, availW));
                newY = Math.max(0, Math.min(newY, availH));

                draft.hudPosXPct = availW == 0 ? 0f : (float)newX / (float)availW;
                draft.hudPosYPct = availH == 0 ? 0f : (float)newY / (float)availH;

                return true;
            }
        }

        return false;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (draggingScrollbar && button == 0) {
            draggingScrollbar = false;
            return true;
        }
        if (draggingHud && button == 0) {
            draggingHud = false;
            return true;
        }
        return false;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // SOLUÇÃO PARA MINECRAFT 1.21.9:
        // Os métodos mouseClicked() e mouseDragged() não são mais chamados devido a mudanças na API.
        // Detectamos o estado do botão do mouse diretamente via GLFW durante o render.
        if (moveHudMode && this.client != null && this.client.getWindow() != null) {
            long window = this.client.getWindow().getHandle();
            boolean leftButtonPressed = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
            
            PreviewSize size = computePreviewSize();
            int availW = Math.max(0, this.width - size.maxWidth);
            int availH = Math.max(0, this.height - size.totalHeight);
            
            int hudX = Math.round(draft.hudPosXPct * availW);
            int hudY = Math.round(draft.hudPosYPct * availH);
            
            // Verifica se mouse está sobre o HUD
            boolean mouseOverHud = mouseX >= hudX && mouseX <= hudX + size.maxWidth &&
                                  mouseY >= hudY && mouseY <= hudY + size.totalHeight;
            
            if (leftButtonPressed && mouseOverHud && !draggingHud) {
                // Inicia arrasto
                draggingHud = true;
                dragOffsetX = mouseX - hudX;
                dragOffsetY = mouseY - hudY;
            } else if (!leftButtonPressed && draggingHud) {
                // Termina arrasto
                draggingHud = false;
            } else if (draggingHud && leftButtonPressed) {
                // Atualiza posição durante arrasto
                int newX = mouseX - dragOffsetX;
                int newY = mouseY - dragOffsetY;
                newX = Math.max(0, Math.min(newX, availW));
                newY = Math.max(0, Math.min(newY, availH));
                draft.hudPosXPct = availW == 0 ? 0f : (float)newX / (float)availW;
                draft.hudPosYPct = availH == 0 ? 0f : (float)newY / (float)availH;
            }
        }
        
        // Sem blur/fundo sólido para evitar crash
        ctx.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 6, 0xFFFFFF);

        super.render(ctx, mouseX, mouseY, delta);

        // Prévia e guia de arrasto do HUD
        if (moveHudMode) {
            PreviewSize size = computePreviewSize();
            int width = this.width;
            int height = this.height;

            int availW = Math.max(0, width - size.maxWidth);
            int availH = Math.max(0, height - size.totalHeight);

            int hudX = Math.round(draft.hudPosXPct * availW);
            int hudY = Math.round(draft.hudPosYPct * availH);

            int x1 = hudX, y1 = hudY;
            int x2 = hudX + size.maxWidth, y2 = hudY + size.totalHeight;

            // borda
            ctx.fill(x1 - 1, y1 - 1, x2 + 1, y1, 0xAAFFFFFF);
            ctx.fill(x1 - 1, y2, x2 + 1, y2 + 1, 0xAAFFFFFF);
            ctx.fill(x1 - 1, y1, x1, y2, 0xAAFFFFFF);
            ctx.fill(x2, y1, x2 + 1, y2, 0xAAFFFFFF);
            // preenchimento
            ctx.fill(x1, y1, x2, y2, 0x3300AAFF);

            // handle central
            int hx = (x1 + x2) / 2;
            int hy = (y1 + y2) / 2;
            ctx.fill(hx - 4, hy - 1, hx + 5, hy + 2, 0xCCFFFFFF);
            ctx.fill(hx - 1, hy - 4, hx + 2, hy + 5, 0xCCFFFFFF);
        }

        drawScrollbar(ctx);
    }

    @Override
    public void close() {
        cancelAndClose();
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Tecla X para fechar ou ESC
        if (keyCode == GLFW.GLFW_KEY_X || keyCode == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        return false;
    }

    private void saveAndClose() {
        draft.applyTo(ShowMeClient.CONFIG);
        try {
            ShowMeConfig.save(ShowMeClient.CONFIG);
        } catch (Exception ignored) {}
        MinecraftClient.getInstance().setScreen(parent);
    }

    private void cancelAndClose() {
        MinecraftClient.getInstance().setScreen(parent);
    }

    private void clampScroll() {
        int viewport = contentBottom - contentTop;
        int maxScroll = Math.max(0, contentHeight - viewport);
        if (scrollY < 0) scrollY = 0;
        if (scrollY > maxScroll) scrollY = maxScroll;
    }

    private PreviewSize computePreviewSize() {
        var mc = MinecraftClient.getInstance();
        var font = mc.textRenderer;

        List<String> lines = new ArrayList<>();
        if (draft.showFps) lines.add("FPS: 120");
        if (draft.showCoords) lines.add("XYZ: 123 / 64 / 456");
        if (draft.showBrightness) lines.add("Luz: 15/15");
        if (draft.showDays) lines.add("Dia: 12");
        if (draft.showClock) lines.add("Hora: 12:34");
        if (draft.showBiome) lines.add("Bioma: minecraft:plains");
        if (draft.showPing) lines.add("Ping: 42 ms");
        if (draft.showMemory) lines.add("Memória: 512/8192 MB");
        if (draft.showSeed) lines.add("Seed: 123456789");
        // if (draft.showDebug) {
        //     lines.add("Window Width: 1920");
        //     lines.add("Window Height: 1080");
        // }
        if (lines.isEmpty()) lines.add("HUD");

        int maxW = 0;
        for (String l : lines) maxW = Math.max(maxW, font.getWidth(l));
        int lineSpacing = 2;
        int totalH = lines.size() * font.fontHeight + (lines.size() - 1) * lineSpacing;

        return new PreviewSize(maxW + 8, totalH + 6);
    }

    private String compactIcon() {
        return compactMode ? "▥" : "▭";
    }

    private String moveIcon() {
        return moveHudMode ? "✥" : "⤧";
    }

    // ---------- modelos internos ----------
    private static class Section {
        final String title;
        boolean expanded = false;
        final List<Option> options = new ArrayList<>();
        Section(String title) { this.title = title; }
        void addOption(String label, Supplier<Boolean> get, Consumer<Boolean> set) {
            options.add(new Option(label, get, set));
        }
    }

    private record Option(String label, Supplier<Boolean> get, Consumer<Boolean> set) {}
    private record PreviewSize(int maxWidth, int totalHeight) {}

    private static class WorkingConfig {
        boolean showFps, showCoords, showClock, showDays, showBrightness, showBiome, showSeed, showPing, showMemory, showDebug;
        boolean useCustomHudPos;           // <— novo
        float hudPosXPct, hudPosYPct;

        WorkingConfig(ShowMeConfig src) {
            showFps = src.showFps;
            showCoords = src.showCoords;
            showClock = src.showClock;
            showDays = src.showDays;
            showBrightness = src.showBrightness;
            showBiome = src.showBiome;
            showSeed = src.showSeed;
            showPing = src.showPing;
            showMemory = src.showMemory;
            showDebug = src.showDebug;

            useCustomHudPos = src.useCustomHudPos;   // <— novo
            hudPosXPct = src.hudPosXPct;
            hudPosYPct = src.hudPosYPct;
        }
        void applyTo(ShowMeConfig dst) {
            dst.showFps = showFps;
            dst.showCoords = showCoords;
            dst.showClock = showClock;
            dst.showDays = showDays;
            dst.showBrightness = showBrightness;
            dst.showBiome = showBiome;
            dst.showSeed = showSeed;
            dst.showPing = showPing;
            dst.showMemory = showMemory;
            dst.showDebug = showDebug;

            dst.useCustomHudPos = useCustomHudPos;   // <— novo
            dst.hudPosXPct = hudPosXPct;
            dst.hudPosYPct = hudPosYPct;
        }
    }

    @Override
    public void resize(MinecraftClient mc, int width, int height) {
        super.resize(mc, width, height);
        recomputeLayout();
        clampScroll();
        rebuildWidgets();
    }

    // Recalcula margens/área útil conforme estado (compacto = 3%, normal = 8%)
    private void recomputeLayout() {
        int marginTop = 12;
        double pct = compactMode ? H_MARGIN_PCT_COMPACT : H_MARGIN_PCT_NORMAL;
        int marginH = Math.max(10, (int) (this.width * pct));

        contentLeft = marginH;
        contentTop = marginTop + TOP_BAR_H; 
        contentWidth = this.width - marginH * 2;
        contentBottom = this.height - marginTop - 24;
    }
}

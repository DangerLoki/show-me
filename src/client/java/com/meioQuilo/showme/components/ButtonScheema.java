package com.meioQuilo.showme.components;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ButtonScheema {
    // --- adicionar abaixo das variáveis ou em qualquer lugar dentro da classe ---

    /**
     * Interface usada por enums que querem expor um rótulo customizado
     * (ShowMeConfig.Position implementa isto).
     */
    public interface LabeledEnum {
        String label();
    }

    private final java.util.function.Function<ButtonWidget, ButtonWidget> adder;

    private int x;
    private int startY;
    private int width = 200;
    private int height = 20;
    private int spacing = 4;

    private int currentY;
    private boolean started = false;

    public ButtonScheema(java.util.function.Function<ButtonWidget, ButtonWidget> adder) {
        this.adder = adder;
    }

    public ButtonScheema setX(int x) {
        this.x = x;
        return this;
    }

    public ButtonScheema setY(int y) {
        this.startY = y;
        this.currentY = y;
        return this;
    }

    public ButtonScheema setWidth(int width) {
        this.width = width;
        return this;
    }

    public ButtonScheema setHeight(int height) {
        this.height = height;
        return this;
    }

    public ButtonScheema setSpacing(int spacing) {
        this.spacing = spacing;
        return this;
    }

    private void advance() {
        // avanço correto: altura + espaçamento
        this.currentY += height + spacing;
    }

    private int nextY() {
        if (!started) {
            started = true;
            currentY = startY;
            return currentY;
        }
        return currentY;
    }

    private ButtonWidget baseButton(Text msg, ButtonWidget.PressAction onPress) {
        int y = nextY();
        ButtonWidget w = ButtonWidget.builder(msg, b -> {
            onPress.onPress(b);
        }).dimensions(x - width / 2, y, width, height).build();
        adder.apply(w);
        advance();
        return w;
    }

    // Botão boolean (switch)
    public ButtonWidget newSwitchButton(String key,
                                        Supplier<Boolean> getter,
                                        Consumer<Boolean> setter) {

        ButtonWidget[] ref = new ButtonWidget[1];

        java.util.function.Supplier<Text> msg = () ->
                Text.translatable(key).append(": ").append(
                        Text.translatable(getter.get() ? "options.on" : "options.off"));

        ButtonWidget w = baseButton(msg.get(), b -> {
            setter.accept(!getter.get());
            b.setMessage(msg.get());
        });
        ref[0] = w;
        return w;
    }

    // Botão enum genérico (aqui específico para Position)
    public <E extends Enum<E>> ButtonWidget newToggleButton(String key,
                                                            Supplier<E> getter,
                                                            Consumer<E> setter) {
        java.util.function.Supplier<Text> msg = () -> {
            E val = getter.get();
            Text valueText = (val instanceof LabeledEnum)
                    ? Text.translatable(((LabeledEnum) val).label())
                    : Text.literal(formatEnum(val));
            return Text.translatable(key).append(": ").append(valueText);
        };

        return baseButton(msg.get(), b -> {
            E val = getter.get();
            E[] arr = val.getDeclaringClass().getEnumConstants();
            int next = (val.ordinal() + 1) % arr.length;
            setter.accept(arr[next]);
            b.setMessage(msg.get());
        });
    }

    private String formatEnum(Enum<?> e) {
        if (e instanceof LabeledEnum) {
            return ((LabeledEnum) e).label();
        }
        String n = e.name().toLowerCase().replace('_', ' ');
        return Character.toUpperCase(n.charAt(0)) + (n.length() > 1 ? n.substring(1) : "");
    }
}

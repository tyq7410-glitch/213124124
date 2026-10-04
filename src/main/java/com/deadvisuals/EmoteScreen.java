package com.deadvisuals;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class EmoteScreen extends Screen {
    public EmoteScreen() {
        super(Text.literal("emotes"));
    }

    @Override
    protected void init() {
        for (int i = 0; i < Emotes.NAMES.length; i++) {
            final int idx = i;
            int x = width / 2 - 148 + (i % 4) * 76;
            int y = height / 2 - 24 + (i / 4) * 26;
            addDrawableChild(ButtonWidget.builder(Text.literal(Emotes.NAMES[i]), b -> {
                Emotes.play(idx);
                close();
            }).dimensions(x, y, 70, 20).build());
        }
    }

    @Override
    public void render(DrawContext g, int mouseX, int mouseY, float delta) {
        super.render(g, mouseX, mouseY, delta);
        g.drawCenteredTextWithShadow(textRenderer, Text.literal("emotes"), width / 2, height / 2 - 44, 0xFFB3122A);
    }
}

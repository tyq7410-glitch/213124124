package com.deadvisuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public class DeadTitleScreen extends Screen {
    private static boolean introShown = false;
    private static final String[] LABELS = {"singleplayer", "multiplayer", "options", "visuals menu", "quit"};

    private final long born = System.nanoTime();
    private long lastNs = born;
    private final float[] hov = new float[LABELS.length];
    private boolean intro;

    public DeadTitleScreen() {
        super(Text.literal("dead visuals"));
        intro = Mods.TITLE.on("Intro") && !introShown;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private int bx() {
        return width / 2 - 85;
    }

    private int by(int i) {
        return height / 2 - 30 + i * 28;
    }

    @Override
    public void render(DrawContext g, int mx, int my, float delta) {
        long now = System.nanoTime();
        float t = (now - born) / 1e9f;
        float dt = Math.min(0.1f, (now - lastNs) / 1e9f);
        lastNs = now;
        int accent = 0xFF000000 | MathHelper.hsvToRgb((float) (Mods.TITLE.v("Accent hue") / 360.0), 0.9f, 0.75f);

        g.fill(0, 0, width, height, 0xFF08080A);
        for (int y = 0; y < height; y += 4) g.fill(0, y, width, y + 1, 0x08FFFFFF);

        int n = (int) Mods.TITLE.v("Particles");
        for (int i = 0; i < n; i++) {
            float sp = 10 + (i % 7) * 5;
            float px = ((i * 97) % Math.max(1, width) + t * sp * 0.4f) % width;
            float py = height - (((i * 53) % Math.max(1, height) + t * sp) % height);
            int sz = 1 + (i % 3 == 0 ? 1 : 0);
            int al = 40 + (i % 5) * 30;
            g.fill((int) px, (int) py, (int) px + sz, (int) py + sz, (al << 24) | (accent & 0xFFFFFF));
        }

        float pulse = 0.5f + 0.5f * (float) Math.sin(t * 2f);
        MatrixStack ms = g.getMatrices();
        ms.push();
        ms.translate(width / 2f, height / 2f - 78, 0f);
        float ls = 3f + 0.1f * pulse;
        ms.scale(ls, ls, 1f);
        g.drawCenteredTextWithShadow(textRenderer, Text.literal("DEAD VISUALS"), 0, 0, 0xFFFFFFFF);
        ms.pop();
        g.fill(width / 2 - 80, height / 2 - 50, width / 2 + 80, height / 2 - 49, accent);
        g.drawCenteredTextWithShadow(textRenderer, Text.literal("pvp visuals  |  1.21.4"), width / 2, height / 2 - 44, 0xFF707070);

        for (int i = 0; i < LABELS.length; i++) {
            int x = bx();
            int y = by(i);
            boolean h = !intro && mx >= x && mx < x + 170 && my >= y && my < y + 22;
            hov[i] += ((h ? 1f : 0f) - hov[i]) * Math.min(1f, dt * 14f);
            g.fill(x, y, x + 170, y + 22, 0xC0101010);
            g.fill(x, y, x + 2 + (int) (hov[i] * 8), y + 22, accent);
            g.drawTextWithShadow(textRenderer, LABELS[i], x + 14 + (int) (hov[i] * 4), y + 7,
                    Colors.lerp(0xFF8A8A8A, 0xFFFFFFFF, hov[i]));
        }
        g.drawCenteredTextWithShadow(textRenderer,
                Text.literal("right shift: menu   g: mark   h: clear marks   v: emotes"),
                width / 2, height - 14, 0xFF555555);

        if (intro) {
            if (t < 2.6f) {
                float a = t < 2.0f ? 1f : 1f - (t - 2.0f) / 0.6f;
                int al = Math.min(255, (int) (255 * a));
                if (al > 0) g.fill(0, 0, width, height, al << 24);
                if (al > 8) {
                    ms.push();
                    ms.translate(width / 2f, height / 2f - 10, 0f);
                    float is = 4f + 0.15f * pulse;
                    ms.scale(is, is, 1f);
                    g.drawCenteredTextWithShadow(textRenderer, Text.literal("DEAD VISUALS"), 0, 0, (al << 24) | 0xFFFFFF);
                    ms.pop();
                    int bw = 120;
                    int bx = width / 2 - bw / 2;
                    int byy = height / 2 + 30;
                    g.fill(bx, byy, bx + bw, byy + 2, (al << 24) | 0x2A2A2A);
                    g.fill(bx, byy, bx + (int) (bw * Math.min(1f, t / 2f)), byy + 2, (al << 24) | (accent & 0xFFFFFF));
                }
            } else {
                intro = false;
                introShown = true;
            }
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (intro || btn != 0) return false;
        MinecraftClient mc = MinecraftClient.getInstance();
        for (int i = 0; i < LABELS.length; i++) {
            int x = bx();
            int y = by(i);
            if (mx >= x && mx < x + 170 && my >= y && my < y + 22) {
                switch (i) {
                    case 0:
                        mc.setScreen(new SelectWorldScreen(this));
                        break;
                    case 1:
                        mc.setScreen(new MultiplayerScreen(this));
                        break;
                    case 2:
                        mc.setScreen(new OptionsScreen(this, mc.options));
                        break;
                    case 3:
                        mc.setScreen(new MenuScreen());
                        break;
                    default:
                        mc.scheduleStop();
                }
                return true;
            }
        }
        return false;
    }
}

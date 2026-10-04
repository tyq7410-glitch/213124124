package com.deadvisuals;

import net.minecraft.client.gui.DrawContext;

public final class Ui {
    private Ui() {
    }

    public static final int ACCENT = 0xFFB3122A;
    public static final int ACCENT2 = 0xFF7A1E5C;

    public static void rrect(DrawContext g, int x, int y, int w, int h, int r, int color) {
        if (w <= 0 || h <= 0) return;
        r = Math.min(r, Math.min(w, h) / 2);
        if (r <= 0) {
            g.fill(x, y, x + w, y + h, color);
            return;
        }
        g.fill(x, y + r, x + w, y + h - r, color);
        for (int i = 0; i < r; i++) {
            double dy = r - i - 0.5;
            int inset = r - (int) Math.round(Math.sqrt(r * r - dy * dy));
            g.fill(x + inset, y + i, x + w - inset, y + i + 1, color);
            g.fill(x + inset, y + h - 1 - i, x + w - inset, y + h - i, color);
        }
    }

    public static void hgrad(DrawContext g, int x, int y, int w, int h, int c1, int c2) {
        for (int i = 0; i < w; i += 2) {
            g.fill(x + i, y, Math.min(x + i + 2, x + w), y + h, Colors.lerp(c1, c2, i / (float) Math.max(1, w)));
        }
    }
}

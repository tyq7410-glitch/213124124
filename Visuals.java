package com.deadvisuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Visuals {
    private Visuals() {
    }

    private static final Item[] CD_ITEMS = {Items.ENDER_PEARL, Items.CHORUS_FRUIT, Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE};
    private static final String[] CD_NAMES = {"pearl", "chorus", "gapple", "egapple"};
    private static final String[] CD_KEYS = {"Pearl", "Chorus", "Gapple", "Gapple"};
    private static final Map<Item, Integer> PREV = new HashMap<>();
    private static final Map<String, Long> END_AT = new LinkedHashMap<>();
    private static Item heldPrev;
    private static Item offPrev;
    private static float dyn;

    public static void tick(MinecraftClient mc, ClientPlayerEntity p) {
        Item held = p.getMainHandStack().getItem();
        Item off = p.getOffHandStack().getItem();
        if (Mods.CD.enabled) {
            long now = System.currentTimeMillis();
            for (int i = 0; i < CD_ITEMS.length; i++) {
                int c = p.getInventory().count(CD_ITEMS[i]);
                Integer pr = PREV.put(CD_ITEMS[i], c);
                if (pr != null && c < pr && mc.options.useKey.isPressed()
                        && (heldPrev == CD_ITEMS[i] || offPrev == CD_ITEMS[i])) {
                    double d = Mods.CD.v(CD_KEYS[i]);
                    if (d > 0) END_AT.put(CD_NAMES[i], now + (long) (d * 1000));
                }
            }
        }
        heldPrev = held;
        offPrev = off;
    }

    public static void render(DrawContext g, MinecraftClient mc, int sw, int sh, long now, float dt) {
        ClientPlayerEntity p = mc.player;
        if (p == null) return;
        float t = (now % 1000000L) / 1000f;

        Module fm = Mods.FILTER;
        if (fm.enabled) {
            int rgb = Colors.pick(fm.mode("Color"), fm.v("Hue"), Fx.ticks) & 0xFFFFFF;
            int a = (int) (255 * fm.v("Strength") / 100.0);
            if (a > 0) g.fill(0, 0, sw, sh, (a << 24) | rgb);
            if (fm.on("Vignette")) {
                int base = (int) (255 * fm.v("Vignette strength") / 100.0 * 0.6);
                int th = 40;
                for (int i = 0; i < th; i += 2) {
                    int al = (int) (base * (1f - i / (float) th));
                    if (al <= 0) continue;
                    int col = al << 24;
                    g.fill(0, i, sw, i + 2, col);
                    g.fill(0, sh - i - 2, sw, sh - i, col);
                    g.fill(i, 0, i + 2, sh, col);
                    g.fill(sw - i - 2, 0, sw - i, sh, col);
                }
            }
        }

        if (Mods.SCREENFX.enabled) particles(g, sw, sh, t, Mods.SCREENFX);

        Module cm = Mods.CROSS;
        if (cm.enabled && mc.currentScreen == null && mc.options.getPerspective().isFirstPerson()) {
            crosshair(g, p, cm, sw, sh, dt);
        }

        Module cd = Mods.CD;
        if (cd.enabled) {
            END_AT.entrySet().removeIf(e -> e.getValue() <= now);
            List<String> l = new ArrayList<>();
            List<Integer> c = new ArrayList<>();
            for (Map.Entry<String, Long> e : END_AT.entrySet()) {
                l.add(String.format("%s %.1fs", e.getKey(), (e.getValue() - now) / 1000.0));
                c.add(0xFFFFC83C);
            }
            Hud2.panel(g, mc, cd, l, c, sw, sh);
        }
    }

    private static int wrap(int v, int m) {
        int mm = Math.max(1, m);
        return ((v % mm) + mm) % mm;
    }

    private static void particles(DrawContext g, int sw, int sh, float t, Module sm) {
        int n = (int) sm.v("Density");
        double sp = sm.v("Speed");
        int type = sm.mode("Type");
        int size = (int) sm.v("Size");
        for (int i = 0; i < n; i++) {
            float fx = ((i * 7919) % 1000) / 1000f;
            float fy = ((i * 104729) % 1000) / 1000f;
            float v = 0.4f + ((i * 31) % 10) / 10f;
            int x;
            int y;
            int col;
            int sz = size;
            switch (type) {
                case 1:
                    y = wrap((int) (fy * sh + t * 30 * sp * v), sh);
                    x = wrap((int) (fx * sw + Math.sin(t * 1.2 + i) * 18 + t * 8 * sp), sw);
                    col = 0xE0FFB7D5;
                    sz = size + 1;
                    break;
                case 2: {
                    x = (int) (fx * sw);
                    y = (int) (fy * sh);
                    int al = (int) (60 + 195 * (0.5 + 0.5 * Math.sin(t * 2 * sp + i)));
                    col = (al << 24) | 0xFFF2B0;
                    break;
                }
                case 3:
                    y = sh - wrap((int) (fy * sh + t * 40 * sp * v), sh);
                    x = wrap((int) (fx * sw + Math.sin(t * 2 + i) * 10), sw);
                    col = 0xE0FF8A1F;
                    break;
                default:
                    y = wrap((int) (fy * sh + t * 35 * sp * v), sh);
                    x = wrap((int) (fx * sw + Math.sin(t + i) * 14), sw);
                    col = 0xD0FFFFFF;
            }
            g.fill(x, y, x + sz, y + sz, col);
        }
    }

    private static void bar(DrawContext g, int x1, int y1, int x2, int y2, int col, boolean ol) {
        if (ol) g.fill(x1 - 1, y1 - 1, x2 + 1, y2 + 1, 0xFF000000);
        g.fill(x1, y1, x2, y2, col);
    }

    private static void crosshair(DrawContext g, ClientPlayerEntity p, Module cm, int sw, int sh, float dt) {
        float spd = (float) p.getVelocity().horizontalLength();
        float target = cm.on("Dynamic") ? Math.min(8f, spd * 14f) : 0f;
        if (cm.on("Hit flash") && Fx.hitRecent()) target += 3f;
        dyn += (target - dyn) * (1f - (float) Math.exp(-14 * dt));

        int cx = sw / 2;
        int cy = sh / 2;
        int size = (int) cm.v("Size");
        int gap = (int) (cm.v("Gap") + dyn);
        int t = (int) cm.v("Thickness");
        int h = t / 2;
        boolean ol = cm.on("Outline");
        int col = 0xFF000000 | Colors.pick(cm.mode("Color"), cm.v("Hue"), Fx.ticks);
        int dot = Math.max(2, size / 2);

        switch (cm.mode("Style")) {
            case 1:
                bar(g, cx - dot / 2, cy - dot / 2, cx - dot / 2 + dot, cy - dot / 2 + dot, col, ol);
                break;
            case 2: {
                int r = size + gap;
                for (int k = 0; k < 28; k++) {
                    double a = Math.PI * 2 * k / 28;
                    int x = cx + (int) Math.round(Math.cos(a) * r);
                    int y = cy + (int) Math.round(Math.sin(a) * r);
                    g.fill(x, y, x + t, y + t, col);
                }
                break;
            }
            case 4:
                for (int i = gap; i < gap + size; i++) {
                    g.fill(cx + i, cy + i, cx + i + t, cy + i + t, col);
                    g.fill(cx - i - t, cy + i, cx - i, cy + i + t, col);
                    g.fill(cx + i, cy - i - t, cx + i + t, cy - i, col);
                    g.fill(cx - i - t, cy - i - t, cx - i, cy - i, col);
                }
                break;
            default:
                bar(g, cx - gap - size, cy - h, cx - gap, cy - h + t, col, ol);
                bar(g, cx + gap, cy - h, cx + gap + size, cy - h + t, col, ol);
                bar(g, cx - h, cy - gap - size, cx - h + t, cy - gap, col, ol);
                bar(g, cx - h, cy + gap, cx - h + t, cy + gap + size, col, ol);
                if (cm.mode("Style") == 3) bar(g, cx - h, cy - h, cx - h + t, cy - h + t, col, ol);
        }
    }

    private static double timeTicks() {
        Module m = Mods.SKY;
        switch (m.mode("Time")) {
            case 1:
                return 23000;
            case 2:
                return 1000;
            case 3:
                return 6000;
            case 4:
                return 12500;
            case 5:
                return 14000;
            case 6:
                return 18000;
            case 7:
                return (System.currentTimeMillis() / 50.0 * m.v("Cycle speed")) % 24000;
            default:
                return m.v("Custom time");
        }
    }

    public static float skyAngle(float orig) {
        Module m = Mods.SKY;
        if (!m.enabled || m.mode("Time") == 0) return orig;
        double d = timeTicks() / 24000.0 - 0.25;
        d = d - Math.floor(d);
        double e = 0.5 - Math.cos(d * Math.PI) / 2.0;
        return (float) ((d * 2.0 + e) / 3.0);
    }

    public static float rain(float orig) {
        Module m = Mods.SKY;
        if (!m.enabled) return orig;
        switch (m.mode("Weather")) {
            case 1:
                return 0f;
            case 2:
            case 3:
                return 1f;
            default:
                return orig;
        }
    }

    public static float thunder(float orig) {
        Module m = Mods.SKY;
        if (!m.enabled) return orig;
        switch (m.mode("Weather")) {
            case 1:
            case 2:
                return 0f;
            case 3:
                return 1f;
            default:
                return orig;
        }
    }

    public static Object skyColor(Object cur) {
        Module m = Mods.SKY;
        int mode = m.mode("Sky color");
        if (!m.enabled || mode == 0) return null;
        int rgb = Colors.pick(mode - 1, m.v("Sky hue"), Fx.ticks) & 0xFFFFFF;
        double s = m.v("Sky strength");
        if (cur instanceof Integer i) {
            return (i & 0xFF000000) | (Colors.lerp(i & 0xFFFFFF, rgb, (float) s) & 0xFFFFFF);
        }
        if (cur instanceof Vec3d v) {
            return new Vec3d(v.x + ((rgb >> 16 & 255) / 255.0 - v.x) * s,
                    v.y + ((rgb >> 8 & 255) / 255.0 - v.y) * s,
                    v.z + ((rgb & 255) / 255.0 - v.z) * s);
        }
        return null;
    }
}

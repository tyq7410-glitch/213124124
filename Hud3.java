package com.deadvisuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;

public final class Hud3 {
    private Hud3() {
    }

    private static final ArrayDeque<Long> LC = new ArrayDeque<>();
    private static final ArrayDeque<Long> RC = new ArrayDeque<>();
    private static boolean pl;
    private static boolean pr;

    public static void render(DrawContext g, MinecraftClient mc, int sw, int sh, long now) {
        if (mc.player == null) return;
        TextRenderer tr = mc.textRenderer;
        if (Mods.WATER.enabled) watermark(g, mc, tr, sw);
        if (Mods.KEYS.enabled) keys(g, mc, tr, sw, sh, now);
    }

    private static void watermark(DrawContext g, MinecraftClient mc, TextRenderer tr, int sw) {
        Module m = Mods.WATER;
        String name = "dead visuals";
        StringBuilder rest = new StringBuilder();
        if (m.on("Username")) rest.append("  |  ").append(mc.player.getName().getString());
        if (m.on("FPS")) rest.append("  |  ").append(mc.getCurrentFps()).append(" fps");
        if (m.on("Ping")) {
            ClientPlayNetworkHandler nh = mc.getNetworkHandler();
            PlayerListEntry pe = nh == null ? null : nh.getPlayerListEntry(mc.player.getUuid());
            rest.append("  |  ").append(pe == null ? 0 : pe.getLatency()).append(" ms");
        }
        if (m.on("Clock")) rest.append("  |  ").append(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        String r = rest.toString();
        int w1 = tr.getWidth(name);
        int w = w1 + tr.getWidth(r) + 16;
        int h = 16;
        Hud2.SIZES.put("watermark", new int[]{w, h});
        int pos = m.mode("Position");
        int x = pos == 0 ? sw / 2 - w / 2 : pos == 1 ? 6 : sw - w - 6;
        int y = (int) m.v("Y offset");
        Ui.rrect(g, x, y, w, h, 8, 0xC00B0B0E);
        Ui.hgrad(g, x + 8, y + h - 1, w - 16, 1, Ui.ACCENT, Ui.ACCENT2);
        g.drawTextWithShadow(tr, name, x + 8, y + 4, Ui.ACCENT);
        g.drawTextWithShadow(tr, r, x + 8 + w1, y + 4, 0xFFD0D0D4);
    }

    private static void key(DrawContext g, TextRenderer tr, int x, int y, int w, int h, String label, boolean pressed) {
        Ui.rrect(g, x, y, w, h, 4, pressed ? Ui.ACCENT : 0xB00B0B0E);
        int tw = tr.getWidth(label);
        g.drawTextWithShadow(tr, label, x + (w - tw) / 2, y + (h - 8) / 2, pressed ? 0xFFFFFFFF : 0xFF9A9AA0);
    }

    private static void keys(DrawContext g, MinecraftClient mc, TextRenderer tr, int sw, int sh, long now) {
        Module m = Mods.KEYS;
        boolean a = mc.options.attackKey.isPressed();
        boolean u = mc.options.useKey.isPressed();
        if (a && !pl) LC.add(now);
        if (u && !pr) RC.add(now);
        pl = a;
        pr = u;
        while (!LC.isEmpty() && now - LC.peekFirst() > 1000) LC.pollFirst();
        while (!RC.isEmpty() && now - RC.peekFirst() > 1000) RC.pollFirst();

        boolean space = m.on("Space");
        boolean mouse = m.on("Mouse");
        int bs = 20;
        int gp = 2;
        int w = bs * 3 + gp * 2;
        int h = bs * 2 + gp + (space ? gp + 14 : 0) + (mouse ? gp + bs : 0);
        Hud2.SIZES.put("keystrokes", new int[]{w, h});
        int an = m.mode("Anchor");
        int ox = (int) m.v("X offset");
        int oy = (int) m.v("Y offset");
        int x = (an == 1 || an == 3) ? sw - w - ox : ox;
        int y = an >= 2 ? sh - h - oy : oy;

        key(g, tr, x + bs + gp, y, bs, bs, "W", mc.options.forwardKey.isPressed());
        key(g, tr, x, y + bs + gp, bs, bs, "A", mc.options.leftKey.isPressed());
        key(g, tr, x + bs + gp, y + bs + gp, bs, bs, "S", mc.options.backKey.isPressed());
        key(g, tr, x + 2 * (bs + gp), y + bs + gp, bs, bs, "D", mc.options.rightKey.isPressed());
        int yy = y + 2 * (bs + gp);
        if (space) {
            key(g, tr, x, yy, w, 14, "space", mc.options.jumpKey.isPressed());
            yy += 14 + gp;
        }
        if (mouse) {
            int mw = (w - gp) / 2;
            boolean cps = m.on("CPS");
            key(g, tr, x, yy, mw, bs, cps ? "L " + LC.size() : "L", a);
            key(g, tr, x + mw + gp, yy, mw, bs, cps ? "R " + RC.size() : "R", u);
        }
    }
}

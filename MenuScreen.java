package com.deadvisuals;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class MenuScreen extends Screen {
    private static final int ACCENT = 0xFFB3122A;

    private static final class Row {
        int x, y, w, h;
        Module m;
        Setting s;
    }

    private final List<Row> rows = new ArrayList<>();
    private final String only;
    private final long born = System.nanoTime();
    private long lastNs = born;
    private Row drag;
    private int cat = 0;
    private int scroll = 0;
    private int contentH = 0;
    private int hdrX0, hdrX1;
    private int px, py, pw = 360, ph, cx0, cy0, cy1, cw;

    public MenuScreen() {
        this(null);
    }

    public MenuScreen(String only) {
        super(Text.literal("dead visuals"));
        this.only = only;
    }

    @Override
    public void render(DrawContext g, int mx, int my, float delta) {
        long nowNs = System.nanoTime();
        float dt = Math.min(0.1f, (nowNs - lastNs) / 1e9f);
        lastNs = nowNs;
        float pr = Math.min(1f, (nowNs - born) / 2.2e8f);
        float e = 1f - (1f - pr) * (1f - pr) * (1f - pr);
        boolean single = only != null;

        ph = Math.min(height - 30, 320);
        px = (width - pw) / 2;
        py = (height - ph) / 2;
        cx0 = px + 8;
        cw = pw - 16;
        cy0 = py + (single ? 34 : 44);
        cy1 = py + ph - 18;

        g.fill(0, 0, width, height, ((int) (0x90 * e)) << 24);
        MatrixStack ms = g.getMatrices();
        ms.push();
        ms.translate(0f, (1f - e) * 14f, 0f);

        g.fill(px, py, px + pw, py + ph, 0xF00A0A0A);
        g.fill(px, py, px + pw, py + 2, ACCENT);
        g.fill(px, py + ph - 1, px + pw, py + ph, 0xFF222222);
        g.fill(px, py, px + 1, py + ph, 0xFF222222);
        g.fill(px + pw - 1, py, px + pw, py + ph, 0xFF222222);
        g.drawTextWithShadow(textRenderer, "dead visuals", px + 10, py + 9, ACCENT);

        String hl = single ? "< back" : "target hud >";
        int hw = textRenderer.getWidth(hl);
        hdrX0 = px + pw - 10 - hw;
        hdrX1 = px + pw - 10;
        boolean hh = mx >= hdrX0 && mx < hdrX1 && my >= py + 5 && my < py + 20;
        g.drawTextWithShadow(textRenderer, hl, hdrX0, py + 9, hh ? 0xFFFFFFFF : 0xFF909090);

        if (single) {
            g.drawTextWithShadow(textRenderer, only, px + 10, py + 24, 0xFFFFFFFF);
            g.fill(px + 10, py + 34 - 2, px + pw - 10, py + 34 - 1, ACCENT);
        } else {
            int tabW = pw / Mods.CATS.length;
            for (int i = 0; i < Mods.CATS.length; i++) {
                String name = Mods.CATS[i];
                int tx = px + tabW * i + tabW / 2 - textRenderer.getWidth(name) / 2;
                g.drawTextWithShadow(textRenderer, name, tx, py + 26, i == cat ? 0xFFFFFFFF : 0xFF707070);
                if (i == cat) g.fill(px + tabW * i + 10, py + 37, px + tabW * (i + 1) - 10, py + 38, ACCENT);
            }
        }

        rows.clear();
        int top = cy0 - scroll;
        int cy = top;
        g.enableScissor(cx0, cy0, cx0 + cw, cy1);
        for (Module m : Mods.ALL) {
            if (single ? !m.name.equals(only) : !m.cat.equals(Mods.CATS[cat])) continue;
            m.anim += ((m.enabled ? 1f : 0f) - m.anim) * Math.min(1f, dt * 14f);
            if (!single) {
                Row r = new Row();
                r.x = cx0;
                r.y = cy;
                r.w = cw;
                r.h = 18;
                r.m = m;
                rows.add(r);
                boolean hov = mx >= r.x && mx < r.x + r.w && my >= r.y && my < r.y + r.h && my >= cy0 && my < cy1;
                g.fill(r.x, r.y, r.x + r.w, r.y + r.h, hov ? 0xFF1A1A1A : 0xFF111111);
                g.fill(r.x, r.y, r.x + 3, r.y + r.h, Colors.lerp(0xFF2C2C2C, ACCENT, m.anim));
                g.drawTextWithShadow(textRenderer, m.name, r.x + 9, r.y + 5, Colors.lerp(0xFF8A8A8A, 0xFFFFFFFF, m.anim));
                g.drawTextWithShadow(textRenderer, m.open ? "v" : ">", r.x + r.w - 10, r.y + 5, 0xFF666666);
                cy += 20;
            }
            float tgt = (single || m.open) ? 1f : 0f;
            m.openA += (tgt - m.openA) * Math.min(1f, dt * 14f);
            if (Math.abs(tgt - m.openA) < 0.01f) m.openA = tgt;
            int full = m.settings.size() * 16 + 4;
            int hgt = (int) (full * m.openA);
            if (hgt > 0) {
                int y1 = Math.max(cy, cy0);
                int y2 = Math.min(cy + hgt, cy1);
                if (y2 > y1) {
                    g.enableScissor(cx0, y1, cx0 + cw, y2);
                    int i = 0;
                    for (Setting s : m.settings.values()) {
                        Row sr = new Row();
                        sr.x = cx0 + 8;
                        sr.y = cy + i * 16;
                        sr.w = cw - 8;
                        sr.h = 16;
                        sr.m = m;
                        sr.s = s;
                        i++;
                        if (m.openA >= 0.98f) rows.add(sr);
                        boolean sh = mx >= sr.x && mx < sr.x + sr.w && my >= sr.y && my < sr.y + sr.h && my >= cy0 && my < cy1;
                        g.fill(sr.x, sr.y, sr.x + sr.w, sr.y + sr.h, sh ? 0xFF141414 : 0xFF0D0D0D);
                        if (s.type == Setting.Type.NUM) {
                            double f = (s.num - s.min) / (s.max - s.min);
                            g.fill(sr.x, sr.y, sr.x + (int) (sr.w * f), sr.y + sr.h, 0x70B3122A);
                        }
                        g.drawTextWithShadow(textRenderer, s.name.toLowerCase(), sr.x + 6, sr.y + 4, 0xFFBBBBBB);
                        String val = s.display();
                        int vw = textRenderer.getWidth(val);
                        int vc = s.type == Setting.Type.BOOL ? (s.bool ? ACCENT : 0xFF666666) : 0xFFFFFFFF;
                        g.drawTextWithShadow(textRenderer, val, sr.x + sr.w - vw - 6, sr.y + 4, vc);
                    }
                    g.disableScissor();
                }
                cy += hgt;
            }
        }
        if (!single && cat == 1) {
            g.drawTextWithShadow(textRenderer, "item textures: options > resource packs", cx0 + 4, cy + 4, 0xFF666666);
            cy += 18;
        }
        g.disableScissor();

        contentH = cy - top;
        int maxScroll = Math.max(0, contentH - (cy1 - cy0));
        if (scroll > maxScroll) scroll = maxScroll;

        g.drawCenteredTextWithShadow(textRenderer, Text.literal("left click: toggle   right click: settings"),
                px + pw / 2, py + ph - 13, 0xFF555555);
        ms.pop();
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (my >= py + 5 && my < py + 20 && mx >= hdrX0 && mx < hdrX1) {
            Mods.save();
            client.setScreen(only == null ? new MenuScreen("target hud") : new MenuScreen());
            return true;
        }
        if (only == null && my >= py + 22 && my < py + 40 && mx >= px && mx < px + pw) {
            int i = (int) ((mx - px) / (pw / Mods.CATS.length));
            if (i >= 0 && i < Mods.CATS.length) {
                cat = i;
                scroll = 0;
            }
            return true;
        }
        if (my < cy0 || my >= cy1) return super.mouseClicked(mx, my, btn);
        for (Row r : rows) {
            if (mx >= r.x && mx < r.x + r.w && my >= r.y && my < r.y + r.h) {
                if (r.s == null) {
                    if (btn == 0) r.m.enabled = !r.m.enabled;
                    else if (btn == 1) r.m.open = !r.m.open;
                } else if (r.s.type == Setting.Type.BOOL) {
                    r.s.bool = !r.s.bool;
                } else if (r.s.type == Setting.Type.MODE) {
                    int n = r.s.modes.length;
                    r.s.idx = (r.s.idx + (btn == 1 ? -1 : 1) + n) % n;
                } else {
                    drag = r;
                    updateDrag(mx);
                }
                Mods.save();
                return true;
            }
        }
        return super.mouseClicked(mx, my, btn);
    }

    private void updateDrag(double mx) {
        if (drag == null || drag.s == null) return;
        double f = Math.max(0, Math.min(1, (mx - drag.x) / (double) drag.w));
        drag.s.setNum(drag.s.min + f * (drag.s.max - drag.s.min));
    }

    @Override
    public boolean mouseDragged(double mx, double my, int btn, double dx, double dy) {
        if (drag != null) {
            updateDrag(mx);
            return true;
        }
        return super.mouseDragged(mx, my, btn, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int btn) {
        if (drag != null) {
            drag = null;
            Mods.save();
            return true;
        }
        return super.mouseReleased(mx, my, btn);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double h, double v) {
        int maxScroll = Math.max(0, contentH - (cy1 - cy0));
        scroll = (int) Math.max(0, Math.min(maxScroll, scroll - v * 16));
        return true;
    }

    @Override
    public void close() {
        Mods.save();
        super.close();
    }
}

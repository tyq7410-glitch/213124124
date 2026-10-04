package com.deadvisuals;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class MenuScreen extends Screen {
    private static final int ACCENT = Ui.ACCENT;

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
    private String tip;
    private int cat = 0;
    private int scroll = 0;
    private int contentH = 0;
    private int hdrX0, hdrX1, hdr2X0, hdr2X1, hdr3X0, hdr3X1;
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
        tip = null;

        ph = Math.min(height - 70, 320);
        px = (width - pw) / 2;
        py = (height - ph) / 2 + 8;
        cx0 = px + 8;
        cw = pw - 16;
        cy0 = py + (single ? 36 : 46);
        cy1 = py + ph - 18;

        g.fill(0, 0, width, height, ((int) (0x90 * e)) << 24);
        MatrixStack ms = g.getMatrices();
        ms.push();
        ms.translate(0f, (1f - e) * 14f, 0f);

        Ui.rrect(g, px - 1, py - 1, pw + 2, ph + 2, 9, 0xFF26262C);
        Ui.rrect(g, px, py, pw, ph, 8, 0xF00B0B0E);
        Ui.hgrad(g, px + 10, py + 1, pw - 20, 2, ACCENT, Ui.ACCENT2);
        Ui.rrect(g, px + 10, py + 9, 7, 7, 3, 0xFFFF5F57);
        Ui.rrect(g, px + 21, py + 9, 7, 7, 3, 0xFFFEBC2E);
        Ui.rrect(g, px + 32, py + 9, 7, 7, 3, 0xFF28C840);
        g.drawTextWithShadow(textRenderer, "dead visuals", px + 48, py + 9, ACCENT);

        String hl = single ? "< back" : "target hud >";
        int hw = textRenderer.getWidth(hl);
        hdrX0 = px + pw - 10 - hw;
        hdrX1 = px + pw - 10;
        boolean hh = mx >= hdrX0 && mx < hdrX1 && my >= py + 5 && my < py + 20;
        g.drawTextWithShadow(textRenderer, hl, hdrX0, py + 9, hh ? 0xFFFFFFFF : 0xFF909090);
        if (hh) tip = single ? "back - назад до всіх функцій" : "target hud - окремі налаштування панелі ворога";
        if (!single) {
            String h2 = "hud editor";
            int w2 = textRenderer.getWidth(h2);
            hdr2X1 = hdrX0 - 14;
            hdr2X0 = hdr2X1 - w2;
            boolean h2h = mx >= hdr2X0 && mx < hdr2X1 && my >= py + 5 && my < py + 20;
            if (h2h) tip = "hud editor - перетягуй панелі інтерфейсу мишкою";
            g.drawTextWithShadow(textRenderer, h2, hdr2X0, py + 9, h2h ? 0xFFFFFFFF : 0xFF909090);
            String h3 = "textures";
            int w3 = textRenderer.getWidth(h3);
            hdr3X1 = hdr2X0 - 14;
            hdr3X0 = hdr3X1 - w3;
            boolean h3h = mx >= hdr3X0 && mx < hdr3X1 && my >= py + 5 && my < py + 20;
            if (h3h) tip = "textures - у Resource Packs вибери текстури зброї";
            g.drawTextWithShadow(textRenderer, h3, hdr3X0, py + 9, h3h ? 0xFFFFFFFF : 0xFF909090);
        } else {
            hdr2X0 = -1;
            hdr2X1 = -1;
            hdr3X0 = -1;
            hdr3X1 = -1;
        }

        if (single) {
            g.drawTextWithShadow(textRenderer, only, px + 12, py + 24, 0xFFFFFFFF);
            Ui.hgrad(g, px + 10, py + 34, pw - 20, 1, ACCENT, Ui.ACCENT2);
        } else {
            int tabW = pw / Mods.CATS.length;
            for (int i = 0; i < Mods.CATS.length; i++) {
                String name = Mods.CATS[i];
                if (i == cat) Ui.rrect(g, px + tabW * i + 4, py + 22, tabW - 8, 16, 6, 0x50B3122A);
                int tx = px + tabW * i + tabW / 2 - textRenderer.getWidth(name) / 2;
                g.drawTextWithShadow(textRenderer, name, tx, py + 26, i == cat ? 0xFFFFFFFF : 0xFF707070);
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
                if (hov) tip = Lang.module(m);
                Ui.rrect(g, r.x, r.y, r.w, r.h, 5, hov ? 0xFF1B1B20 : 0xFF121216);
                g.drawTextWithShadow(textRenderer, m.name, r.x + 9, r.y + 5, Colors.lerp(0xFF8A8A8A, 0xFFFFFFFF, m.anim));
                g.drawTextWithShadow(textRenderer, m.open ? "v" : ">", r.x + r.w - 50, r.y + 5, 0xFF666666);
                int tx = r.x + r.w - 32;
                Ui.rrect(g, tx, r.y + 4, 24, 10, 5, Colors.lerp(0xFF34343A, ACCENT, m.anim));
                Ui.rrect(g, tx + 2 + (int) (m.anim * 14), r.y + 5, 8, 8, 4, 0xFFFFFFFF);
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
                        if (sh) tip = Lang.setting(m, s);
                        Ui.rrect(g, sr.x, sr.y, sr.w, 15, 4, sh ? 0xFF16161B : 0xFF0E0E12);
                        if (s.type == Setting.Type.NUM) {
                            double f = (s.num - s.min) / (s.max - s.min);
                            Ui.rrect(g, sr.x, sr.y, (int) (sr.w * f), 15, 4, 0x70B3122A);
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

        String tt = tip != null ? tip : "наведи на функцію - тут буде опис";
        Ui.rrect(g, px, py - 22, pw, 16, 8, 0xE00B0B0E);
        g.drawCenteredTextWithShadow(textRenderer, Text.literal(tt), px + pw / 2, py - 18,
                tip != null ? 0xFFFFFFFF : 0xFF666666);
        ms.pop();
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (my >= py + 5 && my < py + 20 && mx >= hdrX0 && mx < hdrX1) {
            Mods.save();
            client.setScreen(only == null ? new MenuScreen("target hud") : new MenuScreen());
            return true;
        }
        if (only == null && my >= py + 5 && my < py + 20 && mx >= hdr2X0 && mx < hdr2X1) {
            Mods.save();
            client.setScreen(new HudEditorScreen(this));
            return true;
        }
        if (only == null && my >= py + 5 && my < py + 20 && mx >= hdr3X0 && mx < hdr3X1) {
            Mods.save();
            client.setScreen(new OptionsScreen(this, client.options));
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

package com.deadvisuals;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class HudEditorScreen extends Screen {
    private static final class El {
        Module m;
        char kind;
        int w, h, x, y;
    }

    private final Screen parent;
    private final List<El> els = new ArrayList<>();
    private El drag;
    private int gx, gy;

    public HudEditorScreen(Screen parent) {
        super(Text.literal("hud editor"));
        this.parent = parent;
    }

    private void add(Module m, char kind, int dw, int dh) {
        El e = new El();
        e.m = m;
        e.kind = kind;
        int[] s = Hud2.SIZES.get(m.name);
        e.w = s != null ? s[0] : dw;
        e.h = s != null ? s[1] : dh;
        els.add(e);
    }

    @Override
    protected void init() {
        els.clear();
        add(Mods.ARMOR, 'A', 70, 64);
        add(Mods.ITEMS, 'A', 60, 40);
        add(Mods.EFFECTS, 'A', 90, 28);
        add(Mods.INFO, 'A', 90, 50);
        add(Mods.CD, 'A', 70, 24);
        add(Mods.KEYS, 'A', 64, 70);
        add(Mods.REACH, 'A', 60, 17);
        add(Mods.WATER, 'W', 220, 16);
        add(Mods.THUD, 'T', 130, 40);
        add(Mods.COMBO, 'C', 90, 12);
    }

    private void place(El e) {
        Module m = e.m;
        switch (e.kind) {
            case 'A': {
                int an = m.mode("Anchor");
                int ox = (int) m.v("X offset");
                int oy = (int) m.v("Y offset");
                e.x = (an == 1 || an == 3) ? width - e.w - ox : ox;
                e.y = an >= 2 ? height - e.h - oy : oy;
                break;
            }
            case 'W': {
                int pos = m.mode("Position");
                e.x = pos == 0 ? width / 2 - e.w / 2 : pos == 1 ? 6 : width - e.w - 6;
                e.y = (int) m.v("Y offset");
                break;
            }
            case 'T':
                e.x = width / 2 + (int) m.v("X offset");
                e.y = height / 2 + (int) m.v("Y offset");
                break;
            default:
                e.x = width / 2 - e.w / 2;
                e.y = (int) m.v("Y offset");
        }
    }

    private void apply(El e, int nx, int ny) {
        Module m = e.m;
        switch (e.kind) {
            case 'A': {
                int cx = nx + e.w / 2;
                int cy = ny + e.h / 2;
                int an = (cx > width / 2 ? 1 : 0) + (cy > height / 2 ? 2 : 0);
                m.settings.get("Anchor").idx = an;
                m.settings.get("X offset").setNum((an == 1 || an == 3) ? width - e.w - nx : nx);
                m.settings.get("Y offset").setNum(an >= 2 ? height - e.h - ny : ny);
                break;
            }
            case 'W': {
                int cx = nx + e.w / 2;
                m.settings.get("Position").idx = cx < width / 3 ? 1 : cx > width * 2 / 3 ? 2 : 0;
                m.settings.get("Y offset").setNum(ny);
                break;
            }
            case 'T':
                m.settings.get("X offset").setNum(nx - width / 2);
                m.settings.get("Y offset").setNum(ny - height / 2);
                break;
            default:
                m.settings.get("Y offset").setNum(ny);
        }
    }

    @Override
    public void render(DrawContext g, int mx, int my, float delta) {
        g.fill(0, 0, width, height, 0xC0000000);
        for (El e : els) {
            place(e);
            boolean on = e.m.enabled;
            boolean hov = mx >= e.x && mx < e.x + e.w && my >= e.y && my < e.y + e.h;
            Ui.rrect(g, e.x, e.y, e.w, e.h, 5, hov || e == drag ? 0xC01B1B20 : 0xA00B0B0E);
            int oc = on ? Ui.ACCENT : 0xFF444444;
            g.fill(e.x, e.y, e.x + e.w, e.y + 1, oc);
            g.fill(e.x, e.y + e.h - 1, e.x + e.w, e.y + e.h, oc);
            g.fill(e.x, e.y, e.x + 1, e.y + e.h, oc);
            g.fill(e.x + e.w - 1, e.y, e.x + e.w, e.y + e.h, oc);
            String label = e.m.name + (on ? "" : " (off)");
            g.drawCenteredTextWithShadow(textRenderer, Text.literal(label), e.x + e.w / 2, e.y + e.h / 2 - 4,
                    on ? 0xFFFFFFFF : 0xFF777777);
        }
        g.drawCenteredTextWithShadow(textRenderer,
                Text.literal("hud editor: тягни панелі мишкою  |  права кнопка: вкл/викл  |  Esc: назад"),
                width / 2, height / 2 - 4, 0xFF888888);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        for (int i = els.size() - 1; i >= 0; i--) {
            El e = els.get(i);
            if (mx >= e.x && mx < e.x + e.w && my >= e.y && my < e.y + e.h) {
                if (btn == 0) {
                    drag = e;
                    gx = (int) mx - e.x;
                    gy = (int) my - e.y;
                } else if (btn == 1) {
                    e.m.enabled = !e.m.enabled;
                    Mods.save();
                }
                return true;
            }
        }
        return super.mouseClicked(mx, my, btn);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int btn, double dx, double dy) {
        if (drag != null) {
            int nx = Math.max(0, Math.min(width - drag.w, (int) mx - gx));
            int ny = Math.max(0, Math.min(height - drag.h, (int) my - gy));
            apply(drag, nx, ny);
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
    public void close() {
        Mods.save();
        client.setScreen(parent);
    }
}

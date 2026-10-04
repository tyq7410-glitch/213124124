package com.deadvisuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public final class Hud2 {
    private Hud2() {
    }

    static final java.util.Map<String, int[]> SIZES = new java.util.HashMap<>();
    private static int combo;
    private static int kills;
    private static long lastHitAt;
    private static float prevHp = -1f;

    public static void reset() {
        combo = 0;
        kills = 0;
        prevHp = -1f;
    }

    public static void onHit() {
        combo++;
        lastHitAt = System.currentTimeMillis();
    }

    public static void onKill() {
        kills++;
    }

    public static void tick(ClientPlayerEntity p) {
        float hp = p.getHealth();
        if (prevHp >= 0 && hp < prevHp - 0.01f) combo = 0;
        prevHp = hp;
        if (combo > 0 && System.currentTimeMillis() - lastHitAt > Mods.COMBO.v("Timeout") * 1000) combo = 0;

        if (Mods.ARMOR.enabled && Fx.ticks % 100 == 0) {
            EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
            for (EquipmentSlot s : slots) {
                ItemStack st = p.getEquippedStack(s);
                if (st.getMaxDamage() > 0 && (st.getMaxDamage() - st.getDamage()) * 100 / st.getMaxDamage() < 10) {
                    Helper.sayLimited("armor is breaking!", 30000);
                }
            }
        }
    }

    static void panel(DrawContext g, MinecraftClient mc, Module m, List<String> lines, List<Integer> cols, int sw, int sh) {
        if (lines.isEmpty()) return;
        TextRenderer tr = mc.textRenderer;
        int w = 0;
        for (String s : lines) w = Math.max(w, tr.getWidth(s));
        w += 14;
        int h = lines.size() * 11 + 6;
        SIZES.put(m.name, new int[]{w, h});
        int a = m.mode("Anchor");
        int ox = (int) m.v("X offset");
        int oy = (int) m.v("Y offset");
        int x = (a == 1 || a == 3) ? sw - w - ox : ox;
        int y = a >= 2 ? sh - h - oy : oy;
        Ui.rrect(g, x, y, w, h, 5, 0xB00B0B0E);
        Ui.hgrad(g, x + 6, y, w - 12, 1, Ui.ACCENT, Ui.ACCENT2);
        for (int i = 0; i < lines.size(); i++) {
            g.drawTextWithShadow(tr, lines.get(i), x + 8, y + 4 + i * 11, cols.get(i));
        }
    }

    private static void addDur(List<String> lines, List<Integer> cols, String name, ItemStack st, boolean pct) {
        if (st.isEmpty() || st.getMaxDamage() <= 0) return;
        int left = st.getMaxDamage() - st.getDamage();
        int p = left * 100 / st.getMaxDamage();
        lines.add(name + " " + (pct ? p + "%" : String.valueOf(left)));
        cols.add(Colors.lerp(0xFFE0192E, 0xFF39FF88, p / 100f));
    }

    public static void render(DrawContext g, MinecraftClient mc, int sw, int sh, long now) {
        ClientPlayerEntity p = mc.player;
        if (p == null) return;

        Module rm = Mods.REACH;
        if (rm.enabled && now - Fx.reachAt < 3000) {
            List<String> rl = new ArrayList<>();
            List<Integer> rc = new ArrayList<>();
            rl.add(String.format("reach %.2f", Fx.lastReach));
            rc.add(0xFFFFFFFF);
            panel(g, mc, rm, rl, rc, sw, sh);
        }

        Module cm = Mods.COMBO;
        if (cm.enabled) {
            boolean showC = combo >= 2;
            boolean showK = cm.on("Show kills") && kills > 0;
            if (showC || showK) {
                String s = (showC ? "combo x" + combo : "") + (showC && showK ? "   " : "") + (showK ? "kills " + kills : "");
                float pop = Math.max(0f, 1f - (now - lastHitAt) / 220f);
                float sc = (float) cm.v("Scale") * (1f + 0.5f * pop * pop);
                int col = 0xFF000000 | Colors.pick(cm.mode("Color"), cm.v("Hue"), Fx.ticks);
                MatrixStack ms = g.getMatrices();
                ms.push();
                ms.translate(sw / 2f, (float) cm.v("Y offset"), 0f);
                ms.scale(sc, sc, 1f);
                g.drawCenteredTextWithShadow(mc.textRenderer, Text.literal(s), 0, 0, col);
                ms.pop();
            }
        }

        Module am = Mods.ARMOR;
        if (am.enabled) {
            List<String> l = new ArrayList<>();
            List<Integer> c = new ArrayList<>();
            boolean pct = am.on("Percent");
            addDur(l, c, "head ", p.getEquippedStack(EquipmentSlot.HEAD), pct);
            addDur(l, c, "chest", p.getEquippedStack(EquipmentSlot.CHEST), pct);
            addDur(l, c, "legs ", p.getEquippedStack(EquipmentSlot.LEGS), pct);
            addDur(l, c, "feet ", p.getEquippedStack(EquipmentSlot.FEET), pct);
            if (am.on("Held item")) addDur(l, c, "hand ", p.getMainHandStack(), pct);
            panel(g, mc, am, l, c, sw, sh);
        }

        Module im = Mods.ITEMS;
        if (im.enabled) {
            List<String> l = new ArrayList<>();
            List<Integer> c = new ArrayList<>();
            if (im.on("Totems")) {
                int n = p.getInventory().count(Items.TOTEM_OF_UNDYING);
                l.add("totems " + n);
                c.add(n <= 1 ? 0xFFE0192E : 0xFFFFFFFF);
            }
            if (im.on("Pearls")) {
                l.add("pearls " + p.getInventory().count(Items.ENDER_PEARL));
                c.add(0xFF8EE8D8);
            }
            if (im.on("Golden apples")) {
                l.add("gapples " + (p.getInventory().count(Items.GOLDEN_APPLE) + p.getInventory().count(Items.ENCHANTED_GOLDEN_APPLE)));
                c.add(0xFFFFC83C);
            }
            if (im.on("Arrows")) {
                l.add("arrows " + p.getInventory().count(Items.ARROW));
                c.add(0xFFCCCCCC);
            }
            if (im.on("XP bottles")) {
                l.add("xp " + p.getInventory().count(Items.EXPERIENCE_BOTTLE));
                c.add(0xFF7CFF6B);
            }
            panel(g, mc, im, l, c, sw, sh);
        }

        Module em = Mods.EFFECTS;
        if (em.enabled) {
            List<String> l = new ArrayList<>();
            List<Integer> c = new ArrayList<>();
            for (StatusEffectInstance in : p.getStatusEffects()) {
                String n = in.getEffectType().value().getName().getString();
                int amp = in.getAmplifier() + 1;
                int sec = in.getDuration() / 20;
                String t = in.isInfinite() ? "inf" : String.format("%d:%02d", sec / 60, sec % 60);
                l.add(n + (amp > 1 ? " " + amp : "") + "  " + t);
                c.add(0xFF000000 | in.getEffectType().value().getColor());
            }
            panel(g, mc, em, l, c, sw, sh);
        }

        Module nm = Mods.INFO;
        if (nm.enabled) {
            List<String> l = new ArrayList<>();
            List<Integer> c = new ArrayList<>();
            if (nm.on("FPS")) {
                l.add("fps " + mc.getCurrentFps());
                c.add(0xFFFFFFFF);
            }
            if (nm.on("Ping")) {
                ClientPlayNetworkHandler nh = mc.getNetworkHandler();
                PlayerListEntry pe = nh == null ? null : nh.getPlayerListEntry(p.getUuid());
                l.add("ping " + (pe == null ? 0 : pe.getLatency()) + "ms");
                c.add(0xFFFFFFFF);
            }
            if (nm.on("Coords")) {
                l.add(String.format("xyz %d %d %d", (int) Math.floor(p.getX()), (int) Math.floor(p.getY()), (int) Math.floor(p.getZ())));
                c.add(0xFFFFFFFF);
            }
            if (nm.on("Direction")) {
                l.add(p.getHorizontalFacing().asString());
                c.add(0xFFAAAAAA);
            }
            if (nm.on("Speed")) {
                l.add(String.format("speed %.1f b/s", p.getVelocity().horizontalLength() * 20));
                c.add(0xFFFFFFFF);
            }
            if (nm.on("Hunger")) {
                l.add(String.format("food %d  sat %.1f", p.getHungerManager().getFoodLevel(), p.getHungerManager().getSaturationLevel()));
                c.add(0xFFFFFFFF);
            }
            if (nm.on("Light") && mc.world != null) {
                l.add("light " + mc.world.getLightLevel(p.getBlockPos()));
                c.add(0xFFFFFFFF);
            }
            if (nm.on("Server")) {
                ServerInfo si = mc.getCurrentServerEntry();
                l.add(si == null ? "singleplayer" : si.address);
                c.add(0xFFAAAAAA);
            }
            panel(g, mc, nm, l, c, sw, sh);
        }

        Module lp = Mods.LOWHP;
        if (lp.enabled && p.isAlive()) {
            float pct = p.getHealth() / Math.max(1f, p.getMaxHealth()) * 100f;
            if (pct < lp.v("Threshold")) {
                float pulse = 0.5f + 0.5f * (float) Math.sin(now / 1000.0 * lp.v("Speed") * 4.0);
                int base = (int) (40 + 90 * pulse);
                int rgb = Colors.pick(lp.mode("Color"), lp.v("Hue"), Fx.ticks) & 0xFFFFFF;
                int th = (int) lp.v("Thickness");
                for (int i = 0; i < th; i += 2) {
                    int al = (int) (base * (1f - i / (float) th));
                    int col = (al << 24) | rgb;
                    g.fill(0, i, sw, i + 2, col);
                    g.fill(0, sh - i - 2, sw, sh - i, col);
                    g.fill(i, 0, i + 2, sh, col);
                    g.fill(sw - i - 2, 0, sw - i, sh, col);
                }
            }
        }
    }
}

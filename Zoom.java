package com.deadvisuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.SimpleOption;

public final class Zoom {
    private Zoom() {
    }

    private static boolean zooming;
    private static int orig;
    private static double cur;

    public static void tick(MinecraftClient mc, boolean pressed) {
        SimpleOption<Integer> opt = mc.options.getFov();
        if (!Mods.ZOOM.enabled) {
            if (zooming) {
                opt.setValue(orig);
                zooming = false;
            }
            return;
        }
        double k = Mods.ZOOM.on("Smooth") ? 0.3 : 1.0;
        if (pressed) {
            if (!zooming) {
                orig = opt.getValue();
                cur = orig;
                zooming = true;
            }
            cur += (Mods.ZOOM.v("Level") - cur) * k;
            opt.setValue((int) Math.round(cur));
        } else if (zooming) {
            cur += (orig - cur) * k;
            if (Math.abs(orig - cur) < 1.0) {
                opt.setValue(orig);
                zooming = false;
            } else {
                opt.setValue((int) Math.round(cur));
            }
        }
    }
}

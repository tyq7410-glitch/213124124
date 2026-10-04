package com.deadvisuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;

public final class Tweaks {
    private Tweaks() {
    }

    private static boolean applied;
    private static int counter;
    private static double oFov;
    private static double oDist;
    private static boolean oBob;

    public static void tick(MinecraftClient mc) {
        boolean want = Mods.TWEAKS.enabled;
        counter++;
        if (want && (!applied || counter % 100 == 0)) apply(mc);
        else if (!want && applied) restore(mc);
    }

    private static void apply(MinecraftClient mc) {
        GameOptions o = mc.options;
        Module m = Mods.TWEAKS;
        if (!applied) {
            oFov = o.getFovEffectScale().getValue();
            oDist = o.getDistortionEffectScale().getValue();
            oBob = o.getBobView().getValue();
        }
        if (m.on("No FOV effects")) o.getFovEffectScale().setValue(0.0);
        if (m.on("No distortion")) o.getDistortionEffectScale().setValue(0.0);
        if (m.on("No view bobbing")) o.getBobView().setValue(false);
        applied = true;
    }

    private static void restore(MinecraftClient mc) {
        GameOptions o = mc.options;
        o.getFovEffectScale().setValue(oFov);
        o.getDistortionEffectScale().setValue(oDist);
        o.getBobView().setValue(oBob);
        applied = false;
    }
}

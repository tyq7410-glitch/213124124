package com.deadvisuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.ParticlesMode;

public final class Perf {
    private Perf() {
    }

    private static boolean applied;
    private static int counter;
    private static ParticlesMode oParticles;
    private static boolean oShadows;
    private static boolean oAo;
    private static boolean oVsync;
    private static CloudRenderMode oClouds;
    private static int oBlend;
    private static int oMaxFps;
    private static double oEntDist;

    public static float scale() {
        Module m = Mods.PERF;
        if (!m.enabled || !m.on("Adaptive effects")) return 1f;
        return MinecraftClient.getInstance().getCurrentFps() < m.v("Low FPS limit") ? 0.5f : 1f;
    }

    public static void tick(MinecraftClient mc) {
        boolean want = Mods.PERF.enabled;
        counter++;
        if (want && (!applied || counter % 100 == 0)) apply(mc);
        else if (!want && applied) restore(mc);
    }

    private static void apply(MinecraftClient mc) {
        GameOptions o = mc.options;
        Module m = Mods.PERF;
        if (!applied) {
            oParticles = o.getParticles().getValue();
            oShadows = o.getEntityShadows().getValue();
            oAo = o.getAo().getValue();
            oVsync = o.getEnableVsync().getValue();
            oClouds = o.getCloudRenderMode().getValue();
            oBlend = o.getBiomeBlendRadius().getValue();
            oMaxFps = o.getMaxFps().getValue();
            oEntDist = o.getEntityDistanceScaling().getValue();
        }
        if (m.on("Particles minimal")) o.getParticles().setValue(ParticlesMode.MINIMAL);
        if (m.on("No entity shadows")) o.getEntityShadows().setValue(false);
        if (m.on("No clouds")) o.getCloudRenderMode().setValue(CloudRenderMode.OFF);
        if (m.on("Biome blend 0")) o.getBiomeBlendRadius().setValue(0);
        if (m.on("Smooth light off")) o.getAo().setValue(false);
        if (m.on("Entities closer")) o.getEntityDistanceScaling().setValue(0.5);
        if (m.on("Unlimited FPS")) o.getMaxFps().setValue(260);
        if (m.on("VSync off")) o.getEnableVsync().setValue(false);
        applied = true;
    }

    private static void restore(MinecraftClient mc) {
        GameOptions o = mc.options;
        o.getParticles().setValue(oParticles);
        o.getEntityShadows().setValue(oShadows);
        o.getAo().setValue(oAo);
        o.getEnableVsync().setValue(oVsync);
        o.getCloudRenderMode().setValue(oClouds);
        o.getBiomeBlendRadius().setValue(oBlend);
        o.getMaxFps().setValue(oMaxFps);
        o.getEntityDistanceScaling().setValue(oEntDist);
        applied = false;
    }
}

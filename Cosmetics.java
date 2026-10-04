package com.deadvisuals;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;

import java.util.Random;

public final class Cosmetics {
    private Cosmetics() {
    }

    private static final Random R = new Random();

    public static void tick(ClientWorld w, ClientPlayerEntity p) {
        if (Mods.AURA.enabled) aura(w, p);
        if (Mods.TRAIL.enabled) trail(w, p);
        Pets.tick(w);
    }

    private static void aura(ClientWorld w, ClientPlayerEntity p) {
        Module m = Mods.AURA;
        int t = Fx.ticks;
        int rgb = Colors.pick(m.mode("Style"), m.v("Hue"), t) & 0xFFFFFF;
        DustParticleEffect d = new DustParticleEffect(rgb, (float) m.v("Size"));
        int n = Math.max(1, (int) (m.v("Count") * Perf.scale()));
        double sp = m.v("Speed");
        double r = m.v("Radius");
        double h = m.v("Height");
        int shape = m.mode("Shape");
        for (int k = 0; k < n; k++) {
            double a = 0;
            double y = 0;
            double rr = r;
            double off = k * Math.PI * 2 / n;
            if (shape == 1) {
                double ph = ((t * 0.02 * sp) + (double) k / n) % 1.0;
                y = ph * 2.0;
                a = ph * 10 + off;
            } else if (shape == 2) {
                y = p.getHeight() + 0.35;
                a = t * 0.3 * sp + off;
                rr = Math.max(0.2, r * 0.45);
            } else if (shape == 3) {
                y = 0.05;
                a = t * 0.2 * sp + off;
            } else if (shape == 4) {
                double ph = ((t * 0.03 * sp) + (double) k / n) % 1.0;
                y = 2.2 * (1.0 - ph);
                a = ph * 14 + off;
                rr = r * (0.4 + 0.6 * ph);
            } else if (shape == 5) {
                a = R.nextDouble() * Math.PI * 2;
                y = R.nextDouble() * 2.0;
                rr = r * (0.6 + R.nextDouble() * 0.6);
            } else if (shape == 6) {
                if (t % 3 == 0) {
                    w.addParticle(ParticleTypes.CHERRY_LEAVES,
                            p.getX() + (R.nextDouble() - 0.5) * 2 * r, p.getY() + 1.6 + R.nextDouble() * 0.8,
                            p.getZ() + (R.nextDouble() - 0.5) * 2 * r, 0, -0.02, 0);
                }
                continue;
            } else {
                y = h + Math.sin(t * 0.1 + k) * 0.5;
                a = t * 0.15 * sp + off;
            }
            w.addParticle(d, p.getX() + Math.cos(a) * rr, p.getY() + y, p.getZ() + Math.sin(a) * rr, 0, 0, 0);
        }
    }

    private static void trail(ClientWorld w, ClientPlayerEntity p) {
        if (!p.isOnGround() || p.getVelocity().horizontalLengthSquared() < 0.002) return;
        Module m = Mods.TRAIL;
        int dens = Math.max(1, (int) (m.v("Density") * Perf.scale()));
        int type = m.mode("Type");
        int rgb = Colors.pick(m.mode("Color"), m.v("Hue"), Fx.ticks) & 0xFFFFFF;
        DustParticleEffect dust = new DustParticleEffect(rgb, 1.0f);
        for (int i = 0; i < dens; i++) {
            double x = p.getX() + (R.nextDouble() - 0.5) * 0.4;
            double y = p.getY() + 0.05;
            double z = p.getZ() + (R.nextDouble() - 0.5) * 0.4;
            switch (type) {
                case 1:
                    w.addParticle(ParticleTypes.SOUL, x, y, z, 0, 0.02, 0);
                    break;
                case 2:
                    w.addParticle(ParticleTypes.FLAME, x, y, z, 0, 0.01, 0);
                    break;
                case 3:
                    w.addParticle(ParticleTypes.END_ROD, x, y, z, 0, 0.02, 0);
                    break;
                case 4:
                    w.addParticle(ParticleTypes.HEART, x, y + 0.3, z, 0, 0.02, 0);
                    break;
                case 5:
                    w.addParticle(ParticleTypes.SNOWFLAKE, x, y + 0.2, z, 0, 0.02, 0);
                    break;
                case 6:
                    w.addParticle(ParticleTypes.ELECTRIC_SPARK, x, y + 0.1, z, 0, 0.02, 0);
                    break;
                case 7:
                    w.addParticle(ParticleTypes.CHERRY_LEAVES, x, y + 0.6, z, 0, -0.01, 0);
                    break;
                default:
                    w.addParticle(dust, x, y, z, 0, 0, 0);
            }
        }
    }
}

package com.deadvisuals;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.particle.DustParticleEffect;

import java.util.Random;

public final class Extra {
    private Extra() {
    }

    private static final Random R = new Random();
    private static boolean wasGround = true;

    public static void tick(ClientWorld w, ClientPlayerEntity p) {
        boolean g = p.isOnGround();
        if (Mods.JUMP.enabled && wasGround && !g && p.getVelocity().y > 0.2) jump(w, p, Mods.JUMP);
        wasGround = g;
        if (Mods.TRACK.enabled) track(w, p, Mods.TRACK);
    }

    private static void jump(ClientWorld w, ClientPlayerEntity p, Module j) {
        int rgb = Colors.pick(j.mode("Color"), j.v("Hue"), Fx.ticks) & 0xFFFFFF;
        DustParticleEffect d = new DustParticleEffect(rgb, (float) j.v("Size"));
        int n = Math.max(8, (int) (j.v("Amount") * Perf.scale()));
        double r = j.v("Radius");
        for (int k = 0; k < n; k++) {
            double a = Math.PI * 2 * k / n;
            w.addParticle(d, p.getX() + Math.cos(a) * r, p.getY() + 0.1, p.getZ() + Math.sin(a) * r, 0, 0, 0);
            if (j.mode("Type") == 1) {
                w.addParticle(d, p.getX() + Math.cos(a) * r * 0.7, p.getY() + 0.4, p.getZ() + Math.sin(a) * r * 0.7, 0, 0, 0);
            }
        }
    }

    private static void track(ClientWorld w, ClientPlayerEntity p, Module t) {
        int rgb = Colors.pick(t.mode("Color"), t.v("Hue"), Fx.ticks) & 0xFFFFFF;
        DustParticleEffect d = new DustParticleEffect(rgb, (float) t.v("Size"));
        int dens = Math.max(1, (int) (t.v("Density") * Perf.scale()));
        for (Entity e : w.getEntities()) {
            if (!(e instanceof ProjectileEntity pe)) continue;
            if (t.on("Only mine") && pe.getOwner() != p) continue;
            if (pe.squaredDistanceTo(p) > 4096) continue;
            for (int i = 0; i < dens; i++) {
                w.addParticle(d, pe.getX() + (R.nextDouble() - 0.5) * 0.1, pe.getY() + (R.nextDouble() - 0.5) * 0.1,
                        pe.getZ() + (R.nextDouble() - 0.5) * 0.1, 0, 0, 0);
            }
        }
    }
}

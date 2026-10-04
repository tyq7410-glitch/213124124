package com.deadvisuals;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

public final class Emotes {
    private Emotes() {
    }

    public static final String[] NAMES = {"GG", "<3", "LOL", "RIP", "!!", "EZ", "UwU", "o7"};
    private static final ParticleEffect[] PARTS = {
            ParticleTypes.FIREWORK, ParticleTypes.HEART, ParticleTypes.NOTE, ParticleTypes.SOUL,
            ParticleTypes.ANGRY_VILLAGER, ParticleTypes.HAPPY_VILLAGER, ParticleTypes.CHERRY_LEAVES, ParticleTypes.END_ROD
    };
    private static final Random R = new Random();

    private static String text;
    private static int age;
    private static int life;
    private static boolean active;

    public static void clear() {
        active = false;
    }

    public static void play(int i) {
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity p = mc.player;
        if (p == null || mc.world == null || !Mods.EMOTES.enabled) return;
        text = NAMES[i];
        age = 0;
        life = (int) Mods.EMOTES.v("Duration");
        active = true;
        if (Mods.EMOTES.on("Particles")) {
            Vec3d b = p.getPos().add(0, p.getHeight() + 0.3, 0);
            int n = Math.max(4, (int) (14 * Perf.scale()));
            for (int k = 0; k < n; k++) {
                mc.world.addParticle(PARTS[i],
                        b.x + (R.nextDouble() - 0.5) * 0.8, b.y + R.nextDouble() * 0.4, b.z + (R.nextDouble() - 0.5) * 0.8,
                        (R.nextDouble() - 0.5) * 0.1, 0.05 + R.nextDouble() * 0.1, (R.nextDouble() - 0.5) * 0.1);
            }
        }
        if (Mods.EMOTES.on("Sound")) {
            mc.getSoundManager().play(PositionedSoundInstance.master(
                    SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f + 0.1f * i, 0.7f));
        }
        if (i == 0) Helper.say("gg~");
        if (i == 6) Helper.say("uwu~");
    }

    public static void tick() {
        if (active && ++age > life) active = false;
    }

    public static void render(WorldRenderContext ctx, MinecraftClient mc, MatrixStack ms,
                              VertexConsumerProvider vp, Vec3d cam, float pt) {
        if (!active || !Mods.EMOTES.enabled || mc.player == null) return;
        ClientPlayerEntity p = mc.player;
        float prog = age / (float) life;
        Vec3d pos = p.getLerpedPos(pt).add(0, p.getHeight() + 0.65 + 0.5 * prog, 0);
        float pop = Math.min(1f, (age + pt) / 6f);
        float sc = 0.04f * (float) Mods.EMOTES.v("Scale") * pop;
        int alpha = prog > 0.8f ? (int) (255 * (1f - prog) / 0.2f) : 255;
        alpha = Math.max(8, Math.min(255, alpha));
        int rgb = Colors.pick(Mods.EMOTES.mode("Bubble color"), Mods.EMOTES.v("Hue"), Fx.ticks) & 0xFFFFFF;
        TextRenderer tr = mc.textRenderer;

        ms.push();
        ms.translate(pos.x - cam.x, pos.y - cam.y, pos.z - cam.z);
        ms.multiply(ctx.camera().getRotation());
        ms.scale(-sc, -sc, sc);
        float wd = tr.getWidth(text);
        tr.draw(text, -wd / 2f, 0, (alpha << 24) | rgb, false, ms.peek().getPositionMatrix(), vp,
                TextRenderer.TextLayerType.SEE_THROUGH, 0x70000000, 0xF000F0);
        ms.pop();
    }
}

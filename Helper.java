package com.deadvisuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

import java.util.Random;

public final class Helper {
    private Helper() {
    }

    private static final Random R = new Random();
    private static final String[] FACES = {"(^_^)", "(^o^)", "(>w<)", "(uwu)"};
    private static String msg = "";
    private static long start = -100000;
    private static long lastSpeak;
    private static long lastLow;

    public static void say(String t) {
        if (!Mods.HELPER.enabled) return;
        msg = t;
        start = System.currentTimeMillis();
        lastSpeak = start;
        if (Mods.HELPER.on("Voice")) chirp();
    }

    public static void sayLimited(String t, long cooldownMs) {
        if (System.currentTimeMillis() - lastSpeak < cooldownMs) return;
        say(t);
    }

    private static void chirp() {
        SoundEvent[] s = {SoundEvents.ENTITY_ALLAY_ITEM_GIVEN, SoundEvents.ENTITY_ALLAY_AMBIENT_WITH_ITEM,
                SoundEvents.ENTITY_ALLAY_AMBIENT_WITHOUT_ITEM};
        SoundEvent e = s[R.nextInt(s.length)];
        MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(
                e, (float) Mods.HELPER.v("Pitch") + R.nextFloat() * 0.25f, (float) Mods.HELPER.v("Volume")));
    }

    public static void tick(ClientPlayerEntity p) {
        if (!Mods.HELPER.enabled || !Mods.HELPER.on("Low hp warning")) return;
        long now = System.currentTimeMillis();
        if (p.isAlive() && p.getHealth() < 6f && now - lastLow > 10000) {
            lastLow = now;
            say("careful!! hp is low");
        }
    }

    public static void render(DrawContext g, MinecraftClient mc, int sw, int sh, long now) {
        if (!Mods.HELPER.enabled) return;
        long dur = (long) (Mods.HELPER.v("Duration") * 1000);
        long age = now - start;
        if (age > dur + 400 || msg.isEmpty()) return;
        float in = Math.min(1f, age / 250f);
        float out = age > dur ? 1f - (age - dur) / 400f : 1f;
        float a = Math.max(0f, Math.min(in, out));
        a = a * a * (3f - 2f * a);

        String face = FACES[Mods.HELPER.mode("Face")];
        int chars = (int) Math.min(msg.length(), age / 30);
        String shown = msg.substring(0, chars);
        int fw = mc.textRenderer.getWidth(face);
        int tw = mc.textRenderer.getWidth(msg);
        int w = fw + tw + 24;
        int h = 20;
        int x = 8 - (int) ((1f - a) * (w + 12));
        int y = sh - 52 + (int) (Math.sin(now / 250.0) * 1.5);
        g.fill(x, y, x + w, y + h, 0xC0090909);
        g.fill(x, y, x + 2, y + h, 0xFFB3122A);
        g.drawTextWithShadow(mc.textRenderer, face, x + 8, y + 6, 0xFFB3122A);
        g.drawTextWithShadow(mc.textRenderer, shown, x + 14 + fw, y + 6, 0xFFFFFFFF);
    }
}

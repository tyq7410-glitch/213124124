package com.deadvisuals;

import net.minecraft.util.math.MathHelper;

public final class Colors {
    private Colors() {
    }

    public static final String[] NAMES = {"Gold", "Red", "White", "Ice", "Rainbow", "Custom", "Purple", "Green",
            "Pink", "Cyan", "Orange", "Sunset", "Aurora",
            "Black", "Dark blue", "Dark green", "Dark aqua", "Dark red", "Dark purple", "Gray", "Dark gray",
            "Blue", "Aqua", "Light purple", "Yellow", "Lime", "Teal", "Magenta", "Coral", "Mint", "Sky"};

    private static final int[] PAL = {0xFFC83C, 0xE0192E, 0xFFFFFF, 0x66CCFF, 0, 0, 0xA855F7, 0x39FF88,
            0xFF5FA2, 0x22E5FF, 0xFF8A1F, 0, 0,
            0x0A0A0A, 0x0000AA, 0x00AA00, 0x00AAAA, 0xAA0000, 0xAA00AA, 0xAAAAAA, 0x555555,
            0x5555FF, 0x55FFFF, 0xFF55FF, 0xFFFF55, 0x84CC16, 0x14B8A6, 0xD946EF, 0xFF7F6E, 0x98FFD0, 0x38BDF8};

    public static int pick(int mode, double hueDeg, int tick) {
        switch (mode) {
            case 4:
                return MathHelper.hsvToRgb((tick % 120) / 120f, 0.8f, 1f);
            case 5:
                return MathHelper.hsvToRgb((float) (hueDeg / 360.0), 0.85f, 1f);
            case 11:
                return MathHelper.hsvToRgb(0.02f + 0.1f * (0.5f + 0.5f * (float) Math.sin(tick * 0.05)), 0.9f, 1f);
            case 12:
                return MathHelper.hsvToRgb(0.45f + 0.35f * (0.5f + 0.5f * (float) Math.sin(tick * 0.04)), 0.7f, 1f);
            default:
                return PAL[Math.max(0, Math.min(PAL.length - 1, mode))];
        }
    }

    public static int lerp(int a, int b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int r = 0;
        for (int s = 0; s <= 24; s += 8) {
            int ca = (a >> s) & 255;
            int cb = (b >> s) & 255;
            r |= ((int) (ca + (cb - ca) * t) & 255) << s;
        }
        return r;
    }
}

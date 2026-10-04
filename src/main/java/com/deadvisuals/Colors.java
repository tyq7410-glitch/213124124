package com.deadvisuals;

import net.minecraft.util.math.MathHelper;

public final class Colors {
    private Colors() {
    }

    public static int pick(int mode, double hueDeg, int tick) {
        switch (mode) {
            case 0:
                return 0xFFC83C;
            case 1:
                return 0xE0192E;
            case 2:
                return 0xFFFFFF;
            case 3:
                return 0x66CCFF;
            case 4:
                return MathHelper.hsvToRgb((tick % 120) / 120f, 0.8f, 1f);
            case 6:
                return 0xA855F7;
            case 7:
                return 0x39FF88;
            case 8:
                return 0xFF5FA2;
            case 9:
                return 0x22E5FF;
            case 10:
                return 0xFF8A1F;
            case 11:
                return MathHelper.hsvToRgb(0.02f + 0.1f * (0.5f + 0.5f * (float) Math.sin(tick * 0.05)), 0.9f, 1f);
            case 12:
                return MathHelper.hsvToRgb(0.45f + 0.35f * (0.5f + 0.5f * (float) Math.sin(tick * 0.04)), 0.7f, 1f);
            default:
                return MathHelper.hsvToRgb((float) (hueDeg / 360.0), 0.85f, 1f);
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

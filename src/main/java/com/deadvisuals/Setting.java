package com.deadvisuals;

public class Setting {
    public enum Type { BOOL, NUM, MODE }

    public final String name;
    public final Type type;
    public boolean bool;
    public double num, min, max, step;
    public int idx;
    public String[] modes;

    private Setting(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    public static Setting ofBool(String n, boolean d) {
        Setting s = new Setting(n, Type.BOOL);
        s.bool = d;
        return s;
    }

    public static Setting ofNum(String n, double d, double min, double max, double step) {
        Setting s = new Setting(n, Type.NUM);
        s.num = d;
        s.min = min;
        s.max = max;
        s.step = step;
        return s;
    }

    public static Setting ofMode(String n, int def, String... modes) {
        Setting s = new Setting(n, Type.MODE);
        s.idx = def;
        s.modes = modes;
        return s;
    }

    public void setNum(double v) {
        v = Math.round(v / step) * step;
        num = Math.max(min, Math.min(max, v));
    }

    public String display() {
        switch (type) {
            case BOOL:
                return bool ? "on" : "off";
            case NUM:
                if (step >= 1) return String.valueOf((int) Math.round(num));
                if (step < 0.1) return String.format("%.2f", num);
                return String.format("%.1f", num);
            default:
                return modes[idx];
        }
    }
}

package com.deadvisuals;

import java.util.LinkedHashMap;

public class Module {
    public final String name;
    public final String cat;
    public boolean enabled;
    public boolean open;
    public float anim;
    public float openA;
    public final LinkedHashMap<String, Setting> settings = new LinkedHashMap<>();

    public Module(String name, String cat, boolean enabled) {
        this.name = name;
        this.cat = cat;
        this.enabled = enabled;
        this.anim = enabled ? 1f : 0f;
    }

    public Module b(String n, boolean d) {
        settings.put(n, Setting.ofBool(n, d));
        return this;
    }

    public Module n(String n, double d, double min, double max, double step) {
        settings.put(n, Setting.ofNum(n, d, min, max, step));
        return this;
    }

    public Module m(String n, int def, String... modes) {
        settings.put(n, Setting.ofMode(n, def, modes));
        return this;
    }

    public boolean on(String n) {
        return settings.get(n).bool;
    }

    public double v(String n) {
        return settings.get(n).num;
    }

    public int mode(String n) {
        return settings.get(n).idx;
    }
}

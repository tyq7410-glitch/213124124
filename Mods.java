package com.deadvisuals;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class Mods {
    private Mods() {
    }

    public static final String[] CATS = {"combat", "cosmetics", "emotes", "hud", "world", "extras"};
    private static final String[] CM = Colors.NAMES;
    private static final String[] WS = {"Angel", "Demon", "Dead", "Neon", "Ice", "Gold", "Galaxy",
            "Blood", "Sakura", "Void", "Toxic", "Fire", "Mono"};
    private static final String[] SKYC = withOff(Colors.NAMES);

    private static String[] withOff(String[] n) {
        String[] r = new String[n.length + 1];
        r[0] = "Off";
        System.arraycopy(n, 0, r, 1, n.length);
        return r;
    }
    public static final List<Module> ALL = new ArrayList<>();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("deadvisuals.json");

    private static Module reg(Module m) {
        ALL.add(m);
        return m;
    }

    public static final Module HIT = reg(new Module("hit particles", "combat", true)
            .m("Color", 0, CM).n("Hue", 0, 0, 360, 5).m("Crit color", 1, CM)
            .n("Amount", 14, 4, 60, 1).n("Size", 1.2, 0.4, 3, 0.1).n("Spread", 0.7, 0.2, 2, 0.1));

    public static final Module CRIT = reg(new Module("crit effects", "combat", true)
            .m("Type", 2, "Crit", "Enchant", "Both", "Totem", "Spark")
            .n("Amount", 18, 4, 60, 1).n("Speed", 0.8, 0.2, 2.5, 0.1));

    public static final Module NUM = reg(new Module("damage numbers", "combat", true)
            .n("Scale", 1, 0.4, 3, 0.1).n("Lifetime", 30, 10, 80, 1).n("Rise", 0.8, 0.2, 3, 0.1)
            .m("Color", 0, CM).n("Hue", 0, 0, 360, 5).m("Crit color", 1, CM)
            .m("Decimals", 1, "0", "1", "2").b("Fade", true).b("Through walls", true).b("Show heal", true));

    public static final Module THUD = reg(new Module("target hud", "combat", true)
            .m("Style", 0, "Dead", "Minimal", "Compact")
            .n("X offset", 30, -300, 300, 1).n("Y offset", 30, -200, 200, 1).n("Scale", 1, 0.6, 2, 0.1)
            .n("Duration", 4, 2, 12, 1).n("Accent hue", 350, 0, 360, 5)
            .b("Name", true).b("Health bar", true).b("Health text", true).b("Armor", true)
            .b("Distance", true).b("Absorption", true).b("Held item", false));

    public static final Module TRAJ = reg(new Module("trajectory", "combat", true)
            .m("Color", 3, CM).n("Hue", 190, 0, 360, 5).m("Hit color", 1, CM)
            .n("Max steps", 120, 20, 300, 5).b("Landing marker", true).n("Marker size", 0.25, 0.1, 0.8, 0.05)
            .b("Bow", true).b("Crossbow", true).b("Trident", true).b("Throwables", true));

    public static final Module MARK = reg(new Module("hit marker", "combat", true)
            .n("Size", 6, 3, 16, 1).n("Gap", 4, 1, 10, 1).n("Duration", 5, 2, 20, 1)
            .m("Color", 2, CM).n("Hue", 0, 0, 360, 5).m("Crit color", 1, CM));

    public static final Module SOUND = reg(new Module("hit sound", "combat", false)
            .m("Sound", 0, "Orb", "Chime", "Arrow", "Crit", "Allay")
            .n("Volume", 0.6, 0.1, 1, 0.1).n("Pitch", 1.2, 0.5, 2, 0.1));

    public static final Module KILL = reg(new Module("kill effect", "combat", true)
            .m("Type", 0, "Soul", "Smoke", "Totem", "Spark", "Hearts", "Sakura")
            .n("Amount", 30, 10, 100, 5).m("Sound", 1, "None", "Chime", "Orb", "Totem"));

    public static final Module WINGS = reg(new Module("wings", "cosmetics", true)
            .m("Style", 2, WS)
            .n("Scale", 1, 0.5, 2, 0.1).n("Flap speed", 1, 0.2, 3, 0.1).n("Flap angle", 0.4, 0, 1, 0.05)
            .n("Spread", 0.5, 0.1, 1.2, 0.05).n("Height", 0, -4, 6, 0.5).b("Hide with elytra", true));

    public static final Module AURA = reg(new Module("aura", "cosmetics", true)
            .m("Style", 0, CM).n("Hue", 300, 0, 360, 5)
            .m("Shape", 0, "Orbit", "Helix", "Halo", "Ring", "Vortex", "Sparkle", "Sakura", "Hearts", "Flames", "Notes")
            .n("Count", 3, 1, 8, 1).n("Radius", 0.9, 0.4, 2, 0.1).n("Speed", 1, 0.3, 3, 0.1)
            .n("Size", 0.8, 0.4, 2, 0.1).n("Height", 0.9, 0, 2, 0.1));

    public static final Module TRAIL = reg(new Module("trail", "cosmetics", false)
            .m("Type", 0, "Dust", "Soul", "Flame", "End rod", "Hearts", "Snow", "Electric", "Sakura", "Portal", "Enchant", "Smoke", "Glow", "Note")
            .m("Color", 4, CM).n("Hue", 0, 0, 360, 5).n("Density", 2, 1, 6, 1));

    public static final Module PETS = reg(new Module("pet", "cosmetics", true)
            .m("Style", 0, "Ghost", "Imp", "Cube", "Cat", "Fox", "Slime", "Pumpkin", "Frost", "Gold")
            .n("Scale", 1, 0.5, 2, 0.1).n("Distance", 1.2, 0.6, 3, 0.1).n("Height", 1.5, 0.5, 2.5, 0.1)
            .n("Speed", 6, 2, 15, 1).n("Bob", 1, 0, 2, 0.1).b("Sparkles", true));

    public static final Module EMOTES = reg(new Module("emotes", "emotes", true)
            .n("Scale", 1.2, 0.5, 3, 0.1).n("Duration", 50, 20, 120, 5)
            .b("Particles", true).b("Sound", true).m("Bubble color", 0, CM).n("Hue", 0, 0, 360, 5));

    private static final String[] ANCH = {"Top left", "Top right", "Bottom left", "Bottom right"};

    public static final Module COMBO = reg(new Module("combo", "hud", true)
            .m("Color", 11, CM).n("Hue", 0, 0, 360, 5).n("Timeout", 3, 1, 10, 1)
            .n("Scale", 1.4, 0.8, 3, 0.1).n("Y offset", 40, 10, 200, 1).b("Show kills", true));

    public static final Module ARMOR = reg(new Module("armor hud", "hud", true)
            .m("Anchor", 1, ANCH).n("X offset", 6, -300, 300, 1).n("Y offset", 6, -300, 300, 1)
            .b("Held item", true).b("Percent", true));

    public static final Module ITEMS = reg(new Module("item counter", "hud", true)
            .m("Anchor", 0, ANCH).n("X offset", 6, -300, 300, 1).n("Y offset", 60, -300, 300, 1)
            .b("Totems", true).b("Pearls", true).b("Golden apples", true).b("Arrows", false).b("XP bottles", false));

    public static final Module EFFECTS = reg(new Module("effects hud", "hud", true)
            .m("Anchor", 1, ANCH).n("X offset", 6, -300, 300, 1).n("Y offset", 70, -300, 300, 1));

    public static final Module INFO = reg(new Module("info hud", "hud", true)
            .m("Anchor", 0, ANCH).n("X offset", 6, -300, 300, 1).n("Y offset", 6, -300, 300, 1)
            .b("FPS", true).b("Ping", true).b("Coords", true).b("Direction", true)
            .b("Speed", false).b("Hunger", false).b("Light", false).b("Server", false));

    public static final Module LOWHP = reg(new Module("low hp pulse", "hud", true)
            .n("Threshold", 30, 10, 60, 5).m("Color", 1, CM).n("Hue", 0, 0, 360, 5)
            .n("Thickness", 14, 4, 40, 2).n("Speed", 1, 0.3, 3, 0.1));

    public static final Module HITBOX = reg(new Module("hitboxes", "world", false)
            .b("Players", true).b("Mobs", false).n("Range", 32, 8, 64, 2)
            .m("Color", 3, CM).n("Hue", 190, 0, 360, 5));

    public static final Module BLOCKO = reg(new Module("block overlay", "world", true)
            .m("Color", 11, CM).n("Hue", 0, 0, 360, 5));

    public static final Module RING = reg(new Module("item radius", "world", true)
            .n("Radius", 5, 1, 20, 0.5).n("Inner radius", 0, 0, 20, 0.5).n("Height", 0.05, 0, 2, 0.05)
            .m("Color", 9, CM).n("Hue", 190, 0, 360, 5).b("Only renamed items", true).b("Pulse", true));

    public static final Module MOBHP = reg(new Module("mob health", "world", true)
            .n("Range", 20, 6, 48, 2).b("Passive too", false).n("Scale", 1, 0.5, 2.5, 0.1));

    public static final Module CROSS = reg(new Module("crosshair", "hud", true)
            .m("Style", 0, "Cross", "Dot", "Circle", "Plus gap", "X")
            .n("Size", 5, 2, 16, 1).n("Gap", 2, 0, 10, 1).n("Thickness", 1, 1, 3, 1)
            .m("Color", 2, CM).n("Hue", 0, 0, 360, 5).b("Outline", true).b("Dynamic", true).b("Hit flash", true));

    public static final Module FILTER = reg(new Module("screen filter", "hud", false)
            .m("Color", 6, CM).n("Hue", 0, 0, 360, 5).n("Strength", 15, 0, 60, 1)
            .b("Vignette", true).n("Vignette strength", 40, 0, 100, 5));

    public static final Module SCREENFX = reg(new Module("screen particles", "hud", false)
            .m("Type", 0, "Snow", "Sakura", "Stars", "Embers")
            .n("Density", 60, 10, 200, 5).n("Speed", 1, 0.3, 3, 0.1).n("Size", 2, 1, 5, 1));

    public static final Module CD = reg(new Module("cooldowns", "hud", true)
            .m("Anchor", 2, ANCH).n("X offset", 6, -300, 300, 1).n("Y offset", 70, -300, 300, 1)
            .n("Pearl", 1, 0, 60, 0.5).n("Chorus", 1, 0, 60, 0.5).n("Gapple", 0, 0, 60, 0.5));

    public static final Module SKY = reg(new Module("sky & weather", "world", true)
            .m("Time", 0, "Off", "Sunrise", "Day", "Noon", "Sunset", "Night", "Midnight", "Cycle", "Custom")
            .n("Custom time", 6000, 0, 24000, 250).n("Cycle speed", 5, 1, 60, 1)
            .m("Weather", 0, "Default", "Clear", "Rain", "Thunder")
            .m("Sky color", 0, SKYC)
            .n("Sky hue", 280, 0, 360, 5).n("Sky strength", 0.6, 0, 1, 0.05));

    public static final Module CAPE = reg(new Module("cape", "cosmetics", false)
            .m("Style", 2, WS).n("Scale", 1, 0.5, 2, 0.1).b("Hide with elytra", true));

    public static final Module ZOOM = reg(new Module("zoom", "extras", true)
            .n("Level", 30, 30, 70, 1).b("Smooth", true));

    public static final Module TWEAKS = reg(new Module("visual tweaks", "extras", false)
            .b("No FOV effects", true).b("No distortion", true).b("No view bobbing", false));

    public static final Module REACH = reg(new Module("reach display", "hud", true)
            .m("Anchor", 0, ANCH).n("X offset", 6, -300, 300, 1).n("Y offset", 100, -300, 300, 1));

    public static final Module JUMP = reg(new Module("jump particles", "cosmetics", true)
            .m("Color", 4, CM).n("Hue", 0, 0, 360, 5).m("Type", 0, "Ring", "Double ring")
            .n("Amount", 20, 8, 60, 1).n("Radius", 0.6, 0.3, 1.5, 0.1).n("Size", 1, 0.4, 3, 0.1));

    public static final Module TRACK = reg(new Module("projectile trail", "world", true)
            .m("Color", 3, CM).n("Hue", 190, 0, 360, 5).n("Size", 0.8, 0.4, 2, 0.1)
            .n("Density", 2, 1, 6, 1).b("Only mine", true));

    public static final Module WATER = reg(new Module("watermark", "hud", true)
            .m("Position", 0, "Top center", "Top left", "Top right").n("Y offset", 4, 0, 100, 1)
            .b("Username", true).b("FPS", true).b("Ping", true).b("Clock", true));

    public static final Module KEYS = reg(new Module("keystrokes", "hud", true)
            .m("Anchor", 3, ANCH).n("X offset", 6, -300, 300, 1).n("Y offset", 70, -300, 300, 1)
            .b("Space", true).b("Mouse", true).b("CPS", true));

    public static final Module HELPER = reg(new Module("helper", "extras", true)
            .b("Voice", true).n("Pitch", 1.6, 0.8, 2, 0.1).n("Volume", 0.6, 0.1, 1, 0.1)
            .b("Hit messages", true).b("Low hp warning", true).n("Duration", 3, 1, 8, 1)
            .m("Face", 0, "^_^", "^o^", ">w<", "uwu")
            .m("Style", 0, "Corner", "Island"));

    public static final Module MARKS = reg(new Module("marks", "extras", true)
            .b("Mark on death", true).m("Color", 9, CM).n("Hue", 190, 0, 360, 5).n("Max marks", 5, 1, 12, 1)
            .b("Show distance", true).b("Beam", true).n("Scale", 1, 0.5, 3, 0.1));

    public static final Module PERF = reg(new Module("performance", "extras", true)
            .b("Unlimited FPS", true).b("VSync off", true).b("No entity shadows", true)
            .b("No clouds", true).b("Biome blend 0", true).b("Entities closer", true)
            .b("Smooth light off", false)
            .b("Adaptive effects", true).n("Low FPS limit", 60, 20, 144, 5));

    public static final Module TITLE = reg(new Module("main menu", "extras", true)
            .b("Custom menu", true).b("Intro", true).n("Particles", 60, 10, 150, 5)
            .n("Accent hue", 350, 0, 360, 5));

    static {
        load();
    }

    public static void load() {
        try {
            if (!Files.exists(PATH)) return;
            JsonObject root = JsonParser.parseString(Files.readString(PATH)).getAsJsonObject();
            for (Module m : ALL) {
                if (!root.has(m.name)) continue;
                JsonObject o = root.getAsJsonObject(m.name);
                if (o.has("enabled")) m.enabled = o.get("enabled").getAsBoolean();
                m.anim = m.enabled ? 1f : 0f;
                if (!o.has("settings")) continue;
                JsonObject ss = o.getAsJsonObject("settings");
                for (Setting s : m.settings.values()) {
                    if (!ss.has(s.name)) continue;
                    switch (s.type) {
                        case BOOL:
                            s.bool = ss.get(s.name).getAsBoolean();
                            break;
                        case NUM:
                            s.setNum(ss.get(s.name).getAsDouble());
                            break;
                        default:
                            s.idx = Math.max(0, Math.min(s.modes.length - 1, ss.get(s.name).getAsInt()));
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    public static void save() {
        try {
            JsonObject root = new JsonObject();
            for (Module m : ALL) {
                JsonObject o = new JsonObject();
                o.addProperty("enabled", m.enabled);
                JsonObject ss = new JsonObject();
                for (Setting s : m.settings.values()) {
                    switch (s.type) {
                        case BOOL:
                            ss.addProperty(s.name, s.bool);
                            break;
                        case NUM:
                            ss.addProperty(s.name, s.num);
                            break;
                        default:
                            ss.addProperty(s.name, s.idx);
                    }
                }
                o.add("settings", ss);
                root.add(m.name, o);
            }
            Files.writeString(PATH, new GsonBuilder().setPrettyPrinting().create().toJson(root));
        } catch (Exception ignored) {
        }
    }
}

package dev.auroravisuals;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.awt.Color;
import java.nio.file.Files;
import java.nio.file.Path;

public class Config {
    public static class Pos {
        public float x, y;
        public transient float w = 60, h = 14;
        public Pos() {}
        public Pos(float x, float y) { this.x = x; this.y = y; }
    }
    public static final String[] PRESETS = {"Ocean", "Sunset", "Mint", "Violet", "Sakura", "Rainbow"};
    public static final String[] TEXT_MODES = {"White", "Accent"};
    public Pos pPanel = new Pos(0.01f, 0.01f), pKeys = new Pos(0.01f, 0.34f), pArmor = new Pos(0.9f, 0.01f),
               pPot = new Pos(0.78f, 0.3f), pTarget = new Pos(0.56f, 0.6f), pCd = new Pos(0.01f, 0.5f);
    public boolean cdHud = true, cdReady = true, combatTimer = true, lowHp = true, targetArmor = true, showReach = true, pops = false;
    public int combatSecs = 15, lowHpThreshold = 8;
    // info 2
    public boolean day = false, worldTime = false, biome = false, light = false, angles = false, heldItem = false,
                   netherCoords = false, hpText = false, hungerText = false, xpLevel = false, session = false,
                   fpsColor = true, pingColor = true, keysMouse = true;
    // visuals 2
    public boolean compass = false, lowFood = true, atkBar = false, frame = false, hurtFlash = false, kills = false,
                   bestCombo = false, chTargetColor = true;
    public int chAlpha = 255, frameThick = 3;
    // cosmetics 2
    public int pShape = 0;
    public boolean ring = false, helix = false, cloud = false, jumpRings = false, lvlBurst = false, fireflies = false;
    public float ringRadius = 0.7f;
    // colors
    public boolean accentCustom = false, chCustom = false;
    public int accentRgb = 0x4DA6FF, chRgb = 0xFFFFFF, partRgb = 0x4DA6FF;
    // cosmetics 3
    public boolean snow = false, heartAura = false, notes = false, electric = false, flameFeet = false,
                   souls = false, sakura = false, enchant = false, totemAura = false;
    public int cosIntensity = 2, killType = 0;
    public boolean wings = false, killEffect = true, noBob = false, noFovFx = false, hudBorder = false, accentBar = true;
    public float wingSize = 1f;
    public int textMode = 0, preset = 0;
    public static Config I = new Config();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("auroravisuals.json");

    // HUD
    public boolean watermark = true, fps = true, coords = true, cps = true, ping = true, keystrokes = true;
    public int hudX = 4, hudY = 4;
    public float hudScale = 1f;
    public boolean textShadow = true;
    // Crosshair
    public boolean crosshair = true, chDot = false, chOutline = true, chCross = true, chRainbow = false, chDynamic = false;
    public int chSize = 5, chGap = 3, chThick = 1;
    public boolean hitmarker = true;
    public int hitSize = 5, hitTicks = 8;
    // Theme
    public float hue = 0.58f, rainbowSpeed = 1f;
    public boolean rainbow = false;
    public int bgOpacity = 110;
    // Info widgets
    public boolean speed = false, direction = false, clock = false, memory = false, server = false, combo = true;
    public boolean armorHud = true, potionHud = true, targetHud = true;
    // Effects
    public boolean trail = false, trailFirstPerson = false, hat = false, hitParticles = true;
    public int trailDensity = 2, hatPoints = 14, hitCount = 8, hitType = 1;
    public float hatSize = 0.55f, hatHeight = 0.25f;
    public int hitSound = 0, pColor = 0, zoomFov = 40;
    public boolean state = false, itemCount = false, chCircle = false;
    // Cosmetics
    public boolean cosFirst = false, halo = false, aura = false, footsteps = false;
    public float haloSize = 0.35f, haloHeight = 0.3f, auraRadius = 0.8f, auraSpeed = 1f;
    public int auraCount = 6;

    public int accent() {
        if (accentCustom && !rainbow) return 0xFF000000 | accentRgb;
        float h = hue;
        if (rainbow) h += (System.currentTimeMillis() % 10000L) / 10000f * rainbowSpeed * 3f;
        return Color.HSBtoRGB(h % 1f, 0.65f, 1f) | 0xFF000000;
    }
    public int bg() { return (bgOpacity << 24); }
    public int textCol() { return textMode == 1 ? accent() : 0xFFFFFFFF; }
    public static void applyPreset(int i) {
        float[] hues = {0.58f, 0.04f, 0.42f, 0.75f, 0.92f};
        I.preset = i;
        I.rainbow = i >= hues.length;
        if (i < hues.length) I.hue = hues[i];
    }
    public static void resetPositions() {
        Config d = new Config();
        I.pPanel = d.pPanel; I.pKeys = d.pKeys; I.pArmor = d.pArmor; I.pPot = d.pPot; I.pTarget = d.pTarget; I.pCd = d.pCd;
    }
    public static void reset() { I = new Config(); save(); }

    private static void keepLook(Config o, Config n) {
        n.hue = o.hue; n.rainbow = o.rainbow; n.rainbowSpeed = o.rainbowSpeed; n.bgOpacity = o.bgOpacity; n.preset = o.preset;
        n.accentCustom = o.accentCustom; n.accentRgb = o.accentRgb; n.chCustom = o.chCustom; n.chRgb = o.chRgb; n.partRgb = o.partRgb;
        n.hudScale = o.hudScale; n.textShadow = o.textShadow; n.textMode = o.textMode;
        n.pPanel = o.pPanel; n.pKeys = o.pKeys; n.pArmor = o.pArmor; n.pPot = o.pPot; n.pTarget = o.pTarget; n.pCd = o.pCd;
    }

    /** 0 = minimal, 1 = PvP, 2 = cosmetic. Theme and HUD positions are kept. */
    public static void applyProfile(int i) {
        Config n = new Config();
        keepLook(I, n);
        if (i == 0) {
            n.watermark = false; n.coords = false; n.ping = false; n.keystrokes = false; n.combo = false;
            n.armorHud = false; n.potionHud = false; n.targetHud = false; n.cdHud = false; n.lowHp = false;
            n.crosshair = false; n.hitmarker = false; n.hitParticles = false; n.killEffect = false; n.lowFood = false;
        } else if (i == 1) {
            n.pops = true; n.itemCount = true; n.atkBar = true; n.hurtFlash = true; n.kills = true; n.bestCombo = true;
            n.hitSound = 1; n.chDot = true; n.chOutline = true;
        } else {
            n.wings = true; n.halo = true; n.aura = true; n.trail = true; n.ring = true; n.footsteps = true;
            n.sakura = true; n.heartAura = true; n.cosFirst = true; n.watermark = true;
        }
        I = n; save();
    }
    private static Path slot(int n) { return FabricLoader.getInstance().getConfigDir().resolve("auroravisuals_slot" + n + ".json"); }
    public static void saveSlot(int n) { try { Files.writeString(slot(n), GSON.toJson(I)); } catch (Exception ignored) {} }
    public static boolean loadSlot(int n) {
        try {
            if (!Files.exists(slot(n))) return false;
            Config c = GSON.fromJson(Files.readString(slot(n)), Config.class);
            if (c == null) return false;
            I = c; save(); return true;
        } catch (Exception e) { return false; }
    }

    public static void load() {
        try { if (Files.exists(FILE)) I = GSON.fromJson(Files.readString(FILE), Config.class); } catch (Exception ignored) {}
        if (I == null) I = new Config();
    }
    public static void save() {
        try { Files.writeString(FILE, GSON.toJson(I)); } catch (Exception ignored) {}
    }
}

package dev.auroravisuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import java.awt.Color;
import java.util.List;
import java.util.Random;

public class Extras {
    private static final Random R = new Random();
    public static final String[] HIT_TYPES = {"Crit", "Accent dust", "Sparks", "Hearts", "Totem", "Notes", "Flames", "Electric"};
    public static final String[] KILL_TYPES = {"Totem", "Firework", "Hearts", "Souls"};
    public static final String[] SOUNDS = {"Off", "Orb", "Ding", "Crit"};
    public static final String[] COLORS = {"Theme", "Random", "White", "Custom"};
    public static final String[] SHAPES = {"Dust", "End rod", "Soul flame", "Sparkle", "Flame", "Cloud"};
    private static final long START = System.currentTimeMillis();
    private static int kills = 0, maxCombo = 0, lastLevel = -1;
    private static boolean wasGround = true;
    public static int combo = 0;
    private static long lastHit = 0, lastDmgTime = 0;
    private static double lx, lz, speed;
    private static float spin = 0, lastHealth = 0, lastDmg = 0;
    private static Entity lastTarget;
    private static boolean zooming = false, killDone = false;
    private static long combatUntil = 0;
    private static int lastTotems = 0, popCount = 0;
    private static final Item[] CD_ITEMS = {Items.ENDER_PEARL, Items.CHORUS_FRUIT, Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE, Items.SHIELD, Items.WIND_CHARGE};
    private static final float[] cdProg = new float[CD_ITEMS.length];
    private static final float[] cdPrev = new float[CD_ITEMS.length];
    private static int savedFov = 70;

    private static int col() {
        Config c = Config.I;
        if (c.pColor == 1) return Color.HSBtoRGB(R.nextFloat(), 0.7f, 1f);
        if (c.pColor == 2) return 0xFFFFFFFF;
        if (c.pColor == 3) return 0xFF000000 | c.partRgb;
        return c.accent();
    }
    private static ParticleEffect dust(float size) {
        switch (Config.I.pShape) {
            case 1: return ParticleTypes.END_ROD;
            case 2: return ParticleTypes.SOUL_FIRE_FLAME;
            case 3: return ParticleTypes.HAPPY_VILLAGER;
            case 4: return ParticleTypes.FLAME;
            case 5: return ParticleTypes.CLOUD;
            default: return new DustParticleEffect(col() & 0xFFFFFF, size);
        }
    }

    private static ParticleEffect killParticle() {
        switch (Config.I.killType) {
            case 1: return ParticleTypes.FIREWORK;
            case 2: return ParticleTypes.HEART;
            case 3: return ParticleTypes.SOUL;
            default: return ParticleTypes.TOTEM_OF_UNDYING;
        }
    }

    public static void tick(MinecraftClient mc) {
        if (mc.player == null || mc.world == null) return;
        Config c = Config.I;
        var p = mc.player;
        speed = Math.hypot(p.getX() - lx, p.getZ() - lz) * 20;
        lx = p.getX(); lz = p.getZ();
        long nowMs = System.currentTimeMillis();
        if (p.hurtTime > 0) combatUntil = nowMs + c.combatSecs * 1000L;
        int totems = p.getInventory().count(Items.TOTEM_OF_UNDYING);
        if (totems < lastTotems && p.isAlive()) popCount++;
        lastTotems = totems;
        for (int i = 0; i < CD_ITEMS.length; i++) {
            float pr = p.getItemCooldownManager().getCooldownProgress(new ItemStack(CD_ITEMS[i]), 0f);
            if (cdPrev[i] > 0 && pr <= 0 && c.cdReady)
                mc.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.5f, 0.6f));
            cdPrev[i] = pr; cdProg[i] = pr;
        }
        if (combo > 0 && System.currentTimeMillis() - lastHit > 3000) combo = 0;
        boolean third = !mc.options.getPerspective().isFirstPerson();
        boolean show = third || c.cosFirst;

        // zoom
        if (AuroraVisuals.zoomKey != null && AuroraVisuals.zoomKey.isPressed()) {
            if (!zooming) { savedFov = mc.options.getFov().getValue(); zooming = true; }
            mc.options.getFov().setValue(c.zoomFov);
        } else if (zooming) { mc.options.getFov().setValue(savedFov); zooming = false; }

        // target damage tracking
        if (mc.targetedEntity instanceof LivingEntity le) {
            if (le == lastTarget && le.getHealth() < lastHealth) { lastDmg = lastHealth - le.getHealth(); lastDmgTime = System.currentTimeMillis(); }
            lastTarget = le; lastHealth = le.getHealth();
        }

        if (c.noBob && mc.options.getBobView().getValue()) mc.options.getBobView().setValue(false);
        if (c.noFovFx && mc.options.getFovEffectScale().getValue() > 0) mc.options.getFovEffectScale().setValue(0.0);
        if (lastTarget instanceof LivingEntity dead && dead.isDead()) {
            if (!killDone && System.currentTimeMillis() - lastHit < 5000) {
                killDone = true; kills++;
                if (c.killEffect) for (int i = 0; i < 30; i++)
                    mc.world.addParticleClient(killParticle(), dead.getX(), dead.getY() + dead.getHeight() / 2, dead.getZ(),
                        (R.nextDouble() - 0.5) * 0.5, R.nextDouble() * 0.4, (R.nextDouble() - 0.5) * 0.5);
            }
        } else killDone = false;

        spin += 0.12f * c.auraSpeed;
        if (c.wings && show) {
            double yaw = Math.toRadians(p.getBodyYaw());
            double fx = -Math.sin(yaw), fz = Math.cos(yaw), rx = -Math.cos(yaw), rz = -Math.sin(yaw);
            double flap = 0.85 + 0.15 * Math.sin(spin * 3);
            for (int side = -1; side <= 1; side += 2)
                for (int i = 0; i < 10; i++) {
                    double t = i / 9.0;
                    double lat = (0.2 + t * 1.1 * c.wingSize) * flap * side;
                    double y = p.getY() + 1.3 + Math.sin(t * Math.PI * 0.9) * 0.6 * c.wingSize - t * 0.5;
                    double back = -(0.25 + t * 0.35);
                    mc.world.addParticleClient(dust(0.6f), p.getX() + fx * back + rx * lat, y, p.getZ() + fz * back + rz * lat, 0, 0, 0);
                }
        }
        if (c.ring && show) {
            double y = p.getY() + 0.9;
            for (int k = 0; k < 20; k++) {
                double a = Math.PI * 2 * k / 20 + spin * 0.5;
                mc.world.addParticleClient(dust(0.5f), p.getX() + Math.cos(a) * c.ringRadius, y + Math.sin(spin * 2 + k * 0.3) * 0.1, p.getZ() + Math.sin(a) * c.ringRadius, 0, 0, 0);
            }
        }
        if (c.helix && show) {
            for (int k = 0; k < 12; k++) {
                double t = k / 12.0, a = t * Math.PI * 4 + spin;
                mc.world.addParticleClient(dust(0.5f), p.getX() + Math.cos(a) * 0.6, p.getY() + t * 2.0, p.getZ() + Math.sin(a) * 0.6, 0, 0, 0);
            }
        }
        if (c.cloud && show) {
            double cy = p.getY() + p.getHeight() + 0.9;
            for (int k = 0; k < 2; k++) {
                double ox = (R.nextDouble() - 0.5) * 0.8, oz = (R.nextDouble() - 0.5) * 0.8;
                mc.world.addParticleClient(ParticleTypes.CLOUD, p.getX() + ox, cy, p.getZ() + oz, 0, 0, 0);
                mc.world.addParticleClient(ParticleTypes.FALLING_WATER, p.getX() + ox, cy - 0.1, p.getZ() + oz, 0, 0, 0);
            }
        }
        if (c.jumpRings && wasGround && !p.isOnGround() && p.getVelocity().y > 0.2 && show) {
            for (int k = 0; k < 24; k++) {
                double a = Math.PI * 2 * k / 24;
                mc.world.addParticleClient(dust(0.8f), p.getX() + Math.cos(a) * 0.7, p.getY() + 0.05, p.getZ() + Math.sin(a) * 0.7, Math.cos(a) * 0.1, 0.02, Math.sin(a) * 0.1);
            }
        }
        wasGround = p.isOnGround();
        if (lastLevel >= 0 && p.experienceLevel > lastLevel && c.lvlBurst)
            for (int k = 0; k < 40; k++)
                mc.world.addParticleClient(ParticleTypes.FIREWORK, p.getX(), p.getY() + 1, p.getZ(), (R.nextDouble() - 0.5) * 0.4, R.nextDouble() * 0.3, (R.nextDouble() - 0.5) * 0.4);
        lastLevel = p.experienceLevel;
        if (c.fireflies && R.nextInt(3) == 0)
            mc.world.addParticleClient(ParticleTypes.END_ROD, p.getX() + (R.nextDouble() - 0.5) * 12, p.getY() + R.nextDouble() * 3, p.getZ() + (R.nextDouble() - 0.5) * 12, 0, 0.01, 0);
        int ci = c.cosIntensity;
        double headY = p.getY() + p.getHeight();
        if (show) {
            if (c.snow) for (int q = 0; q < ci * 2; q++)
                mc.world.addParticleClient(ParticleTypes.SNOWFLAKE, p.getX() + (R.nextDouble() - 0.5) * 8, p.getY() + 2 + R.nextDouble() * 3, p.getZ() + (R.nextDouble() - 0.5) * 8, 0, -0.02, 0);
            if (c.sakura) for (int q = 0; q < ci; q++)
                mc.world.addParticleClient(ParticleTypes.CHERRY_LEAVES, p.getX() + (R.nextDouble() - 0.5) * 6, p.getY() + 2 + R.nextDouble() * 2, p.getZ() + (R.nextDouble() - 0.5) * 6, 0, -0.01, 0);
            if (c.heartAura && R.nextInt(4) == 0) for (int q = 0; q < ci; q++)
                mc.world.addParticleClient(ParticleTypes.HEART, p.getX() + (R.nextDouble() - 0.5), headY + 0.2 + R.nextDouble() * 0.3, p.getZ() + (R.nextDouble() - 0.5), 0, 0.02, 0);
            if (c.notes && R.nextInt(3) == 0)
                mc.world.addParticleClient(ParticleTypes.NOTE, p.getX() + (R.nextDouble() - 0.5), headY + 0.4, p.getZ() + (R.nextDouble() - 0.5), R.nextDouble(), 0, 0);
            if (c.electric) for (int q = 0; q < ci; q++)
                mc.world.addParticleClient(ParticleTypes.ELECTRIC_SPARK, p.getX() + (R.nextDouble() - 0.5), p.getY() + R.nextDouble() * 1.8, p.getZ() + (R.nextDouble() - 0.5), 0, 0, 0);
            if (c.flameFeet && p.isSprinting()) for (int q = 0; q < ci; q++)
                mc.world.addParticleClient(ParticleTypes.FLAME, p.getX() + (R.nextDouble() - 0.5) * 0.4, p.getY() + 0.05, p.getZ() + (R.nextDouble() - 0.5) * 0.4, 0, 0.02, 0);
            if (c.souls && R.nextInt(2) == 0)
                mc.world.addParticleClient(ParticleTypes.SOUL, p.getX() + (R.nextDouble() - 0.5) * 1.2, p.getY() + R.nextDouble() * 1.5, p.getZ() + (R.nextDouble() - 0.5) * 1.2, 0, 0.03, 0);
            if (c.enchant) for (int q = 0; q < ci; q++)
                mc.world.addParticleClient(ParticleTypes.ENCHANT, p.getX() + (R.nextDouble() - 0.5) * 1.6, headY + 0.3 + R.nextDouble() * 0.5, p.getZ() + (R.nextDouble() - 0.5) * 1.6, (R.nextDouble() - 0.5) * 0.4, -0.3, (R.nextDouble() - 0.5) * 0.4);
            if (c.totemAura && R.nextInt(3) == 0)
                mc.world.addParticleClient(ParticleTypes.TOTEM_OF_UNDYING, p.getX() + (R.nextDouble() - 0.5), p.getY() + R.nextDouble() * 1.8, p.getZ() + (R.nextDouble() - 0.5), 0, 0.05, 0);
        }
        if (c.trail && (third || c.trailFirstPerson)) {
            for (int i = 0; i < c.trailDensity; i++)
                mc.world.addParticleClient(dust(1f), p.getX() + (R.nextDouble() - 0.5) * 0.4, p.getY() + 0.1 + R.nextDouble() * 0.2,
                    p.getZ() + (R.nextDouble() - 0.5) * 0.4, 0, 0, 0);
        }
        if (c.footsteps && p.isOnGround() && speed > 1.0) {
            mc.world.addParticleClient(dust(0.8f), p.getX() + (R.nextDouble() - 0.5) * 0.3, p.getY() + 0.05, p.getZ() + (R.nextDouble() - 0.5) * 0.3, 0, 0, 0);
        }
        if (c.hat && show) {
            double top = p.getY() + p.getHeight() + c.hatHeight;
            for (int level = 0; level < 3; level++) {
                double r = c.hatSize * (1.0 - level / 3.0);
                int pts = Math.max(4, (int) (c.hatPoints * (1.0 - level / 3.0)));
                for (int k = 0; k < pts; k++) {
                    double a = Math.PI * 2 * k / pts + spin;
                    mc.world.addParticleClient(dust(0.6f), p.getX() + Math.cos(a) * r, top - level * 0.12, p.getZ() + Math.sin(a) * r, 0, 0, 0);
                }
            }
        }
        if (c.halo && show) {
            double y = p.getY() + p.getHeight() + c.haloHeight;
            for (int k = 0; k < 18; k++) {
                double a = Math.PI * 2 * k / 18;
                mc.world.addParticleClient(dust(0.5f), p.getX() + Math.cos(a) * c.haloSize, y, p.getZ() + Math.sin(a) * c.haloSize, 0, 0, 0);
            }
        }
        if (c.aura && show) {
            for (int k = 0; k < c.auraCount; k++) {
                double a = spin + Math.PI * 2 * k / c.auraCount;
                double y = p.getY() + 0.9 + Math.sin(spin * 1.5 + k) * 0.8;
                mc.world.addParticleClient(dust(0.7f), p.getX() + Math.cos(a) * c.auraRadius, y, p.getZ() + Math.sin(a) * c.auraRadius, 0, 0, 0);
            }
        }
    }

    public static void onHit(Entity e) {
        MinecraftClient mc = MinecraftClient.getInstance();
        Config c = Config.I;
        combo++; lastHit = System.currentTimeMillis();
        combatUntil = lastHit + c.combatSecs * 1000L;
        maxCombo = Math.max(maxCombo, combo);
        if (mc.world == null) return;
        SoundEvent snd = switch (c.hitSound) {
            case 1 -> SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP;
            case 2 -> SoundEvents.ENTITY_ARROW_HIT_PLAYER;
            case 3 -> SoundEvents.ENTITY_PLAYER_ATTACK_CRIT;
            default -> null;
        };
        if (snd != null) mc.getSoundManager().play(PositionedSoundInstance.master(snd, 1.2f, 0.8f));
        if (!c.hitParticles) return;
        ParticleEffect type = switch (c.hitType) {
            case 0 -> ParticleTypes.CRIT;
            case 2 -> ParticleTypes.END_ROD;
            case 3 -> ParticleTypes.HEART;
            case 4 -> ParticleTypes.TOTEM_OF_UNDYING;
            case 5 -> ParticleTypes.NOTE;
            case 6 -> ParticleTypes.FLAME;
            case 7 -> ParticleTypes.ELECTRIC_SPARK;
            default -> dust(1.2f);
        };
        double y = e.getY() + e.getHeight() / 2;
        for (int i = 0; i < c.hitCount; i++)
            mc.world.addParticleClient(type, e.getX() + (R.nextDouble() - 0.5) * 0.6, y + (R.nextDouble() - 0.5) * 0.6,
                e.getZ() + (R.nextDouble() - 0.5) * 0.6, (R.nextDouble() - 0.5) * 0.2, R.nextDouble() * 0.15, (R.nextDouble() - 0.5) * 0.2);
    }

    public static void addInfoLines(List<String> lines, MinecraftClient mc) {
        Config c = Config.I;
        var p = mc.player;
        if (c.speed) lines.add(String.format("Speed: %.1f b/s", speed));
        if (c.direction) lines.add("Facing: " + p.getHorizontalFacing().name());
        if (c.state) lines.add(p.isSneaking() ? "Sneaking" : p.isSprinting() ? "Sprinting" : "Walking");
        if (c.clock) lines.add("Time: " + java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")));
        if (c.memory) { Runtime r = Runtime.getRuntime(); lines.add("RAM: " + (r.totalMemory() - r.freeMemory()) / 1048576 + " MB"); }
        if (c.server) lines.add(mc.getCurrentServerEntry() != null ? mc.getCurrentServerEntry().address : "Singleplayer");
        if (c.itemCount) {
            lines.add("Totems: " + p.getInventory().count(Items.TOTEM_OF_UNDYING));
            lines.add("Gapples: " + p.getInventory().count(Items.GOLDEN_APPLE));
            lines.add("Pearls: " + p.getInventory().count(Items.ENDER_PEARL));
        }
        if (c.pops) lines.add("Pops: " + popCount);
        long left = combatUntil - System.currentTimeMillis();
        if (c.combatTimer && left > 0) lines.add(String.format("Combat: %.1fs", left / 1000f));
        if (c.hpText) lines.add(String.format("HP: %.1f", p.getHealth()));
        if (c.hungerText) lines.add("Food: " + p.getHungerManager().getFoodLevel());
        if (c.xpLevel) lines.add("Level: " + p.experienceLevel);
        if (c.angles) { float yw = ((p.getYaw() % 360) + 540) % 360 - 180; lines.add(String.format("Yaw %.0f Pitch %.0f", yw, p.getPitch())); }
        if (c.day) lines.add("Day " + (mc.world.getTimeOfDay() / 24000L + 1));
        if (c.worldTime) {
            long t = mc.world.getTimeOfDay();
            lines.add(String.format("World: %02d:%02d", (t / 1000 + 6) % 24, (t % 1000) * 60 / 1000));
        }
        if (c.biome) lines.add("Biome: " + mc.world.getBiome(p.getBlockPos()).getKey().map(k -> k.getValue().getPath()).orElse("?"));
        if (c.light) lines.add("Light: " + mc.world.getLightLevel(p.getBlockPos()));
        if (c.heldItem) {
            ItemStack h = p.getMainHandStack();
            if (!h.isEmpty()) lines.add(h.getName().getString() + (h.isDamageable() ? " " + (h.getMaxDamage() - h.getDamage()) : " x" + h.getCount()));
        }
        if (c.netherCoords) {
            boolean nether = mc.world.getRegistryKey().getValue().getPath().equals("the_nether");
            double f = nether ? 8 : 0.125;
            lines.add(String.format("%s: %.0f %.0f", nether ? "Overworld" : "Nether", p.getX() * f, p.getZ() * f));
        }
        if (c.session) { long sec = (System.currentTimeMillis() - START) / 1000; lines.add(String.format("Session: %d:%02d:%02d", sec / 3600, (sec / 60) % 60, sec % 60)); }
        if (c.kills) lines.add("Kills: " + kills);
        if (c.bestCombo) lines.add("Best combo: " + maxCombo);
        if (c.combo) lines.add("Combo: " + combo);
    }

    public static void renderSide(DrawContext ctx, MinecraftClient mc) {
        Config c = Config.I;
        var p = mc.player;
        int sw = ctx.getScaledWindowWidth(), sh = ctx.getScaledWindowHeight();
        int acc = c.accent();
        float sc = c.hudScale;
        if (c.armorHud) {
            AuroraVisuals.begin(ctx, c.pArmor, sw, sh);
            EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
            for (int i = 0; i < 4; i++) {
                ItemStack st = p.getEquippedStack(slots[i]);
                if (st.isEmpty()) continue;
                int y = i * 18;
                AuroraVisuals.panel(ctx, 0, y, 52, 17, acc);
                ctx.drawItem(st, 34, y);
                if (st.isDamageable())
                    {
                    int left = st.getMaxDamage() - st.getDamage();
                    boolean low = left < st.getMaxDamage() * 0.15;
                    ctx.drawText(mc.textRenderer, String.valueOf(left), 5, y + 4, low ? 0xFFFF5555 : c.textCol(), c.textShadow);
                }
            }
            AuroraVisuals.end(ctx);
            c.pArmor.w = 52 * sc; c.pArmor.h = 72 * sc;
        }
        if (c.potionHud) {
            java.util.List<String> rows = new java.util.ArrayList<>();
            for (StatusEffectInstance e : p.getStatusEffects())
                rows.add(e.getEffectType().value().getName().getString() + " " + (e.getAmplifier() + 1) + " "
                    + (e.getDuration() / 20 / 60) + ":" + String.format("%02d", (e.getDuration() / 20) % 60));
            if (!rows.isEmpty()) {
                int w = 0;
                for (String r : rows) w = Math.max(w, mc.textRenderer.getWidth(r));
                AuroraVisuals.begin(ctx, c.pPot, sw, sh);
                for (int i = 0; i < rows.size(); i++) {
                    AuroraVisuals.panel(ctx, 0, i * 13, w + 8, 12, acc);
                    ctx.drawText(mc.textRenderer, rows.get(i), 5, i * 13 + 2, c.textCol(), c.textShadow);
                }
                AuroraVisuals.end(ctx);
                c.pPot.w = (w + 8) * sc; c.pPot.h = rows.size() * 13 * sc;
            }
        }
        if (c.targetHud && mc.targetedEntity instanceof LivingEntity le) {
            int bw = 120;
            boolean arm = c.targetArmor;
            int bh = 36 + (arm ? 18 : 0);
            AuroraVisuals.begin(ctx, c.pTarget, sw, sh);
            AuroraVisuals.panel(ctx, 0, 0, bw, bh, acc);
            ctx.drawText(mc.textRenderer, le.getName().getString(), 6, 3, c.textCol(), c.textShadow);
            if (System.currentTimeMillis() - lastDmgTime < 2000 && le == lastTarget)
                ctx.drawText(mc.textRenderer, String.format("-%.1f", lastDmg), bw - 32, 3, 0xFFFF5555, c.textShadow);
            float pct = Math.max(0f, Math.min(1f, le.getHealth() / le.getMaxHealth()));
            ctx.fill(6, 16, bw - 6, 22, 0x80000000);
            ctx.fill(6, 16, 6 + (int) ((bw - 12) * pct), 22, acc);
            if (c.showReach)
                ctx.drawText(mc.textRenderer, String.format("%.1f m  %.0f HP", p.distanceTo(le), le.getHealth()), 6, 25, c.textCol(), c.textShadow);
            if (arm) {
                EquipmentSlot[] sl = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.MAINHAND};
                for (int k = 0; k < sl.length; k++) {
                    ItemStack it = le.getEquippedStack(sl[k]);
                    if (!it.isEmpty()) ctx.drawItem(it, 6 + k * 20, 37);
                }
            }
            AuroraVisuals.end(ctx);
            c.pTarget.w = bw * sc; c.pTarget.h = bh * sc;
        }
        if (c.cdHud) {
            int rows = 0;
            AuroraVisuals.begin(ctx, c.pCd, sw, sh);
            for (int k = 0; k < CD_ITEMS.length; k++) {
                if (cdProg[k] <= 0) continue;
                int y = rows * 20;
                AuroraVisuals.panel(ctx, 0, y, 72, 19, acc);
                ctx.drawItem(new ItemStack(CD_ITEMS[k]), 4, y + 1);
                ctx.fill(24, y + 7, 68, y + 12, 0x80000000);
                ctx.fill(24, y + 7, 24 + (int) (44 * cdProg[k]), y + 12, acc);
                rows++;
            }
            AuroraVisuals.end(ctx);
            c.pCd.w = 72 * sc; c.pCd.h = Math.max(1, rows) * 20 * sc;
        }
        if (c.lowHp && p.getHealth() <= c.lowHpThreshold && p.isAlive()) {
            int a = (int) (70 + 60 * Math.sin(System.currentTimeMillis() / 150.0));
            int col = (a << 24) | 0xFF2020;
            int t = 8;
            ctx.fill(0, 0, sw, t, col); ctx.fill(0, sh - t, sw, sh, col);
            ctx.fill(0, t, t, sh - t, col); ctx.fill(sw - t, t, sw, sh - t, col);
        }
        if (c.hurtFlash && p.hurtTime > 0) ctx.fill(0, 0, sw, sh, (Math.min(120, 30 + p.hurtTime * 9) << 24) | 0xFF2020);
        if (c.frame) {
            int a = 0xA0000000 | (acc & 0xFFFFFF), ft = c.frameThick;
            ctx.fill(0, 0, sw, ft, a); ctx.fill(0, sh - ft, sw, sh, a);
            ctx.fill(0, ft, ft, sh - ft, a); ctx.fill(sw - ft, ft, sw, sh - ft, a);
        }
        if (c.atkBar) {
            float cd = p.getAttackCooldownProgress(0f);
            if (cd < 1f) {
                int bx = sw / 2 - 15, by = sh / 2 + 14;
                ctx.fill(bx, by, bx + 30, by + 3, 0x80000000);
                ctx.fill(bx, by, bx + (int) (30 * cd), by + 3, acc);
            }
        }
        if (c.lowFood && p.getHungerManager().getFoodLevel() <= 6) {
            String lf = "LOW FOOD";
            ctx.drawText(mc.textRenderer, lf, (sw - mc.textRenderer.getWidth(lf)) / 2, sh / 2 + 24, 0xFFFFAA00, true);
        }
        if (c.compass) {
            float hd = ((((p.getYaw() % 360) + 360) % 360) + 180) % 360;
            String[] nm = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
            int cx = sw / 2;
            ctx.fill(cx - 140, 2, cx + 140, 14, c.bg());
            for (int i = 0; i < 8; i++) {
                float d = ((i * 45f - hd + 540) % 360) - 180;
                if (Math.abs(d) < 90) {
                    int x = cx + (int) (d * 1.5f), tw = mc.textRenderer.getWidth(nm[i]);
                    ctx.drawText(mc.textRenderer, nm[i], x - tw / 2, 4, (i % 2 == 0) ? acc : c.textCol(), c.textShadow);
                }
            }
            ctx.fill(cx, 14, cx + 1, 17, acc);
        }
    }
}

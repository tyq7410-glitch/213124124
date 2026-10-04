package com.deadvisuals;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

public final class Fx {
    private Fx() {
    }

    private static final class Dmg {
        Vec3d pos;
        float amount;
        int age;
        int life;
        boolean crit;
        boolean heal;
    }

    private static final String[] HIT_MSG = {"nice hit~", "good one!", "keep going!"};
    private static final String[] CRIT_MSG = {"crit!!", "ouch for them~", "so strong!"};
    private static final String[] KILL_MSG = {"nice kill~!", "gg!", "you did it!"};

    private static final Random RND = new Random();
    private static final List<Dmg> NUMBERS = new ArrayList<>();
    private static final Map<UUID, Float> LAST_HP = new HashMap<>();

    public static int ticks;
    private static long lastTickNanos = System.nanoTime();
    private static long lastHudNs;
    private static LivingEntity target;
    private static LivingEntity hudTarget;
    private static LivingEntity lastHit;
    private static long targetUntil;
    private static long hitMarkerUntil;
    private static long attackedAt;
    private static int attackedId = -1;
    private static boolean wasCrit;
    private static boolean killDone;
    private static boolean inWorld;
    private static boolean deadHandled;
    private static float shownRatio = 1f;
    private static float hudA;

    public static float lastReach;
    public static long reachAt;

    public static boolean hitRecent() {
        return System.currentTimeMillis() < hitMarkerUntil;
    }

    public static float partial() {
        float f = (System.nanoTime() - lastTickNanos) / 50_000_000f;
        return f < 0 ? 0 : Math.min(f, 1f);
    }

    private static SoundEvent hitSound(int mode) {
        switch (mode) {
            case 1:
                return SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME;
            case 2:
                return SoundEvents.ENTITY_ARROW_HIT_PLAYER;
            case 3:
                return SoundEvents.ENTITY_PLAYER_ATTACK_CRIT;
            case 4:
                return SoundEvents.ENTITY_ALLAY_ITEM_GIVEN;
            default:
                return SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP;
        }
    }

    public static void onAttack(PlayerEntity p, LivingEntity le) {
        long now = System.currentTimeMillis();
        wasCrit = p.fallDistance > 0.0 && !p.isOnGround() && !p.isClimbing() && !p.isTouchingWater()
                && !p.hasStatusEffect(StatusEffects.BLINDNESS) && !p.hasVehicle()
                && p.getAttackCooldownProgress(0.5f) > 0.9f;
        if (target != le) shownRatio = le.getHealth() / Math.max(1f, le.getMaxHealth());
        if (lastHit != le) killDone = false;
        target = le;
        hudTarget = le;
        lastHit = le;
        targetUntil = now + (long) (Mods.THUD.v("Duration") * 1000);
        attackedId = le.getId();
        attackedAt = now;
        lastReach = (float) Math.max(0.0, p.getEyePos().distanceTo(le.getEyePos()) - le.getWidth() / 2.0);
        reachAt = now;
        hitMarkerUntil = now + (long) (Mods.MARK.v("Duration") * 50);

        if (Mods.SOUND.enabled) {
            MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(
                    hitSound(Mods.SOUND.mode("Sound")),
                    (float) Mods.SOUND.v("Pitch") + (wasCrit ? 0.2f : 0f), (float) Mods.SOUND.v("Volume")));
        }
        if (Mods.HELPER.enabled && Mods.HELPER.on("Hit messages")) {
            if (wasCrit) Helper.sayLimited(CRIT_MSG[RND.nextInt(CRIT_MSG.length)], 2500);
            else if (RND.nextInt(6) == 0) Helper.sayLimited(HIT_MSG[RND.nextInt(HIT_MSG.length)], 4000);
        }
    }

    public static void tick(MinecraftClient mc) {
        lastTickNanos = System.nanoTime();
        ClientWorld w = mc.world;
        ClientPlayerEntity p = mc.player;
        if (w == null || p == null) {
            LAST_HP.clear();
            NUMBERS.clear();
            target = null;
            hudTarget = null;
            lastHit = null;
            inWorld = false;
            Emotes.clear();
            Marks.clear();
            Pets.reset();
            Hud2.reset();
            return;
        }
        if (!inWorld) {
            inWorld = true;
            Helper.say("hi~ lets have fun!");
        }
        ticks++;
        long now = System.currentTimeMillis();

        if (p.getHealth() <= 0f) {
            if (!deadHandled) {
                deadHandled = true;
                if (Mods.MARKS.enabled && Mods.MARKS.on("Mark on death")) Marks.addAuto(p.getPos());
            }
        } else {
            deadHandled = false;
        }
        Hud2.tick(p);
        Visuals.tick(mc, p);

        for (Entity e : w.getEntities()) {
            if (!(e instanceof LivingEntity le) || e == p) continue;
            float hp = le.getHealth() + le.getAbsorptionAmount();
            Float prev = LAST_HP.put(le.getUuid(), hp);
            if (prev != null && hp < prev - 0.01f && le.getId() == attackedId && now - attackedAt < 1500) {
                if (Mods.NUM.enabled) {
                    Dmg d = new Dmg();
                    d.pos = le.getPos().add((RND.nextDouble() - 0.5) * 0.6, le.getHeight() + 0.2, (RND.nextDouble() - 0.5) * 0.6);
                    d.amount = prev - hp;
                    d.crit = wasCrit;
                    d.life = (int) Mods.NUM.v("Lifetime");
                    NUMBERS.add(d);
                }
                burst(w, le, wasCrit);
                Hud2.onHit();
            } else if (prev != null && hp > prev + 0.49f && le.getId() == attackedId && now - attackedAt < 10000
                    && Mods.NUM.enabled && Mods.NUM.on("Show heal")) {
                Dmg d = new Dmg();
                d.pos = le.getPos().add((RND.nextDouble() - 0.5) * 0.6, le.getHeight() + 0.2, (RND.nextDouble() - 0.5) * 0.6);
                d.amount = hp - prev;
                d.heal = true;
                d.life = (int) Mods.NUM.v("Lifetime");
                NUMBERS.add(d);
            }
        }
        if (LAST_HP.size() > 512) LAST_HP.clear();

        NUMBERS.removeIf(d -> ++d.age > d.life);
        if (target != null && (!target.isAlive() || now > targetUntil + 500)) target = null;

        if (lastHit != null && !killDone && lastHit.isDead() && now - attackedAt < 6000) {
            killDone = true;
            if (Mods.KILL.enabled) killFx(w, lastHit);
            Hud2.onKill();
            if (Mods.HELPER.enabled && Mods.HELPER.on("Hit messages")) Helper.say(KILL_MSG[RND.nextInt(KILL_MSG.length)]);
        }

        Emotes.tick();
        Helper.tick(p);
        Cosmetics.tick(w, p);
        Extra.tick(w, p);
    }

    private static void burst(ClientWorld w, LivingEntity le, boolean crit) {
        Vec3d b = le.getPos().add(0, le.getHeight() * 0.6, 0);
        float ps = Perf.scale();
        Module hit = Mods.HIT;
        if (hit.enabled) {
            int rgb = Colors.pick(crit ? hit.mode("Crit color") : hit.mode("Color"), hit.v("Hue"), ticks) & 0xFFFFFF;
            DustParticleEffect dust = new DustParticleEffect(rgb, (float) hit.v("Size"));
            int n = Math.max(2, (int) (hit.v("Amount") * (crit ? 1.5 : 1.0) * ps));
            double sp = hit.v("Spread");
            for (int i = 0; i < n; i++) {
                w.addParticle(dust,
                        b.x + (RND.nextDouble() - 0.5) * sp,
                        b.y + (RND.nextDouble() - 0.5) * sp,
                        b.z + (RND.nextDouble() - 0.5) * sp,
                        0, 0, 0);
            }
        }
        Module cm = Mods.CRIT;
        if (crit && cm.enabled) {
            int n = Math.max(2, (int) (cm.v("Amount") * ps));
            double sp = cm.v("Speed");
            int type = cm.mode("Type");
            for (int i = 0; i < n; i++) {
                double vx = (RND.nextDouble() - 0.5) * sp;
                double vy = RND.nextDouble() * sp * 0.75;
                double vz = (RND.nextDouble() - 0.5) * sp;
                if (type == 0 || type == 2) w.addParticle(ParticleTypes.CRIT, b.x, b.y, b.z, vx, vy, vz);
                if (type == 1 || type == 2) w.addParticle(ParticleTypes.ENCHANTED_HIT, b.x, b.y, b.z, vx, vy, vz);
                if (type == 3) w.addParticle(ParticleTypes.TOTEM_OF_UNDYING, b.x, b.y, b.z, vx, vy, vz);
                if (type == 4) w.addParticle(ParticleTypes.ELECTRIC_SPARK, b.x, b.y, b.z, vx, vy, vz);
            }
        }
    }

    private static void killFx(ClientWorld w, LivingEntity le) {
        Vec3d b = le.getPos().add(0, le.getHeight() / 2, 0);
        int n = Math.max(4, (int) (Mods.KILL.v("Amount") * Perf.scale()));
        int type = Mods.KILL.mode("Type");
        ParticleEffect eff;
        switch (type) {
            case 1:
                eff = ParticleTypes.LARGE_SMOKE;
                break;
            case 2:
                eff = ParticleTypes.TOTEM_OF_UNDYING;
                break;
            case 3:
                eff = ParticleTypes.ELECTRIC_SPARK;
                break;
            case 4:
                eff = ParticleTypes.HEART;
                break;
            case 5:
                eff = ParticleTypes.CHERRY_LEAVES;
                break;
            default:
                eff = ParticleTypes.SOUL;
        }
        for (int i = 0; i < n; i++) {
            double vx = (RND.nextDouble() - 0.5) * 0.3;
            double vy = RND.nextDouble() * 0.4;
            double vz = (RND.nextDouble() - 0.5) * 0.3;
            w.addParticle(eff, b.x + (RND.nextDouble() - 0.5) * 0.5, b.y + (RND.nextDouble() - 0.5) * 0.8,
                    b.z + (RND.nextDouble() - 0.5) * 0.5, vx, vy, vz);
            if (type == 0 && i % 3 == 0) w.addParticle(ParticleTypes.SOUL_FIRE_FLAME, b.x, b.y, b.z, vx, vy, vz);
        }
        int ks = Mods.KILL.mode("Sound");
        if (ks > 0) {
            SoundEvent se = ks == 1 ? SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME
                    : ks == 2 ? SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP : SoundEvents.ITEM_TOTEM_USE;
            MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(se, 1.0f, 0.6f));
        }
    }

    public static void renderHud(DrawContext g, RenderTickCounter tc) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.options.hudHidden) return;
        long nowNs = System.nanoTime();
        float dt = lastHudNs == 0 ? 0.016f : Math.min(0.1f, (nowNs - lastHudNs) / 1e9f);
        lastHudNs = nowNs;
        long now = System.currentTimeMillis();
        int sw = g.getScaledWindowWidth();
        int sh = g.getScaledWindowHeight();

        Module hm = Mods.MARK;
        if (hm.enabled && now < hitMarkerUntil) {
            int cx = sw / 2;
            int cy = sh / 2;
            int gap = (int) hm.v("Gap");
            int size = (int) hm.v("Size");
            int mode = wasCrit ? hm.mode("Crit color") : hm.mode("Color");
            int col = 0xFF000000 | Colors.pick(mode, hm.v("Hue"), ticks);
            for (int i = gap; i < gap + size; i++) {
                g.fill(cx + i, cy + i, cx + i + 1, cy + i + 1, col);
                g.fill(cx - i - 1, cy + i, cx - i, cy + i + 1, col);
                g.fill(cx + i, cy - i - 1, cx + i + 1, cy - i, col);
                g.fill(cx - i - 1, cy - i - 1, cx - i, cy - i, col);
            }
        }

        Module th = Mods.THUD;
        boolean wanted = th.enabled && target != null && now < targetUntil;
        hudA += ((wanted ? 1f : 0f) - hudA) * (1f - (float) Math.exp(-12 * dt));
        if (th.enabled && hudTarget != null && hudA > 0.02f) drawTarget(g, mc, th, sw, sh, dt);

        Visuals.render(g, mc, sw, sh, now, dt);
        Hud3.render(g, mc, sw, sh, now);
        Hud2.render(g, mc, sw, sh, now);
        Helper.render(g, mc, sw, sh, now);
    }

    private static void drawTarget(DrawContext g, MinecraftClient mc, Module th, int sw, int sh, float dt) {
        LivingEntity t = hudTarget;
        MatrixStack ms = g.getMatrices();
        float sc = (float) th.v("Scale") * (0.92f + 0.08f * hudA);
        int x = sw / 2 + (int) th.v("X offset");
        int y = sh / 2 + (int) th.v("Y offset");
        int style = th.mode("Style");
        int accent = 0xFF000000 | MathHelper.hsvToRgb((float) (th.v("Accent hue") / 360.0), 0.9f, 0.75f);
        float ratio = MathHelper.clamp(t.getHealth() / Math.max(1f, t.getMaxHealth()), 0f, 1f);
        shownRatio += (ratio - shownRatio) * (1f - (float) Math.exp(-10 * dt));
        int barColor = 0xFF000000 | MathHelper.hsvToRgb(shownRatio * 0.33f, 0.8f, 0.95f);

        boolean showName = th.on("Name") && style != 2;
        boolean showBar = th.on("Health bar") || style == 2;
        StringBuilder info = new StringBuilder();
        if (th.on("Health text")) info.append(String.format("%.1f/%.0f", t.getHealth(), t.getMaxHealth()));
        if (th.on("Absorption") && t.getAbsorptionAmount() > 0) info.append(String.format(" +%.0f", t.getAbsorptionAmount()));
        if (th.on("Armor")) info.append("  AR ").append(t.getArmor());
        if (th.on("Distance")) info.append(String.format("  %.1fm", mc.player.distanceTo(t)));
        String line = info.toString().trim();
        String held = th.on("Held item") && !t.getMainHandStack().isEmpty() ? t.getMainHandStack().getName().getString() : "";

        int w = 130;
        boolean panel = style != 1;
        int pad = panel ? 6 : 0;
        int h = (panel ? 8 : 0) + (showName ? 13 : 0) + (showBar ? 9 : 0) + (line.isEmpty() ? 0 : 11) + (held.isEmpty() ? 0 : 11);
        Hud2.SIZES.put("target hud", new int[]{w, h});

        ms.push();
        ms.translate(x + (1f - hudA) * 30f, y, 0f);
        ms.scale(sc, sc, 1f);
        if (panel) {
            Ui.rrect(g, 0, 0, w, h, 5, 0xC00B0B0E);
            Ui.hgrad(g, 6, 0, w - 12, 1, accent, Ui.ACCENT2);
        }
        int yy = panel ? 5 : 0;
        if (showName) {
            g.drawTextWithShadow(mc.textRenderer, t.getName(), pad, yy, 0xFFFFFFFF);
            yy += 13;
        }
        if (showBar) {
            g.fill(pad, yy, w - pad, yy + 5, 0xFF2A2A2A);
            g.fill(pad, yy, pad + (int) ((w - 2 * pad) * shownRatio), yy + 5, barColor);
            yy += 9;
        }
        if (!line.isEmpty()) {
            g.drawTextWithShadow(mc.textRenderer, line, pad, yy, 0xFFAAAAAA);
            yy += 11;
        }
        if (!held.isEmpty()) g.drawTextWithShadow(mc.textRenderer, held, pad, yy, 0xFF888888);
        ms.pop();
    }

    public static void renderWorld(WorldRenderContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;
        MatrixStack ms = ctx.matrixStack();
        VertexConsumerProvider vp = ctx.consumers();
        if (ms == null || vp == null) return;
        Vec3d cam = ctx.camera().getPos();
        float pt = partial();

        if (Mods.NUM.enabled) drawNumbers(ctx, mc, ms, vp, cam);
        if (Mods.TRAJ.enabled) drawTrajectory(mc, ms, vp, cam);
        World2.render(ctx, mc, ms, vp, cam, pt);
        Marks.render(ctx, mc, ms, vp, cam);
        Wings.render(mc, ms, vp, cam, pt);
        Cape.render(mc, ms, vp, cam, pt);
        Pets.render(mc, ms, vp, cam, pt);
        Emotes.render(ctx, mc, ms, vp, cam, pt);

        if (vp instanceof VertexConsumerProvider.Immediate imm) imm.draw();
    }

    private static void drawNumbers(WorldRenderContext ctx, MinecraftClient mc, MatrixStack ms, VertexConsumerProvider vp, Vec3d cam) {
        Module m = Mods.NUM;
        TextRenderer tr = mc.textRenderer;
        String fmt = "%." + m.mode("Decimals") + "f";
        double rise = m.v("Rise");
        TextRenderer.TextLayerType layer = m.on("Through walls")
                ? TextRenderer.TextLayerType.SEE_THROUGH : TextRenderer.TextLayerType.NORMAL;
        for (Dmg d : NUMBERS) {
            float prog = d.age / (float) Math.max(1, d.life);
            double y = d.pos.y + prog * rise;
            double dist = Math.sqrt(d.pos.squaredDistanceTo(cam));
            float pop = Math.min(1f, 0.6f + d.age / 4f);
            float sc = 0.035f * (float) m.v("Scale") * pop * (d.crit ? 1.3f : 1f) * (float) Math.max(1.0, dist / 10.0);
            int alpha = m.on("Fade") ? Math.max(8, (int) (255 * (1f - prog * prog))) : 255;
            int rgb = d.heal ? 0x39FF88 : Colors.pick(d.crit ? m.mode("Crit color") : m.mode("Color"), m.v("Hue"), ticks) & 0xFFFFFF;
            int color = (alpha << 24) | rgb;
            String s = (d.heal ? "+" : d.crit ? "* " : "-") + String.format(fmt, d.amount);

            ms.push();
            ms.translate(d.pos.x - cam.x, y - cam.y, d.pos.z - cam.z);
            ms.multiply(ctx.camera().getRotation());
            ms.scale(-sc, -sc, sc);
            float wd = tr.getWidth(s);
            tr.draw(s, -wd / 2f, 0, color, false, ms.peek().getPositionMatrix(), vp, layer, 0, 0xF000F0);
            ms.pop();
        }
    }

    private static void drawTrajectory(MinecraftClient mc, MatrixStack ms, VertexConsumerProvider vp, Vec3d cam) {
        Module tj = Mods.TRAJ;
        ClientPlayerEntity p = mc.player;
        ItemStack held = p.getMainHandStack().isEmpty() ? p.getOffHandStack() : p.getMainHandStack();
        if (p.isUsingItem()) held = p.getActiveItem();
        Item it = held.getItem();

        double speed;
        double grav;
        if (it == Items.BOW && tj.on("Bow")) {
            if (!p.isUsingItem()) return;
            float f = BowItem.getPullProgress(p.getItemUseTime());
            if (f < 0.1f) return;
            speed = f * 3.0;
            grav = 0.05;
        } else if (it == Items.CROSSBOW && tj.on("Crossbow")) {
            if (!CrossbowItem.isCharged(held)) return;
            speed = 3.15;
            grav = 0.05;
        } else if (it == Items.TRIDENT && tj.on("Trident")) {
            if (!p.isUsingItem()) return;
            speed = 2.5;
            grav = 0.05;
        } else if ((it == Items.ENDER_PEARL || it == Items.SNOWBALL || it == Items.EGG) && tj.on("Throwables")) {
            speed = 1.5;
            grav = 0.03;
        } else if ((it == Items.SPLASH_POTION || it == Items.LINGERING_POTION) && tj.on("Throwables")) {
            speed = 0.5;
            grav = 0.05;
        } else {
            return;
        }

        Vec3d pos = p.getEyePos().add(0, -0.1, 0);
        Vec3d vel = p.getRotationVec(1f).multiply(speed);
        List<Vec3d> pts = new ArrayList<>();
        pts.add(pos);
        boolean hitEntity = false;
        Vec3d end = null;
        int steps = (int) tj.v("Max steps");

        for (int i = 0; i < steps; i++) {
            Vec3d next = pos.add(vel);
            HitResult hr = mc.world.raycast(new RaycastContext(pos, next,
                    RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, p));
            boolean blockHit = hr.getType() != HitResult.Type.MISS;
            if (blockHit) next = hr.getPos();

            Vec3d entHit = null;
            double best = Double.MAX_VALUE;
            for (Entity e : mc.world.getOtherEntities(p, new Box(pos, next).expand(1.0), en -> en instanceof LivingEntity)) {
                Optional<Vec3d> o = e.getBoundingBox().expand(0.3).raycast(pos, next);
                if (o.isPresent()) {
                    double dd = pos.squaredDistanceTo(o.get());
                    if (dd < best) {
                        best = dd;
                        entHit = o.get();
                    }
                }
            }
            if (entHit != null) {
                pts.add(entHit);
                end = entHit;
                hitEntity = true;
                break;
            }
            pts.add(next);
            if (blockHit) {
                end = next;
                break;
            }
            pos = next;
            vel = vel.multiply(0.99).add(0, -grav, 0);
            if (pos.y < mc.world.getBottomY() - 10) break;
        }

        VertexConsumer vc = vp.getBuffer(RenderLayer.getLines());
        MatrixStack.Entry en = ms.peek();
        int col = 0xFF000000 | Colors.pick(hitEntity ? tj.mode("Hit color") : tj.mode("Color"), tj.v("Hue"), ticks);
        for (int i = 0; i < pts.size() - 1; i++) {
            line(vc, en, pts.get(i).subtract(cam), pts.get(i + 1).subtract(cam), col);
        }
        if (end != null && tj.on("Landing marker")) {
            Vec3d e = end.subtract(cam);
            double s = tj.v("Marker size");
            line(vc, en, e.add(-s, 0, 0), e.add(s, 0, 0), col);
            line(vc, en, e.add(0, -s, 0), e.add(0, s, 0), col);
            line(vc, en, e.add(0, 0, -s), e.add(0, 0, s), col);
        }
    }

    static void line(VertexConsumer vc, MatrixStack.Entry en, Vec3d a, Vec3d b, int col) {
        Vec3d n = b.subtract(a).normalize();
        vc.vertex(en, (float) a.x, (float) a.y, (float) a.z).color(col).normal(en, (float) n.x, (float) n.y, (float) n.z);
        vc.vertex(en, (float) b.x, (float) b.y, (float) b.z).color(col).normal(en, (float) n.x, (float) n.y, (float) n.z);
    }
}

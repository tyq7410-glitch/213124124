package com.deadvisuals;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.block.BlockState;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import org.joml.Matrix4f;

public final class World2 {
    private World2() {
    }

    public static void render(WorldRenderContext ctx, MinecraftClient mc, MatrixStack ms,
                              VertexConsumerProvider vp, Vec3d cam, float pt) {
        ClientPlayerEntity p = mc.player;
        ClientWorld w = mc.world;
        if (p == null || w == null) return;

        if (Mods.HITBOX.enabled || Mods.BLOCKO.enabled || Mods.RING.enabled) {
            VertexConsumer lc = vp.getBuffer(RenderLayer.getLines());
            MatrixStack.Entry en = ms.peek();
            if (Mods.HITBOX.enabled) hitboxes(lc, en, p, w, cam, pt);
            if (Mods.BLOCKO.enabled) blockOverlay(lc, en, mc, w, cam);
            if (Mods.RING.enabled) ring(lc, en, p, w, cam, pt);
        }
        if (Mods.MOBHP.enabled) mobHealth(ctx, mc, ms, vp, cam, pt, p, w);
    }

    private static void hitboxes(VertexConsumer lc, MatrixStack.Entry en, ClientPlayerEntity p, ClientWorld w, Vec3d cam, float pt) {
        Module m = Mods.HITBOX;
        double r = m.v("Range");
        int col = 0xFF000000 | Colors.pick(m.mode("Color"), m.v("Hue"), Fx.ticks);
        for (Entity e : w.getEntities()) {
            if (e == p || !(e instanceof LivingEntity)) continue;
            boolean pl = e instanceof PlayerEntity;
            if (pl ? !m.on("Players") : !m.on("Mobs")) continue;
            if (e.squaredDistanceTo(p) > r * r) continue;
            Vec3d lp = e.getLerpedPos(pt);
            Box b = e.getBoundingBox().offset(lp.x - e.getX(), lp.y - e.getY(), lp.z - e.getZ());
            box(lc, en, b, cam, col);
        }
    }

    private static void blockOverlay(VertexConsumer lc, MatrixStack.Entry en, MinecraftClient mc, ClientWorld w, Vec3d cam) {
        HitResult ch = mc.crosshairTarget;
        if (!(ch instanceof BlockHitResult bhr) || ch.getType() != HitResult.Type.BLOCK) return;
        BlockPos pos = bhr.getBlockPos();
        BlockState st = w.getBlockState(pos);
        VoxelShape sh = st.getOutlineShape(w, pos);
        if (sh.isEmpty()) return;
        Module m = Mods.BLOCKO;
        int col = 0xFF000000 | Colors.pick(m.mode("Color"), m.v("Hue"), Fx.ticks);
        box(lc, en, sh.getBoundingBox().offset(pos).expand(0.003), cam, col);
    }

    private static void ring(VertexConsumer lc, MatrixStack.Entry en, ClientPlayerEntity p, ClientWorld w, Vec3d cam, float pt) {
        Module m = Mods.RING;
        ItemStack held = p.getMainHandStack().isEmpty() ? p.getOffHandStack() : p.getMainHandStack();
        if (held.isEmpty()) return;
        if (m.on("Only renamed items") && !held.contains(DataComponentTypes.CUSTOM_NAME)) return;
        int col = 0xFF000000 | Colors.pick(m.mode("Color"), m.v("Hue"), Fx.ticks);
        for (PlayerEntity o : w.getPlayers()) {
            if (o != p && o.distanceTo(p) <= m.v("Radius")) {
                col = 0xFF39FF88;
                break;
            }
        }
        Vec3d c = p.getLerpedPos(pt).add(0, m.v("Height"), 0).subtract(cam);
        double k = m.on("Pulse") ? 1.0 + 0.02 * Math.sin((Fx.ticks + pt) * 0.2) : 1.0;
        circle(lc, en, c, m.v("Radius") * k, col);
        if (m.v("Inner radius") > 0) circle(lc, en, c, m.v("Inner radius") * k, col);
    }

    private static void circle(VertexConsumer lc, MatrixStack.Entry en, Vec3d c, double r, int col) {
        int n = 64;
        for (int i = 0; i < n; i++) {
            double a0 = Math.PI * 2 * i / n;
            double a1 = Math.PI * 2 * (i + 1) / n;
            Fx.line(lc, en,
                    new Vec3d(c.x + Math.cos(a0) * r, c.y, c.z + Math.sin(a0) * r),
                    new Vec3d(c.x + Math.cos(a1) * r, c.y, c.z + Math.sin(a1) * r), col);
        }
    }

    private static void box(VertexConsumer lc, MatrixStack.Entry en, Box b, Vec3d cam, int col) {
        double x0 = b.minX - cam.x, y0 = b.minY - cam.y, z0 = b.minZ - cam.z;
        double x1 = b.maxX - cam.x, y1 = b.maxY - cam.y, z1 = b.maxZ - cam.z;
        e(lc, en, x0, y0, z0, x1, y0, z0, col);
        e(lc, en, x1, y0, z0, x1, y0, z1, col);
        e(lc, en, x1, y0, z1, x0, y0, z1, col);
        e(lc, en, x0, y0, z1, x0, y0, z0, col);
        e(lc, en, x0, y1, z0, x1, y1, z0, col);
        e(lc, en, x1, y1, z0, x1, y1, z1, col);
        e(lc, en, x1, y1, z1, x0, y1, z1, col);
        e(lc, en, x0, y1, z1, x0, y1, z0, col);
        e(lc, en, x0, y0, z0, x0, y1, z0, col);
        e(lc, en, x1, y0, z0, x1, y1, z0, col);
        e(lc, en, x1, y0, z1, x1, y1, z1, col);
        e(lc, en, x0, y0, z1, x0, y1, z1, col);
    }

    private static void e(VertexConsumer lc, MatrixStack.Entry en, double ax, double ay, double az,
                          double bx, double by, double bz, int col) {
        Fx.line(lc, en, new Vec3d(ax, ay, az), new Vec3d(bx, by, bz), col);
    }

    private static void mobHealth(WorldRenderContext ctx, MinecraftClient mc, MatrixStack ms, VertexConsumerProvider vp,
                                  Vec3d cam, float pt, ClientPlayerEntity p, ClientWorld w) {
        Module m = Mods.MOBHP;
        double r = m.v("Range");
        TextRenderer tr = mc.textRenderer;
        String full = "||||||||||";
        for (Entity e : w.getEntities()) {
            if (!(e instanceof LivingEntity le) || e == p || e instanceof PlayerEntity) continue;
            if (!(le instanceof Monster) && !m.on("Passive too")) continue;
            if (le.isDead() || le.squaredDistanceTo(p) > r * r || !p.canSee(le)) continue;
            float ratio = MathHelper.clamp(le.getHealth() / Math.max(1f, le.getMaxHealth()), 0f, 1f);
            Vec3d lp = le.getLerpedPos(pt).add(0, le.getHeight() + 0.45, 0);
            double dist = Math.sqrt(lp.squaredDistanceTo(cam));
            float sc = 0.02f * (float) m.v("Scale") * (float) Math.max(1.0, dist / 8.0);
            int filled = Math.round(ratio * 10);
            String on = full.substring(0, filled);
            String off = full.substring(filled);
            int col = 0xFF000000 | MathHelper.hsvToRgb(ratio * 0.33f, 0.8f, 0.95f);

            ms.push();
            ms.translate(lp.x - cam.x, lp.y - cam.y, lp.z - cam.z);
            ms.multiply(ctx.camera().getRotation());
            ms.scale(-sc, -sc, sc);
            Matrix4f mat = ms.peek().getPositionMatrix();
            float wd = tr.getWidth(full);
            tr.draw(on, -wd / 2f, 0, col, false, mat, vp, TextRenderer.TextLayerType.NORMAL, 0x60000000, 0xF000F0);
            tr.draw(off, -wd / 2f + tr.getWidth(on), 0, 0xFF444444, false, mat, vp, TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0);
            String num = String.format("%.0f/%.0f", le.getHealth(), le.getMaxHealth());
            tr.draw(num, -tr.getWidth(num) / 2f, 10, 0xFFFFFFFF, false, mat, vp, TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0);
            ms.pop();
        }
    }
}

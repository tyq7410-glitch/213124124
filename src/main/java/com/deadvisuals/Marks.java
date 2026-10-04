package com.deadvisuals;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public final class Marks {
    private Marks() {
    }

    private static final List<Vec3d> LIST = new ArrayList<>();

    public static void place(MinecraftClient mc) {
        ClientPlayerEntity p = mc.player;
        if (p == null || !Mods.MARKS.enabled) return;
        HitResult hr = p.raycast(256.0, 1.0f, false);
        if (hr.getType() == HitResult.Type.MISS) return;
        LIST.add(hr.getPos());
        while (LIST.size() > (int) Mods.MARKS.v("Max marks")) LIST.remove(0);
        Helper.say("mark set!");
    }

    public static void addAuto(Vec3d v) {
        LIST.add(v);
        while (LIST.size() > (int) Mods.MARKS.v("Max marks")) LIST.remove(0);
        Helper.say("death point saved");
    }

    public static void clear() {
        LIST.clear();
    }

    public static void render(WorldRenderContext ctx, MinecraftClient mc, MatrixStack ms,
                              VertexConsumerProvider vp, Vec3d cam) {
        Module m = Mods.MARKS;
        if (!m.enabled || LIST.isEmpty()) return;
        int col = 0xFF000000 | Colors.pick(m.mode("Color"), m.v("Hue"), Fx.ticks);
        TextRenderer tr = mc.textRenderer;
        VertexConsumer lc = m.on("Beam") ? vp.getBuffer(RenderLayer.getLines()) : null;
        for (int i = 0; i < LIST.size(); i++) {
            Vec3d v = LIST.get(i);
            double dist = Math.sqrt(v.squaredDistanceTo(cam));
            if (lc != null) {
                Fx.line(lc, ms.peek(), v.subtract(cam), v.add(0, 5, 0).subtract(cam), col);
            }
            String s = "mark " + (i + 1) + (m.on("Show distance") ? "  " + (int) dist + "m" : "");
            float sc = 0.025f * (float) m.v("Scale") * (float) Math.max(1.0, dist / 8.0);
            ms.push();
            ms.translate(v.x - cam.x, v.y + 0.9 - cam.y, v.z - cam.z);
            ms.multiply(ctx.camera().getRotation());
            ms.scale(-sc, -sc, sc);
            float wd = tr.getWidth(s);
            tr.draw(s, -wd / 2f, 0, col, false, ms.peek().getPositionMatrix(), vp,
                    TextRenderer.TextLayerType.SEE_THROUGH, 0x70000000, 0xF000F0);
            ms.pop();
        }
    }
}

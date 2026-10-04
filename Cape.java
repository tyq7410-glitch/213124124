package com.deadvisuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

public final class Cape {
    private Cape() {
    }

    private static final String[] STYLES = {"angel", "demon", "dead", "neon", "ice", "gold", "galaxy",
            "blood", "sakura", "void", "toxic", "fire", "mono"};
    private static ModelPart model;
    private static Identifier[] tex;
    private static float sway;

    private static void init() {
        if (model != null) return;
        ModelData md = new ModelData();
        ModelPartData root = md.getRoot();
        root.addChild("cape", ModelPartBuilder.create().uv(0, 0).cuboid(-5, 0, 0, 10, 16, 1), ModelTransform.NONE);
        model = TexturedModelData.of(md, 64, 64).createModel();
        tex = new Identifier[STYLES.length];
        for (int i = 0; i < STYLES.length; i++) {
            tex[i] = Identifier.of("deadvisuals", "textures/wings/" + STYLES[i] + ".png");
        }
    }

    public static void render(MinecraftClient mc, MatrixStack ms, VertexConsumerProvider vp, Vec3d cam, float pt) {
        Module m = Mods.CAPE;
        ClientPlayerEntity p = mc.player;
        if (!m.enabled || p == null || p.isInvisible()) return;
        if (mc.options.getPerspective().isFirstPerson()) return;
        if (m.on("Hide with elytra") && p.getEquippedStack(EquipmentSlot.CHEST).isOf(Items.ELYTRA)) return;
        init();

        Vec3d pos = p.getLerpedPos(pt);
        float yaw = MathHelper.lerpAngleDegrees(pt, p.prevBodyYaw, p.bodyYaw);
        float speed = (float) p.getVelocity().horizontalLength();
        float target = 0.08f + Math.min(1.1f, speed * 3.5f);
        sway += (target - sway) * 0.1f;
        model.pitch = sway + (float) Math.sin((Fx.ticks + pt) * 0.1) * 0.03f;
        model.setPivot(0, 0, 2.2f);

        ms.push();
        ms.translate(pos.x - cam.x, pos.y - cam.y, pos.z - cam.z);
        ms.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180f - yaw));
        ms.scale(-1f, -1f, 1f);
        ms.translate(0f, -1.501f, 0f);
        float s = (float) m.v("Scale");
        ms.scale(s, s, s);
        VertexConsumer vc = vp.getBuffer(RenderLayer.getEntityCutoutNoCull(tex[m.mode("Style")]));
        model.render(ms, vc, 0xF000F0, OverlayTexture.DEFAULT_UV);
        ms.pop();
    }
}

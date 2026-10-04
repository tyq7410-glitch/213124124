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

public final class Wings {
    private Wings() {
    }

    private static final String[] STYLES = {"angel", "demon", "dead", "neon", "ice", "gold", "galaxy"};
    private static final float[] LEN = {6, 11, 15, 14, 10, 5};
    private static ModelPart plus;
    private static ModelPart minus;
    private static Identifier[] tex;

    private static ModelPart build(boolean positive) {
        ModelData md = new ModelData();
        ModelPartData root = md.getRoot();
        for (int i = 0; i < LEN.length; i++) {
            float len = LEN[i];
            float y = -9 + i * 3;
            root.addChild("f" + i,
                    ModelPartBuilder.create().uv(0, i * 4).cuboid(positive ? 0 : -len, y, 0, len, 3, 1),
                    ModelTransform.NONE);
        }
        return TexturedModelData.of(md, 64, 64).createModel();
    }

    private static void init() {
        if (plus != null) return;
        plus = build(true);
        minus = build(false);
        tex = new Identifier[STYLES.length];
        for (int i = 0; i < STYLES.length; i++) {
            tex[i] = Identifier.of("deadvisuals", "textures/wings/" + STYLES[i] + ".png");
        }
    }

    public static void render(MinecraftClient mc, MatrixStack ms, VertexConsumerProvider vp, Vec3d cam, float pt) {
        Module m = Mods.WINGS;
        ClientPlayerEntity p = mc.player;
        if (!m.enabled || p == null || p.isInvisible()) return;
        if (mc.options.getPerspective().isFirstPerson()) return;
        if (m.on("Hide with elytra") && p.getEquippedStack(EquipmentSlot.CHEST).isOf(Items.ELYTRA)) return;
        init();

        Vec3d pos = p.getLerpedPos(pt);
        float yaw = MathHelper.lerpAngleDegrees(pt, p.prevBodyYaw, p.bodyYaw);
        float t = (Fx.ticks + pt) * 0.15f * (float) m.v("Flap speed");
        float flap = (float) (Math.sin(t) * m.v("Flap angle"));
        if (p.isOnGround()) flap *= 0.25f;
        float ang = (float) m.v("Spread") + flap;
        plus.yaw = -ang;
        minus.yaw = ang;
        float h = (float) m.v("Height");
        plus.setPivot(3, 4 + h, 2.5f);
        minus.setPivot(-3, 4 + h, 2.5f);

        ms.push();
        ms.translate(pos.x - cam.x, pos.y - cam.y, pos.z - cam.z);
        ms.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180f - yaw));
        ms.scale(-1f, -1f, 1f);
        ms.translate(0f, -1.501f, 0f);
        float s = (float) m.v("Scale");
        ms.scale(s, s, s);
        VertexConsumer vc = vp.getBuffer(RenderLayer.getEntityCutoutNoCull(tex[m.mode("Style")]));
        plus.render(ms, vc, 0xF000F0, OverlayTexture.DEFAULT_UV);
        minus.render(ms, vc, 0xF000F0, OverlayTexture.DEFAULT_UV);
        ms.pop();
    }
}

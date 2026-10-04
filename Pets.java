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
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

public final class Pets {
    private Pets() {
    }

    private static final String[] STYLES = {"ghost", "imp", "cube", "cat", "fox", "slime", "pumpkin", "frost", "gold"};
    private static final boolean[] EARS = {false, true, false, true, true, false, false, false, false};
    private static ModelPart model;
    private static Identifier[] tex;
    private static Vec3d pos;
    private static long lastNs;
    private static float yawS;

    public static void reset() {
        pos = null;
    }

    private static void init() {
        if (model != null) return;
        ModelData md = new ModelData();
        ModelPartData r = md.getRoot();
        r.addChild("body", ModelPartBuilder.create().uv(0, 0).cuboid(-3, -3, -3, 6, 6, 6), ModelTransform.NONE);
        r.addChild("eyeL", ModelPartBuilder.create().uv(0, 12).cuboid(-2.5f, -1.5f, -3.2f, 1.5f, 1.5f, 0.4f), ModelTransform.NONE);
        r.addChild("eyeR", ModelPartBuilder.create().uv(8, 12).cuboid(1f, -1.5f, -3.2f, 1.5f, 1.5f, 0.4f), ModelTransform.NONE);
        r.addChild("earL", ModelPartBuilder.create().uv(0, 16).cuboid(-3, -5, -1, 2, 2, 2), ModelTransform.NONE);
        r.addChild("earR", ModelPartBuilder.create().uv(8, 16).cuboid(1, -5, -1, 2, 2, 2), ModelTransform.NONE);
        model = TexturedModelData.of(md, 32, 32).createModel();
        tex = new Identifier[STYLES.length];
        for (int i = 0; i < STYLES.length; i++) {
            tex[i] = Identifier.of("deadvisuals", "textures/pets/" + STYLES[i] + ".png");
        }
    }

    public static void tick(ClientWorld w) {
        Module m = Mods.PETS;
        if (pos == null || !m.enabled || !m.on("Sparkles")) return;
        if (Fx.ticks % 5 == 0 && Perf.scale() >= 1f) {
            w.addParticle(ParticleTypes.END_ROD, pos.x, pos.y, pos.z, 0, -0.01, 0);
        }
    }

    public static void render(MinecraftClient mc, MatrixStack ms, VertexConsumerProvider vp, Vec3d cam, float pt) {
        Module m = Mods.PETS;
        ClientPlayerEntity p = mc.player;
        if (!m.enabled || p == null || p.isInvisible()) return;
        init();

        long ns = System.nanoTime();
        float dt = lastNs == 0 ? 0.016f : Math.min(0.1f, (ns - lastNs) / 1e9f);
        lastNs = ns;

        Vec3d pp = p.getLerpedPos(pt);
        float by = MathHelper.lerpAngleDegrees(pt, p.prevBodyYaw, p.bodyYaw);
        double yr = Math.toRadians(by);
        double d = m.v("Distance");
        Vec3d tgt = pp.add(
                -Math.cos(yr) * d + Math.sin(yr) * d * 0.5,
                m.v("Height") + Math.sin((Fx.ticks + pt) * 0.12) * 0.12 * m.v("Bob"),
                -Math.sin(yr) * d - Math.cos(yr) * d * 0.5);
        if (pos == null || pos.squaredDistanceTo(tgt) > 256) pos = tgt;
        else pos = pos.lerp(tgt, 1.0 - Math.exp(-m.v("Speed") * dt));
        yawS += MathHelper.wrapDegrees(by - yawS) * (1f - (float) Math.exp(-8 * dt));

        int style = m.mode("Style");
        boolean ears = EARS[style];
        model.getChild("earL").visible = ears;
        model.getChild("earR").visible = ears;

        ms.push();
        ms.translate(pos.x - cam.x, pos.y - cam.y, pos.z - cam.z);
        ms.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180f - yawS));
        ms.scale(-1f, -1f, 1f);
        float s = (float) m.v("Scale");
        ms.scale(s, s, s);
        VertexConsumer vc = vp.getBuffer(RenderLayer.getEntityCutoutNoCull(tex[style]));
        model.render(ms, vc, 0xF000F0, OverlayTexture.DEFAULT_UV);
        ms.pop();
    }
}

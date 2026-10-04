package com.deadvisuals.mixin;

import com.deadvisuals.Visuals;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin {
    @Inject(method = "getSkyColor", at = @At("RETURN"), cancellable = true, require = 0)
    private void dv$skyColor(Vec3d cameraPos, float tickDelta, CallbackInfoReturnable<Object> cir) {
        Object r = Visuals.skyColor(cir.getReturnValue());
        if (r != null) cir.setReturnValue(r);
    }
}

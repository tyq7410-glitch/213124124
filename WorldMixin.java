package com.deadvisuals.mixin;

import com.deadvisuals.Visuals;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public abstract class WorldMixin {
    @Inject(method = "getSkyAngle", at = @At("RETURN"), cancellable = true, require = 0)
    private void dv$skyAngle(float tickDelta, CallbackInfoReturnable<Float> cir) {
        if (!((Object) this instanceof ClientWorld)) return;
        cir.setReturnValue(Visuals.skyAngle(cir.getReturnValue()));
    }

    @Inject(method = "getRainGradient", at = @At("RETURN"), cancellable = true, require = 0)
    private void dv$rain(float delta, CallbackInfoReturnable<Float> cir) {
        if (!((Object) this instanceof ClientWorld)) return;
        cir.setReturnValue(Visuals.rain(cir.getReturnValue()));
    }

    @Inject(method = "getThunderGradient", at = @At("RETURN"), cancellable = true, require = 0)
    private void dv$thunder(float delta, CallbackInfoReturnable<Float> cir) {
        if (!((Object) this instanceof ClientWorld)) return;
        cir.setReturnValue(Visuals.thunder(cir.getReturnValue()));
    }
}

package com.boatleash.mixin.client;

import com.boatleash.client.BoatLeashRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.BoatEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.vehicle.BoatEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BoatEntityRenderer.class)
public class BoatEntityRendererMixin {

    @Inject(method = "render*", at = @At("RETURN"))
    private void boatLeash$render(BoatEntity boatEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        BoatLeashRenderer.renderLeash(boatEntity, g, matrixStack, vertexConsumerProvider, i);
    }
}

package com.boatleash.mixin;

import com.boatleash.config.ModConfig;
import com.boatleash.leash.BoatLeashAccess;
import com.boatleash.leash.BoatLeashComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BoatEntity.class)
public abstract class BoatEntityMixin implements BoatLeashAccess {
    @Unique
    private final BoatLeashComponent boatLeash$leash = new BoatLeashComponent();

    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void boatLeash$interact(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (!ModConfig.enabled) {
            return;
        }

        if (player.getStackInHand(hand).getItem() == Items.LEAD) {
            boatLeash$leash.setHolder(player);
            if (!player.abilities.creativeMode) {
                player.getStackInHand(hand).decrement(1);
            }
            cir.setReturnValue(ActionResult.success(player.world.isClient));
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void boatLeash$tick(CallbackInfo ci) {
        BoatEntity boat = (BoatEntity) (Object) this;
        if (!ModConfig.enabled || boat.world.isClient || !boatLeash$leash.hasLeash()) {
            return;
        }

        Entity holder = boatLeash$leash.getHolder();
        if (holder == null || !holder.isAlive()) {
            boatLeash$leash.clear();
            return;
        }

        double dist = boat.distanceTo(holder);
        if (dist > ModConfig.breakDistance) {
            boatLeash$leash.clear();
            return;
        }

        if (dist > ModConfig.maxDistance) {
            double dx = (holder.getX() - boat.getX()) / dist;
            double dz = (holder.getZ() - boat.getZ()) / dist;
            double stretch = dist - ModConfig.maxDistance;

            // Stretch-based pull keeps movement smooth as distance changes.
            double strength = Math.min(ModConfig.pullForce, stretch * 0.05D);
            boat.addVelocity(dx * strength, 0.0D, dz * strength);
            boat.velocityModified = true;
        }
    }

    @Override
    public Entity boatLeash$getHolder() {
        return boatLeash$leash.getHolder();
    }

    @Override
    public void boatLeash$setHolder(Entity holder) {
        boatLeash$leash.setHolder(holder);
    }

    @Override
    public void boatLeash$clearHolder() {
        boatLeash$leash.clear();
    }
}
package com.boatleash.mixin;

import com.boatleash.config.ModConfig;
import com.boatleash.leash.BoatLeashAccess;
import com.boatleash.leash.BoatLeashComponent;
import com.boatleash.net.BoatLeashNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
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

    @Unique
    private int boatLeash$syncTimer = 0;

    // 🔗 Attach or Detach leash
    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void boatLeash$interact(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (!ModConfig.enabled) return;

        BoatEntity boat = (BoatEntity) (Object) this;

        if (boatLeash$leash.hasLeash()) {
            if (boatLeash$leash.getHolder() == player) {
                if (!boat.world.isClient) {
                    this.boatLeash$clearHolder();
                    boat.dropItem(Items.LEAD);
                }
                cir.setReturnValue(ActionResult.success(boat.world.isClient));
            }
            return;
        }

        if (player.getStackInHand(hand).getItem() == Items.LEAD) {
            if (!boat.world.isClient) {
                this.boatLeash$setHolder(player);
                if (!player.abilities.creativeMode) {
                    player.getStackInHand(hand).decrement(1);
                }
            }
            cir.setReturnValue(ActionResult.success(boat.world.isClient));
        }
    }

    // 🚤 Physics tick (SMOOTH + OPTIMIZED)
    @Inject(method = "tick", at = @At("TAIL"))
    private void boatLeash$tick(CallbackInfo ci) {
        BoatEntity boat = (BoatEntity) (Object) this;

        if (!ModConfig.enabled || !boatLeash$leash.hasLeash()) {
            return;
        }

        if (boat.world.isClient) {
            return; // Only server handles physics pulling
        }

        if (this.boatLeash$syncTimer++ % 20 == 0) {
            this.boatLeash$sync();
        }

        Entity holder = boatLeash$leash.getHolder();
        if (holder == null && boatLeash$leash.getHolderUUID() != null && boat.world instanceof ServerWorld) {
            holder = ((ServerWorld) boat.world).getEntity(boatLeash$leash.getHolderUUID());
            if (holder != null) {
                this.boatLeash$setHolder(holder);
            } else {
                return; // Holder is missing (offline/unloaded), wait for them
            }
        }

        if (holder == null) {
            return;
        }

        if (holder.removed || (holder instanceof net.minecraft.entity.LivingEntity && ((net.minecraft.entity.LivingEntity)holder).getHealth() <= 0.0F)) {
            if (holder instanceof net.minecraft.entity.LivingEntity && ((net.minecraft.entity.LivingEntity)holder).getHealth() > 0.0F) {
                // Holder was removed (unloaded/disconnected), but not dead. Keep UUID.
                boatLeash$leash.setHolder(null);
                return;
            }
            // Holder is dead or completely invalid
            this.boatLeash$clearHolder();
            boat.dropItem(Items.LEAD);
            return;
        }

        double dx = holder.getX() - boat.getX();
        double dy = holder.getY() - boat.getY();
        double dz = holder.getZ() - boat.getZ();

        double distSq = dx * dx + dy * dy + dz * dz;
        double dist = Math.sqrt(distSq);

        // break leash if too far
        if (dist > ModConfig.breakDistance) {
            this.boatLeash$clearHolder();
            boat.dropItem(Items.LEAD);
            return;
        }

        // apply pulling force
        if (dist > ModConfig.maxDistance) {

            double invDist = 1.0 / dist;
            double nx = dx * invDist;
            double ny = dy * invDist;
            double nz = dz * invDist;

            double stretch = dist - ModConfig.maxDistance;

            // 🚀 STRONGER physics for mobs (0.15 base + stretch + multiplier)
            double strength = Math.min(ModConfig.pullForce * 1.5, 0.15 + stretch * 0.1);

            // sprint boost (important)
            if (holder instanceof PlayerEntity && ((PlayerEntity) holder).isSprinting()) {
                strength *= 1.5;
            }

            // apply velocity with high vertical dampening but enough to clear water blocks
            boat.addVelocity(nx * strength, ny * 0.06, nz * strength);
            boat.velocityModified = true;
        }

        // damping (prevents jitter, VERY important)
        // slightly less damping for smoother movement when pulled
        boat.setVelocity(
                boat.getVelocity().x * 0.95,
                boat.getVelocity().y,
                boat.getVelocity().z * 0.95
        );
        boat.velocityModified = true;
    }

    // accessors
    @Override
    public Entity boatLeash$getHolder() {
        return boatLeash$leash.getHolder();
    }

    @Override
    public void boatLeash$setHolder(Entity holder) {
        boatLeash$leash.setHolder(holder);
        this.boatLeash$sync();
    }

    @Override
    public void boatLeash$clearHolder() {
        boatLeash$leash.clear();
        this.boatLeash$sync();
    }

    private void boatLeash$sync() {
        BoatEntity boat = (BoatEntity) (Object) this;
        if (!boat.world.isClient && boat.world instanceof ServerWorld) {
            BoatLeashNetworking.sendLeashSync((ServerWorld) boat.world, boat, boatLeash$leash.getHolder());
        }
    }


    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void boatLeash$writeNbt(NbtCompound tag, CallbackInfo ci) {
        if (boatLeash$leash.hasLeash() && boatLeash$leash.getHolder() != null) {
            tag.putUuid("LeashHolder", boatLeash$leash.getHolder().getUuid());
        }
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void boatLeash$readNbt(NbtCompound tag, CallbackInfo ci) {
        if (tag.containsUuid("LeashHolder")) {
            boatLeash$leash.setHolderUUID(tag.getUuid("LeashHolder"));
        }
    }
}
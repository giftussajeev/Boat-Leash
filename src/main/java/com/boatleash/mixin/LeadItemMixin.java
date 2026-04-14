package com.boatleash.mixin;

import com.boatleash.config.ModConfig;
import com.boatleash.leash.BoatLeashAccess;
import net.minecraft.block.FenceBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.LeashKnotEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.LeadItem;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LeadItem.class)
public class LeadItemMixin {

    @Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
    private void boatLeash$useOnFence(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        if (!ModConfig.enabled) {
            return;
        }

        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        PlayerEntity player = context.getPlayer();

        if (player == null || !(world.getBlockState(pos).getBlock() instanceof FenceBlock)) {
            return;
        }

        LeashKnotEntity knot = LeashKnotEntity.getOrCreate(world, pos);
        boolean attached = false;

        for (Entity entity : world.getOtherEntities(player, player.getBoundingBox().expand(12.0D))) {
            if (entity instanceof BoatEntity && entity instanceof BoatLeashAccess) {
                BoatLeashAccess access = (BoatLeashAccess) entity;
                if (access.boatLeash$getHolder() == player) {
                    access.boatLeash$setHolder(knot);
                    attached = true;
                }
            }
        }

        if (attached) {
            cir.setReturnValue(ActionResult.success(world.isClient));
        }
    }
}
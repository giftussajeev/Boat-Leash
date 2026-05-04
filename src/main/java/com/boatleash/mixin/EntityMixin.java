package com.boatleash.mixin;

import com.boatleash.leash.BoatLeashAccess;
import net.minecraft.entity.Entity;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Inject(method = "remove", at = @At("HEAD"))
    private void boatLeash$onRemove(CallbackInfo ci) {
        if ((Object) this instanceof BoatLeashAccess) {
            BoatLeashAccess access = (BoatLeashAccess) (Object) this;
            if (access.boatLeash$getHolder() != null) {
                Entity entity = (Entity) (Object) this;
                if (!entity.world.isClient) {
                    entity.dropItem(Items.LEAD);
                    access.boatLeash$clearHolder();
                }
            }
        }
    }
}

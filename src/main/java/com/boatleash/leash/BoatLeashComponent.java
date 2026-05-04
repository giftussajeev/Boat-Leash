package com.boatleash.leash;

import net.minecraft.entity.Entity;
import java.util.UUID;

public class BoatLeashComponent {
    private Entity holder;
    private UUID holderUUID;

    public Entity getHolder() {
        return holder;
    }

    public void setHolder(Entity holder) {
        this.holder = holder;
        if (holder != null) {
            this.holderUUID = holder.getUuid();
        }
    }

    public UUID getHolderUUID() {
        return holderUUID;
    }

    public void setHolderUUID(UUID holderUUID) {
        this.holderUUID = holderUUID;
    }

    public boolean hasLeash() {
        return holder != null || holderUUID != null;
    }

    public void clear() {
        this.holder = null;
        this.holderUUID = null;
    }
}
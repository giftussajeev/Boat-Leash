package com.boatleash.leash;

import net.minecraft.entity.Entity;

public class BoatLeashComponent {
    private Entity holder;

    public Entity getHolder() {
        return holder;
    }

    public void setHolder(Entity holder) {
        this.holder = holder;
    }

    public boolean hasLeash() {
        return holder != null;
    }

    public void clear() {
        holder = null;
    }
}
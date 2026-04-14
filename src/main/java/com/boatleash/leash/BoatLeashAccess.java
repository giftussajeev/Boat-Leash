package com.boatleash.leash;

import net.minecraft.entity.Entity;

public interface BoatLeashAccess {
    Entity boatLeash$getHolder();

    void boatLeash$setHolder(Entity holder);

    void boatLeash$clearHolder();
}
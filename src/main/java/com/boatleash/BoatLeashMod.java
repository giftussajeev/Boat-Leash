package com.boatleash;

import com.boatleash.config.ModConfig;
import net.fabricmc.api.ModInitializer;

public class BoatLeashMod implements ModInitializer {

    public static final String MOD_ID = "boatleash";

    @Override
    public void onInitialize() {
        ModConfig.load();
        System.out.println("Boat Leash Mod Loaded");
    }
}
package com.boatleash;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class BoatLeashClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Packet receiving is handled via ClientNetworkHandlerMixin — no Fabric API needed.
    }
}

package com.boatleash.mixin.client;

import com.boatleash.leash.BoatLeashAccess;
import com.boatleash.net.BoatLeashNetworking;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientNetworkHandlerMixin {

    @Inject(method = "onCustomPayload", at = @At("HEAD"), cancellable = true)
    private void boatLeash$onCustomPayload(CustomPayloadS2CPacket packet, CallbackInfo ci) {
        if (!BoatLeashNetworking.SYNC_LEASH_PACKET.equals(packet.getChannel())) return;

        // Read data before buffer is released
        PacketByteBuf data = packet.getData();
        int boatId   = data.readInt();
        int holderId = data.readInt();

        MinecraftClient client = MinecraftClient.getInstance();
        client.execute(new Runnable() {
            @Override
            public void run() {
                if (client.world == null) return;

                Entity entity = client.world.getEntityById(boatId);
                if (!(entity instanceof BoatEntity)) return;
                if (!(entity instanceof BoatLeashAccess)) return;

                BoatLeashAccess access = (BoatLeashAccess) entity;
                if (holderId == -1) {
                    access.boatLeash$clearHolder();
                } else {
                    Entity holder = client.world.getEntityById(holderId);
                    if (holder != null) {
                        access.boatLeash$setHolder(holder);
                    }
                }
            }
        });

        ci.cancel(); // We handled it
    }
}

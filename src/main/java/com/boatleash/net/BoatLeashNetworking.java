package com.boatleash.net;

import com.boatleash.BoatLeashMod;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public class BoatLeashNetworking {

    public static final Identifier SYNC_LEASH_PACKET =
            new Identifier(BoatLeashMod.MOD_ID, "sync_leash");

    /**
     * Sends leash state to all players within 256 blocks of the boat,
     * using vanilla CustomPayloadS2CPacket — no Fabric API required.
     */
    public static void sendLeashSync(ServerWorld world, BoatEntity boat, Entity holder) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeInt(boat.getEntityId());
        buf.writeInt(holder == null ? -1 : holder.getEntityId());
        CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(SYNC_LEASH_PACKET, buf);

        Vec3d boatPos = boat.getPos();
        for (ServerPlayerEntity player : world.getPlayers()) {
            if (player.squaredDistanceTo(boatPos.x, boatPos.y, boatPos.z) < 256.0 * 256.0) {
                player.networkHandler.sendPacket(packet);
            }
        }
    }
}

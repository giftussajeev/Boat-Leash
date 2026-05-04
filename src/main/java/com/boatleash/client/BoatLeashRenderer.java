package com.boatleash.client;

import com.boatleash.leash.BoatLeashAccess;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;


/**
 * Renders a leash rope between the boat and its holder,
 * matching vanilla lead rendering from LivingEntityRenderer.renderLeash().
 */
public class BoatLeashRenderer {

    public static void renderLeash(BoatEntity boat, float tickDelta, MatrixStack matrices,
                                   VertexConsumerProvider provider, int light) {

        if (!(boat instanceof BoatLeashAccess)) return;
        Entity holder = ((BoatLeashAccess) boat).boatLeash$getHolder();
        if (holder == null) return;

        // --- Interpolated world positions ---
        double holderX = MathHelper.lerp(tickDelta, holder.prevX, holder.getX());
        double holderY = MathHelper.lerp(tickDelta, holder.prevY, holder.getY());
        double holderZ = MathHelper.lerp(tickDelta, holder.prevZ, holder.getZ());

        double boatX   = MathHelper.lerp(tickDelta, boat.prevX, boat.getX());
        double boatY   = MathHelper.lerp(tickDelta, boat.prevY, boat.getY());
        double boatZ   = MathHelper.lerp(tickDelta, boat.prevZ, boat.getZ());

        // Attachment points: holder eye-height * 0.7, boat deck
        double attachHolderY = holderY + holder.getStandingEyeHeight() * 0.7;
        double attachBoatY   = boatY   + 0.3;

        matrices.push();
        matrices.translate(0.0D, 0.3D, 0.0D);

        // The MatrixStack at this point has already been translated to the
        // entity's render origin by the EntityRenderer framework, so we must
        // undo that and work in camera-relative space.  The boat's camera-
        // space origin IS the origin of the current matrix, so delta from
        // boat to holder is all we need.
        float dx = (float)(holderX - boatX);
        float dy = (float)(attachHolderY - attachBoatY);
        float dz = (float)(holderZ - boatZ);

        VertexConsumer buf = provider.getBuffer(RenderLayer.getLeash());
        Matrix4f mat = matrices.peek().getModel();

        // Width vector perpendicular to the rope direction (XZ plane)
        float invSqrt = MathHelper.fastInverseSqrt(dx * dx + dz * dz) * 0.025f / 2.0f;
        float wX = dz * invSqrt;
        float wZ = dx * invSqrt;

        // Two passes: top ribbon + bottom ribbon — exactly as vanilla does it
        for (int i = 0; i <= 24; i++) {
            drawSegment(buf, mat, dx, dy, dz, light, 0.025f, 0.025f,  wX, wZ, i, false);
        }
        for (int i = 0; i <= 24; i++) {
            drawSegment(buf, mat, dx, dy, dz, light, 0.025f, 0.0f,    wX, wZ, i, true);
        }

        matrices.pop();
    }

    /**
     * Draws one quad-segment of the rope ribbon, matching vanilla's algorithm.
     *
     * @param isEdge  true = second (edge) pass, false = first (face) pass
     */
    private static void drawSegment(VertexConsumer buf, Matrix4f mat,
                                    float dx, float dy, float dz, int light,
                                    float widthOff, float lenOff,
                                    float wX, float wZ,
                                    int seg, boolean isEdge) {
        float t = seg / 24.0f;

        // Alternate dark / light segments like vanilla
        float bright = (seg % 2 == (isEdge ? 1 : 0)) ? 0.7f : 1.0f;
        float r = 0.5f * bright;
        float g = 0.4f * bright;
        float b = 0.3f * bright;

        // Catenary-ish sag: positive dy goes up linear, negative dy sags quadratic
        float px = dx * t;
        float py = dy > 0.0f ? dy * t * t : dy - dy * (1.0f - t) * (1.0f - t);
        float pz = dz * t;

        buf.vertex(mat, px - wX, py + lenOff, pz + wZ)
                .color(r, g, b, 1.0f).light(light).next();
        buf.vertex(mat, px + wX, py + widthOff - lenOff, pz - wZ)
                .color(r, g, b, 1.0f).light(light).next();
    }
}
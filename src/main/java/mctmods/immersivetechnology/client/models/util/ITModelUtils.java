package mctmods.immersivetechnology.client.models.util;

import blusunrize.immersiveengineering.client.utils.ModelUtils;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.pipeline.QuadBakingVertexConsumer;

public class ITModelUtils {
    public static BakedQuad reverseOrder(BakedQuad quad) {
        return ModelUtils.reverseOrder(quad);
    }

    public static BakedQuad createBakedQuad(Vec3[] vertices, Direction side, TextureAtlasSprite sprite, double[] uv, float[] color, boolean invert) {
        QuadBakingVertexConsumer consumer = new QuadBakingVertexConsumer();
        consumer.setSprite(sprite);
        consumer.setDirection(side);
        consumer.setShade(true);
        double u0 = uv[0];
        double v0 = uv[1];
        double u1 = uv[2];
        double v1 = uv[3];
        double[][] cornerUV = new double[][]{{u0, v0}, {u0, v1}, {u1, v1}, {u1, v0}};
        for (int i = 0; i < 4; i++) {
            int idx = invert ? (3 - i) : i;
            Vec3 v = vertices[idx];
            consumer.addVertex((float) v.x, (float) v.y, (float) v.z);
            consumer.setColor(color[0], color[1], color[2], color[3]);
            consumer.setUv(sprite.getU((float) (cornerUV[idx][0] / 16.0)), sprite.getV((float) (cornerUV[idx][1] / 16.0)));
            consumer.setNormal(side.getStepX(), side.getStepY(), side.getStepZ());
        }
        return consumer.bakeQuad();
    }
}

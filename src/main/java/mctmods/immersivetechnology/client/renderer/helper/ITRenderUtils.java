package mctmods.immersivetechnology.client.renderer.helper;

import blusunrize.immersiveengineering.api.utils.DirectionUtils;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.config.IEClientConfig;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.joml.Matrix3fc;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;
import java.util.List;

public class ITRenderUtils {
    private static final Vector4f[] quadCoords = new Vector4f[4];
    private static final int[][] neighbourBrightness = new int[2][6];
    private static final float[][] normalizationFactors = new float[2][8];
    private static final VertexFormat FORMAT = DefaultVertexFormat.BLOCK;
    private static final int VERTEX_SIZE = FORMAT.getIntegerSize();
    private static final int UV_OFFSET = ClientUtils.findTextureOffset(FORMAT);
    private static final int POSITION_OFFSET = ClientUtils.findPositionOffset(FORMAT);

    static {
        for (int i = 0; i < quadCoords.length; ++i) { quadCoords[i] = new Vector4f(); }
    }

    public static void renderModelTESRFancy(List<BakedQuad> quads, VertexConsumer renderer, PoseStack transform, Level world, BlockPos pos, boolean useCached, int color, int light) {
        if (IEClientConfig.disableFancyTESR.get()) { renderModelTESRFast(quads, renderer, transform, -1, LevelRenderer.getLightColor(world, pos), OverlayTexture.NO_OVERLAY); }
        else {
            if (!useCached) {
                for (Direction f : DirectionUtils.VALUES) {
                    int val = LevelRenderer.getLightColor(world, pos.relative(f));
                    neighbourBrightness[0][f.get3DDataValue()] = val >> 16 & 255;
                    neighbourBrightness[1][f.get3DDataValue()] = val & 255;
                }
                for (int type = 0; type < 2; ++type) for (int i = 0; i < 8; ++i) { normalizationFactors[type][i] = (float) Math.sqrt(computeSSquared(type, i)); }
            }
            int[] rgba = {255, 255, 255, 255};
            if (color >= 0) {
                rgba[0] = color >> 16 & 255;
                rgba[1] = color >> 8 & 255;
                rgba[2] = color & 255;
            }
            Matrix4fc positionTransform = transform.last().pose();
            Matrix3fc normalTransform = transform.last().normal();
            for (BakedQuad quad : quads) {
                int[] vData = quad.getVertices();
                for (int i = 0; i < 4; ++i) { quadCoords[i].set(Float.intBitsToFloat(vData[VERTEX_SIZE * i + POSITION_OFFSET]), Float.intBitsToFloat(vData[VERTEX_SIZE * i + POSITION_OFFSET + 1]), Float.intBitsToFloat(vData[VERTEX_SIZE * i + POSITION_OFFSET + 2]), 1.0F); }
                Vector3f normal = new Vector3f(quadCoords[1].x, quadCoords[1].y, quadCoords[1].z);
                Vector3f side2 = new Vector3f(quadCoords[2].x, quadCoords[2].y, quadCoords[2].z);
                normal.add(-quadCoords[3].x(), -quadCoords[3].y(), -quadCoords[3].z());
                side2.add(-quadCoords[0].x(), -quadCoords[0].y(), -quadCoords[0].z());
                normal.cross(side2);
                normal.normalize();
                int l1 = getLightValue(neighbourBrightness[1], normalizationFactors[1], light & 255, normal);
                int l2 = getLightValue(neighbourBrightness[0], normalizationFactors[0], light >> 16 & 255, normal);
                normal.mul(normalTransform);
                for (int i = 0; i < 4; ++i) {
                    Vector4f vertexPos = quadCoords[i];
                    vertexPos.mul(positionTransform);
                    renderer.vertex(vertexPos.x(), vertexPos.y(), vertexPos.z(), rgba[0] / 255.0F, rgba[1] / 255.0F, rgba[2] / 255.0F, rgba[3] / 255.0F, Float.intBitsToFloat(vData[VERTEX_SIZE * i + UV_OFFSET]), Float.intBitsToFloat(vData[VERTEX_SIZE * i + UV_OFFSET + 1]), OverlayTexture.NO_OVERLAY, LightTexture.pack(l1 >> 4, l2 >> 4), normal.x(), normal.y(), normal.z());
                }
            }
        }
    }

    private static int getLightValue(int[] neighbourBrightness, float[] normalizationFactors, int localBrightness, Vector3f normal) {
        byte type = 0;
        double sideBrightness;
        if (normal.x() > 0.0F) {
            sideBrightness = normal.x() * neighbourBrightness[5];
            type |= 1;
        } else { sideBrightness = -normal.x() * neighbourBrightness[4]; }
        if (normal.y() > 0.0F) {
            sideBrightness += normal.y() * neighbourBrightness[1];
            type |= 2;
        } else { sideBrightness += -normal.y() * neighbourBrightness[0]; }
        if (normal.z() > 0.0F) {
            sideBrightness += normal.z() * neighbourBrightness[3];
            type |= 4;
        } else { sideBrightness += -normal.z() * neighbourBrightness[2]; }
        return (int)((localBrightness + sideBrightness / normalizationFactors[type]) / 2.0F);
    }

    private static float scaledSquared(int val) { return val / 255.0F * (val / 255.0F); }

    private static float computeSSquared(int type, int i) {
        float sSquared = 0.0F;
        if ((i & 1) != 0) { sSquared += scaledSquared(neighbourBrightness[type][5]); }
        else { sSquared += scaledSquared(neighbourBrightness[type][4]); }
        if ((i & 2) != 0) { sSquared += scaledSquared(neighbourBrightness[type][1]); }
        else { sSquared += scaledSquared(neighbourBrightness[type][0]); }
        if ((i & 4) != 0) { sSquared += scaledSquared(neighbourBrightness[type][3]); }
        else { sSquared += scaledSquared(neighbourBrightness[type][2]); }
        return sSquared;
    }

    public static void renderModelTESRFast(List<BakedQuad> quads, VertexConsumer renderer, PoseStack transform, int color, int light, int overlay) {
        float red = 1.0F, green = 1.0F, blue = 1.0F;
        if (color >= 0) {
            red = (color >> 16 & 255) / 255.0F;
            green = (color >> 8 & 255) / 255.0F;
            blue = (color & 255) / 255.0F;
        }
        for (BakedQuad quad : quads) { renderer.putBulkData(transform.last(), quad, red, green, blue, light, overlay); }
    }
}

package mctmods.immersivetechnology.client.models.util;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Transformation;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class ModelUtils {

    public static BakedQuad createBakedQuad(Vec3[] vertices, Direction facing, TextureAtlasSprite sprite, double[] uvs, float[] colour, boolean invert) {
        ITBakedQuadBuilder builder = new ITBakedQuadBuilder();
        Vec3i normalInt = facing.getNormal();
        Vec3 faceNormal = new Vec3(normalInt.getX(), normalInt.getY(), normalInt.getZ());
        int vId = invert ? 3 : 0;
        int u = vId > 1 ? 2 : 0;
        builder.putVertexData(vertices[vId], faceNormal, uvs[u], uvs[1], sprite, colour, 1.0F);
        vId = invert ? 2 : 1;
        builder.putVertexData(vertices[vId], faceNormal, uvs[u], uvs[3], sprite, colour, 1.0F);
        vId = invert ? 1 : 2;
        u = vId > 1 ? 2 : 0;
        builder.putVertexData(vertices[vId], faceNormal, uvs[u], uvs[3], sprite, colour, 1.0F);
        vId = invert ? 0 : 3;
        builder.putVertexData(vertices[vId], faceNormal, uvs[u], uvs[1], sprite, colour, 1.0F);
        return builder.bake(-1, facing, sprite, true);
    }

    public static BakedQuad reverseOrder(BakedQuad in) {
        int[] oldData = in.getVertices();
        int[] newData = new int[oldData.length];
        int vertexLength = oldData.length / 4;
        for (int i = 0; i < 4; ++i) { System.arraycopy(oldData, i * vertexLength, newData, (3 - i) * vertexLength, vertexLength); }
        return new BakedQuad(newData, in.getTintIndex(), in.getDirection(), in.getSprite(), in.isShade());
    }

    public static Transformation fromItemTransform(ItemTransform transform, boolean leftHand) {
        Vector3f translate = new Vector3f(transform.translation);
        if (leftHand) { translate.setComponent(0, -translate.x()); }

        float rx = transform.rotation.x();
        float ry = transform.rotation.y();
        float rz = transform.rotation.z();
        if (leftHand) {
            ry = -ry;
            rz = -rz;
        }
        Quaternionf leftRotation = new Quaternionf().rotateXYZ(Mth.DEG_TO_RAD * rx, Mth.DEG_TO_RAD * ry, Mth.DEG_TO_RAD * rz);

        rx = transform.rightRotation.x();
        ry = transform.rightRotation.y() * (leftHand ? -1.0F : 1.0F);
        rz = transform.rightRotation.z() * (leftHand ? -1.0F : 1.0F);
        Quaternionf rightRotation = new Quaternionf().rotateXYZ(Mth.DEG_TO_RAD * rx, Mth.DEG_TO_RAD * ry, Mth.DEG_TO_RAD * rz);

        return new Transformation(translate, leftRotation, new Vector3f(transform.scale), rightRotation);
    }

    public static class ITBakedQuadBuilder {
        public static final VertexFormat FORMAT = DefaultVertexFormat.BLOCK;
        private static final int INTS_PER_VERTEX = FORMAT.getIntegerSize();

        private static final int POS_OFFSET = 0;
        private static final int COLOR_OFFSET = 3;
        private static final int UV_OFFSET = 4;
        private static final int LIGHTMAP_OFFSET = 6;
        private static final int NORMAL_OFFSET = 7;

        private int nextVertex = 0;
        private final int[] data = new int[INTS_PER_VERTEX * 4];

        public void putVertexData(Vec3 pos, Vec3 faceNormal, double u, double v, TextureAtlasSprite sprite, float[] colour, float alpha) {
            int base = nextVertex * INTS_PER_VERTEX;

            data[base + POS_OFFSET]     = Float.floatToIntBits((float) pos.x);
            data[base + POS_OFFSET + 1] = Float.floatToIntBits((float) pos.y);
            data[base + POS_OFFSET + 2] = Float.floatToIntBits((float) pos.z);

            data[base + COLOR_OFFSET] = (int)(colour[0] * 255) |
                    ((int)(colour[1] * 255) << 8) |
                    ((int)(colour[2] * 255) << 16) |
                    ((int)(colour[3] * alpha * 255) << 24);

            data[base + UV_OFFSET]     = Float.floatToIntBits(sprite.getU((float) u));
            data[base + UV_OFFSET + 1] = Float.floatToIntBits(sprite.getV((float) v));

            // TODO: Fix lighting on the Cooling Tower - changed from 0xF00000 to 0 for the time being to fix lighting in enclosed spaces
            data[base + LIGHTMAP_OFFSET] = 0;

            int normalPacked = ((int)(faceNormal.x * 127) & 0xFF) |
                    (((int)(faceNormal.y * 127) & 0xFF) << 8) |
                    (((int)(faceNormal.z * 127) & 0xFF) << 16);
            data[base + NORMAL_OFFSET] = normalPacked;

            ++nextVertex;
        }

        public BakedQuad bake(int tint, Direction side, TextureAtlasSprite texture, boolean shade) {
            return new BakedQuad(data, tint, side, texture, shade);
        }
    }
}

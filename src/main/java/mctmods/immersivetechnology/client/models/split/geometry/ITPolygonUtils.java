package mctmods.immersivetechnology.client.models.split.geometry;

import blusunrize.immersiveengineering.client.utils.BakedQuadBuilder;
import com.google.common.base.Preconditions;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import com.mojang.math.Transformation;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

public class ITPolygonUtils {

    private static final int VERTEX_SIZE_INTS;
    private static final int POS_OFFSET;
    private static final int UV_OFFSET;
    private static final int NORMAL_OFFSET;
    private static final int COLOR_OFFSET;

    static {
        VERTEX_SIZE_INTS = DefaultVertexFormat.BLOCK.getVertexSize() / 4;
        POS_OFFSET = getOffset(VertexFormatElement.POSITION);
        UV_OFFSET = getOffset(VertexFormatElement.UV);
        NORMAL_OFFSET = getOffset(VertexFormatElement.NORMAL);
        COLOR_OFFSET = getOffset(VertexFormatElement.COLOR);
    }

    private static int getOffset(VertexFormatElement element) {
        int offset = 0;
        for (VertexFormatElement e : DefaultVertexFormat.BLOCK.getElements()) {
            if (e == element) {
                return offset / 4;
            } else {
                offset += e.byteSize();
            }
        }
        throw new IllegalStateException("Did not find element with usage " + element.usage().name() + " and type " + element.type().name());
    }

    public static ITPolygon<ExtraQuadData> toPolygon(BakedQuad quad) {
        List<ITVertex> vertices = new ArrayList<>(4);
        final int[] verts = quad.getVertices();
        final int color = verts[COLOR_OFFSET];

        for (int v = 0; v < 4; ++v) {
            final int base = v * VERTEX_SIZE_INTS;

            int packedNormal = verts[NORMAL_OFFSET + base];
            final ITVec3d normalVec = new ITVec3d(
                    (byte) packedNormal,
                    (byte) (packedNormal >> 8),
                    (byte) (packedNormal >> 16)
            ).normalize();

            final ITUVCoords uv = new ITUVCoords(
                    Float.intBitsToFloat(verts[UV_OFFSET + base]),
                    Float.intBitsToFloat(verts[UV_OFFSET + base + 1])
            );

            final ITVec3d pos = new ITVec3d(
                    Float.intBitsToFloat(verts[base + POS_OFFSET]),
                    Float.intBitsToFloat(verts[base + POS_OFFSET + 1]),
                    Float.intBitsToFloat(verts[base + POS_OFFSET + 2])
            );

            vertices.add(new ITVertex(pos, normalVec, uv));
        }

        return new ITPolygon<>(vertices, new ExtraQuadData(
                quad.getSprite(),
                new Vector4f((color & 255) / 255f, ((color >> 8) & 255) / 255f, ((color >> 16) & 255) / 255f, (color >> 24) / 255f))
        );
    }

    public static BakedQuad toBakedQuad(List<ITVertex> points, ExtraQuadData data, Transformation rotation, boolean absoluteUV) {
        Preconditions.checkArgument(points.size() == 4);

        BakedQuadBuilder quadBuilder = new BakedQuadBuilder();
        Vector3f normal = new Vector3f();

        for (ITVertex v : points) {
            Vector4f pos = new Vector4f();
            pos.set(toArray(v.position(), 4));
            normal.set(toArray(v.normal(), 3));

            pos.x -= 0.5f;
            pos.y -= 0.5f;
            pos.z -= 0.5f;

            rotation.transformPosition(pos);
            rotation.transformNormal(normal);

            pos.mul(1 / pos.w());

            pos.x += 0.5f;
            pos.y += 0.5f;
            pos.z += 0.5f;

            final double epsilon = 1e-5;
            for (int i = 0; i < 2; ++i) {
                if (Math.abs(i - pos.x()) < epsilon) { pos.setComponent(0, i); }
                if (Math.abs(i - pos.y()) < epsilon) { pos.setComponent(1, i); }
                if (Math.abs(i - pos.z()) < epsilon) { pos.setComponent(2, i); }
            }

            float shade = Math.min(normal.x() * normal.x() * 0.6f + normal.y() * normal.y() * ((3.0f + normal.y()) / 4.0f) + normal.z() * normal.z() * 0.8f, 1.0f);

            quadBuilder.putVertexData(
                    new Vec3(pos.x(), pos.y(), pos.z()),
                    new Vec3(normal),
                    absoluteUV ? v.uv().u() : data.sprite().getU((float) v.uv().u()),
                    absoluteUV ? v.uv().v() : data.sprite().getV((float) (1 - v.uv().v())),
                    new float[]{data.color.x() * shade, data.color.y() * shade, data.color.z() * shade, data.color.w()},
                    1
            );
        }

        BakedQuad quad = quadBuilder.bake(-1, Direction.getNearest(normal.x(), normal.y(), normal.z()), data.sprite(), false);
        int[] verts = quad.getVertices();
        for (int i = 0; i < 4; ++i) { verts[i * VERTEX_SIZE_INTS + UV_OFFSET + 2] = 0xF00000; }
        return quad;
    }

    private static float[] toArray(ITVec3d vec, int length) {
        float[] ret = new float[length];
        for (int i = 0; i < 3; ++i) {
            ret[i] = (float) vec.get(i);
        }
        for (int i = 3; i < length; ++i) {
            ret[i] = 1.0f;
        }
        return ret;
    }

    public record ExtraQuadData(TextureAtlasSprite sprite, Vector4f color) {}
}

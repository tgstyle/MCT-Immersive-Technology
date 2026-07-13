package mctmods.immersivetechnology.client.models.split.geometry;

import mctmods.immersivetechnology.client.models.util.ITModelUtils;

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
        POS_OFFSET = getOffset(DefaultVertexFormat.ELEMENT_POSITION) / 4;
        UV_OFFSET = getOffset(DefaultVertexFormat.ELEMENT_UV) / 4;
        NORMAL_OFFSET = getOffset(DefaultVertexFormat.ELEMENT_NORMAL) / 4;
        COLOR_OFFSET = getOffset(DefaultVertexFormat.ELEMENT_COLOR) / 4;
    }

    private static int getOffset(VertexFormatElement element) {
        int off = 0;
        for (VertexFormatElement e : DefaultVertexFormat.BLOCK.getElements()) {
            if (e == element) { return off; }
            off += e.getByteSize();
        }
        throw new IllegalStateException("Element not found: " + element);
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

        ITModelUtils.ITBakedQuadBuilder quadBuilder = new ITModelUtils.ITBakedQuadBuilder();
        Vector3f normal = new Vector3f();
        float u0 = data.sprite().getU0();
        float u1 = data.sprite().getU1();
        float v0 = data.sprite().getV0();
        float v1 = data.sprite().getV1();

        for (ITVertex v : points) {
            Vector4f pos = new Vector4f();
            pos.set(toArray(v.position(), 4));
            normal.set(toArray(v.normal(), 3));

            rotation.transformPosition(pos);
            rotation.transformNormal(normal);

            pos.mul(1 / pos.w());

            final double epsilon = 1e-5;
            if (Math.abs(pos.x() - Math.round(pos.x())) < epsilon) { pos.setComponent(0, Math.round(pos.x())); }
            if (Math.abs(pos.y() - Math.round(pos.y())) < epsilon) { pos.setComponent(1, Math.round(pos.y())); }
            if (Math.abs(pos.z() - Math.round(pos.z())) < epsilon) { pos.setComponent(2, Math.round(pos.z())); }

            double builder_u = absoluteUV
                    ? (v.uv().u() - u0) / (u1 - u0) * 16.0
                    : 16.0 * v.uv().u();
            double builder_v = absoluteUV
                    ? (v.uv().v() - v0) / (v1 - v0) * 16.0
                    : 16.0 * (1.0 - v.uv().v());

            float shade = Math.min(normal.x() * normal.x() * 0.6f + normal.y() * normal.y() * ((3.0f + normal.y()) / 4.0f) + normal.z() * normal.z() * 0.8f, 1.0f);

            quadBuilder.putVertexData(
                    new Vec3(pos.x(), pos.y(), pos.z()),
                    new Vec3(normal),
                    builder_u,
                    builder_v,
                    data.sprite(),
                    new float[]{data.color.x() * shade, data.color.y() * shade, data.color.z() * shade, data.color.w()},
                    1.0f
            );
        }

        return quadBuilder.bake(-1, Direction.getNearest(normal.x(), normal.y(), normal.z()), data.sprite(), false);
    }

    private static float[] toArray(ITVec3d vec, int length) {
        float[] ret = new float[length];
        for (int i = 0; i < 3; ++i) { ret[i] = (float) vec.get(i); }
        for (int i = 3; i < length; ++i) { ret[i] = 1.0f; }
        return ret;
    }

    public record ExtraQuadData(TextureAtlasSprite sprite, Vector4f color) {}
}

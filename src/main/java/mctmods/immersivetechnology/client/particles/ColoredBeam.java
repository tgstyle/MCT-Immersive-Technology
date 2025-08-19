package mctmods.immersivetechnology.client.particles;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import java.lang.reflect.Field;

public class ColoredBeam {
    private static final RenderStateShard.ShaderStateShard BEACON_BEAM_SHADER;
    private static final RenderStateShard.TransparencyStateShard TRANSLUCENT_TRANSPARENCY;
    private static final RenderStateShard.TransparencyStateShard NO_TRANSPARENCY;
    private static final RenderStateShard.WriteMaskStateShard COLOR_WRITE;
    private static final RenderStateShard.WriteMaskStateShard COLOR_DEPTH_WRITE;
    private static final RenderStateShard.CullStateShard NO_CULL;

    static {
        try {
            BEACON_BEAM_SHADER = (RenderStateShard.ShaderStateShard) getProtectedField("RENDERTYPE_BEACON_BEAM_SHADER");
            TRANSLUCENT_TRANSPARENCY = (RenderStateShard.TransparencyStateShard) getProtectedField("TRANSLUCENT_TRANSPARENCY");
            NO_TRANSPARENCY = (RenderStateShard.TransparencyStateShard) getProtectedField("NO_TRANSPARENCY");
            COLOR_WRITE = (RenderStateShard.WriteMaskStateShard) getProtectedField("COLOR_WRITE");
            COLOR_DEPTH_WRITE = (RenderStateShard.WriteMaskStateShard) getProtectedField("COLOR_DEPTH_WRITE");
            NO_CULL = (RenderStateShard.CullStateShard) getProtectedField("NO_CULL");
        } catch (Exception e) {
            throw new RuntimeException("Failed to access RenderStateShard fields via reflection", e);
        }
    }

    private static Object getProtectedField(String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field field = RenderStateShard.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(null);
    }

    public static void renderBeam(PoseStack matrixStack, MultiBufferSource buffer, ResourceLocation texture, float partialTicks, float textureScale, long time, boolean upward, float innerBottomG, float innerTopG, float innerA, float outerBottomG, float outerTopG, float outerA, float yBottom, float yTop, double worldX, double worldYBottom, double worldYTop, double worldZ) {
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vec3 camPos = camera.getPosition();
        double dx = worldX - camPos.x;
        double dz = worldZ - camPos.z;
        double dhorSq = dx * dx + dz * dz;
        if (dhorSq >= 100 * 100) { return; }
        double delta = Math.sqrt(100 * 100 - dhorSq);
        double yLow = camPos.y - delta;
        double yHigh = camPos.y + delta;
        double clippedWorldBottom = Math.max(worldYBottom, yLow);
        double clippedWorldTop = Math.min(worldYTop, yHigh);
        if (clippedWorldBottom >= clippedWorldTop) { return; }
        float deltaBottom = (float)(clippedWorldBottom - worldYBottom);
        float deltaTop = (float)(clippedWorldTop - worldYTop);
        float renderYBottom = yBottom + deltaBottom;
        float renderYTop = yTop + deltaTop;
        matrixStack.pushPose();
        matrixStack.translate(0.5, 0, 0.5);
        float spin = (float) Math.floorMod(time, 40L) + partialTicks;
        float offset = upward ? -spin : spin;
        float frac = Mth.frac(offset * 0.2F - (float) Mth.floor(offset * 0.1F));
        float vMin = -1 + frac;
        float vMax = (yTop - yBottom) * textureScale + vMin;
        float renderVMin = vMin + deltaBottom * textureScale;
        float renderVMax = vMax + deltaTop * textureScale;
        matrixStack.pushPose();
        matrixStack.mulPose(Axis.YP.rotationDegrees(spin * (upward ? 2.25F : -2.25F) - 45.0F));
        float x0 = 0;
        float z0 = 0.2F;
        float x1 = 0.2F;
        float z1 = 0;
        float x2 = -0.2F;
        float z2 = 0;
        float x3 = 0;
        float z3 = -0.2F;
        float uMin = 0;
        float uMax = 1;
        renderPart(matrixStack, buffer.getBuffer(getBeamRenderType(texture, false)), innerTopG, innerBottomG, innerA, x0, z0, x1, z1, x2, z2, x3, z3, uMin, uMax, renderVMax, renderVMin, renderYTop, renderYBottom);
        matrixStack.popPose();
        x0 = -0.25F;
        z0 = -0.25F;
        x1 = 0.25F;
        z1 = -0.25F;
        x2 = 0.25F;
        z2 = 0.25F;
        x3 = -0.25F;
        z3 = 0.25F;
        renderPart(matrixStack, buffer.getBuffer(getBeamRenderType(texture, true)), outerTopG, outerBottomG, outerA, x0, z0, x1, z1, x2, z2, x3, z3, uMin, uMax, renderVMax, renderVMin, renderYTop, renderYBottom);
        matrixStack.popPose();
    }

    private static RenderType getBeamRenderType(ResourceLocation texture, boolean translucent) {
        RenderType.CompositeState state = RenderType.CompositeState.builder()
                .setShaderState(BEACON_BEAM_SHADER)
                .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                .setTransparencyState(translucent ? TRANSLUCENT_TRANSPARENCY : NO_TRANSPARENCY)
                .setWriteMaskState(translucent ? COLOR_WRITE : COLOR_DEPTH_WRITE)
                .setCullState(NO_CULL)
                .createCompositeState(false);
        return RenderType.create("colored_beam", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 256, false, translucent, state);
    }

    private static void renderPart(PoseStack matrixStack, VertexConsumer consumer, float gTop, float gBottom, float a, float x0, float z0, float x1, float z1, float x2, float z2, float x3, float z3, float uMin, float uMax, float vTop, float vBottom, float yTop, float yBottom) {
        Matrix4f pose = matrixStack.last().pose();
        Matrix3f normal = matrixStack.last().normal();
        renderQuad(pose, normal, consumer, gTop, gBottom, a, x0, z0, x1, z1, uMax, uMin, vTop, vBottom, yTop, yBottom);
        renderQuad(pose, normal, consumer, gTop, gBottom, a, x3, z3, x2, z2, uMax, uMin, vTop, vBottom, yTop, yBottom);
        renderQuad(pose, normal, consumer, gTop, gBottom, a, x0, z0, x3, z3, uMax, uMin, vTop, vBottom, yTop, yBottom);
        renderQuad(pose, normal, consumer, gTop, gBottom, a, x1, z1, x2, z2, uMax, uMin, vTop, vBottom, yTop, yBottom);
    }

    private static void renderQuad(Matrix4f pose, Matrix3f normal, VertexConsumer consumer, float gTop, float gBottom, float a, float x0, float z0, float x1, float z1, float u0, float u1, float vTop, float vBottom, float yTop, float yBottom) {
        addVertex(pose, normal, consumer, gTop, a, yTop, x0, z0, u0, vTop);
        addVertex(pose, normal, consumer, gBottom, a, yBottom, x0, z0, u0, vBottom);
        addVertex(pose, normal, consumer, gBottom, a, yBottom, x1, z1, u1, vBottom);
        addVertex(pose, normal, consumer, gTop, a, yTop, x1, z1, u1, vTop);
    }

    private static void addVertex(Matrix4f pose, Matrix3f normal, VertexConsumer consumer, float g, float a, float y, float x, float z, float u, float v) {
        consumer.vertex(pose, x, y, z).color(1.0F, g, 0.0F, a).uv(u, v).uv2(15728880).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
    }
}

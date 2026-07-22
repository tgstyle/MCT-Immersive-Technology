package mctmods.immersivetechnology.mixin;

import mctmods.immersivetechnology.core.MCTMixinConfig;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.client.ClientUtils;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.property.IExtendedBlockState;
import org.lwjgl.util.vector.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Mixin(value = ClientUtils.class, remap = false)
public abstract class MixinIEClientUtils {

    @Unique private static final float[] ALPHA_FADING_IT = {0, 0, 1, 1};
    @Unique private static final float[] ALPHA_NORMAL_IT = {1, 1, 1, 1};
    @Unique private static final Vector3f UP_IT = new Vector3f(0, 1, 0);
    @Unique private static final Vector3f FADING_OFFSET_IT = new Vector3f(0.0001F, 0.0001F, 0.0001F);

    @SuppressWarnings("unchecked")
    @Inject(method = "convertConnectionFromBlockstate(Lnet/minecraftforge/common/property/IExtendedBlockState;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;)[Ljava/util/List;", at = @At("HEAD"), cancellable = true, remap = false)
    private static void injectConvertConnection(IExtendedBlockState s, TextureAtlasSprite t, CallbackInfoReturnable<List<BakedQuad>[]> cir) {
        if (!MCTMixinConfig.mixinSettings.fix_IE_wires) { return; }

        List<BakedQuad>[] ret = new ArrayList[2];
        ret[0] = new ArrayList<>();
        ret[1] = new ArrayList<>();

        Set<Connection> conns = s.getValue(IEProperties.CONNECTIONS);
        if (conns == null || conns.isEmpty()) {
            cir.setReturnValue(ret);
            return;
        }

        Vector3f dir = new Vector3f();
        Vector3f cross = new Vector3f();

        for (Connection conn : conns) {
            Vec3d[] vertices = conn.catenaryVertices;
            if (vertices == null || vertices.length < 2) { continue; }

            int color = conn.cableType.getColour(conn);
            float[] rgb = new float[]{(color >> 16 & 255) / 255f,(color >> 8 & 255) / 255f,(color & 255) / 255f,1f};
            float radius = (float) (conn.cableType.getRenderDiameter() / 2.0);

            BlockPos basePos = conn.start;

            List<Integer> crossings = new ArrayList<>();
            for (int i = 1; i < vertices.length; i++) { if (ClientUtils.crossesChunkBoundary(vertices[i], vertices[i - 1], basePos)) { crossings.add(i); } }

            int max;
            if (crossings.size() <= 1) {
                boolean greater = conn.start.compareTo(conn.end) > 0;
                max = !crossings.isEmpty() ? crossings.get(0) + (greater ? 1 : 2) : (greater ? vertices.length + 1 : 0);
            }
            else { max = vertices.length; }

            for (int i = 1; i < max && i < vertices.length; i++) {
                boolean isFading = (i == max - 1);
                List<BakedQuad> targetList = ret[isFading ? 1 : 0];

                Vec3d v0 = vertices[i - 1];
                Vec3d v1 = vertices[i];

                Vector3f here = new Vector3f((float) v1.x, (float) v1.y, (float) v1.z);
                Vector3f there = new Vector3f((float) v0.x, (float) v0.y, (float) v0.z);

                if (isFading) {
                    Vector3f.add(here, FADING_OFFSET_IT, here);
                    Vector3f.add(there, FADING_OFFSET_IT, there);
                }

                boolean nearlyVertical = Math.abs(here.x - there.x) < 0.01 && Math.abs(here.z - there.z) < 0.01;

                if (!nearlyVertical) {
                    Vector3f.sub(here, there, dir);
                    Vector3f.cross(UP_IT, dir, cross);
                    float len = cross.length();
                    if (len > 0.001f) { cross.scale(radius / len); }
                    else { cross.set(radius, 0, 0); }
                }
                else { cross.set(radius, 0, 0); }

                Vector3f[] quadVerts = {add_IT(here, cross), sub_IT(here, cross),sub_IT(there, cross), add_IT(there, cross)};

                targetList.add(ClientUtils.createSmartLightingBakedQuad(net.minecraft.client.renderer.vertex.DefaultVertexFormats.ITEM, quadVerts, net.minecraft.util.EnumFacing.DOWN, t, rgb, false, isFading ? ALPHA_FADING_IT : ALPHA_NORMAL_IT, basePos));
                targetList.add(ClientUtils.createSmartLightingBakedQuad(net.minecraft.client.renderer.vertex.DefaultVertexFormats.ITEM, quadVerts, net.minecraft.util.EnumFacing.UP, t, rgb, true, isFading ? ALPHA_FADING_IT : ALPHA_NORMAL_IT, basePos));

                if (!nearlyVertical) {
                    Vector3f.cross(cross, dir, cross);
                    float len = cross.length();
                    if (len > 0.001f) { cross.scale(radius / len); }
                    else { cross.set(0, 0, radius); }
                }
                else { cross.set(0, 0, radius); }

                quadVerts = new Vector3f[]{add_IT(here, cross), sub_IT(here, cross),sub_IT(there, cross), add_IT(there, cross)};

                targetList.add(ClientUtils.createSmartLightingBakedQuad(net.minecraft.client.renderer.vertex.DefaultVertexFormats.ITEM, quadVerts, net.minecraft.util.EnumFacing.WEST, t, rgb, false, isFading ? ALPHA_FADING_IT : ALPHA_NORMAL_IT, basePos));
                targetList.add(ClientUtils.createSmartLightingBakedQuad(net.minecraft.client.renderer.vertex.DefaultVertexFormats.ITEM, quadVerts, net.minecraft.util.EnumFacing.EAST, t, rgb, true, isFading ? ALPHA_FADING_IT : ALPHA_NORMAL_IT, basePos));
            }
        }
        cir.setReturnValue(ret);
    }

    @Unique private static Vector3f add_IT(Vector3f a, Vector3f b) { return new Vector3f(a.x + b.x, a.y + b.y, a.z + b.z); }
    @Unique private static Vector3f sub_IT(Vector3f a, Vector3f b) { return new Vector3f(a.x - b.x, a.y - b.y, a.z - b.z); }
}

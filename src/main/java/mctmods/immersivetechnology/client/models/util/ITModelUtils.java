package mctmods.immersivetechnology.client.models.util;

import mctmods.immersivetechnology.client.models.helper.ITBakedQuadBuilder;
import mctmods.immersivetechnology.mixin.client.SimpleModelAccessMixin;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.RenderTypeGroup;

import java.util.List;

public class ITModelUtils {
    public static RenderTypeGroup copyTypes(SimpleBakedModel simpleModel) {
        SimpleModelAccessMixin access = (SimpleModelAccessMixin)simpleModel;
        ChunkRenderTypeSet blockTypes = access.getBlockRenderTypes();
        if (blockTypes != null && !blockTypes.isEmpty()) {
            List<RenderType> itemTypes = access.getItemRenderTypes();
            List<RenderType> fabulousItemTypes = access.getFabulousItemRenderTypes();
            return new RenderTypeGroup(blockTypes.iterator().next(), itemTypes.get(0), fabulousItemTypes.get(0));
        } else { return RenderTypeGroup.EMPTY; }
    }

    public static RenderTypeGroup copyTypesFast(SimpleBakedModel simpleModel) {
        SimpleModelAccessMixin access = (SimpleModelAccessMixin)simpleModel;
        ChunkRenderTypeSet blockTypes = access.getBlockRenderTypesFast();
        if (blockTypes != null && !blockTypes.isEmpty()) {
            List<RenderType> itemTypes = access.getItemRenderTypesFast();
            List<RenderType> fabulousItemTypes = access.getFabulousItemRenderTypes();
            return new RenderTypeGroup(blockTypes.iterator().next(), itemTypes.get(0), fabulousItemTypes.get(0));
        } else { return RenderTypeGroup.EMPTY; }
    }

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
}

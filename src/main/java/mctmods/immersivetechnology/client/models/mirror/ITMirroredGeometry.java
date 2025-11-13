package mctmods.immersivetechnology.client.models.mirror;

import blusunrize.immersiveengineering.api.utils.DirectionUtils;
import mctmods.immersivetechnology.client.models.util.ITModelUtils;
import mctmods.immersivetechnology.client.models.helper.ITICacheKeyProvider;
import mctmods.immersivetechnology.mixin.accessors.client.SimpleModelAccess;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public record ITMirroredGeometry(UnbakedModel inner) implements IUnbakedGeometry<ITMirroredGeometry> {
    public BakedModel bake(IGeometryBakingContext owner, ModelBaker bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides, ResourceLocation modelLoc) {
        BakedModel baseResult = inner.bake(bakery, spriteGetter, new ITMirroredModelState(modelState), modelLoc);
        if (!(baseResult instanceof SimpleBakedModel simpleModel)) {
            if (baseResult instanceof ITICacheKeyProvider<?> cachedModel) { return new ITCachedMirroredModel<>(cachedModel); }
            throw new RuntimeException("Tried to mirror model " + inner + " which is neither simple nor cacheable");
        }
        SimpleModelAccess access = (SimpleModelAccess) simpleModel;
        List<BakedQuad> unculledQuads = ITMirroredModelLoader.getReversedQuads(simpleModel, null);
        Map<Direction, List<BakedQuad>> culledQuads = new EnumMap<>(Direction.class);
        for (Direction d : DirectionUtils.VALUES) { culledQuads.put(d, ITMirroredModelLoader.getReversedQuads(simpleModel, d)); }
        return new SimpleBakedModel(unculledQuads, culledQuads, baseResult.useAmbientOcclusion(), baseResult.usesBlockLight(), baseResult.isGui3d(), baseResult.getParticleIcon(ModelData.EMPTY), access.getTransformsField(), baseResult.getOverrides(), ITModelUtils.copyTypes(simpleModel), ITModelUtils.copyTypesFast(simpleModel));
    }
}

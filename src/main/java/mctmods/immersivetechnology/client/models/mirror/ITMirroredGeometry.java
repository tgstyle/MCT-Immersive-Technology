package mctmods.immersivetechnology.client.models.mirror;

import blusunrize.immersiveengineering.api.utils.DirectionUtils;
import mctmods.immersivetechnology.client.models.util.ITICacheKeyProvider;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.client.RenderTypeGroup;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public record ITMirroredGeometry(UnbakedModel inner) implements IUnbakedGeometry<ITMirroredGeometry> {
    @Override @Nonnull public BakedModel bake(@NotNull IGeometryBakingContext owner, @NotNull ModelBaker bakery, @NotNull Function<Material, TextureAtlasSprite> spriteGetter, @NotNull ModelState modelState, @NotNull ItemOverrides overrides) {
        BakedModel baseResult = inner.bake(bakery, spriteGetter, new ITMirroredModelState(modelState));
        if(baseResult instanceof SimpleBakedModel simpleModel) {
            List<BakedQuad> unculledQuads = ITMirroredModelLoader.getReversedQuads(simpleModel, null);
            Map<Direction, List<BakedQuad>> culledQuads = new EnumMap<>(Direction.class);
            for(Direction d : DirectionUtils.VALUES) { culledQuads.put(d, ITMirroredModelLoader.getReversedQuads(simpleModel, d)); }
            return new SimpleBakedModel(
                    unculledQuads, culledQuads,
                    baseResult.useAmbientOcclusion(), baseResult.usesBlockLight(), baseResult.isGui3d(),
                    baseResult.getParticleIcon(ModelData.EMPTY), ItemTransforms.NO_TRANSFORMS, baseResult.getOverrides(),
                    RenderTypeGroup.EMPTY
            );
        }
        else if(baseResult instanceof ITICacheKeyProvider<?> cachedModel) { return new ITCachedMirroredModel<>(cachedModel); }
        else { throw new RuntimeException("Tried to mirror model "+inner+" which is neither simple nor cacheable"); }
    }
}

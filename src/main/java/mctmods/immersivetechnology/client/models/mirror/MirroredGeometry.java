package mctmods.immersivetechnology.client.models.mirror;

import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;

import javax.annotation.Nonnull;
import java.util.function.Function;

public record MirroredGeometry(UnbakedModel inner) implements IUnbakedGeometry<MirroredGeometry> {
    @Override @Nonnull public BakedModel bake(@Nonnull IGeometryBakingContext owner, @Nonnull ModelBaker bakery, @Nonnull Function<Material, TextureAtlasSprite> spriteGetter, @Nonnull ModelState modelState, @Nonnull ItemOverrides overrides) {
        BakedModel baseResult = inner.bake(bakery, spriteGetter, new MirroredModelState(modelState));
        return new MirroredBakedModel<>(baseResult);
    }
}

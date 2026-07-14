package mctmods.immersivetechnology.client.models.mirror;

import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.function.Function;

public record ITMirroredGeometry(UnbakedModel inner) implements IUnbakedGeometry<ITMirroredGeometry> {
    @Override @Nonnull public BakedModel bake(@NotNull IGeometryBakingContext owner, @NotNull ModelBaker bakery, @NotNull Function<Material, TextureAtlasSprite> spriteGetter, @NotNull ModelState modelState, @NotNull ItemOverrides overrides) {
        BakedModel baseResult = inner.bake(bakery, spriteGetter, new ITMirroredModelState(modelState));
        return new ITMirroredBakedModel<>(baseResult);
    }
}

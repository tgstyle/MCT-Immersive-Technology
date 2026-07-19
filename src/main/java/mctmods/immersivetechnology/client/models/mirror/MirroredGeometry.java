package mctmods.immersivetechnology.client.models.mirror;

import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;

import java.util.function.Function;

public record MirroredGeometry(UnbakedModel inner) implements IUnbakedGeometry<MirroredGeometry> {
    public BakedModel bake(IGeometryBakingContext owner, ModelBaker bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides, ResourceLocation modelLoc) {
        BakedModel baseResult = inner.bake(bakery, spriteGetter, new MirroredModelState(modelState), modelLoc);
        return new MirroredBakedModel<>(baseResult);
    }
}

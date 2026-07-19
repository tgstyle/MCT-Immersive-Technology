package mctmods.immersivetechnology.client.models.split;

import mctmods.immersivetechnology.client.models.util.ICacheKeyProvider;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public record UnbakedSplitModel(UnbakedModel baseModel, Set<Vec3i> parts, boolean dynamic, @Nonnull Vec3i size) implements IUnbakedGeometry<UnbakedSplitModel> {
    public UnbakedSplitModel(UnbakedModel baseModel, List<Vec3i> parts, boolean dynamic, @Nonnull Vec3i size) {
        this(baseModel, new HashSet<>(parts), dynamic, size);
    }

    @Override public BakedModel bake(IGeometryBakingContext owner, ModelBaker bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform, ItemOverrides overrides, ResourceLocation modelLocation) {
        BakedModel bakedBase = baseModel.bake(bakery, spriteGetter, BlockModelRotation.X0_Y0, modelLocation);
        if (dynamic) {
            return new BakedSplitModel<>((ICacheKeyProvider<?>)bakedBase, parts, modelTransform, size, true, null);
        } else {
            return new BakedSplitModel<>(bakedBase, parts, modelTransform, size, false, owner.getTransforms());
        }
    }
}

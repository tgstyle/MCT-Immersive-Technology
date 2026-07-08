/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package mctmods.immersivetechnology.client.models.split;

import blusunrize.immersiveengineering.api.client.ICacheKeyProvider;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.Vec3i;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class ITUnbakedSplitModel implements IUnbakedGeometry<ITUnbakedSplitModel> {
    private final UnbakedModel baseModel;
    private final Set<Vec3i> parts;
    private final boolean dynamic;
    @Nonnull private final Vec3i size;

    public ITUnbakedSplitModel(UnbakedModel baseModel, List<Vec3i> parts, boolean dynamic, @NotNull Vec3i size) {
        this.baseModel = baseModel;
        this.parts = new HashSet<>(parts);
        this.dynamic = dynamic;
        this.size = size;
    }

    @Override @NotNull public BakedModel bake(@NotNull IGeometryBakingContext owner, @NotNull ModelBaker bakery, @NotNull Function<Material, TextureAtlasSprite> spriteGetter, @NotNull ModelState modelTransform, @NotNull ItemOverrides overrides) {
        BakedModel bakedBase = baseModel.bake(bakery, spriteGetter, BlockModelRotation.X0_Y0);
        if (dynamic) { return new ITBakedDynamicSplitModel<>((ICacheKeyProvider<?>)bakedBase, parts, modelTransform, size); }
        else { return new ITBakedBasicSplitModel(bakedBase, parts, modelTransform, size, owner.getTransforms()); }
    }
}

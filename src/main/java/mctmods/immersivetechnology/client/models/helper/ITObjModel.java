package mctmods.immersivetechnology.client.models.helper;

import com.mojang.math.Transformation;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;
import net.minecraftforge.client.model.obj.ObjModel;
import java.util.Map;
import java.util.function.Function;

public record ITObjModel(ObjModel inner, Map<String, Boolean> defaultVisibility) implements IUnbakedGeometry<ITObjModel> {
    @Override public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides, ResourceLocation modelLocation) {
        IGeometryBakingContext wrappedContext = new IGeometryBakingContext() {
            @Override public String getModelName() { return context.getModelName(); }
            @Override public boolean hasMaterial(String name) { return context.hasMaterial(name); }
            @Override public Material getMaterial(String name) { return context.getMaterial(name); }
            @Override public boolean isComponentVisible(String part, boolean fallback) { return defaultVisibility.getOrDefault(part, context.isComponentVisible(part, fallback)); }
            @Override public boolean isGui3d() { return context.isGui3d(); }
            @Override public boolean useBlockLight() { return context.useBlockLight(); }
            @Override public boolean useAmbientOcclusion() { return context.useAmbientOcclusion(); }
            @Override public Transformation getRootTransform() { return context.getRootTransform(); }
            @Override public ItemTransforms getTransforms() { return context.getTransforms(); }
            @Override public ResourceLocation getRenderTypeHint() { return context.getRenderTypeHint(); }
        };
        return inner.bake(wrappedContext, baker, spriteGetter, modelState, overrides, modelLocation);
    }
}

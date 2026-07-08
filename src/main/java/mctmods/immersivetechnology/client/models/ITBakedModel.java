package mctmods.immersivetechnology.client.models;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.data.ModelData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public abstract class ITBakedModel implements BakedModel {
    @Nonnull @Override public abstract List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull RandomSource rand, @Nonnull ModelData extraData, @Nullable RenderType layer);

    @Override @Nonnull public final List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull RandomSource rand) { return getQuads(state, side, rand, ModelData.EMPTY, null); }

    @Override public boolean useAmbientOcclusion() { return true; }

    @Override public boolean isGui3d() { return true; }

    @Override public boolean usesBlockLight() { return true; }

    @Override public boolean isCustomRenderer() { return false; }

    @Nonnull @Override public TextureAtlasSprite getParticleIcon() { return getParticleIcon(ModelData.EMPTY); }

    @Nonnull @Override public ItemOverrides getOverrides() { return ItemOverrides.EMPTY; }

    @Nonnull @Override public TextureAtlasSprite getParticleIcon(@Nonnull ModelData data) { return net.minecraft.client.Minecraft.getInstance().getTextureAtlas(net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS).apply(net.minecraft.resources.ResourceLocation.withDefaultNamespace("missingno")); }

    @Nonnull @Override public ChunkRenderTypeSet getRenderTypes(@Nonnull BlockState state, @Nonnull RandomSource rand, @Nonnull ModelData data) { return ChunkRenderTypeSet.all(); }
}

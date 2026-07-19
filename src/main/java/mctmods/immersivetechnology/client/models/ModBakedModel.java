package mctmods.immersivetechnology.client.models;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.extensions.IForgeBakedModel;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class ModBakedModel implements BakedModel, IForgeBakedModel {
    private static final ChunkRenderTypeSet SOLID_ONLY = ChunkRenderTypeSet.of(RenderType.solid());

    @NotNull public abstract List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData data, @Nullable RenderType renderType);

    @Override @NotNull public final List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand) { return getQuads(state, side, rand, ModelData.EMPTY, null); }

    @NotNull public TextureAtlasSprite getParticleIcon(@NotNull ModelData data) { return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(MissingTextureAtlasSprite.getLocation()); }

    @Override @NotNull public TextureAtlasSprite getParticleIcon() { return getParticleIcon(ModelData.EMPTY); }

    @Override public boolean useAmbientOcclusion() { return true; }

    @Override public boolean isGui3d() { return true; }

    @Override public boolean usesBlockLight() { return true; }

    @Override public boolean isCustomRenderer() { return false; }

    @Override @NotNull public ItemOverrides getOverrides() { return ItemOverrides.EMPTY; }

    @NotNull public ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand, @NotNull ModelData data) { return SOLID_ONLY; }
}

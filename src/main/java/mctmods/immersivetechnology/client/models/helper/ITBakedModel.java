package mctmods.immersivetechnology.client.models.helper;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.extensions.IForgeBakedModel;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class ITBakedModel implements BakedModel, IForgeBakedModel {
    @NotNull public abstract List<BakedQuad> getQuads(@Nullable BlockState var1, @Nullable Direction var2, @NotNull RandomSource var3, @NotNull ModelData var4, @Nullable RenderType var5);

    @NotNull public final List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand) { return this.getQuads(state, side, rand, ModelData.EMPTY, null); }

    @NotNull public abstract TextureAtlasSprite getParticleIcon(@NotNull ModelData data);

    @Override @NotNull public TextureAtlasSprite getParticleIcon() { return getParticleIcon(ModelData.EMPTY); }

    public boolean useAmbientOcclusion() { return true; }

    public boolean isGui3d() { return true; }

    public boolean usesBlockLight() { return true; }

    public boolean isCustomRenderer() { return false; }

    @NotNull public ItemOverrides getOverrides() { return ItemOverrides.EMPTY; }
}

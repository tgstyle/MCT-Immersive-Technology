package mctmods.immersivetechnology.client.models.helper;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ITCompositeBakedModel<T extends BakedModel> implements BakedModel {

    protected final T base;

    public ITCompositeBakedModel(T base) {
        this.base = base;
    }

    @Nonnull
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull RandomSource rand) { return this.base.getQuads(state, side, rand, ModelData.EMPTY, null); }

    public boolean useAmbientOcclusion() { return this.base.useAmbientOcclusion(); }

    public boolean isGui3d() { return this.base.isGui3d(); }

    public boolean usesBlockLight() { return this.base.usesBlockLight(); }

    public boolean isCustomRenderer() { return this.base.isCustomRenderer(); }

    @Nonnull
    public TextureAtlasSprite getParticleIcon() { return this.base.getParticleIcon(ModelData.EMPTY); }

    @Nonnull
    public ItemOverrides getOverrides() { return this.base.getOverrides(); }

    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData data, @Nullable RenderType renderType) { return this.base.getQuads(state, side, rand, data, renderType); }

    public boolean useAmbientOcclusion(@NotNull BlockState state) { return this.base.useAmbientOcclusion(state); }

    @Nonnull
    public ModelData getModelData(@Nonnull BlockAndTintGetter world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull ModelData tileData) { return this.base.getModelData(world, pos, state, tileData); }

    public @NotNull TextureAtlasSprite getParticleIcon(@Nonnull ModelData data) { return this.base.getParticleIcon(data); }

    public @NotNull List<RenderType> getRenderTypes(@NotNull ItemStack itemStack, boolean fabulous) { return this.base.getRenderTypes(itemStack, fabulous); }

    public @NotNull ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand, @NotNull ModelData data) { return this.base.getRenderTypes(state, rand, data); }
}

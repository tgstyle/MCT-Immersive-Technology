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
import java.util.List;
import javax.annotation.Nullable;

public class ITCompositeBakedModel<T extends BakedModel> implements BakedModel {

    protected final T base;

    public ITCompositeBakedModel(T base) {
        this.base = base;
    }

    @NotNull public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand) { return this.base.getQuads(state, side, rand, ModelData.EMPTY, null); }

    public boolean useAmbientOcclusion() { return this.base.useAmbientOcclusion(); }

    public boolean isGui3d() { return this.base.isGui3d(); }

    public boolean usesBlockLight() { return this.base.usesBlockLight(); }

    public boolean isCustomRenderer() { return this.base.isCustomRenderer(); }

    @NotNull public TextureAtlasSprite getParticleIcon() { return this.base.getParticleIcon(ModelData.EMPTY); }

    @NotNull public ItemOverrides getOverrides() { return this.base.getOverrides(); }

    @NotNull public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData data, @Nullable RenderType renderType) { return this.base.getQuads(state, side, rand, data, renderType); }

    public boolean useAmbientOcclusion(@NotNull BlockState state) { return this.base.useAmbientOcclusion(state); }

    @NotNull public ModelData getModelData(@NotNull BlockAndTintGetter world, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ModelData tileData) { return this.base.getModelData(world, pos, state, tileData); }

    @NotNull public TextureAtlasSprite getParticleIcon(@NotNull ModelData data) { return this.base.getParticleIcon(data); }

    @NotNull public List<RenderType> getRenderTypes(@NotNull ItemStack itemStack, boolean fabulous) { return this.base.getRenderTypes(itemStack, fabulous); }

    @NotNull public ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand, @NotNull ModelData data) { return this.base.getRenderTypes(state, rand, data); }
}

package mctmods.immersivetechnology.client.models.helper;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface ITICacheKeyProvider<K> extends BakedModel {
    List<BakedQuad> getQuads(K var1);

    @Nonnull default List<BakedQuad> getQuads(@Nullable BlockState pState, @Nullable Direction pSide, @Nonnull RandomSource pRand, @Nonnull ModelData extraData, @Nullable RenderType layer) { return this.getQuads(this.getKey(pState, pSide, pRand, extraData, layer)); }

    @Nonnull default List<BakedQuad> getQuads(@Nullable BlockState pState, @Nullable Direction pSide, @Nonnull RandomSource pRand) { return this.getQuads(pState, pSide, pRand, ModelData.EMPTY, null); }

    @Nullable K getKey(@Nullable BlockState var1, @Nullable Direction var2, @Nonnull RandomSource var3, @Nonnull ModelData var4, @Nullable RenderType var5);
}

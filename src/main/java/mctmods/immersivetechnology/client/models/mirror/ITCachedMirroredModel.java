package mctmods.immersivetechnology.client.models.mirror;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import mctmods.immersivetechnology.client.models.util.ITICacheKeyProvider;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ITCachedMirroredModel<K, T extends ITICacheKeyProvider<K>> extends BakedModelWrapper<T> implements ITICacheKeyProvider<K> {
    private final T base;
    private final LoadingCache<K, List<BakedQuad>> cache;

    public ITCachedMirroredModel(T base) {
        super(base);
        this.base = base;
        this.cache = CacheBuilder.newBuilder().expireAfterAccess(120, TimeUnit.SECONDS).build(CacheLoader.from(k -> ITMirroredModelLoader.reversedQuads(base.getQuads(k))));
    }

    @Override public List<BakedQuad> getQuads(K key) {
        if(key != null) { return cache.getUnchecked(key); }
        else { return List.of(); }
    }

    @Override @Nullable public K getKey(@Nullable BlockState state, @Nullable Direction side, @Nonnull RandomSource rand, @Nonnull ModelData extraData, @Nullable RenderType layer) {
        return base.getKey(state, side, rand, extraData, layer);
    }

    @Override @Nonnull public List<BakedQuad> getQuads(@Nullable BlockState pState, @Nullable Direction pSide, @Nonnull RandomSource pRand, @Nonnull ModelData extraData, @Nullable RenderType layer) {
        return ITICacheKeyProvider.super.getQuads(pState, pSide, pRand, extraData, layer);
    }
}

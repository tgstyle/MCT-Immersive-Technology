package mctmods.immersivetechnology.client.models.mirror;

import blusunrize.immersiveengineering.api.utils.DirectionUtils;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import mctmods.immersivetechnology.client.models.util.ICacheKeyProvider;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.data.ModelData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MirroredBakedModel<T extends BakedModel> extends BakedModelWrapper<T> {
    private final boolean isDynamic;
    private final List<BakedQuad> unculledQuads;
    private final Map<Direction, List<BakedQuad>> culledQuads;
    private final LoadingCache<Object, List<BakedQuad>> quadCache;
    private final ICacheKeyProvider<Object> keyProvider;

    public MirroredBakedModel(T base) {
        super(base);
        this.isDynamic = base instanceof ICacheKeyProvider;
        if (isDynamic) {
            @SuppressWarnings("unchecked") ICacheKeyProvider<Object> kp = (ICacheKeyProvider<Object>) base;
            this.keyProvider = kp;
            this.quadCache = CacheBuilder.newBuilder().maximumSize(1024).expireAfterAccess(10, TimeUnit.MINUTES).build(CacheLoader.from(k -> MirroredModelLoader.reversedQuads(kp.getQuads(k))));
            this.unculledQuads = null;
            this.culledQuads = null;
        } else if (base instanceof SimpleBakedModel simple) {
            this.unculledQuads = MirroredModelLoader.getReversedQuads(simple, null);
            EnumMap<Direction, List<BakedQuad>> map = new EnumMap<>(Direction.class);
            for (Direction d : DirectionUtils.VALUES) { map.put(d, MirroredModelLoader.getReversedQuads(simple, d)); }
            this.culledQuads = ImmutableMap.copyOf(map);
            this.quadCache = null;
            this.keyProvider = null;
        } else { throw new RuntimeException("Tried to mirror model which is neither simple nor cacheable: " + base); }
    }

    @Override @Nonnull public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull RandomSource rand, @Nonnull ModelData data, @Nullable RenderType renderType) {
        if (isDynamic) {
            Object key = keyProvider.getKey(state, side, rand, data, renderType);
            if (key == null) { return ImmutableList.of(); }
            return quadCache.getUnchecked(key);
        } else { return side == null ? unculledQuads : culledQuads.getOrDefault(side, ImmutableList.of()); }
    }
}

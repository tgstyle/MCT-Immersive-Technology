package mctmods.immersivetechnology.client.models.split;

import blusunrize.immersiveengineering.api.IEApi;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import mctmods.immersivetechnology.client.models.helper.ITICacheKeyProvider;
import mctmods.immersivetechnology.common.blocks.helper.ITProperties;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;

public class ITBakedDynamicSplitModel<K, T extends ITICacheKeyProvider<K> & BakedModel> extends ITAbstractSplitModel<T> {
    private static final Set<ITBakedDynamicSplitModel<?, ?>> WEAK_INSTANCES = Collections.newSetFromMap(new WeakHashMap<>());

    static {
        IEApi.renderCacheClearers.add(() -> WEAK_INSTANCES.forEach(m -> m.subModelCache.invalidateAll()));
    }

    private final LoadingCache<K, Map<Vec3i, List<BakedQuad>>> subModelCache;

    public ITBakedDynamicSplitModel(T base, Set<Vec3i> parts, ModelState transform, @Nonnull Vec3i size) {
        super(base, size);
        this.subModelCache = CacheBuilder.newBuilder()
                .maximumSize(10)
                .expireAfterAccess(1, TimeUnit.MINUTES)
                .build(CacheLoader.from(key -> {
                    List<BakedQuad> baseQuads = base.getQuads(key);
                    return split(baseQuads, parts, transform);
                }));
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData data, @Nullable RenderType renderType) {
        BlockPos offset = data.get(ITProperties.Model.SUBMODEL_OFFSET);
        if (offset == null) { return super.getQuads(state, side, rand, data, renderType); }
        K key = base.getKey(state, side, rand, data, renderType);
        if (key == null) { return ImmutableList.of(); }
        return subModelCache.getUnchecked(key).getOrDefault(offset, ImmutableList.of());
    }
}

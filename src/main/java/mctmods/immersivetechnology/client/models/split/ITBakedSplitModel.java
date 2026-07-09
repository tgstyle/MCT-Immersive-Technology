package mctmods.immersivetechnology.client.models.split;

import mctmods.immersivetechnology.client.models.ITICacheKeyProvider;
import mctmods.immersivetechnology.common.blocks.helper.ITProperties;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.utils.ResettableLazy;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class ITBakedSplitModel<T extends BakedModel> extends ITAbstractSplitModel<T> {
    private final boolean dynamic;
    private final ResettableLazy<Map<Vec3i, List<BakedQuad>>> splitModels;
    private final LoadingCache<Object, Map<Vec3i, List<BakedQuad>>> subModelCache;
    private final ItemTransforms itemTransforms;
    private final ITICacheKeyProvider<Object> keyProvider;

    public ITBakedSplitModel(T base, Set<Vec3i> parts, ModelState transform, Vec3i size, boolean dynamic, @Nullable ItemTransforms itemTransforms) {
        super(base, size);
        this.dynamic = dynamic;
        this.itemTransforms = itemTransforms;
        if (dynamic) {
            if (!(base instanceof ITICacheKeyProvider)) {
                throw new RuntimeException("Dynamic split model requires the inner model to implement ITICacheKeyProvider");
            }
            @SuppressWarnings("unchecked") ITICacheKeyProvider<Object> kp = (ITICacheKeyProvider<Object>) base;
            this.keyProvider = kp;
            this.subModelCache = CacheBuilder.newBuilder().maximumSize(1024).expireAfterAccess(10, TimeUnit.MINUTES).build(CacheLoader.from(key -> {
                List<BakedQuad> baseQuads = this.keyProvider.getQuads(key);
                return split(baseQuads, parts, transform);
            }));
            this.splitModels = null;
        }
        else {
            this.splitModels = new ResettableLazy<>(() -> {
                List<BakedQuad> quads = base.getQuads(null, null, ApiUtils.RANDOM_SOURCE, ModelData.EMPTY, null);
                return split(quads, parts, transform);
            });
            this.subModelCache = null;
            this.keyProvider = null;
        }
    }

    @Override @SuppressWarnings("ConstantConditions") @Nonnull public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull RandomSource rand, @Nonnull ModelData data, @Nullable RenderType renderType) {
        BlockPos offset = data.get(ITProperties.Model.SUBMODEL_OFFSET);
        if (offset == null) {
            if (state == null) { return super.getQuads(state, side, rand, data, renderType); }
            return ImmutableList.of();
        }
        if (side != null) { return ImmutableList.of(); }
        if (dynamic) {
            Object key = this.keyProvider.getKey(state, side, rand, data, renderType);
            if (key == null) { return ImmutableList.of(); }
            return subModelCache.getUnchecked(key).getOrDefault(offset, ImmutableList.of());
        }
        else { return splitModels.get().getOrDefault(offset, ImmutableList.of()); }
    }

    @Override @Nonnull public ItemTransforms getTransforms() { return dynamic ? super.getTransforms() : itemTransforms; }

    @Override protected void clearCache() {
        if (dynamic) { subModelCache.invalidateAll(); }
        else { splitModels.reset(); }
    }
}

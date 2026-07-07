package mctmods.immersivetechnology.client.models.split;

import mctmods.immersivetechnology.common.blocks.helper.ITIModelOffsetProvider;
import mctmods.immersivetechnology.common.blocks.helper.ITProperties;
import mctmods.immersivetechnology.client.models.split.geometry.ITPolygon;
import mctmods.immersivetechnology.client.models.split.geometry.ITPolygonUtils;
import mctmods.immersivetechnology.client.models.split.geometry.ITSplitObjModel;
import mctmods.immersivetechnology.client.models.split.geometry.ITSplitModel;
import mctmods.immersivetechnology.client.models.split.geometry.ITClumpedModel;
import mctmods.immersivetechnology.client.models.split.geometry.ITModelSplitterVec3i;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

public abstract class ITAbstractSplitModel<T extends BakedModel> extends BakedModelWrapper<T> {
    @Nonnull private final Vec3i size;

    private static final Set<ITAbstractSplitModel<?>> WEAK_INSTANCES = Collections.newSetFromMap(new WeakHashMap<>());

    static {
        blusunrize.immersiveengineering.api.IEApi.renderCacheClearers.add(() -> WEAK_INSTANCES.forEach(ITAbstractSplitModel::clearCache));
    }

    protected ITAbstractSplitModel(T base, @NotNull Vec3i size) {
        super(base);
        this.size = size;
        WEAK_INSTANCES.add(this);
    }

    @Override @Nonnull public ModelData getModelData(@Nonnull BlockAndTintGetter world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull ModelData tileData) {
        ModelData baseData = super.getModelData(world, pos, state, tileData);
        BlockEntity te = world.getBlockEntity(pos);
        BlockPos offset = null;
        if (te instanceof ITIModelOffsetProvider offsetProvider) {
            offset = offsetProvider.getModelOffset(state, size);
        } else if (state.getBlock() instanceof ITIModelOffsetProvider offsetProvider) {
            offset = offsetProvider.getModelOffset(state, size);
        }
        if (offset != null) {
            return baseData.derive().with(ITProperties.Model.SUBMODEL_OFFSET, offset).build();
        } else {
            return baseData;
        }
    }

    protected Map<Vec3i, List<BakedQuad>> split(List<BakedQuad> in, Set<Vec3i> parts, ModelState transform) {
        List<ITPolygon<ITPolygonUtils.ExtraQuadData>> polys = in.stream().map(ITPolygonUtils::toPolygon).collect(Collectors.toList());
        ITSplitObjModel<ITPolygonUtils.ExtraQuadData> objModel = new ITSplitObjModel<>(polys);
        ITSplitModel<ITPolygonUtils.ExtraQuadData> splitData = new ITSplitModel<>(objModel);
        Set<ITModelSplitterVec3i> partsBMS = parts.stream().map(v -> new ITModelSplitterVec3i(v.getX(), v.getY(), v.getZ())).collect(Collectors.toSet());
        ITClumpedModel<ITPolygonUtils.ExtraQuadData> clumpedModel = new ITClumpedModel<>(splitData, partsBMS);

        Map<Vec3i, List<BakedQuad>> map = new HashMap<>();
        for (var e : clumpedModel.getClumpedParts().entrySet()) {
            List<BakedQuad> subModelFaces = new ArrayList<>(e.getValue().getFaces().size());
            for (ITPolygon<ITPolygonUtils.ExtraQuadData> p : e.getValue().getFaces()) {
                subModelFaces.add(ITPolygonUtils.toBakedQuad(p.getPoints(), p.getTexture(), transform.getRotation().blockCenterToCorner(), true));
            }
            Vec3i mcKey = new Vec3i(e.getKey().x(), e.getKey().y(), e.getKey().z());
            map.put(mcKey, subModelFaces);
        }
        return map;
    }

    protected abstract void clearCache();
}

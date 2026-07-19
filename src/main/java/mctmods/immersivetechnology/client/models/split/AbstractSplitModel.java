package mctmods.immersivetechnology.client.models.split;

import mctmods.immersivetechnology.common.blocks.helper.IModelOffsetProvider;
import mctmods.immersivetechnology.common.blocks.helper.ModProperties;
import mctmods.immersivetechnology.client.models.split.geometry.Polygon;
import mctmods.immersivetechnology.client.models.split.geometry.PolygonUtils;
import mctmods.immersivetechnology.client.models.split.geometry.SplitObjModel;
import mctmods.immersivetechnology.client.models.split.geometry.SplitModel;
import mctmods.immersivetechnology.client.models.split.geometry.ClumpedModel;
import mctmods.immersivetechnology.client.models.split.geometry.ModelSplitterVec3i;

import net.minecraft.client.renderer.RenderType;
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
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractSplitModel<T extends BakedModel> extends BakedModelWrapper<T> {
    @Nonnull private final Vec3i size;

    private static final Set<AbstractSplitModel<?>> WEAK_INSTANCES = Collections.newSetFromMap(new WeakHashMap<>());

    static {
        blusunrize.immersiveengineering.api.IEApi.renderCacheClearers.add(() -> WEAK_INSTANCES.forEach(AbstractSplitModel::clearCache));
    }

    protected AbstractSplitModel(T base, @NotNull Vec3i size) {
        super(base);
        this.size = size;
        WEAK_INSTANCES.add(this);
    }

    @Override public boolean useAmbientOcclusion() { return false; }

    @Override public boolean useAmbientOcclusion(@Nonnull BlockState state) { return false; }

    @Override public boolean useAmbientOcclusion(@Nonnull BlockState state, @Nullable RenderType renderType) { return false; }

    @Override @Nonnull public ModelData getModelData(@Nonnull BlockAndTintGetter world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull ModelData tileData) {
        ModelData baseData = super.getModelData(world, pos, state, tileData);
        BlockEntity te = world.getBlockEntity(pos);
        BlockPos offset = null;
        if (te instanceof IModelOffsetProvider offsetProvider) { offset = offsetProvider.getModelOffset(state, size); }
        else if (state.getBlock() instanceof IModelOffsetProvider offsetProvider) { offset = offsetProvider.getModelOffset(state, size); }
        if (offset != null) { return baseData.derive().with(ModProperties.Model.SUBMODEL_OFFSET, offset).build(); }
        else { return baseData; }
    }

    protected Map<Vec3i, List<BakedQuad>> split(List<BakedQuad> in, Set<Vec3i> parts, ModelState transform) {
        List<Polygon<PolygonUtils.ExtraQuadData>> polys = in.stream().map(PolygonUtils::toPolygon).collect(Collectors.toList());
        SplitObjModel<PolygonUtils.ExtraQuadData> objModel = new SplitObjModel<>(polys);
        SplitModel<PolygonUtils.ExtraQuadData> splitData = new SplitModel<>(objModel);
        Set<ModelSplitterVec3i> partsBMS = parts.stream().map(v -> new ModelSplitterVec3i(v.getX(), v.getY(), v.getZ())).collect(Collectors.toSet());
        ClumpedModel<PolygonUtils.ExtraQuadData> clumpedModel = new ClumpedModel<>(splitData, partsBMS);

        Map<Vec3i, List<BakedQuad>> map = new HashMap<>();
        for (var e : clumpedModel.getClumpedParts().entrySet()) {
            List<BakedQuad> subModelFaces = new ArrayList<>(e.getValue().getFaces().size());
            for (Polygon<PolygonUtils.ExtraQuadData> p : e.getValue().getFaces()) {
                subModelFaces.add(PolygonUtils.toBakedQuad(p.getPoints(), p.getTexture(), transform.getRotation().blockCenterToCorner(), true));
            }
            Vec3i mcKey = new Vec3i(e.getKey().x(), e.getKey().y(), e.getKey().z());
            map.put(mcKey, subModelFaces);
        }
        return map;
    }

    protected abstract void clearCache();
}

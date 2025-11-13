package mctmods.immersivetechnology.client.models.split;

import blusunrize.immersiveengineering.api.client.IModelOffsetProvider;
import malte0811.modelsplitter.ClumpedModel;
import malte0811.modelsplitter.SplitModel;
import malte0811.modelsplitter.math.ModelSplitterVec3i;
import malte0811.modelsplitter.model.OBJModel;
import malte0811.modelsplitter.model.Polygon;
import mctmods.immersivetechnology.client.models.helper.ITCompositeBakedModel;
import mctmods.immersivetechnology.common.blocks.helper.ITProperties;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

public abstract class ITAbstractSplitModel<T extends BakedModel> extends ITCompositeBakedModel<T> {
    @Nonnull
    private final Vec3i size;

    public ITAbstractSplitModel(T base, @NotNull Vec3i size) {
        super(base);
        this.size = size;
    }

    @Nonnull
    @Override
    public ModelData getModelData(@Nonnull BlockAndTintGetter world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull ModelData tileData) {
        ModelData baseData = super.getModelData(world, pos, state, tileData);
        BlockEntity te = world.getBlockEntity(pos);
        BlockPos offset = null;
        if (te instanceof IModelOffsetProvider offsetProvider) { offset = offsetProvider.getModelOffset(state, size); }
        else if (state.getBlock() instanceof IModelOffsetProvider offsetProvider) { offset = offsetProvider.getModelOffset(state, size); }
        if (offset != null) { return baseData.derive().with(ITProperties.Model.SUBMODEL_OFFSET, offset).build(); }
        else { return baseData; }
    }

    protected Map<Vec3i, List<BakedQuad>> split(List<BakedQuad> in, Set<Vec3i> parts, ModelState transform) {
        List<Polygon<ITPolygonUtils.ExtraQuadData>> polys = in.stream()
                .map(ITPolygonUtils::toPolygon)
                .collect(Collectors.toList());
        SplitModel<ITPolygonUtils.ExtraQuadData> splitData = new SplitModel<>(new OBJModel<>(polys));
        Set<ModelSplitterVec3i> partsBMS = parts.stream()
                .map(v -> new ModelSplitterVec3i(v.getX(), v.getY(), v.getZ()))
                .collect(Collectors.toSet());
        ClumpedModel<ITPolygonUtils.ExtraQuadData> clumpedModel = new ClumpedModel<>(splitData, partsBMS);

        Map<Vec3i, List<BakedQuad>> map = new HashMap<>();
        for (Map.Entry<ModelSplitterVec3i, OBJModel<ITPolygonUtils.ExtraQuadData>> e : clumpedModel.getClumpedParts().entrySet()) {
            List<BakedQuad> subModelFaces = new ArrayList<>(e.getValue().getFaces().size());
            for (Polygon<ITPolygonUtils.ExtraQuadData> p : e.getValue().getFaces()) { subModelFaces.add(ITPolygonUtils.toBakedQuad(p, transform)); }
            Vec3i mcKey = new Vec3i(e.getKey().x(), e.getKey().y(), e.getKey().z());
            map.put(mcKey, subModelFaces);
        }
        return map;
    }
}

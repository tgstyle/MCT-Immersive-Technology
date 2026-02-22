package mctmods.immersivetechnology.client.models.split.geometry;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class ITSplitModel<Texture> {
    private static final ITEpsilonMath EPS_MATH = ITEpsilonMath.DEFAULT;
    private final Map<ITModelSplitterVec3i, ITSplitObjModel<Texture>> submodels;

    public ITSplitModel(ITSplitObjModel<Texture> input) {
        ImmutableMap.Builder<ITModelSplitterVec3i, ITSplitObjModel<Texture>> builder = ImmutableMap.builder();
        for (var xSlice : splitInPlanes(input, 0).entrySet()) {
            for (var zColumn : splitInPlanes(xSlice.getValue(), 2).entrySet()) {
                for (var yDice : splitInPlanes(zColumn.getValue(), 1).entrySet()) {
                    builder.put(new ITModelSplitterVec3i(xSlice.getKey(), yDice.getKey(), zColumn.getKey()), yDice.getValue());
                }
            }
        }
        this.submodels = builder.build();
    }

    public Map<ITModelSplitterVec3i, ITSplitObjModel<Texture>> getParts() {
        return this.submodels;
    }

    private static <Texture> Map<Integer, ITSplitObjModel<Texture>> splitInPlanes(ITSplitObjModel<Texture> input, int axis) {
        if (input.isEmpty()) {
            return Map.of();
        }
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        for (ITPolygon<Texture> f : input.getFaces()) {
            for (ITVertex v : f.getPoints()) {
                double pos = v.position().get(axis);
                min = Math.min(min, pos);
                max = Math.max(max, pos);
            }
        }
        int firstBorder = EPS_MATH.ceil(min);
        int lastBorder = EPS_MATH.floor(max);
        Map<Integer, ITSplitObjModel<Texture>> modelPerSection = new java.util.LinkedHashMap<>();
        double[] vecData = new double[3];
        vecData[axis] = 1.0;
        ITVec3d normal = new ITVec3d(vecData);
        for (int borderPos = firstBorder; borderPos <= lastBorder; ++borderPos) {
            ITPlane cut = new ITPlane(normal, borderPos);
            Map<ITEpsilonMath.Sign, ITSplitObjModel<Texture>> splitModel = input.split(cut);
            ITSplitObjModel<Texture> sectionModel = splitModel.get(ITEpsilonMath.Sign.NEGATIVE);
            putModel(modelPerSection, axis, borderPos - 1, sectionModel);
            input = ITSplitObjModel.union(splitModel.get(ITEpsilonMath.Sign.POSITIVE), splitModel.get(ITEpsilonMath.Sign.ZERO));
        }
        putModel(modelPerSection, axis, lastBorder, input);
        return modelPerSection;
    }

    private static <Texture> void putModel(Map<Integer, ITSplitObjModel<Texture>> sectionModels, int axis, int section, ITSplitObjModel<Texture> baseSectionModel) {
        if (baseSectionModel != null && !baseSectionModel.isEmpty()) {
            sectionModels.put(section, baseSectionModel.translate(axis, -section).quadify());
        }
    }
}

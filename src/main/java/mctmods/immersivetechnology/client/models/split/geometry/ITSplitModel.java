package mctmods.immersivetechnology.client.models.split.geometry;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class ITSplitModel<Texture> {
    private static final ITEpsilonMath EPS_MATH = ITEpsilonMath.DEFAULT;
    private final Map<ITModelSplitterVec3i, ITSplitObjModel<Texture>> submodels;

    public ITSplitModel(ITSplitObjModel<Texture> input) {
        ImmutableMap.Builder<ITModelSplitterVec3i, ITSplitObjModel<Texture>> builder = ImmutableMap.builder();
        for (var xSlice : splitInPlanes(input, Axis.X).entrySet()) {
            for (var zColumn : splitInPlanes(xSlice.getValue(), Axis.Z).entrySet()) {
                for (var yDice : splitInPlanes(zColumn.getValue(), Axis.Y).entrySet()) {
                    builder.put(new ITModelSplitterVec3i(xSlice.getKey(), yDice.getKey(), zColumn.getKey()), yDice.getValue());
                }
            }
        }
        this.submodels = builder.build();
    }

    public Map<ITModelSplitterVec3i, ITSplitObjModel<Texture>> getParts() {
        return this.submodels;
    }

    private static <Texture> Map<Integer, ITSplitObjModel<Texture>> splitInPlanes(ITSplitObjModel<Texture> input, Axis axis) {
        if (input.isEmpty()) {
            return Map.of();
        }

        double min = axis.getMin(input);
        double max = axis.getMax(input);
        if (max - min < 1.0) {
            Map<Integer, ITSplitObjModel<Texture>> result = new java.util.LinkedHashMap<>();
            putModel(result, axis, EPS_MATH.floor(min), input);
            return result;
        }

        int firstBorder = EPS_MATH.ceil(min);
        int lastBorder = EPS_MATH.floor(max);
        Map<Integer, ITSplitObjModel<Texture>> modelPerSection = new java.util.LinkedHashMap<>(lastBorder - firstBorder + 2);
        for (int borderPos = firstBorder; borderPos <= lastBorder; ++borderPos) {
            ITPlane cut = new ITPlane(axis.getNormal(), borderPos);
            Map<ITEpsilonMath.Sign, ITSplitObjModel<Texture>> splitModel = input.split(cut);
            ITSplitObjModel<Texture> sectionModel = splitModel.get(ITEpsilonMath.Sign.NEGATIVE);
            putModel(modelPerSection, axis, borderPos - 1, sectionModel);
            input = ITSplitObjModel.union(splitModel.get(ITEpsilonMath.Sign.POSITIVE), splitModel.get(ITEpsilonMath.Sign.ZERO));
        }
        putModel(modelPerSection, axis, lastBorder, input);
        return modelPerSection;
    }

    private static <Texture> void putModel(Map<Integer, ITSplitObjModel<Texture>> sectionModels, Axis axis, int section, ITSplitObjModel<Texture> baseSectionModel) {
        if (baseSectionModel != null && !baseSectionModel.isEmpty()) {
            sectionModels.put(section, baseSectionModel.translate(axis.ordinal(), -section).quadify());
        }
    }

    private enum Axis {
        X(0), Y(1), Z(2);
        private final int idx;
        Axis(int idx) { this.idx = idx; }
        public ITVec3d getNormal() {
            double[] data = new double[3];
            data[idx] = 1.0;
            return new ITVec3d(data);
        }
        public double getMin(ITSplitObjModel<?> m) {
            return switch (this) {
                case X -> m.getMinX();
                case Y -> m.getMinY();
                case Z -> m.getMinZ();
            };
        }
        public double getMax(ITSplitObjModel<?> m) {
            return switch (this) {
                case X -> m.getMaxX();
                case Y -> m.getMaxY();
                case Z -> m.getMaxZ();
            };
        }
    }
}

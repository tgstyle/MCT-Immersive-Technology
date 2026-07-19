package mctmods.immersivetechnology.client.models.split.geometry;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class SplitModel<Texture> {
    private static final EpsilonMath EPS_MATH = EpsilonMath.DEFAULT;
    private final Map<ModelSplitterVec3i, SplitObjModel<Texture>> submodels;

    public SplitModel(SplitObjModel<Texture> input) {
        ImmutableMap.Builder<ModelSplitterVec3i, SplitObjModel<Texture>> builder = ImmutableMap.builder();
        for (var xSlice : splitInPlanes(input, Axis.X).entrySet()) {
            for (var zColumn : splitInPlanes(xSlice.getValue(), Axis.Z).entrySet()) {
                for (var yDice : splitInPlanes(zColumn.getValue(), Axis.Y).entrySet()) { builder.put(new ModelSplitterVec3i(xSlice.getKey(), yDice.getKey(), zColumn.getKey()), yDice.getValue()); }
            }
        }
        this.submodels = builder.build();
    }

    public Map<ModelSplitterVec3i, SplitObjModel<Texture>> getParts() {
        return this.submodels;
    }

    private static <Texture> Map<Integer, SplitObjModel<Texture>> splitInPlanes(SplitObjModel<Texture> input, Axis axis) {
        if (input.isEmpty()) { return Map.of(); }

        double min = axis.getMin(input);
        double max = axis.getMax(input);
        if (max - min < 1.0) {
            Map<Integer, SplitObjModel<Texture>> result = new java.util.LinkedHashMap<>();
            putModel(result, axis, EPS_MATH.floor(min), input);
            return result;
        }

        int firstBorder = EPS_MATH.ceil(min);
        int lastBorder = EPS_MATH.floor(max);
        Map<Integer, SplitObjModel<Texture>> modelPerSection = new java.util.LinkedHashMap<>(lastBorder - firstBorder + 2);
        for (int borderPos = firstBorder; borderPos <= lastBorder; ++borderPos) {
            ModPlane cut = new ModPlane(axis.getNormal(), borderPos);
            Map<EpsilonMath.Sign, SplitObjModel<Texture>> splitModel = input.split(cut);
            SplitObjModel<Texture> sectionModel = splitModel.get(EpsilonMath.Sign.NEGATIVE);
            putModel(modelPerSection, axis, borderPos - 1, sectionModel);
            input = SplitObjModel.union(splitModel.get(EpsilonMath.Sign.POSITIVE), splitModel.get(EpsilonMath.Sign.ZERO));
        }
        putModel(modelPerSection, axis, lastBorder, input);
        return modelPerSection;
    }

    private static <Texture> void putModel(Map<Integer, SplitObjModel<Texture>> sectionModels, Axis axis, int section, SplitObjModel<Texture> baseSectionModel) { if (baseSectionModel != null && !baseSectionModel.isEmpty()) { sectionModels.put(section, baseSectionModel.translate(axis.ordinal(), -section).quadify()); } }

    private enum Axis {
        X(0), Y(1), Z(2);
        private final int idx;
        Axis(int idx) { this.idx = idx; }
        public Vec3d getNormal() {
            double[] data = new double[3];
            data[idx] = 1.0;
            return new Vec3d(data);
        }
        public double getMin(SplitObjModel<?> m) {
            return switch (this) {
                case X -> m.getMinX();
                case Y -> m.getMinY();
                case Z -> m.getMinZ();
            };
        }
        public double getMax(SplitObjModel<?> m) {
            return switch (this) {
                case X -> m.getMaxX();
                case Y -> m.getMaxY();
                case Z -> m.getMaxZ();
            };
        }
    }
}

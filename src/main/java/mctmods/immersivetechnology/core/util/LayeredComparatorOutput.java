package mctmods.immersivetechnology.core.util;

import net.minecraft.util.Mth;

import java.util.function.ObjIntConsumer;

public class LayeredComparatorOutput<CTX> {
    private final double maxValue;
    private final int numLayers;
    private final double layerSize;
    private final ObjIntConsumer<CTX> updateMaster;
    private final LayerUpdater<CTX> updateLayer;
    private double lastValue = -1;
    private int currentMasterOutput;
    private final int[] currentLayerOutputs;

    public LayeredComparatorOutput(double maxValue, int numLayers, ObjIntConsumer<CTX> updateMaster, LayerUpdater<CTX> updateLayer) {
        this.maxValue = maxValue;
        this.numLayers = numLayers;
        this.updateMaster = updateMaster;
        this.updateLayer = updateLayer;
        this.currentMasterOutput = 0;
        this.currentLayerOutputs = new int[numLayers];
        this.layerSize = maxValue / numLayers;
    }

    public void update(CTX ctx, double newValue) {
        if (newValue == lastValue) { return; }
        lastValue = newValue;
        int newMasterOutput = (int) ((15 * newValue) / maxValue);
        if (currentMasterOutput != newMasterOutput) {
            currentMasterOutput = newMasterOutput;
            updateMaster.accept(ctx, newMasterOutput);
        }
        for (int layer = 0; layer < numLayers; ++layer) {
            double layerValue = newValue - layer * layerSize;
            int newLayerOutput = (int) Mth.clamp((15 * layerValue) / layerSize, 0, 15);
            if (newLayerOutput != currentLayerOutputs[layer]) {
                currentLayerOutputs[layer] = newLayerOutput;
                updateLayer.update(ctx, layer, newLayerOutput);
            }
        }
    }

    @FunctionalInterface public interface LayerUpdater<CTX> { void update(CTX ctx, int layer, int value); }
}

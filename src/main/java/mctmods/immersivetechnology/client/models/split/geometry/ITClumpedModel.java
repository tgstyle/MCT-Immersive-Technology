package mctmods.immersivetechnology.client.models.split.geometry;

import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ITClumpedModel<Texture> {
    private final Map<ITModelSplitterVec3i, ITSplitObjModel<Texture>> clumpedParts;

    public ITClumpedModel(ITSplitModel<Texture> splitModel, Set<ITModelSplitterVec3i> parts) {
        Map<ITModelSplitterVec3i, ITSplitObjModel<Texture>> clumped = new HashMap<>();
        for (var entry : splitModel.getParts().entrySet()) {
            ITModelSplitterVec3i originalTarget = entry.getKey();
            ITModelSplitterVec3i target = originalTarget;
            ITSplitObjModel<Texture> translatedModel = entry.getValue();
            if (!parts.contains(originalTarget)) {
                int optDist = Integer.MAX_VALUE;
                for (ITModelSplitterVec3i candidate : parts) {
                    int currentDist = candidate.distanceSq(originalTarget);
                    if (currentDist < optDist) {
                        optDist = currentDist;
                        target = candidate;
                    }
                }
                ITModelSplitterVec3i translatedBy = originalTarget.subtract(target);
                translatedModel = translatedModel.translate(new ITVec3d(translatedBy));
            }
            clumped.merge(target, translatedModel, ITSplitObjModel::union);
        }
        this.clumpedParts = ImmutableMap.copyOf(clumped);
    }

    public Map<ITModelSplitterVec3i, ITSplitObjModel<Texture>> getClumpedParts() {
        return this.clumpedParts;
    }
}

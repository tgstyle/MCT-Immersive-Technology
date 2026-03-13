package mctmods.immersivetechnology.client.models.split.geometry;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ITClumpedModel<Texture> {
    private final Map<ITModelSplitterVec3i, ITSplitObjModel<Texture>> clumpedParts;

    public ITClumpedModel(ITSplitModel<Texture> splitModel, Set<ITModelSplitterVec3i> parts) {
        Preconditions.checkArgument(!parts.isEmpty());
        Map<ITModelSplitterVec3i, ITSplitObjModel<Texture>> clumpedParts = new HashMap<>();
        for (Map.Entry<ITModelSplitterVec3i, ITSplitObjModel<Texture>> splitPart : splitModel.getParts().entrySet()) {
            final ITModelSplitterVec3i originalTarget = splitPart.getKey();
            ITModelSplitterVec3i target = originalTarget;
            ITSplitObjModel<Texture> translatedModel = splitPart.getValue();
            if (!parts.contains(target)) {
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
            clumpedParts.merge(target, translatedModel, ITSplitObjModel::union);
        }
        this.clumpedParts = ImmutableMap.copyOf(clumpedParts);
    }

    public Map<ITModelSplitterVec3i, ITSplitObjModel<Texture>> getClumpedParts() {
        return this.clumpedParts;
    }
}

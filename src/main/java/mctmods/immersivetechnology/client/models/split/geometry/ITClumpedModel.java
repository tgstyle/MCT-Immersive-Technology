package mctmods.immersivetechnology.client.models.split.geometry;

import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ITClumpedModel<Texture> {
    private final Map<ITModelSplitterVec3i, ITSplitObjModel<Texture>> clumpedParts;

    public ITClumpedModel(ITSplitModel<Texture> splitModel, Set<ITModelSplitterVec3i> parts) {
        Map<ITModelSplitterVec3i, ITSplitObjModel<Texture>> clumped = new HashMap<>(parts.size());
        for (var entry : splitModel.getParts().entrySet()) {
            ITModelSplitterVec3i originalTarget = entry.getKey();
            ITSplitObjModel<Texture> translatedModel = entry.getValue();
            ITModelSplitterVec3i target = computeBestTarget(translatedModel, parts, originalTarget);
            if (!target.equals(originalTarget)) {
                ITModelSplitterVec3i translatedBy = originalTarget.subtract(target);
                translatedModel = translatedModel.translate(new ITVec3d(translatedBy));
            }
            clumped.merge(target, translatedModel, ITSplitObjModel::union);
        }
        this.clumpedParts = ImmutableMap.copyOf(clumped);
    }

    private ITModelSplitterVec3i computeBestTarget(ITSplitObjModel<Texture> model, Set<ITModelSplitterVec3i> validParts, ITModelSplitterVec3i fallback) {
        if (model.isEmpty() || validParts.isEmpty()) {
            return fallback;
        }
        ITModelSplitterVec3i centerKey = new ITModelSplitterVec3i(
                model.getCenterX(),
                model.getCenterY(),
                model.getCenterZ()
        );
        if (validParts.contains(centerKey)) {
            return centerKey;
        }
        int bestDist = Integer.MAX_VALUE;
        ITModelSplitterVec3i best = fallback;
        for (ITModelSplitterVec3i candidate : validParts) {
            int dist = candidate.distanceSq(centerKey);
            if (dist < bestDist) {
                bestDist = dist;
                best = candidate;
            }
        }
        return best;
    }

    public Map<ITModelSplitterVec3i, ITSplitObjModel<Texture>> getClumpedParts() {
        return this.clumpedParts;
    }
}

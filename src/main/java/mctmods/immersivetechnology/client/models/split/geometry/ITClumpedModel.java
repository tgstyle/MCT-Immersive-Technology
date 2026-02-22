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
        double minX = Double.POSITIVE_INFINITY, maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;
        double minZ = Double.POSITIVE_INFINITY, maxZ = Double.NEGATIVE_INFINITY;
        for (ITPolygon<Texture> p : model.getFaces()) {
            for (ITVertex v : p.getPoints()) {
                ITVec3d pos = v.position();
                minX = Math.min(minX, pos.x());
                maxX = Math.max(maxX, pos.x());
                minY = Math.min(minY, pos.y());
                maxY = Math.max(maxY, pos.y());
                minZ = Math.min(minZ, pos.z());
                maxZ = Math.max(maxZ, pos.z());
            }
        }
        int cx = (int) Math.round((minX + maxX) * 0.5);
        int cy = (int) Math.round((minY + maxY) * 0.5);
        int cz = (int) Math.round((minZ + maxZ) * 0.5);
        ITModelSplitterVec3i centerKey = new ITModelSplitterVec3i(cx, cy, cz);
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

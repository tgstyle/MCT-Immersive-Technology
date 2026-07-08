package mctmods.immersivetechnology.core.util.solarregistry;

import mctmods.immersivetechnology.common.multiblocks.metal.logic.SolarReflectorLogic;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.*;

public class SolarRegistry {
    public static final int SOLAR_MAX_RANGE = 22;
    public static final int SOLAR_MIN_RANGE = 12;

    public static class RegisterResult {
        public boolean success = false;
        public boolean vertical = false;
        public int requiredMove = 0;
    }

    public static class GroupData {
        public long danceStartTick = -1;
        public long pendingTick = -1L;
        public int animationPhase = -4;
        public int groupSize = 1;
    }

    public static SolarRegistryData getData(Level level) { return ((ServerLevel)level).getDataStorage().computeIfAbsent(SolarRegistryData::load, SolarRegistryData::new, "it_solar_registry"); }

    public static synchronized RegisterResult registerTower(Level level, BlockPos base) {
        RegisterResult result = new RegisterResult();
        if (level.isClientSide) return result;
        SolarRegistryData data = getData(level);
        int y = base.getY();
        Set<BlockPos> towersAtY = data.towerBasesByY.computeIfAbsent(y, k -> new HashSet<>());
        if (towersAtY.contains(base)) { result.success = true; return result; }
        boolean verticalFail = false;
        double minDist = Double.MAX_VALUE;
        boolean overlapFail = false;
        for (Set<BlockPos> set : data.towerBasesByY.values()) for (BlockPos existing : set) {
            int dx = base.getX() - existing.getX();
            int dy = base.getY() - existing.getY();
            int dz = base.getZ() - existing.getZ();
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            minDist = Math.min(minDist, dist);
            if (dx == 0 && dz == 0) { verticalFail = true; }
            else if (dist < SOLAR_MAX_RANGE * 2) { overlapFail = true; }
        }
        if (verticalFail) { result.vertical = true; return result; }
        if (overlapFail) {
            result.requiredMove = (int) Math.ceil(SOLAR_MAX_RANGE * 2 - minDist) + 1;
            return result;
        }
        towersAtY.add(base);
        data.setDirty();
        result.success = true;
        return result;
    }

    public static synchronized void unregisterTower(Level level, BlockPos base) {
        if (level.isClientSide) return;
        SolarRegistryData data = getData(level);
        int y = base.getY();
        if (data.towerBasesByY.containsKey(y)) {
            data.towerBasesByY.get(y).remove(base);
            if (data.towerBasesByY.get(y).isEmpty()) data.towerBasesByY.remove(y);
            data.setDirty();
        }
    }

    private static boolean hasNeighbor(SolarRegistryData data, BlockPos poi) {
        double connSq = Math.pow(SOLAR_MAX_RANGE * 2D, 2);
        int rad = (int) Math.ceil(SOLAR_MAX_RANGE * 2D);
        int y = poi.getY();
        for (int dy = -rad; dy <= rad; dy++) {
            Set<BlockPos> set = data.reflectorPOIsByY.get(y + dy);
            if (set == null) { continue; }
            for (BlockPos other : set) {
                if (!other.equals(poi) && data.untakenReflectors.contains(other) && other.distSqr(poi) <= connSq) { return true; }
            }
        }
        return false;
    }

    public static synchronized void registerReflector(Level level, BlockPos poi) {
        if (level.isClientSide) return;
        SolarRegistryData data = getData(level);
        int y = poi.getY();
        data.reflectorPOIsByY.computeIfAbsent(y, k -> new HashSet<>()).add(poi);
        data.untakenReflectors.add(poi);
        if (hasNeighbor(data, poi)) { data.groupsDirty = true; }
        else {
            data.parent.put(poi, poi);
            data.rank.put(poi, 0);
            GroupData gd = new GroupData();
            gd.pendingTick = level.getGameTime() + 100L;
            gd.animationPhase = -5;
            gd.groupSize = 1;
            data.groupData.put(poi, gd);
        }
        data.setDirty();
    }

    public static synchronized void unregisterReflector(Level level, BlockPos poi) {
        if (level.isClientSide) return;
        SolarRegistryData data = getData(level);
        int y = poi.getY();
        if (data.reflectorPOIsByY.containsKey(y)) {
            data.reflectorPOIsByY.get(y).remove(poi);
            if (data.reflectorPOIsByY.get(y).isEmpty()) data.reflectorPOIsByY.remove(y);
            boolean wasUntaken = data.untakenReflectors.remove(poi);
            if (wasUntaken) {
                if (hasNeighbor(data, poi)) { data.groupsDirty = true; }
                else {
                    data.parent.remove(poi);
                    data.rank.remove(poi);
                    data.groupData.remove(poi);
                }
            }
            data.setDirty();
        }
    }

    public static synchronized void notifyTaken(Level level, BlockPos poi, boolean taken) {
        if (level.isClientSide) return;
        SolarRegistryData data = getData(level);
        boolean changed;
        if (taken) { changed = data.untakenReflectors.remove(poi); }
        else { changed = data.untakenReflectors.add(poi); }
        if (changed) {
            if (taken && data.untakenReflectors.isEmpty()) {
                data.groupData.clear();
                data.parent.clear();
                data.rank.clear();
                data.groupsDirty = false;
            }
            else if (!hasNeighbor(data, poi)) {
                if (taken) {
                    data.parent.remove(poi);
                    data.rank.remove(poi);
                    data.groupData.remove(poi);
                } else {
                    data.parent.put(poi, poi);
                    data.rank.put(poi, 0);
                    GroupData gd = new GroupData();
                    gd.pendingTick = level.getGameTime() + 100L;
                    gd.animationPhase = -5;
                    gd.groupSize = 1;
                    data.groupData.put(poi, gd);
                }
            }
            else { data.groupsDirty = true; }
            data.setDirty();
        }
    }

    public static synchronized void updateDance(Level level) {
        if (level.isClientSide) return;
        SolarRegistryData data = getData(level);
        if (data.groupsDirty) {
            buildGroups(level);
            data.groupsDirty = false;
            data.setDirty();
        }
        float danceDuration = SolarReflectorLogic.getDanceDuration();
        boolean changed = false;
        for (GroupData gd : data.groupData.values()) {
            if (gd.animationPhase == -5) {
                long currentTime = level.getGameTime();
                if (currentTime >= gd.pendingTick) {
                    gd.danceStartTick = currentTime;
                    gd.animationPhase = -2;
                    changed = true;
                }
            } else if (gd.animationPhase == -2) {
                float currentPhase = (level.getGameTime() - gd.danceStartTick) * 0.05f;
                if (currentPhase >= danceDuration) {
                    gd.animationPhase = -3;
                    changed = true;
                }
            } else if (gd.animationPhase == -3) {
                float currentPhase = (level.getGameTime() - gd.danceStartTick) * 0.05f;
                if (currentPhase >= danceDuration + 1) {
                    gd.animationPhase = -4;
                    changed = true;
                }
            }
        }
        if (changed) data.setDirty();
    }

    private static synchronized void buildGroups(Level level) {
        SolarRegistryData data = getData(level);
        data.parent.clear();
        data.rank.clear();
        Set<BlockPos> untaken = new HashSet<>(data.untakenReflectors);
        for (BlockPos pos : untaken) {
            data.parent.put(pos, pos);
            data.rank.put(pos, 0);
        }
        double connSq = Math.pow(SOLAR_MAX_RANGE * 2D, 2);
        int rad = (int) Math.ceil(SOLAR_MAX_RANGE * 2D);
        for (BlockPos p1 : untaken) {
            int y1 = p1.getY();
            for (int dy = -rad; dy <= rad; dy++) {
                int cy = y1 + dy;
                Set<BlockPos> set = data.reflectorPOIsByY.getOrDefault(cy, new HashSet<>());
                for (BlockPos p2 : set) {
                    if (untaken.contains(p2) && p1.asLong() > p2.asLong() && p1.distSqr(p2) <= connSq) { union(data, p1, p2); }
                }
            }
        }
        Map<BlockPos, Set<BlockPos>> groups = new HashMap<>();
        for (BlockPos pos : untaken) {
            BlockPos root = find(data, pos);
            groups.computeIfAbsent(root, k -> new HashSet<>()).add(pos);
        }
        Map<BlockPos, GroupData> newGroupData = new HashMap<>();
        long currentTime = level.getGameTime();
        for (Map.Entry<BlockPos, Set<BlockPos>> entry : groups.entrySet()) {
            BlockPos root = entry.getKey();
            Set<BlockPos> members = entry.getValue();
            BlockPos leader = members.stream().min(Comparator.comparingLong(BlockPos::asLong)).orElse(root);
            GroupData gd = data.groupData.remove(leader);
            int currentSize = members.size();
            if (gd == null || gd.animationPhase == -4) {
                gd = new GroupData();
                gd.pendingTick = currentTime + 100L;
                gd.animationPhase = -5;
            }
            gd.groupSize = currentSize;
            newGroupData.put(leader, gd);
            if (!leader.equals(root)) {
                Integer r = data.rank.remove(root);
                if (r != null) data.rank.put(leader, r);
            }
            for (BlockPos p : members) { data.parent.put(p, leader); }
        }
        data.groupData.clear();
        data.groupData.putAll(newGroupData);
    }

    public static BlockPos find(SolarRegistryData data, BlockPos pos) {
        BlockPos p = data.parent.get(pos);
        if (p == null) return null;
        if (!pos.equals(p)) {
            BlockPos pp = find(data, p);
            data.parent.put(pos, pp);
        }
        return data.parent.get(pos);
    }

    public static void union(SolarRegistryData data, BlockPos a, BlockPos b) {
        BlockPos pa = find(data, a);
        BlockPos pb = find(data, b);
        if (pa == null || pb == null) return;
        if (pa.equals(pb)) return;
        int ra = data.rank.getOrDefault(pa, 0);
        int rb = data.rank.getOrDefault(pb, 0);
        if (ra > rb) {
            data.parent.put(pb, pa);
        } else {
            data.parent.put(pa, pb);
            if (ra == rb) { data.rank.put(pb, rb + 1); }
        }
    }

    public static BlockPos findRoot(Level level, BlockPos poi) {
        if (level.isClientSide) return null;
        SolarRegistryData data = getData(level);
        return find(data, poi);
    }

    public static GroupData getGroupData(Level level, BlockPos root) {
        if (level.isClientSide) return null;
        SolarRegistryData data = getData(level);
        return data.groupData.get(root);
    }

    public static synchronized Set<BlockPos> getReflectorsInRange(Level level, BlockPos base, double minDist, double maxDist) {
        if (level.isClientSide) return new HashSet<>();
        SolarRegistryData data = getData(level);
        int y = base.getY();
        Set<BlockPos> inRange = new HashSet<>();
        Set<BlockPos> allAtY = data.reflectorPOIsByY.getOrDefault(y, new HashSet<>());
        for (BlockPos poi : allAtY) {
            double distSq = base.distSqr(poi);
            if (distSq >= minDist * minDist && distSq <= maxDist * maxDist) { inRange.add(poi); }
        }
        return inRange;
    }
}

package mctmods.immersivetechnology.common.util.solarregistry;

import mctmods.immersivetechnology.common.Config;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

import java.util.HashSet;
import java.util.Set;

public class SolarRegistry {
    public static final int SOLAR_MAX_RANGE = Config.ITConfig.Multiblocks.solarReflector.solarReflector_maxRange;

    public static class RegisterResult {
        public boolean success = false;
        public boolean vertical = false;
        public int requiredMove = 0;
    }

    public static SolarRegistryData getData(World world) {
        MapStorage storage = world.getMapStorage();
        if (storage == null) {
            throw new RuntimeException("MapStorage is null");
        }
        WorldSavedData wsd = storage.getOrLoadData(SolarRegistryData.class, "it_solar_registry");
        SolarRegistryData instance;
        if (wsd instanceof SolarRegistryData) {
            instance = (SolarRegistryData) wsd;
        } else {
            instance = new SolarRegistryData("it_solar_registry");
            storage.setData("it_solar_registry", instance);
        }
        return instance;
    }

    public static synchronized RegisterResult canRegisterTower(World world, BlockPos base) {
        RegisterResult result = new RegisterResult();
        if (world.isRemote) return result;
        SolarRegistryData data = getData(world);
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
        result.success = true;
        return result;
    }

    public static synchronized RegisterResult registerTower(World world, BlockPos base) {
        RegisterResult result = canRegisterTower(world, base);
        if (!result.success) return result;
        SolarRegistryData data = getData(world);
        int y = base.getY();
        data.towerBasesByY.computeIfAbsent(y, k -> new HashSet<>()).add(base);
        data.markDirty();
        return result;
    }

    public static synchronized void unregisterTower(World world, BlockPos base) {
        if (world.isRemote) return;
        SolarRegistryData data = getData(world);
        int y = base.getY();
        if (data.towerBasesByY.containsKey(y)) {
            data.towerBasesByY.get(y).remove(base);
            if (data.towerBasesByY.get(y).isEmpty()) data.towerBasesByY.remove(y);
            data.markDirty();
        }
    }

    public static synchronized void registerReflector(World world, BlockPos poi) {
        if (world.isRemote) return;
        SolarRegistryData data = getData(world);
        int y = poi.getY();
        data.reflectorPOIsByY.computeIfAbsent(y, k -> new HashSet<>()).add(poi);
        data.untakenReflectors.add(poi);
        data.markDirty();
    }

    public static synchronized void unregisterReflector(World world, BlockPos poi) {
        if (world.isRemote) return;
        SolarRegistryData data = getData(world);
        int y = poi.getY();
        if (data.reflectorPOIsByY.containsKey(y)) {
            data.reflectorPOIsByY.get(y).remove(poi);
            if (data.reflectorPOIsByY.get(y).isEmpty()) data.reflectorPOIsByY.remove(y);
            data.untakenReflectors.remove(poi);
            data.markDirty();
        }
    }

    public static synchronized void notifyTaken(World world, BlockPos poi, boolean taken) {
        if (world.isRemote) return;
        SolarRegistryData data = getData(world);
        boolean changed;
        if (taken) { changed = data.untakenReflectors.remove(poi); }
        else { changed = data.untakenReflectors.add(poi); }
        if (changed) {
            data.markDirty();
        }
    }

    public static synchronized Set<BlockPos> getReflectorsInRange(World world, BlockPos base, int min, int max) {
        SolarRegistryData data = getData(world);
        int y = base.getY();
        Set<BlockPos> inRange = new HashSet<>();
        double minSq = min * min;
        double maxSq = max * max;
        Set<BlockPos> set = data.reflectorPOIsByY.getOrDefault(y, new HashSet<>());
        for (BlockPos p : set) {
            double dsq = base.distanceSq(p);
            if (dsq >= minSq && dsq <= maxSq) inRange.add(p);
        }
        return inRange;
    }
}

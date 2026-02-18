package mctmods.immersivetechnology.core.util.solarregistry;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.core.BlockPos;

public class SolarRegistryData extends SavedData {
    public final Map<Integer, Set<BlockPos>> towerBasesByY = new HashMap<>();
    public final Map<Integer, Set<BlockPos>> reflectorPOIsByY = new HashMap<>();
    public final Set<BlockPos> untakenReflectors = new HashSet<>();
    public boolean groupsDirty = true;
    public final Map<BlockPos, BlockPos> parent = new HashMap<>();
    public final Map<BlockPos, Integer> rank = new HashMap<>();
    public final Map<BlockPos, SolarRegistry.GroupData> groupData = new HashMap<>();

    public SolarRegistryData() { super(); }

    public static SolarRegistryData load(CompoundTag nbt) {
        SolarRegistryData data = new SolarRegistryData();
        for (String key : nbt.getAllKeys()) {
            if (key.startsWith("towers_")) {
                int y = Integer.parseInt(key.substring(7));
                ListTag list = nbt.getList(key, Tag.TAG_COMPOUND);
                Set<BlockPos> set = new HashSet<>();
                for (Tag tag : list) { set.add(BlockPos.of(((CompoundTag)tag).getLong("pos"))); }
                if (!set.isEmpty()) data.towerBasesByY.put(y, set);
            }
            else if (key.startsWith("reflectors_")) {
                int y = Integer.parseInt(key.substring(11));
                ListTag list = nbt.getList(key, Tag.TAG_COMPOUND);
                Set<BlockPos> set = new HashSet<>();
                for (Tag tag : list) { set.add(BlockPos.of(((CompoundTag)tag).getLong("pos"))); }
                if (!set.isEmpty()) data.reflectorPOIsByY.put(y, set);
            }
        }
        if (nbt.contains("untaken")) {
            ListTag list = nbt.getList("untaken", Tag.TAG_LONG);
            for (Tag tag : list) { data.untakenReflectors.add(BlockPos.of(((LongTag)tag).getAsLong())); }
        }
        if (nbt.contains("groups")) {
            ListTag groups = nbt.getList("groups", Tag.TAG_COMPOUND);
            for (Tag t : groups) {
                CompoundTag tag = (CompoundTag)t;
                BlockPos leader = BlockPos.of(tag.getLong("leader"));
                SolarRegistry.GroupData gd = new SolarRegistry.GroupData();
                gd.danceStartTick = tag.getLong("danceStartTick");
                gd.pendingTick = tag.getLong("pendingTick");
                gd.animationPhase = tag.getInt("animationPhase");
                gd.groupSize = tag.getInt("groupSize");
                data.groupData.put(leader, gd);
            }
        }
        data.groupsDirty = true;
        return data;
    }

    @Override @NotNull public CompoundTag save(@NotNull CompoundTag nbt) {
        for (Map.Entry<Integer, Set<BlockPos>> entry : towerBasesByY.entrySet()) {
            if (entry.getValue().isEmpty()) continue;
            ListTag list = new ListTag();
            for (BlockPos pos : entry.getValue()) {
                CompoundTag tag = new CompoundTag();
                tag.putLong("pos", pos.asLong());
                list.add(tag);
            }
            nbt.put("towers_" + entry.getKey(), list);
        }
        for (Map.Entry<Integer, Set<BlockPos>> entry : reflectorPOIsByY.entrySet()) {
            if (entry.getValue().isEmpty()) continue;
            ListTag list = new ListTag();
            for (BlockPos pos : entry.getValue()) {
                CompoundTag tag = new CompoundTag();
                tag.putLong("pos", pos.asLong());
                list.add(tag);
            }
            nbt.put("reflectors_" + entry.getKey(), list);
        }
        if (!untakenReflectors.isEmpty()) {
            ListTag untakenList = new ListTag();
            for (BlockPos pos : untakenReflectors) { untakenList.add(LongTag.valueOf(pos.asLong())); }
            nbt.put("untaken", untakenList);
        }
        if (!groupData.isEmpty()) {
            nbt.put("groups", createGroupsTag());
        }
        return nbt;
    }

    private ListTag createGroupsTag() {
        ListTag groups = new ListTag();
        for (Map.Entry<BlockPos, SolarRegistry.GroupData> entry : groupData.entrySet()) {
            CompoundTag tag = new CompoundTag();
            tag.putLong("leader", entry.getKey().asLong());
            tag.putLong("danceStartTick", entry.getValue().danceStartTick);
            tag.putLong("pendingTick", entry.getValue().pendingTick);
            tag.putInt("animationPhase", entry.getValue().animationPhase);
            tag.putInt("groupSize", entry.getValue().groupSize);
            groups.add(tag);
        }
        return groups;
    }
}

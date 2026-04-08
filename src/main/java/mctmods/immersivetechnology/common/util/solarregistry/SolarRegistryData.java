package mctmods.immersivetechnology.common.util.solarregistry;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.storage.WorldSavedData;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SolarRegistryData extends WorldSavedData {
    public final Map<Integer, Set<BlockPos>> towerBasesByY = new HashMap<>();
    public final Map<Integer, Set<BlockPos>> reflectorPOIsByY = new HashMap<>();
    public final Set<BlockPos> untakenReflectors = new HashSet<>();

    public SolarRegistryData(String name) {
        super(name);
    }

    @Override public void readFromNBT(NBTTagCompound nbt) {
        for (String key : nbt.getKeySet()) {
            if (key.startsWith("towers_")) {
                int y = Integer.parseInt(key.substring(7));
                NBTTagList list = nbt.getTagList(key, 10);
                Set<BlockPos> set = new HashSet<>();
                for (int i = 0; i < list.tagCount(); i++) {
                    NBTTagCompound tag = list.getCompoundTagAt(i);
                    set.add(BlockPos.fromLong(tag.getLong("pos")));
                }
                towerBasesByY.put(y, set);
            } else if (key.startsWith("reflectors_")) {
                int y = Integer.parseInt(key.substring(11));
                NBTTagList list = nbt.getTagList(key, 10);
                Set<BlockPos> set = new HashSet<>();
                for (int i = 0; i < list.tagCount(); i++) {
                    NBTTagCompound tag = list.getCompoundTagAt(i);
                    set.add(BlockPos.fromLong(tag.getLong("pos")));
                }
                reflectorPOIsByY.put(y, set);
            }
        }
        if (nbt.hasKey("untaken")) {
            NBTTagList list = nbt.getTagList("untaken", 4);
            for (int i = 0; i < list.tagCount(); i++) {
                untakenReflectors.add(BlockPos.fromLong(((NBTTagLong)list.get(i)).getLong()));
            }
        }
    }

    @Override @Nonnull public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound nbt) {
        for (Map.Entry<Integer, Set<BlockPos>> entry : towerBasesByY.entrySet()) {
            if (entry.getValue().isEmpty()) continue;
            NBTTagList list = new NBTTagList();
            for (BlockPos pos : entry.getValue()) {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setLong("pos", pos.toLong());
                list.appendTag(tag);
            }
            nbt.setTag("towers_" + entry.getKey(), list);
        }
        for (Map.Entry<Integer, Set<BlockPos>> entry : reflectorPOIsByY.entrySet()) {
            if (entry.getValue().isEmpty()) continue;
            NBTTagList list = new NBTTagList();
            for (BlockPos pos : entry.getValue()) {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setLong("pos", pos.toLong());
                list.appendTag(tag);
            }
            nbt.setTag("reflectors_" + entry.getKey(), list);
        }
        if (!untakenReflectors.isEmpty()) {
            NBTTagList untakenList = new NBTTagList();
            for (BlockPos pos : untakenReflectors) {
                untakenList.appendTag(new NBTTagLong(pos.toLong()));
            }
            nbt.setTag("untaken", untakenList);
        }
        return nbt;
    }
}

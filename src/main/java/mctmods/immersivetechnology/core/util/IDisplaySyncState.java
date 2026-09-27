package mctmods.immersivetechnology.core.util;

import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import com.immersiveconvergence.api.multiblock.IDisplayContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public interface IDisplaySyncState extends IMultiblockState, IDisplayContext {
    @Override default void writeSyncNBT(CompoundTag nbt) {
        CompoundTag display = new CompoundTag();
        writeDisplaySyncNBT(display);
        nbt.put("display", display);
    }

    @Override default void readSyncNBT(CompoundTag nbt) { if (nbt.contains("display", Tag.TAG_COMPOUND)) { readDisplaySyncNBT(nbt.getCompound("display")); } }
}

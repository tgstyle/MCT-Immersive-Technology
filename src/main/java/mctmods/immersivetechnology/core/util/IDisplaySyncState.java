package mctmods.immersivetechnology.core.util;

import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import com.immersiveconvergence.api.multiblock.IDisplayContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public interface IDisplaySyncState extends IMultiblockState, IDisplayContext {
    @Override default void writeSyncNBT(CompoundTag nbt, HolderLookup.Provider provider) {
        CompoundTag display = new CompoundTag();
        writeDisplaySyncNBT(display, provider);
        nbt.put("display", display);
    }

    @Override default void readSyncNBT(CompoundTag nbt, HolderLookup.Provider provider) { if (nbt.contains("display", Tag.TAG_COMPOUND)) { readDisplaySyncNBT(nbt.getCompound("display"), provider); } }
}

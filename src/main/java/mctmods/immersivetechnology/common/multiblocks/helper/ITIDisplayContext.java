package mctmods.immersivetechnology.common.multiblocks.helper;

import blusunrize.immersiveengineering.api.energy.AveragingEnergyStorage;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.ProcessContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import java.util.List;

public interface ITIDisplayContext {
    default boolean isActive() { return false; }

    default AveragingEnergyStorage getEnergy() { return null; }

    default List<AveragingEnergyStorage> getEnergies() { AveragingEnergyStorage e = getEnergy(); return e != null ? List.of(e) : List.of(); }

    default IItemHandlerModifiable getInventory() { return ProcessContext.EMPTY_ITEM_HANDLER; }

    default IFluidTank[] getInternalTanks() { return ProcessContext.EMPTY_TANKS; }

    default void writeDisplaySyncNBT(CompoundTag nbt, HolderLookup.Provider provider) { }

    default void readDisplaySyncNBT(CompoundTag nbt, HolderLookup.Provider provider) { }
}

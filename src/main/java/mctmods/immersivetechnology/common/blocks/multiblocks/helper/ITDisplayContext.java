package mctmods.immersivetechnology.common.blocks.multiblocks.helper;

import blusunrize.immersiveengineering.api.energy.AveragingEnergyStorage;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.ProcessContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.List;

@SuppressWarnings("unused")
public interface ITDisplayContext {
    default boolean isActive() { return false; }

    default AveragingEnergyStorage getEnergy() { return null; }

    default List<AveragingEnergyStorage> getEnergies() { AveragingEnergyStorage e = getEnergy(); return e != null ? List.of(e) : List.of(); }

    default IItemHandlerModifiable getInventory() { return ProcessContext.EMPTY_ITEM_HANDLER; }

    default IFluidTank[] getInternalTanks() { return ProcessContext.EMPTY_TANKS; }

    default void writeDisplaySyncNBT(CompoundTag nbt) { }

    default void readDisplaySyncNBT(CompoundTag nbt) { }
}

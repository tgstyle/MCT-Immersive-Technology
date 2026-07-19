package mctmods.immersivetechnology.common.blocks.metal.logic;

import mctmods.immersivetechnology.common.blocks.metal.shapes.ITrashCanShape;
import mctmods.immersivetechnology.core.util.TranslationKey;
import mctmods.immersivetechnology.core.ITClientConfig;
import mctmods.immersivetechnology.core.registration.ITBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

public class TrashEnergyBlockEntity extends OSDCommonBlockEntity implements IEnergyStorage, ITrashCanShape {
    public TrashEnergyBlockEntity(BlockPos pos, BlockState state) { super(ITBlockEntities.TRASH_ENERGY.get(), pos, state); }

    @SuppressWarnings("unused")
    public IEnergyStorage getEnergyHandler(@Nullable Direction side) {
        return this;
    }

    @Override public int receiveEnergy(int maxReceive, boolean simulate) {
        if (!simulate) { acceptedAmount += maxReceive; }
        return maxReceive;
    }

    @Override public int extractEnergy(int maxExtract, boolean simulate) { return 0; }

    @Override public int getEnergyStored() { return 0; }

    @Override public int getMaxEnergyStored() { return Integer.MAX_VALUE; }

    @Override public boolean canExtract() { return false; }

    @Override public boolean canReceive() { return true; }

    @Override public TranslationKey text() { return ITClientConfig.perTickTrashCans ? TranslationKey.OVERLAY_OSD_TRASH_ENERGY_NORMAL_ALTERNATIVE : TranslationKey.OVERLAY_OSD_TRASH_ENERGY_NORMAL_FIRST_LINE; }
}

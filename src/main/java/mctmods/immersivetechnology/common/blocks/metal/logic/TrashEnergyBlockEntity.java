package mctmods.immersivetechnology.common.blocks.metal.logic;

import mctmods.immersivetechnology.common.blocks.metal.shape.TrashCanShape;
import mctmods.immersivetechnology.common.util.TranslationKey;
import mctmods.immersivetechnology.core.ITClientConfig;
import mctmods.immersivetechnology.core.registration.ITBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.annotation.Nonnull;

public class TrashEnergyBlockEntity extends OSDCommonBlockEntity implements IEnergyStorage, TrashCanShape {
    public TrashEnergyBlockEntity(BlockPos pos, BlockState state) { super(ITBlockEntities.TRASH_ENERGY.get(), pos, state); }

    @SuppressWarnings("unchecked")
    @Override @Nonnull public <T>LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction facing) {
        if (capability == ForgeCapabilities.ENERGY) { return LazyOptional.of(() -> (T) this); }
        return super.getCapability(capability, facing);
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

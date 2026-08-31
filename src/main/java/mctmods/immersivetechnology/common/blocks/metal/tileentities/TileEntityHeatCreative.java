package mctmods.immersivetechnology.common.blocks.metal.tileentities;

import com.immersiveconvergence.api.capability.IHeatProvider;

import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;

import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import javax.annotation.Nonnull;
import net.minecraft.nbt.NBTTagCompound;

public class TileEntityHeatCreative extends TileEntityIEBase implements IHeatProvider {
    @Override public double getHeatLevel() { return Multiblocks.boilerHeat.boiler_heat_max; }

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { }
}

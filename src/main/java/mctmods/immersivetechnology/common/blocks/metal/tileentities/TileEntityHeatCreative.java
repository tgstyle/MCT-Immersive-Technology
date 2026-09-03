package mctmods.immersivetechnology.common.blocks.metal.tileentities;

import com.immersiveconvergence.common.ICContent;
import com.immersiveconvergence.common.blocks.types.ICBlockType_Device;

import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;

import javax.annotation.Nonnull;

import mctmods.immersivetechnology.common.util.ITUtils;

public class TileEntityHeatCreative extends TileEntityIEBase implements ITickable {
    @Override public void update() {
        if (world.isRemote) { return; }
        world.setBlockState(pos, ITUtils.stateOf(ICContent.blockDevice, ICBlockType_Device.HEAT_CREATIVE), 3);
    }

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {}

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {}
}

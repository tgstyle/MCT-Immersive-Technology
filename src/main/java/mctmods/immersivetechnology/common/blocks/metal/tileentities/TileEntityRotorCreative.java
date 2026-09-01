package mctmods.immersivetechnology.common.blocks.metal.tileentities;

import com.immersiveconvergence.common.ICContent;
import com.immersiveconvergence.common.blocks.types.ICBlockType_Device;

import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;

import javax.annotation.Nonnull;

public class TileEntityRotorCreative extends TileEntityIEBase implements ITickable {
    private int rpm;
    private EnumFacing facing = EnumFacing.NORTH;

    @Override public void update() {
        if (world.isRemote) { return; }
        IBlockState state = ICContent.blockDevice.getStateFromMeta(ICBlockType_Device.ROTOR_CREATIVE.getMeta());
        world.setBlockState(pos, state, 3);
        TileEntity converted = world.getTileEntity(pos);
        if (converted instanceof com.immersiveconvergence.common.blocks.tileentities.TileEntityRotorCreative) {
            com.immersiveconvergence.common.blocks.tileentities.TileEntityRotorCreative rotor = (com.immersiveconvergence.common.blocks.tileentities.TileEntityRotorCreative)converted;
            rotor.rpm = rpm;
            rotor.facing = facing;
            rotor.markDirty();
            rotor.markContainingBlockForUpdate(null);
        }
    }

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        rpm = nbt.getInteger("rpm");
        facing = EnumFacing.values()[nbt.getInteger("facing")];
    }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        nbt.setInteger("rpm", rpm);
        nbt.setInteger("facing", facing.ordinal());
    }
}

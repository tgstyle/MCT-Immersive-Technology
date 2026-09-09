package mctmods.immersivetechnology.common.blocks.metal.tileentities;

import com.immersiveconvergence.api.block.ICTileEntityBase;
import com.immersiveconvergence.api.util.ICUtils;
import com.immersiveconvergence.common.ICContent;
import com.immersiveconvergence.common.blocks.types.ICBlockType_Device;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;

import javax.annotation.Nonnull;


public class TileEntityHeatCreative extends ICTileEntityBase implements ITickable {
    @Override public void update() {
        if (world.isRemote) { return; }
        world.setBlockState(pos, ICUtils.stateOf(ICContent.blockDevice, ICBlockType_Device.HEAT_CREATIVE), 3);
    }

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {}

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {}
}

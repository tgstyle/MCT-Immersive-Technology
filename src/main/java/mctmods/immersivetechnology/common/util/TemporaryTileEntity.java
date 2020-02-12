package mctmods.immersivetechnology.common.util;

import blusunrize.immersiveengineering.api.MultiblockHandler;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.util.Utils;
import mctmods.immersivetechnology.common.CommonProxy;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;

public abstract class TemporaryTileEntity extends TileEntityIEBase implements ITickable {

    public abstract MultiblockHandler.IMultiblock getMultiblock();

    public abstract int[] dimensions();

    public ItemStack checkPos(int pos) {
        int[] structureDimensions = dimensions();
        MultiblockHandler.IMultiblock multiblock = getMultiblock();
        ItemStack s = ItemStack.EMPTY;
        try {
            int blocksPerLevel = structureDimensions[1]*structureDimensions[2];
            int h = (pos/blocksPerLevel);
            int l = (pos%blocksPerLevel/structureDimensions[2]);
            int w = (pos%structureDimensions[2]);
            s = multiblock.getStructureManual()[h][l][w];
        } catch(Exception e) {
            e.printStackTrace();
        }
        return s.copy();
    }

    public void update() {
        if (changeTo != null) {
            world.setBlockToAir(worldPosition);
            world.setBlockState(worldPosition, changeTo);
            if (master) {
                TemporaryTileEntityRequest request = new TemporaryTileEntityRequest();
                request.facing = facing;
                request.multiblock = getMultiblock();
                request.nbtTag = thisNbt;
                request.position = worldPosition;
                request.world = world;
                CommonProxy.toReform.add(request);
            }
        }
    }

    int pos;
    BlockPos worldPosition;
    IBlockState changeTo;
    boolean master;
    EnumFacing facing;
    NBTTagCompound thisNbt;

    @Override
    public void readCustomNBT(NBTTagCompound nbt, boolean descPacket) {
        pos = nbt.getInteger("pos");
        int x = nbt.getInteger("x");
        int y = nbt.getInteger("y");
        int z = nbt.getInteger("z");
        int[] offset = nbt.getIntArray("offset");
        facing = EnumFacing.VALUES[nbt.getInteger("facing")];
        thisNbt = nbt;
        if (offset[0] == 0 && offset[1] == 0 && offset[2] == 0) master = true;
        worldPosition = new BlockPos(x, y, z);
        if(pos < 0) return;
        ItemStack s = ItemStack.EMPTY;
        try {
            s = checkPos(pos);
        } catch(Exception e) {
            e.printStackTrace();
        }
        if (s == ItemStack.EMPTY) return;
        changeTo = Utils.getStateFromItemStack(s);
    }

    @Override
    public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket) {

    }
}

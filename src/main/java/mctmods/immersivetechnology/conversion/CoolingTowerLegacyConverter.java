package mctmods.immersivetechnology.conversion;

import blusunrize.immersiveengineering.api.IEProperties;

import mctmods.immersivetechnology.common.ITContent;
import mctmods.immersivetechnology.common.multiblocks.stone.tileentities.TileEntityCoolingTowerMaster;
import mctmods.immersivetechnology.common.multiblocks.stone.tileentities.TileEntityCoolingTowerSlave;
import mctmods.immersivetechnology.common.multiblocks.stone.tileentitiesmultiblockpart.TileEntityITMultiblockPartCoolingTower;
import mctmods.immersivetechnology.common.multiblocks.stone.types.BlockType_StoneMultiblock;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CoolingTowerLegacyConverter {
    public static void convert(TileEntityCoolingTowerMaster master) {
        World world = master.getWorld();
        BlockPos masterPos = master.getPos();
        for (BlockPos offset : TileEntityITMultiblockPartCoolingTower.instance.worldOffsetsFromMaster(master.facing, master.mirrored)) {
            if (!BlockPos.ORIGIN.equals(offset)) { convertBlock(world, masterPos.add(offset)); }
        }
        convertBlock(world, masterPos);
    }

    private static void convertBlock(World world, BlockPos position) {
        if (world.getBlockState(position).getBlock() != ITContent.blockMetalMultiblock) { return; }
        TileEntity te = world.getTileEntity(position);
        if (!(te instanceof TileEntityCoolingTowerSlave)) { return; }
        TileEntityCoolingTowerSlave part = (TileEntityCoolingTowerSlave)te;
        NBTTagCompound nbt = part.writeToNBT(new NBTTagCompound());
        boolean isMaster = part instanceof TileEntityCoolingTowerMaster;
        part.formed = false;
        IBlockState state = ITContent.blockStoneMultiblock.getStateFromMeta((isMaster ? BlockType_StoneMultiblock.COOLING_TOWER : BlockType_StoneMultiblock.COOLING_TOWER_SLAVE).getMeta()).withProperty(IEProperties.FACING_HORIZONTAL, part.facing).withProperty(IEProperties.MULTIBLOCKSLAVE, !isMaster);
        world.setBlockState(position, state, 2);
        TileEntity converted = world.getTileEntity(position);
        if (converted != null) {
            converted.readFromNBT(nbt);
            converted.markDirty();
            world.notifyBlockUpdate(position, state, state, 2);
            world.addBlockEvent(position, state.getBlock(), 255, 0);
        }
    }
}

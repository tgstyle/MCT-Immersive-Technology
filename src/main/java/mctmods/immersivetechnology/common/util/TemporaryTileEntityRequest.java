package mctmods.immersivetechnology.common.util;

import blusunrize.immersiveengineering.api.MultiblockHandler;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TemporaryTileEntityRequest {

	public BlockPos position;
	public EnumFacing facing;
	public NBTTagCompound nbtTag;
	public World world;
	public MultiblockHandler.IMultiblock multiblock;
	public BlockPos formationPosition;

}
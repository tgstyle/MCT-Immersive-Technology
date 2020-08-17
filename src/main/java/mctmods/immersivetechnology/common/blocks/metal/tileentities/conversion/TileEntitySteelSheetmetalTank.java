package mctmods.immersivetechnology.common.blocks.metal.tileentities.conversion;

import blusunrize.immersiveengineering.api.MultiblockHandler;
import blusunrize.immersiveengineering.common.util.Utils;
import mctmods.immersivetechnology.common.CommonProxy;
import mctmods.immersivetechnology.common.blocks.metal.multiblocks.MultiblockSteelSheetmetalTank;
import mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntitySteelSheetmetalTankSlave;
import mctmods.immersivetechnology.common.util.TemporaryTileEntityRequest;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

//REMOVE THIS AFTER PORTING!!!
public class TileEntitySteelSheetmetalTank extends TileEntitySteelSheetmetalTankSlave {

	public MultiblockHandler.IMultiblock getMultiblock() {
		return MultiblockSteelSheetmetalTank.instance;
	}

	public int[] dimensions() {
		return new int[]{5, 3, 3};
	}

	public ItemStack checkPos(int pos) {
		 return getOriginalBlock();
	}

	int counter = 0;

	public void update() {
		if(++counter > 5 && changeTo != null) {
			world.setBlockState(worldPosition, changeTo);
			if(master) {
				TemporaryTileEntityRequest request = new TemporaryTileEntityRequest();
				request.facing = facing;
				request.multiblock = getMultiblock();
				request.nbtTag = thisNbt;
				request.position = worldPosition;
				request.formationPosition = worldPosition.offset(facing, -1).up();
				request.world = world;
				CommonProxy.toReform.add(request);
			}
		}
	}

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
		if(offset[0] == 0 && offset[1] == 0 && offset[2] == 0) master = true;
		worldPosition = new BlockPos(x, y, z);
		if(pos < 0) return;
		ItemStack s = ItemStack.EMPTY;
		try {
			s = checkPos(pos);
		} catch(Exception e) {
			e.printStackTrace();
		}
		if(s == ItemStack.EMPTY) return;
		changeTo = Utils.getStateFromItemStack(s);
	}
}
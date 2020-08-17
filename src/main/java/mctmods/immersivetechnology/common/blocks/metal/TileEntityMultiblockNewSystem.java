package mctmods.immersivetechnology.common.blocks.metal;

import blusunrize.immersiveengineering.api.MultiblockHandler;
import blusunrize.immersiveengineering.api.crafting.IMultiblockRecipe;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMultiblockMetal;
import blusunrize.immersiveengineering.common.util.Utils;
import mctmods.immersivetechnology.common.util.multiblock.ITMultiblock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

import javax.annotation.Nullable;

public abstract class TileEntityMultiblockNewSystem<T extends TileEntityMultiblockNewSystem<T, R, M>, R extends IMultiblockRecipe, M extends T> extends TileEntityMultiblockMetal<T,R> {

	public TileEntityMultiblockNewSystem(MultiblockHandler.IMultiblock instance, int[] structureDimensions, int energyCapacity, boolean redstoneControl) {
		super(instance, structureDimensions, energyCapacity, redstoneControl);
	}

	public TileEntityMultiblockNewSystem(ITMultiblock<?> instance, int energyCapacity, boolean redstoneControl) {
		super(instance, new int[] { instance.height, instance.length, instance.width }, energyCapacity, redstoneControl);
	}

	@SuppressWarnings("unchecked")
	@Nullable
	@Override
	public T getTileForPos(int targetPos) {
		BlockPos target = getBlockPosForPos(targetPos);
		TileEntity tile = Utils.getExistingTileEntity(world, target);
		if(tile instanceof TileEntityMultiblockNewSystem && tile.getClass().isInstance(this))
			return (T)tile;
		return null;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
		if(capability== CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY&&this.getAccessibleFluidTanks(facing).length > 0)
			return true;
		return super.hasCapability(capability, facing);
	}

	@SuppressWarnings({ "unchecked", "hiding" })
	@Override
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
		if(capability==CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY&&this.getAccessibleFluidTanks(facing).length > 0)
			return (T)new MultiblockFluidWrapper(this, facing);
		return super.getCapability(capability, facing);
	}
}
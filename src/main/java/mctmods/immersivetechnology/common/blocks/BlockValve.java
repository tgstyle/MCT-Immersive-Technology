package mctmods.immersivetechnology.common.blocks;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.client.models.IOBJModelCallback;
import mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntityFluidValve;
import mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntityLoadController;
import mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntityStackLimiter;
import mctmods.immersivetechnology.common.shared.BlockITTileProvider;
import mctmods.immersivetechnology.common.shared.tileentities.TileEntityCommonValve;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Locale;

public class BlockValve extends BlockITTileProvider<BlockValve.BlockType_Valve> {

	public static final PropertyInteger ROTATION = PropertyInteger.create("rotation", 0, 3);

	@Override @Nullable public TileEntity createBasicTE(World worldIn, BlockType_Valve type) {
		switch(type) {
			case FLUID_VALVE: return new TileEntityFluidValve();
			case LOAD_CONTROLLER: return new TileEntityLoadController();
			case STACK_LIMITER: return new TileEntityStackLimiter();
		}
		return null;
	}

	public enum BlockType_Valve implements IStringSerializable, BlockITBase.IBlockEnum {
		FLUID_VALVE,
		LOAD_CONTROLLER,
		STACK_LIMITER;

		@Override @Nonnull public String getName() {
			return this.toString().toLowerCase(Locale.ENGLISH);
		}

		@Override public int getMeta() {
			return ordinal();
		}

		@Override public boolean listForCreative() {
			return true;
		}
	}

	public BlockValve() {
		super("valve", Material.IRON, PropertyEnum.create("type", BlockType_Valve.class), ItemBlockValve.class, IEProperties.FACING_ALL, IOBJModelCallback.PROPERTY, ROTATION);
		this.setHardness(3.0F);
		this.setResistance(15.0F);
		lightOpacity = 0;
		setMetaBlockLayer(BlockType_Valve.FLUID_VALVE.getMeta(), BlockRenderLayer.CUTOUT);
		setMetaBlockLayer(BlockType_Valve.LOAD_CONTROLLER.getMeta(), BlockRenderLayer.CUTOUT);
		this.setAllNotNormalBlock();
	}

	@Override public boolean useCustomStateMapper() { return true; }

	@Override @Nonnull public String getCustomStateMapping(int meta, boolean itemBlock) {
		switch (BlockType_Valve.values()[meta]) {
			case FLUID_VALVE: { return "fluid_valve"; }
			case LOAD_CONTROLLER: { return "load_controller"; }
			default: { return ""; }
		}
	}

	@SuppressWarnings("deprecation")
	@Override @Nonnull public IBlockState getStateForPlacement(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull EnumFacing clickedSide, float hitX, float hitY, float hitZ, int meta, @Nonnull EntityLivingBase placer) {
		IBlockState state = super.getStateForPlacement(world, pos, clickedSide, hitX, hitY, hitZ, meta, placer);
		return state.withProperty(ROTATION, EnumFacing.fromAngle(placer.rotationYaw).getHorizontalIndex());
	}

	@Override public void neighborChanged(@Nonnull IBlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull Block blockIn, @Nonnull BlockPos fromPos) {
		super.neighborChanged(state, world, pos, blockIn, fromPos);
		if (world.isRemote) { return; }
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof TileEntityCommonValve) { ((TileEntityCommonValve)te).updateOpenState(); }
	}

	@Override public boolean canConnectRedstone(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, EnumFacing side) {
		return true;
	}
}

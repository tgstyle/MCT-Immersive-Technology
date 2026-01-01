package mctmods.immersivetechnology.common.blocks;

import blusunrize.immersiveengineering.api.IEProperties;
import mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntityFluidValve;
import mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntityLoadController;
import mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntityStackLimiter;
import mctmods.immersivetechnology.common.shared.BlockITTileProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Locale;

public class BlockValve extends BlockITTileProvider<BlockValve.BlockType_Valve> {

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
		super("valve", Material.IRON, PropertyEnum.create("type", BlockType_Valve.class), ItemBlockITBase.class, IEProperties.FACING_ALL);
		this.setHardness(3.0F);
		this.setResistance(15.0F);
		lightOpacity = 0;
		this.setAllNotNormalBlock();
	}

	@Override public boolean canConnectRedstone(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, EnumFacing side) {
		return true;
	}
}

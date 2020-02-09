package mctmods.immersivetechnology.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.TileEntityMultiblockPart;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMultiblockMetal;
import mctmods.immersivetechnology.common.blocks.BlockITMultiblock;
import mctmods.immersivetechnology.common.blocks.ItemBlockITBase;
import mctmods.immersivetechnology.common.blocks.metal.tileentities.*;
import mctmods.immersivetechnology.common.blocks.metal.types.BlockType_MetalMultiblock;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import net.minecraftforge.common.property.Properties;

public class BlockMetalMultiblock extends BlockITMultiblock<BlockType_MetalMultiblock> {
	public BlockMetalMultiblock() {
		super("metal_multiblock", Material.IRON, PropertyEnum.create("type", BlockType_MetalMultiblock.class), ItemBlockITBase.class, IEProperties.DYNAMICRENDER, IEProperties.BOOLEANS[0], Properties.AnimationProperty, IEProperties.OBJ_TEXTURE_REMAP);
		setHardness(3.0F);
		setResistance(15.0F);
		this.setAllNotNormalBlock();
		lightOpacity = 0;
	}

	@Override
	public boolean useCustomStateMapper() {
		return true;
	}

	@Override
	public String getCustomStateMapping(int meta, boolean itemBlock) {
		if(BlockType_MetalMultiblock.values()[meta].needsCustomState()) return BlockType_MetalMultiblock.values()[meta].getCustomState();
		return null;
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof TileEntityMultiblockPart) {
			TileEntityMultiblockPart<?> tile = (TileEntityMultiblockPart<?>)te;
			if(te instanceof TileEntityMultiblockMetal && ((TileEntityMultiblockMetal<? , ? >) tile).isRedstonePos()) return true;
			if(te instanceof TileEntityDistillerSlave) return tile.pos == 0 || tile.pos == 3 || tile.pos == 5 || tile.pos == 7 || tile.pos == 9 || (tile.pos == 11 && side.getOpposite() == tile.facing) || ((tile.pos == 22 || tile.pos == 23 || tile.pos == 25 || tile.pos == 26) && side == EnumFacing.UP);
			if(te instanceof TileEntitySolarReflectorSlave) return tile.pos == 13 && side == EnumFacing.UP;
			if(te instanceof TileEntitySolarTowerSlave) return (tile.pos%9 == 1 && (tile.pos<19 || tile.pos>37)) || (tile.pos%9 == 3 && (tile.pos<21 || tile.pos>39)) || (tile.pos%9 == 5 && (tile.pos<23 || tile.pos>41)) || (tile.pos%9 == 7 && (tile.pos<25 || tile.pos>43)) || tile.pos == 58 || ((tile.pos == 56 || tile.pos == 54 || tile.pos == 60 || tile.pos == 62) && side == EnumFacing.UP);
			if(te instanceof TileEntityBoilerSlave) return tile.pos == 5 || tile.pos == 9 || (tile.pos == 19 && side.getOpposite() == tile.facing) || ((tile.pos>=35 && tile.pos<=37) && side == EnumFacing.UP);
			if(te instanceof TileEntitySteamTurbineSlave) return tile.pos == 9 || tile.pos == 11 || tile.pos == 15 || tile.pos == 17 || tile.pos == 24 || tile.pos == 26 || (tile.pos == 36 && side.getOpposite() == tile.facing) || (tile.pos == 38 && side == tile.facing.rotateY()) || ((tile.pos == 33 || tile.pos == 69) && side == tile.facing.rotateYCCW()) || ((tile.pos == 35 || tile.pos == 71) && side == tile.facing.rotateY()) || (tile.pos == 105 && (side == tile.facing.rotateYCCW() || side == EnumFacing.UP)) || (tile.pos == 107 && (side == tile.facing.rotateY() || side == EnumFacing.UP)) || (tile.pos == 106 && side == EnumFacing.UP);
		}
		return super.isSideSolid(state, world, pos, side);
	}

	@Override
	public boolean allowHammerHarvest(IBlockState state) {
		return true;
	}

	@Override
	public TileEntity createBasicTE(World worldIn, BlockType_MetalMultiblock type) {
		switch(type) {
			case ALTERNATOR:
				return new TileEntityAlternatorMaster();
			case ALTERNATOR_SLAVE:
				return new TileEntityAlternatorSlave();
			case BOILER:
				return new TileEntityBoilerMaster();
			case BOILER_SLAVE:
				return new TileEntityBoilerSlave();
			case DISTILLER:
				return new TileEntityDistillerMaster();
			case DISTILLER_SLAVE:
				return new TileEntityDistillerSlave();
			case SOLAR_REFLECTOR:
				return new TileEntitySolarReflectorMaster();
			case SOLAR_REFLECTOR_SLAVE:
				return new TileEntitySolarReflectorSlave();
			case SOLAR_TOWER:
				return new TileEntitySolarTowerMaster();
			case SOLAR_TOWER_SLAVE:
				return new TileEntitySolarTowerSlave();
			case STEAM_TURBINE:
				return new TileEntitySteamTurbineMaster();
			case STEAM_TURBINE_SLAVE:
				return new TileEntitySteamTurbineSlave();
		}
		return null;
	}

}
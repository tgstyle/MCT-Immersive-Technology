package mctmods.immersivetechnology.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;

import mctmods.immersivetechnology.common.blocks.BlockITMultiblock;
import mctmods.immersivetechnology.common.blocks.ItemBlockITBase;
import mctmods.immersivetechnology.common.blocks.metal.tileentities.*;
import mctmods.immersivetechnology.common.blocks.metal.types.BlockType_MetalMultiblock;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.world.World;

import net.minecraftforge.common.property.Properties;

public class BlockMetalMultiblock extends BlockITMultiblock<BlockType_MetalMultiblock> {
	public BlockMetalMultiblock() {
		super("metal_multiblock", Material.IRON, PropertyEnum.create("type", BlockType_MetalMultiblock.class), ItemBlockITBase.class, IEProperties.DYNAMICRENDER, IEProperties.BOOLEANS[0], Properties.AnimationProperty, IEProperties.OBJ_TEXTURE_REMAP);
		setHardness(3.0F);
		setResistance(15.0F);
		this.setMetaBlockLayer(BlockType_MetalMultiblock.STEEL_TANK.getMeta(), BlockRenderLayer.CUTOUT);
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

	@Override
	public boolean allowHammerHarvest(IBlockState state) {
		return true;
	}

	@Override
	public TileEntity createBasicTE(World worldIn, BlockType_MetalMultiblock type) {
		switch(type) {
			case ALTERNATOR:
				return new TileEntityAlternator();
			case BOILER:
				return new TileEntityBoiler();
			case DISTILLER:
				return new TileEntityDistiller();
			case SOLAR_REFLECTOR:
				return new TileEntitySolarReflector();
			case SOLAR_TOWER:
				return new TileEntitySolarTower();
			case STEAM_TURBINE:
				return new TileEntitySteamTurbine();
			case STEEL_TANK:
				return new TileEntitySteelSheetmetalTank();
		default:
			break;
		}
		return null;
	}

}
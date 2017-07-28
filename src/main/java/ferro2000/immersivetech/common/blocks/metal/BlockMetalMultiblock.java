package ferro2000.immersivetech.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.TileEntityMultiblockPart;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMultiblockMetal;
import ferro2000.immersivetech.common.blocks.BlockITMultiblock;
import ferro2000.immersivetech.common.blocks.ItemBlockITBase;
import ferro2000.immersivetech.common.blocks.metal.tileentities.TileEntityDistiller;
import ferro2000.immersivetech.common.blocks.metal.tileentities.TileEntitySolarReflector;
import ferro2000.immersivetech.common.blocks.metal.tileentities.TileEntitySolarTower;
import ferro2000.immersivetech.common.blocks.metal.tileentities.TileEntitySteamTurbine;
import ferro2000.immersivetech.common.blocks.metal.types.BlockType_MetalMultiblock;
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
	
	public BlockMetalMultiblock()
	{
		super("metalMultiblock",Material.IRON, PropertyEnum.create("type", BlockType_MetalMultiblock.class), 
				ItemBlockITBase.class, IEProperties.DYNAMICRENDER,IEProperties.BOOLEANS[0],Properties.AnimationProperty,IEProperties.OBJ_TEXTURE_REMAP);
		setHardness(3.0F);
		setResistance(15.0F);
		/*this.setMetaBlockLayer(BlockTypes_MetalMultiblock.TANK.getMeta(), BlockRenderLayer.CUTOUT);
		this.setMetaBlockLayer(BlockTypes_MetalMultiblock.DIESEL_GENERATOR.getMeta(), BlockRenderLayer.CUTOUT);
		this.setMetaBlockLayer(BlockTypes_MetalMultiblock.BOTTLING_MACHINE.getMeta(), BlockRenderLayer.SOLID,BlockRenderLayer.TRANSLUCENT);*/
		this.setAllNotNormalBlock();
		lightOpacity = 0;
	}

	@Override
	public boolean useCustomStateMapper()
	{
		return true;
	}
	@Override
	public String getCustomStateMapping(int meta, boolean itemBlock)
	{
		if(BlockType_MetalMultiblock.values()[meta].needsCustomState())
			return BlockType_MetalMultiblock.values()[meta].getCustomState();
		return null;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta)
	{
		switch(BlockType_MetalMultiblock.values()[meta])
		{
			case DISTILLER:
				return new TileEntityDistiller();
			case SOLAR_TOWER:
				return new TileEntitySolarTower();
			case SOLAR_REFLECTOR:
				return new TileEntitySolarReflector();
			case STEAM_TURBINE:
				return new TileEntitySteamTurbine();
		}
		return null;
	}
	
	@Override
	public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side)
	{
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof TileEntityMultiblockPart)
		{
			TileEntityMultiblockPart tile = (TileEntityMultiblockPart)te;
			if(tile instanceof TileEntityMultiblockMetal && ((TileEntityMultiblockMetal) tile).isRedstonePos())
				return true;
		}
		return super.isSideSolid(state, world, pos, side);
	}
	
	@Override
	public boolean allowHammerHarvest(IBlockState state)
	{
		return true;
	}

}

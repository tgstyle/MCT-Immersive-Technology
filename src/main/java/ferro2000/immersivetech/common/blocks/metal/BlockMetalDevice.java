package ferro2000.immersivetech.common.blocks.metal;

import java.util.Arrays;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.client.models.IOBJModelCallback;
import ferro2000.immersivetech.common.blocks.BlockITTileProvider;
import ferro2000.immersivetech.common.blocks.ItemBlockITBase;
import ferro2000.immersivetech.common.blocks.metal.types.BlockType_MetalDevice;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.common.property.Properties;

public class BlockMetalDevice extends BlockITTileProvider<BlockType_MetalDevice> {

	public BlockMetalDevice()
	{
		super("metal_device", Material.IRON, PropertyEnum.create("type", BlockType_MetalDevice.class), ItemBlockITBase.class, IEProperties.FACING_ALL, IEProperties.MULTIBLOCKSLAVE, IEProperties.BOOLEANS[0], Properties.AnimationProperty, IOBJModelCallback.PROPERTY, IEProperties.OBJ_TEXTURE_REMAP);
		this.setHardness(3.0F);
		this.setResistance(15.0F);
		lightOpacity = 0;
		this.setAllNotNormalBlock();
		/*this.setMetaBlockLayer(BlockTypes_MetalDevice1.CHARGING_STATION.getMeta(), BlockRenderLayer.SOLID, BlockRenderLayer.TRANSLUCENT);
		this.setMetaBlockLayer(BlockTypes_MetalDevice1.FLUID_PIPE.getMeta(), BlockRenderLayer.CUTOUT);
		this.setMetaBlockLayer(BlockTypes_MetalDevice1.SAMPLE_DRILL.getMeta(), BlockRenderLayer.CUTOUT);
		this.setMetaBlockLayer(BlockTypes_MetalDevice1.FLOODLIGHT.getMeta(), BlockRenderLayer.SOLID, BlockRenderLayer.TRANSLUCENT);
		this.setMetaBlockLayer(BlockTypes_MetalDevice1.ELECTRIC_LANTERN.getMeta(), BlockRenderLayer.SOLID, BlockRenderLayer.TRANSLUCENT);
		this.setMetaBlockLayer(BlockTypes_MetalDevice1.FURNACE_HEATER.getMeta(), BlockRenderLayer.CUTOUT);

		this.setMetaLightOpacity(BlockTypes_MetalDevice1.FURNACE_HEATER.getMeta(), 255);
		this.setMetaLightOpacity(BlockTypes_MetalDevice1.DYNAMO.getMeta(), 255);
		this.setMetaLightOpacity(BlockTypes_MetalDevice1.THERMOELECTRIC_GEN.getMeta(), 255);
		this.setNotNormalBlock(BlockTypes_MetalDevice1.BLAST_FURNACE_PREHEATER.getMeta());
		this.setNotNormalBlock(BlockTypes_MetalDevice1.ELECTRIC_LANTERN.getMeta());
		this.setNotNormalBlock(BlockTypes_MetalDevice1.CHARGING_STATION.getMeta());
		this.setNotNormalBlock(BlockTypes_MetalDevice1.FLUID_PIPE.getMeta());
		this.setNotNormalBlock(BlockTypes_MetalDevice1.SAMPLE_DRILL.getMeta());
		this.setNotNormalBlock(BlockTypes_MetalDevice1.TESLA_COIL.getMeta());
		this.setNotNormalBlock(BlockTypes_MetalDevice1.FLOODLIGHT.getMeta());
		this.setNotNormalBlock(BlockTypes_MetalDevice1.TURRET_CHEM.getMeta());
		this.setNotNormalBlock(BlockTypes_MetalDevice1.TURRET_GUN.getMeta());
		this.setNotNormalBlock(BlockTypes_MetalDevice1.BELLJAR.getMeta());

		this.setMetaMobilityFlag(BlockTypes_MetalDevice1.BLAST_FURNACE_PREHEATER.getMeta(), EnumPushReaction.BLOCK);
		this.setMetaMobilityFlag(BlockTypes_MetalDevice1.SAMPLE_DRILL.getMeta(), EnumPushReaction.BLOCK);
		this.setMetaMobilityFlag(BlockTypes_MetalDevice1.TESLA_COIL.getMeta(), EnumPushReaction.BLOCK);
		this.setMetaMobilityFlag(BlockTypes_MetalDevice1.TURRET_CHEM.getMeta(), EnumPushReaction.BLOCK);
		this.setMetaMobilityFlag(BlockTypes_MetalDevice1.TURRET_GUN.getMeta(), EnumPushReaction.BLOCK);
		this.setMetaMobilityFlag(BlockTypes_MetalDevice1.BELLJAR.getMeta(), EnumPushReaction.BLOCK);*/
	}

	@Override
	public boolean useCustomStateMapper()
	{
		return true;
	}
	@Override
	public String getCustomStateMapping(int meta, boolean itemBlock){
				
		/*if(BlockTypes_MetalDevice1.values()[meta]==BlockTypes_MetalDevice1.ELECTRIC_LANTERN)
			return "lantern";
		else if(BlockTypes_MetalDevice1.values()[meta]==BlockTypes_MetalDevice1.CHARGING_STATION)
			return "charging_station";
		else if(BlockTypes_MetalDevice1.values()[meta]==BlockTypes_MetalDevice1.FLUID_PIPE)
			return "pipe";
		else if(BlockTypes_MetalDevice1.values()[meta]==BlockTypes_MetalDevice1.SAMPLE_DRILL)
			return "core_drill";
		else if(BlockTypes_MetalDevice1.values()[meta]==BlockTypes_MetalDevice1.FLOODLIGHT)
			return "floodlight";
		else if(BlockTypes_MetalDevice1.values()[meta]==BlockTypes_MetalDevice1.BELLJAR)
			return "belljar";
		//		else if(BlockTypes_MetalDevice1.values()[meta]==BlockTypes_MetalDevice1.TESLA_COIL)
		//			return "teslaCoil";*/
		return null;
	}

	@Override
	protected BlockStateContainer createBlockState()
	{
		BlockStateContainer base = super.createBlockState();
		IUnlistedProperty[] unlisted = (base instanceof ExtendedBlockState)?((ExtendedBlockState)base).getUnlistedProperties().toArray(new IUnlistedProperty[0]):new IUnlistedProperty[0];
		unlisted = Arrays.copyOf(unlisted, unlisted.length+1);
		unlisted[unlisted.length-1] = IEProperties.CONNECTIONS;
		return new ExtendedBlockState(this, base.getProperties().toArray(new IProperty[0]), unlisted);
	}
	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		state = super.getExtendedState(state, world, pos);
		/*TileEntity tile = world.getTileEntity(pos);
		if(tile instanceof TileEntityImmersiveConnectable&&state instanceof IExtendedBlockState)
			state = ((IExtendedBlockState)state).withProperty(IEProperties.CONNECTIONS, ((TileEntityImmersiveConnectable)tile).genConnBlockstate());
		if(tile instanceof TileEntityElectricLantern)
			state = state.withProperty(IEProperties.BOOLEANS[0], ((TileEntityElectricLantern) tile).active);
		if(tile instanceof TileEntityFloodlight)
			state = state.withProperty(IEProperties.BOOLEANS[0], ((TileEntityFloodlight) tile).active);*/
		return state;
	}

	/*@Override
	public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side)
	{
		TileEntity tile = world.getTileEntity(pos);
		if(tile instanceof TileEntityTeslaCoil)
			return !((TileEntityTeslaCoil)tile).dummy;
		else if(tile instanceof TileEntityTurret)
			return !((TileEntityTurret)tile).dummy;
		return !(tile instanceof TileEntityElectricLantern || tile instanceof TileEntityChargingStation);
	}*/



	@Override
	public TileEntity createNewTileEntity(World world, int meta)
	{
		/*switch(BlockType_MetalDevice.values()[meta])
		{
			
		}*/
		return null;
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos)
	{
		super.neighborChanged(state, world, pos, block, fromPos);
		//if (world.getBlockState(pos).getValue(property)==BlockTypes_MetalDevice1.FLUID_PIPE)
			//TileEntityFluidPipe.indirectConnections.clear();
	}
	
	@Override
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
	{
		//TileEntityFluidPipe.indirectConnections.clear();
		return super.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer);
	}

	@Override
	public boolean allowHammerHarvest(IBlockState state)
	{
		return true;
	}
	
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state)
	{
		/*if (state.getValue(property)==BlockTypes_MetalDevice1.FLUID_PIPE)
		{
			TileEntity te = world.getTileEntity(pos);
			if (te instanceof TileEntityFluidPipe)
			{
				TileEntityFluidPipe here = (TileEntityFluidPipe) te;
				for (int i = 0;i<6;i++)
					if (here.sideConfig[i]==-1)
					{
						EnumFacing f = EnumFacing.VALUES[i];

						TileEntity there = world.getTileEntity(pos.offset(f));
						if (there instanceof TileEntityFluidPipe)
							((TileEntityFluidPipe) there).toggleSide(f.getOpposite().ordinal());
					}
			}
		}*/
		super.breakBlock(world, pos, state);
	}

}

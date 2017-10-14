package ferro2000.immersivetech.common.blocks.metal;

import java.util.Arrays;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.energy.wires.TileEntityImmersiveConnectable;
import blusunrize.immersiveengineering.client.models.IOBJModelCallback;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityConnectorRedstone;
import ferro2000.immersivetech.common.blocks.BlockITTileProvider;
import ferro2000.immersivetech.common.blocks.ItemBlockITBase;
import ferro2000.immersivetech.common.blocks.metal.tileentities.TileEntityTimer;
import ferro2000.immersivetech.common.blocks.metal.types.BlockType_Connectors;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

public class BlockConnectors extends BlockITTileProvider<BlockType_Connectors> {
	
	public BlockConnectors()
	{
		super("connectors", Material.IRON, PropertyEnum.create("type", BlockType_Connectors.class), ItemBlockITBase.class, IEProperties.FACING_ALL, IEProperties.BOOLEANS[0], IEProperties.BOOLEANS[1], IEProperties.MULTIBLOCKSLAVE, IOBJModelCallback.PROPERTY);
		setHardness(3.0F);
		setResistance(15.0F);
		lightOpacity = 0;
		setMetaBlockLayer(BlockType_Connectors.CONNECTORS_TIMER.getMeta(), BlockRenderLayer.CUTOUT, BlockRenderLayer.TRANSLUCENT, BlockRenderLayer.SOLID);
		setAllNotNormalBlock();
	}
	
	@Override
	public boolean useCustomStateMapper()
	{
		return true;
	}
	
	@Override
	protected BlockStateContainer createBlockState()
	{
		BlockStateContainer base = super.createBlockState();
		IUnlistedProperty[] unlisted = (base instanceof ExtendedBlockState) ? ((ExtendedBlockState) base).getUnlistedProperties().toArray(new IUnlistedProperty[0]) : new IUnlistedProperty[0];
		unlisted = Arrays.copyOf(unlisted, unlisted.length+1);
		unlisted[unlisted.length-1] = IEProperties.CONNECTIONS;
		return new ExtendedBlockState(this, base.getProperties().toArray(new IProperty[0]), unlisted);
	}
	
	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		state = super.getExtendedState(state, world, pos);
		if(state instanceof IExtendedBlockState)
		{
			IExtendedBlockState ext = (IExtendedBlockState) state;
			TileEntity te = world.getTileEntity(pos);
			if (!(te instanceof TileEntityImmersiveConnectable))
				return state;
			state = ext.withProperty(IEProperties.CONNECTIONS, ((TileEntityImmersiveConnectable)te).genConnBlockstate());
		}
		return state;
	}
	
	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos)
	{
		super.neighborChanged(state, world, pos, blockIn, fromPos);
		TileEntity te = world.getTileEntity(pos);
		
		if(te instanceof TileEntityConnectorRedstone)
		{
			TileEntityConnectorRedstone connector = (TileEntityConnectorRedstone) te;
			if(world.isAirBlock(pos.offset(connector.facing)))
			{
				this.dropBlockAsItem(connector.getWorld(), pos, world.getBlockState(pos), 0);
				connector.getWorld().setBlockToAir(pos);
				return;
			}
			if (connector.isRSInput())
				connector.rsDirty = true;
		}
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		
		switch(BlockType_Connectors.values()[meta]){
			case CONNECTORS_TIMER:
				return new TileEntityTimer();
		}	
		
		return null;
	}
	
	@Override
	public boolean allowHammerHarvest(IBlockState state)
	{
		return true;
	}

}

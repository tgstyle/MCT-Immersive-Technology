package ferro2000.immersivetech.client;

import java.util.Locale;

import javax.annotation.Nonnull;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.IECustomStateMapper;
import blusunrize.immersiveengineering.client.models.obj.IEOBJLoader;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IIEMetaBlock;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.compat.IECompatModule;
import ferro2000.immersivetech.ImmersiveTech;
import ferro2000.immersivetech.common.CommonProxy;
import ferro2000.immersivetech.common.ITContent;
import ferro2000.immersivetech.common.blocks.BlockITFluid;
import ferro2000.immersivetech.common.items.ItemITBase;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.common.registry.GameData;

public class ClientProxy extends CommonProxy{
	
	@Override
	public void preInit(){
		
		ClientUtils.mc().getFramebuffer().enableStencil();
		ModelLoaderRegistry.registerLoader(IEOBJLoader.instance);
		OBJLoader.INSTANCE.addDomain("immersivetech");
		IEOBJLoader.instance.addDomain("immersivetech");
		
	}
	
	@Override
	public void preInitEnd(){
		for(Block block : ITContent.registeredITBlocks)
		{
			Item blockItem = Item.getItemFromBlock(block);
			final ResourceLocation loc = GameData.getBlockRegistry().getNameForObject(block);
			if(block instanceof IIEMetaBlock)
			{
				IIEMetaBlock ieMetaBlock = (IIEMetaBlock) block;
				if(ieMetaBlock.useCustomStateMapper())
					ModelLoader.setCustomStateMapper(block, IECustomStateMapper.getStateMapper(ieMetaBlock));
				ModelLoader.setCustomMeshDefinition(blockItem, new ItemMeshDefinition()
				{
					@Override
					public ModelResourceLocation getModelLocation(ItemStack stack)
					{
						return new ModelResourceLocation(loc, "inventory");
					}
				});
				for(int meta = 0; meta < ieMetaBlock.getMetaEnums().length; meta++)
				{
					String location = loc.toString();
					String prop = ieMetaBlock.appendPropertiesToState() ? ("inventory," + ieMetaBlock.getMetaProperty().getName() + "=" + ieMetaBlock.getMetaEnums()[meta].toString().toLowerCase(Locale.US)) : null;
					if(ieMetaBlock.useCustomStateMapper())
					{
						String custom = ieMetaBlock.getCustomStateMapping(meta, true);
						if(custom != null)
							location += "_" + custom;
					}
					try
					{
						ModelLoader.setCustomModelResourceLocation(blockItem, meta, new ModelResourceLocation(location, prop));
					} catch(NullPointerException npe)
					{
						throw new RuntimeException("WELP! apparently " + ieMetaBlock + " lacks an item!", npe);
					}
				}
			} else if(block instanceof BlockITFluid)
				mapFluidState(block, ((BlockITFluid) block).getFluid());
			else
				ModelLoader.setCustomModelResourceLocation(blockItem, 0, new ModelResourceLocation(loc, "inventory"));
		}

		for(Item item : ITContent.registeredITItems)
		{
			if(item instanceof ItemITBase)
			{
				ItemITBase ipMetaItem = (ItemITBase) item;
				if(ipMetaItem.registerSubModels && ipMetaItem.getSubNames() != null && ipMetaItem.getSubNames().length > 0)
				{
					for(int meta = 0; meta < ipMetaItem.getSubNames().length; meta++)
					{
						ResourceLocation loc = new ResourceLocation("immersivetech", ipMetaItem.itemName + "/" + ipMetaItem.getSubNames()[meta]);

						ModelBakery.registerItemVariants(ipMetaItem, loc);
						ModelLoader.setCustomModelResourceLocation(ipMetaItem, meta, new ModelResourceLocation(loc, "inventory"));
					}
				}
				else
				{
					final ResourceLocation loc = new ResourceLocation("immersivetech", ipMetaItem.itemName);
					ModelBakery.registerItemVariants(ipMetaItem, loc);
					ModelLoader.setCustomMeshDefinition(ipMetaItem, new ItemMeshDefinition()
					{
						@Override
						public ModelResourceLocation getModelLocation(ItemStack stack)
						{
							return new ModelResourceLocation(loc, "inventory");
						}
					});
				}
			} 
			else
			{
				final ResourceLocation loc = GameData.getItemRegistry().getNameForObject(item);
				ModelBakery.registerItemVariants(item, loc);
				ModelLoader.setCustomMeshDefinition(item, new ItemMeshDefinition()
				{
					@Override
					public ModelResourceLocation getModelLocation(ItemStack stack)
					{
						return new ModelResourceLocation(loc, "inventory");
					}
				});
			}
		}
		
	}
	
	@Override
	public void init(){
		
	}
	
	@Override
	public void postInit(){
		
	}
	
	private static void mapFluidState(Block block, Fluid fluid)
	{
		Item item = Item.getItemFromBlock(block);
		FluidStateMapper mapper = new FluidStateMapper(fluid);
		if(item != null)
		{
			ModelLoader.registerItemVariants(item);
			ModelLoader.setCustomMeshDefinition(item, mapper);
		}
		ModelLoader.setCustomStateMapper(block, mapper);
	}

	static class FluidStateMapper extends StateMapperBase implements ItemMeshDefinition
	{
		public final ModelResourceLocation location;

		public FluidStateMapper(Fluid fluid)
		{
			this.location = new ModelResourceLocation(ImmersiveTech.MODID + ":fluid_block", fluid.getName());
		}

		@Nonnull
		@Override
		protected ModelResourceLocation getModelResourceLocation(@Nonnull IBlockState state)
		{
			return location;
		}

		@Nonnull
		@Override
		public ModelResourceLocation getModelLocation(@Nonnull ItemStack stack)
		{
			return location;
		}
	}

}

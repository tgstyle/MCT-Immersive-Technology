package mctmods.immersivetechnology.client;

import java.util.Locale;

import javax.annotation.Nonnull;

import blusunrize.immersiveengineering.api.ManualHelper;
import blusunrize.immersiveengineering.api.ManualPageMultiblock;
import blusunrize.immersiveengineering.api.energy.wires.WireApi;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.IECustomStateMapper;
import blusunrize.immersiveengineering.client.models.obj.IEOBJLoader;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IIEMetaBlock;
import blusunrize.lib.manual.ManualPages;
import mctmods.immersivetechnology.ImmersiveTech;
import mctmods.immersivetechnology.api.ITUtils;
import mctmods.immersivetechnology.client.render.TileRenderSteamTurbine;
import mctmods.immersivetechnology.common.CommonProxy;
import mctmods.immersivetechnology.common.ITContent;
import mctmods.immersivetechnology.common.blocks.BlockITFluid;
import mctmods.immersivetechnology.common.blocks.connectors.types.BlockType_Connectors;
import mctmods.immersivetechnology.common.blocks.metal.multiblocks.MultiblockAlternator;
import mctmods.immersivetechnology.common.blocks.metal.multiblocks.MultiblockBoiler;
import mctmods.immersivetechnology.common.blocks.metal.multiblocks.MultiblockDistiller;
import mctmods.immersivetechnology.common.blocks.metal.multiblocks.MultiblockSolarReflector;
import mctmods.immersivetechnology.common.blocks.metal.multiblocks.MultiblockSolarTower;
import mctmods.immersivetechnology.common.blocks.metal.multiblocks.MultiblockSteamTurbine;
import mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntitySteamTurbineMaster;
import mctmods.immersivetechnology.common.blocks.metal.types.BlockType_MetalDevice;
import mctmods.immersivetechnology.common.blocks.stone.multiblocks.MultiblockCokeOvenAdvanced;
import mctmods.immersivetechnology.common.items.ItemITBase;
import mctmods.immersivetechnology.common.util.network.MessageRequestUpdate;
import mctmods.immersivetechnology.common.util.network.MessageStopSound;
import mctmods.immersivetechnology.common.util.network.MessageTileSync;
import mctmods.immersivetechnology.common.util.sound.ITSoundHandler;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientProxy extends CommonProxy {
	public static final String CAT_IT = "it";

	@Override
	public void preInit() {
		ClientUtils.mc().getFramebuffer().enableStencil();
		ModelLoaderRegistry.registerLoader(IEOBJLoader.instance);
		OBJLoader.INSTANCE.addDomain(ImmersiveTechnology.MODID);
		IEOBJLoader.instance.addDomain(ImmersiveTechnology.MODID);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent()
	public void PlayerChangedDimensions(PlayerEvent.PlayerChangedDimensionEvent e) {
		ITSoundHandler.DeleteAllSounds();
	}

	@SubscribeEvent()
	public void PlayerLeftSession(PlayerEvent.PlayerLoggedOutEvent e) {
		ITSoundHandler.DeleteAllSounds();
	}

	@SubscribeEvent()
	public void PlayerDisconnected(FMLNetworkEvent.ClientDisconnectionFromServerEvent e) {
		ITSoundHandler.DeleteAllSounds();
	}

	@SubscribeEvent
	public void onClientTick(TickEvent.ClientTickEvent event) {
		if (!ITUtils.REMOVE_FROM_TICKING.isEmpty() && event.phase == TickEvent.Phase.END) {
			Minecraft.getMinecraft().world.tickableTileEntities.removeAll(ITUtils.REMOVE_FROM_TICKING);
			ITUtils.REMOVE_FROM_TICKING.clear();
		}
	}

	
	/*
	@author BluSunrize
	*/

	@SuppressWarnings("deprecation")
	@SubscribeEvent
	public static void registerModels(ModelRegistryEvent evt) {
		WireApi.registerConnectorForRender("conn_timer", new ResourceLocation("immersivetech:block/connector/connectors_timer.obj.ie"), null);
		WireApi.registerConnectorForRender("conn_con_net", new ResourceLocation("immersivetech:block/connector/connectors_con_net.obj.ie"), null);

		for(Block block : ITContent.registeredITBlocks) {
			final ResourceLocation loc = Block.REGISTRY.getNameForObject(block);
			Item blockItem = Item.getItemFromBlock(block);
			if(blockItem==null)	throw new RuntimeException("ITEMBLOCK for" + loc + " : " + block + " IS NULL");
			if(block instanceof IIEMetaBlock) {
				IIEMetaBlock ieMetaBlock = (IIEMetaBlock)block;
				if(ieMetaBlock.useCustomStateMapper()) ModelLoader.setCustomStateMapper(block, IECustomStateMapper.getStateMapper(ieMetaBlock));
				ModelLoader.setCustomMeshDefinition(blockItem, new ItemMeshDefinition() {
					@Override
					public ModelResourceLocation getModelLocation(ItemStack stack) {
						return new ModelResourceLocation(loc, "inventory");
					}
				});
				for(int meta = 0; meta < ieMetaBlock.getMetaEnums().length; meta++) {
					String location = loc.toString();
					String prop = ieMetaBlock.appendPropertiesToState() ? ("inventory," + ieMetaBlock.getMetaProperty().getName() + "=" + ieMetaBlock.getMetaEnums()[meta].toString().toLowerCase(Locale.US)): null;
					if(ieMetaBlock.useCustomStateMapper()) {
						String custom = ieMetaBlock.getCustomStateMapping(meta, true);
						if(custom != null) location += "_" + custom;
					} try {
						ModelLoader.setCustomModelResourceLocation(blockItem, meta, new ModelResourceLocation(location, prop));
					} catch (NullPointerException npe) {
						throw new RuntimeException("WELP! apparently " + ieMetaBlock + " lacks an item!", npe);
					}
				}
			} else if(block instanceof BlockITFluid) {
				mapFluidState(block, ((BlockITFluid) block).getFluid());
			} else {
				ModelLoader.setCustomModelResourceLocation(blockItem, 0, new ModelResourceLocation(loc, "inventory"));
			}
		}

		for(Item item : ITContent.registeredITItems) {
			if(item instanceof ItemBlock) continue;
			if(item instanceof ItemITBase) {
				ItemITBase ipMetaItem = (ItemITBase) item;
				if(ipMetaItem.registerSubModels && ipMetaItem.getSubNames() != null && ipMetaItem.getSubNames().length > 0) {
					for(int meta = 0; meta < ipMetaItem.getSubNames().length; meta++) {
						ResourceLocation loc = new ResourceLocation(ImmersiveTechnology.MODID, ipMetaItem.itemName + "/" + ipMetaItem.getSubNames()[meta]);
						ModelBakery.registerItemVariants(ipMetaItem, loc);
						ModelLoader.setCustomModelResourceLocation(ipMetaItem, meta, new ModelResourceLocation(loc, "inventory"));
					}
				} else {
					final ResourceLocation loc = new ResourceLocation(ImmersiveTechnology.MODID, ipMetaItem.itemName);
					ModelBakery.registerItemVariants(ipMetaItem, loc);
					ModelLoader.setCustomMeshDefinition(ipMetaItem, new ItemMeshDefinition() {
						@Override
						public ModelResourceLocation getModelLocation(ItemStack stack) {
							return new ModelResourceLocation(loc, "inventory");
						}
					});
				}
			} else {
				final ResourceLocation loc = Item.REGISTRY.getNameForObject(item);
				ModelBakery.registerItemVariants(item, loc);
				ModelLoader.setCustomMeshDefinition(item, new ItemMeshDefinition() {
					@Override
					public ModelResourceLocation getModelLocation(ItemStack stack) {
						return new ModelResourceLocation(loc, "inventory");
					}
				});
			}
		}
	}

	@Override
	public void preInitEnd() {
	}

	@Override
	public void init() {
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySteamTurbineMaster.class, new TileRenderSteamTurbine());
		ImmersiveTech.packetHandler.registerMessage(MessageTileSync.HandlerClient.class, MessageTileSync.class, 0, Side.CLIENT);
		//has to be here as well because this one is used when playing Singleplayer, go figure
		ImmersiveTechnology.packetHandler.registerMessage(MessageTileSync.HandlerServer.class, MessageTileSync.class, 0, Side.SERVER);
		ImmersiveTechnology.packetHandler.registerMessage(MessageStopSound.HandlerClient.class, MessageStopSound.class, 1, Side.CLIENT);
		ImmersiveTechnology.packetHandler.registerMessage(MessageRequestUpdate.HandlerClient.class, MessageRequestUpdate.class, 2, Side.CLIENT);
		ImmersiveTechnology.packetHandler.registerMessage(MessageRequestUpdate.HandlerServer.class, MessageRequestUpdate.class, 2, Side.SERVER);

	}

	@Override
	public void postInit() {
		ManualHelper.addEntry("advancedCokeOven", CAT_IT, new ManualPageMultiblock(ManualHelper.getManual(), "advancedCokeOven0", MultiblockCokeOvenAdvanced.instance), new ManualPages.Text(ManualHelper.getManual(), "advancedCokeOven1"), new ManualPages.Crafting(ManualHelper.getManual(), "advancedCokeOven2", new ItemStack(ITContent.blockMetalDevice, 1, BlockType_MetalDevice.COKE_OVEN_PREHEATER.getMeta())));
		ManualHelper.addEntry("alternator", CAT_IT, new ManualPageMultiblock(ManualHelper.getManual(), "alternator0", MultiblockAlternator.instance), new ManualPages.Text(ManualHelper.getManual(), "alternator1"), new ManualPages.Image(ManualHelper.getManual(), "alternator2", "immersivetech:textures/misc/alternator.png;0;0;110;50"));
		ManualHelper.addEntry("boiler", CAT_IT, new ManualPageMultiblock(ManualHelper.getManual(), "boiler0", MultiblockBoiler.instance), new ManualPages.Text(ManualHelper.getManual(), "boiler1"), new ManualPages.Text(ManualHelper.getManual(), "boiler2"));
		ManualHelper.addEntry("distiller", CAT_IT, new ManualPageMultiblock(ManualHelper.getManual(), "distiller0", MultiblockDistiller.instance), new ManualPages.Text(ManualHelper.getManual(), "distiller1"));
		ManualHelper.addEntry("solarTower", CAT_IT, new ManualPageMultiblock(ManualHelper.getManual(), "solarTower0", MultiblockSolarTower.instance), new ManualPages.Text(ManualHelper.getManual(), "solarTower1"), new ManualPageMultiblock(ManualHelper.getManual(), "solarTower2", MultiblockSolarReflector.instance), new ManualPages.Text(ManualHelper.getManual(), "solarTower3"));
		ManualHelper.addEntry("steamTurbine", CAT_IT, new ManualPageMultiblock(ManualHelper.getManual(), "steamTurbine0", MultiblockSteamTurbine.instance), new ManualPages.Text(ManualHelper.getManual(), "steamTurbine1"), new ManualPages.Text(ManualHelper.getManual(), "steamTurbine2"));
		ManualHelper.addEntry("redstone", CAT_IT, new ManualPages.Crafting(ManualHelper.getManual(), "redstone0", new ItemStack(ITContent.blockConnectors, 1, BlockType_Connectors.CONNECTORS_TIMER.getMeta())));
	}

	private static void mapFluidState(Block block, Fluid fluid)	{
		Item item = Item.getItemFromBlock(block);
		FluidStateMapper mapper = new FluidStateMapper(fluid);
		if(item != Items.AIR) {
			ModelLoader.registerItemVariants(item);
			ModelLoader.setCustomMeshDefinition(item, mapper);
		}
		ModelLoader.setCustomStateMapper(block, mapper);
	}

	static class FluidStateMapper extends StateMapperBase implements ItemMeshDefinition	{
		public final ModelResourceLocation location;

		public FluidStateMapper(Fluid fluid) {
			this.location = new ModelResourceLocation(ImmersiveTechnology.MODID + ":fluid_block", fluid.getName());
		}

		@Nonnull
		@Override
		protected ModelResourceLocation getModelResourceLocation(@Nonnull IBlockState state) {
			return location;
		}

		@Nonnull
		@Override
		public ModelResourceLocation getModelLocation(@Nonnull ItemStack stack)	{
			return location;
		}
	}
}
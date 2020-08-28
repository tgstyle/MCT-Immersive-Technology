package mctmods.immersivetechnology.client;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.ManualHelper;
import blusunrize.immersiveengineering.api.ManualPageMultiblock;
import blusunrize.immersiveengineering.api.energy.wires.WireApi;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.IECustomStateMapper;
import blusunrize.immersiveengineering.client.models.obj.IEOBJLoader;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGuiTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IIEMetaBlock;
import blusunrize.immersiveengineering.common.items.ItemEarmuffs;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.lib.manual.ManualPages;
import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.api.ITLib;
import mctmods.immersivetechnology.api.ITUtils;
import mctmods.immersivetechnology.client.gui.GuiBoiler;
import mctmods.immersivetechnology.client.gui.GuiCokeOvenAdvanced;
import mctmods.immersivetechnology.client.gui.GuiDistiller;
import mctmods.immersivetechnology.client.gui.GuiFluidValve;
import mctmods.immersivetechnology.client.gui.GuiLoadController;
import mctmods.immersivetechnology.client.gui.GuiSolarTower;
import mctmods.immersivetechnology.client.gui.GuiStackLimiter;
import mctmods.immersivetechnology.client.gui.GuiTimer;
import mctmods.immersivetechnology.client.gui.GuiTrashItem;
import mctmods.immersivetechnology.client.models.ModelConfigurableSides;
import mctmods.immersivetechnology.client.render.TileRenderBarrelOpen;
import mctmods.immersivetechnology.client.render.TileRenderSteamTurbine;
import mctmods.immersivetechnology.client.render.TileRenderSteelSheetmetalTank;
import mctmods.immersivetechnology.client.render.TileRendererGasTurbine;
import mctmods.immersivetechnology.common.CommonProxy;
import mctmods.immersivetechnology.common.Config.ITConfig.Machines.Multiblock;
import mctmods.immersivetechnology.common.ITContent;
import mctmods.immersivetechnology.common.blocks.BlockITFluid;
import mctmods.immersivetechnology.common.blocks.BlockValve.BlockType_Valve;
import mctmods.immersivetechnology.common.blocks.connectors.tileentities.TileEntityTimer;
import mctmods.immersivetechnology.common.blocks.connectors.types.BlockType_Connectors;
import mctmods.immersivetechnology.common.blocks.metal.multiblocks.*;
import mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntityBarrelOpen;
import mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntityBoilerMaster;
import mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntityDistillerMaster;
import mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntityGasTurbineMaster;
import mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntitySolarTowerMaster;
import mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntitySteamTurbineMaster;
import mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntitySteelSheetmetalTankMaster;
import mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntityTrashItem;
import mctmods.immersivetechnology.common.blocks.metal.types.BlockType_MetalBarrel;
import mctmods.immersivetechnology.common.blocks.metal.types.BlockType_MetalDevice;
import mctmods.immersivetechnology.common.blocks.stone.multiblocks.MultiblockCokeOvenAdvanced;
import mctmods.immersivetechnology.common.blocks.stone.tileentities.TileEntityCokeOvenAdvancedMaster;
import mctmods.immersivetechnology.common.items.ItemITBase;
import mctmods.immersivetechnology.common.tileentities.TileEntityFluidValve;
import mctmods.immersivetechnology.common.tileentities.TileEntityLoadController;
import mctmods.immersivetechnology.common.tileentities.TileEntityStackLimiter;
import mctmods.immersivetechnology.common.util.ITLogger;
import mctmods.immersivetechnology.common.util.network.BinaryMessageTileSync;
import mctmods.immersivetechnology.common.util.network.MessageRequestUpdate;
import mctmods.immersivetechnology.common.util.network.MessageStopSound;
import mctmods.immersivetechnology.common.util.network.MessageTileSync;
import mctmods.immersivetechnology.common.util.sound.ITSoundHandler;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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

import javax.annotation.Nonnull;
import java.util.Locale;

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

		ModelLoaderRegistry.registerLoader(new ModelConfigurableSides.Loader());
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
		if(!ITUtils.REMOVE_FROM_TICKING.isEmpty() && event.phase == TickEvent.Phase.END) {
			World world = Minecraft.getMinecraft().world;
			if(world == null) ITLogger.warn("ClientProxy has tried to access null world! This shouldn't normally happen...");
			else {
				world.tickableTileEntities.removeAll(ITUtils.REMOVE_FROM_TICKING);
				ITUtils.REMOVE_FROM_TICKING.clear();
			}
		}
		calculateVolume();
	}

	public static float volumeAdjustment = 1;

	public void calculateVolume() {
		float prevVolume = volumeAdjustment;
		EntityPlayerSP player = ClientUtils.mc().player;
		if(player == null) return;
		ItemStack stack = player.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
		if(!stack.isEmpty()) {
			if(IEContent.itemEarmuffs.equals(stack.getItem())) volumeAdjustment = ItemEarmuffs.getVolumeMod(stack);
			 else if(ItemNBTHelper.hasKey(stack, "IE:Earmuffs")) {
				stack = ItemNBTHelper.getItemStack(stack, "IE:Earmuffs");
				if(!stack.isEmpty() && IEContent.itemEarmuffs.equals(stack.getItem())) volumeAdjustment = ItemEarmuffs.getVolumeMod(stack);
				else volumeAdjustment = 1;
			} else volumeAdjustment = 1;
		} else volumeAdjustment = 1;

		if(prevVolume != volumeAdjustment) ITSoundHandler.UpdateAllVolumes();
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
			if(blockItem == null)	throw new RuntimeException("ITEMBLOCK for" + loc + " : " + block + " IS NULL");
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
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityGasTurbineMaster.class, new TileRendererGasTurbine());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBarrelOpen.class, new TileRenderBarrelOpen());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySteelSheetmetalTankMaster.class, new TileRenderSteelSheetmetalTank());
		ImmersiveTechnology.packetHandler.registerMessage(MessageTileSync.HandlerClient.class, MessageTileSync.class, 0, Side.CLIENT);
		//has to be here as well because this one is used when playing Singleplayer, go figure
		ImmersiveTechnology.packetHandler.registerMessage(MessageTileSync.HandlerServer.class, MessageTileSync.class, 0, Side.SERVER);
		ImmersiveTechnology.packetHandler.registerMessage(MessageStopSound.HandlerClient.class, MessageStopSound.class, 1, Side.CLIENT);
		ImmersiveTechnology.packetHandler.registerMessage(MessageRequestUpdate.HandlerClient.class, MessageRequestUpdate.class, 2, Side.CLIENT);
		ImmersiveTechnology.packetHandler.registerMessage(MessageRequestUpdate.HandlerServer.class, MessageRequestUpdate.class, 2, Side.SERVER);
		ImmersiveTechnology.packetHandler.registerMessage(BinaryMessageTileSync.HandlerClient.class, BinaryMessageTileSync.class, 3, Side.CLIENT);
		ImmersiveTechnology.packetHandler.registerMessage(BinaryMessageTileSync.HandlerServer.class, BinaryMessageTileSync.class, 3, Side.SERVER);
	}

	@Override
	public void postInit() {
		if(Multiblock.enable_advancedCokeOven) {
			ManualHelper.addEntry("advancedCokeOven", CAT_IT, new ManualPageMultiblock(ManualHelper.getManual(), "advancedCokeOven0", MultiblockCokeOvenAdvanced.instance), new ManualPages.Text(ManualHelper.getManual(), "advancedCokeOven1"), new ManualPages.Crafting(ManualHelper.getManual(), "advancedCokeOven2", new ItemStack(ITContent.blockMetalDevice, 1, BlockType_MetalDevice.COKE_OVEN_PREHEATER.getMeta())));
		}
		if(Multiblock.enable_boiler) {
			ManualHelper.addEntry("boiler", CAT_IT, new ManualPageMultiblock(ManualHelper.getManual(), "boiler0", MultiblockBoiler.instance), new ManualPages.Text(ManualHelper.getManual(), "boiler1"), new ManualPages.Text(ManualHelper.getManual(), "boiler2"));
		}
		if(Multiblock.enable_distiller) {
			ManualHelper.addEntry("distiller", CAT_IT, new ManualPageMultiblock(ManualHelper.getManual(), "distiller0", MultiblockDistiller.instance), new ManualPages.Text(ManualHelper.getManual(), "distiller1"));
		}
		if(Multiblock.enable_solarTower) {
			ManualHelper.addEntry("solarTower", CAT_IT, new ManualPageMultiblock(ManualHelper.getManual(), "solarTower0", MultiblockSolarTower.instance), new ManualPages.Text(ManualHelper.getManual(), "solarTower1"), new ManualPageMultiblock(ManualHelper.getManual(), "solarTower2", MultiblockSolarReflector.instance), new ManualPages.Text(ManualHelper.getManual(), "solarTower3"));
		}
		if (Multiblock.enable_gasTurbine || Multiblock.enable_steamTurbine) {
			ManualHelper.addEntry("alternator", CAT_IT, new ManualPageMultiblock(ManualHelper.getManual(), "alternator0", MultiblockAlternator.instance), new ManualPages.Text(ManualHelper.getManual(), "alternator1"), new ManualPages.Image(ManualHelper.getManual(), "alternator2", "immersivetech:textures/misc/alternator.png;0;0;110;50"));
		}
		if(Multiblock.enable_steamTurbine) {
			ManualHelper.addEntry("steamTurbine", CAT_IT, new ManualPageMultiblock(ManualHelper.getManual(), "steamTurbine0", MultiblockSteamTurbine.instance), new ManualPages.Text(ManualHelper.getManual(), "steamTurbine1"), new ManualPages.Text(ManualHelper.getManual(), "steamTurbine2"));
		}
		if(Multiblock.enable_gasTurbine) {
			ManualHelper.addEntry("gasTurbine", CAT_IT, new ManualPageMultiblock(ManualHelper.getManual(), "gasTurbine0", MultiblockGasTurbine.instance), new ManualPages.Text(ManualHelper.getManual(), "gasTurbine1"), new ManualPages.Text(ManualHelper.getManual(), "gasTurbine2"), new ManualPages.Text(ManualHelper.getManual(), "gasTurbine3"));
		}

		ManualHelper.addEntry("fluidValve", CAT_IT, new ManualPages.Crafting(ManualHelper.getManual(), "fluidValve0", new ItemStack(ITContent.blockValve, 1, BlockType_Valve.FLUID_VALVE.getMeta())));
		ManualHelper.addEntry("loadController", CAT_IT, new ManualPages.Crafting(ManualHelper.getManual(), "loadController0", new ItemStack(ITContent.blockValve, 1, BlockType_Valve.LOAD_CONTROLLER.getMeta())));
		ManualHelper.addEntry("redstone", CAT_IT, new ManualPages.Crafting(ManualHelper.getManual(), "redstone0", new ItemStack(ITContent.blockConnectors, 1, BlockType_Connectors.CONNECTORS_TIMER.getMeta())));
		ManualHelper.addEntry("openBarrel", CAT_IT, new ManualPages.Crafting(ManualHelper.getManual(), "openBarrel0", new ItemStack(ITContent.blockMetalBarrel, 1, BlockType_MetalBarrel.BARREL_OPEN.getMeta())));
		ManualHelper.addEntry("stackLimiter", CAT_IT, new ManualPages.Crafting(ManualHelper.getManual(), "stackLimiter0", new ItemStack(ITContent.blockValve, 1, BlockType_Valve.STACK_LIMITER.getMeta())));
		ManualHelper.addEntry("steelBarrel", CAT_IT, new ManualPages.Crafting(ManualHelper.getManual(), "steelBarrel0", new ItemStack(ITContent.blockMetalBarrel, 2, BlockType_MetalBarrel.BARREL_STEEL.getMeta())));
		ManualHelper.addEntry("steelTank", CAT_IT, new ManualPageMultiblock(ManualHelper.getManual(), "steelTank0", MultiblockSteelSheetmetalTank.instance), new ManualPages.Text(ManualHelper.getManual(), "steelTank1"));
		if (Multiblock.enable_coolingTower)
			ManualHelper.addEntry("coolingTower", CAT_IT, new ManualPageMultiblock(ManualHelper.getManual(), "coolingTower0", MultiblockCoolingTower.instance), new ManualPages.Text(ManualHelper.getManual(), "coolingTower1"));
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

	static {
		IEApi.renderCacheClearers.add(ModelConfigurableSides.modelCache::clear);
	}

	@Override
	public void clearRenderCaches() {
		for(Runnable r : IEApi.renderCacheClearers) r.run();
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
		if(tile instanceof IGuiTile) {
			Object gui = null;
			if(ID == ITLib.GUIID_Boiler && tile instanceof TileEntityBoilerMaster) gui = new GuiBoiler(player.inventory, (TileEntityBoilerMaster) tile);
			if(ID == ITLib.GUIID_Coke_oven_advanced && tile instanceof TileEntityCokeOvenAdvancedMaster) gui = new GuiCokeOvenAdvanced(player.inventory, (TileEntityCokeOvenAdvancedMaster) tile);
			if(ID == ITLib.GUIID_Distiller && tile instanceof TileEntityDistillerMaster) gui = new GuiDistiller(player.inventory, (TileEntityDistillerMaster) tile);
			if(ID == ITLib.GUIID_Solar_Tower && tile instanceof TileEntitySolarTowerMaster) gui = new GuiSolarTower(player.inventory, (TileEntitySolarTowerMaster) tile);
			if(ID == ITLib.GUIID_Timer && tile instanceof TileEntityTimer) gui = new GuiTimer(player.inventory, (TileEntityTimer) tile);
			if(ID == ITLib.GUIID_Trash_Item && tile instanceof TileEntityTrashItem) gui = new GuiTrashItem(player.inventory, (TileEntityTrashItem) tile);
			if(ID == ITLib.GUIID_Fluid_Valve && tile instanceof TileEntityFluidValve) gui = new GuiFluidValve((TileEntityFluidValve) tile);
			if(ID == ITLib.GUIID_Load_Controller && tile instanceof TileEntityLoadController) gui = new GuiLoadController((TileEntityLoadController) tile);
			if(ID == ITLib.GUIID_Stack_Limiter && tile instanceof TileEntityStackLimiter) gui = new GuiStackLimiter((TileEntityStackLimiter) tile);
			return gui;
		}
		return null;
	}

}
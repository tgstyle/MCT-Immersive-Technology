package mctmods.immersivetechnology.common;

import javax.annotation.Nonnull;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGuiTile;
import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.api.ITUtils;
import mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntityBoilerMaster;
import mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntityDistillerMaster;
import mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntitySolarTowerMaster;
import mctmods.immersivetechnology.common.blocks.stone.tileentities.TileEntityCokeOvenAdvancedMaster;
import mctmods.immersivetechnology.api.ITLib;
import mctmods.immersivetechnology.client.gui.GuiBoiler;
import mctmods.immersivetechnology.client.gui.GuiCokeOvenAdvanced;
import mctmods.immersivetechnology.client.gui.GuiDistiller;
import mctmods.immersivetechnology.client.gui.GuiSolarTower;
import mctmods.immersivetechnology.client.gui.GuiTimer;
import mctmods.immersivetechnology.client.gui.GuiTrashItem;
import mctmods.immersivetechnology.common.blocks.connectors.tileentities.TileEntityTimer;
import mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntityTrashItem;
import mctmods.immersivetechnology.common.gui.ContainerBoiler;
import mctmods.immersivetechnology.common.gui.ContainerCokeOvenAdvanced;
import mctmods.immersivetechnology.common.gui.ContainerDistiller;
import mctmods.immersivetechnology.common.gui.ContainerSolarTower;
import mctmods.immersivetechnology.common.gui.ContainerTimer;
import mctmods.immersivetechnology.common.gui.ContainerTrashItem;
import mctmods.immersivetechnology.common.util.network.MessageRequestUpdate;
import mctmods.immersivetechnology.common.util.network.MessageStopSound;
import mctmods.immersivetechnology.common.util.network.MessageTileSync;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;

public class CommonProxy implements IGuiHandler {

	public void preInit() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onWorldTick(TickEvent.WorldTickEvent event) {
		if (!ITUtils.REMOVE_FROM_TICKING.isEmpty() && event.phase == TickEvent.Phase.END) {
			event.world.tickableTileEntities.removeAll(ITUtils.REMOVE_FROM_TICKING);
			ITUtils.REMOVE_FROM_TICKING.clear();
		}
	}

	public void preInitEnd() {}

	public void init() {
		ImmersiveTechnology.packetHandler.registerMessage(MessageTileSync.HandlerServer.class, MessageTileSync.class, 0, Side.SERVER);
		ImmersiveTechnology.packetHandler.registerMessage(MessageStopSound.HandlerServer.class, MessageStopSound.class, 1, Side.SERVER);
		ImmersiveTechnology.packetHandler.registerMessage(MessageRequestUpdate.HandlerServer.class, MessageRequestUpdate.class, 2, Side.SERVER);
	}

	public void initEnd() {}

	public void postInit() {}

	public void postInitEnd() {}

	public static <T extends TileEntity & IGuiTile> void openGuiForTile(@Nonnull EntityPlayer player, @Nonnull T tile) {
		player.openGui(ImmersiveTechnology.instance, tile.getGuiID(), tile.getWorld(), tile.getPos().getX(), tile.getPos().getY(), tile.getPos().getZ());
	}

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
		if(tile instanceof IGuiTile) {
			Object gui = null;
			if(ID == ITLib.GUIID_Boiler && tile instanceof TileEntityBoilerMaster) gui = new ContainerBoiler(player.inventory, (TileEntityBoilerMaster) tile);
			if(ID == ITLib.GUIID_Coke_oven_advanced && tile instanceof TileEntityCokeOvenAdvancedMaster) gui = new ContainerCokeOvenAdvanced(player.inventory, (TileEntityCokeOvenAdvancedMaster) tile);
			if(ID == ITLib.GUIID_Distiller && tile instanceof TileEntityDistillerMaster) gui = new ContainerDistiller(player.inventory, (TileEntityDistillerMaster) tile);
			if(ID == ITLib.GUIID_Solar_Tower && tile instanceof TileEntitySolarTowerMaster) gui = new ContainerSolarTower(player.inventory, (TileEntitySolarTowerMaster) tile);
			if(ID == ITLib.GUIID_Timer && tile instanceof TileEntityTimer) gui = new ContainerTimer(player.inventory, (TileEntityTimer) tile);
			if(ID == ITLib.GUIID_Trash_Item && tile instanceof TileEntityTrashItem) gui = new ContainerTrashItem(player.inventory, (TileEntityTrashItem) tile);
			if(gui != null) ((IGuiTile)tile).onGuiOpened(player, false);
			return gui;
		}
		return null;
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
			return gui;
		}
		return null;
	}

	public EntityPlayer getClientPlayer() {
		return null;
	}

	public World getClientWorld() {
		return null;
	}
}
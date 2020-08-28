package mctmods.immersivetechnology.common;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGuiTile;
import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.api.ITLib;
import mctmods.immersivetechnology.api.ITUtils;
import mctmods.immersivetechnology.common.blocks.connectors.tileentities.TileEntityTimer;
import mctmods.immersivetechnology.common.blocks.metal.tileentities.*;
import mctmods.immersivetechnology.common.blocks.stone.tileentities.TileEntityCokeOvenAdvancedMaster;
import mctmods.immersivetechnology.common.gui.*;
import mctmods.immersivetechnology.common.util.TemporaryTileEntityRequest;
import mctmods.immersivetechnology.common.util.network.BinaryMessageTileSync;
import mctmods.immersivetechnology.common.util.network.MessageRequestUpdate;
import mctmods.immersivetechnology.common.util.network.MessageStopSound;
import mctmods.immersivetechnology.common.util.network.MessageTileSync;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class CommonProxy implements IGuiHandler {

	public void preInit() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	public static List<TemporaryTileEntityRequest> toReform = new ArrayList<>();

	@SubscribeEvent
	public void onWorldUnload(WorldEvent.Unload event) {
		//sanitation
		TileEntityFluidPipe.indirectConnections.clear();
	}

	@SubscribeEvent
	public void onWorldTick(TickEvent.WorldTickEvent event) {
		if(!ITUtils.REMOVE_FROM_TICKING.isEmpty() && event.phase == TickEvent.Phase.END) {
			event.world.tickableTileEntities.removeAll(ITUtils.REMOVE_FROM_TICKING);
			ITUtils.REMOVE_FROM_TICKING.clear();
		}

		//REMOVE THIS GARBAGE WHEN PORTING THIS MOD PAST 1.12
		if(!toReform.isEmpty() && event.phase == TickEvent.Phase.END) {
			for(TemporaryTileEntityRequest request : toReform) {
				request.multiblock.createStructure(request.world, request.formationPosition == null? request.position : request.formationPosition, request.facing.getOpposite(), null);
				TileEntity te = request.world.getTileEntity(request.position);
				if(te != null) te.readFromNBT(request.nbtTag);
			}
			toReform.clear();
		}
	}

	public void preInitEnd() {}

	public void init() {
		ImmersiveTechnology.packetHandler.registerMessage(MessageTileSync.HandlerServer.class, MessageTileSync.class, 0, Side.SERVER);
		ImmersiveTechnology.packetHandler.registerMessage(MessageStopSound.HandlerServer.class, MessageStopSound.class, 1, Side.SERVER);
		ImmersiveTechnology.packetHandler.registerMessage(MessageRequestUpdate.HandlerServer.class, MessageRequestUpdate.class, 2, Side.SERVER);
		ImmersiveTechnology.packetHandler.registerMessage(BinaryMessageTileSync.HandlerServer.class, BinaryMessageTileSync.class, 3, Side.SERVER);
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
		return null;
	}

	public EntityPlayer getClientPlayer() {
		return null;
	}

	public World getClientWorld() {
		return null;
	}

	public void clearRenderCaches() {
	}

}
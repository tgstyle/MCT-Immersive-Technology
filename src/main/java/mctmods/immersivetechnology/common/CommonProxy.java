package mctmods.immersivetechnology.common;


import com.immersiveconvergence.api.ICJEICatalysts;
import com.immersiveconvergence.api.ICMultiblockNames;
import com.immersiveconvergence.api.multiblock.ICBlockInterfaces.IGuiTile;
import com.immersiveconvergence.common.blocks.pipes.TileEntityFluidPipeAlternative;

import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.client.ITGUI;
import mctmods.immersivetechnology.common.blocks.connectors.tileentities.TileEntityTimer;
import mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntityTrashItem;
import mctmods.immersivetechnology.common.blocks.wooden.tileentities.TileEntityCrate;
import mctmods.immersivetechnology.common.gui.*;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.*;
import mctmods.immersivetechnology.common.multiblocks.stone.tileentities.TileEntityAdvancedCokeOvenMaster;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;

import javax.annotation.Nonnull;

@SuppressWarnings("unused")
public class CommonProxy implements IGuiHandler {

	public void preInit() {
		MinecraftForge.EVENT_BUS.register(this);
		ICMultiblockNames.register("IT", ImmersiveTechnology.MODID);
		if (Config.ITConfig.Multiblocks.enable.enable_advancedCokeOven) { ICJEICatalysts.suppress("cokeoven"); }
	}

	@SubscribeEvent public void onWorldUnload(WorldEvent.Unload event) {
		if (Loader.isModLoaded("immersiveengineering")) { TileEntityFluidPipeAlternative.indirectConnections.remove(event.getWorld().provider.getDimension()); }
	}

	public void preInitEnd() {}

	public void init() {}

	public void initEnd() {}

	public void postInit() {}

	public void postInitEnd() {}

	public static <T extends TileEntity & IGuiTile> void openGuiForTile(@Nonnull EntityPlayer player, @Nonnull T tile) {
		player.openGui(ImmersiveTechnology.instance, tile.getGuiID(), tile.getWorld(), tile.getPos().getX(), tile.getPos().getY(), tile.getPos().getZ());
	}

	@Override public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
		if (tile instanceof IGuiTile) {
			Object gui = null;
			if (ID == ITGUI.GUIID_Advanced_coke_oven && tile instanceof TileEntityAdvancedCokeOvenMaster) { gui = new ContainerAdvancedCokeOven(player.inventory, (TileEntityAdvancedCokeOvenMaster)tile); }
			if (ID == ITGUI.GUIID_Boiler_Tank && tile instanceof TileEntityBoilerTankMaster) { gui = new ContainerBoilerTank(player.inventory, (TileEntityBoilerTankMaster)tile); }
			if (ID == ITGUI.GUIID_Boiler_Liquid && tile instanceof TileEntityBoilerLiquidMaster) { gui = new ContainerBoilerLiquid(player.inventory, (TileEntityBoilerLiquidMaster)tile); }
			if (ID == ITGUI.GUIID_Boiler_Solid && tile instanceof TileEntityBoilerSolidMaster) { gui = new ContainerBoilerSolid(player.inventory, (TileEntityBoilerSolidMaster)tile); }
			if (ID == ITGUI.GUIID_Crate && tile instanceof TileEntityCrate) { gui = new ContainerCrate(player.inventory, (TileEntityCrate)tile); }
			if (ID == ITGUI.GUIID_Distiller && tile instanceof TileEntityDistillerMaster) { gui = new ContainerDistiller(player.inventory, (TileEntityDistillerMaster)tile); }
			if (ID == ITGUI.GUIID_Melting_Crucible && tile instanceof TileEntityMeltingCrucibleMaster) { gui = new ContainerMeltingCrucible(player.inventory, (TileEntityMeltingCrucibleMaster) tile); }
			if (ID == ITGUI.GUIID_Solar_Melter && tile instanceof TileEntitySolarMelterMaster) { gui = new ContainerSolarMelter(player.inventory, (TileEntitySolarMelterMaster)tile); }
			if (ID == ITGUI.GUIID_Solar_Tower && tile instanceof TileEntitySolarTowerMaster) { gui = new ContainerSolarTower(player.inventory, (TileEntitySolarTowerMaster)tile); }
			if (ID == ITGUI.GUIID_Timer && tile instanceof TileEntityTimer) { gui = new ContainerTimer(player.inventory, (TileEntityTimer)tile); }
			if (ID == ITGUI.GUIID_Trash_Item && tile instanceof TileEntityTrashItem) { gui = new ContainerTrashItem(player.inventory, (TileEntityTrashItem)tile); }
			if (gui != null) { ((IGuiTile)tile).onGuiOpened(player, false); }
			return gui;
		}
		return null;
	}

	@Override public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) { return null; }

	public EntityPlayer getClientPlayer() { return null; }

	public World getClientWorld() { return null; }

	public void clearRenderCaches() {}
}

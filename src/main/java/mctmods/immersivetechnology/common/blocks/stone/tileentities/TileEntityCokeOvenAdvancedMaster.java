package mctmods.immersivetechnology.common.blocks.stone.tileentities;

import blusunrize.immersiveengineering.api.crafting.CokeOvenRecipe;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.api.ITUtils;
import mctmods.immersivetechnology.common.Config.ITConfig.Machines.AdvancedCokeOven;
import mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntityCokeOvenPreheater;
import mctmods.immersivetechnology.common.util.ITFluidTank;
import mctmods.immersivetechnology.common.util.ITSounds;
import mctmods.immersivetechnology.common.util.network.MessageStopSound;
import mctmods.immersivetechnology.common.util.network.MessageTileSync;
import mctmods.immersivetechnology.common.util.sound.ITSoundHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.oredict.OreDictionary;

public class TileEntityCokeOvenAdvancedMaster extends TileEntityCokeOvenAdvancedSlave implements IEBlockInterfaces.IActiveState, IEBlockInterfaces.IProcessTile, ITFluidTank.TankListener {

	private static int tankSize = AdvancedCokeOven.advancedCokeOven_tankSize;
	public static float baseSpeed = AdvancedCokeOven.advancedCokeOven_speed_base;
	public static float preheaterAdd = AdvancedCokeOven.advancedCokeOven_preheater_speed_increase;
	public static float preheaterMult = AdvancedCokeOven.advancedCokeOven_preheater_speed_multiplier;
	BlockPos fluidOutputPos;

	public float process = 0;
	public int processMax = 0;
	public boolean active = false;
	private float soundVolume;
	private CokeOvenRecipe processing;

  public ITFluidTank tank = new ITFluidTank(tankSize, this);
  public static int slotCount = 4;
  NonNullList<ItemStack> inventory = NonNullList.withSize(slotCount, ItemStack.EMPTY);

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket) {
		super.readCustomNBT(nbt, descPacket);
		process = nbt.getFloat("process");
		processMax = nbt.getInteger("processMax");
		active = nbt.getBoolean("active");
		tank.readFromNBT(nbt.getCompoundTag("tank"));
		if(!descPacket) inventory = Utils.readInventory(nbt.getTagList("inventory", 10), slotCount);
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket) {
		super.writeCustomNBT(nbt, descPacket);
		nbt.setFloat("process", process);
		nbt.setInteger("processMax", processMax);
		nbt.setBoolean("active", active);
		NBTTagCompound tankTag = tank.writeToNBT(new NBTTagCompound());
		nbt.setTag("tank", tankTag);
		if(!descPacket) nbt.setTag("inventory", Utils.writeInventory(inventory));
	}

	private void pumpOutputOut() {
		if(tank.getFluidAmount() == 0) return;
		if(fluidOutputPos == null) fluidOutputPos = ITUtils.LocalOffsetToWorldBlockPos(this.getPos(), 0, - 1, 3, facing, mirrored);
		IFluidHandler output = FluidUtil.getFluidHandler(world, fluidOutputPos, facing.getOpposite());
		if(output == null) return;
		FluidStack out = tank.getFluid();
		int accepted = output.fill(out, false);
		if(accepted == 0) return;
		int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
		this.tank.drain(drained, true);
	}

	public void handleSounds() {
		if(active) {
			if(soundVolume < 1) soundVolume += 0.01f;
		} else if(soundVolume > 0) soundVolume -= 0.01f;
		BlockPos center = getPos();
		if(soundVolume == 0) ITSoundHandler.StopSound(center);
		else {
			EntityPlayerSP player = Minecraft.getMinecraft().player;
			float attenuation = Math.max((float) player.getDistanceSq(center.getX(), center.getY(), center.getZ()) / 8, 1);
			ITSounds.advCokeOven.PlayRepeating(center, soundVolume / attenuation, 1);
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void onChunkUnload() {
		ITSoundHandler.StopSound(getPos());
		super.onChunkUnload();
	}

	@Override
	public void disassemble() {
		BlockPos center = getPos();
		ImmersiveTechnology.packetHandler.sendToAllTracking(new MessageStopSound(center), new NetworkRegistry.TargetPoint(world.provider.getDimension(), center.getX(), center.getY(), center.getZ(), 0));
		super.disassemble();
	}

	public void notifyNearbyClients() {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setBoolean("active", active);
		BlockPos center = getPos();
		ImmersiveTechnology.packetHandler.sendToAllTracking(new MessageTileSync(this, tag), new NetworkRegistry.TargetPoint(world.provider.getDimension(), center.getX(), center.getY(), center.getZ(), 0));
	}

	@Override
	public void receiveMessageFromServer(NBTTagCompound message) {
		if(message.hasKey("active")) active = message.getBoolean("active");
		else if(message.hasKey("process")) {
			process = message.getFloat("process");
			processMax = message.getInteger("processMax");
		}
	}

	public void updateRequested(EntityPlayerMP player) {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setFloat("process", process);
		tag.setInteger("processMax", processMax);
		ImmersiveTechnology.packetHandler.sendTo(new MessageTileSync(this, tag), player);
	}

	public void efficientMarkDirty() { // !!!!!!! only use it within update() function !!!!!!!
		world.getChunkFromBlockCoords(this.getPos()).markDirty();
	}

	@Override
	public void update() {
		if(!formed) return;
		if(world.isRemote) {
			handleSounds();
			return;
		}
		boolean update = false;
		if(!inventory.get(0).isEmpty()) {
			if(processing == null) {
				processing = getRecipe();
				if(processing == null) {
					if(active) {
						process = 0;
						processMax = 0;
						active = false;
						update = true;
						notifyNearbyClients();
					}
				} else {
					if(!active) {
						this.process = this.processMax = processing.time;
						active = true;
						update = true;
						notifyNearbyClients();
					}
				}
			}
			if(active && process > 0) {
				process -= getProcessSpeed();
				update = true;
			}
			if(processing != null && process <= 0) {
				if(tank.getFluidAmount() + processing.creosoteOutput <= tank.getCapacity() &&
						inventory.get(1).getCount() + getRecipe().output.getCount() <= inventory.get(1).getMaxStackSize()) {
					Utils.modifyInvStackSize(inventory, 0, -1);
					if(!inventory.get(1).isEmpty()) {
						inventory.get(1).grow(processing.output.copy().getCount());
					} else if(inventory.get(1).isEmpty()) {
						inventory.set(1, processing.output.copy());
					}
					this.tank.fill(new FluidStack(IEContent.fluidCreosote, processing.creosoteOutput), true);
					this.markContainingBlockForUpdate(null);

					active = false;
					update = true;
					process = 0;
					processMax = 0;
					processing = null;
					notifyNearbyClients();
				} else {
					if(active) {
						update = true;
						active = false;
						notifyNearbyClients();
					}
				}
			}
		} else {
			if(active) {
				active = false;
				update = true;
				process = 0;
				processMax = 0;
				processing = null;
				notifyNearbyClients();
			}
		}
		if(tank.getFluidAmount() > 0 && tank.getFluid() != null && (inventory.get(3).isEmpty() || inventory.get(3).getCount() + 1 <= inventory.get(3).getMaxStackSize())) {
			ItemStack filledContainer = Utils.fillFluidContainer(tank, inventory.get(2), inventory.get(3), null);
			if(!filledContainer.isEmpty()) {
				if(inventory.get(2).getCount() == 1 && !Utils.isFluidContainerFull(filledContainer)) {
					inventory.set(2, filledContainer.copy());
					update = true;
				} else {
					if(!inventory.get(3).isEmpty() && OreDictionary.itemMatches(inventory.get(3), filledContainer, true)) {
						inventory.get(3).grow(filledContainer.getCount());
					} else if(inventory.get(3).isEmpty()) {
						inventory.set(3, filledContainer.copy());
						Utils.modifyInvStackSize(inventory, 2, - filledContainer.getCount());
						update = true;
					}
				}
			}
		}
		TileEntity inventoryFront = Utils.getExistingTileEntity(world, getPos().offset(facing.getOpposite(), 1).add(0, - 1, 0));
		if(!this.inventory.get(1).isEmpty()) {
			ItemStack stack = this.inventory.get(1);
			if(inventoryFront != null) stack = Utils.insertStackIntoInventory(inventoryFront, stack, facing);
			this.inventory.set(1, stack);
		}
		pumpOutputOut();
		if(update) efficientMarkDirty();
	}

	public CokeOvenRecipe getRecipe() {
		CokeOvenRecipe recipe = CokeOvenRecipe.findRecipe(inventory.get(0));
		if(recipe == null) return null;
		if(inventory.get(1).isEmpty() || (OreDictionary.itemMatches(inventory.get(1), recipe.output, false) && inventory.get(1).getCount() + recipe.output.getCount() <= getSlotLimit(1))) if(tank.getFluidAmount()+recipe.creosoteOutput <= tank.getCapacity()) return recipe;
		return null;
	}

	@Override
	public boolean isDummy() {
		return false;
	}

	@Override
	public TileEntityCokeOvenAdvancedMaster master() {
		master = this;
		return this;
	}

	@Override
	public int[] getCurrentProcessesStep() {
		return new int[] { Math.round(processMax - process) };
	}

	@Override
	public int[] getCurrentProcessesMax() {
		return new int[] { processMax };
	}

	private float getProcessSpeed() {
		int activePreheaters = 0;
		for(int k = 0; k < 2; k++) {
			EnumFacing f = k == 0 ? facing.rotateY() : facing.rotateYCCW();
			BlockPos pos = getPos().add(0, - 1, 0).offset(f, 2).offset(facing, 1);
			TileEntity tile = Utils.getExistingTileEntity(world, pos);
			if(!(tile instanceof TileEntityCokeOvenPreheater)) continue;
			TileEntityCokeOvenPreheater preheater = (TileEntityCokeOvenPreheater)tile;
			if(preheater.facing != f.getOpposite() || !preheater.doSpeedup()) continue;
			activePreheaters++;
		}
		return (baseSpeed + activePreheaters * preheaterAdd) * (1 + activePreheaters * (preheaterMult - 1));
	}
  
	IItemHandler inputHandler = new IEInventoryHandler(1, this, 0, new boolean[] {true}, new boolean[] {false});
	IItemHandler outputHandler = new IEInventoryHandler(1, this, 1, new boolean[] {false}, new boolean[] {true});

	@Override
	public void TankContentsChanged() {
		this.markContainingBlockForUpdate(null);
	}

}
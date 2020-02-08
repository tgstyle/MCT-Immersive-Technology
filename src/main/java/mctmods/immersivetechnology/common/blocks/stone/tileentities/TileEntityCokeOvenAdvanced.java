package mctmods.immersivetechnology.common.blocks.stone.tileentities;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.IEProperties.PropertyBoolInverted;
import blusunrize.immersiveengineering.api.crafting.CokeOvenRecipe;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IActiveState;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedCollisionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedSelectionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGuiTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IProcessTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IUsesBooleanProperty;
import blusunrize.immersiveengineering.common.blocks.TileEntityMultiblockPart;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.api.ITLib;
import mctmods.immersivetechnology.api.ITUtils;
import mctmods.immersivetechnology.common.Config.ITConfig.Machines.AdvancedCokeOven;
import mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntityCokeOvenPreheater;
import mctmods.immersivetechnology.common.blocks.stone.multiblocks.MultiblockCokeOvenAdvanced;
import mctmods.immersivetechnology.common.util.ITSounds;
import mctmods.immersivetechnology.common.util.network.MessageStopSound;
import mctmods.immersivetechnology.common.util.network.MessageTileSync;
import mctmods.immersivetechnology.common.util.sound.ITSoundHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.oredict.OreDictionary;

public class TileEntityCokeOvenAdvanced extends TileEntityMultiblockPart<TileEntityCokeOvenAdvanced> implements IIEInventory, IActiveState, IGuiTile, IProcessTile, IAdvancedSelectionBounds, IAdvancedCollisionBounds {
	private static final int[] size = { 4, 3, 3 };
	public static float baseSpeed = AdvancedCokeOven.advancedCokeOven_speed_base;
	public static float preheaterAdd = AdvancedCokeOven.advancedCokeOven_preheater_speed_increase;
	public static float preheaterMult = AdvancedCokeOven.advancedCokeOven_preheater_speed_multiplier;

	public FluidTank tank = new FluidTank(24000);

	NonNullList<ItemStack> inventory = NonNullList.withSize(4, ItemStack.EMPTY);

	public float process = 0;
	public int processMax = 0;
	public boolean active = false;
	private float soundVolume;
	private CokeOvenRecipe processing;

	public TileEntityCokeOvenAdvanced() {
		super(size);
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket) {
		super.readCustomNBT(nbt, descPacket);
		process = nbt.getFloat("process");
		processMax = nbt.getInteger("processMax");
		active = nbt.getBoolean("active");
		tank.readFromNBT(nbt.getCompoundTag("tank"));
		if(!descPacket) inventory = Utils.readInventory(nbt.getTagList("inventory", 10), 4);
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

	public void handleSounds() {
		if(active) {
			if(soundVolume < 1) soundVolume += 0.01f;
		} else if(soundVolume > 0) soundVolume -= 0.01f;
		BlockPos center = getPos();
		if(soundVolume == 0) ITSoundHandler.StopSound(center);
		else {
			EntityPlayerSP player = Minecraft.getMinecraft().player;
			float attenuation = Math.max((float) player.getDistanceSq(center.getX(), center.getY(), center.getZ()) / 8, 1);
			ITSoundHandler.PlaySound(center, ITSounds.advCokeOven, SoundCategory.BLOCKS, true, soundVolume / attenuation, 1);
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void onChunkUnload() {
		if(!isDummy()) ITSoundHandler.StopSound(getPos());
		super.onChunkUnload();
	}

	@Override
	public void disassemble() {
		if(!isDummy()) {
			BlockPos center = getPos();
			ImmersiveTechnology.packetHandler.sendToAllTracking(new MessageStopSound(center), new NetworkRegistry.TargetPoint(world.provider.getDimension(), center.getX(), center.getY(), center.getZ(), 0));
		}
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
		ApiUtils.checkForNeedlessTicking(this);
		if(formed && !isDummy()) {
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
			if(tank.getFluidAmount() > 0) {
				int outSize = Math.min(144, tank.getFluidAmount());
				FluidStack out = Utils.copyFluidStackWithAmount(tank.getFluid(), outSize, false);
				BlockPos outPos = getPos().offset(facing, 3).add(0, - 1, 0);
				IFluidHandler output = FluidUtil.getFluidHandler(world, outPos, facing.getOpposite());
				if(output != null) {
					int accepted = output.fill(out, false);
					if(accepted > 0) {
						int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
						this.tank.drain(drained, true);
						this.markContainingBlockForUpdate(null);
					}
				}
			}
			if(update) efficientMarkDirty();
		}
	}
	
	@Override
	public PropertyBoolInverted getBoolProperty(Class<? extends IUsesBooleanProperty> inf) {
		return null;
	}

	public CokeOvenRecipe getRecipe() {
		CokeOvenRecipe recipe = CokeOvenRecipe.findRecipe(inventory.get(0));
		if(recipe == null) return null;
		if(inventory.get(1).isEmpty() || (OreDictionary.itemMatches(inventory.get(1), recipe.output, false) && inventory.get(1).getCount() + recipe.output.getCount() <= getSlotLimit(1))) if(tank.getFluidAmount()+recipe.creosoteOutput <= tank.getCapacity()) return recipe;
		return null;
	}

	@Override
	public int[] getCurrentProcessesStep() {
		TileEntityCokeOvenAdvanced master = master();
		if(master != this && master != null) return master.getCurrentProcessesStep();
		return new int[] { Math.round(processMax - process) };
	}

	@Override
	public int[] getCurrentProcessesMax() {
		TileEntityCokeOvenAdvanced master = master();
		if(master != this && master != null) return master.getCurrentProcessesMax();
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

	@Override
	public boolean getIsActive() {
		return this.active;
	}

	IItemHandler inputHandler = new IEInventoryHandler(1, this, 0, new boolean[] {true}, new boolean[] {false});
	IItemHandler outputHandler = new IEInventoryHandler(1, this, 1, new boolean[] {false}, new boolean[] {true});

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if((pos == 1 || pos == 31) && capability == net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			TileEntityCokeOvenAdvanced master = (TileEntityCokeOvenAdvanced)master();
			if(master == null) {
				return null;
			} else if(pos == 1 && facing == master.facing) {
				return (T)master.outputHandler;
			} else if(pos == 31 && facing == EnumFacing.UP) {
				return (T)master.inputHandler;
			}
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		if((pos == 1 || pos == 31) && capability == net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			TileEntityCokeOvenAdvanced master = (TileEntityCokeOvenAdvanced)master();
			if(master == null) {
				return false;
			} else if(pos == 1 && facing == master.facing) {
				return true;
			} else if(pos == 31 && facing == EnumFacing.UP) {
				return true;
			}
		}
		return super.hasCapability(capability, facing);
	}

	@Override
	public NonNullList<ItemStack> getInventory() {
		TileEntityCokeOvenAdvanced master = master();
		if(master != null && master.formed && formed) return master.inventory;
		return this.inventory;
	}

	@Override
	public boolean isStackValid(int slot, ItemStack stack) {
		if(stack.isEmpty()) return false;
		if(slot == 0) return CokeOvenRecipe.findRecipe(stack) != null;
		if(slot == 2) return Utils.isFluidRelatedItemStack(stack);
		return false;
	}

	@Override
	public int getSlotLimit(int slot) {
		return 64;
	}

	@Override
	public void doGraphicalUpdates(int slot) {
	}

	@Override
	protected IFluidTank[] getAccessibleFluidTanks(EnumFacing side) {
		TileEntityCokeOvenAdvanced master = master();
		if(master != null) {
			if(pos == 7 && (side == null || side == facing)) {
				return new FluidTank[] {master.tank};
			}
		}
		return new FluidTank[0];
	}

	@Override
	protected boolean canFillTankFrom(int iTank, EnumFacing side, FluidStack resource) {
		return false;
	}

	@Override
	protected boolean canDrainTankFrom(int iTank, EnumFacing side) {
		return (pos == 7 && (side == null || side == facing));
	}

	@Override
	public boolean canOpenGui() {
		return formed;
	}

	@Override
	public int getGuiID() {
		return ITLib.GUIID_Coke_oven_advanced;
	}

	@Override
	public TileEntity getGuiMaster() {
		return master();
	}

	@Override
	public ItemStack getOriginalBlock() {
		if(pos<0) return ItemStack.EMPTY;
		ItemStack s = ItemStack.EMPTY;
		try {
			s = MultiblockCokeOvenAdvanced.instance.getStructureManual()[pos/9][pos%9/3][pos%3];
		} catch(Exception e) {e.printStackTrace();}
		return s.copy();
	}

	@Override
	public float[] getBlockBounds() {
		return null;
	}

	@Override
	public List<AxisAlignedBB> getAdvancedColisionBounds() {
		return getAdvancedSelectionBounds();
	}

	@Override
	public List<AxisAlignedBB> getAdvancedSelectionBounds() {
		double[] boundingArray = new double[6];
		EnumFacing fl = facing;
		EnumFacing fw = facing.rotateY();
		if(pos == 0 || pos == 2) {
			if(pos == 2) fw = fw.getOpposite();
			boundingArray = ITUtils.smartBoundingBox(0, .6875f, .6875f, 0, 0, 1, fl, fw);
			return Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		}
		if(pos == 3 || pos == 5 || pos == 12 || pos == 14) {
			if(pos == 5 || pos == 14) fw = fw.getOpposite();
			boundingArray = ITUtils.smartBoundingBox(0, 0, .5f, 0, 0, 1, fl, fw);
			return Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		}
		if(pos == 6 || pos == 8) {
			if(pos == 8) fw = fw.getOpposite();
			boundingArray = ITUtils.smartBoundingBox(.6875f, 0, .6875f, 0, 0, 1, fl, fw);
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(0, .3125f, .375f, 0, 0, 1, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;
		}
		if(pos == 9 || pos == 11) {
			if(pos == 11) fw = fw.getOpposite();
			boundingArray = ITUtils.smartBoundingBox(0, .5f, .5f, 0, 0, 1, fl, fw);
			return Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		}
		if(pos == 10) {
			boundingArray = ITUtils.smartBoundingBox(0, .5f, 0, 0, 0, 1, fl, fw);
			return Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		}
		if(pos == 15 || pos == 17) {
			if(pos == 17) fw = fw.getOpposite();
			boundingArray = ITUtils.smartBoundingBox(.6875f, 0, .5f, 0, 0, 1, fl, fw);
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(0, .3125f, .375f, 0, 0, 1, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;
		}
		if(pos == 18 || pos == 20) {
			if(pos == 20) fw = fw.getOpposite();
			boundingArray = ITUtils.smartBoundingBox(0, .625f, .625f, 0, 0, 1, fl, fw);
			return Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		}
		if(pos == 19) {
			boundingArray = ITUtils.smartBoundingBox(0, .625f, 0, 0, 0, 1, fl, fw);
			return Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		}
		if(pos == 21 || pos == 23) {
			if(pos == 23) fw = fw.getOpposite();
			boundingArray = ITUtils.smartBoundingBox(0, 0, .625f, 0, 0, 1, fl, fw);
			return Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		}
		if(pos == 24 || pos == 26) {
			if(pos == 26) fw = fw.getOpposite();
			boundingArray = ITUtils.smartBoundingBox(.6875f, 0, .625f, 0, 0, 1, fl, fw);
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(0, .3125f, .375f, 0, 0, .25f, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;
		}
		if(pos == 25) {
			boundingArray = ITUtils.smartBoundingBox(.6875f, 0, 0, 0, 0, 1, fl, fw);
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(0, .3125f, 0, 0, 0, .25f, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;
		}
		return null;
	}

	@Override
	public boolean isOverrideBox(AxisAlignedBB box, EntityPlayer player, RayTraceResult mop, ArrayList<AxisAlignedBB> list) {
		return false;
	}

}
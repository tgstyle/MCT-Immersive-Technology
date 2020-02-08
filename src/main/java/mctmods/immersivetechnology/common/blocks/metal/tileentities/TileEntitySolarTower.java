package mctmods.immersivetechnology.common.blocks.metal.tileentities;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedCollisionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedSelectionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGuiTile;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMultiblockMetal;
import blusunrize.immersiveengineering.common.util.Utils;
import mctmods.immersivetechnology.ImmersiveTech;
import mctmods.immersivetechnology.api.ITLib;
import mctmods.immersivetechnology.api.crafting.SolarTowerRecipe;
import mctmods.immersivetechnology.common.Config.ITConfig.Machines.SolarReflector;
import mctmods.immersivetechnology.common.Config.ITConfig.Machines.SolarTower;
import mctmods.immersivetechnology.common.blocks.metal.multiblocks.MultiblockSolarTower;
import mctmods.immersivetechnology.common.util.ITSounds;
import mctmods.immersivetechnology.common.util.network.MessageStopSound;
import mctmods.immersivetechnology.common.util.network.MessageTileSync;
import mctmods.immersivetechnology.common.util.sound.ITSoundHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

public class TileEntitySolarTower extends TileEntityMultiblockMetal<TileEntitySolarTower, SolarTowerRecipe> implements IGuiTile, IAdvancedSelectionBounds, IAdvancedCollisionBounds {
	public TileEntitySolarTower() {
		super(MultiblockSolarTower.instance, new int[] { 7, 3, 3 }, 0, true);
	}
	
	private static int solarMaxRange = SolarReflector.solarReflector_maxRange;
	private static int solarMinRange = SolarReflector.solarReflector_minRange;
	private static float speedMult = SolarTower.solarTower_speed_multiplier;
	private static float reflectorSpeedMult = SolarTower.solarTower_solarReflector_speed_multiplier;

	public FluidTank[] tanks = new FluidTank[] {
		new FluidTank(32000),
		new FluidTank(32000)
	};

	public NonNullList<ItemStack> inventory = NonNullList.withSize(4, ItemStack.EMPTY);

	private float processTime = 0;
	private float soundVolume;
	private boolean isProcessing;
	private SolarTowerRecipe processing;
	public int[] reflectors = new int[4];

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket) {
		super.readCustomNBT(nbt, descPacket);
		tanks[0].readFromNBT(nbt.getCompoundTag("tank0"));
		tanks[1].readFromNBT(nbt.getCompoundTag("tank1"));
		reflectors = nbt.getIntArray("reflectors");
		processTime = nbt.getFloat("processTime");
		isProcessing = nbt.getBoolean("isProcessing");
		if(reflectors.length != 4) reflectors = new int[4];
		if(!descPacket) inventory = Utils.readInventory(nbt.getTagList("inventory", 10), 4);
	}

	@Override
    public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        nbt.setTag("tank0", tanks[0].writeToNBT(new NBTTagCompound()));
        nbt.setTag("tank1", tanks[1].writeToNBT(new NBTTagCompound()));
        nbt.setIntArray("reflectors", reflectors);
        nbt.setBoolean("isProcessing", isProcessing);
        if(!descPacket) nbt.setTag("inventory", Utils.writeInventory(inventory));
    }

	public void handleSounds() {
		if(isProcessing && !isRSDisabled()) {
			if(soundVolume < 1) soundVolume += 0.01f;
		} else if(soundVolume > 0) soundVolume -= 0.01f;
		BlockPos center = getPos();
		int activeReflectors = 0;
		for(int reflector : reflectors) if(reflector > 0) activeReflectors++;
		if(soundVolume == 0 || activeReflectors == 0) ITSoundHandler.StopSound(center);
		else {
			EntityPlayerSP player = Minecraft.getMinecraft().player;
			float attenuation = Math.max((float) player.getDistanceSq(center.getX(), center.getY(), center.getZ()) / 8, 1);
			ITSoundHandler.PlaySound(center, ITSounds.solarTower, SoundCategory.BLOCKS, true, (soundVolume / attenuation) / activeReflectors, 1);
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
			ImmersiveTech.packetHandler.sendToAllTracking(new MessageStopSound(center), new NetworkRegistry.TargetPoint(world.provider.getDimension(), center.getX(), center.getY(), center.getZ(), 0));
		}
		super.disassemble();
	}

	public void notifyNearbyClients() {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setBoolean("isProcessing", isProcessing);
		BlockPos center = getPos();
		ImmersiveTech.packetHandler.sendToAllTracking(new MessageTileSync(this, tag), new NetworkRegistry.TargetPoint(world.provider.getDimension(), center.getX(), center.getY(), center.getZ(), 0));
	}

	@Override
	public void receiveMessageFromServer(NBTTagCompound message) {
		isProcessing = message.getBoolean("isProcessing");
	}

	public void efficientMarkDirty() { // !!!!!!! only use it within update() function !!!!!!!
		world.getChunkFromBlockCoords(this.getPos()).markDirty();
	}

	@Override
	public void update() {
		super.update();
		if(isDummy()) return;
		if(world.isRemote) {
			handleSounds();
			return;
		}
		boolean update = false;
		if(processing == null) {
			processing = SolarTowerRecipe.findRecipe(tanks[0].getFluid());
			if(processing == null) {
				if(isProcessing) {
					isProcessing = false;
					update = true;
					notifyNearbyClients();
				}
			} else if(!isProcessing) {
				isProcessing = true;
				update = true;
				notifyNearbyClients();
			}
		}
		if(!isRSDisabled() && isProcessing && checkReflector()) processTime += getSpeed();
		if(processing != null && processTime >= processing.getTotalProcessTime()) {
			if(tanks[1].fill(processing.fluidOutput, false) == processing.fluidOutput.amount) {
				this.processTime = 0;
				tanks[0].drain(processing.fluidInput, true);
				tanks[1].fill(processing.fluidOutput, true);
				processing = null;
				if(!isProcessing) {
					update = true;
					isProcessing = true;
				}
			} else {
				if(isProcessing) {
					update = true;
					notifyNearbyClients();
				}
				isProcessing = false;
			}
		}
		if(this.tanks[1].getFluidAmount() > 0) {
			ItemStack filledContainer = Utils.fillFluidContainer(tanks[1], inventory.get(2), inventory.get(3), null);
			if(!filledContainer.isEmpty()) {
				if(!inventory.get(3).isEmpty() && OreDictionary.itemMatches(inventory.get(3), filledContainer, true)) inventory.get(3).grow(filledContainer.getCount());
				else if(inventory.get(3).isEmpty()) inventory.set(3, filledContainer.copy());
				inventory.get(2).shrink(1);
				if(inventory.get(2).getCount() <= 0) inventory.set(2, ItemStack.EMPTY);
				update = true;
			}
			if(this.tanks[1].getFluidAmount() > 0) {
				FluidStack out = Utils.copyFluidStackWithAmount(this.tanks[1].getFluid(), Math.min(this.tanks[1].getFluidAmount(), 1000), true);
				BlockPos outputPos = this.getPos().add(0, -1, 0).offset(facing, 3);
				IFluidHandler output = FluidUtil.getFluidHandler(world, outputPos, facing);
				if(output != null) {
					int accepted = output.fill(out, false);
					if(accepted > 0) {
						int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
						this.tanks[1].drain(drained, true);
						update = true;
					}
				}
			}
		}
		ItemStack emptyContainer = Utils.drainFluidContainer(tanks[0], inventory.get(0), inventory.get(1), null);
		if(!emptyContainer.isEmpty() && emptyContainer.getCount() > 0) {
			if(!inventory.get(1).isEmpty() && OreDictionary.itemMatches(inventory.get(1), emptyContainer, true))
				inventory.get(1).grow(emptyContainer.getCount());
			else if(inventory.get(1).isEmpty())
				inventory.set(1, emptyContainer.copy());
			inventory.get(0).shrink(1);
			if(inventory.get(0).getCount() <= 0)
				inventory.set(0, ItemStack.EMPTY);
			update = true;
		}
		if(update) {
			efficientMarkDirty();
			this.markContainingBlockForUpdate(null);
		}
	}

	protected boolean checkReflector() {
		boolean ver = false;
		EnumFacing fw;
		EnumFacing fr;
		BlockPos pos;
		TileEntity tile;
		int maxRange = solarMaxRange;
		int minRange = solarMinRange;
		for(int cont = 0; cont < 4; cont++) {
			fw = facing;
			if(cont == 1) {
				fw = fw.rotateYCCW();
			} else if(cont == 2) {
				fw = fw.getOpposite();
			} else if(cont == 3) {
				fw = fw.rotateY();
			}
			reflectors[cont] = 0;
			for(int i = minRange; i < maxRange + 2; i++) {
				if(cont == 0) {
					pos = this.getPos().offset(fw, i + 2).add(0, 2, 0);
				} else if(cont % 2 != 0) {
					pos = this.getPos().offset(facing, 1).offset(fw, i + 1).add(0, 2, 0);
				} else {
					pos = this.getPos().offset(fw, i).add(0, 2, 0);
				}
				if(!Utils.isBlockAt(world, pos, Blocks.AIR, 0)) {
					tile = world.getTileEntity(pos);
					if(tile instanceof TileEntitySolarReflector) {
						fr = ((TileEntitySolarReflector) tile).facing;
						if((cont % 2 == 0 && (facing == EnumFacing.NORTH || facing == EnumFacing.SOUTH)) || (cont % 2 != 0 && (facing == EnumFacing.EAST || facing == EnumFacing.WEST))) {
							if(fr == EnumFacing.NORTH || fr == EnumFacing.SOUTH) {
								if(((TileEntitySolarReflector) tile).getSunState()) {
									ver = true;
									reflectors[cont] = 1;
								}
								break;
							}
						} else {
							if(fr == EnumFacing.EAST || fr == EnumFacing.WEST) {
								if(((TileEntitySolarReflector) tile).getSunState()) {
									ver = true;
									reflectors[cont] = 1;
								}
								break;
							}
						}
					} else {
						break;
					}
				}
			}
		}
		return ver;
	}

	protected float getSpeed() {
		int activeReflectors = 0;
		for(int reflectorValue : reflectors) activeReflectors += reflectorValue;
		if(activeReflectors == 0) return 0;
		return speedMult * (1 + (activeReflectors - 1) * (reflectorSpeedMult - 1));
	}

	@Override
	public NonNullList<ItemStack> getInventory() {
		return inventory;
	}

	@Override
	public boolean isStackValid(int slot, ItemStack stack) {
		return true;
	}

	@Override
	public int getSlotLimit(int slot) {
		return 64;
	}

	@Override
	public void doGraphicalUpdates(int slot) {
		this.markDirty();
		this.markContainingBlockForUpdate(null);
	}

	@Override
	public IFluidTank[] getInternalTanks() {
		return tanks;
	}
	
	@Override
	protected SolarTowerRecipe readRecipeFromNBT(NBTTagCompound tag) {
		return SolarTowerRecipe.loadFromNBT(tag);
	}

	@Override
	public SolarTowerRecipe findRecipeForInsertion(ItemStack inserting) {
		return null;
	}
	
	@Override
	public int[] getEnergyPos() {
		return new int[0];
	}

	@Override
	public int[] getRedstonePos() {
		return new int[] { 10 };
	}

	@Override
	public int[] getOutputSlots() {
		return new int[0];
	}

	@Override
	public int[] getOutputTanks() {
		return new int[] { 1 };
	}

	@Override
	public boolean additionalCanProcessCheck(MultiblockProcess<SolarTowerRecipe> process) {
		return true;
	}

	@Override
	public void doProcessOutput(ItemStack output) {
		BlockPos pos = getPos().offset(facing, 2);
		TileEntity inventoryTile = this.world.getTileEntity(pos);
		if(inventoryTile != null) output = Utils.insertStackIntoInventory(inventoryTile, output, facing.getOpposite());
		if(output != null) Utils.dropStackAtPos(world, pos, output, facing);
	}

	@Override
	public void doProcessFluidOutput(FluidStack output) {
	}

	@Override
	public void onProcessFinish(MultiblockProcess<SolarTowerRecipe> process) {
	}

	@Override
	public int getMaxProcessPerTick() {
		return 1;
	}

	@Override
	public int getProcessQueueMaxLength() {
		return 1;
	}

	@Override
	public float getMinProcessDistance(MultiblockProcess<SolarTowerRecipe> process) {
		return 0;
	}

	@Override
	public boolean isInWorldProcessingMachine() {
		return false;
	}

	@Override
	protected IFluidTank[] getAccessibleFluidTanks(EnumFacing side) {
		TileEntitySolarTower master = this.master();
		if(master != null) {
			if((pos == 3 || pos == 5) && (side == null || side.getAxis() == facing.rotateYCCW().getAxis())) {
				return new FluidTank[] { master.tanks[0] };
			} else if(pos == 7 && (side == null || side == facing)) {
				return new FluidTank[] { master.tanks[1] };
			}
		}
		return new FluidTank[0];
	}

	@Override
	protected boolean canFillTankFrom(int iTank, EnumFacing side, FluidStack resource) {
		TileEntitySolarTower master = this.master();
		if(master == null) return false;
		if((pos == 3 || pos == 5) && (side == null || side.getAxis() == facing.rotateYCCW().getAxis())) {
			if(master.tanks[iTank].getFluidAmount() >= master.tanks[iTank].getCapacity()) return false;
			if(master.tanks[iTank].getFluid() == null) return SolarTowerRecipe.findRecipeByFluid(resource.getFluid()) != null;
			else return resource.getFluid() == master.tanks[iTank].getFluid().getFluid();
		}
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
		return ITLib.GUIID_Solar_Tower;
	}

	@Override
	public TileEntity getGuiMaster() {
		return master();
	}

	@Override
	public TileEntitySolarTower getTileForPos(int targetPos) {
		BlockPos target = getBlockPosForPos(targetPos);
		TileEntity tile = world.getTileEntity(target);
		return tile instanceof TileEntitySolarTower ? (TileEntitySolarTower) tile : null;
	}

	@Override
	public float[] getBlockBounds() {
		if(pos == 0 || pos == 2 || pos == 6 || pos == 8) return new float[] { 0, 0, 0, 1, .5f, 1 };
		if(pos == 62 || pos == 60 || pos == 54 || pos == 52) return new float[] { 0, .5f, 0, 1, 1, 1 };
		return new float[] { 0, 0, 0, 1, 1, 1 };
	}

	@Override
	public List<AxisAlignedBB> getAdvancedColisionBounds() {
		return getAdvancedSelectionBounds();
	}

	@Override
	public List<AxisAlignedBB> getAdvancedSelectionBounds() {
		EnumFacing fl = facing;
		EnumFacing fw = facing.rotateY();
	int h = (pos - (pos % 9)) / 9;
	if(pos == 0 || pos == 2 || pos == 6 || pos == 8 || pos == 62 || pos == 60 || pos == 56 || pos == 54) {
		float minY = 0;
		float maxY = .5f;
		if(h == 6) {
			minY = .5f;
			maxY = 1;
		}
		List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(0, minY, 0, 1, maxY, 1)
				.offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		if(pos == 6 || pos == 8 || pos == 60 || pos == 62) fl = fl.getOpposite();
		if(pos == 2 || pos == 8 || pos == 56 || pos == 62) fw = fw.getOpposite();
		float minX = fl == EnumFacing.WEST ? .6875f : fl == EnumFacing.EAST ? .0625f : fw == EnumFacing.EAST ? .0625f : .6875f;
		float maxX = fl == EnumFacing.EAST ? .3125f : fl == EnumFacing.WEST ? .9375f : fw == EnumFacing.EAST ? .3125f : .9375f;
		float minZ = fl == EnumFacing.NORTH ? .6875f : fl == EnumFacing.SOUTH ? .0625f : fw == EnumFacing.SOUTH ? .0625f : .6875f;
		float maxZ = fl == EnumFacing.SOUTH ? .3125f : fl == EnumFacing.NORTH ? .9375f : fw == EnumFacing.SOUTH ? .3125f : .9375f;
		minY = .5f;
		maxY = 1;
		if(pos > 8) {
			minY = 0;
			maxY = .5f;
		}
		list.add(new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		minX = fl == EnumFacing.EAST ? .5f : fl == EnumFacing.WEST ? 0 : fw == EnumFacing.EAST ? .5f : 0;
		maxX = fl == EnumFacing.EAST ? 1 : fl == EnumFacing.WEST ? .5f : fw == EnumFacing.EAST ? 1 : .5f;
		list.add(new AxisAlignedBB(minX, minY, 0, maxX, maxY, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		minZ = fl == EnumFacing.NORTH ? 0 : fl == EnumFacing.SOUTH ? .5f : fw == EnumFacing.NORTH ? 0 : .5f;
		maxZ = fl == EnumFacing.NORTH ? .5f : fl == EnumFacing.SOUTH ? 1 : fw == EnumFacing.NORTH ? .5f : 1;
		list.add(new AxisAlignedBB(0, minY, minZ, 1, maxY, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		return list;
	}
	if((pos != 0 && pos != 54 && (pos % 9 == 0))) {
		float minX = fl == EnumFacing.WEST ? .6875f : fl == EnumFacing.EAST ? .0625f : fw == EnumFacing.EAST ? .0625f : .6875f;
		float maxX = fl == EnumFacing.EAST ? .3125f : fl == EnumFacing.WEST ? .9375f : fw == EnumFacing.EAST ? .3125f : .9375f;
		float minZ = fl == EnumFacing.NORTH ? .6875f : fl == EnumFacing.SOUTH ? .0625f : fw == EnumFacing.SOUTH ? .0625f : .6875f;
		float maxZ = fl == EnumFacing.SOUTH ? .3125f : fl == EnumFacing.NORTH ? .9375f : fw == EnumFacing.SOUTH ? .3125f : .9375f;
		List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(minX, 0, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		minX = fl == EnumFacing.EAST ? .5f : fl == EnumFacing.WEST ? 0 : fw == EnumFacing.EAST ? .5f : 0;
		maxX = fl == EnumFacing.EAST ? 1 : fl == EnumFacing.WEST ? .5f : fw == EnumFacing.EAST ? 1 : .5f;
		list.add(new AxisAlignedBB(minX, 0, 0, maxX, 1, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		minZ = fl == EnumFacing.NORTH ? 0 : fl == EnumFacing.SOUTH ? .5f : fw == EnumFacing.NORTH ? 0 : .5f;
		maxZ = fl == EnumFacing.NORTH ? .5f : fl == EnumFacing.SOUTH ? 1 : fw == EnumFacing.NORTH ? .5f : 1;
		list.add(new AxisAlignedBB(0, 0, minZ, 1, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		return list;
	}
	if(pos == 2 + 9 * h) {
		fw = fw.getOpposite();
		float minX = fl == EnumFacing.WEST ? .6875f : fl == EnumFacing.EAST ? .0625f : fw == EnumFacing.EAST ? .0625f : .6875f;
		float maxX = fl == EnumFacing.EAST ? .3125f : fl == EnumFacing.WEST ? .9375f : fw == EnumFacing.EAST ? .3125f : .9375f;
		float minZ = fl == EnumFacing.NORTH ? .6875f : fl == EnumFacing.SOUTH ? .0625f : fw == EnumFacing.SOUTH ? .0625f : .6875f;
		float maxZ = fl == EnumFacing.SOUTH ? .3125f : fl == EnumFacing.NORTH ? .9375f : fw == EnumFacing.SOUTH ? .3125f : .9375f;
		List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(minX, 0, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		minX = fl == EnumFacing.EAST ? .5f : fl == EnumFacing.WEST ? 0 : fw == EnumFacing.EAST ? .5f : 0;
		maxX = fl == EnumFacing.EAST ? 1 : fl == EnumFacing.WEST ? .5f : fw == EnumFacing.EAST ? 1 : .5f;
		list.add(new AxisAlignedBB(minX, 0, 0, maxX, 1, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		minZ = fl == EnumFacing.NORTH ? 0 : fl == EnumFacing.SOUTH ? .5f : fw == EnumFacing.NORTH ? 0 : .5f;
		maxZ = fl == EnumFacing.NORTH ? .5f : fl == EnumFacing.SOUTH ? 1 : fw == EnumFacing.NORTH ? .5f : 1;
		list.add(new AxisAlignedBB(0, 0, minZ, 1, 1, maxZ).offset(getPos().getX(), getPos().getY(), 	getPos().getZ()));
		return list;
	}
	if(pos == 6 + 9 * h) {
		fl = fl.getOpposite();
		float minX = fl == EnumFacing.WEST ? .6875f	: fl == EnumFacing.EAST ? .0625f : fw == EnumFacing.EAST ? .0625f : .6875f;
		float maxX = fl == EnumFacing.EAST ? .3125f	: fl == EnumFacing.WEST ? .9375f : fw == EnumFacing.EAST ? .3125f : .9375f;
		float minZ = fl == EnumFacing.NORTH ? .6875f : fl == EnumFacing.SOUTH ? .0625f : fw == EnumFacing.SOUTH ? .0625f : .6875f;
		float maxZ = fl == EnumFacing.SOUTH ? .3125f : fl == EnumFacing.NORTH ? .9375f : fw == EnumFacing.SOUTH ? .3125f : .9375f;
		List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(minX, 0, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		minX = fl == EnumFacing.EAST ? .5f : fl == EnumFacing.WEST ? 0 : fw == EnumFacing.EAST ? .5f : 0;
		maxX = fl == EnumFacing.EAST ? 1 : fl == EnumFacing.WEST ? .5f : fw == EnumFacing.EAST ? 1 : .5f;
		list.add(new AxisAlignedBB(minX, 0, 0, maxX, 1, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		minZ = fl == EnumFacing.NORTH ? 0 : fl == EnumFacing.SOUTH ? .5f : fw == EnumFacing.NORTH ? 0 : .5f;
		maxZ = fl == EnumFacing.NORTH ? .5f : fl == EnumFacing.SOUTH ? 1 : fw == EnumFacing.NORTH ? .5f : 1;
		list.add(new AxisAlignedBB(0, 0, minZ, 1, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		return list;
	}
	if(pos == 8 + 9 * h) {
		fl = fl.getOpposite();
		fw = fw.getOpposite();
		float minX = fl == EnumFacing.WEST ? .6875f : fl == EnumFacing.EAST ? .0625f : fw == EnumFacing.EAST ? .0625f : .6875f;
		float maxX = fl == EnumFacing.EAST ? .3125f : fl == EnumFacing.WEST ? .9375f : fw == EnumFacing.EAST ? .3125f : .9375f;
		float minZ = fl == EnumFacing.NORTH ? .6875f : fl == EnumFacing.SOUTH ? .0625f : fw == EnumFacing.SOUTH ? .0625f : .6875f;
		float maxZ = fl == EnumFacing.SOUTH ? .3125f : fl == EnumFacing.NORTH ? .9375f : fw == EnumFacing.SOUTH ? .3125f : .9375f;
		List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(minX, 0, minZ, maxX, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		minX = fl == EnumFacing.EAST ? .5f : fl == EnumFacing.WEST ? 0 : fw == EnumFacing.EAST ? .5f : 0;
		maxX = fl == EnumFacing.EAST ? 1 : fl == EnumFacing.WEST ? .5f : fw == EnumFacing.EAST ? 1 : .5f;
		list.add(new AxisAlignedBB(minX, 0, 0, maxX, 1, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		minZ = fl == EnumFacing.NORTH ? 0 : fl == EnumFacing.SOUTH ? .5f : fw == EnumFacing.NORTH ? 0 : .5f;
		maxZ = fl == EnumFacing.NORTH ? .5f : fl == EnumFacing.SOUTH ? 1 : fw == EnumFacing.NORTH ? .5f : 1;
		list.add(new AxisAlignedBB(0, 0, minZ, 1, 1, maxZ).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		return list;
	}
	return null;
	}

	@Override
	public boolean isOverrideBox(AxisAlignedBB box, EntityPlayer player, RayTraceResult mop, 
		ArrayList<AxisAlignedBB> list) {
	return false;
	}

}
package mctmods.immersivetechnology.common.blocks.metal.tileentities;

import blusunrize.immersiveengineering.common.util.Utils;
import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.api.ITUtils;
import mctmods.immersivetechnology.api.crafting.SolarTowerRecipe;
import mctmods.immersivetechnology.common.Config.ITConfig.Machines.SolarReflector;
import mctmods.immersivetechnology.common.Config.ITConfig.Machines.SolarTower;
import mctmods.immersivetechnology.common.util.ITFluidTank;
import mctmods.immersivetechnology.common.util.ITSounds;
import mctmods.immersivetechnology.common.util.network.MessageStopSound;
import mctmods.immersivetechnology.common.util.network.MessageTileSync;
import mctmods.immersivetechnology.common.util.sound.ITSoundHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

public class TileEntitySolarTowerMaster extends TileEntitySolarTowerSlave implements ITFluidTank.TankListener {

	private static int inputTankSize = SolarTower.solarTower_input_tankSize;
	private static int outputTankSize = SolarTower.solarTower_output_tankSize;	
	private static int solarMaxRange = SolarReflector.solarReflector_maxRange;
	private static int solarMinRange = SolarReflector.solarReflector_minRange;
	private static int progressLossPerTick = SolarTower.solarTower_progress_lossInTicks;
	private static int heatLossPerTick = SolarTower.solarTower_heat_lossPerTick;
	private static float speedMult = SolarTower.solarTower_speed_multiplier;
	private static float reflectorSpeedMult = SolarTower.solarTower_solarReflector_speed_multiplier;
	private static double workingHeatLevel = SolarTower.solarTower_heat_workingLevel;
	BlockPos fluidOutputPos;

	public FluidTank[] tanks = new FluidTank[] {
			new ITFluidTank(inputTankSize, this),
			new ITFluidTank(outputTankSize, this)
	};

	public static int slotCount = 4;
	public NonNullList<ItemStack> inventory = NonNullList.withSize(slotCount, ItemStack.EMPTY);

	public int recipeTimeRemaining = 0;
	public double heatLevel = 0;
	public int[] reflectors = new int[4];
	private int clientUpdateCooldown = 20;

	private SolarTowerRecipe lastRecipe;

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket) {
		super.readCustomNBT(nbt, descPacket);
		tanks[0].readFromNBT(nbt.getCompoundTag("tank0"));
		tanks[1].readFromNBT(nbt.getCompoundTag("tank1"));
		heatLevel = nbt.getDouble("heatLevel");
		recipeTimeRemaining = nbt.getInteger("recipeTimeRemaining");
		reflectors = nbt.getIntArray("reflectors");
		if(reflectors.length != 4) reflectors = new int[4];
		if(!descPacket) inventory = Utils.readInventory(nbt.getTagList("inventory", 10), slotCount);
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket) {
		super.writeCustomNBT(nbt, descPacket);
		nbt.setTag("tank0", tanks[0].writeToNBT(new NBTTagCompound()));
		nbt.setTag("tank1", tanks[1].writeToNBT(new NBTTagCompound()));
		nbt.setDouble("heatLevel", heatLevel);
		nbt.setInteger("recipeTimeRemaining", recipeTimeRemaining);
		nbt.setIntArray("reflectors", reflectors);
		if(!descPacket) nbt.setTag("inventory", Utils.writeInventory(inventory));
	}

	protected boolean checkReflector() {
		boolean update = false;
		if(!world.isDaytime()) {
			for(int cont = 0; cont < 4; cont++) {
				reflectors[cont] = 0;
			}
			return update;
		} else if(world.isRaining()) {
			for(int cont = 0; cont < 4; cont++) {
				reflectors[cont] = 0;
			}
			update = true;
			return update;
		}
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
					if(tile instanceof TileEntitySolarReflectorMaster) {
						fr = ((TileEntitySolarReflectorMaster) tile).facing;
						if((cont % 2 == 0 && (facing == EnumFacing.NORTH || facing == EnumFacing.SOUTH)) || (cont % 2 != 0 && (facing == EnumFacing.EAST || facing == EnumFacing.WEST))) {
							if(fr == EnumFacing.NORTH || fr == EnumFacing.SOUTH) {
								if(((TileEntitySolarReflectorMaster) tile).canSeeSun()) {
									update = true;
									reflectors[cont] = 1;
								}
								break;
							}
						} else {
							if(fr == EnumFacing.EAST || fr == EnumFacing.WEST) {
								if(((TileEntitySolarReflectorMaster) tile).canSeeSun()) {
									update = true;
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
		return update;
	}

	private boolean heatUp() {
		double previousHeatLevel = heatLevel;
		double temp = 0.1;
		if(!world.isRaining()) temp = world.getBiomeProvider().getTemperatureAtHeight(world.getBiome(this.getPos()).getTemperature(this.getPos()), this.getPos().getY());
		heatLevel = Math.min(getSpeed() + (heatLevel + temp), workingHeatLevel);
		return previousHeatLevel != heatLevel;
	}

	protected float getSpeed() {
		int activeReflectors = 0;
		for(int reflectorValue : reflectors) activeReflectors += reflectorValue;
		if(activeReflectors == 0) return 0;
		return speedMult * (1 + (activeReflectors - 1) * (reflectorSpeedMult - 1));
	}

	private boolean cooldown() {
		double previousHeatLevel = heatLevel;
		double temp = world.getBiomeProvider().getTemperatureAtHeight(world.getBiome(this.getPos()).getTemperature(this.getPos()), this.getPos().getY());
		heatLevel = Math.max((heatLevel - temp) - heatLossPerTick, 0);
		return previousHeatLevel != heatLevel;
	}

	private boolean loseProgress() {
		int previousProgress = recipeTimeRemaining;
		if(lastRecipe == null) {
			recipeTimeRemaining = 0;
			return true;
		}
		recipeTimeRemaining = Math.min(recipeTimeRemaining + progressLossPerTick, lastRecipe.getTotalProcessTime());
		return previousProgress != recipeTimeRemaining;
	}

	private boolean gainProgress() {
		if(lastRecipe == null) {
			recipeTimeRemaining = 0;
			return true;
		}
		recipeTimeRemaining--;
		if(recipeTimeRemaining == 0) {
			tanks[0].drain(lastRecipe.fluidInput.amount, true);
			tanks[1].fillInternal(lastRecipe.fluidOutput, true);
			markContainingBlockForUpdate(null);
			return true;
		}
		return false;
	}

	private void pumpOutputOut() {
		if(tanks[1].getFluidAmount() == 0) return;
		if(fluidOutputPos == null) fluidOutputPos = ITUtils.LocalOffsetToWorldBlockPos(this.getPos(), 0, -1, 3, facing, mirrored);
		IFluidHandler output = FluidUtil.getFluidHandler(world, fluidOutputPos, facing.getOpposite());
		if(output == null) return;
		FluidStack out = tanks[1].getFluid();
		int accepted = output.fill(out, false);
		if(accepted == 0) return;
		int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
		this.tanks[1].drain(drained, true);
	}

	public void handleSounds() {
		BlockPos center = getPos();
		float level = (float) (heatLevel / workingHeatLevel);
		if(level == 0) ITSoundHandler.StopSound(center);
		else {
			EntityPlayerSP player = Minecraft.getMinecraft().player;
			float attenuation = Math.max((float) player.getDistanceSq(center.getX(), center.getY(), center.getZ()) / 8, 1);
			ITSounds.solarTower.PlayRepeating(center, (2 * level) / attenuation, level);
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
		BlockPos center = getPos();
		ImmersiveTechnology.packetHandler.sendToAllTracking(new MessageStopSound(center), new NetworkRegistry.TargetPoint(world.provider.getDimension(), center.getX(), center.getY(), center.getZ(), 0));
		super.disassemble();
	}

	public void notifyNearbyClients() {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setDouble("heat", heatLevel);
		BlockPos center = getPos();
		ImmersiveTechnology.packetHandler.sendToAllAround(new MessageTileSync(this, tag), new NetworkRegistry.TargetPoint(world.provider.getDimension(), center.getX(), center.getY(), center.getZ(), 40));
	}

	@Override
	public void receiveMessageFromServer(NBTTagCompound message) {
		heatLevel = message.getDouble("heat");
	}

	public void efficientMarkDirty() { // !!!!!!! only use it within update() function !!!!!!!
		world.getChunkFromBlockCoords(this.getPos()).markDirty();
	}

	private boolean heatLogic() {
		boolean update = false;
		if(checkReflector()) {
			if(!isRSDisabled()) {
				if(heatUp()) update = true;
			} else if(cooldown()) update = true;
		} else if(cooldown()) update = true;
		return update;
	}

	private boolean recipeLogic() {
		boolean update = false;
		if(heatLevel >= workingHeatLevel) {
			if(recipeTimeRemaining > 0) {
				if(gainProgress()) update = true;
			} else if(tanks[0].getFluid() != null) {
				SolarTowerRecipe recipe = (lastRecipe != null && tanks[0].getFluid().isFluidEqual(lastRecipe.fluidInput)) ?	lastRecipe : SolarTowerRecipe.findRecipe(tanks[0].getFluid());
				if(recipe != null && recipe.fluidInput.amount <= tanks[0].getFluidAmount() && recipe.fluidOutput.amount == tanks[1].fillInternal(recipe.fluidOutput, false)) {
					lastRecipe = recipe;
					recipeTimeRemaining = recipe.getTotalProcessTime();
					gainProgress();
					update = true;
				}
			}
		} else if(recipeTimeRemaining > 0) {
			if(loseProgress()) update = true;
		}
		return update;
	}

	private boolean outputTankLogic() {
		boolean update = false;
		if(this.tanks[1].getFluidAmount() > 0) {
			ItemStack filledContainer = Utils.fillFluidContainer(tanks[1], inventory.get(2), inventory.get(3), null);
			if(!filledContainer.isEmpty()) {
				if(!inventory.get(3).isEmpty() && OreDictionary.itemMatches(inventory.get(3), filledContainer, true)) inventory.get(3).grow(filledContainer.getCount());
				else if(inventory.get(3).isEmpty()) inventory.set(3, filledContainer.copy());
				inventory.get(2).shrink(1);
				if(inventory.get(2).getCount() <= 0) inventory.set(2, ItemStack.EMPTY);
				markContainingBlockForUpdate(null);
				update = true;
			}
			pumpOutputOut();
		}
		return update;
	}

	private boolean inputTankLogic() {
		int amount_prev = tanks[0].getFluidAmount();
		ItemStack emptyContainer = Utils.drainFluidContainer(tanks[0], inventory.get(0), inventory.get(1), null);
		if(amount_prev != tanks[0].getFluidAmount()) {
			if(!inventory.get(1).isEmpty() && OreDictionary.itemMatches(inventory.get(1), emptyContainer, true)) inventory.get(1).grow(emptyContainer.getCount());
			else if(inventory.get(1).isEmpty()) inventory.set(1, emptyContainer.copy());
			inventory.get(0).shrink(1);
			if(inventory.get(0).getCount() <= 0) inventory.set(0, ItemStack.EMPTY);
			markContainingBlockForUpdate(null);
			return true;
		}
		return false;
	}

	@Override
	public void update() {
		if(!formed) return;
		if(world.isRemote) {
			handleSounds();
			return;
		}
		boolean update = false;
		if(heatLogic()) update = true;
		if(recipeLogic()) update = true;
		if(outputTankLogic()) update = true;
		if(inputTankLogic()) update = true;
		if(clientUpdateCooldown > 1) clientUpdateCooldown--;
		if(update) {
			efficientMarkDirty();
			if(clientUpdateCooldown == 1) {
				notifyNearbyClients();
				clientUpdateCooldown = 20;
			}
		}
	}

	@Override
	public void TankContentsChanged() {
		this.markContainingBlockForUpdate(null);
	}

	@Override
	public boolean isDummy() {
		return false;
	}

	@Override
	public TileEntitySolarTowerMaster master() {
		master = this;
		return this;
	}

}
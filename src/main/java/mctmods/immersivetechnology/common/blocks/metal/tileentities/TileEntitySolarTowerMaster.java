package mctmods.immersivetechnology.common.blocks.metal.tileentities;

import blusunrize.immersiveengineering.common.util.Utils;
import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.api.ITUtils;
import mctmods.immersivetechnology.api.crafting.SolarTowerRecipe;
import mctmods.immersivetechnology.common.Config.ITConfig.Machines.SolarReflector;
import mctmods.immersivetechnology.common.Config.ITConfig.Machines.SolarTower;
import mctmods.immersivetechnology.common.ITContent;
import mctmods.immersivetechnology.common.util.ITFluidTank;
import mctmods.immersivetechnology.common.util.ITSounds;
import mctmods.immersivetechnology.common.util.compat.ITCompatModule;
import mctmods.immersivetechnology.common.util.compat.advancedrocketry.AdvancedRocketryHelper;
import mctmods.immersivetechnology.common.util.network.MessageStopSound;
import mctmods.immersivetechnology.common.util.network.MessageTileSync;
import mctmods.immersivetechnology.common.util.sound.ITSoundHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
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
	private static double heatLossMultiplier = SolarTower.solarTower_heat_loss_multiplier;
	private static float speedMult = SolarTower.solarTower_speed_multiplier;
	private static double workingHeatLevel = SolarTower.solarTower_heat_workingLevel;
	private static double maximumReflectorStrength = SolarTower.solarTower_maximum_reflector_strength;

	BlockPos fluidOutputPos;

	public FluidTank[] tanks = new FluidTank[] {
			new ITFluidTank(inputTankSize, this),
			new ITFluidTank(outputTankSize, this)
	};

	public static int slotCount = 4;
	public NonNullList<ItemStack> inventory = NonNullList.withSize(slotCount, ItemStack.EMPTY);

	public int recipeTimeRemaining = 0;
	public double heatLevel = 0;
	public double reflectorStrength = 0;
	public int solarIncidenceAngleSection = 0;
	private int clientUpdateCooldown = 20;

	private SolarTowerRecipe lastRecipe;

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket) {
		super.readCustomNBT(nbt, descPacket);
		tanks[0].readFromNBT(nbt.getCompoundTag("tank0"));
		tanks[1].readFromNBT(nbt.getCompoundTag("tank1"));
		heatLevel = nbt.getDouble("heatLevel");
		recipeTimeRemaining = nbt.getInteger("recipeTimeRemaining");
		reflectorStrength = nbt.getDouble("reflectorStrength");
		solarIncidenceAngleSection = nbt.getInteger("solarIncidenceAngleSection");
		if(!descPacket) inventory = Utils.readInventory(nbt.getTagList("inventory", 10), slotCount);
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket) {
		super.writeCustomNBT(nbt, descPacket);
		nbt.setTag("tank0", tanks[0].writeToNBT(new NBTTagCompound()));
		nbt.setTag("tank1", tanks[1].writeToNBT(new NBTTagCompound()));
		nbt.setDouble("heatLevel", heatLevel);
		nbt.setInteger("recipeTimeRemaining", recipeTimeRemaining);
		nbt.setDouble("reflectorStrength", reflectorStrength);
		nbt.setInteger("solarIncidenceAngleSection", getSolarIncidenceAngleSection());
		if(!descPacket) nbt.setTag("inventory", Utils.writeInventory(inventory));
	}

	protected boolean checkReflectorPositions() {
		double totalMirrorStrength = 0;
		for (int x = -(solarMaxRange + 1); x <= (solarMaxRange + 1); x++) {
			for (int z = -(solarMaxRange + 1); z <= (solarMaxRange + 1); z++) {
				double distance = Math.sqrt(this.getPos().distanceSq(this.getPos().add(x, 0, z)));
				if (distance >= solarMinRange && distance <= solarMaxRange && Utils.isBlockAt(world, this.getPos().add(x, 0, z), ITContent.blockMetalMultiblock, 2)) {
					TileEntity tile = world.getTileEntity(this.getPos().add(x, 0, z));
					if (tile instanceof TileEntitySolarReflectorMaster && ((TileEntitySolarReflectorMaster) tile).setTowerCollectorPosition(this.getPos().add(0, 17, 0))) {
						totalMirrorStrength += ((TileEntitySolarReflectorMaster) tile).getSolarCollectorStrength();
					}
				}
			}
		}
		//Factors that influence heat production
		//Rain multiplier, Combines with light level adjustments to become 0.1x
		totalMirrorStrength *= (world.isRaining() ? 0.4f : 1f);

		//Insolation multiplier
		if (ITCompatModule.isAdvancedRocketryLoaded)
			totalMirrorStrength *= AdvancedRocketryHelper.getInsolation(world, this.getPos());

		//Humidity multiplier
		double humidityBonus = 0.075 * totalMirrorStrength * -((world.getBiome(this.getPos()).getRainfall() - 0.5)/0.5);
		if (ITCompatModule.isAdvancedRocketryLoaded) {
			humidityBonus *= AdvancedRocketryHelper.getWaterPartialPressureMultiplier(world, this.getPos());
		}
        totalMirrorStrength += humidityBonus;

		//Final set
		reflectorStrength = totalMirrorStrength;
		return getSolarIncidenceAngleSection() != 0;
	}

	protected void detachMirrors() {
		for (int x = -(solarMaxRange + 1); x <= (solarMaxRange + 1); x++) {
			for (int z = -(solarMaxRange + 1); z <= (solarMaxRange + 1); z++) {
				double distance = Math.sqrt(this.getPos().distanceSq(this.getPos().add(x, 0, z)));
				if (distance >= solarMinRange && distance <= solarMaxRange && Utils.isBlockAt(world, this.getPos().add(x, 0, z), ITContent.blockMetalMultiblock, 2)) {
					TileEntity tile = world.getTileEntity(this.getPos().add(x, 0, z));
					if (tile instanceof TileEntitySolarReflectorMaster && ((TileEntitySolarReflectorMaster) tile).setTowerCollectorPosition(this.getPos().add(0, 17, 0))) {
						((TileEntitySolarReflectorMaster) tile).detachTower(this.getPos());
					}
				}
			}
		}
	}

	private boolean heatUp() {
		double previousHeatLevel = heatLevel;
		heatLevel = Math.min(getTemperatureIncrease() + heatLevel, workingHeatLevel);
		return previousHeatLevel != heatLevel;
	}

	protected float getTemperatureIncrease() {
		return speedMult * (1 + (getSolarIncidenceAngleSection() - 1)) * 10 * (float)(reflectorStrength/maximumReflectorStrength) * (world.isRaining() ? 0.1f : world.isThundering() ? 0.05f : 1f);
	}

	private boolean cooldown() {
		double previousHeatLevel = heatLevel;
		double heatLost = world.getBiomeProvider().getTemperatureAtHeight(world.getBiome(this.getPos()).getTemperature(this.getPos()), this.getPos().getY());
		double conductionMultiplier = 1.0;
		if(ITCompatModule.isAdvancedRocketryLoaded)
			conductionMultiplier *= AdvancedRocketryHelper.getHeatTransferCoefficient(world, this.getPos().add(0, 19, 0));
		heatLevel = Math.max((heatLevel - ((world.isRaining() ? 2 : 1 * (1/heatLost)) * heatLossMultiplier * conductionMultiplier)), 0);
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
			tanks[0].drain((int)(lastRecipe.fluidInput.amount * reflectorStrength/maximumReflectorStrength), true);
			tanks[1].fillInternal(new FluidStack(lastRecipe.fluidOutput.getFluid(), (int)(lastRecipe.fluidOutput.amount * reflectorStrength/maximumReflectorStrength)), true);
			markContainingBlockForUpdate(null);
			return true;
		}
		return false;
	}

	private void pumpOutputOut() {
		if(tanks[1].getFluidAmount() == 0) return;
		if(fluidOutputPos == null) fluidOutputPos = ITUtils.LocalOffsetToWorldBlockPos(this.getPos(), 0, -1, -2, facing);
		IFluidHandler output = FluidUtil.getFluidHandler(world, fluidOutputPos, facing.getOpposite());
		if(output == null) return;
		FluidStack out = tanks[1].getFluid();
		int accepted = output.fill(out, false);
		if(accepted == 0) return;
		int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
		this.tanks[1].drain(drained, true);
	}

	public void handleSounds() {
		BlockPos center = this.getPos();
		float level = (float) (heatLevel / workingHeatLevel);
		if(level == 0) ITSoundHandler.StopSound(center);
		else {
			EntityPlayerSP player = Minecraft.getMinecraft().player;
			float attenuation = Math.max((float) player.getDistanceSq(center.getX(), center.getY(), center.getZ()) / 8, 1);
			ITSounds.solarTower.PlayRepeating(center, (2 * level) / attenuation, level);
		}
	}

	public int getSolarIncidenceAngleSection() {
		if (world.getSkylightSubtracted() == 3) return 1;
		else if(world.getSkylightSubtracted() == 2) return 2;
		else if(world.getSkylightSubtracted() == 1) return 3;
		else if(world.getSkylightSubtracted() == 0) return 4;
		return 0;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void onChunkUnload() {
		if(!isDummy()) ITSoundHandler.StopSound(this.getPos());
		super.onChunkUnload();
	}

	@Override
	public void disassemble() {
		BlockPos center = this.getPos();
		ImmersiveTechnology.packetHandler.sendToAllTracking(new MessageStopSound(center), new NetworkRegistry.TargetPoint(world.provider.getDimension(), center.getX(), center.getY(), center.getZ(), 0));
		detachMirrors();
		super.disassemble();
	}

	public void notifyNearbyClients() {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setDouble("heat", heatLevel);
		tag.setInteger("solarIncidenceAngleSection", getSolarIncidenceAngleSection());
		BlockPos center = this.getPos();
		ImmersiveTechnology.packetHandler.sendToAllAround(new MessageTileSync(this, tag), new NetworkRegistry.TargetPoint(world.provider.getDimension(), center.getX(), center.getY(), center.getZ(), 40));
	}

	@Override
	public void receiveMessageFromServer(NBTTagCompound message) {
		heatLevel = message.getDouble("heat");
		solarIncidenceAngleSection = message.getInteger("solarIncidenceAngleSection");
	}

	public void efficientMarkDirty() { // !!!!!!! only use it within update() function !!!!!!!
		world.getChunkFromBlockCoords(this.getPos()).markDirty();
	}

	private boolean heatLogic() {
		boolean update = false;
		if(getSolarIncidenceAngleSection() != 0) {
			if(heatUp()) update = true;
		} else if(cooldown()) update = true;
		return update;
	}

	private boolean recipeLogic() {
		boolean update = false;
		if(heatLevel >= workingHeatLevel && !isRSDisabled()) {
			if(recipeTimeRemaining > 0) {
				if(gainProgress()) update = true;
			} else if(tanks[0].getFluid() != null) {
				SolarTowerRecipe recipe = (lastRecipe != null && tanks[0].getFluid().isFluidEqual(lastRecipe.fluidInput)) ?	lastRecipe : SolarTowerRecipe.findRecipe(tanks[0].getFluid());
				if(recipe != null && recipe.fluidInput.amount <= tanks[0].getFluidAmount() && recipe.fluidOutput.amount == tanks[1].fillInternal(recipe.fluidOutput, false)) {
					lastRecipe = recipe;
					recipeTimeRemaining = (int)(recipe.getTotalProcessTime() / (speedMult * getSolarIncidenceAngleSection()));
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
		//Heat
		if (world.getTotalWorldTime() % 600 == 0) checkReflectorPositions();
		if (heatLogic()) update = true;
		//Recipes
		if (getSolarIncidenceAngleSection() != 0) {
			if (recipeLogic()) update = true;
		}
		//Tank outputs & inputs
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
package mctmods.immersivetechnology.common.blocks.metal.tileentities;

import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.api.ITUtils;
import mctmods.immersivetechnology.api.crafting.MeltingCrucibleRecipe;
import mctmods.immersivetechnology.common.Config;
import mctmods.immersivetechnology.common.Config.ITConfig.Machines.SolarReflector;
import mctmods.immersivetechnology.common.Config.ITConfig.Machines.SolarMelter;
import mctmods.immersivetechnology.common.ITContent;
import mctmods.immersivetechnology.common.util.ITFluidTank;
import mctmods.immersivetechnology.common.util.ITSounds;
import mctmods.immersivetechnology.common.util.compat.ITCompatModule;
import mctmods.immersivetechnology.common.util.compat.advancedrocketry.AdvancedRocketryHelper;
import mctmods.immersivetechnology.common.util.network.MessageStopSound;
import mctmods.immersivetechnology.common.util.sound.ITSoundHandler;
import net.minecraft.client.Minecraft;
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
import net.minecraftforge.items.IItemHandler;

public class TileEntitySolarMelterMaster extends TileEntitySolarMelterSlave implements ITFluidTank.TankListener {

	private static int outputTankSize = SolarMelter.solarMelter_output_tankSize;
	private static int solarMaxRange = SolarReflector.solarReflector_maxRange;
	private static int solarMinRange = SolarReflector.solarReflector_minRange;
	private static int energyLossPerTick = SolarMelter.solarMelter_progress_lossEnergy;
	private static double maximumReflectorStrength = SolarMelter.solarMelter_maximum_reflector_strength;

	BlockPos fluidOutputPos;

	public FluidTank[] tanks = new FluidTank[] {
			new ITFluidTank(outputTankSize, this)
	};

	public static int slotCount = 1;
	public NonNullList<ItemStack> inventory = NonNullList.withSize(slotCount, ItemStack.EMPTY);

	IItemHandler insertionHandler = new IEInventoryHandler(slotCount, this, 0, new boolean[]{true}, new boolean[1]);

	public int recipeEnergyRemaining = 0;
	public double reflectorStrength = 0;
	public int solarIncidenceAngleSection = 0;
	private int soundVolume = 20;

	private MeltingCrucibleRecipe lastRecipe;

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket) {
		super.readCustomNBT(nbt, descPacket);
		tanks[0].readFromNBT(nbt.getCompoundTag("tank0"));
		recipeEnergyRemaining = nbt.getInteger("recipeEnergyRemaining");
		reflectorStrength = nbt.getDouble("reflectorStrength");
		solarIncidenceAngleSection = nbt.getInteger("solarIncidenceAngleSection");
		inventory = Utils.readInventory(nbt.getTagList("inventory", 10), slotCount);
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket) {
		super.writeCustomNBT(nbt, descPacket);
		nbt.setTag("tank0", tanks[0].writeToNBT(new NBTTagCompound()));
		nbt.setInteger("recipeEnergyRemaining", recipeEnergyRemaining);
		nbt.setDouble("reflectorStrength", reflectorStrength);
		nbt.setInteger("solarIncidenceAngleSection", getSolarIncidenceAngleSection());
		nbt.setTag("inventory", Utils.writeInventory(inventory));
	}

	protected boolean checkReflectorPositions() {
		double totalMirrorStrength = 0;
		for (int x = -(solarMaxRange + 1); x <= (solarMaxRange + 1); x++) {
			for (int z = -(solarMaxRange + 1); z <= (solarMaxRange + 1); z++) {
				double distance = Math.sqrt(this.getPos().distanceSq(this.getPos().add(x, -1, z)));
				if (distance >= solarMinRange && distance <= solarMaxRange && Utils.isBlockAt(world, this.getPos().add(x, -1, z), ITContent.blockMetalMultiblock, 2)) {
					TileEntity tile = world.getTileEntity(this.getPos().add(x, -1, z));
					if (tile instanceof TileEntitySolarReflectorMaster && ((TileEntitySolarReflectorMaster) tile).setTowerCollectorPosition(this.getPos().add(0, 16, 0))) {
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

	private boolean loseProgress() {
		int previousProgress = recipeEnergyRemaining;
		if(lastRecipe == null) {
			recipeEnergyRemaining = 0;
			return true;
		}
		recipeEnergyRemaining = (int)Math.min(recipeEnergyRemaining + energyLossPerTick * (ITCompatModule.isAdvancedRocketryLoaded ? AdvancedRocketryHelper.getHeatTransferCoefficient(world, this.getPos()) : 1), lastRecipe.getTotalProcessEnergy());
		return previousProgress != recipeEnergyRemaining;
	}

	private boolean gainProgress() {
		if(lastRecipe == null) {
			recipeEnergyRemaining = 0;
			return true;
		}
		//Because at max one tower can power one turbine and the tower is assumed to be ~39% efficient, we can direct up to ~2.5x the power from a turbine into the melter
		//getSolarIncidenceAngleSection returns at max 4, so we do 12288 * 2.5 / 4 and get 7680 as our base input with minimum solar incidence angle section
		recipeEnergyRemaining -= getSolarIncidenceAngleSection() * 7680 * (reflectorStrength / maximumReflectorStrength);
		if(recipeEnergyRemaining <= 0) {
			inventory.get(0).shrink(lastRecipe.itemInput.inputSize);
			tanks[0].fillInternal(new FluidStack(lastRecipe.fluidOutput.getFluid(), lastRecipe.fluidOutput.amount), true);
			markContainingBlockForUpdate(null);
			return true;
		}
		return false;
	}

	private void pumpOutputOut() {
		if(tanks[0].getFluidAmount() == 0) return;
		if(fluidOutputPos == null) fluidOutputPos = ITUtils.LocalOffsetToWorldBlockPos(this.getPos(), 0, -2, -2, facing);
		IFluidHandler output = FluidUtil.getFluidHandler(world, fluidOutputPos, facing.getOpposite());
		if(output == null) return;
		FluidStack out = tanks[0].getFluid();
		int accepted = output.fill(out, false);
		if(accepted == 0) return;
		int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
		this.tanks[0].drain(drained, true);
	}

	public void handleSounds() {
		BlockPos center = this.getPos();
		if(lastRecipe != null) {
			if(soundVolume < 1) soundVolume += 0.02f;
		} else {
			if(soundVolume > 0) soundVolume -= 0.02f;
		}
		if(soundVolume == 0) ITSoundHandler.StopSound(center);
		else {
			float attenuation = Math.max((float) Minecraft.getMinecraft().player.getDistanceSq(center.getX(), center.getY(), center.getZ()) / 8, 1);
			ITSounds.heatExchanger.PlayRepeating(center, soundVolume / attenuation, 1);
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
		super.disassemble();
	}

	public void efficientMarkDirty() { // !!!!!!! only use it within update() function !!!!!!!
		world.getChunkFromBlockCoords(this.getPos()).markDirty();
	}

	private boolean recipeLogic() {
		boolean update = false;
		if(!isRSDisabled()) {
			if(recipeEnergyRemaining > 0) {
				if(gainProgress()) update = true;
			} else if(!inventory.get(0).isEmpty()) {
				MeltingCrucibleRecipe recipe = (lastRecipe != null && lastRecipe.itemInput.matches(inventory.get(0))) ? lastRecipe : MeltingCrucibleRecipe.findRecipe(inventory.get(0));
				if(recipe != null && recipe.fluidOutput.amount == tanks[0].fillInternal(recipe.fluidOutput, false)) {
					lastRecipe = recipe;
					recipeEnergyRemaining = recipe.getTotalProcessEnergy();
					gainProgress();
					update = true;
				}
			}
		} else if(recipeEnergyRemaining > 0) {
			if(loseProgress()) update = true;
		}
		return update;
	}

	private boolean outputTankLogic() {
		boolean update = false;
		if(this.tanks[0].getFluidAmount() > 0) {
			update = true;
			pumpOutputOut();
		}
		return update;
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
		//Recipes
		if (getSolarIncidenceAngleSection() != 0) {
			if (recipeLogic()) update = true;
		}
		//Tank outputs & inputs
		if(outputTankLogic()) update = true;
		if(update) {
			efficientMarkDirty();
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
	public TileEntitySolarMelterMaster master() {
		master = this;
		return this;
	}

}
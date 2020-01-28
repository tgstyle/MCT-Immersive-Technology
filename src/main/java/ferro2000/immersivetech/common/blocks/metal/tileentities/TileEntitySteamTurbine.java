package ferro2000.immersivetech.common.blocks.metal.tileentities;

import java.util.ArrayList;
import java.util.List;

import blusunrize.immersiveengineering.common.util.Utils;

import com.google.common.collect.Lists;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedCollisionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedSelectionBounds;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMultiblockMetal;

import ferro2000.immersivetech.api.ITUtils;
import ferro2000.immersivetech.api.client.MechanicalEnergyAnimation;
import ferro2000.immersivetech.api.crafting.SteamTurbineRecipe;
import ferro2000.immersivetech.common.Config;
import ferro2000.immersivetech.common.Config.ITConfig;
import ferro2000.immersivetech.common.blocks.ITBlockInterface.IMechanicalEnergy;
import ferro2000.immersivetech.common.blocks.metal.multiblocks.MultiblockSteamTurbine;

import ferro2000.immersivetech.common.util.ITSoundHandler;
import ferro2000.immersivetech.common.util.ITSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
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

public class TileEntitySteamTurbine extends TileEntityMultiblockMetal<TileEntitySteamTurbine, SteamTurbineRecipe> implements IAdvancedSelectionBounds, IAdvancedCollisionBounds, IMechanicalEnergy {
	public TileEntitySteamTurbine() {
		super(MultiblockSteamTurbine.instance, new int[] { 4, 10, 3 }, 0, true);
	}

	private static int maxSpeed = ITConfig.Machines.mechanicalEnergy_maxSpeed;
	private static int speedGainPerTick = ITConfig.Machines.steamTurbine_speedGainPerTick;
	private static int speedLossPerTick = ITConfig.Machines.steamTurbine_speedLossPerTick;
	private static int energyMaxSpeed = ITConfig.Machines.mechanicalEnergy_maxSpeed;
	private static int inputTankSize = Config.ITConfig.Machines.steamTurbine_input_tankSize;
	private static int outputTankSize = Config.ITConfig.Machines.steamTurbine_input_tankSize;
	private static float maxRotationSpeed = ITConfig.Machines.steamTurbine_maxRotationSpeed;
	
	public FluidTank[] tanks = new FluidTank[] {
		new FluidTank(inputTankSize),
		new FluidTank(outputTankSize)
	};

	public int burnRemaining = 0;
	public int speed;

	public static BlockPos fluidOutputPos;

	public SteamTurbineRecipe lastRecipe;

	private ITSoundHandler runningSound;

	MechanicalEnergyAnimation animation = new MechanicalEnergyAnimation();

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket) {
		super.readCustomNBT(nbt, descPacket);
		tanks[0].readFromNBT(nbt.getCompoundTag("tank0"));
		tanks[1].readFromNBT(nbt.getCompoundTag("tank1"));
		speed = nbt.getInteger("speed");
		animation.readFromNBT(nbt);
		burnRemaining = nbt.getInteger("burnRemaining");
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket) {
		super.writeCustomNBT(nbt, descPacket);
		nbt.setTag("tank0", tanks[0].writeToNBT(new NBTTagCompound()));
		nbt.setTag("tank1", tanks[1].writeToNBT(new NBTTagCompound()));
		nbt.setInteger("speed", speed);
		animation.writeToNBT(nbt);
		nbt.setInteger("burnRemaining", burnRemaining);
	}

	private void speedUp() {
		speed = Math.min(maxSpeed, speed + speedGainPerTick);
	}

	private void speedDown() {
		speed = Math.max(0, speed - speedLossPerTick);
	}

	private void pumpOutputOut() {
		if(tanks[1].getFluidAmount() == 0) return;
		if(fluidOutputPos == null) fluidOutputPos = ITUtils.LocalOffsetToWorldBlockPos(this.getPos(), 0, 2, 8, facing);
		IFluidHandler output = FluidUtil.getFluidHandler(world, fluidOutputPos, facing.getOpposite());
		if(output == null) return;
		FluidStack out = tanks[1].getFluid();
		int accepted = output.fill(out, false);
		if(accepted == 0) return;
		int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
		this.tanks[1].drain(drained, true);
		this.markDirty();
		this.markContainingBlockForUpdate(null);
	}

	public void handleSounds() {
		if (runningSound == null) runningSound = new ITSoundHandler(this, ITSounds.turbine, SoundCategory.BLOCKS, true, 10, 1, getPos().offset(facing, 5));
		BlockPos center = getPos().offset(facing, 5);
		EntityPlayerSP player = Minecraft.getMinecraft().player;
		float attenuation = Math.max((float) player.getDistanceSq(center.getX(), center.getY(), center.getZ()) / 8, 1);
		float level = (float) speed / maxSpeed;
		runningSound.updatePitch(level);
		runningSound.updateVolume((10 * level) / attenuation);
		if (level > 0) runningSound.playSound();
		else runningSound.stopSound();
	}

	@Override
	public void update() {
		super.update();
		if(isDummy()) return;
		float rotationSpeed = speed == 0 ? 0f : ((float) speed / (float) energyMaxSpeed) * maxRotationSpeed;
		if(ITUtils.setRotationAngle(animation, rotationSpeed) && !world.isRemote) {
			this.markDirty();
			this.markContainingBlockForUpdate(null);
		}

		if(world.isRemote) {
			handleSounds();
			return;
		}

		if(burnRemaining > 0) {
			burnRemaining--;
			speedUp();
		} else if (!isRSDisabled() && tanks[0].getFluid() != null && tanks[0].getFluid().getFluid() != null && ITUtils.checkMechanicalEnergyReceiver(world, getPos()) && ITUtils.checkAlternatorStatus(world, getPos())) {
			SteamTurbineRecipe recipe = (lastRecipe != null && tanks[0].getFluid().isFluidEqual(lastRecipe.fluidInput)) ? lastRecipe : SteamTurbineRecipe.findFuel(tanks[0].getFluid());
			if(recipe != null && recipe.fluidInput.amount <= tanks[0].getFluidAmount()) {
				lastRecipe = recipe;
				burnRemaining = recipe.getTotalProcessTime();
				tanks[0].drain(recipe.fluidInput.amount, true);
				if(recipe.fluidOutput != null) tanks[1].fill(recipe.fluidOutput, true);
				speedUp();
			} else speedDown();
		} else speedDown();
		pumpOutputOut();
	}

	@Override
	public boolean isMechanicalEnergyTransmitter() {
		return true;
	}

	@Override
	public boolean isMechanicalEnergyReceiver() {
		return false;
	}

	@Override
	public EnumFacing getMechanicalEnergyOutputFacing() {
		return facing;
	}

	@Override
	public EnumFacing getMechanicalEnergyInputFacing() {
		return null;
	}

	@Override
	public int inputToCenterDistance() {
		return -1;
	}

	@Override
	public int outputToCenterDistance() {
		return 9;
	}

	@Override
	public int getEnergy() {
		return speed;
	}

	public MechanicalEnergyAnimation getAnimation() {
		return animation;
	}

	@Override
	public NonNullList <ItemStack> getInventory() {
		return null;
	}

	@Override
	public boolean isStackValid(int slot, ItemStack stack) {
		return false;
	}

	@Override
	public int getSlotLimit(int slot) {
		return 0;
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
	protected SteamTurbineRecipe readRecipeFromNBT(NBTTagCompound tag) {
		return SteamTurbineRecipe.loadFromNBT(tag);
	}

	@Override
	public SteamTurbineRecipe findRecipeForInsertion(ItemStack inserting) {
		return null;
	}

	@Override
	public int[] getEnergyPos() {
		return new int[0];
	}

	@Override
	public int[] getRedstonePos() {
		return new int[] { 32 };
	}

	@Override
	public int[] getOutputSlots() {
		return new int[0];
	}

	@Override
	public int[] getOutputTanks() {
		return new int[] {1};
	}

	@Override
	public boolean additionalCanProcessCheck(MultiblockProcess <SteamTurbineRecipe> process) {
		return false;
	}

	@Override
	public void doProcessOutput(ItemStack output) {
	}

	@Override
	public void doProcessFluidOutput(FluidStack output) {
	}

	@Override
	public void onProcessFinish(MultiblockProcess <SteamTurbineRecipe> process) {
	}

	@Override
	public int getMaxProcessPerTick() {
		return 0;
	}

	@Override
	public int getProcessQueueMaxLength() {
		return 0;
	}

	@Override
	public float getMinProcessDistance(MultiblockProcess <SteamTurbineRecipe> process) {
		return 0;
	}
	
	@Override
	public boolean isInWorldProcessingMachine() {
		return false;
	}

	@Override
	protected IFluidTank[] getAccessibleFluidTanks(EnumFacing side) {
		TileEntitySteamTurbine master = master();
		if(master != null) {
			if(pos == 30 && (side == null || side == facing.getOpposite())) return new FluidTank[] {master.tanks[0]};
			else if(pos == 112 && (side == null || side == facing)) return new FluidTank[] {master.tanks[1]};
		}
		return ITUtils.emptyIFluidTankList;
	}

	@Override
	protected boolean canFillTankFrom(int iTank, EnumFacing side, FluidStack resources) {
		TileEntitySteamTurbine master = this.master();
		if(master == null) return false;
		if((pos == 30) && (side == null || side == facing.getOpposite())) {
			FluidStack resourceClone = Utils.copyFluidStackWithAmount(resources, 1000, false);
			FluidStack resourceClone2 = Utils.copyFluidStackWithAmount(master.tanks[iTank].getFluid(), 1000, false);
			if(master.tanks[iTank].getFluidAmount() >= master.tanks[iTank].getCapacity()) return false;
			if(master.tanks[iTank].getFluid() == null) {
				SteamTurbineRecipe incompleteRecipes = SteamTurbineRecipe.findFuel(resourceClone);
				return incompleteRecipes != null;
			} else {
				SteamTurbineRecipe incompleteRecipes1 = SteamTurbineRecipe.findFuel(resourceClone);
				SteamTurbineRecipe incompleteRecipes2 = SteamTurbineRecipe.findFuel(resourceClone2);
				return incompleteRecipes1 == incompleteRecipes2;
			}
		}
		return false;
	}

	@Override
	protected boolean canDrainTankFrom(int iTank, EnumFacing side) {
		return (pos == 112 && (side == null || side == facing));
	}
	
	@Override
	public float[] getBlockBounds() {
		return null;
	}

	@Override
	public List <AxisAlignedBB> getAdvancedColisionBounds() {
		return getAdvancedSelectionBounds();
	}

	@Override
	public List <AxisAlignedBB> getAdvancedSelectionBounds() {
		double[] boundingArray = new double[6];
		EnumFacing fl = facing;
		EnumFacing fw = facing.rotateY();
		if(mirrored) fw = fw.getOpposite();
		if(pos <= 2) {
			boundingArray = ITUtils.smartBoundingBox(0, 0, 0, 0, 0, .5f, fl, fw);
			List <AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			if(pos == 1) {
				boundingArray = ITUtils.smartBoundingBox(.25f, .125f, .125f, .125f, .625f, 1, fl, fw);
				list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
				boundingArray = ITUtils.smartBoundingBox(0, .75f, 0, 0, .5f, 1, fl, fw);
				list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			}
			if(pos == 2) {
				boundingArray = ITUtils.smartBoundingBox(.125f, .75f, .625f, .125f, .5f, 1, fl, fw);
				list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
				boundingArray = ITUtils.smartBoundingBox(.75f, .125f, .625f, .125f, .5f, 1, fl, fw);
				list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			}
			return list;
		}
		if(pos == 3 || pos == 5) {
			if(pos == 5) fw = fw.getOpposite();
			boundingArray = ITUtils.smartBoundingBox(0, 0, .5f, 0, 0, 1, fl, fw);
			return Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		}
		if(pos == 6 || pos == 8 || pos == 18 || pos == 20 || pos == 21 || pos == 23) {
			if(pos == 8 || pos == 20 || pos == 23) fw = fw.getOpposite();
			if(pos == 18 || pos == 20) fl = fl.getOpposite();
			boundingArray = ITUtils.smartBoundingBox(0, 0, .5f, 0, 0, 1, fl, fw);
			List <AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(0, .5f, 0, .5f, 0, 1, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;
		}
		if(pos == 30) {
			boundingArray = ITUtils.smartBoundingBox(.875f, 0, .125f, .125f, .125f, .875f, fl, fw);
			List <AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(0, .125f, .25f, .25f, .25f, .75f, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(.375f, .25f, .3125f, .3125f, .75f, .875f, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(.4375f, .3125f, .375f, .375f, .875f, 1, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;
		}
		if(pos == 31) {
			boundingArray = ITUtils.smartBoundingBox(.25f, .125f, .125f, .125f, 0, .375f, fl, fw);
			List <AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(0, .75f, 0, 0, 0, .5f, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(.375f, .25f, .3125f, .3125f, .375f, .5f, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(.4375f, .3125f, .375f, .375f, .5f, 1, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;
		}
		if(pos == 32) {
			boundingArray = ITUtils.smartBoundingBox(0, 0, .5f, 0, 0, 1, fl, fw);
			return Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		}
		if(pos == 33) {
			boundingArray = ITUtils.smartBoundingBox(0, 0, .25f, .25f, .25f, .75f, fl, fw);
			List <AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(0, .25f, .75f, 0, 0, .75f, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;
		}
		if(pos == 34) {
			boundingArray = ITUtils.smartBoundingBox(0, .25f, 0, 0, 0, 1, fl, fw);
			List <AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(.75f, 0, 0, 0, 0, .5f, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;
		}
		if(pos == 35) {
			fw = fw.getOpposite();
			boundingArray = ITUtils.smartBoundingBox(0, .25f, .75f, 0, 0, .75f, fl, fw);
			return Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		}
		if(pos == 36 || pos == 38 || pos == 48 || pos == 50 || pos == 51 || pos == 53) {
			if(pos == 38 || pos == 50 || pos == 53) fw = fw.getOpposite();
			if(pos == 48 || pos == 50) fl = fl.getOpposite();
			boundingArray = ITUtils.smartBoundingBox(.25f, 0, .75f, 0, 0, .75f, fl, fw);
			List <AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(0, .75f, .25f, 0, 0, 1, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			if(pos == 36) {
				boundingArray = ITUtils.smartBoundingBox(.75f, 0, .25f, .25f, .25f, .75f, fl, fw);
				list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));

			}
			return list;
		}
		if(pos == 37 || pos == 49 || pos == 52) {
			if(pos == 49) fl = fl.getOpposite();
			boundingArray = ITUtils.smartBoundingBox(.25f, 0, 0, 0, .75f, 1, fl, fw);
			return Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		}
		if(pos == 39 || pos == 41 || pos == 45 || pos == 47 || pos == 54 || pos == 56) {
			if(pos == 41 || pos == 47 || pos == 56) fw = fw.getOpposite();
			if(pos == 45 || pos == 47) fl = fl.getOpposite();
			boundingArray = ITUtils.smartBoundingBox(.25f, 0, .25f, 0, 0, 1, fl, fw);
			return Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		}
		if(pos == 60 || pos == 61) {
			if(pos == 61) fw = fw.getOpposite();
			boundingArray = ITUtils.smartBoundingBox(.4375f, .3125f, .375f, .375f, 0, .125f, fl, fw);
			List <AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(.375f, .25f, .3125f, .3125f, .125f, .5f, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(.4375f, .3125f, .6875f, 0, .1875f, .4375f, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;
		}
		if(pos == 66 || pos == 68 || pos == 78 || pos == 80 || pos == 81 || pos == 83) {
			if(pos == 68 || pos == 80 || pos == 83) fw = fw.getOpposite();
			if(pos == 78 || pos == 80) fl = fl.getOpposite();
			boundingArray = ITUtils.smartBoundingBox(0, .75f, .5f, 0, 0, .4375f, fl, fw);
			return Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		}
		if(pos == 67) {
			boundingArray = ITUtils.smartBoundingBox(0, .75f, 0, 0, 0, .5f, fl, fw);
			return Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		}
		if(pos == 69 || pos == 71 || pos == 75 || pos == 77 || pos == 84 || pos == 86) {
			if(pos == 71 || pos == 77 || pos == 86) fw = fw.getOpposite();
			if(pos == 75 || pos == 77) fl = fl.getOpposite();
			boundingArray = ITUtils.smartBoundingBox(.25f, 0, .5f, 0, 0, .4375f, fl, fw);
			return Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		}
		if(pos == 79 || pos == 82) {
			if(pos == 82) fl = fl.getOpposite();
			boundingArray = ITUtils.smartBoundingBox(0, .75f, .25f, .25f, 0, 1, fl, fw);
			List <AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(.75f, 0, 0, 0, 0, .5f, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;
		}
		if(pos == 70 || pos == 76 || pos == 85) {
			if(pos == 76) fl = fl.getOpposite();
			boundingArray = ITUtils.smartBoundingBox(.25f, 0, 0, 0, 0, .5f, fl, fw);
			List <AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			if(pos == 70) {
				boundingArray = ITUtils.smartBoundingBox(.5f, 0, .25f, .25f, .5f, 1, fl, fw);
				list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			}
			return list;
		}
		if(pos == 100) {
			boundingArray = ITUtils.smartBoundingBox(.5f, 0, .25f, .25f, 0, .75f, fl, fw);
			List <AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(0, .5f, .125f, .125f, .125f, .875f, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;
		}
		if(pos == 103 || pos == 106 || pos == 109) {
			boundingArray = ITUtils.smartBoundingBox(0, 0, .125f, .125f, .125f, .875f, fl, fw);
			List <AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			if(pos == 109) {
				boundingArray = ITUtils.smartBoundingBox(0, .75f, .25f, .25f, 0, .125f, fl, fw);
				list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			}
			return list;
		}
		if(pos == 112) {
			boundingArray = ITUtils.smartBoundingBox(0, 0, .125f, .125f, .125f, .875f, fl, fw);
			List <AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(.75f, 0, .25f, .25f, 0, .125f, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;
		}
		return null;
	}

	@Override
	public boolean isOverrideBox(AxisAlignedBB box, EntityPlayer player, RayTraceResult mop, ArrayList <AxisAlignedBB> list) {
		return false;
	}
}
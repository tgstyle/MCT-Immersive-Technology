package mctmods.immersivetechnology.common.blocks.metal.tileentities;

import blusunrize.immersiveengineering.common.util.Utils;
import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.api.ITUtils;
import mctmods.immersivetechnology.api.crafting.RadiatorRecipe;
import mctmods.immersivetechnology.common.Config.ITConfig.Machines.Radiator;
import mctmods.immersivetechnology.common.util.ITFluidTank;
import mctmods.immersivetechnology.common.util.ITSounds;
import mctmods.immersivetechnology.common.util.compat.ITCompatModule;
import mctmods.immersivetechnology.common.util.compat.advancedrocketry.AdvancedRocketryHelper;
import mctmods.immersivetechnology.common.util.network.MessageStopSound;
import mctmods.immersivetechnology.common.util.sound.ITSoundHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityRadiatorMaster extends TileEntityRadiatorSlave implements ITFluidTank.TankListener {

	private static int inputTankSize = Radiator.radiator_input_tankSize;
	private static int outputTankSize = Radiator.radiator_output_tankSize;
	private static float speedMult = Radiator.radiator_speed_multiplier;

	BlockPos fluidOutputPos;

	public FluidTank[] tanks = new FluidTank[] {
			new ITFluidTank(inputTankSize, this),
			new ITFluidTank(outputTankSize, this)
	};

	public int recipeTimeRemaining = 0;
	private int clientUpdateCooldown = 20;
	private double radiationEfficiency = 0;

	private RadiatorRecipe lastRecipe;

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket) {
		super.readCustomNBT(nbt, descPacket);
		tanks[0].readFromNBT(nbt.getCompoundTag("tank0"));
		tanks[1].readFromNBT(nbt.getCompoundTag("tank1"));
		recipeTimeRemaining = nbt.getInteger("recipeTimeRemaining");
		radiationEfficiency = nbt.getDouble("radiationEfficiency");
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket) {
		super.writeCustomNBT(nbt, descPacket);
		nbt.setTag("tank0", tanks[0].writeToNBT(new NBTTagCompound()));
		nbt.setTag("tank1", tanks[1].writeToNBT(new NBTTagCompound()));
		nbt.setInteger("recipeTimeRemaining", recipeTimeRemaining);
		nbt.setDouble("radiationEfficiency", radiationEfficiency);
	}

	private boolean gainProgress() {
		if(lastRecipe == null) {
			recipeTimeRemaining = 0;
			return true;
		}
		recipeTimeRemaining--;
		if(recipeTimeRemaining == 0) {
			int[] fluidAmounts = getProcessedFluidAmounts(lastRecipe);
			tanks[0].drain(fluidAmounts[0], true);
			tanks[1].fillInternal(new FluidStack(lastRecipe.fluidOutput.getFluid(), fluidAmounts[1]), true);
			markContainingBlockForUpdate(null);
			return true;
		}
		return false;
	}

	private void checkReflectorEfficiency() {
		if (this.mirrored) {
			radiationEfficiency = checkLineEfficiency(-2) + checkLineEfficiency(2);
		} else {
            radiationEfficiency = checkRowEfficiency(-2) + checkRowEfficiency( 2);
		}
	}

	private double checkRowEfficiency(int offsetY) {
		double halfEfficiency = 0;
		//Grab and return the columnar efficiencies for each of the six checks on this level, divide by twelve to return the half of the efficiency here
		BlockPos pos2 = this.getPos().offset(this.facing, 1).add(0, offsetY, 0);
		halfEfficiency += checkColumnEfficiency(pos2, this.facing.rotateY())/12.0;
		halfEfficiency += checkColumnEfficiency(pos2, this.facing.rotateYCCW())/12.0;
		pos2 = this.getPos().offset(this.facing, 3);
		halfEfficiency += checkColumnEfficiency(pos2, this.facing.rotateY())/12.0;
		halfEfficiency += checkColumnEfficiency(pos2, this.facing.rotateYCCW())/12.0;
		pos2 = this.getPos().offset(this.facing, 3);
		halfEfficiency += checkColumnEfficiency(pos2, this.facing.rotateY())/12.0;
		halfEfficiency += checkColumnEfficiency(pos2, this.facing.rotateYCCW())/12.0;
		//When we're done, return that
		return halfEfficiency;
	}

	private double checkLineEfficiency(int offsetX) {
		double halfEfficiency = 0;
		//Grab and return the columnar efficiencies for each of the six checks on this level, divide by twelve to return the half of the efficiency here
		BlockPos pos2 = this.getPos().offset(this.facing, 1).offset(this.facing.rotateY(), offsetX);
		halfEfficiency += checkColumnEfficiency(pos2, EnumFacing.DOWN)/12.0;
		halfEfficiency += checkColumnEfficiency(pos2, EnumFacing.UP)/12.0;
		pos2 = this.getPos().offset(this.facing, 3);
		halfEfficiency += checkColumnEfficiency(pos2, EnumFacing.DOWN)/12.0;
		halfEfficiency += checkColumnEfficiency(pos2, EnumFacing.UP)/12.0;
		pos2 = this.getPos().offset(this.facing, 3);
		halfEfficiency += checkColumnEfficiency(pos2, EnumFacing.DOWN)/12.0;
		halfEfficiency += checkColumnEfficiency(pos2, EnumFacing.UP)/12.0;
		//When we're done, return that
		return halfEfficiency;
	}

	private double checkColumnEfficiency(BlockPos pos, EnumFacing facing) {
		double j = 1;
		for (int i = 1; i < 49; i++) {
			if (!(world.getBlockState(pos.offset(facing, i)).getBlock() == Blocks.AIR)) {
				//Square the inverse distance, one block away is 48^2 uselessness, etc
				j = 1.0/((49 - i) * (49 - i));
				break;
			}
		}
		return j;
	}

	private double getTotalRadiationEfficiency(int inputFluidTemperature) {
		if (world.provider.isNether()) return 0;
		return (ITCompatModule.isAdvancedRocketryLoaded ? AdvancedRocketryHelper.getRadiatorHeatTransferCoefficient(this.world, this.getPos(), inputFluidTemperature, radiationEfficiency) : radiationEfficiency);
	}

	private void pumpOutputOut() {
		if(tanks[1].getFluidAmount() == 0) return;
		if(fluidOutputPos == null) fluidOutputPos = ITUtils.LocalOffsetToWorldBlockPos(this.getPos(), 0, 0, 9, facing);
		IFluidHandler output = FluidUtil.getFluidHandler(world, fluidOutputPos, facing.getOpposite());
		if(output == null) return;
		FluidStack out = tanks[1].getFluid();
		int accepted = output.fill(out, false);
		if(accepted == 0) return;
		int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
		this.tanks[1].drain(drained, true);
	}

	public int[] getProcessedFluidAmounts(RadiatorRecipe recipe) {
		int inputToOutputRatio = recipe.fluidInput.amount/recipe.fluidOutput.amount;
		int outputFluidAmount = (int)(getTotalRadiationEfficiency(recipe.fluidInput.getFluid().getTemperature()) * recipe.fluidOutput.amount);
		int inputFluidAmount = inputToOutputRatio * outputFluidAmount;
		return new int[]{inputFluidAmount, outputFluidAmount};
	}

	public void handleSounds() {
		BlockPos center = this.getPos();
		float level = tickedProcesses;
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
		if(recipeTimeRemaining > 0) {
				if(gainProgress()) update = true;
		} else if(tanks[0].getFluid() != null) {
			RadiatorRecipe recipe = (lastRecipe != null && tanks[0].getFluid().isFluidEqual(lastRecipe.fluidInput)) ? lastRecipe : RadiatorRecipe.findRecipe(tanks[0].getFluid());
			if (recipe != null && recipe.fluidInput.amount <= tanks[0].getFluidAmount() && recipe.fluidOutput.amount == tanks[1].fillInternal(recipe.fluidOutput, false)) {
				lastRecipe = recipe;
				recipeTimeRemaining = (int) (recipe.getTotalProcessTime() / (speedMult));
				gainProgress();
				update = true;
			}
		}
		return update;
	}

	private boolean outputTankLogic() {
		boolean update = false;
		if(this.tanks[1].getFluidAmount() > 0) {
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

		//Rarely check reflector efficiency so as not to kill the server
		if (world.getTotalWorldTime() % 600 == 0) checkReflectorEfficiency();

		boolean update = recipeLogic();
		//Recipes
		//Tank outputs & inputs
		if(outputTankLogic()) update = true;
		if(clientUpdateCooldown > 1) clientUpdateCooldown--;
		if(update) {
			efficientMarkDirty();
			if(clientUpdateCooldown == 1) {
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
	public TileEntityRadiatorMaster master() {
		master = this;
		return this;
	}

}
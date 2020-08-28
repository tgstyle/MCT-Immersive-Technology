package mctmods.immersivetechnology.common.blocks.metal.tileentities;

import blusunrize.immersiveengineering.common.util.Utils;
import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.api.ITUtils;
import mctmods.immersivetechnology.api.client.MechanicalEnergyAnimation;
import mctmods.immersivetechnology.api.crafting.SteamTurbineRecipe;
import mctmods.immersivetechnology.common.Config.ITConfig.Machines.SteamTurbine;
import mctmods.immersivetechnology.common.Config.ITConfig.MechanicalEnergy;
import mctmods.immersivetechnology.common.blocks.ITBlockInterfaces.IMechanicalEnergy;
import mctmods.immersivetechnology.common.util.ITFluidTank;
import mctmods.immersivetechnology.common.util.ITSounds;
import mctmods.immersivetechnology.common.util.network.MessageStopSound;
import mctmods.immersivetechnology.common.util.sound.ITSoundHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntitySteamTurbineMaster extends TileEntitySteamTurbineSlave implements ITFluidTank.TankListener {

	private static int inputTankSize = SteamTurbine.steamTurbine_input_tankSize;
	private static int outputTankSize = SteamTurbine.steamTurbine_input_tankSize;
	private static int maxSpeed = MechanicalEnergy.mechanicalEnergy_speed_max;
	private static int speedGainPerTick = SteamTurbine.steamTurbine_speed_gainPerTick;
	private static int speedLossPerTick = SteamTurbine.steamTurbine_speed_lossPerTick;
	private static float maxRotationSpeed = SteamTurbine.steamTurbine_speed_maxRotation;
	BlockPos fluidOutputPos;

	public FluidTank[] tanks = new FluidTank[] {
		new ITFluidTank(inputTankSize, this),
		new ITFluidTank(outputTankSize, this)
	};

	public int burnRemaining = 0;
	public int speed;

	public SteamTurbineRecipe lastRecipe;

	MechanicalEnergyAnimation animation = new MechanicalEnergyAnimation();

	IMechanicalEnergy alternator;

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
		if(fluidOutputPos == null) fluidOutputPos = ITUtils.LocalOffsetToWorldBlockPos(this.getPos(), 0, 2, 8, facing, mirrored);
		IFluidHandler output = FluidUtil.getFluidHandler(world, fluidOutputPos, facing.getOpposite());
		if(output == null) return;
		FluidStack out = tanks[1].getFluid();
		int accepted = output.fill(out, false);
		if(accepted == 0) return;
		int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
		this.tanks[1].drain(drained, true);
	}

	public void handleSounds() {
		float level = ITUtils.remapRange(0, maxSpeed, 0.5f, 1.0f, speed);
		BlockPos center = getPos().offset(facing, 5);
		if(speed == 0) ITSoundHandler.StopSound(center);
		else {
			EntityPlayerSP player = Minecraft.getMinecraft().player;
			float attenuation = Math.max((float) player.getDistanceSq(center.getX(), center.getY(), center.getZ()) / 8, 1);
			ITSounds.turbine.PlayRepeating(center, (11 * (level - 0.5f)) / attenuation, level);
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void onChunkUnload() {
		ITSoundHandler.StopSound(getPos().offset(facing, 5));
		super.onChunkUnload();
	}

	@Override
	public void disassemble() {
		super.disassemble();
		BlockPos center = getPos().offset(facing, 5);
		ImmersiveTechnology.packetHandler.sendToAllTracking(new MessageStopSound(center), new NetworkRegistry.TargetPoint(world.provider.getDimension(), center.getX(), center.getY(), center.getZ(), 0));
	}

	public void efficientMarkDirty() { // !!!!!!! only use it within update() function !!!!!!!
		world.getChunkFromBlockCoords(this.getPos()).markDirty();
	}

	public boolean isValidAlternator() {
		if (alternator == null || !alternator.isValid()) {
			TileEntity tile = world.getTileEntity(getPos().offset(facing, 10));
			if (tile instanceof IMechanicalEnergy) {
				IMechanicalEnergy possibleAlternator = (IMechanicalEnergy) tile;
				if (possibleAlternator.isValid() && possibleAlternator.isMechanicalEnergyReceiver(facing.getOpposite())) {
					alternator = possibleAlternator;
				}
			}
		}
		return alternator != null && alternator.isValid();
	}

	@Override
	public void update() {
		if(!formed) return;
		if(world.isRemote) {
			handleSounds();
			return;
		}
		float rotationSpeed = speed == 0 ? 0f : ((float) speed / (float) maxSpeed) * maxRotationSpeed;
		if(ITUtils.setRotationAngle(animation, rotationSpeed)) {
			efficientMarkDirty();
			this.markContainingBlockForUpdate(null);
		}
		if(burnRemaining > 0) {
			burnRemaining--;
			speedUp();
		} else if(!isRSDisabled() && tanks[0].getFluid() != null && tanks[0].getFluid().getFluid() != null && isValidAlternator()) {
			SteamTurbineRecipe recipe = (lastRecipe != null && tanks[0].getFluid().isFluidEqual(lastRecipe.fluidInput)) ? lastRecipe : SteamTurbineRecipe.findFuel(tanks[0].getFluid());
			if(recipe != null && recipe.fluidInput.amount <= tanks[0].getFluidAmount()) {
				lastRecipe = recipe;
				burnRemaining = recipe.getTotalProcessTime() - 1;
				tanks[0].drain(recipe.fluidInput.amount, true);
				if(recipe.fluidOutput != null) tanks[1].fill(recipe.fluidOutput, true);
				this.markContainingBlockForUpdate(null);
				speedUp();
			} else speedDown();
		} else speedDown();
		pumpOutputOut();
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
	public TileEntitySteamTurbineMaster master() {
		master = this;
		return this;
	}

	public boolean isMechanicalEnergyTransmitter(EnumFacing facing, int position) {
		return facing == this.facing && position == 58;
	}
}
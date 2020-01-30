package ferro2000.immersivetech.common.blocks.metal.tileentities;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxProvider;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedCollisionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedSelectionBounds;
import blusunrize.immersiveengineering.common.blocks.TileEntityMultiblockPart;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.*;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.api.IEEnums.SideConfig;

import ferro2000.immersivetech.ImmersiveTech;
import ferro2000.immersivetech.api.ITUtils;
import ferro2000.immersivetech.api.client.MechanicalEnergyAnimation;
import ferro2000.immersivetech.common.Config.ITConfig;
import ferro2000.immersivetech.common.blocks.ITBlockInterface.IMechanicalEnergy;
import ferro2000.immersivetech.common.blocks.metal.multiblocks.MultiblockAlternator;
import ferro2000.immersivetech.common.util.ITSoundHandler;
import ferro2000.immersivetech.common.util.ITSounds;
import ferro2000.immersivetech.common.util.network.MessageStopSound;
import ferro2000.immersivetech.common.util.network.MessageTileSync;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.common.network.NetworkRegistry;

public class TileEntityAlternator extends TileEntityMultiblockPart <TileEntityAlternator> implements IMechanicalEnergy, IAdvancedSelectionBounds, IAdvancedCollisionBounds, IFluxProvider, IIEInternalFluxHandler {
	private static int[] size = new int[] {3, 4, 3};	

	FluxStorage energyStorage = new FluxStorage(ITConfig.Machines.alternator_energyStorage,rfPerTick,rfPerTickPerPort);

	private BlockPos[] EnergyOutputPositions = new BlockPos[6];

	private static int maxSpeed = ITConfig.Machines.mechanicalEnergy_maxSpeed;
	private static int rfPerTick = ITConfig.Machines.alternator_RfPerTick;
	private static int rfPerTickPerPort = rfPerTick / 6;
	
	public int speed;

	private float clientEnergyPercentage;
	private int oldEnergy = energyStorage.getEnergyStored();
	
	MechanicalEnergyAnimation animation = new MechanicalEnergyAnimation();

	public TileEntityAlternator() {
		super(size);
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket) {
		super.readCustomNBT(nbt, descPacket);
		energyStorage.readFromNBT(nbt);
		clientEnergyPercentage = (float) energyStorage.getEnergyStored() / energyStorage.getMaxEnergyStored();
		speed = nbt.getInteger("speed");
		animation.readFromNBT(nbt);
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket) {
		super.writeCustomNBT(nbt, descPacket);
		energyStorage.writeToNBT(nbt);
		nbt.setInteger("speed", speed);
		animation.writeToNBT(nbt);
	}

	public int energyGenerated() {
		return Math.round(((float)speed / maxSpeed) * rfPerTick);
	}

	public void handleSounds() {
		BlockPos center = getPos();
		if(clientEnergyPercentage == 0) ITSoundHandler.StopSound(center);
		else {
			EntityPlayerSP player = Minecraft.getMinecraft().player;
			float attenuation = Math.max((float) player.getDistanceSq(center.getX(), center.getY(), center.getZ()) / 8, 1);
			ITSoundHandler.PlaySound(center, ITSounds.alternator, SoundCategory.BLOCKS, true, (2 * clientEnergyPercentage) / attenuation, clientEnergyPercentage);
		}
	}

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
		tag.setInteger("energy", energyStorage.getEnergyStored());
		BlockPos center = getPos();
		ImmersiveTech.packetHandler.sendToAllTracking(new MessageTileSync(this, tag), new NetworkRegistry.TargetPoint(world.provider.getDimension(), center.getX(), center.getY(), center.getZ(), 0));
	}

	@Override
	public void update() {
		if(formed && pos == 13) {
			if(!world.isRemote) {
				if(ITUtils.checkMechanicalEnergyTransmitter(world, getPos())) speed = ITUtils.getMechanicalEnergy(world, getPos());
				if(speed > 0) this.energyStorage.modifyEnergyStored(energyGenerated());
				TileEntity tileEntity;
				int currentEnergy = energyStorage.getEnergyStored();
				for(int i = 0;i < 6;i++) {
					if(currentEnergy == 0) break;
					if(EnergyOutputPositions[i] == null) EnergyOutputPositions[i] = ITUtils.LocalOffsetToWorldBlockPos(getPos(), i < 3 ? -2 : 2, i < 3 ? i - 1 : i - 4, 0, facing);
					tileEntity = Utils.getExistingTileEntity(world, EnergyOutputPositions[i]);
					EnumFacing energyFacing = i < 3 ? facing.rotateY() : facing.rotateYCCW();
					if(!EnergyHelper.isFluxReceiver(tileEntity, energyFacing)) continue;
					int canReceiveAmount = EnergyHelper.insertFlux(tileEntity, energyFacing, Math.min(currentEnergy, rfPerTickPerPort), true);
					if(canReceiveAmount == 0) continue;
					EnergyHelper.insertFlux(tileEntity, energyFacing, canReceiveAmount, false);
					energyStorage.modifyEnergyStored(-canReceiveAmount);
					currentEnergy = energyStorage.getEnergyStored();
				}
				if(oldEnergy != currentEnergy) {
					this.markDirty();
					this.markContainingBlockForUpdate(null);
					notifyNearbyClients();
				}
				oldEnergy = currentEnergy;
			} else handleSounds();
		}
	}

	@Override
	public void receiveMessageFromServer(NBTTagCompound message) {
		if(message.hasKey("energy")) {
			clientEnergyPercentage = (float) message.getInteger("energy") / energyStorage.getMaxEnergyStored();
		}
	}

	@Override
	public boolean isDummy() {
		return pos != 13;
	}
	
	IEForgeEnergyWrapper wrapper = new IEForgeEnergyWrapper(this, null);

	private boolean isEnergyPos(@Nullable EnumFacing enumFacing) {
		if(!this.formed || enumFacing == null || enumFacing == EnumFacing.DOWN || enumFacing == EnumFacing.UP) return false;
		TileEntityAlternator master = this.master();
		if(master == null) return false;
		return (enumFacing.rotateY() == master().facing) && (pos == 0 || pos == 12 || pos == 24) ||
				(enumFacing.rotateYCCW() == master().facing) && (pos == 2 || pos == 14 || pos == 26);
	}

	@Nonnull
	@Override
	public SideConfig getEnergySideConfig(@Nullable EnumFacing enumFacing) {
		return this.formed && this.isEnergyPos(enumFacing)? SideConfig.OUTPUT: SideConfig.NONE;
	}

	@Override
	public IEForgeEnergyWrapper getCapabilityWrapper(EnumFacing facing) {
		if(this.formed && this.isEnergyPos(facing)) return wrapper;
		return null;
	}

	@Nonnull
	@Override
	public FluxStorage getFluxStorage() {
		TileEntityAlternator master = this.master();
		if(master != null) return master.energyStorage;
		return energyStorage;
	}

	@Override
	public int receiveEnergy(@Nullable EnumFacing fd, int amount, boolean simulate) {
		return 0;
	}

	@Override
	public boolean canConnectEnergy(@Nullable EnumFacing from) {
		return isEnergyPos(from);
	}

	@Override
	public int extractEnergy(@Nullable EnumFacing from, int energy, boolean simulate) {
		if(!isEnergyPos(from)) return 0;
		TileEntityAlternator master = master();
		return master == null ? 0:master.energyStorage.extractEnergy(energy, simulate);
	}

	@Override
	public int getEnergyStored(@Nullable EnumFacing from) {
		TileEntityAlternator master = master();
		return master == null ? 0:master.energyStorage.getEnergyStored();
	}

	@Override
	public int getMaxEnergyStored(@Nullable EnumFacing from) {
		TileEntityAlternator master = master();
		return master == null ? 0:master.energyStorage.getMaxEnergyStored();
	}

	public boolean canRunMechanicalEnergy() {
		return true;
	}

	@Override
	public boolean isMechanicalEnergyTransmitter() {
		return false;
	}

	@Override
	public boolean isMechanicalEnergyReceiver() {
		return true;
	}

	@Override
	public EnumFacing getMechanicalEnergyOutputFacing() {
		return null;
	}

	@Override
	public EnumFacing getMechanicalEnergyInputFacing() {
		return facing;
	}

	@Override
	public int inputToCenterDistance() {
		return 3;
	}

	@Override
	public int outputToCenterDistance() {
		return -1;
	}

	@Override
	public int getEnergy() {
		return speed;
	}

	public MechanicalEnergyAnimation getAnimation() {
		return animation;
	}

	@Override
	protected IFluidTank[] getAccessibleFluidTanks(EnumFacing side) {
		return new IFluidTank[0];
	}

	@Override
	protected boolean canFillTankFrom(int iTank, EnumFacing side, FluidStack resource) {
		return false;
	}

	@Override
	protected boolean canDrainTankFrom(int iTank, EnumFacing side) {
		return false;
	}

	@Override
	public ItemStack getOriginalBlock() {
		if(pos < 0) return ItemStack.EMPTY;
		ItemStack s = ItemStack.EMPTY;
		try {
			s = MultiblockAlternator.instance.getStructureManual()[pos/12][pos%12/3][pos%3];
		} catch(Exception e) {
			e.printStackTrace();
		}
		return s.copy();
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
		if(pos == 0 || pos == 2 || pos == 12 || pos == 14 || pos == 24 || pos == 26) {
			if(pos == 2 || pos == 14 || pos == 26) fw = fw.getOpposite();
			boundingArray = ITUtils.smartBoundingBox(.25f, .25f, 0, .875f, .25f, .75f, fl, fw);
			List <AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(.25f, .25f, .625f, .25f, .25f, .75f, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(.375f, .375f, .75f, 0, .375f, .625f, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			if(pos <= 2) {
				boundingArray = ITUtils.smartBoundingBox(0, 0, .125f, .375f, 0, 1, fl, fw);
				list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			} else {
				boundingArray = ITUtils.smartBoundingBox(0, 0, .125f, .375f, 0, 1, fl, fw);
				list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			}
			return list;
		}
		if(pos == 1 || pos == 25) {
			boundingArray = ITUtils.smartBoundingBox(.25f, .25f, .25f, .25f, .25f, .75f, fl, fw);
			List <AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(.375f, .375f, 0, .75f, .375f, .625f, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(.375f, .375f, .75f, 0, .375f, .625f, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			if(pos == 1) {
				boundingArray = ITUtils.smartBoundingBox(.375f, .375f, .375f, .375f, .75f, 1, fl, fw);
				list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			} else {
				boundingArray = ITUtils.smartBoundingBox(.375f, .375f, .375f, .375f, 0, .25f, fl, fw);
				list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			}
			return list;
		}
		if(pos == 3 || pos == 5) {
			if(pos == 5) fw = fw.getOpposite();
			boundingArray = ITUtils.smartBoundingBox(0, .5f, .5f, 0, 0, 1, fl, fw);
			return Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		}
		if(pos == 9 || pos == 11) {
			if(pos == 11) fw = fw.getOpposite();
			boundingArray = ITUtils.smartBoundingBox(.5f, 0, 0, 0, 0, 1, fl, fw);
			List <AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(0, .5f, .5f, 0, 0, 1, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;
		}
		if(pos == 13) {
			boundingArray = ITUtils.smartBoundingBox(.25f, .25f, .125f, .125f, .125f, .875f, fl, fw);
			List <AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(.375f, .375f, 0, .875f, .375f, .625f, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(.375f, .375f, .875f, 0, .375f, .625f, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(.375f, .375f, .375f, .375f, .875f, 1, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(.375f, .375f, .375f, .375f, 0, .125f, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(0, .75f, .25f, .25f, .25f, .75f, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;
		}
		if(pos == 15 || pos == 17) {
			if(pos == 17) fw = fw.getOpposite();
			boundingArray = ITUtils.smartBoundingBox(0, .75f, .75f, 0, 0, .75f, fl, fw);
			return Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		}
		if(pos == 16) {
			boundingArray = ITUtils.smartBoundingBox(.5f, 0, 0, 0, 0, 1, fl, fw);
			List <AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(.25f, .5f, .25f, .25f, .25f, .75f, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(0, .75f, 0, 0, 0, 1, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;
		}
		if(pos == 18 || pos == 20) {
			if(pos == 20) fw = fw.getOpposite();
			boundingArray = ITUtils.smartBoundingBox(0, .25f, .25f, 0, 0, 1, fl, fw);
			List <AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(.75f, 0, .75f, 0, 0, .75f, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;
		}
		if(pos == 21 || pos == 23) {
			if(pos == 23) fw = fw.getOpposite();
			boundingArray = ITUtils.smartBoundingBox(.75f, 0, .25f, 0, 0, 1, fl, fw);
			List <AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(.25f, .25f, .75f, 0, 0, 1, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;
		}
		if(pos == 30 || pos == 32) {
			if(pos == 32) fw = fw.getOpposite();
			boundingArray = ITUtils.smartBoundingBox(0, .25f, .5f, 0, 0, .4375f, fl, fw);
			return Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		}
		if(pos == 31) {
			boundingArray = ITUtils.smartBoundingBox(0, .25f, 0, 0, 0, .5f, fl, fw);
			return Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		}
		if(pos == 33 || pos == 35) {
			if(pos == 35) fw = fw.getOpposite();
			boundingArray = ITUtils.smartBoundingBox(.75f, 0, .5f, 0, 0, .4375f, fl, fw);
			List <AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(.25f, .25f, .75f, 0, 0, .125f, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;
		}
		if(pos == 34) {
			boundingArray = ITUtils.smartBoundingBox(.75f, 0, 0, 0, 0, .5f, fl, fw);
			List <AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(.25f, .25f, 0, 0, 0, .125f, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
			return list;
		}
		return null;
	}

	@Override
	public boolean isOverrideBox(AxisAlignedBB box, EntityPlayer player, RayTraceResult mop, 
		ArrayList <AxisAlignedBB> list) {
		return false;
	}

}
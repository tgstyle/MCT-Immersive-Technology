package mctmods.immersivetechnology.common.blocks.metal.tileentities;

import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.api.ITUtils;
import mctmods.immersivetechnology.api.client.MechanicalEnergyAnimation;
import mctmods.immersivetechnology.common.Config.ITConfig.Machines.Alternator;
import mctmods.immersivetechnology.common.Config.ITConfig.MechanicalEnergy;
import mctmods.immersivetechnology.common.util.ITSounds;
import mctmods.immersivetechnology.common.util.network.MessageStopSound;
import mctmods.immersivetechnology.common.util.network.MessageTileSync;
import mctmods.immersivetechnology.common.util.sound.ITSoundHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityAlternatorMaster extends TileEntityAlternatorSlave {

	private static int maxSpeed = MechanicalEnergy.mechanicalEnergy_speed_max;
	private static int rfPerTick = Alternator.alternator_energy_perTick;
	private static int rfPerTickPerPort = rfPerTick / 6;

	public FluxStorage energyStorage = new FluxStorage(Alternator.alternator_energy_capacitorSize,rfPerTick,rfPerTickPerPort);

	private BlockPos[] EnergyOutputPositions = new BlockPos[6];

	public int speed;
	private int clientUpdateCooldown = 20;
	private float clientEnergyPercentage;
	private int oldEnergy = energyStorage.getEnergyStored();

	MechanicalEnergyAnimation animation = new MechanicalEnergyAnimation();

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
		tag.setInteger("energy", energyStorage.getEnergyStored());
		BlockPos center = getPos();
		ImmersiveTechnology.packetHandler.sendToAllTracking(new MessageTileSync(this, tag), new NetworkRegistry.TargetPoint(world.provider.getDimension(), center.getX(), center.getY(), center.getZ(), 0));
	}

	public void efficientMarkDirty() { // !!!!!!! only use it within update() function !!!!!!!
		world.getChunkFromBlockCoords(this.getPos()).markDirty();
	}

	@Override
	public void update() {
		if(!formed) return;
		if(!world.isRemote) {
			speed = ITUtils.getMechanicalEnergy(world, getPos());
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
			if(clientUpdateCooldown > 0) clientUpdateCooldown--;
			if(oldEnergy != currentEnergy) {
				efficientMarkDirty();
				this.markContainingBlockForUpdate(null);
				if(clientUpdateCooldown == 0) {
					notifyNearbyClients();
					clientUpdateCooldown = 20;
				}
			}
			oldEnergy = currentEnergy;
		} else handleSounds();
	}

	@Override
	public void receiveMessageFromServer(NBTTagCompound message) {
		if(message.hasKey("energy")) {
			clientEnergyPercentage = (float) message.getInteger("energy") / energyStorage.getMaxEnergyStored();
		}
	}

	@Override
	public boolean isDummy() {
		return false;
	}

	EnergyHelper.IEForgeEnergyWrapper wrapper = new EnergyHelper.IEForgeEnergyWrapper(this, null);

	@Override
	public TileEntityAlternatorMaster master() {
		master = this;
		return this;
	}

}
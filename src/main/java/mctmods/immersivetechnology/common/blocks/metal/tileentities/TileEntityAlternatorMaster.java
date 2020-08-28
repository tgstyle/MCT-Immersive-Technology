package mctmods.immersivetechnology.common.blocks.metal.tileentities;

import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.api.ITUtils;
import mctmods.immersivetechnology.api.client.MechanicalEnergyAnimation;
import mctmods.immersivetechnology.common.Config.ITConfig.Machines.Alternator;
import mctmods.immersivetechnology.common.Config.ITConfig.Machines.SteamTurbine;
import mctmods.immersivetechnology.common.Config.ITConfig.MechanicalEnergy;
import mctmods.immersivetechnology.common.blocks.ITBlockInterfaces;
import mctmods.immersivetechnology.common.blocks.ITBlockInterfaces.IMechanicalEnergy;
import mctmods.immersivetechnology.common.util.ITSounds;
import mctmods.immersivetechnology.common.util.network.BinaryMessageTileSync;
import mctmods.immersivetechnology.common.util.network.IBinaryMessageReceiver;
import mctmods.immersivetechnology.common.util.network.MessageStopSound;
import mctmods.immersivetechnology.common.util.sound.ITSoundHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityAlternatorMaster extends TileEntityAlternatorSlave implements IBinaryMessageReceiver {

	private static int maxSpeed = MechanicalEnergy.mechanicalEnergy_speed_max;
	private static int rfPerTick = Alternator.alternator_energy_perTick;
	private static int rfPerTickPerPort = rfPerTick / 6;
	private static int speedLossPerTick = SteamTurbine.steamTurbine_speed_lossPerTick;
	private static boolean soundRPM = Alternator.alternator_sound_RPM;

	public FluxStorage energyStorage = new FluxStorage(Alternator.alternator_energy_capacitorSize,rfPerTick,rfPerTickPerPort);

	private BlockPos[] EnergyOutputPositions = new BlockPos[6];

	public int speed;
	public float torqueMult = 1;
	public ITBlockInterfaces.IMechanicalEnergy provider;
	private int clientUpdateCooldown = 20;
	private float clientEnergyPercentage;
	private int oldEnergy = energyStorage.getEnergyStored();
	private int oldSpeed = maxSpeed;

	MechanicalEnergyAnimation animation = new MechanicalEnergyAnimation();

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket) {
		super.readCustomNBT(nbt, descPacket);
		energyStorage.readFromNBT(nbt);
		animation.readFromNBT(nbt);
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket) {
		super.writeCustomNBT(nbt, descPacket);
		energyStorage.writeToNBT(nbt);
		animation.writeToNBT(nbt);
	}

	public int energyGenerated() {
		return Math.round(((float)speed / maxSpeed) * torqueMult * rfPerTick);
	}

	public void handleSounds() {
		BlockPos center = getPos();
		if(clientEnergyPercentage == 0) ITSoundHandler.StopSound(center);
		else {
			EntityPlayerSP player = Minecraft.getMinecraft().player;
			float attenuation = Math.max((float) player.getDistanceSq(center.getX(), center.getY(), center.getZ()) / 8, 1);
			float level = ITUtils.remapRange(0, 1, 0.5f, 1.0f, clientEnergyPercentage);
			ITSounds.alternator.PlayRepeating(center, (5 * clientEnergyPercentage) / attenuation, level);
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
		super.disassemble();
		BlockPos center = getPos();
		ImmersiveTechnology.packetHandler.sendToAllTracking(new MessageStopSound(center), new NetworkRegistry.TargetPoint(world.provider.getDimension(), center.getX(), center.getY(), center.getZ(), 0));
	}

	public void notifyNearbyClients() {
		BlockPos center = getPos();
		ImmersiveTechnology.packetHandler.sendToAllTracking(
				new BinaryMessageTileSync(center, Unpooled.copyInt(energyStorage.getEnergyStored(), speed)),
				new NetworkRegistry.TargetPoint(world.provider.getDimension(), center.getX(), center.getY(), center.getZ(), 0));
	}

	public void efficientMarkDirty() { // !!!!!!! only use it within update() function !!!!!!!
		world.getChunkFromBlockCoords(this.getPos()).markDirty();
	}

	public void checkProvider() {
		if (provider == null || !provider.isValid()) {
			TileEntity tile = world.getTileEntity(getPos().offset(facing, 4));
			if (tile instanceof IMechanicalEnergy) {
				IMechanicalEnergy possibleProvider = (IMechanicalEnergy)tile;
				if (possibleProvider.isValid() && possibleProvider.isMechanicalEnergyTransmitter(facing.getOpposite())) {
					provider = possibleProvider;
				}
			}
		}
		if (provider != null) {
			speed = provider.getSpeed();
			torqueMult = provider.getTorqueMultiplier();
		} else if (speed > 0) speed = Math.max(speed - speedLossPerTick, 0);
	}

	@Override
	public void update() {
		if(!formed) return;
		if(!world.isRemote) {
			checkProvider();
			if(speed > 0) this.energyStorage.modifyEnergyStored(energyGenerated());
			TileEntity tileEntity;
			int currentEnergy = energyStorage.getEnergyStored();
			int transferRate = (int)Math.ceil(rfPerTickPerPort * torqueMult);
			for(int i = 0;i < 6;i++) {
				if(currentEnergy == 0) break;
				if(EnergyOutputPositions[i] == null) EnergyOutputPositions[i] = ITUtils.LocalOffsetToWorldBlockPos(getPos(), i < 3 ? -2 : 2, i < 3 ? i - 1 : i - 4, 0, facing);
				tileEntity = Utils.getExistingTileEntity(world, EnergyOutputPositions[i]);
				EnumFacing energyFacing = i < 3 ? facing.rotateY() : facing.rotateYCCW();
				if(!EnergyHelper.isFluxReceiver(tileEntity, energyFacing)) continue;
				int canReceiveAmount = EnergyHelper.insertFlux(tileEntity, energyFacing, Math.min(currentEnergy, transferRate), true);
				if(canReceiveAmount == 0) continue;
				EnergyHelper.insertFlux(tileEntity, energyFacing, canReceiveAmount, false);
				energyStorage.modifyEnergyStored(-canReceiveAmount);
				currentEnergy = energyStorage.getEnergyStored();
			}
			if(clientUpdateCooldown > 0) clientUpdateCooldown--;
			if(oldEnergy != currentEnergy || oldSpeed != speed) { //check both since this is done server-side and clients get to decide which one to use
				efficientMarkDirty();
				this.markContainingBlockForUpdate(null);
				notifyNearbyClients();
				clientUpdateCooldown = 20;
			} else if(clientUpdateCooldown == 0) { //sync with clients every now and then, even if there's no change of values
				notifyNearbyClients();
				clientUpdateCooldown = 20;
			}
			oldEnergy = currentEnergy;
			oldSpeed = speed;
		} else handleSounds();
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

	@Override
	public void receiveMessageFromServer(ByteBuf buf) {
		int energy = buf.readInt();
		int speed = buf.readInt();
		clientEnergyPercentage = (!soundRPM)? (float) energy / energyStorage.getMaxEnergyStored() : (float) speed / maxSpeed;
	}

	public boolean isMechanicalEnergyReceiver(EnumFacing facing, int position) {
		return this.facing == facing && position == 22;
	}
}
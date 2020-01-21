package ferro2000.immersivetech.api.energy;

import net.minecraft.nbt.NBTTagCompound;

public class MechanicalEnergy {
	protected int torque;
	protected int speed;
	protected int energy;

	public MechanicalEnergy() {
		this.torque = 0;
		this.speed = 0;
		this.energy = 0;
	}

	public MechanicalEnergy(int torqueIn, int speedIn, int energyIn) {
		this.torque = torqueIn;
		this.speed = speedIn;
		this.energy = energyIn;
	}

	public MechanicalEnergy readFromNBT(NBTTagCompound nbt) {
		this.torque = nbt.getInteger("mechanicalEnergyTorque");
		this.speed = nbt.getInteger("mechanicalEnergySpeed");
		this.energy = nbt.getInteger("mechanicalEnergyEnergy");

		return this;
	}

	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt.setInteger("mechanicalEnergyTorque", torque);
		nbt.setInteger("mechanicalEnergySpeed", speed);
		nbt.setInteger("mechanicalEnergyEnergy", energy);

		return nbt;
	}

	public int getTorque() {
		return this.torque;
	}

	public int getSpeed() {
		return this.speed;
	}

	public int getEnergy() {
		return this.energy;
	}

	public void setTorque(int torque) {
		this.torque = torque;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}

	public void setEnergy(int torque, int speed) {
		this.energy = torque * speed;
	}

	public void setMechanicalEnergy(int torque, int speed) {
		this.torque = torque;
		this.speed = speed;
		this.energy = torque * speed;
	}

}
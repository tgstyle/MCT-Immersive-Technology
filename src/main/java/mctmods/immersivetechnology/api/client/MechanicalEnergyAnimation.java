package mctmods.immersivetechnology.api.client;

import net.minecraft.nbt.NBTTagCompound;

public class MechanicalEnergyAnimation {
	protected float animationRotation;
	protected float animationMomentum;

	public MechanicalEnergyAnimation() {
		this.animationRotation = 0;
		this.animationMomentum = 0;
	}

	public MechanicalEnergyAnimation(float rotation, float momentum) {
		this.animationRotation = rotation;
		this.animationMomentum = momentum;
	}

	public MechanicalEnergyAnimation readFromNBT(NBTTagCompound nbt) {
		this.animationRotation = nbt.getFloat("animationRotation");
		this.animationMomentum = nbt.getFloat("animationMomentum");
		return this;
	}

	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt.setFloat("animationRotation", animationRotation);
		nbt.setFloat("animationMomentum", animationMomentum);
		return nbt;
	}

	public float getAnimationRotation() {
		return animationRotation;
	}

	public void setAnimationRotation(float animationRotation) {
		this.animationRotation = animationRotation;
	}

	public float getAnimationMomentum() {
		return animationMomentum;
	}

	public void setAnimationMomentum(float animationMomentum) {
		this.animationMomentum = animationMomentum;
	}

}
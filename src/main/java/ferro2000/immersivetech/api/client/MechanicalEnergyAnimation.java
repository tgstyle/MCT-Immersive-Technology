package ferro2000.immersivetech.api.client;

import net.minecraft.nbt.NBTTagCompound;

public class MechanicalEnergyAnimation {
	
	protected int animationFadeIn;
	protected int animationFadeOut;
	protected float animationRotation;
	protected float animationStep;

	public MechanicalEnergyAnimation() {
		
		this.animationFadeIn = 0;
		this.animationFadeOut = 0;
		this.animationRotation = 0;
		this.animationStep = 0;
		
	}
	
	public MechanicalEnergyAnimation(int fadeIn, int fadeOut, float rotation, float step) {
		
		this.animationFadeIn = fadeIn;
		this.animationFadeOut = fadeOut;
		this.animationRotation = rotation;
		this.animationStep = step;
		
	}
	
	public MechanicalEnergyAnimation readFromNBT(NBTTagCompound nbt) {
		
		this.animationFadeIn = nbt.getInteger("animationFadeIn");
		this.animationFadeOut = nbt.getInteger("animationFadeOut");
		this.animationRotation = nbt.getFloat("animationRotation");
		//this.animationStep = nbt.getFloat("animationStep");
		
		return this;
		
	}
	
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		
		nbt.setInteger("animationFadeIn", animationFadeIn);
		nbt.setInteger("animationFadeOut", animationFadeOut);
		nbt.setFloat("animationRotation", animationRotation);
		//nbt.setFloat("animationStep", animationStep);
		
		return nbt;
		
	}
	
	public int getAnimationFadeIn() {
		return animationFadeIn;
	}

	public void setAnimationFadeIn(int animationFadeIn) {
		this.animationFadeIn = animationFadeIn;
	}

	public int getAnimationFadeOut() {
		return animationFadeOut;
	}

	public void setAnimationFadeOut(int animationFadeOut) {
		this.animationFadeOut = animationFadeOut;
	}

	public float getAnimationRotation() {
		return animationRotation;
	}

	public void setAnimationRotation(float animationRotation) {
		this.animationRotation = animationRotation;
	}
	
	public float getAnimationStep() {
		return animationStep;
	}
	
	public void setAnimationStep(float animationStep) {
		this.animationStep = animationStep;
	}

}

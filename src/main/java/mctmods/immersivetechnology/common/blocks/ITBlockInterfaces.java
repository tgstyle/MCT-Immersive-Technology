package mctmods.immersivetechnology.common.blocks;

import mctmods.immersivetechnology.api.client.MechanicalEnergyAnimation;
import net.minecraft.util.EnumFacing;

public class ITBlockInterfaces {

	public interface IMechanicalEnergy {
		boolean isValid();

		boolean isMechanicalEnergyTransmitter();
		boolean isMechanicalEnergyReceiver();

		EnumFacing getMechanicalEnergyOutputFacing();
		EnumFacing getMechanicalEnergyInputFacing();

		int inputToCenterDistance();
		int outputToCenterDistance();

		int getSpeed();
		float getTorqueMultiplier();
		MechanicalEnergyAnimation getAnimation();
	}

}
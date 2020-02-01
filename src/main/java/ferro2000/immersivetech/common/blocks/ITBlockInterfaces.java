package ferro2000.immersivetech.common.blocks;

import ferro2000.immersivetech.api.client.MechanicalEnergyAnimation;

import net.minecraft.util.EnumFacing;

public class ITBlockInterfaces {

	public interface IMechanicalEnergy {
		boolean isMechanicalEnergyTransmitter();
		boolean isMechanicalEnergyReceiver();

		EnumFacing getMechanicalEnergyOutputFacing();
		EnumFacing getMechanicalEnergyInputFacing();

		int inputToCenterDistance();
		int outputToCenterDistance();

		int getEnergy();
		MechanicalEnergyAnimation getAnimation();
	}

}
package ferro2000.immersivetech.common.blocks;

import ferro2000.immersivetech.api.client.MechanicalEnergyAnimation;
import ferro2000.immersivetech.api.energy.MechanicalEnergy;

import net.minecraft.util.EnumFacing;

public class ITBlockInterface {
	public interface IMechanicalEnergy {
		boolean isMechanicalEnergyTransmitter();
		boolean isMechanicalEnergyReceiver();

		EnumFacing getMechanicalEnergyOutputFacing();
		EnumFacing getMechanicalEnergyInputFacing();

		int inputToCenterDistance();
		int outputToCenterDistance();

		MechanicalEnergy getEnergy();
		MechanicalEnergyAnimation getAnimation();
	}

}
package mctmods.immersivetechnology.api;

import mctmods.immersivetechnology.api.capability.IMechanicalEnergyConsumer;
import mctmods.immersivetechnology.api.capability.IMechanicalEnergyProvider;
import net.minecraftforge.common.capabilities.Capability;

public class MechanicalCapabilities {
    public static final int MAX_RPM = 7200;
    public static Capability<IMechanicalEnergyProvider> MECHANICAL_PROVIDER_CAPABILITY;
    public static Capability<IMechanicalEnergyConsumer> MECHANICAL_CONSUMER_CAPABILITY;
}

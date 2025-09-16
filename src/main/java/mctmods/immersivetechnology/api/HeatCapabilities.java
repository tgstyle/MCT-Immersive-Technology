package mctmods.immersivetechnology.api;

import mctmods.immersivetechnology.api.capability.IHeatProvider;
import mctmods.immersivetechnology.api.capability.IHeatConsumer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class HeatCapabilities {
    public static Capability<IHeatProvider> HEAT_PROVIDER_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});
    public static Capability<IHeatConsumer> HEAT_CONSUMER_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});
}

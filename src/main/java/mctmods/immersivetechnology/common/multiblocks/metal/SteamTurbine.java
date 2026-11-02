package mctmods.immersivetechnology.common.multiblocks.metal;

import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;

import com.immersiveconvergence.api.multiblock.MachineTemplateMultiblock;

public class SteamTurbine extends MachineTemplateMultiblock {
    public static final SteamTurbine INSTANCE = new SteamTurbine();

    public SteamTurbine() { super(Reference.rl("multiblocks/steam_turbine"), () -> ITShapes.get("steam_turbine"), MultiblockRegistry.STEAM_TURBINE); }
}

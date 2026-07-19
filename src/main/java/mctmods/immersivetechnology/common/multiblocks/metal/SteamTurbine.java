package mctmods.immersivetechnology.common.multiblocks.metal;

import mctmods.immersivetechnology.common.multiblocks.helper.ITTemplateMultiblock;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.SteamTurbineShape;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.registration.ITMultiblockRegistry;

import net.minecraft.core.BlockPos;

public class SteamTurbine extends ITTemplateMultiblock {
    public static final SteamTurbine INSTANCE = new SteamTurbine();

    public SteamTurbine() { super(ITLib.rl("multiblocks/steam_turbine"), SteamTurbineShape.MASTER_POS, SteamTurbineShape.TRIGGER_POS, new BlockPos(SteamTurbineShape.WIDTH, SteamTurbineShape.HEIGHT, SteamTurbineShape.LENGTH), SteamTurbineShape.CLIENT_OFFSET, SteamTurbineShape.MANUAL_SCALE, ITMultiblockRegistry.STEAM_TURBINE); }
}

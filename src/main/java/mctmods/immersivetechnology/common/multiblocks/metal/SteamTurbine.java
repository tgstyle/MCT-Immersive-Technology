package mctmods.immersivetechnology.common.multiblocks.metal;

import mctmods.immersivetechnology.common.multiblocks.helper.ModTemplateMultiblock;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.SteamTurbineShape;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;
import net.minecraft.core.BlockPos;

public class SteamTurbine extends ModTemplateMultiblock {
    public static final SteamTurbine INSTANCE = new SteamTurbine();

    public SteamTurbine() { super(Reference.rl("multiblocks/steam_turbine"), SteamTurbineShape.MASTER_POS, SteamTurbineShape.TRIGGER_POS, new BlockPos(SteamTurbineShape.WIDTH,SteamTurbineShape.HEIGHT,SteamTurbineShape.LENGTH), SteamTurbineShape.CLIENT_OFFSET, SteamTurbineShape.MANUAL_SCALE, MultiblockRegistry.STEAM_TURBINE); }
}

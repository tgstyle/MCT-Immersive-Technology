package mctmods.immersivetechnology.common.multiblocks.metal;

import com.immersiveconvergence.api.multiblock.MachineTemplateMultiblock;
import com.immersiveconvergence.api.multiblock.ShapeData;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;
import net.minecraft.core.BlockPos;

public class SteamTurbine extends MachineTemplateMultiblock {
    private static final ShapeData SHAPE = ITShapes.get("steam_turbine");
    public static final SteamTurbine INSTANCE = new SteamTurbine();

    public SteamTurbine() { super(Reference.rl("multiblocks/steam_turbine"), SHAPE.masterPos, SHAPE.triggerPos, new BlockPos(SHAPE.width,SHAPE.height,SHAPE.length), SHAPE.clientOffset, SHAPE.manualScale, MultiblockRegistry.STEAM_TURBINE); }
}

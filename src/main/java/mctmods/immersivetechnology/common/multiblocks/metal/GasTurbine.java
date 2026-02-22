package mctmods.immersivetechnology.common.multiblocks.metal;

import blusunrize.immersiveengineering.api.multiblocks.ClientMultiblocks;
import mctmods.immersivetechnology.common.multiblocks.helper.ITClientMultiblockProperties;
import mctmods.immersivetechnology.common.multiblocks.helper.ITTemplateMultiblock;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.GasTurbineShape;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.registration.ITMultiblockProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class GasTurbine extends ITTemplateMultiblock {
    public static final GasTurbine INSTANCE = new GasTurbine();

    public GasTurbine() { super(ITLib.rl("multiblocks/gas_turbine"), GasTurbineShape.MASTER_POS, GasTurbineShape.TRIGGER_POS, new BlockPos(GasTurbineShape.WIDTH,GasTurbineShape.HEIGHT,GasTurbineShape.LENGTH), ITMultiblockProvider.GAS_TURBINE); }

    @Override public float getManualScale() { return GasTurbineShape.MANUAL_SCALE; }

    @Override public void initializeClient(Consumer<ClientMultiblocks.MultiblockManualData> consumer) { consumer.accept(new ITClientMultiblockProperties(this, GasTurbineShape.CLIENT_OFFSET.getX(), GasTurbineShape.CLIENT_OFFSET.getY(), GasTurbineShape.CLIENT_OFFSET.getZ())); }

    @Override public boolean canBeMirrored() { return true; }
}

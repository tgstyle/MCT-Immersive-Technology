package mctmods.immersivetechnology.common.multiblocks.metal;

import blusunrize.immersiveengineering.api.multiblocks.ClientMultiblocks;
import mctmods.immersivetechnology.common.multiblocks.helper.ITClientMultiblockProperties;
import mctmods.immersivetechnology.common.multiblocks.helper.ITTemplateMultiblock;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.BoilerTankShape;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.registration.ITMultiblockProvider;
import net.minecraft.core.BlockPos;

import java.util.List;
import java.util.function.Consumer;

public class BoilerTank extends ITTemplateMultiblock {
    public static final BoilerTank INSTANCE = new BoilerTank();

    public BoilerTank() {
        super(ITLib.rl("multiblocks/boiler_tank"), BoilerTankShape.MASTER_POS, BoilerTankShape.TRIGGER_POS, new BlockPos(BoilerTankShape.WIDTH, BoilerTankShape.HEIGHT, BoilerTankShape.LENGTH), ITMultiblockProvider.BOILER_TANK);
    }

    @Override public float getManualScale() { return BoilerTankShape.MANUAL_SCALE; }

    @Override public void initializeClient(Consumer<ClientMultiblocks.MultiblockManualData> consumer) { consumer.accept(new ITClientMultiblockProperties(this, BoilerTankShape.CLIENT_OFFSET.getX(), BoilerTankShape.CLIENT_OFFSET.getY(), BoilerTankShape.CLIENT_OFFSET.getZ())); }

    @Override public boolean canBeMirrored() { return false; }

    @Override protected List<BlockPos> symmetricMirror() { return BoilerTankShape.SYMMETRIC_TRIGGER_OFFSETS; }
}

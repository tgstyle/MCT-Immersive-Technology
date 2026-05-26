package mctmods.immersivetechnology.common.multiblocks.metal;

import blusunrize.immersiveengineering.api.multiblocks.ClientMultiblocks;
import mctmods.immersivetechnology.common.multiblocks.helper.ITClientMultiblockProperties;
import mctmods.immersivetechnology.common.multiblocks.helper.ITTemplateMultiblock;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.MeltingCrucibleShape;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.registration.ITMultiblockProvider;
import net.minecraft.core.BlockPos;

import java.util.function.Consumer;

public class MeltingCrucible extends ITTemplateMultiblock {
    public static final MeltingCrucible INSTANCE = new MeltingCrucible();

    public MeltingCrucible() { super(ITLib.rl("multiblocks/melting_crucible"), MeltingCrucibleShape.MASTER_POS, MeltingCrucibleShape.TRIGGER_POS, new BlockPos(MeltingCrucibleShape.WIDTH,MeltingCrucibleShape.HEIGHT,MeltingCrucibleShape.LENGTH), ITMultiblockProvider.MELTING_CRUCIBLE); }

    @Override public float getManualScale() { return MeltingCrucibleShape.MANUAL_SCALE; }

    @Override public void initializeClient(Consumer<ClientMultiblocks.MultiblockManualData> consumer) { consumer.accept(new ITClientMultiblockProperties(this, MeltingCrucibleShape.CLIENT_OFFSET.getX(), MeltingCrucibleShape.CLIENT_OFFSET.getY(), MeltingCrucibleShape.CLIENT_OFFSET.getZ())); }

    @Override public boolean canBeMirrored() { return true; }
}
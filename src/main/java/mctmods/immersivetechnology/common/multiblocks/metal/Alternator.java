package mctmods.immersivetechnology.common.multiblocks.metal;

import blusunrize.immersiveengineering.api.multiblocks.ClientMultiblocks;
import mctmods.immersivetechnology.common.multiblocks.helper.ITClientMultiblockProperties;
import mctmods.immersivetechnology.common.multiblocks.helper.ITTemplateMultiblock;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.AlternatorShape;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.registration.ITMultiblockProvider;
import net.minecraft.core.BlockPos;

import java.util.function.Consumer;

public class Alternator extends ITTemplateMultiblock {
    public static final Alternator INSTANCE = new Alternator();

    public Alternator() { super(ITLib.rl("multiblocks/alternator"), AlternatorShape.MASTER_POS, AlternatorShape.TRIGGER_POS, new BlockPos(AlternatorShape.WIDTH,AlternatorShape.HEIGHT,AlternatorShape.LENGTH), ITMultiblockProvider.ALTERNATOR); }

    @Override public float getManualScale() { return AlternatorShape.MANUAL_SCALE; }

    @Override public void initializeClient(Consumer<ClientMultiblocks.MultiblockManualData> consumer) { consumer.accept(new ITClientMultiblockProperties(this, AlternatorShape.CLIENT_OFFSET.getX(), AlternatorShape.CLIENT_OFFSET.getY(), AlternatorShape.CLIENT_OFFSET.getZ())); }

    @Override public boolean canBeMirrored() { return false; }
}

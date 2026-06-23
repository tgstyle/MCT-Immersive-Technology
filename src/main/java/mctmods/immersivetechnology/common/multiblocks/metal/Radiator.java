package mctmods.immersivetechnology.common.multiblocks.metal;

import blusunrize.immersiveengineering.api.multiblocks.ClientMultiblocks;
import mctmods.immersivetechnology.common.multiblocks.helper.ITClientMultiblockProperties;
import mctmods.immersivetechnology.common.multiblocks.helper.ITTemplateMultiblock;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.RadiatorShape;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.registration.ITMultiblockProvider;
import net.minecraft.core.BlockPos;

import java.util.function.Consumer;

public class Radiator extends ITTemplateMultiblock {
    public static final Radiator INSTANCE = new Radiator();

    public Radiator() { super(ITLib.rl("multiblocks/radiator"), RadiatorShape.MASTER_POS, RadiatorShape.TRIGGER_POS, new BlockPos(RadiatorShape.WIDTH, RadiatorShape.HEIGHT, RadiatorShape.LENGTH), ITMultiblockProvider.RADIATOR); }

    @Override public float getManualScale() { return RadiatorShape.MANUAL_SCALE; }

    @Override public void initializeClient(Consumer<ClientMultiblocks.MultiblockManualData> consumer) { consumer.accept(new ITClientMultiblockProperties(this, RadiatorShape.CLIENT_OFFSET.getX(), RadiatorShape.CLIENT_OFFSET.getY(), RadiatorShape.CLIENT_OFFSET.getZ())); }

    @Override public boolean canBeMirrored() { return false; }
}

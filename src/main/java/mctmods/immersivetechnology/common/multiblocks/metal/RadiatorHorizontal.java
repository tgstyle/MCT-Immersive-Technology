package mctmods.immersivetechnology.common.multiblocks.metal;

import blusunrize.immersiveengineering.api.multiblocks.ClientMultiblocks;
import mctmods.immersivetechnology.common.multiblocks.helper.ITClientMultiblockProperties;
import mctmods.immersivetechnology.common.multiblocks.helper.ITTemplateMultiblock;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.RadiatorHorizontalShape;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.registration.ITMultiblockProvider;
import net.minecraft.core.BlockPos;

import java.util.function.Consumer;

public class RadiatorHorizontal extends ITTemplateMultiblock {
    public static final RadiatorHorizontal INSTANCE = new RadiatorHorizontal();

    public RadiatorHorizontal() { super(ITLib.rl("multiblocks/radiator_horizontal"), RadiatorHorizontalShape.MASTER_POS, RadiatorHorizontalShape.TRIGGER_POS, new BlockPos(RadiatorHorizontalShape.WIDTH, RadiatorHorizontalShape.HEIGHT, RadiatorHorizontalShape.LENGTH), ITMultiblockProvider.RADIATOR_HORIZONTAL); }

    @Override public float getManualScale() { return RadiatorHorizontalShape.MANUAL_SCALE; }

    @Override public void initializeClient(Consumer<ClientMultiblocks.MultiblockManualData> consumer) { consumer.accept(new ITClientMultiblockProperties(this, RadiatorHorizontalShape.CLIENT_OFFSET.getX(), RadiatorHorizontalShape.CLIENT_OFFSET.getY(), RadiatorHorizontalShape.CLIENT_OFFSET.getZ())); }

    @Override public boolean canBeMirrored() { return false; }
}

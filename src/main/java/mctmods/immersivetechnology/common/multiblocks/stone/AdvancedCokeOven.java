package mctmods.immersivetechnology.common.multiblocks.stone;

import blusunrize.immersiveengineering.api.multiblocks.ClientMultiblocks;
import mctmods.immersivetechnology.common.multiblocks.helper.ITClientMultiblockProperties;
import mctmods.immersivetechnology.common.multiblocks.helper.ITTemplateMultiblock;
import mctmods.immersivetechnology.common.multiblocks.stone.shapes.AdvancedCokeOvenShape;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.registration.ITMultiblockProvider;
import net.minecraft.core.BlockPos;

import java.util.function.Consumer;

public class AdvancedCokeOven extends ITTemplateMultiblock {
    public static final AdvancedCokeOven INSTANCE = new AdvancedCokeOven();

    public AdvancedCokeOven() { super(ITLib.rl("multiblocks/advanced_coke_oven"), AdvancedCokeOvenShape.MASTER_POS, AdvancedCokeOvenShape.TRIGGER_POS, new BlockPos(AdvancedCokeOvenShape.WIDTH,AdvancedCokeOvenShape.HEIGHT,AdvancedCokeOvenShape.LENGTH), ITMultiblockProvider.ADVANCED_COKE_OVEN); }

    @Override public float getManualScale() { return AdvancedCokeOvenShape.MANUAL_SCALE; }

    @Override public void initializeClient(Consumer<ClientMultiblocks.MultiblockManualData> consumer) { consumer.accept(new ITClientMultiblockProperties(this, AdvancedCokeOvenShape.CLIENT_OFFSET.getX(), AdvancedCokeOvenShape.CLIENT_OFFSET.getY(), AdvancedCokeOvenShape.CLIENT_OFFSET.getZ())); }

    @Override public boolean canBeMirrored() { return false; }
}

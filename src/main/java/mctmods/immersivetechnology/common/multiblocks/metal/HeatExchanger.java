package mctmods.immersivetechnology.common.multiblocks.metal;

import blusunrize.immersiveengineering.api.multiblocks.ClientMultiblocks;
import mctmods.immersivetechnology.common.multiblocks.helper.ITClientMultiblockProperties;
import mctmods.immersivetechnology.common.multiblocks.helper.ITTemplateMultiblock;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.HeatExchangerShape;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.registration.ITMultiblockProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Mirror;

import java.util.function.Consumer;

public class HeatExchanger extends ITTemplateMultiblock {
    public static final HeatExchanger INSTANCE = new HeatExchanger();

    public HeatExchanger() { super(ITLib.rl("multiblocks/heat_exchanger"), HeatExchangerShape.MASTER_POS, HeatExchangerShape.TRIGGER_POS, new BlockPos(HeatExchangerShape.WIDTH, HeatExchangerShape.HEIGHT, HeatExchangerShape.LENGTH), ITMultiblockProvider.HEAT_EXCHANGER); }

    @Override public float getManualScale() { return HeatExchangerShape.MANUAL_SCALE; }

    @Override public void initializeClient(Consumer<ClientMultiblocks.MultiblockManualData> consumer) { consumer.accept(new ITClientMultiblockProperties(this, HeatExchangerShape.CLIENT_OFFSET.getX(), HeatExchangerShape.CLIENT_OFFSET.getY(), HeatExchangerShape.CLIENT_OFFSET.getZ())); }

    @Override public boolean canBeMirrored() { return true; }

    @Override protected Mirror getAlternateMirror() { return Mirror.LEFT_RIGHT; }

    @Override protected boolean compensateMirrorFacing() { return true; }

    @Override protected boolean flipTriggerForMirror() { return false; }
}

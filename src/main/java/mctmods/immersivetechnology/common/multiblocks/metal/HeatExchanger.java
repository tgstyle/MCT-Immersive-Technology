package mctmods.immersivetechnology.common.multiblocks.metal;

import mctmods.immersivetechnology.common.multiblocks.helper.ITClientMultiblockProperties;
import mctmods.immersivetechnology.common.multiblocks.helper.ITTemplateMultiblock;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.HeatExchangerShape;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.registration.ITMultiblockProvider;

import blusunrize.immersiveengineering.api.multiblocks.ClientMultiblocks;
import net.minecraft.core.BlockPos;

import java.util.function.Consumer;

public class HeatExchanger extends ITTemplateMultiblock {
    public static final HeatExchanger INSTANCE = new HeatExchanger();

    public HeatExchanger() { super(ITLib.rl("multiblocks/heat_exchanger"), HeatExchangerShape.MASTER_POS, HeatExchangerShape.TRIGGER_POS, new BlockPos(HeatExchangerShape.WIDTH, HeatExchangerShape.HEIGHT, HeatExchangerShape.LENGTH), ITMultiblockProvider.HEAT_EXCHANGER); }

    @Override public float getManualScale() { return HeatExchangerShape.MANUAL_SCALE; }

    @Override public void initializeClient(Consumer<ClientMultiblocks.MultiblockManualData> consumer) { consumer.accept(new ITClientMultiblockProperties(this, HeatExchangerShape.CLIENT_OFFSET.getX(), HeatExchangerShape.CLIENT_OFFSET.getY(), HeatExchangerShape.CLIENT_OFFSET.getZ())); }

    @Override public boolean canBeMirrored() { return true; }
}

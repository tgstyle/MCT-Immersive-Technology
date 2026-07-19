package mctmods.immersivetechnology.common.multiblocks.metal;

import mctmods.immersivetechnology.common.multiblocks.helper.ITTemplateMultiblock;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.HeatExchangerShape;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.registration.ITMultiblockRegistry;

import net.minecraft.core.BlockPos;

public class HeatExchanger extends ITTemplateMultiblock {
    public static final HeatExchanger INSTANCE = new HeatExchanger();

    public HeatExchanger() { super(ITLib.rl("multiblocks/heat_exchanger"), HeatExchangerShape.MASTER_POS, HeatExchangerShape.TRIGGER_POS, new BlockPos(HeatExchangerShape.WIDTH, HeatExchangerShape.HEIGHT, HeatExchangerShape.LENGTH), HeatExchangerShape.CLIENT_OFFSET, HeatExchangerShape.MANUAL_SCALE, ITMultiblockRegistry.HEAT_EXCHANGER); }
}

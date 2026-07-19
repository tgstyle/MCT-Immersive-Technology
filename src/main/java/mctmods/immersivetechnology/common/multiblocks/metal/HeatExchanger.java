package mctmods.immersivetechnology.common.multiblocks.metal;

import mctmods.immersivetechnology.common.multiblocks.helper.ModTemplateMultiblock;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.HeatExchangerShape;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;

import net.minecraft.core.BlockPos;

public class HeatExchanger extends ModTemplateMultiblock {
    public static final HeatExchanger INSTANCE = new HeatExchanger();

    public HeatExchanger() { super(Reference.rl("multiblocks/heat_exchanger"), HeatExchangerShape.MASTER_POS, HeatExchangerShape.TRIGGER_POS, new BlockPos(HeatExchangerShape.WIDTH, HeatExchangerShape.HEIGHT, HeatExchangerShape.LENGTH), HeatExchangerShape.CLIENT_OFFSET, HeatExchangerShape.MANUAL_SCALE, MultiblockRegistry.HEAT_EXCHANGER); }
}

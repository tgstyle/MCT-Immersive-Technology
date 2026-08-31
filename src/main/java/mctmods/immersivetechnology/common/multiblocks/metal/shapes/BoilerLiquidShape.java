package mctmods.immersivetechnology.common.multiblocks.metal.shapes;

import com.immersiveconvergence.api.multiblock.GenericShape;
import com.immersiveconvergence.api.multiblock.ShapeData;

import mctmods.immersivetechnology.ImmersiveTechnology;

public class BoilerLiquidShape {
    public static final ShapeData SHAPE = ShapeData.load(ImmersiveTechnology.MODID, "boiler_liquid");
    public static final GenericShape GETTER = SHAPE;
}

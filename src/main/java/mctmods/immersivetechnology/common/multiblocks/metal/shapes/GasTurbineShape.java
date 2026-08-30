package mctmods.immersivetechnology.common.multiblocks.metal.shapes;

import com.immersiveconvergence.api.multiblock.GenericShape;
import com.immersiveconvergence.api.multiblock.ShapeData;

import mctmods.immersivetechnology.ImmersiveTechnology;

public class GasTurbineShape {
    public static final ShapeData SHAPE = ShapeData.load(ImmersiveTechnology.MODID, "gas_turbine");
    public static final GenericShape GETTER = SHAPE;
}

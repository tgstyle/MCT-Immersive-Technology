package mctmods.immersivetechnology.common.blocks.metal;

import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDevice0;
import mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntityFluidPump;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockMetalDevice0 extends blusunrize.immersiveengineering.common.blocks.metal.BlockMetalDevice0 {

    @Override
    public TileEntity createBasicTE(World world, BlockTypes_MetalDevice0 type) {
        if(type == BlockTypes_MetalDevice0.FLUID_PUMP) return new TileEntityFluidPump();
        else return super.createBasicTE(world,type);
    }
}
package mctmods.immersivetechnology.mixin;

import blusunrize.immersiveengineering.common.blocks.metal.TileEntityFluidPipe;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TileEntityFluidPipe.class)
public interface MixinIETileEntityFluidPipe {
    @Accessor(value="connections", remap=false)
    byte getConnections();
    @Accessor(value="connections", remap=false)
    void setConnections(byte connections);
}

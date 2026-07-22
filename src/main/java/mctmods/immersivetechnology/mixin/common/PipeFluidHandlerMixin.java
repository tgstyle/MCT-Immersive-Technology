package mctmods.immersivetechnology.mixin.common;

import mctmods.immersivetechnology.core.ServerConfig;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "blusunrize.immersiveengineering.common.blocks.metal.FluidPipeBlockEntity$PipeFluidHandler")
public abstract class PipeFluidHandlerMixin {

    @Redirect(method = "getTransferableAmount(Lnet/minecraftforge/fluids/FluidStack;Lnet/minecraft/world/level/block/entity/BlockEntity;)I",
            at = @At(value = "INVOKE", target = "Lblusunrize/immersiveengineering/api/fluid/IFluidPipe;getTransferableAmount(Z)I"), remap = false)
    private int it$configurableTransferRate(boolean pressurized) { return pressurized ? ServerConfig.fluidPipeAmountPressurized : ServerConfig.fluidPipeAmountUnpressurized; }
}

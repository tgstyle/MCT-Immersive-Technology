package mctmods.immersivetechnology.mixin.common;

import mctmods.immersivetechnology.core.ServerConfig;

import blusunrize.immersiveengineering.api.fluid.IFluidPipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(IFluidPipe.class)
public interface IFluidPipeMixin {
    @Inject(method = "getTransferableAmount(Z)I", at = @At("HEAD"), cancellable = true, remap = false)
    private static void it$configurableTransferRate(boolean pressurized, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(pressurized ? ServerConfig.fluidPipeAmountPressurized : ServerConfig.fluidPipeAmountUnpressurized);
    }
}

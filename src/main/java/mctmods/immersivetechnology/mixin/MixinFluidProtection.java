package mctmods.immersivetechnology.mixin;

import blusunrize.immersiveengineering.common.register.IEBlocks;
import mctmods.immersivetechnology.core.lib.ITLib;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(value = IEBlocks.class, remap = false)
public class MixinFluidProtection {
    @Shadow @Final @Mutable
    public static Supplier<BlockBehaviour.Properties> METAL_PROPERTIES_NO_OCCLUSION;

    @Shadow @Final
    private static Supplier<BlockBehaviour.Properties> METAL_PROPERTIES_NO_OVERLAY;

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void injectAfterStaticInitialization(CallbackInfo ci) {
        ITLib.IT_LOGGER.info("Mixin applied: IEBlocks <clinit> method called.");
        ITLib.IT_LOGGER.info("Original METAL_PROPERTIES_NO_OVERLAY: {}", METAL_PROPERTIES_NO_OVERLAY.get());
        METAL_PROPERTIES_NO_OCCLUSION = () -> METAL_PROPERTIES_NO_OVERLAY.get()
                .noOcclusion()
                .forceSolidOn();
        ITLib.IT_LOGGER.info("Modified METAL_PROPERTIES_NO_OCCLUSION has been set.");
    }
}

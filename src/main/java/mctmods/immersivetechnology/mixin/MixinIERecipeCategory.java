package mctmods.immersivetechnology.mixin;

import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.core.MCTMixin;
import blusunrize.immersiveengineering.common.util.compat.jei.IERecipeCategory;
import mezz.jei.api.IModRegistry;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = IERecipeCategory.class, remap = false)
public abstract class MixinIERecipeCategory {

    @Shadow
    public String uniqueName;

    @Inject(method = "addCatalysts(Lmezz/jei/api/IModRegistry;)V", at = @At("HEAD"), cancellable = true)
    private void preventIECokeOvenCatalyst(IModRegistry registry, CallbackInfo ci) {
        if ("cokeoven".equals(uniqueName) && Multiblocks.enable.enable_advancedCokeOven) {
            MCTMixin.LOGGER.info("Suppressed IE's duplicate coke oven catalyst - IT registers it alongside the advanced coke oven");
            ci.cancel();
        }
    }
}

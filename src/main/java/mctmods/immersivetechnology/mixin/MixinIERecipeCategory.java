package mctmods.immersivetechnology.mixin;

import blusunrize.immersiveengineering.common.util.compat.jei.IERecipeCategory;
import mctmods.immersivetechnology.core.MCTMixin;
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
        if ("cokeoven".equals(uniqueName)) {
            MCTMixin.LOGGER.info("Prevented IE coke oven catalyst registration - IT now fully controls the JEI tab icon");
            ci.cancel();
        }
    }
}

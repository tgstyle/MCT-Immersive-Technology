package mctmods.immersivetechnology.mixin;

import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.conversion.BoilerLegacyConverter;

import net.minecraftforge.fml.common.StartupQuery;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "net.minecraftforge.registries.GameData", remap = false)
public abstract class MixinMCTGameDataBoiler {
    @Redirect(method = "injectSnapshot", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fml/common/StartupQuery;confirm(Ljava/lang/String;)Z"), remap = false)
    private static boolean explainBoilerConversion(String text) {
        if (!text.contains(ImmersiveTechnology.MODID + ":")) { return StartupQuery.confirm(text); }
        BoilerLegacyConverter.logMissingEntries(text);
        return StartupQuery.confirm(BoilerLegacyConverter.annotate(text));
    }
}

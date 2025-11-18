package mctmods.immersivetechnology.mixin.common;

import blusunrize.immersiveengineering.api.utils.SetRestrictedField;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.List;

@Mixin(targets = "blusunrize.immersiveengineering.api.utils.SetRestrictedField$InitializationTracker", remap = false)
public class InitializationTrackerMixin {
    @Shadow @Final @Mutable
    private List<Pair<Exception, SetRestrictedField<?>>> fields;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) { this.fields = Collections.synchronizedList(this.fields); }
}

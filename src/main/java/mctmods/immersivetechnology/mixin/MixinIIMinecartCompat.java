package mctmods.immersivetechnology.mixin;

import mctmods.immersivetechnology.core.MCTMixin;

import pl.pabilo8.immersiveintelligence.common.compat.it.ImmersiveTechnologyHelper;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Mixin(value = ImmersiveTechnologyHelper.class, remap = false)
public abstract class MixinIIMinecartCompat {

    @Unique private static final String IT_RETYPED_DESCRIPTOR = "mctmods/immersivetechnology/common/blocks/BlockITBase";

    @Unique private static Boolean immersivetechnology$bindsRetypedFields;

    @Inject(method = "addMinecarts", at = @At("HEAD"), cancellable = true, require = 0)
    private void preventCrashOnRetypedBlockFields(CallbackInfo ci) {
        if (!immersivetechnology$bindsRetypedFields()) { return; }
        MCTMixin.LOGGER.info("Suppressed II's IT minecart recipes - this build would crash on the retyped ITContent fields (II #716)");
        ci.cancel();
    }

    @Unique
    private static boolean immersivetechnology$bindsRetypedFields() {
        if (immersivetechnology$bindsRetypedFields == null) {
            immersivetechnology$bindsRetypedFields = immersivetechnology$probeCompatClass();
        }
        return immersivetechnology$bindsRetypedFields;
    }

    @Unique
    private static boolean immersivetechnology$probeCompatClass() {
        try (InputStream in = ImmersiveTechnologyHelper.class.getResourceAsStream("ImmersiveTechnologyHelper.class")) {
            if (in == null) { return true; }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] chunk = new byte[8192];
            for (int read = in.read(chunk); read > 0; read = in.read(chunk)) { out.write(chunk, 0, read); }
            return new String(out.toByteArray(), StandardCharsets.ISO_8859_1).contains(IT_RETYPED_DESCRIPTOR);
        } catch (IOException e) {
            MCTMixin.LOGGER.warn("Could not read Immersive Intelligence's IT compat class to tell whether it needs the minecart guard, assuming it does: {}", String.valueOf(e));
            return true;
        }
    }
}

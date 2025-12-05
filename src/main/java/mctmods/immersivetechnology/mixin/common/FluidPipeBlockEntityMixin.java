package mctmods.immersivetechnology.mixin.common;

import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.common.blocks.metal.FluidPipeBlockEntity;
import net.minecraft.core.Direction;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;
import java.util.Map;

@Mixin(FluidPipeBlockEntity.class)
public abstract class FluidPipeBlockEntityMixin {
    @Final
    @Shadow(remap = false) private Map<Direction, CapabilityReference<IFluidHandler>> neighbors;

    @Inject(method = "updateConnectionByte(Lnet/minecraft/core/Direction;)Z", at = @At("HEAD"), remap = false)
    private void invalidateCache(Direction dir, CallbackInfoReturnable<Boolean> cir) {
        CapabilityReference<IFluidHandler> ref = neighbors.get(dir);
        if (ref != null) {
            try {
                Class<?> refClass = ref.getClass();
                Field currentCapField = refClass.getDeclaredField("currentCap");
                currentCapField.setAccessible(true);
                currentCapField.set(ref, LazyOptional.empty());

                Field lastPosField = refClass.getDeclaredField("lastPos");
                lastPosField.setAccessible(true);
                lastPosField.set(ref, null);

                Field lastWorldField = refClass.getDeclaredField("lastWorld");
                lastWorldField.setAccessible(true);
                lastWorldField.set(ref, null);

                Field lastBEField = refClass.getDeclaredField("lastBE");
                lastBEField.setAccessible(true);
                lastBEField.set(ref, null);
            } catch (Exception ignored) {
                // Silent fallback
            }
        }
    }
}

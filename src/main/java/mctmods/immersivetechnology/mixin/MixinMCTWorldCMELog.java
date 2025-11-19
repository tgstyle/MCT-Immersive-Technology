package mctmods.immersivetechnology.mixin;

import mctmods.immersivetechnology.core.MCTMixin;
import mctmods.immersivetechnology.core.MCTMixinConfig;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ConcurrentModificationException;

@Mixin(World.class)
public abstract class MixinMCTWorldCMELog {
    @Redirect(method = "updateEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/ITickable;update()V", remap = false), remap = false)
    private void redirectTEUpdate(ITickable instance) {
        if (MCTMixinConfig.mixinSettings.enableWorldMixin) { instance.update(); return; }
        try { instance.update(); }
        catch (ConcurrentModificationException e) {
            TileEntity tile = (TileEntity) instance;
            MCTMixin.LOGGER.error("CME caused by TE: {} at {}", tile.getClass().getName(), tile.getPos());
            throw e;
        }
    }
}

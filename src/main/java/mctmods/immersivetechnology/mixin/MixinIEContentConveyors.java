package mctmods.immersivetechnology.mixin;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityConveyorBelt;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityConveyorVertical;
import mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntityConveyorBeltAlternative;
import mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntityConveyorVerticalAlternative;
import mctmods.immersivetechnology.core.MCTMixin;
import mctmods.immersivetechnology.core.MCTMixinConfig;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Locale;

@Mixin(value = IEContent.class, remap = false)
public abstract class MixinIEContentConveyors {
    @Inject(method = "registerTile(Ljava/lang/Class;)V", at = @At("HEAD"), cancellable = true)
    private static void injectRegisterTile(Class<? extends TileEntity> tile, CallbackInfo ci) {
        if (MCTMixinConfig.mixinSettings.replace_IE_conveyors && (tile == TileEntityConveyorBelt.class || tile == TileEntityConveyorVertical.class)) {
            Class<? extends TileEntity> alt = tile == TileEntityConveyorBelt.class ? TileEntityConveyorBeltAlternative.class : TileEntityConveyorVerticalAlternative.class;
            String s = tile.getSimpleName().substring("TileEntity".length()).toLowerCase(Locale.US);
            GameRegistry.registerTileEntity(alt, new ResourceLocation(ImmersiveEngineering.MODID, s));
            MCTMixin.LOGGER.info("Replaced IE {} registration with IT alternative", s);
            ci.cancel();
        }
    }
}

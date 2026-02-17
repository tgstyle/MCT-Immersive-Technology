package mctmods.immersivetechnology.mixin;

import blusunrize.immersiveengineering.common.blocks.metal.BlockConveyor;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_Conveyor;
import mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntityConveyorBeltAlternative;
import mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntityConveyorVerticalAlternative;
import mctmods.immersivetechnology.core.MCTMixinConfig;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockConveyor.class)
public abstract class MixinIEBlockConveyor {
    @Inject(method = "createBasicTE(Lnet/minecraft/world/World;Lblusunrize/immersiveengineering/common/blocks/metal/BlockTypes_Conveyor;)Lnet/minecraft/tileentity/TileEntity;", at = @At("HEAD"), cancellable = true, remap = false)
    private void injectCreateTE(World world, BlockTypes_Conveyor type, CallbackInfoReturnable<TileEntity> cir) {
        if (MCTMixinConfig.mixinSettings.replace_IE_conveyors) {
            if (type.name().contains("VERTICAL")) { cir.setReturnValue(new TileEntityConveyorVerticalAlternative()); }
            else { cir.setReturnValue(new TileEntityConveyorBeltAlternative()); }
        }
    }
}

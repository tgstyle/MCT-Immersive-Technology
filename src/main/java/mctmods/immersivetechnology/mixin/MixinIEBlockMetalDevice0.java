package mctmods.immersivetechnology.mixin;

import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalDevice0;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDevice0;

import mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntityFluidPumpAlternative;
import mctmods.immersivetechnology.core.MCTMixinConfig;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockMetalDevice0.class)
public abstract class MixinIEBlockMetalDevice0 {
    @Inject(method = "createBasicTE(Lnet/minecraft/world/World;Lblusunrize/immersiveengineering/common/blocks/metal/BlockTypes_MetalDevice0;)Lnet/minecraft/tileentity/TileEntity;", at = @At("HEAD"), cancellable = true, remap = false)
    private void injectCreateTE(World world, BlockTypes_MetalDevice0 type, CallbackInfoReturnable<TileEntity> cir) { if (type == BlockTypes_MetalDevice0.FLUID_PUMP && MCTMixinConfig.mixinSettings.replace_IE_pipes) { cir.setReturnValue(new TileEntityFluidPumpAlternative()); } }
}
package mctmods.immersivetechnology.mixin.client;

import mctmods.immersivetechnology.common.multiblocks.helper.ITMultiblockPartBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(BlockRenderDispatcher.class)
public abstract class BlockRenderDispatcherMixin {
    @ModifyVariable(method = "renderBreakingTexture(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/BlockAndTintGetter;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraftforge/client/model/data/ModelData;)V", at = @At(value = "STORE", ordinal = 0), remap = false, name = "bakedmodel")
    private BakedModel it$useFullBlockModelForDamage(BakedModel model, BlockState pState, BlockPos pPos, BlockAndTintGetter pLevel) {
        if (pState.getBlock() instanceof ITMultiblockPartBlock) {
            BlockModelShaper modelShapes = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper();
            return modelShapes.getBlockModel(Blocks.STONE.defaultBlockState());
        }
        return model;
    }
}

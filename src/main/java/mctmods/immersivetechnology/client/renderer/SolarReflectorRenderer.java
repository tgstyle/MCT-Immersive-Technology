package mctmods.immersivetechnology.client.renderer;

import mctmods.immersivetechnology.client.models.multiblock.SolarReflectorModels;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.SolarReflectorLogic;
import mctmods.immersivetechnology.core.util.MirrorAnimation;

import com.immersiveconvergence.api.client.BaseBlockEntityRenderer;
import com.immersiveconvergence.api.multiblock.blocks.env.IMultiblockContext;
import com.immersiveconvergence.api.multiblock.blocks.registry.MultiblockBlockEntityMaster;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.AABB;
import javax.annotation.Nonnull;

public class SolarReflectorRenderer extends BaseBlockEntityRenderer<MultiblockBlockEntityMaster<SolarReflectorLogic.State>> {

    @Override @Nonnull public AABB getRenderBoundingBox(MultiblockBlockEntityMaster<SolarReflectorLogic.State> tile) { return new AABB(tile.getBlockPos()).inflate(6); }

    @Override public void render(@Nonnull MultiblockBlockEntityMaster<SolarReflectorLogic.State> tile, float partialTicks, @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        IMultiblockContext<SolarReflectorLogic.State> context = tile.getHelper().getContext();
        MirrorAnimation animation = context.getState().animation;
        MirrorRendering.render(context, tile.getBlockPos(), tile.getLevel(), poseStack, buffer, packedLight, SolarReflectorModels.SUPPORT, SolarReflectorModels.MIRROR, animation.supportRotation, animation.tilt, 0);
    }
}

package mctmods.immersivetechnology.client.renderer;

import mctmods.immersivetechnology.client.models.multiblock.SolarCollectorModels;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.SolarCollectorLogic;
import mctmods.immersivetechnology.core.util.MirrorAnimation;

import com.immersiveconvergence.api.client.BaseBlockEntityRenderer;
import com.immersiveconvergence.api.multiblock.blocks.env.IMultiblockContext;
import com.immersiveconvergence.api.multiblock.blocks.registry.MultiblockBlockEntityMaster;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.AABB;
import javax.annotation.Nonnull;

public class SolarCollectorRenderer extends BaseBlockEntityRenderer<MultiblockBlockEntityMaster<SolarCollectorLogic.State>> {
    private static final double MODEL_OFFSET = -1.5;

    @Override @Nonnull public AABB getRenderBoundingBox(MultiblockBlockEntityMaster<SolarCollectorLogic.State> tile) { return new AABB(tile.getBlockPos()).inflate(6); }

    @Override public void render(@Nonnull MultiblockBlockEntityMaster<SolarCollectorLogic.State> tile, float partialTicks, @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        IMultiblockContext<SolarCollectorLogic.State> context = tile.getHelper().getContext();
        MirrorAnimation animation = context.getState().animation;
        MirrorRendering.render(context, tile.getBlockPos(), tile.getLevel(), poseStack, buffer, packedLight, SolarCollectorModels.SUPPORT, SolarCollectorModels.PANEL, animation.supportRotation, animation.tilt, MODEL_OFFSET);
    }
}

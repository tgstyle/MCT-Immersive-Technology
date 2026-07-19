package mctmods.immersivetechnology.client.renderer;

import mctmods.immersivetechnology.client.models.multiblock.RotorModels;
import mctmods.immersivetechnology.client.models.ModDynamicModel;
import mctmods.immersivetechnology.client.renderer.helper.RenderUtils;
import mctmods.immersivetechnology.common.blocks.metal.RotorCreativeBlock;
import mctmods.immersivetechnology.common.blocks.metal.logic.RotorCreativeBlockEntity;

import blusunrize.immersiveengineering.api.ApiUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import java.util.List;

public class RotorCreativeRenderer implements BlockEntityRenderer<RotorCreativeBlockEntity> {
    private static final Quaternionf ROTATION = new Quaternionf();

    public RotorCreativeRenderer() {}

    @Override public void render(@NotNull RotorCreativeBlockEntity tile, float partialTicks, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = tile.getBlockState();
        Direction dir = state.getValue(RotorCreativeBlock.FACING);
        Vec3 axisVec = Vec3.atLowerCornerOf(dir.getNormal());
        double angle = tile.animation_rotation + tile.animation_step * partialTicks * Math.signum(tile.rpm);
        ModDynamicModel selectedModel = (dir == Direction.EAST || dir == Direction.WEST) ? RotorModels.ROTOR_EAST_WEST : RotorModels.ROTOR;
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        ROTATION.rotationAxis((float)(angle * Mth.DEG_TO_RAD), (float) axisVec.x, (float) axisVec.y, (float) axisVec.z);
        poseStack.mulPose(ROTATION);
        renderDynamicModel(selectedModel, poseStack, buffer, tile.getLevel(), tile.getBlockPos(), packedLight);
        poseStack.popPose();
    }

    private void renderDynamicModel(ModDynamicModel model, PoseStack matrix, MultiBufferSource buffer, Level level, BlockPos pos, int light) {
        List<BakedQuad> quads = model.get().getQuads(null, null, ApiUtils.RANDOM_SOURCE, ModelData.EMPTY, null);
        RenderUtils.renderModelTESRFancy(quads, buffer.getBuffer(RenderType.solid()), matrix, level, pos, false, 0xffffff, light);
    }
}

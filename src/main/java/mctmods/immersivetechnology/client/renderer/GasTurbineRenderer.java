package mctmods.immersivetechnology.client.renderer;

import mctmods.immersivetechnology.client.models.multiblock.RotorModels;
import mctmods.immersivetechnology.client.models.ModDynamicModel;
import mctmods.immersivetechnology.client.renderer.helper.BaseBlockEntityRenderer;
import com.immersiveconvergence.api.client.RenderUtils;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.GasTurbineLogic;
import mctmods.immersivetechnology.core.ClientConfig;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockBEHelperMaster;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityMaster;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MultiblockOrientation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.ModelData;
import org.joml.Quaternionf;
import java.util.List;

import javax.annotation.Nonnull;

public class GasTurbineRenderer extends BaseBlockEntityRenderer<MultiblockBlockEntityMaster<GasTurbineLogic.State>> {
    private static final Quaternionf ROTATION = new Quaternionf();

    public GasTurbineRenderer() {}

    @Override public void render(@Nonnull MultiblockBlockEntityMaster<GasTurbineLogic.State> tile, float partialTicks, @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        IMultiblockBEHelperMaster<GasTurbineLogic.State> helper = tile.getHelper();
        IMultiblockContext<GasTurbineLogic.State> context = helper.getContext();
        GasTurbineLogic.State state = context.getState();
        MultiblockOrientation orientation = context.getLevel().getOrientation();
        BlockPos pos = tile.getBlockPos();
        Level level = tile.getLevel();
        Direction dir = orientation.front();
        Vec3 axisVec = Vec3.atLowerCornerOf(dir.getNormal());
        double angle = state.animation_fanRotation + state.animation_fanRotationStep * partialTicks;
        if (!ClientConfig.doSpecialRenderGasTurbine) { angle = 0; }
        Vec3 rotorStart = Vec3.atLowerCornerOf(context.getLevel().toAbsolute(new BlockPos(1, 1, 0)).subtract(pos));
        poseStack.pushPose();
        poseStack.translate(rotorStart.x + 0.5, rotorStart.y + 0.5, rotorStart.z + 0.5);
        ROTATION.rotationAxis((float)(angle * Mth.DEG_TO_RAD), (float) axisVec.x, (float) axisVec.y, (float) axisVec.z);
        poseStack.mulPose(ROTATION);
        ModDynamicModel selectedModel = (dir == Direction.EAST || dir == Direction.WEST) ? RotorModels.ROTOR_EAST_WEST : RotorModels.ROTOR;
        renderDynamicModel(selectedModel, poseStack, buffer, level, pos, packedLight);
        poseStack.popPose();
    }

    private void renderDynamicModel(ModDynamicModel model, PoseStack matrix, MultiBufferSource buffer, Level level, BlockPos pos, int light) {
        matrix.pushPose();
        List<BakedQuad> quads = model.get().getQuads(null, null, ApiUtils.RANDOM_SOURCE, ModelData.EMPTY, null);
        RenderUtils.renderModelTESRFancy(quads, buffer.getBuffer(RenderType.solid()), matrix, level, pos, false, 0xffffff, light);
        matrix.popPose();
    }
}

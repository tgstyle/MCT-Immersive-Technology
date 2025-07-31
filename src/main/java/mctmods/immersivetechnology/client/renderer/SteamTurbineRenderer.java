package mctmods.immersivetechnology.client.renderer;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockBEHelperMaster;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityMaster;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MultiblockOrientation;
import blusunrize.immersiveengineering.client.utils.RenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import mctmods.immersivetechnology.client.models.ITDynamicModel;
import mctmods.immersivetechnology.common.blocks.multiblocks.logic.ITSteamTurbineLogic;
import mctmods.immersivetechnology.core.ITClientConfig;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;

import java.util.List;

public class SteamTurbineRenderer extends ITBlockEntityRenderer<MultiblockBlockEntityMaster<ITSteamTurbineLogic.State>> {
    public static ITDynamicModel MODEL;
    public static ITDynamicModel MODEL_EAST_WEST;
    public static final String NAME = "steam_turbine_rotor";
    public static final String NAME_EAST_WEST = "steam_turbine_rotor_west_east";

    @Override
    public void render(@NotNull MultiblockBlockEntityMaster<ITSteamTurbineLogic.State> tile, float partialTicks, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int pPackedLight, int pPackedOverlay) {
        if (!ITClientConfig.doSpecialRenderSteamTurbine.get()) return;

        IMultiblockBEHelperMaster<ITSteamTurbineLogic.State> helper = tile.getHelper();
        IMultiblockContext<ITSteamTurbineLogic.State> context = helper.getContext();
        ITSteamTurbineLogic.State state = context.getState();

        final MultiblockOrientation orientation = context.getLevel().getOrientation();
        BlockPos pos = tile.getBlockPos();
        Level level = tile.getLevel();
        Direction dir = orientation.front();

        poseStack.pushPose();
        {
            double ox = 0, oy = 0, oz = 0;
            if (dir == Direction.NORTH) oz = 5;
            else if (dir == Direction.EAST) ox = -5;
            poseStack.translate(ox + 0.5, oy + 0.5, oz + 0.5);
            poseStack.mulPose(new Quaternionf().rotateAxis((state.animation_fanRotation + (state.animation_fanRotationStep * partialTicks)) * Mth.DEG_TO_RAD, Vec3.atLowerCornerOf(dir.getNormal()).toVector3f()));
            ITDynamicModel selectedModel = (dir == Direction.EAST || dir == Direction.WEST) ? MODEL_EAST_WEST : MODEL;
            renderDynamicModel(selectedModel, poseStack, buffer, level, pos, pPackedLight);
        }
        poseStack.popPose();
    }

    private void renderDynamicModel(ITDynamicModel model, PoseStack matrix, MultiBufferSource buffer, Level level, BlockPos pos, int light) {
        matrix.pushPose();
        {
            List<BakedQuad> quads = model.get().getQuads(null, null, ApiUtils.RANDOM_SOURCE, ModelData.EMPTY, null);
            RenderUtils.renderModelTESRFancy(quads, buffer.getBuffer(RenderType.cutout()), matrix, level, pos, false, 0xffffff, light);
        }
        matrix.popPose();
    }
}

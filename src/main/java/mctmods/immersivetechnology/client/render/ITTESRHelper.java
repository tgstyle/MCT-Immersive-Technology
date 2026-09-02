package mctmods.immersivetechnology.client.render;

import blusunrize.immersiveengineering.client.ClientUtils;

import mctmods.immersivetechnology.common.Config.ITConfig;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.List;

public final class ITTESRHelper {
    private static final double BASE_RENDER_DISTANCE = 64;

    private ITTESRHelper() {}

    public static boolean inRenderRange(double x, double y, double z) {
        double max = BASE_RENDER_DISTANCE * ITConfig.client.render.multiblockSpecialRenderDistanceModifier;
        return x * x + y * y + z * z <= max * max;
    }

    public static void renderModel(BlockModelRenderer renderer, IBlockAccess world, IBakedModel model, IBlockState state, BlockPos pos, BufferBuilder buffer) {
        if (ITConfig.client.render.disableFancyTESR) { renderer.renderModelFlat(world, model, state, pos, buffer, false, 0L); }
        else { renderer.renderModel(world, model, state, pos, buffer, false); }
    }

    public static void renderQuads(List<BakedQuad> quads, BufferBuilder buffer, World world, BlockPos pos, boolean useCached) {
        if (ITConfig.client.render.disableFancyTESR) { ClientUtils.renderModelTESRFast(quads, buffer, world, pos); }
        else { ClientUtils.renderModelTESRFancy(quads, buffer, world, pos, useCached); }
    }
}

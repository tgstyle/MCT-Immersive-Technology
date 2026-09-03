package mctmods.immersivetechnology.client.render;

import com.immersiveconvergence.api.client.RenderUtils;

import blusunrize.immersiveengineering.client.ClientUtils;

import mctmods.immersivetechnology.common.Config.ITConfig;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public final class ITTESRHelper {
    private static final double BASE_RENDER_DISTANCE = 64;

    private ITTESRHelper() {}

    public static boolean outOfRenderRange(double x, double y, double z) {
        double max = BASE_RENDER_DISTANCE * ITConfig.Client.render.multiblockSpecialRenderDistanceModifier;
        return x * x + y * y + z * z > max * max;
    }

    public static void renderQuads(List<BakedQuad> quads, BufferBuilder buffer, World world, BlockPos pos, boolean useCached) {
        if (ITConfig.Client.render.disableFancyTESR) { ClientUtils.renderModelTESRFast(quads, buffer, world, pos); }
        else { RenderUtils.renderModelTESRFancy(quads, buffer, world, pos, useCached); }
    }
}

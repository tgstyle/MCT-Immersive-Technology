package mctmods.immersivetechnology.client.renderer;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityMaster;
import com.mojang.blaze3d.vertex.PoseStack;
import mctmods.immersivetechnology.client.particles.ColoredBeam;
import mctmods.immersivetechnology.client.renderer.helper.ITBaseBlockEntityRenderer;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.SolarMelterLogic;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.SolarMelterRecipe;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public class SolarMelterRenderer extends ITBaseBlockEntityRenderer<MultiblockBlockEntityMaster<SolarMelterLogic.State>> {

    public SolarMelterRenderer() {}

    private static final ResourceLocation BEAM_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/beacon_beam.png");

    @Override public void render(MultiblockBlockEntityMaster<SolarMelterLogic.State> be, float partialTicks, @NotNull PoseStack matrixStack, @NotNull MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        SolarMelterLogic.State state = be.getHelper().getState();
        IMultiblockContext<SolarMelterLogic.State> ctx = be.getHelper().getContext();
        IMultiblockLevel mLevel = ctx.getLevel();
        Level level = be.getLevel();
        FluidStack fs = state.tanks.input().getFluid();
        double maxHeat = SolarMelterLogic.WORKING_HEAT_LEVEL;
        if (fs.getAmount() > 0) {
            SolarMelterRecipe recipe = SolarMelterRecipe.findRecipe(level, fs);
            if (recipe != null) { maxHeat = recipe.requiredTemp; }
        }
        if (state.heatLevel < maxHeat || !state.sunVisible || state.reflectorStrength <= 0) { return; }
        BlockPos masterPos = be.getBlockPos();
        BlockPos particlePos = mLevel.toAbsolute(SolarMelterLogic.PARTICLE_POI);
        BlockPos centerBottomPos = mLevel.toAbsolute(new BlockPos(SolarMelterLogic.PARTICLE_POI.getX(), 0, SolarMelterLogic.PARTICLE_POI.getZ()));
        double relX = centerBottomPos.getX() - masterPos.getX();
        double relY = centerBottomPos.getY() - masterPos.getY();
        double relZ = centerBottomPos.getZ() - masterPos.getZ();
        matrixStack.pushPose();
        matrixStack.translate(relX, relY, relZ);
        assert level != null;
        long time = level.getGameTime();
        float innerBottomG = 1.0F;
        float innerTopG = 0.0F;
        float innerA = 1.0F;
        float outerBottomG = 1.0F;
        float outerTopG = 0.0F;
        float outerA = 0.2F;
        double worldX = centerBottomPos.getX() + 0.5D;
        double worldZ = centerBottomPos.getZ() + 0.5D;
        double worldYBottom = particlePos.getY();
        double worldYTop = particlePos.getY() + 16.0D;
        ColoredBeam.renderBeam(matrixStack, bufferIn, BEAM_TEXTURE, partialTicks, 1.0F, time, false, innerBottomG, innerTopG, innerA, outerBottomG, outerTopG, outerA, 2.0F, 18.0F, worldX, worldYBottom, worldYTop, worldZ);
        matrixStack.popPose();
    }

    @Override public boolean shouldRenderOffScreen(@NotNull MultiblockBlockEntityMaster<SolarMelterLogic.State> be) { return true; }
}

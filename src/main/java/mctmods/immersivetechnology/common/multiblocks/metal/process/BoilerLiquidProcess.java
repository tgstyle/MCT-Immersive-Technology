package mctmods.immersivetechnology.common.multiblocks.metal.process;

import mctmods.immersivetechnology.common.multiblocks.metal.logic.BoilerLiquidLogic;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.BoilerLiquidRecipe;
import mctmods.immersivetechnology.core.util.Ignition;

import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IMultiblockComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

public class BoilerLiquidProcess implements IMultiblockComponent<BoilerLiquidLogic.State> {
    @Override public InteractionResult click(IMultiblockContext<BoilerLiquidLogic.State> ctx, BlockPos posInMultiblock, Player player, InteractionHand hand, BlockHitResult absoluteHit, boolean isClient) {
        if (Ignition.misses(ctx, posInMultiblock, absoluteHit, player.getItemInHand(hand), BoilerLiquidLogic.IGNITION_POIS, BoilerLiquidLogic.IGNITION_FACING)) { return InteractionResult.PASS; }
        BoilerLiquidLogic.State state = ctx.getState();
        if (state.pilotLit || state.tanks.input1().getFluidAmount() <= 0 || BoilerLiquidRecipe.findRecipe(ctx.getLevel().getRawLevel(), state.tanks.input1().getFluid()) == null) { return InteractionResult.PASS; }
        if (isClient) { return InteractionResult.SUCCESS; }
        state.pilotLit = true;
        state.heatLevel = BoilerLiquidLogic.pilotHeat();
        Ignition.light(ctx, BoilerLiquidLogic.IGNITION_POIS.get(0), player, hand);
        return InteractionResult.SUCCESS;
    }
}

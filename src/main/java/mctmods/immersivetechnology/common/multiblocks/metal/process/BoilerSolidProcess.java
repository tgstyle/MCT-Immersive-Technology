package mctmods.immersivetechnology.common.multiblocks.metal.process;

import mctmods.immersivetechnology.common.multiblocks.metal.logic.BoilerSolidLogic;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.BoilerSolidRecipe;
import mctmods.immersivetechnology.core.util.Ignition;

import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IMultiblockComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.phys.BlockHitResult;

public class BoilerSolidProcess implements IMultiblockComponent<BoilerSolidLogic.State> {
    @Override public ItemInteractionResult click(IMultiblockContext<BoilerSolidLogic.State> ctx, BlockPos posInMultiblock, Player player, InteractionHand hand, BlockHitResult absoluteHit, boolean isClient) {
        if (Ignition.misses(ctx, posInMultiblock, absoluteHit, player.getItemInHand(hand), BoilerSolidLogic.IGNITION_POIS, BoilerSolidLogic.IGNITION_FACING)) { return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION; }
        BoilerSolidLogic.State state = ctx.getState();
        if (state.pilotLit) { return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION; }
        ItemStack fuelStack = state.inventory.getStackInSlot(BoilerSolidLogic.INPUT_FUEL_SLOT);
        if (fuelStack.isEmpty()) { return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION; }
        BoilerSolidRecipe recipe = BoilerSolidRecipe.findRecipe(ctx.getLevel().getRawLevel(), fuelStack);
        int consumeAmount = recipe != null ? recipe.input().getCount() : 1;
        if (fuelStack.copyWithCount(1).getBurnTime(RecipeType.SMELTING) <= 0 || fuelStack.getCount() < consumeAmount) { return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION; }
        if (isClient) { return ItemInteractionResult.SUCCESS; }
        state.pilotLit = true;
        state.heatLevel = BoilerSolidLogic.pilotHeat();
        Ignition.light(ctx, BoilerSolidLogic.IGNITION_POIS.getFirst(), player, hand);
        return ItemInteractionResult.SUCCESS;
    }
}

package mctmods.immersivetechnology.common.multiblocks.metal.process;

import mctmods.immersivetechnology.common.multiblocks.metal.logic.BoilerSolidLogic;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.BoilerSolidRecipe;
import mctmods.immersivetechnology.core.util.Ignition;

import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IMultiblockComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.ForgeHooks;

public class BoilerSolidProcess implements IMultiblockComponent<BoilerSolidLogic.State> {
    @Override public InteractionResult click(IMultiblockContext<BoilerSolidLogic.State> ctx, BlockPos posInMultiblock, Player player, InteractionHand hand, BlockHitResult absoluteHit, boolean isClient) {
        if (Ignition.misses(ctx, posInMultiblock, absoluteHit, player.getItemInHand(hand), BoilerSolidLogic.IGNITION_POIS, BoilerSolidLogic.IGNITION_FACING)) { return InteractionResult.PASS; }
        BoilerSolidLogic.State state = ctx.getState();
        if (state.pilotLit) { return InteractionResult.PASS; }
        ItemStack fuelStack = state.inventory.getStackInSlot(BoilerSolidLogic.INPUT_FUEL_SLOT);
        if (fuelStack.isEmpty()) { return InteractionResult.PASS; }
        BoilerSolidRecipe recipe = BoilerSolidRecipe.findRecipe(ctx.getLevel().getRawLevel(), fuelStack);
        int consumeAmount = recipe != null ? recipe.input.getCount() : 1;
        if (ForgeHooks.getBurnTime(fuelStack, RecipeType.SMELTING) <= 0 || fuelStack.getCount() < consumeAmount) { return InteractionResult.PASS; }
        if (isClient) { return InteractionResult.SUCCESS; }
        state.pilotLit = true;
        state.heatLevel = BoilerSolidLogic.pilotHeat();
        Ignition.light(ctx, BoilerSolidLogic.IGNITION_POIS.get(0), player, hand);
        return InteractionResult.SUCCESS;
    }
}

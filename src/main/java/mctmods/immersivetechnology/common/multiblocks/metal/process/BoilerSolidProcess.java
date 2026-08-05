package mctmods.immersivetechnology.common.multiblocks.metal.process;

import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IMultiblockComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.BoilerSolidLogic;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.BoilerSolidRecipe;
import mctmods.immersivetechnology.core.registration.Sounds;
import mctmods.immersivetechnology.core.registration.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

public class BoilerSolidProcess implements IMultiblockComponent<BoilerSolidLogic.State> {
    @Override public ItemInteractionResult click(IMultiblockContext<BoilerSolidLogic.State> ctx, BlockPos posInMultiblock, Player player, InteractionHand hand, BlockHitResult absoluteHit, boolean isClient) {
        if (!BoilerSolidLogic.IGNITION_POIS.contains(posInMultiblock)) { return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION; }
        Direction hitDir = absoluteHit.getDirection();
        if (BoilerSolidLogic.IGNITION_FACING != null) {
            Direction poiSide = ctx.getLevel().toAbsolute(BoilerSolidLogic.IGNITION_FACING);
            if (hitDir != poiSide) { return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION; }
        }
        ItemStack held = player.getItemInHand(hand);
        if (!held.is(ModTags.igniters)) { return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION; }
        BoilerSolidLogic.State state = ctx.getState();
        if (state.pilotLit) { return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION; }
        Level level = ctx.getLevel().getRawLevel();
        ItemStack fuelStack = state.inventory.getStackInSlot(BoilerSolidLogic.INPUT_FUEL_SLOT);
        BoilerSolidRecipe recipe = fuelStack.isEmpty() ? null : BoilerSolidRecipe.findRecipe(level, fuelStack);
        ItemStack single = fuelStack.copy();
        single.setCount(1);
        int burnTime = single.getBurnTime(RecipeType.SMELTING);
        int consumeAmount = (recipe != null) ? recipe.input().getCount() : 1;
        if (fuelStack.isEmpty() || burnTime <= 0 || fuelStack.getCount() < consumeAmount) { return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION; }
        if (isClient) { return ItemInteractionResult.SUCCESS; }
        state.pilotLit = true;
        state.heatLevel = BoilerSolidLogic.pilotHeat();
        level.playSound(null, ctx.getLevel().toAbsolute(BoilerSolidLogic.IGNITION_POIS.getFirst()), Sounds.gasIgnite.get(), SoundSource.BLOCKS, 0.5f, 1.0f);
        if (held.is(ModTags.igniters_consume)) { held.shrink(1); }
        else if (held.getMaxDamage() > 0) { held.hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND); }
        ctx.markMasterDirty();
        ctx.requestMasterBESync();
        return ItemInteractionResult.SUCCESS;
    }
}

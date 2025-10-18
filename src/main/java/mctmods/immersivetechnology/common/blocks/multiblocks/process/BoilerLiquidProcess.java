package mctmods.immersivetechnology.common.blocks.multiblocks.process;

import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IMultiblockComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import mctmods.immersivetechnology.common.blocks.multiblocks.logic.BoilerLiquidLogic;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.BoilerLiquidRecipe;
import mctmods.immersivetechnology.core.registration.ITSounds;
import mctmods.immersivetechnology.core.registration.ITTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

public class BoilerLiquidProcess implements IMultiblockComponent<BoilerLiquidLogic.State> {
    @Override
    public InteractionResult click(IMultiblockContext<BoilerLiquidLogic.State> ctx, BlockPos posInMultiblock, Player player, InteractionHand hand, BlockHitResult absoluteHit, boolean isClient) {
        if (!BoilerLiquidLogic.IGNITION_POI.contains(posInMultiblock)) { return InteractionResult.PASS; }
        Direction hitDir = absoluteHit.getDirection();
        if (BoilerLiquidLogic.IGNITION_FACING != null) {
            Direction poiSide = ctx.getLevel().toAbsolute(BoilerLiquidLogic.IGNITION_FACING);
            if (hitDir != poiSide) { return InteractionResult.PASS; }
        }
        ItemStack held = player.getItemInHand(hand);
        if (!held.is(ITTags.igniters)) { return InteractionResult.PASS; }
        BoilerLiquidLogic.State state = ctx.getState();
        if (state.pilotLit) { return InteractionResult.PASS; }
        Level level = ctx.getLevel().getRawLevel();
        if (state.tanks.input1().getFluidAmount() <= 0 || BoilerLiquidRecipe.findRecipe(level, state.tanks.input1().getFluid()) == null) { return InteractionResult.PASS; }
        if (isClient) { return InteractionResult.SUCCESS; }
        state.pilotLit = true;
        state.heatLevel = BoilerLiquidLogic.PILOT_HEAT;
        level.playSound(null, ctx.getLevel().toAbsolute(BoilerLiquidLogic.IGNITION_POI.get(0)), ITSounds.gasIgnite.get(), SoundSource.BLOCKS, 0.5f, 1.0f);
        if (held.is(ITTags.igniters_consume)) { held.shrink(1); }
        else if (held.getMaxDamage() > 0) { held.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand)); }
        ctx.markMasterDirty();
        ctx.requestMasterBESync();
        return InteractionResult.SUCCESS;
    }
}

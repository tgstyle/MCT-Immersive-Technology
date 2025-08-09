package mctmods.immersivetechnology.common.blocks.multiblocks.process;

import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IMultiblockComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import mctmods.immersivetechnology.common.blocks.multiblocks.logic.BoilerLogic;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.BoilerFuelRecipe;
import mctmods.immersivetechnology.core.registration.ITSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

public class BoilerProcess implements IMultiblockComponent<BoilerLogic.State> {
    private static final BlockPos IGNITION_POS = new BlockPos(4, 1, 1);

    @Override
    public InteractionResult click(IMultiblockContext<BoilerLogic.State> ctx, BlockPos posInMultiblock, Player player, InteractionHand hand, BlockHitResult absoluteHit, boolean isClient) {
        if (!IGNITION_POS.equals(posInMultiblock)) return InteractionResult.PASS;
        ItemStack held = player.getItemInHand(hand);
        if (!held.is(Items.TORCH)) return InteractionResult.PASS;
        BoilerLogic.State state = ctx.getState();
        if (state.pilotLit) return InteractionResult.PASS;
        Level level = ctx.getLevel().getRawLevel();
        if (state.tanks.input1().getFluidAmount() <= 0 || BoilerFuelRecipe.findFuel(level, state.tanks.input1().getFluid()) == null) return InteractionResult.PASS;
        state.pilotLit = true;
        state.heatLevel = BoilerLogic.PILOT_HEAT;
        level.playSound(null, ctx.getLevel().toAbsolute(IGNITION_POS), ITSounds.gasIgnite.get(), SoundSource.BLOCKS, 0.5f, 1.0f);
        ctx.markMasterDirty();
        ctx.requestMasterBESync();
        state.clientUpdateCooldown = 1;
        return InteractionResult.sidedSuccess(isClient);
    }
}

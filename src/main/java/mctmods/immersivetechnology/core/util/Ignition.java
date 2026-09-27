package mctmods.immersivetechnology.core.util;

import mctmods.immersivetechnology.core.registration.ModTags;
import mctmods.immersivetechnology.core.registration.Sounds;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.RelativeBlockFace;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import java.util.List;

public class Ignition {
    public static boolean misses(IMultiblockContext<?> ctx, BlockPos posInMultiblock, BlockHitResult hit, ItemStack held, List<BlockPos> pois, RelativeBlockFace facing) {
        if (!pois.contains(posInMultiblock)) { return true; }
        if (facing != null && hit.getDirection() != ctx.getLevel().toAbsolute(facing)) { return true; }
        return !held.is(ModTags.igniters);
    }

    public static void light(IMultiblockContext<?> ctx, BlockPos poi, Player player, InteractionHand hand) {
        ctx.getLevel().getRawLevel().playSound(null, ctx.getLevel().toAbsolute(poi), Sounds.gasIgnite.get(), SoundSource.BLOCKS, 0.5f, 1.0f);
        ItemStack held = player.getItemInHand(hand);
        if (held.is(ModTags.igniters_consume)) { held.shrink(1); }
        else if (held.getMaxDamage() > 0) { held.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand)); }
        ctx.markMasterDirty();
        ctx.requestMasterBESync();
    }
}

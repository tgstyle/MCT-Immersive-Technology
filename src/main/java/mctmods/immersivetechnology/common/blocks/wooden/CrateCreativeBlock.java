package mctmods.immersivetechnology.common.blocks.wooden;

import mctmods.immersivetechnology.common.blocks.helper.ITEntityBlock;
import mctmods.immersivetechnology.common.blocks.wooden.logic.CrateCreativeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import java.util.function.BiFunction;

public class CrateCreativeBlock extends ITEntityBlock<CrateCreativeBlockEntity> {
    public CrateCreativeBlock(BiFunction<BlockPos, BlockState, CrateCreativeBlockEntity> beFactory, Properties p) { super(beFactory, p); }

    @Override public void setPlacedBy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, LivingEntity placer, @NotNull ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack); if (!level.isClientSide) { CrateCreativeBlockEntity be = (CrateCreativeBlockEntity) level.getBlockEntity(pos); if (be != null) be.onBEPlaced(stack); }
    }

    @Override public @NotNull InteractionResult use(@NotNull BlockState state, Level level, @NotNull BlockPos pos, Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS; CrateCreativeBlockEntity tile = (CrateCreativeBlockEntity) level.getBlockEntity(pos); if (tile != null) NetworkHooks.openScreen((ServerPlayer) player, tile, b -> b.writeBlockPos(pos)); return InteractionResult.CONSUME;
    }
}

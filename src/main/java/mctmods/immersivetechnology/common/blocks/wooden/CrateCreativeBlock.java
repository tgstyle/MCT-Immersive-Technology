package mctmods.immersivetechnology.common.blocks.wooden;

import com.immersiveconvergence.api.block.ModEntityBlock;
import mctmods.immersivetechnology.common.blocks.wooden.logic.CrateCreativeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import java.util.function.BiFunction;

public class CrateCreativeBlock extends ModEntityBlock<CrateCreativeBlockEntity> {
    public CrateCreativeBlock(BiFunction<BlockPos, BlockState, CrateCreativeBlockEntity> beFactory, Properties p) { super(beFactory, p); }

    @Override public void setPlacedBy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, LivingEntity placer, @NotNull ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack); if (!level.isClientSide) { CrateCreativeBlockEntity be = (CrateCreativeBlockEntity) level.getBlockEntity(pos); if (be != null) be.onBEPlaced(stack); }
    }
}

package mctmods.immersivetechnology.common.blocks.wooden;

import com.immersiveconvergence.api.block.ModEntityBlock;
import mctmods.immersivetechnology.common.blocks.wooden.logic.CrateCreativeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import java.util.function.BiFunction;

import javax.annotation.Nonnull;

public class CrateCreativeBlock extends ModEntityBlock<CrateCreativeBlockEntity> {
    public CrateCreativeBlock(BiFunction<BlockPos, BlockState, CrateCreativeBlockEntity> beFactory, Properties p) { super(beFactory, p); }

    @Override public void setPlacedBy(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state, LivingEntity placer, @Nonnull ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack); if (!level.isClientSide) { CrateCreativeBlockEntity be = (CrateCreativeBlockEntity) level.getBlockEntity(pos); if (be != null) be.onBEPlaced(stack); }
    }
}

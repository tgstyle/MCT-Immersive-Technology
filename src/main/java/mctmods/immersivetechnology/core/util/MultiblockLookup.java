package mctmods.immersivetechnology.core.util;

import com.immersiveconvergence.api.multiblock.blocks.env.IMultiblockBEHelper;
import com.immersiveconvergence.api.multiblock.blocks.logic.IMultiblockBE;
import com.immersiveconvergence.api.multiblock.blocks.logic.IMultiblockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import javax.annotation.Nullable;

public final class MultiblockLookup {
    private MultiblockLookup() {}

    @Nullable public static IMultiblockBEHelper<?> helperAt(Level level, BlockPos pos) {
        if (!level.isLoaded(pos) || !(level.getBlockEntity(pos) instanceof IMultiblockBE<?> mbe)) { return null; }
        return mbe.getHelper();
    }

    @Nullable public static IMultiblockState stateAt(Level level, BlockPos pos) {
        IMultiblockBEHelper<?> helper = helperAt(level, pos);
        return helper != null ? helper.getState() : null;
    }
}

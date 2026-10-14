package mctmods.immersivetechnology.core.util;

import com.immersiveconvergence.api.multiblock.blocks.env.IMultiblockContext;
import net.minecraft.core.BlockPos;

public class RedstoneInput {
    public static boolean unpowered(IMultiblockContext<?> ctx, BlockPos pos) { return ctx.getRedstoneInputValue(pos, 0) <= 0; }
}

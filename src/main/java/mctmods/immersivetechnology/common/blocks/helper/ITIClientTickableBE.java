package mctmods.immersivetechnology.common.blocks.helper;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;

public interface ITIClientTickableBE extends ITITickableBase {
    void tickClient();

    static <T extends BlockEntity> BlockEntityTicker<T> makeTicker() { return (level, pos, state, blockEntity) -> { ITIClientTickableBE tickable = (ITIClientTickableBE)blockEntity; if (tickable.canTickAny()) { tickable.tickClient(); } }; }
}

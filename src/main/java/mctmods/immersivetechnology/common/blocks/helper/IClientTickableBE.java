package mctmods.immersivetechnology.common.blocks.helper;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;

public interface IClientTickableBE extends ITickableBase {
    void tickClient();

    static <T extends BlockEntity> BlockEntityTicker<T> makeTicker() { return (level, pos, state, blockEntity) -> { IClientTickableBE tickable = (IClientTickableBE)blockEntity; if (tickable.canTickAny()) { tickable.tickClient(); } }; }
}

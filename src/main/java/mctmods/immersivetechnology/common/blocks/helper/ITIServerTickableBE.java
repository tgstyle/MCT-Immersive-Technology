package mctmods.immersivetechnology.common.blocks.helper;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;

public interface ITIServerTickableBE extends ITITickableBase
{
    void tickServer();

    static <T extends BlockEntity>BlockEntityTicker<T> makeTicker() {
        return (level, pos, state, blockEntity) -> {
            ITIServerTickableBE tickable = (ITIServerTickableBE) blockEntity;
            if (tickable.canTickAny())
                tickable.tickServer();
        };
    }
}

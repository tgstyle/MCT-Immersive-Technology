package mctmods.immersivetechnology.common.blocks.helper;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;

public interface ITServerTickableBE extends ITTickableBase
{
    void tickServer();

    static <T extends BlockEntity>BlockEntityTicker<T> makeTicker() {
        return (level, pos, state, blockEntity) -> {
            ITServerTickableBE tickable = (ITServerTickableBE) blockEntity;
            if (tickable.canTickAny())
                tickable.tickServer();
        };
    }
}

package mctmods.immersivetechnology.common.blocks.helper;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;

public interface ITClientTickableBE extends ITTickableBase {
    void tickClient();

    static <T extends BlockEntity> BlockEntityTicker<T> makeTicker() {
        return (level, pos, state, blockEntity) -> {
            blusunrize.immersiveengineering.common.blocks.ticking.IEClientTickableBE tickable = (blusunrize.immersiveengineering.common.blocks.ticking.IEClientTickableBE)blockEntity;
            if (tickable.canTickAny()) {
                tickable.tickClient();
            }

        };
    }
}

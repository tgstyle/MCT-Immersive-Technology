package mctmods.immersivetechnology.common.blocks.metal;

import mctmods.immersivetechnology.common.blocks.helper.ITEntityBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.BiFunction;

public class CreativeBarrelBlock extends ITEntityBlock<CreativeBarrelBlockEntity> {
    public CreativeBarrelBlock(BiFunction<BlockPos, BlockState, CreativeBarrelBlockEntity> makeEntity, Properties blockProps) {
        super(makeEntity, blockProps);
    }
}

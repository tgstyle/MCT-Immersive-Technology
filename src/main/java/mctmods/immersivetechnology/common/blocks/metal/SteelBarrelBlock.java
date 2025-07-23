package mctmods.immersivetechnology.common.blocks.metal;

import mctmods.immersivetechnology.common.blocks.helper.ITEntityBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import java.util.function.BiFunction;

public class SteelBarrelBlock extends ITEntityBlock<SteelBarrelBlockEntity> {
    public SteelBarrelBlock(BiFunction<BlockPos, BlockState, SteelBarrelBlockEntity> makeEntity, BlockBehaviour.Properties blockProps) {
        super(makeEntity, Properties.of()
                .strength(3.0F, 20.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops());
    }
}
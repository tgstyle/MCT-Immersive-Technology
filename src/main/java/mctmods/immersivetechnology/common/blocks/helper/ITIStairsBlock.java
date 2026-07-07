package mctmods.immersivetechnology.common.blocks.helper;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Supplier;

public class ITIStairsBlock extends StairBlock implements ITIBlock {
    private final Supplier<? extends ITIBlock> base;

    public <T extends Block & ITIBlock> ITIStairsBlock(BlockBehaviour.Properties properties, Supplier<T> base) {
        super(() -> base.get().defaultBlockState(), properties);
        this.base = base;
    }

    public boolean hasFlavour() {
        return this.base.get().hasFlavour();
    }

    public String getNameForFlavour() {
        return this.base.get().getNameForFlavour();
    }
}

package mctmods.immersivetechnology.common.blocks.helper;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Supplier;

public class ITStairsBlock extends StairBlock implements IITBlock {
    private final Supplier<? extends IITBlock> base;

    public <T extends Block & IITBlock> ITStairsBlock(BlockBehaviour.Properties properties, Supplier<T> base) {
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

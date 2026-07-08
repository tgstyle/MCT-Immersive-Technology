package mctmods.immersivetechnology.common.blocks.helper;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Supplier;

public class ITIWallBlock extends WallBlock implements ITIBlock {
    private final Supplier<? extends ITIBlock> base;

    public <T extends Block & ITIBlock> ITIWallBlock(BlockBehaviour.Properties properties, Supplier<T> base) {
        super(properties);
        this.base = base;
    }

    public boolean hasFlavour() {
        return this.base.get().hasFlavour();
    }

    public String getNameForFlavour() {
        return this.base.get().getNameForFlavour();
    }
}

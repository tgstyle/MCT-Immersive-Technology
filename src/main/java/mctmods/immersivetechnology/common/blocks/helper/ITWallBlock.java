package mctmods.immersivetechnology.common.blocks.helper;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Supplier;

public class ITWallBlock extends WallBlock implements IITBlock {
    private final Supplier<? extends IITBlock> base;

    public <T extends Block & IITBlock> ITWallBlock(BlockBehaviour.Properties properties, Supplier<T> base) {
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

package mctmods.immersivetechnology.common.multiblocks.helper;

import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import com.google.common.base.Preconditions;
import mctmods.immersivetechnology.common.blocks.helper.ITProperties;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.jetbrains.annotations.NotNull;

public class ITMultiblockPartBlockWithMirror<S extends IMultiblockState> extends ITMultiblockPartBlock<S> {
    public ITMultiblockPartBlockWithMirror(BlockBehaviour.Properties properties, MultiblockRegistration<S> multiblock) {
        super(properties, multiblock);
        Preconditions.checkState(multiblock.mirrorable());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ITProperties.MIRRORED);
    }
}

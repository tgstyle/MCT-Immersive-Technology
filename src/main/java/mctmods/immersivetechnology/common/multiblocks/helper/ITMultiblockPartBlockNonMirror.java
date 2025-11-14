package mctmods.immersivetechnology.common.multiblocks.helper;

import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.jetbrains.annotations.NotNull;

public class ITMultiblockPartBlockNonMirror<S extends IMultiblockState> extends ITMultiblockPartBlock<S> {
    public ITMultiblockPartBlockNonMirror(BlockBehaviour.Properties properties, MultiblockRegistration<S> registration) { super(properties, registration); }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder) { super.createBlockStateDefinition(builder); }
}

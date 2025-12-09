package mctmods.immersivetechnology.common.multiblocks.helper;

import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import com.google.common.base.Preconditions;
import mctmods.immersivetechnology.common.blocks.helper.ITProperties;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;

import javax.annotation.Nonnull;

public class ITNonMirrorableWithActiveBlock<S extends IMultiblockState> extends ITMultiblockPartBlock<S> {
    public static final Property<Boolean> ACTIVE;

    public ITNonMirrorableWithActiveBlock(BlockBehaviour.Properties properties, MultiblockRegistration<S> multiblock) {
        super(properties, multiblock);
        Preconditions.checkState(!multiblock.mirrorable());
    }

    protected void createBlockStateDefinition(@Nonnull StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ACTIVE);
    }

    static { ACTIVE = ITProperties.ACTIVE; }
}

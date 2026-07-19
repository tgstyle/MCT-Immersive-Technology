package mctmods.immersivetechnology.common.multiblocks.helper;

import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import mctmods.immersivetechnology.common.blocks.helper.ModProperties;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.NotNull;

public class MultiblockPartBlockNonMirrorActive<S extends IMultiblockState> extends MultiblockPartBlockNonMirror<S> {
    public static final Property<Boolean> ACTIVE = ModProperties.ACTIVE;

    public MultiblockPartBlockNonMirrorActive(BlockBehaviour.Properties properties, MultiblockRegistration<S> registration) {
        super(properties, registration);
    }

    @Override protected void createBlockStateDefinition(@NotNull StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ACTIVE);
    }
}

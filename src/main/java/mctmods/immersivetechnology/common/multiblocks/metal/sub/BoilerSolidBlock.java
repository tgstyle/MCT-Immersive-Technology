package mctmods.immersivetechnology.common.multiblocks.metal.sub;

import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration;
import mctmods.immersivetechnology.common.blocks.helper.ITProperties;
import mctmods.immersivetechnology.common.multiblocks.helper.ITMultiblockPartBlockWithMirror;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.BoilerSolidLogic;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.jetbrains.annotations.NotNull;

public class BoilerSolidBlock extends ITMultiblockPartBlockWithMirror<BoilerSolidLogic.State> {
    public BoilerSolidBlock(MultiblockRegistration<BoilerSolidLogic.State> registration) { super(blusunrize.immersiveengineering.common.register.IEBlocks.METAL_PROPERTIES_NO_OCCLUSION.get(), registration); }

    @Override protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder) { super.createBlockStateDefinition(builder); builder.add(ITProperties.ACTIVE); }
}

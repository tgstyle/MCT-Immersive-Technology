package mctmods.immersivetechnology.common.blocks.metal;

import mctmods.immersivetechnology.common.blocks.helper.ITIEntityBlock;
import mctmods.immersivetechnology.common.blocks.helper.ITEnums.IOSideConfig;
import mctmods.immersivetechnology.common.blocks.metal.logic.BarrelOpenIBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.NotNull;
import java.util.function.BiFunction;

public class BarrelOpenBlock extends ITIEntityBlock<BarrelOpenIBlockEntity> {
    public static final EnumProperty<IOSideConfig> BOTTOM_CONFIG = EnumProperty.create("bottom_config", IOSideConfig.class);

    public BarrelOpenBlock(BiFunction<BlockPos, BlockState, BarrelOpenIBlockEntity> makeEntity, BlockBehaviour.Properties blockProps) {
        super(makeEntity, blockProps);
        registerDefaultState(stateDefinition.any().setValue(BOTTOM_CONFIG, IOSideConfig.OUTPUT));
    }

    @Override protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(BOTTOM_CONFIG);
    }

    @SuppressWarnings("deprecation")
    @Override @NotNull public RenderShape getRenderShape(@NotNull BlockState state) { return RenderShape.MODEL; }
}

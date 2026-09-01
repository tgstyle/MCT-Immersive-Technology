package mctmods.immersivetechnology.common.blocks.metal;

import com.immersiveconvergence.api.block.ModEntityBlock;
import com.immersiveconvergence.api.block.Enums.IOSideConfig;
import mctmods.immersivetechnology.common.blocks.metal.logic.BarrelOpenBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import java.util.function.BiFunction;
import com.immersiveconvergence.api.block.Enums;

import javax.annotation.Nonnull;

public class BarrelOpenBlock extends ModEntityBlock<BarrelOpenBlockEntity> {
    public static final EnumProperty<IOSideConfig> BOTTOM_CONFIG = EnumProperty.create("bottom_config", IOSideConfig.class);

    public BarrelOpenBlock(BiFunction<BlockPos, BlockState, BarrelOpenBlockEntity> makeEntity, BlockBehaviour.Properties blockProps) {
        super(makeEntity, blockProps);
        registerDefaultState(stateDefinition.any().setValue(BOTTOM_CONFIG, IOSideConfig.OUTPUT));
    }

    @Override protected void createBlockStateDefinition(@Nonnull StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(BOTTOM_CONFIG);
    }

    @SuppressWarnings("deprecation")
    @Override @Nonnull public RenderShape getRenderShape(@Nonnull BlockState state) { return RenderShape.MODEL; }
}

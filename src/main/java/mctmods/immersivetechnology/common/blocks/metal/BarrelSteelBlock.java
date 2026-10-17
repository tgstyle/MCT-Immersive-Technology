package mctmods.immersivetechnology.common.blocks.metal;

import java.util.Map;
import java.util.EnumMap;
import net.minecraft.core.Direction;
import com.immersiveconvergence.api.block.IStateSideConfig;
import com.immersiveconvergence.api.block.ModEntityBlock;
import com.immersiveconvergence.api.block.Enums.IOSideConfig;
import mctmods.immersivetechnology.common.blocks.metal.logic.BarrelSteelBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import java.util.function.BiFunction;

import javax.annotation.Nonnull;

public class BarrelSteelBlock extends ModEntityBlock<BarrelSteelBlockEntity> implements IStateSideConfig {
    public static final EnumProperty<IOSideConfig> TOP_CONFIG = EnumProperty.create("top_config", IOSideConfig.class);
    public static final EnumProperty<IOSideConfig> BOTTOM_CONFIG = EnumProperty.create("bottom_config", IOSideConfig.class);

    public BarrelSteelBlock(BiFunction<BlockPos, BlockState, BarrelSteelBlockEntity> makeEntity, BlockBehaviour.Properties blockProps) {
        super(makeEntity, blockProps);
        registerDefaultState(stateDefinition.any().setValue(TOP_CONFIG, IOSideConfig.INPUT).setValue(BOTTOM_CONFIG, IOSideConfig.OUTPUT));
    }

    @Override protected void createBlockStateDefinition(@Nonnull StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(TOP_CONFIG, BOTTOM_CONFIG);
    }

    @SuppressWarnings("deprecation")
    @Override @Nonnull public RenderShape getRenderShape(@Nonnull BlockState state) { return RenderShape.MODEL; }

    @Override public Map<Direction, IOSideConfig> getStateSideConfig(BlockState state) {
        Map<Direction, IOSideConfig> config = new EnumMap<>(Direction.class);
        config.put(Direction.UP, state.hasProperty(TOP_CONFIG) ? state.getValue(TOP_CONFIG) : IOSideConfig.INPUT);
        config.put(Direction.DOWN, state.hasProperty(BOTTOM_CONFIG) ? state.getValue(BOTTOM_CONFIG) : IOSideConfig.OUTPUT);
        return config;
    }
}

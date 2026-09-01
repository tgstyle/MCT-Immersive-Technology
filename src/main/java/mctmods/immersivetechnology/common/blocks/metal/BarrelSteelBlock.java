package mctmods.immersivetechnology.common.blocks.metal;

import com.immersiveconvergence.api.block.ModEntityBlock;
import com.immersiveconvergence.api.block.Enums.IOSideConfig;
import mctmods.immersivetechnology.common.blocks.metal.logic.BarrelSteelBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.NotNull;
import java.util.function.BiFunction;
import com.immersiveconvergence.api.block.Enums;

public class BarrelSteelBlock extends ModEntityBlock<BarrelSteelBlockEntity> {
    public static final EnumProperty<IOSideConfig> TOP_CONFIG = EnumProperty.create("top_config", IOSideConfig.class);
    public static final EnumProperty<IOSideConfig> BOTTOM_CONFIG = EnumProperty.create("bottom_config", IOSideConfig.class);

    @SuppressWarnings("unused")
    public BarrelSteelBlock(BiFunction<BlockPos, BlockState, BarrelSteelBlockEntity> makeEntity, BlockBehaviour.Properties blockProps) {
        super(makeEntity, Properties.of().mapColor(MapColor.METAL).strength(3.0F, 20.0F).sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion());
        registerDefaultState(stateDefinition.any().setValue(TOP_CONFIG, IOSideConfig.INPUT).setValue(BOTTOM_CONFIG, IOSideConfig.OUTPUT));
    }

    @Override protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(TOP_CONFIG, BOTTOM_CONFIG);
    }

    @SuppressWarnings("deprecation")
    @Override @NotNull public RenderShape getRenderShape(@NotNull BlockState state) { return RenderShape.MODEL; }
}

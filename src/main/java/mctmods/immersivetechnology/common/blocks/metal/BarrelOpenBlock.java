package mctmods.immersivetechnology.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEEnums.IOSideConfig;
import mctmods.immersivetechnology.common.blocks.helper.ITEntityBlock;
import mctmods.immersivetechnology.common.blocks.metal.logic.BarrelOpenBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.NotNull;
import java.util.function.BiFunction;

public class BarrelOpenBlock extends ITEntityBlock<BarrelOpenBlockEntity> {
    public static final EnumProperty<IOSideConfig> BOTTOM_CONFIG = EnumProperty.create("bottom_config", IOSideConfig.class);

    @SuppressWarnings("unused")
    public BarrelOpenBlock(BiFunction<BlockPos, BlockState, BarrelOpenBlockEntity> makeEntity, BlockBehaviour.Properties blockProps) {
        super(makeEntity, Properties.of().mapColor(MapColor.METAL).strength(3.0F, 20.0F).sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion());
        registerDefaultState(stateDefinition.any().setValue(BOTTOM_CONFIG, IOSideConfig.OUTPUT));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(BOTTOM_CONFIG);
    }

    @Override
    @SuppressWarnings("deprecation")
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) { return RenderShape.ENTITYBLOCK_ANIMATED; }
}

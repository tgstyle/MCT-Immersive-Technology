package mctmods.immersivetechnology.common.blocks.metal;

import mctmods.immersivetechnology.common.blocks.helper.ITEntityBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.jetbrains.annotations.NotNull;
import java.util.function.BiFunction;

public class OpenBarrelBlock extends ITEntityBlock<OpenBarrelBlockEntity> {
    public OpenBarrelBlock(BiFunction<BlockPos, BlockState, OpenBarrelBlockEntity> makeEntity, BlockBehaviour.Properties blockProps) {
        super(makeEntity, Properties.of().mapColor(MapColor.METAL).instrument(NoteBlockInstrument.IRON_XYLOPHONE).strength(3.0F, 20.0F).sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion());
    }

    @Override
    @SuppressWarnings("deprecation")
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) { return RenderShape.MODEL; }
}

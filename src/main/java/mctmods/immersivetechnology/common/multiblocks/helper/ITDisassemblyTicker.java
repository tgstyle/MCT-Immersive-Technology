package mctmods.immersivetechnology.common.multiblocks.helper;

import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IMultiblockComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.AbstractMap;

public record ITDisassemblyTicker<S extends IMultiblockState>(BlockPos masterRel) implements IServerTickableComponent<S>, IMultiblockComponent.StateWrapper<S, Void> {
    @Override
    public void tickServer(IMultiblockContext<S> context) {
        Level level = context.getLevel().getRawLevel();
        BlockPos masterAbs = context.getLevel().toAbsolute(masterRel);
        BlockEntity be = level.getBlockEntity(masterAbs);
        if (be instanceof ITMultiblockBlockEntityMaster<?> master && master.disassembleQueue != null && !master.disassembleQueue.isEmpty()) {
            int blocksPerTick = ITTemplateMultiblock.DISASSEMBLE_QUEUE_SIZE;
            for (int i = 0; i < blocksPerTick && !master.disassembleQueue.isEmpty(); ++i) {
                AbstractMap.SimpleEntry<BlockPos, BlockState> entry = master.disassembleQueue.remove(0);
                BlockPos breakPos = entry.getKey();
                BlockState template = entry.getValue();
                level.setBlock(breakPos, template, 3);
                level.removeBlock(breakPos, false);
            }
            if (master.disassembleQueue.isEmpty()) { master.disassembleQueue = null; }
        }
    }

    @Override
    public Void wrapState(S state) { return null; }
}

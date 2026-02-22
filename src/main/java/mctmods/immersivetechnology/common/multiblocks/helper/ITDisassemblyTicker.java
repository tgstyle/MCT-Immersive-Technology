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

public class ITDisassemblyTicker<S extends IMultiblockState> implements IServerTickableComponent<S>, IMultiblockComponent.StateWrapper<S, Void> {

    private final BlockPos masterRel;

    private ITMultiblockBlockEntityMaster<?> cachedMaster = null;

    public ITDisassemblyTicker(BlockPos masterRel) {
        this.masterRel = masterRel;
    }

    @Override public void tickServer(IMultiblockContext<S> context) {
        Level level = context.getLevel().getRawLevel();
        BlockPos masterAbs = context.getLevel().toAbsolute(masterRel);

        if (cachedMaster == null) {
            BlockEntity be = level.getBlockEntity(masterAbs);
            if (be instanceof ITMultiblockBlockEntityMaster<?> master) { cachedMaster = master; }
            else { return; }
        }

        if (cachedMaster.disassembleQueue != null && !cachedMaster.disassembleQueue.isEmpty()) {
            int blocksPerTick = ITTemplateMultiblock.DISASSEMBLE_QUEUE_SIZE;

            for (int i = 0; i < blocksPerTick && !cachedMaster.disassembleQueue.isEmpty(); ++i) {
                AbstractMap.SimpleEntry<BlockPos, BlockState> entry = cachedMaster.disassembleQueue.remove(0);
                BlockPos breakPos = entry.getKey();
                BlockState template = entry.getValue();
                level.setBlock(breakPos, template, 3);
                level.removeBlock(breakPos, false);
            }

            if (cachedMaster.disassembleQueue.isEmpty()) { cachedMaster.disassembleQueue = null; }
        }
    }

    @Override public Void wrapState(S state) { return null; }
}

package mctmods.immersivetechnology.common.multiblocks.helper;

import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import com.google.common.base.Preconditions;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class MultiblockPartBlockNonMirror<S extends IMultiblockState> extends ModMultiblockPartBlock<S> {
    public MultiblockPartBlockNonMirror(BlockBehaviour.Properties properties, MultiblockRegistration<S> registration) {
        super(properties, registration);
        Preconditions.checkState(!registration.mirrorable());
    }
}

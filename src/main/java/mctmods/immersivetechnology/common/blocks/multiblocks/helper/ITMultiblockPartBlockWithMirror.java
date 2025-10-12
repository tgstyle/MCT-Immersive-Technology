package mctmods.immersivetechnology.common.blocks.multiblocks.helper;

import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;

public class ITMultiblockPartBlockWithMirror<S extends IMultiblockState> extends ITMultiblockPartBlock.WithMirrorState<S> {
    public ITMultiblockPartBlockWithMirror(Properties properties, MultiblockRegistration<S> multiblock) { super(properties, multiblock); }
}

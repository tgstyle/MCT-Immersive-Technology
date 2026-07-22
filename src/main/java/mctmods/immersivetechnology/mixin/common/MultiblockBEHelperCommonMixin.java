package mctmods.immersivetechnology.mixin.common;

import blusunrize.immersiveengineering.common.blocks.multiblocks.blockimpl.MultiblockBEHelperCommon;
import mctmods.immersivetechnology.common.multiblocks.helper.IDisassemblingAware;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MultiblockBEHelperCommon.class)
public abstract class MultiblockBEHelperCommonMixin implements IDisassemblingAware {

    @Shadow(remap = false)
    private boolean beingDisassembled;

    @Override
    public boolean it$isAssembled() { return !beingDisassembled; }

    @Override
    public boolean it$isDisassembling() { return beingDisassembled; }

    @Override
    public void it$markDisassembling() { beingDisassembled = true; }
}

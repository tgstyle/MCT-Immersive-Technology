package mctmods.immersivetechnology.mixin.common;

import blusunrize.immersiveengineering.common.blocks.multiblocks.blockimpl.MultiblockBEHelperCommon;
import mctmods.immersivetechnology.common.multiblocks.helper.ModIMultiblockBEHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MultiblockBEHelperCommon.class)
public abstract class MultiblockBEHelperMixin implements ModIMultiblockBEHelper {

    @Shadow(remap = false)
    private boolean beingDisassembled;

    @Override
    public boolean it$isDisassembling() { return beingDisassembled; }

    @Override
    public void it$markDisassembling() { beingDisassembled = true; }
}

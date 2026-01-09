package mctmods.immersivetechnology.mixin.common;

import blusunrize.immersiveengineering.common.blocks.multiblocks.blockimpl.MultiblockBEHelperCommon;
import mctmods.immersivetechnology.common.multiblocks.helper.ITMultiblockBEHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;

@Mixin(MultiblockBEHelperCommon.class)
@Implements(@Interface(iface = ITMultiblockBEHelper.class, prefix = "it$"))
public abstract class MultiblockBEHelperMixin {
    @Shadow(remap = false)
    private boolean beingDisassembled;

    public boolean it$isAssembled() { return !this.beingDisassembled; }
}

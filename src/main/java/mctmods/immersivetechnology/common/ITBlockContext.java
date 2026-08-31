package mctmods.immersivetechnology.common;

import com.immersiveconvergence.api.block.BlockContext;

import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.common.util.ITLogger;

public class ITBlockContext {
    public static final BlockContext CONTEXT = new BlockContext(ImmersiveTechnology.MODID, ImmersiveTechnology.creativeTab, ITContent.registeredITBlocks, ITContent.registeredITItems, ITLogger.logger, CommonProxy::openGuiForTile);
}

package mctmods.immersivetechnology.common;

import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.common.util.ITLogger;
import mctmods.immersivetechnology.core.proxy.CommonProxy;
import mctmods.immersivetechnology.core.registration.ITCreativeTab;

import com.immersiveconvergence.api.block.BlockContext;

public class ITBlockContext {
    public static final BlockContext CONTEXT = new BlockContext(ImmersiveTechnology.MODID, ITCreativeTab.tab, ITContent.registeredITBlocks, ITContent.registeredITItems, ITLogger.logger, CommonProxy::openGuiForTile);
}

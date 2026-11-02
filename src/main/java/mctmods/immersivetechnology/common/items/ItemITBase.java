package mctmods.immersivetechnology.common.items;

import mctmods.immersivetechnology.common.ITBlockContext;

import com.immersiveconvergence.api.block.ICItemBase;

public class ItemITBase extends ICItemBase {
    public ItemITBase(String name, int stackSize, String... subNames) { super(ITBlockContext.CONTEXT, name, stackSize, subNames); }
}

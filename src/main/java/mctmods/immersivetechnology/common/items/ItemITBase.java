package mctmods.immersivetechnology.common.items;

import com.immersiveconvergence.api.block.ICItemBase;

import mctmods.immersivetechnology.common.ITBlockContext;

public class ItemITBase extends ICItemBase {
    public ItemITBase(String name, int stackSize, String... subNames) { super(ITBlockContext.CONTEXT, name, stackSize, subNames); }
}

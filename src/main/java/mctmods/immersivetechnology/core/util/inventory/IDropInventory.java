package mctmods.immersivetechnology.core.util.inventory;

import java.util.stream.Stream;
import net.minecraft.world.item.ItemStack;

public interface IDropInventory { Stream<ItemStack> getDroppedItems(); }

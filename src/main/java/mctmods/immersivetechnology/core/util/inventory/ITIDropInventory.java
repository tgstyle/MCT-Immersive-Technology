package mctmods.immersivetechnology.core.util.inventory;

import java.util.stream.Stream;
import net.minecraft.world.item.ItemStack;

public interface ITIDropInventory { Stream<ItemStack> getDroppedItems(); }

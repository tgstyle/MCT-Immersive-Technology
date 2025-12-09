package mctmods.immersivetechnology.common.util.inventory;

import java.util.stream.Stream;
import net.minecraft.world.item.ItemStack;

public interface IITDropInventory { Stream<ItemStack> getDroppedItems(); }

package mctmods.immersivetechnology.common.items.helper;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class ITBaseItem extends Item {

    public ITBaseItem(Properties props) { super(props); }

    @Override public int getBurnTime(ItemStack itemStack, RecipeType<?> type) { return 0; }

    @Override public boolean isRepairable(@Nonnull ItemStack stack) { return false; }

    @Override public boolean isBookEnchantable(ItemStack stack, ItemStack book) { return false; }

    public boolean canEquip(ItemStack stack, EquipmentSlot armorType, Entity entity) { return Mob.getEquipmentSlotForItem(stack) == armorType || getEquipmentSlot(stack) == armorType; }

    @Override public int getBarColor(@NotNull ItemStack pStack) { return Mth.hsvToRgb(Math.max(0.0F, getBarWidth(pStack) / (float) MAX_BAR_WIDTH) / 3.0F, 1.0F, 1.0F); }
}

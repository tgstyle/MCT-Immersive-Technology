package mctmods.immersivetechnology.common.items.helper;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class BaseItem extends Item {

    public BaseItem(Properties props) { super(props); }

    @Override public int getBurnTime(@NotNull ItemStack itemStack, RecipeType<?> type) { return 0; }

    @Override public boolean isRepairable(@Nonnull ItemStack stack) { return false; }

    @Override public boolean isBookEnchantable(@NotNull ItemStack stack, @NotNull ItemStack book) { return false; }

    @Override
    public boolean canEquip(@NotNull ItemStack stack, @NotNull EquipmentSlot armorType, LivingEntity entity) {
        return entity.getEquipmentSlotForItem(stack) == armorType || getEquipmentSlot(stack) == armorType;
    }

    @Override public int getBarColor(@NotNull ItemStack pStack) { return Mth.hsvToRgb(Math.max(0.0F, getBarWidth(pStack) / (float) MAX_BAR_WIDTH) / 3.0F, 1.0F, 1.0F); }
}

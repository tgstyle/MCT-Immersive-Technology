package mctmods.immersivetechnology.common.util.compat.jei;

import mezz.jei.api.ingredients.IIngredientHelper;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class GenericMultiblockHelper implements IIngredientHelper<GenericMultiblockIngredient> {

    @Override @Nullable public GenericMultiblockIngredient getMatch(@Nonnull Iterable<GenericMultiblockIngredient> iterable, @Nonnull GenericMultiblockIngredient genericMultiblockIngredient) {
        for (GenericMultiblockIngredient ing : iterable) {
            if (ing.renderStack.isItemEqual(genericMultiblockIngredient.renderStack)) { return ing; }
        }
        return null;
    }

    @Override @Nonnull public String getDisplayName(@Nonnull GenericMultiblockIngredient genericMultiblockIngredient) { return genericMultiblockIngredient.renderStack.getDisplayName(); }

    @Override @Nonnull public String getUniqueId(@Nonnull GenericMultiblockIngredient genericMultiblockIngredient) { return genericMultiblockIngredient.renderStack.getTranslationKey() + genericMultiblockIngredient.renderStack.getMetadata(); }

    @Override @Nonnull public String getWildcardId(@Nonnull GenericMultiblockIngredient genericMultiblockIngredient) { return genericMultiblockIngredient.renderStack.getTranslationKey() + genericMultiblockIngredient.renderStack.getMetadata(); }

    @Override @Nonnull public String getModId(@Nonnull GenericMultiblockIngredient genericMultiblockIngredient) { return "immersivetech"; }

    @Override @Nonnull public String getResourceId(@Nonnull GenericMultiblockIngredient genericMultiblockIngredient) { return Objects.requireNonNull(genericMultiblockIngredient.renderStack.getItem().getRegistryName()).toString(); }

    @Override @Nonnull public ItemStack getCheatItemStack(@Nonnull GenericMultiblockIngredient ingredient) { return ItemStack.EMPTY; }

    @Override @Nonnull public GenericMultiblockIngredient copyIngredient(@Nonnull GenericMultiblockIngredient genericMultiblockIngredient) { return genericMultiblockIngredient; }

    @Override @Nonnull public String getErrorInfo(@Nullable GenericMultiblockIngredient genericMultiblockIngredient) {
        return (genericMultiblockIngredient == null) ? "genericMultiblockIngredient is not supposed to be null!" : "";
    }
}

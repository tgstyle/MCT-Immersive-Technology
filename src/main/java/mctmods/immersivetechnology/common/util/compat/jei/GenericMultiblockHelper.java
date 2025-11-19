package mctmods.immersivetechnology.common.util.compat.jei;

import mezz.jei.api.ingredients.IIngredientHelper;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class GenericMultiblockHelper implements IIngredientHelper<GenericMultiblockIngredient> {

    @Nullable
    @Override
    public GenericMultiblockIngredient getMatch(@Nonnull Iterable<GenericMultiblockIngredient> iterable, @Nonnull GenericMultiblockIngredient genericMultiblockIngredient) {
        for (GenericMultiblockIngredient ing : iterable) {
            if (ing.renderStack.isItemEqual(genericMultiblockIngredient.renderStack)) return ing;
        }
        return null;
    }

    @Override
    public @Nonnull String getDisplayName(@Nonnull GenericMultiblockIngredient genericMultiblockIngredient) {
        return genericMultiblockIngredient.renderStack.getDisplayName();
    }

    @Override
    public @Nonnull String getUniqueId(@Nonnull GenericMultiblockIngredient genericMultiblockIngredient) {
        return genericMultiblockIngredient.renderStack.getTranslationKey() + genericMultiblockIngredient.renderStack.getMetadata();
    }

    @Override
    public @Nonnull String getWildcardId(@Nonnull GenericMultiblockIngredient genericMultiblockIngredient) {
        return genericMultiblockIngredient.renderStack.getTranslationKey() + genericMultiblockIngredient.renderStack.getMetadata();
    }

    @Override
    public @Nonnull String getModId(@Nonnull GenericMultiblockIngredient genericMultiblockIngredient) {
        return "immersivetech";
    }

    @Override
    public @Nonnull String getResourceId(@Nonnull GenericMultiblockIngredient genericMultiblockIngredient) {
        return Objects.requireNonNull(genericMultiblockIngredient.renderStack.getItem().getRegistryName()).toString();
    }

    @Override
    public @Nonnull ItemStack getCheatItemStack(@Nonnull GenericMultiblockIngredient ingredient) {
        return ItemStack.EMPTY;
    }

    @Override
    public @Nonnull GenericMultiblockIngredient copyIngredient(@Nonnull GenericMultiblockIngredient genericMultiblockIngredient) {
        return genericMultiblockIngredient;
    }

    @Override
    public @Nonnull String getErrorInfo(@Nullable GenericMultiblockIngredient genericMultiblockIngredient) {
        return (genericMultiblockIngredient == null)? "genericMultiblockIngredient is not supposed to be null!" : "";
    }
}

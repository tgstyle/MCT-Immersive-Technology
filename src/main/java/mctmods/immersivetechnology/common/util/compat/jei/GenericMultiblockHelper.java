package mctmods.immersivetechnology.common.util.compat.jei;

import mezz.jei.api.ingredients.IIngredientHelper;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

public class GenericMultiblockHelper implements IIngredientHelper<GenericMultiblockIngredient> {

    @Nullable
    @Override
    public GenericMultiblockIngredient getMatch(Iterable<GenericMultiblockIngredient> iterable, GenericMultiblockIngredient genericMultiblockIngredient) {
        for(GenericMultiblockIngredient ing : iterable) {
            if(ing.renderStack.isItemEqual(genericMultiblockIngredient.renderStack)) return ing;
        }
        return null;
    }

    @Override
    public String getDisplayName(GenericMultiblockIngredient genericMultiblockIngredient) {
        return genericMultiblockIngredient.renderStack.getDisplayName();
    }

    @Override
    public String getUniqueId(GenericMultiblockIngredient genericMultiblockIngredient) {
        return genericMultiblockIngredient.renderStack.getUnlocalizedName() + genericMultiblockIngredient.renderStack.getMetadata();
    }

    @Override
    public String getWildcardId(GenericMultiblockIngredient genericMultiblockIngredient) {
        return genericMultiblockIngredient.renderStack.getUnlocalizedName() + genericMultiblockIngredient.renderStack.getMetadata();
    }

    @Override
    public String getModId(GenericMultiblockIngredient genericMultiblockIngredient) {
        return "immersivetech";
    }

    @Override
    public String getResourceId(GenericMultiblockIngredient genericMultiblockIngredient) {
        return genericMultiblockIngredient.renderStack.getItem().getRegistryName().toString();
    }

    @Override
    public ItemStack getCheatItemStack(GenericMultiblockIngredient ingredient) {
        return ItemStack.EMPTY;
    }

    @Override
    public GenericMultiblockIngredient copyIngredient(GenericMultiblockIngredient genericMultiblockIngredient) {
        return genericMultiblockIngredient;
    }

    @Override
    public String getErrorInfo(@Nullable GenericMultiblockIngredient genericMultiblockIngredient) {
        return (genericMultiblockIngredient == null)? "genericMultiblockIngredient is not supposed to be null!" : "";
    }

}
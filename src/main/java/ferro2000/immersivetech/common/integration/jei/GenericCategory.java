package ferro2000.immersivetech.common.integration.jei;

import ferro2000.immersivetech.ImmersiveTech;
import ferro2000.immersivetech.api.crafting.GenericRecipe;
import mezz.jei.api.gui.*;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.IRecipeWrapperFactory;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public abstract class GenericCategory<T extends GenericRecipe, W extends IRecipeWrapper> implements IRecipeCategory<W>, IRecipeWrapperFactory<T> {

    public String UID;
    public String name;
    public IDrawable background;
    protected ResourceLocation backgroundImage;

    public GenericCategory(String UID, String name, String backgroundLocation) {
        this.UID = UID;
        this.name = name;
        this.backgroundImage = new ResourceLocation(backgroundLocation);
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public String getUid() {
        return UID;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public String getTitle() {
        return I18n.format(name);
    }

    @Override
    public String getModName() {
        return ImmersiveTech.NAME;
    }

    @Nullable
    @Override
    public IDrawable getIcon() {
        return null;
    }
}
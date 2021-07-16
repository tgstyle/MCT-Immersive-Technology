package mctmods.immersivetechnology.common.util.compat.crafttweaker;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.liquid.ILiquidStack;
import mctmods.immersivetechnology.api.crafting.DistillerRecipe;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.ArrayList;
import java.util.Iterator;

@ZenClass("mods.immersivetechnology.Distiller")
public class Distiller {
	
	@ZenMethod
	public static void addRecipe(ILiquidStack outputFluid, ILiquidStack inputFluid, IItemStack outputItem, Integer energy, Integer time, Float chance) {
		FluidStack fluidOut = CraftTweakerHelper.toFluidStack(outputFluid);
		FluidStack fluidIn = CraftTweakerHelper.toFluidStack(inputFluid);
		ItemStack itemOut = CraftTweakerHelper.toStack(outputItem);

		if(fluidIn == null || fluidOut == null || itemOut.isEmpty()) return;

		if(energy == null) energy = 10000;
		if(time == null) time = 20;
		if(chance == null) chance = 0.01F;

		DistillerRecipe recipe = new DistillerRecipe(fluidOut, fluidIn, itemOut, energy, time, chance);
		CraftTweakerAPI.apply(new Add(recipe));
	}

	@ZenMethod
	public static void addRecipe(ILiquidStack outputFluid, ILiquidStack inputFluid, IItemStack outputItem) {
		FluidStack fluidOut = CraftTweakerHelper.toFluidStack(outputFluid);
		FluidStack fluidIn = CraftTweakerHelper.toFluidStack(inputFluid);
		ItemStack itemOut = CraftTweakerHelper.toStack(outputItem);

		if(fluidIn == null || fluidOut == null || itemOut.isEmpty()) return;

		int energy = 10000;
		int time = 20;
		float chance = 0.01F;

		DistillerRecipe recipe = new DistillerRecipe(fluidOut, fluidIn, itemOut, energy, time, chance);
		CraftTweakerAPI.apply(new Add(recipe));
	}

	private static class Add implements IAction {
		public DistillerRecipe recipe;
		public Add(DistillerRecipe recipe) {
			this.recipe = recipe;
		}

		@Override
		public void apply() {
			DistillerRecipe.recipeList.add(recipe);
		}

		@Override
		public String describe() {
			return "Adding Distiller Recipe for " + recipe.fluidInput.getLocalizedName() + " -> " + recipe.fluidOutput.getLocalizedName();
		}
	}

	@ZenMethod
	public static void removeRecipe(ILiquidStack inputFluid, @Optional IItemStack outputItem) {
		if(CraftTweakerHelper.toFluidStack(inputFluid) != null) CraftTweakerAPI.apply(new Remove(CraftTweakerHelper.toFluidStack(inputFluid), CraftTweakerHelper.toStack(outputItem)));
	}

	private static class Remove implements IAction {
		private final FluidStack inputFluid;
		private final ItemStack outputItem;
		ArrayList<DistillerRecipe> removedRecipes = new ArrayList<>();

		public Remove(FluidStack inputFluid, ItemStack outputItem) {
			this.inputFluid = inputFluid;
			this.outputItem = outputItem;
		}

		@Override
		public void apply() {
			Iterator<DistillerRecipe> iterator = DistillerRecipe.recipeList.iterator();
			while(iterator.hasNext()) {
				DistillerRecipe recipe = iterator.next();
				if(recipe != null && recipe.fluidInput.isFluidEqual(inputFluid)) {
					removedRecipes.add(recipe);
					iterator.remove();
				}
			}
		}

		@Override
		public String describe() {
			if(this.outputItem.getItem() == Items.AIR) return "Removing Distiller Input Recipe for " + inputFluid.getLocalizedName();
			return "Removing Distiller Input Recipe for " + inputFluid.getLocalizedName() + " -> " + outputItem.getDisplayName();
		}
	}

}
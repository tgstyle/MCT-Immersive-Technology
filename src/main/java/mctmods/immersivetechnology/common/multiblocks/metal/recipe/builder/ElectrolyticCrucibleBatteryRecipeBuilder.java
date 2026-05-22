package mctmods.immersivetechnology.common.multiblocks.metal.recipe.builder;

import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.builders.IEFinishedRecipe;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.ElectrolyticCrucibleBatteryRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public class ElectrolyticCrucibleBatteryRecipeBuilder extends IEFinishedRecipe<ElectrolyticCrucibleBatteryRecipeBuilder> {
    public ElectrolyticCrucibleBatteryRecipeBuilder() {
        super(ElectrolyticCrucibleBatteryRecipe.SERIALIZER.get());
        this.maxInputCount = 1;
    }

    public static ElectrolyticCrucibleBatteryRecipeBuilder builder(FluidTagInput fluidIn, FluidStack fluidOut0, int energy, int time) {
        return new ElectrolyticCrucibleBatteryRecipeBuilder()
                .addFluidTag("input", fluidIn)
                .addFluid("result0", fluidOut0)
                .setEnergy(energy)
                .setTime(time);
    }

    @SuppressWarnings("unused")
    public ElectrolyticCrucibleBatteryRecipeBuilder addFluidOutput1(FluidStack out) {
        return this.addFluid("result1", out);
    }

    @SuppressWarnings("unused")
    public ElectrolyticCrucibleBatteryRecipeBuilder addFluidOutput2(FluidStack out) {
        return this.addFluid("result2", out);
    }

    @SuppressWarnings("unused")
    public ElectrolyticCrucibleBatteryRecipeBuilder addItemOutput(ItemStack item) {
        return this.addItem("item_output", item);
    }
}

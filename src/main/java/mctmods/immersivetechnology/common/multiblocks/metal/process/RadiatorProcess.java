package mctmods.immersivetechnology.common.multiblocks.metal.process;

import mctmods.immersivetechnology.common.multiblocks.metal.recipe.RadiatorRecipe;
import mctmods.immersivetechnology.core.util.ProcessUtils;

import com.immersiveconvergence.api.util.MarkableFluidTank;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nullable;

public class RadiatorProcess {
    private final RadiatorRecipe recipe;
    private final FluidStack recipeInput;
    private int ticksProcessed = 0;
    private double progressAccumulator = 0.0D;
    private boolean inputDrained = false;

    public RadiatorProcess(RadiatorRecipe recipe, FluidStack input) {
        this.recipe = recipe;
        this.recipeInput = input.copyWithAmount(recipe.getInputAmount());
    }

    @Nullable public static RadiatorProcess fromNBT(Level level, CompoundTag tag) {
        FluidStack input = FluidStack.parseOptional(level.registryAccess(), tag.getCompound("recipe").getCompound("input"));
        RadiatorRecipe recipe = RadiatorRecipe.findRecipe(level, input);
        if (recipe == null) { return null; }
        RadiatorProcess process = new RadiatorProcess(recipe, input);
        process.ticksProcessed = tag.getInt("ticksProcessed");
        process.progressAccumulator = tag.getDouble("progressAccumulator");
        process.inputDrained = tag.getBoolean("inputDrained");
        return process;
    }

    public CompoundTag toNBT(HolderLookup.Provider provider) {
        CompoundTag recipeTag = new CompoundTag();
        recipeTag.put("input", recipeInput.saveOptional(provider));
        CompoundTag tag = new CompoundTag();
        tag.put("recipe", recipeTag);
        tag.putInt("ticksProcessed", ticksProcessed);
        tag.putDouble("progressAccumulator", progressAccumulator);
        tag.putBoolean("inputDrained", inputDrained);
        return tag;
    }

    public void tick(MarkableFluidTank input, MarkableFluidTank output, double speedMult) {
        int total = recipe.getTotalProcessTime();
        if (ticksProcessed >= total) { return; }
        if (!inputDrained) {
            inputDrained = true;
            if (ProcessUtils.drainFails(input, recipe.getInputAmount(), recipe::matches)) {
                ticksProcessed = total;
                return;
            }
        }
        progressAccumulator += Math.max(speedMult, 0.0D);
        int advance = (int) progressAccumulator;
        progressAccumulator -= advance;
        for (int i = 0; i < advance && ticksProcessed < total; i++) {
            ticksProcessed++;
            ProcessUtils.fillShare(output, recipe.fluidOutput(), total, ticksProcessed == total);
        }
    }

    public boolean isComplete() { return ticksProcessed >= recipe.getTotalProcessTime(); }

    public int getTicksProcessed() { return ticksProcessed; }

    public RadiatorRecipe getRecipe() { return recipe; }
}

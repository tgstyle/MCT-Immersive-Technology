package mctmods.immersivetechnology.common.blocks.multiblocks.process;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessInMachine;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.ProcessContext;
import mctmods.immersivetechnology.common.blocks.multiblocks.logic.ITDistillerLogic;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.DistillerRecipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;
import java.util.function.BiFunction;

public class DistillerProcess extends MultiblockProcessInMachine<DistillerRecipe> {
    public DistillerProcess(DistillerRecipe recipe) {
        super(recipe, new int[0]);
        this.setInputTanks(0);
    }
    public DistillerProcess(BiFunction<Level, ResourceLocation, DistillerRecipe> getRecipe, CompoundTag data) {
        super(getRecipe, data);
        this.setInputTanks(0);
    }

    @Override
    protected void processFinish(ProcessContext.ProcessContextInMachine<DistillerRecipe> context, IMultiblockLevel level) {
        super.processFinish(context, level);
        DistillerRecipe recipe = getRecipe(level.getRawLevel());
        if (recipe != null && !recipe.itemOutput.isEmpty() && level.getRawLevel().random.nextFloat() < recipe.chance) {
            IItemHandler inv = context.getInventory();
            ItemStack salt = recipe.itemOutput.copy();
            inv.insertItem(ITDistillerLogic.OUTPUT_SLOT, salt, false);
        }
    }
}

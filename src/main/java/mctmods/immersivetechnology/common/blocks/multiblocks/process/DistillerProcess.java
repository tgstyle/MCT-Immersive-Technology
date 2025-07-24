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
    public DistillerProcess(DistillerRecipe recipe) { super(recipe); }
    public DistillerProcess(BiFunction<Level, ResourceLocation, DistillerRecipe> getRecipe, CompoundTag data) { super(getRecipe, data); }

    @Override
    protected void processFinish(ProcessContext.ProcessContextInMachine<DistillerRecipe> context, IMultiblockLevel level) {
        super.processFinish(context, level);
        DistillerRecipe recipe = getLevelData(level.getRawLevel()).recipe();
        if (recipe != null && !recipe.itemOutput.isEmpty() && level.getRawLevel().random.nextFloat() < recipe.chance) {
            IItemHandler inv = context.getInventory();
            ItemStack salt = recipe.itemOutput.copy();
            if (inv.insertItem(ITDistillerLogic.SLOT_WATER_OUT, salt, false).isEmpty()) return;
            if (inv.insertItem(ITDistillerLogic.SLOT_WATER_EMPTY_OUT, salt, false).isEmpty()) return;
        }
    }
}

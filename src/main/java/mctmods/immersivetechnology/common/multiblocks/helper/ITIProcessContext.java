package mctmods.immersivetechnology.common.multiblocks.helper;

import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.api.energy.AveragingEnergyStorage;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.ProcessContext;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcess;
import net.minecraft.world.level.Level;

public interface ITIProcessContext<R extends MultiblockRecipe> extends ProcessContext<R> {
    AveragingEnergyStorage getEnergy();

    default boolean additionalCanProcessCheck(MultiblockProcess<R, ?> process, Level level) { return true; }

    default void onProcessFinish(MultiblockProcess<R, ?> process, Level level) { }

    interface ProcessContextInMachine<R extends MultiblockRecipe> extends ITIProcessContext<R>, ProcessContext.ProcessContextInMachine<R> { }
}

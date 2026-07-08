package mctmods.immersivetechnology.common.blocks.metal.logic;

import mctmods.immersivetechnology.common.blocks.metal.shape.ITrashCanShape;
import mctmods.immersivetechnology.core.util.TranslationKey;
import mctmods.immersivetechnology.core.ITClientConfig;
import mctmods.immersivetechnology.core.registration.ITBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TrashFluidBlockEntity extends OSDCommonBlockEntity implements IFluidHandler, ITrashCanShape {
    public TrashFluidBlockEntity(BlockPos pos, BlockState state) { super(ITBlockEntities.TRASH_FLUID.get(), pos, state); }

    @SuppressWarnings("unused")
    public IFluidHandler getFluidHandler(@Nullable Direction side) {
        return this;
    }

    @Override public int getTanks() { return 1; }

    @Override @NotNull public FluidStack getFluidInTank(int tank) { return FluidStack.EMPTY; }

    @Override public int getTankCapacity(int tank) { return Integer.MAX_VALUE; }

    @Override public boolean isFluidValid(int tank, @NotNull FluidStack stack) { return true; }

    @Override public int fill(@NotNull FluidStack resource, FluidAction action) {
        if (action.execute()) { acceptedAmount += resource.getAmount(); }
        return resource.getAmount();
    }

    @Override @NotNull public FluidStack drain(@NotNull FluidStack resource, @NotNull FluidAction action) { return FluidStack.EMPTY; }

    @Override @NotNull public FluidStack drain(int maxDrain, @NotNull FluidAction action) { return FluidStack.EMPTY; }

    @Override public TranslationKey text() { return ITClientConfig.perTickTrashCans ? TranslationKey.OVERLAY_OSD_TRASH_FLUID_NORMAL_ALTERNATIVE : TranslationKey.OVERLAY_OSD_TRASH_FLUID_NORMAL_FIRST_LINE; }
}

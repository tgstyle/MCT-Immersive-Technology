package mctmods.immersivetechnology.common.blocks.metal.logic;

import mctmods.immersivetechnology.common.blocks.metal.shape.ITrashCanShape;
import mctmods.immersivetechnology.core.util.TranslationKey;
import mctmods.immersivetechnology.core.ITClientConfig;
import mctmods.immersivetechnology.core.registration.ITBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TrashFluidIBlockEntity extends OSDCommonIBlockEntity implements IFluidHandler, ITrashCanShape {
    private final LazyOptional<IFluidHandler> handler = LazyOptional.of(() -> this);

    public TrashFluidIBlockEntity(BlockPos pos, BlockState state) { super(ITBlockEntities.TRASH_FLUID.get(), pos, state); }

    @Override @NotNull public <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction facing) {
        if (capability == ForgeCapabilities.FLUID_HANDLER) { return handler.cast(); }
        return super.getCapability(capability, facing);
    }

    @Override public void invalidateCaps() {
        super.invalidateCaps();
        handler.invalidate();
    }

    @Override public int getTanks() { return 1; }

    @Override @NotNull public FluidStack getFluidInTank(int tank) { return FluidStack.EMPTY; }

    @Override public int getTankCapacity(int tank) { return Integer.MAX_VALUE; }

    @Override public boolean isFluidValid(int tank, @NotNull FluidStack stack) { return true; }

    @Override public int fill(FluidStack resource, FluidAction action) {
        if (action.execute()) { acceptedAmount += resource.getAmount(); }
        return resource.getAmount();
    }

    @Override @NotNull public FluidStack drain(FluidStack resource, FluidAction action) { return FluidStack.EMPTY; }

    @Override @NotNull public FluidStack drain(int maxDrain, FluidAction action) { return FluidStack.EMPTY; }

    @Override public TranslationKey text() { return ITClientConfig.perTickTrashCans ? TranslationKey.OVERLAY_OSD_TRASH_FLUID_NORMAL_ALTERNATIVE : TranslationKey.OVERLAY_OSD_TRASH_FLUID_NORMAL_FIRST_LINE; }
}

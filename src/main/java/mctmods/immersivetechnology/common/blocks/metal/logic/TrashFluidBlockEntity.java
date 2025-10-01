package mctmods.immersivetechnology.common.blocks.metal.logic;

import mctmods.immersivetechnology.common.blocks.metal.shape.TrashCanShape;
import mctmods.immersivetechnology.common.util.TranslationKey;
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

import javax.annotation.Nullable;
import javax.annotation.Nonnull;

public class TrashFluidBlockEntity extends OSDCommonBlockEntity implements IFluidHandler, TrashCanShape {
    private final LazyOptional<IFluidHandler> handler = LazyOptional.of(() -> this);

    public TrashFluidBlockEntity(BlockPos pos, BlockState state) { super(ITBlockEntities.TRASH_FLUID.get(), pos, state); }

    @Override
    public <T> @Nonnull LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction facing) {
        if (capability == ForgeCapabilities.FLUID_HANDLER) { return handler.cast(); }
        return super.getCapability(capability, facing);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        handler.invalidate();
    }

    @Override
    public int getTanks() { return 1; }

    @Nonnull
    @Override
    public FluidStack getFluidInTank(int tank) { return FluidStack.EMPTY; }

    @Override
    public int getTankCapacity(int tank) { return Integer.MAX_VALUE; }

    @Override
    public boolean isFluidValid(int tank, @Nonnull FluidStack stack) { return true; }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (action.execute()) { acceptedAmount += resource.getAmount(); }
        return resource.getAmount();
    }

    @Nonnull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) { return FluidStack.EMPTY; }

    @Nonnull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action) { return FluidStack.EMPTY; }

    @Override
    public TranslationKey text() { return ITClientConfig.perTickTrashCans.get() ? TranslationKey.OVERLAY_OSD_TRASH_FLUID_NORMAL_ALTERNATIVE : TranslationKey.OVERLAY_OSD_TRASH_FLUID_NORMAL_FIRST_LINE; }
}

package mctmods.immersivetechnology.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEEnums.IOSideConfig;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.blocks.metal.MetalBarrelBlockEntity;
import blusunrize.immersiveengineering.common.blocks.ticking.IEServerTickableBE;
import blusunrize.immersiveengineering.common.util.Utils;
import mctmods.immersivetechnology.common.fluids.ITMarkableFluidTank;
import mctmods.immersivetechnology.common.util.TranslationKey;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SteelBarrelBlockEntity extends MetalBarrelBlockEntity implements IEBlockInterfaces.IConfigurableSides, IEBlockInterfaces.IComparatorOverride, IEBlockInterfaces.IBlockOverlayText, IEBlockInterfaces.IPlayerInteraction, IEServerTickableBE {
    private static final int tankSize = 24000;
    private static final int transferSpeed = 500;

    public int[] sideConfig = {1, 0};

    protected int sleep = 0;

    private final LazyOptional<IFluidHandler> topHandler = LazyOptional.of(() -> new SidedFluidHandler(this, Direction.UP));
    protected final LazyOptional<IFluidHandler> bottomHandler = LazyOptional.of(() -> new SidedFluidHandler(this, Direction.DOWN));
    private final LazyOptional<IFluidHandler> nullHandler = LazyOptional.of(() -> new SidedFluidHandler(this, null));

    public SteelBarrelBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
        this.tank = new ITMarkableFluidTank(tankSize, v -> this.setChanged());
    }

    @Override
    public void tickServer() {
        boolean out = !isRSPowered();
        if (out) super.tickServer();

        if (level == null || level.isClientSide) return;

        for (int index = 0; index < 2; index++) {
            if (tank.getFluidAmount() > 0 && sideConfig[index] == 1) {
                Direction face = index == 0 ? Direction.DOWN : Direction.UP;
                FluidUtil.getFluidHandler(level, worldPosition.relative(face), face.getOpposite()).ifPresent(output -> {
                    if (sleep == 0) {
                        FluidStack simulatedDrain = tank.drain(Math.min(transferSpeed, tank.getFluidAmount()), IFluidHandler.FluidAction.SIMULATE);
                        if (simulatedDrain.getAmount() > 0) {
                            int accepted = output.fill(simulatedDrain, IFluidHandler.FluidAction.SIMULATE);
                            if (accepted > 0) {
                                FluidStack drained = tank.drain(accepted, IFluidHandler.FluidAction.EXECUTE);
                                output.fill(drained, IFluidHandler.FluidAction.EXECUTE);
                                sleep = 0;
                            } else {
                                sleep = 20;
                            }
                        } else {
                            sleep = 20;
                        }
                    } else {
                        sleep--;
                    }
                });
            }
        }
    }

    @Override
    public void readCustomNBT(@NotNull CompoundTag nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        sideConfig = nbt.getIntArray("sideConfig");
        if (sideConfig.length < 2) sideConfig = new int[]{1, 0};
    }

    @Override
    public void writeCustomNBT(@NotNull CompoundTag nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        nbt.putIntArray("sideConfig", sideConfig);
    }

    @Override
    public boolean isFluidValid(@NotNull FluidStack fluid) { return !fluid.isEmpty(); }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            if (side == null) return nullHandler.cast();
            if (side.getAxis() == Direction.Axis.Y) return (side == Direction.UP ? topHandler : bottomHandler).cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        topHandler.invalidate();
        bottomHandler.invalidate();
        nullHandler.invalidate();
    }

    @Override
    public Component @NotNull [] getOverlayText(@NotNull Player player, @NotNull HitResult rtr, boolean hammer) {
        if (Utils.isFluidRelatedItemStack(player.getItemInHand(InteractionHand.MAIN_HAND))) {
            FluidStack fs = tank.getFluid();
            if (fs.isEmpty()) return new Component[]{Component.translatable(TranslationKey.GUI_EMPTY.text())};
            return new Component[]{Component.literal(TranslationKey.OVERLAY_OSD_BARREL_NORMAL_FIRST_LINE.format(fs.getDisplayName().getString(), fs.getAmount()))};
        }
        return new Component[0];
    }

    @Override
    public int getComparatorInputOverride() { return (15 * tank.getFluidAmount()) / tank.getCapacity(); }

    public IOSideConfig getSideConfig(int side) {
        if (side < 0 || side > 1) return IOSideConfig.NONE;
        return IOSideConfig.VALUES[sideConfig[side] + 1];
    }

    public boolean toggleSide(int side, Player player) {
        if (side < 0 || side > 1) return false;
        sideConfig[side] = (sideConfig[side] + 1) % 3 - 1;
        setChanged();
        if (level != null) {
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
            level.blockEvent(worldPosition, getBlockState().getBlock(), 0, 0);
        }
        return true;
    }

    @Override
    public boolean interact(@NotNull Direction side, @NotNull Player player, @NotNull InteractionHand hand, @NotNull ItemStack heldItem, float hitX, float hitY, float hitZ) { return FluidUtil.interactWithFluidHandler(player, hand, tank); }

    public static class SidedFluidHandler implements IFluidHandler {
        SteelBarrelBlockEntity barrel;
        Direction facing;

        public SidedFluidHandler(SteelBarrelBlockEntity barrel, Direction facing) {
            this.barrel = barrel;
            this.facing = facing;
        }

        @Override
        public int getTanks() { return barrel.tank.getTanks(); }

        @Override
        public @NotNull FluidStack getFluidInTank(int tank) { return barrel.tank.getFluidInTank(tank); }

        @Override
        public int getTankCapacity(int tank) { return barrel.tank.getTankCapacity(tank); }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) { return barrel.isFluidValid(stack); }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (resource.isEmpty() || (facing != null && barrel.sideConfig[facing == Direction.UP ? 1 : 0] != 0)) return 0;
            return barrel.tank.fill(resource, action);
        }

        @Override
        public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
            if (resource.isEmpty() || (facing != null && barrel.sideConfig[facing == Direction.UP ? 1 : 0] != 1)) return FluidStack.EMPTY;
            return barrel.tank.drain(resource, action);
        }

        @Override
        public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
            if (facing != null && barrel.sideConfig[facing == Direction.UP ? 1 : 0] != 1) return FluidStack.EMPTY;
            return barrel.tank.drain(maxDrain, action);
        }
    }
}

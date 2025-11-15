package mctmods.immersivetechnology.common.blocks.metal.logic;

import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableMap;
import mctmods.immersivetechnology.common.blocks.helper.*;
import mctmods.immersivetechnology.common.blocks.helper.ITEnums.IOSideConfig;
import mctmods.immersivetechnology.common.blocks.metal.BarrelSteelBlock;
import mctmods.immersivetechnology.common.fluids.helper.ITMarkableFluidTank;
import mctmods.immersivetechnology.common.util.TranslationKey;
import mctmods.immersivetechnology.core.registration.ITBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

public class BarrelSteelBlockEntity extends ITBaseBlockEntity implements ITServerTickableBE, ITClientTickableBE, ITBlockInterfaces.IBlockOverlayText, ITBlockInterfaces.IPlayerInteraction, ITBlockInterfaces.IBlockEntityDrop, ITBlockInterfaces.IComparatorOverride, ITBlockInterfaces.IPlacementInteraction, ITBlockInterfaces.IConfigurableSides {
    private static final int tankSize = 24 * FluidType.BUCKET_VOLUME;
    private static final int transferSpeed = FluidType.BUCKET_VOLUME;

    public final ITMarkableFluidTank tank = new ITMarkableFluidTank(tankSize, v -> setChanged());
    public EnumMap<Direction, IOSideConfig> sideConfig = new EnumMap<>(ImmutableMap.of(Direction.DOWN, IOSideConfig.OUTPUT, Direction.UP, IOSideConfig.INPUT));

    private final Map<Direction, CapabilityReference<IFluidHandler>> neighbors = ImmutableMap.of(
            Direction.DOWN, CapabilityReference.forNeighbor(this, ForgeCapabilities.FLUID_HANDLER, Direction.DOWN),
            Direction.UP, CapabilityReference.forNeighbor(this, ForgeCapabilities.FLUID_HANDLER, Direction.UP)
    );

    private final LazyOptional<IFluidHandler> nonsidedHandler = LazyOptional.of(() -> new SidedFluidHandler(this, null));
    private final LazyOptional<IFluidHandler> upHandler = LazyOptional.of(() -> new SidedFluidHandler(this, Direction.UP));
    private final LazyOptional<IFluidHandler> downHandler = LazyOptional.of(() -> new SidedFluidHandler(this, Direction.DOWN));

    public BarrelSteelBlockEntity(BlockPos pos, BlockState state) { super(ITBlockEntities.BARREL_STEEL.get(), pos, state); }

    @Override
    public void tickClient() { }

    @Override
    public void tickServer() {
        if (isRSPowered()) return;
        boolean update = false;
        for (Direction side : neighbors.keySet()) {
            if (tank.getFluidAmount() > 0 && sideConfig.get(side) == IOSideConfig.OUTPUT) {
                IFluidHandler handler = neighbors.get(side).getNullable();
                if (handler != null) {
                    int out = Math.min(transferSpeed, tank.getFluidAmount());
                    FluidStack simulatedDrain = tank.drain(out, IFluidHandler.FluidAction.SIMULATE);
                    if (!simulatedDrain.isEmpty()) {
                        int accepted = handler.fill(simulatedDrain, IFluidHandler.FluidAction.SIMULATE);
                        if (accepted > 0) {
                            FluidStack drained = tank.drain(accepted, IFluidHandler.FluidAction.EXECUTE);
                            handler.fill(drained, IFluidHandler.FluidAction.EXECUTE);
                            update = true;
                        }
                    }
                }
            }
        }
        if (update) { setChanged(); markContainingBlockForUpdate(null); }
    }

    @Override
    public void readCustomNBT(@NotNull CompoundTag nbt, boolean descPacket) {
        sideConfig.clear();
        int[] sideCfgArray = nbt.getIntArray("sideConfig");
        if (sideCfgArray.length >= 2) {
            sideConfig.put(Direction.DOWN, IOSideConfig.VALUES[sideCfgArray[0]]);
            sideConfig.put(Direction.UP, IOSideConfig.VALUES[sideCfgArray[1]]);
        } else {
            sideConfig.put(Direction.DOWN, IOSideConfig.OUTPUT);
            sideConfig.put(Direction.UP, IOSideConfig.INPUT);
        }
        tank.readFromNBT(nbt.getCompound("tank"));
        if (descPacket) markContainingBlockForUpdate(null);
        else updateState();
    }

    @Override
    public void writeCustomNBT(@NotNull CompoundTag nbt, boolean descPacket) {
        int[] sideCfgArray = new int[2];
        sideCfgArray[0] = sideConfig.getOrDefault(Direction.DOWN, IOSideConfig.OUTPUT).ordinal();
        sideCfgArray[1] = sideConfig.getOrDefault(Direction.UP, IOSideConfig.INPUT).ordinal();
        nbt.putIntArray("sideConfig", sideCfgArray);
        nbt.put("tank", tank.writeToNBT(new CompoundTag()));
    }

    public boolean isFluidValid(@NotNull FluidStack fluid) { return !fluid.isEmpty(); }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction facing) {
        if (capability == ForgeCapabilities.FLUID_HANDLER) {
            if (facing == null) return nonsidedHandler.cast();
            if (facing.getAxis() != Direction.Axis.Y) return super.getCapability(capability, facing);
            return (facing == Direction.UP ? upHandler : downHandler).cast();
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        nonsidedHandler.invalidate();
        upHandler.invalidate();
        downHandler.invalidate();
    }

    @Override
    public Component[] getOverlayText(@NotNull Player player, @NotNull HitResult rtr, boolean hammer) {
        if (rtr.getType() == HitResult.Type.MISS) return null;
        if (Utils.isFluidRelatedItemStack(player.getItemInHand(InteractionHand.MAIN_HAND))) {
            FluidStack fs = tank.getFluid();
            if (fs.isEmpty()) return new Component[]{Component.translatable(TranslationKey.GUI_EMPTY.text())};
            return new Component[]{Component.literal(TranslationKey.OVERLAY_OSD_BARREL_NORMAL_FIRST_LINE.format(fs.getDisplayName().getString(), fs.getAmount()))};
        }
        return new Component[0];
    }

    @Override
    public int getComparatorInputOverride() { return (15 * tank.getFluidAmount()) / tank.getCapacity(); }

    @Override
    public @NotNull IOSideConfig getSideConfig(@NotNull Direction side) { return sideConfig.getOrDefault(side, IOSideConfig.NONE); }

    @Override
    public boolean toggleSide(Direction side, @NotNull Player p) {
        if (side.getAxis() != Direction.Axis.Y) return false;
        IOSideConfig next = IOSideConfig.next(sideConfig.getOrDefault(side, IOSideConfig.NONE));
        sideConfig.put(side, next);
        setChanged();
        updateState();
        markContainingBlockForUpdate(null);
        return true;
    }

    private void updateState() {
        if (level == null || level.isClientSide) return;
        BlockState current = getBlockState();
        BlockState newState = current.setValue(BarrelSteelBlock.TOP_CONFIG, sideConfig.getOrDefault(Direction.UP, IOSideConfig.INPUT))
                .setValue(BarrelSteelBlock.BOTTOM_CONFIG, sideConfig.getOrDefault(Direction.DOWN, IOSideConfig.OUTPUT));
        if (!current.equals(newState)) level.setBlock(getBlockPos(), newState, 3);
    }

    @Override
    public boolean interact(@NotNull Direction side, @NotNull Player player, @NotNull InteractionHand hand, @NotNull ItemStack heldItem, float hitX, float hitY, float hitZ) {
        if (FluidUtil.interactWithFluidHandler(player, hand, tank)) { setChanged(); markContainingBlockForUpdate(null); return true; }
        return false;
    }

    @Override
    public void getBlockEntityDrop(@NotNull LootContext context, @NotNull Consumer<ItemStack> drop) {
        ItemStack stack = new ItemStack(getBlockState().getBlock(), 1);
        CompoundTag tag = new CompoundTag();
        writeTank(tag, true);
        if (!tag.isEmpty()) stack.setTag(tag);
        drop.accept(stack);
    }

    @Override
    public void onBEPlaced(BlockPlaceContext ctx) { if (ctx.getItemInHand().hasTag()) readTank(ctx.getItemInHand().getOrCreateTag()); }

    public void writeTank(CompoundTag nbt, boolean toItem) {
        boolean write = tank.getFluidAmount() > 0;
        CompoundTag tankTag = tank.writeToNBT(new CompoundTag());
        if (!toItem || write) nbt.put("tank", tankTag);
    }

    public void readTank(CompoundTag nbt) { tank.readFromNBT(nbt.getCompound("tank")); }

    public static class SidedFluidHandler implements IFluidHandler {
        BarrelSteelBlockEntity barrel;
        @Nullable Direction facing;

        public SidedFluidHandler(BarrelSteelBlockEntity barrel, @Nullable Direction facing) { this.barrel = barrel; this.facing = facing; }

        @Override
        public int getTanks() { return barrel.tank.getTanks(); }

        @Override
        public @NotNull FluidStack getFluidInTank(int tank) { return barrel.tank.getFluidInTank(tank); }

        @Override
        public int getTankCapacity(int tank) { return barrel.tank.getTankCapacity(tank); }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) { return barrel.isFluidValid(stack); }

        @Override
        public int fill(FluidStack resource, FluidAction action) { if (resource.isEmpty() || (facing != null && barrel.sideConfig.get(facing) != IOSideConfig.INPUT)) return 0; return barrel.tank.fill(resource, action); }

        @Override
        public @NotNull FluidStack drain(FluidStack resource, FluidAction action) { if (resource.isEmpty() || (facing != null && barrel.sideConfig.get(facing) != IOSideConfig.OUTPUT)) return FluidStack.EMPTY; return barrel.tank.drain(resource, action); }

        @Override
        public @NotNull FluidStack drain(int maxDrain, FluidAction action) { if (facing != null && barrel.sideConfig.get(facing) != IOSideConfig.OUTPUT) return FluidStack.EMPTY; return barrel.tank.drain(maxDrain, action); }
    }
}

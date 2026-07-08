package mctmods.immersivetechnology.common.blocks.metal.logic;

import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableMap;
import mctmods.immersivetechnology.common.blocks.helper.ITBaseBlockEntity;
import mctmods.immersivetechnology.common.blocks.helper.ITIBlockInterfaces;
import mctmods.immersivetechnology.common.blocks.helper.ITEnums.IOSideConfig;
import mctmods.immersivetechnology.common.blocks.helper.ITIClientTickableBE;
import mctmods.immersivetechnology.common.blocks.helper.ITIServerTickableBE;
import mctmods.immersivetechnology.common.fluids.helper.ITMarkableFluidTank;
import mctmods.immersivetechnology.core.util.TranslationKey;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.EnumMap;
import java.util.function.Consumer;
import java.util.stream.Stream;

public abstract class BarrelCommonBlockEntity extends ITBaseBlockEntity implements ITIServerTickableBE, ITIClientTickableBE, ITIBlockInterfaces.IBlockOverlayText, ITIBlockInterfaces.IPlayerInteraction, ITIBlockInterfaces.IBlockEntityDrop, ITIBlockInterfaces.IComparatorOverride, ITIBlockInterfaces.IPlacementInteraction, ITIBlockInterfaces.IConfigurableSides {
    public final ITMarkableFluidTank tank;
    public EnumMap<Direction, IOSideConfig> sideConfig = new EnumMap<>(ImmutableMap.of(Direction.DOWN, IOSideConfig.OUTPUT, Direction.UP, IOSideConfig.INPUT));
    protected static final int transferSpeed = FluidType.BUCKET_VOLUME;

    public BarrelCommonBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int tankSize) {
        super(type, pos, state);
        this.tank = new ITMarkableFluidTank(tankSize, v -> {
            setChanged();
            markContainingBlockForUpdate(null);
        });
    }

    @Override public void tickClient() { }

    @Override public abstract void tickServer();

    @Override public void readCustomNBT(@NotNull CompoundTag nbt, boolean descPacket) {
        sideConfig.clear();
        int[] sideCfgArray = nbt.getIntArray("sideConfig");
        if (sideCfgArray.length >= 2) {
            sideConfig.put(Direction.DOWN, IOSideConfig.VALUES[sideCfgArray[0]]);
            sideConfig.put(Direction.UP, IOSideConfig.VALUES[sideCfgArray[1]]);
        } else {
            sideConfig.put(Direction.DOWN, IOSideConfig.OUTPUT);
            sideConfig.put(Direction.UP, IOSideConfig.INPUT);
        }
        if (level != null) {
            tank.readFromNBT(level.registryAccess(), nbt.getCompound("tank"));
        } else {
            tank.readFromNBT(HolderLookup.Provider.create(Stream.empty()), nbt.getCompound("tank"));
        }
        postRead(descPacket);
    }

    protected void postRead(boolean descPacket) { if (!descPacket) updateState(); }

    @Override public void writeCustomNBT(@NotNull CompoundTag nbt, boolean descPacket) {
        int[] sideCfgArray = new int[2];
        sideCfgArray[0] = sideConfig.getOrDefault(Direction.DOWN, IOSideConfig.OUTPUT).ordinal();
        sideCfgArray[1] = sideConfig.getOrDefault(Direction.UP, IOSideConfig.INPUT).ordinal();
        nbt.putIntArray("sideConfig", sideCfgArray);
        CompoundTag tankTag = new CompoundTag();
        if (level != null) {
            tank.writeToNBT(level.registryAccess(), tankTag);
        } else {
            tank.writeToNBT(HolderLookup.Provider.create(Stream.empty()), tankTag);
        }
        nbt.put("tank", tankTag);
    }

    @SuppressWarnings("unused")
    public IFluidHandler getFluidHandler(@Nullable Direction facing) {
        if (facing == null) return new SidedFluidHandler(this, null);
        if (facing.getAxis() != Direction.Axis.Y) return null;
        return (facing == Direction.UP ? new SidedFluidHandler(this, Direction.UP) : new SidedFluidHandler(this, Direction.DOWN));
    }

    @Override public boolean interact(@NotNull Direction side, @NotNull Player player, @NotNull InteractionHand hand, @NotNull ItemStack heldItem, float hitX, float hitY, float hitZ) {
        FluidStack contained = FluidUtil.getFluidContained(heldItem).orElse(FluidStack.EMPTY);
        if (!contained.isEmpty() && !isFluidValid(contained)) {
            if (level != null && !level.isClientSide) player.displayClientMessage(Component.translatable(TranslationKey.NO_GAS_ALLOWED.text()), false);
            return true;
        }
        if (FluidUtil.interactWithFluidHandler(player, hand, tank)) { setChanged(); markContainingBlockForUpdate(null); return true; }
        return false;
    }

    @Override public Component[] getOverlayText(@NotNull Player player, @NotNull HitResult rtr, boolean hammer) {
        if (rtr.getType() == HitResult.Type.MISS) return null;
        if (Utils.isFluidRelatedItemStack(player.getItemInHand(InteractionHand.MAIN_HAND))) {
            FluidStack fs = tank.getFluid();
            if (fs.isEmpty()) return new Component[]{Component.translatable(TranslationKey.GUI_EMPTY.text())};
            return new Component[]{Component.literal(TranslationKey.OVERLAY_OSD_BARREL_NORMAL_FIRST_LINE.format(fs.getHoverName().getString(), fs.getAmount()))};
        }
        return new Component[0];
    }

    @Override public int getComparatorInputOverride() { return (15 * tank.getFluidAmount()) / tank.getCapacity(); }

    @Override public void getBlockEntityDrop(@NotNull LootContext context, @NotNull Consumer<ItemStack> drop) {
        ItemStack stack = new ItemStack(getBlockState().getBlock(), 1);
        CompoundTag tag = new CompoundTag();
        writeTank(tag, true);
        if (!tag.isEmpty()) {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
        drop.accept(stack);
    }

    @Override public void onBEPlaced(BlockPlaceContext ctx) {
        ItemStack stack = ctx.getItemInHand();
        if (stack.has(DataComponents.CUSTOM_DATA)) {
            CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
            if (customData != null) {
                CompoundTag tag = customData.copyTag();
                if (tag.contains("tank")) readTank(tag);
            }
        }
    }

    public void writeTank(CompoundTag nbt, boolean toItem) {
        boolean write = tank.getFluidAmount() > 0;
        CompoundTag tankTag = new CompoundTag();
        if (level != null) {
            tank.writeToNBT(level.registryAccess(), tankTag);
        } else {
            tank.writeToNBT(HolderLookup.Provider.create(Stream.empty()), tankTag);
        }
        if (!toItem || write) nbt.put("tank", tankTag);
    }

    public void readTank(CompoundTag nbt) {
        CompoundTag tankTag = nbt.getCompound("tank");
        if (level != null) {
            tank.readFromNBT(level.registryAccess(), tankTag);
        } else {
            tank.readFromNBT(HolderLookup.Provider.create(Stream.empty()), tankTag);
        }
    }

    protected abstract boolean isFluidValid(@NotNull FluidStack fluid);

    @Override @NotNull public IOSideConfig getSideConfig(@NotNull Direction side) { return sideConfig.getOrDefault(side, IOSideConfig.NONE); }

    @Override public boolean toggleSide(Direction side, @NotNull Player p) {
        if (side.getAxis() != Direction.Axis.Y) return false;
        if (!canConfigureSide(side)) return false;
        IOSideConfig next = IOSideConfig.next(sideConfig.getOrDefault(side, IOSideConfig.NONE));
        sideConfig.put(side, next);
        setChanged();
        updateState();
        requestModelDataUpdate();
        markContainingBlockForUpdate(null);
        return true;
    }

    protected abstract boolean canConfigureSide(Direction side);

    protected abstract void updateState();

    @Override @NotNull public CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        writeCustomNBT(tag, true);
        return tag;
    }

    @Override public void handleUpdateTag(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.handleUpdateTag(tag, registries);
        readCustomNBT(tag, true);
    }

    @Override public void onDataPacket(@NotNull Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.@NotNull Provider registries) {
        super.onDataPacket(net, pkt, registries);
        readCustomNBT(pkt.getTag(), true);
    }

    public static class SidedFluidHandler implements IFluidHandler {
        BarrelCommonBlockEntity barrel;
        @Nullable Direction facing;

        public SidedFluidHandler(BarrelCommonBlockEntity barrel, @Nullable Direction facing) { this.barrel = barrel; this.facing = facing; }

        @Override public int getTanks() { return barrel.tank.getTanks(); }

        @Override @NotNull public FluidStack getFluidInTank(int tank) { return barrel.tank.getFluidInTank(tank); }

        @Override public int getTankCapacity(int tank) { return barrel.tank.getTankCapacity(tank); }

        @Override public boolean isFluidValid(int tank, @NotNull FluidStack stack) { return barrel.isFluidValid(stack); }

        @Override public int fill(FluidStack resource, @NotNull FluidAction action) {
            if (resource.isEmpty() || (facing != null && barrel.sideConfig.get(facing) != IOSideConfig.INPUT)) return 0;
            int filled = barrel.tank.fill(resource, action);
            if (filled > 0 && action.execute()) {
                barrel.setChanged();
                barrel.markContainingBlockForUpdate(null);
            }
            return filled;
        }

        @Override @NotNull public FluidStack drain(FluidStack resource, @NotNull FluidAction action) {
            if (resource.isEmpty() || (facing != null && barrel.sideConfig.get(facing) != IOSideConfig.OUTPUT)) return FluidStack.EMPTY;
            FluidStack drained = barrel.tank.drain(resource, action);
            if (!drained.isEmpty() && action.execute()) {
                barrel.setChanged();
                barrel.markContainingBlockForUpdate(null);
            }
            return drained;
        }

        @Override @NotNull public FluidStack drain(int maxDrain, @NotNull FluidAction action) {
            if (facing != null && barrel.sideConfig.get(facing) != IOSideConfig.OUTPUT) return FluidStack.EMPTY;
            FluidStack drained = barrel.tank.drain(maxDrain, action);
            if (!drained.isEmpty() && action.execute()) {
                barrel.setChanged();
                barrel.markContainingBlockForUpdate(null);
            }
            return drained;
        }
    }
}

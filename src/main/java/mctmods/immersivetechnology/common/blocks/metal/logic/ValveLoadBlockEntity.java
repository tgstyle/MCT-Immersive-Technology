package mctmods.immersivetechnology.common.blocks.metal.logic;

import mctmods.immersivetechnology.common.blocks.metal.gui.ValveLoadMenu;
import mctmods.immersivetechnology.common.util.TranslationKey;
import mctmods.immersivetechnology.core.registration.ITBlockEntities;
import mctmods.immersivetechnology.core.registration.ITMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.jetbrains.annotations.NotNull;

import static mctmods.immersivetechnology.common.blocks.metal.ValveLoadBlock.OPEN;

public class ValveLoadBlockEntity extends ValveCommonBlockEntity implements IEnergyStorage {
    public static class DummyBattery implements IEnergyStorage {
        @Override public int receiveEnergy(int maxReceive, boolean simulate) { return 0; }
        @Override public int extractEnergy(int maxExtract, boolean simulate) { return 0; }
        @Override public int getEnergyStored() { return 0; }
        @Override public int getMaxEnergyStored() { return 0; }
        @Override public boolean canExtract() { return false; }
        @Override public boolean canReceive() { return false; }
    }

    public ValveLoadBlockEntity(BlockPos pos, BlockState state) { super(ITBlockEntities.VALVE_LOAD.get(), pos, state, TranslationKey.OVERLAY_OSD_VALVE_LOAD_NORMAL_FIRST_LINE, TranslationKey.OVERLAY_OSD_VALVE_LOAD_SNEAKING_FIRST_LINE, TranslationKey.OVERLAY_OSD_VALVE_LOAD_SNEAKING_SECOND_LINE, 1); }

    @Override
    public void onLoad() {
        super.onLoad();
        assert level != null;
        if (!level.isClientSide) {
            efficientSetChanged();
            for (Direction d : Direction.values()) { level.neighborChanged(worldPosition.relative(d), getBlockState().getBlock(), worldPosition); }
            markContainingBlockForUpdate(null);
            updateRedstoneState();
        }
    }

    @Override
    public void onNeighborBlockChange(BlockPos otherPos) {
        super.onNeighborBlockChange(otherPos);
        updateRedstoneState();
    }

    private LazyOptional<IEnergyStorage> myCapability = null;
    private LazyOptional<IEnergyStorage> dummyCapability = null;

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> capability, Direction facing) {
        if (facing == null) return super.getCapability(capability, null);
        if (capability == ForgeCapabilities.ENERGY && facing.getAxis() == this.facing.getAxis()) {
            if (facing == this.facing.getOpposite()) {
                if (myCapability == null || !myCapability.isPresent()) myCapability = LazyOptional.of(() -> this);
                return myCapability.cast();
            } else if (facing == this.facing) {
                if (dummyCapability == null || !dummyCapability.isPresent()) dummyCapability = LazyOptional.of(DummyBattery::new);
                return dummyCapability.cast();
            }
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        if (myCapability != null) { myCapability.invalidate(); myCapability = null; }
        if (dummyCapability != null) { dummyCapability.invalidate(); dummyCapability = null; }
    }

    @Override
    public void setFacing(@NotNull Direction facing) {
        super.setFacing(facing);
        invalidateCaps();
    }

    boolean busy = false;

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        assert level != null;
        if (level.isClientSide) return 0;
        if (busy) return 0;
        BlockState state = getBlockState();
        if (!state.getValue(OPEN)) return 0;
        IEnergyStorage destination = getDestination();
        if (destination == null) return 0;
        int canAccept = maxReceive;
        canAccept = timeLimit > 0 ? Math.min(Math.max(timeLimit - longToInt(acceptedAmount), 0), canAccept) : canAccept;
        canAccept = keepSize > 0 ? Math.min(Math.max(keepSize - destination.getEnergyStored(), 0), canAccept) : canAccept;
        canAccept = packetLimit > 0 ? Math.min(canAccept, packetLimit) : canAccept;
        if (redstoneMode > 0) canAccept = (int) (canAccept * ((redstoneMode == 1 ? 15 - getRSPower() : getRSPower()) / 15.0));
        if (canAccept == 0) return 0;
        busy = true;
        int toReturn = destination.receiveEnergy(canAccept, simulate);
        busy = false;
        if (!simulate) { acceptedAmount += toReturn; packets++; }
        return toReturn;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) { return 0; }

    @Override
    public int getEnergyStored() {
        IEnergyStorage dest = getDestination();
        return dest == null ? 0 : dest.getEnergyStored();
    }

    @Override
    public int getMaxEnergyStored() {
        IEnergyStorage dest = getDestination();
        return dest == null ? 0 : dest.getMaxEnergyStored();
    }

    @Override
    public boolean canExtract() { return false; }

    @Override
    public boolean canReceive() { return true; }

    public IEnergyStorage getDestination() {
        assert level != null;
        BlockPos dstPos = worldPosition.relative(facing);
        BlockEntity dst = level.getBlockEntity(dstPos);
        if (dst != null) {
            LazyOptional<IEnergyStorage> cap = dst.getCapability(ForgeCapabilities.ENERGY, facing.getOpposite());
            return cap.resolve().orElse(null);
        }
        return null;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) { return ValveLoadMenu.makeServer(ITMenuTypes.VALVE_LOAD.getType(), id, inv, this); }

    @Override
    public @NotNull Component getDisplayName() { return Component.translatable(TranslationKey.GUI_VALVE_LOAD.location); }

    @Override
    public void receiveMessageFromServer(CompoundTag nbt) {
        packetLimit = nbt.getInt("packetLimit");
        timeLimit = nbt.getInt("timeLimit");
        keepSize = nbt.getInt("keepSize");
    }

    @Override
    public void receiveMessageFromClient(CompoundTag nbt) {
        packetLimit = nbt.getInt("packetLimit");
        timeLimit = nbt.getInt("timeLimit");
        keepSize = nbt.getInt("keepSize");
        efficientSetChanged();
    }

    @Override public boolean stillValid(Player player) { return !isRemoved() && player.distanceToSqr(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D) <= 64.0D; }
}

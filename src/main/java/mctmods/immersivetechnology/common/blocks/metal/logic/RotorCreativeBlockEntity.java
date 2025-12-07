package mctmods.immersivetechnology.common.blocks.metal.logic;

import mctmods.immersivetechnology.api.MechanicalCapabilities;
import mctmods.immersivetechnology.api.capability.IMechanicalEnergyProvider;
import mctmods.immersivetechnology.common.blocks.helper.ITBaseBlockEntity;
import mctmods.immersivetechnology.common.blocks.helper.ITClientTickableBE;
import mctmods.immersivetechnology.common.blocks.metal.RotorCreativeBlock;
import mctmods.immersivetechnology.common.util.TranslationKey;
import mctmods.immersivetechnology.core.registration.ITBlockEntities;
import mctmods.immersivetechnology.core.registration.ITMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RotorCreativeBlockEntity extends ITBaseBlockEntity implements MenuProvider, ITClientTickableBE {
    public int rpm;
    private final LazyOptional<IMechanicalEnergyProvider> providerCap = LazyOptional.of(Provider::new);
    public float animation_rotation = 0f;
    public float animation_step = 0f;

    public RotorCreativeBlockEntity(BlockPos pos, BlockState state) { super(ITBlockEntities.ROTOR_CREATIVE.get(), pos, state); rpm = 7200; }

    @Override public void tickClient() {
        animation_step = (Math.abs(rpm) / 7200f) * 72f;
        float dir = Math.signum(rpm);
        animation_rotation += animation_step * dir;
        animation_rotation %= 360;
    }

    @Override public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        Direction facing = getBlockState().getValue(RotorCreativeBlock.FACING);
        if ((side == facing || side == facing.getOpposite()) && cap == MechanicalCapabilities.MECHANICAL_PROVIDER_CAPABILITY) { return providerCap.cast(); }
        return super.getCapability(cap, side);
    }

    private class Provider implements IMechanicalEnergyProvider {
        @Override public int getSpeed() { return rpm; }
        @Override public float getTorque() { return 1f; }
        @Override public int getMaxSpeed() { return Integer.MAX_VALUE / 2; }
        @Override public double getBaseMass() { return 0; }
        @Override public double getDriveTorque() { return 0; }
        @Override public double getFriction() { return 0; }
    }

    @Override public void readCustomNBT(CompoundTag nbt, boolean descPacket) {
        rpm = nbt.getInt("rpm");
        if (descPacket) {
            animation_rotation = nbt.getFloat("animation_rotation");
            animation_step = nbt.getFloat("animation_step");
        }
    }

    @Override public void writeCustomNBT(CompoundTag nbt, boolean descPacket) {
        nbt.putInt("rpm", rpm);
        if (descPacket) {
            nbt.putFloat("animation_rotation", animation_rotation);
            nbt.putFloat("animation_step", animation_step);
        }
    }

    @Override public void receiveMessageFromClient(CompoundTag message) {
        if (message.contains("rpm")) {
            int newRpm = message.getInt("rpm");
            rpm = Math.max(Math.min(newRpm, 7200), -7200);
            setChanged();
            markContainingBlockForUpdate(null);
        }
    }

    public boolean stillValid(Player player) { return player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) < 64; }

    @Override public @Nullable AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) { return ITMenuTypes.ROTOR_CREATIVE.create(id, inv, this); }

    @Override public @NotNull Component getDisplayName() { return Component.translatable(TranslationKey.GUI_ROTOR_CREATIVE.getLocation()); }
}

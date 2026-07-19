package mctmods.immersivetechnology.common.blocks.metal.logic;

import com.immersiveconvergence.api.MechanicalCapabilities;
import com.immersiveconvergence.api.capability.IMechanicalEnergyProvider;
import mctmods.immersivetechnology.common.blocks.helper.BaseBlockEntity;
import mctmods.immersivetechnology.common.blocks.helper.IClientTickableBE;
import mctmods.immersivetechnology.common.blocks.metal.RotorCreativeBlock;
import mctmods.immersivetechnology.core.util.TranslationKey;
import mctmods.immersivetechnology.core.registration.BlockEntities;
import mctmods.immersivetechnology.core.registration.MenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RotorCreativeBlockEntity extends BaseBlockEntity implements MenuProvider, IClientTickableBE {
    public int rpm;
    public float animation_rotation = 0f;
    public float animation_step = 0f;
    private final Provider mechanicalProvider = new Provider();

    public RotorCreativeBlockEntity(BlockPos pos, BlockState state) { super(BlockEntities.ROTOR_CREATIVE.get(), pos, state); rpm = MechanicalCapabilities.MAX_RPM; }

    public IMechanicalEnergyProvider getMechanicalProvider(@Nullable Direction side) {
        Direction facing = getBlockState().getValue(RotorCreativeBlock.FACING);
        if (side == null || side == facing || side == facing.getOpposite()) { return mechanicalProvider; }
        return null;
    }

    @Override public void tickClient() {
        animation_step = (Math.abs(rpm) / (float) MechanicalCapabilities.MAX_RPM) * 72f;
        float dir = Math.signum(rpm);
        animation_rotation += animation_step * dir;
        animation_rotation %= 360;
    }

    private class Provider implements IMechanicalEnergyProvider {
        @Override public int getSpeed() { return rpm; }
        @Override public float getTorque() { return 1f; }
        @Override public int getMaxSpeed() { return MechanicalCapabilities.MAX_RPM; }
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
            rpm = Math.clamp(newRpm, -MechanicalCapabilities.MAX_RPM, MechanicalCapabilities.MAX_RPM);
            setChanged();
            markContainingBlockForUpdate(null);
        }
    }

    public boolean stillValid(Player player) { return player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) < 64; }

    @Override @Nullable public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) { return MenuTypes.ROTOR_CREATIVE.create(id, inv, this); }

    @Override @NotNull public Component getDisplayName() { return Component.translatable(TranslationKey.GUI_ROTOR_CREATIVE.getLocation()); }
}

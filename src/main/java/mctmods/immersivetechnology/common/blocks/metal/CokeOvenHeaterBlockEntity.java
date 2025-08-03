package mctmods.immersivetechnology.common.blocks.metal;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.client.IModelOffsetProvider;
import blusunrize.immersiveengineering.api.energy.MutableEnergyStorage;
import blusunrize.immersiveengineering.common.blocks.PlacementLimitation;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.MultiblockCapability;
import blusunrize.immersiveengineering.common.util.Utils;
import mctmods.immersivetechnology.common.blocks.helper.ITBlockInterfaces;
import mctmods.immersivetechnology.common.blocks.helper.ITClientTickableBE;
import mctmods.immersivetechnology.common.blocks.helper.ITBaseBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Objects;

public class CokeOvenHeaterBlockEntity extends ITBaseBlockEntity implements ITBlockInterfaces.IStateBasedDirectional, ITBlockInterfaces.IHasDummyBlocks, IModelOffsetProvider, ITClientTickableBE, ITBlockInterfaces.ISoundBE, ITBlockInterfaces.IGeneralMultiblock {
    public static final float ANGLE_PER_TICK = (float)Math.toRadians(20);
    public boolean active;
    public int dummy = 0;
    public final MutableEnergyStorage energyStorage = new MutableEnergyStorage(8000);
    public float angle = 0;
    private final MultiblockCapability<IEnergyStorage> energyCap = MultiblockCapability.make(this, be -> be.energyCap, CokeOvenHeaterBlockEntity::master, registerEnergyInput(energyStorage));

    public CokeOvenHeaterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) { super(type, pos, state); }

    public int doSpeedup() {
        int consumed = 32;
        if (this.energyStorage.extractEnergy(consumed, true)==consumed) { if (!active) { active = true; this.markContainingBlockForUpdate(null); } this.energyStorage.extractEnergy(consumed, false); return 1; }
        else { turnOff(); }
        return 0;
    }

    @Override
    public void tickClient() {
        if (active) { angle = (angle+ANGLE_PER_TICK)%Mth.PI; }
        ImmersiveEngineering.proxy.handleTileSound(IESounds.preheater, this, active, 0.5f, 1f);
    }

    public void turnOff() { if (active) { active = false; this.markContainingBlockForUpdate(null); } }

    @Override
    public boolean isDummy() { return dummy == 1; }

    @Nullable
    @Override
    public CokeOvenHeaterBlockEntity master() {
        BlockPos masterPos = getBlockPos().north(dummy);
        BlockEntity te = Utils.getExistingTileEntity(level, masterPos);
        return te instanceof CokeOvenHeaterBlockEntity heater ? heater : null;
    }

    @Override
    public void placeDummies(@NotNull BlockPlaceContext ctx, @NotNull BlockState state) {
        if (isDummy()) return;
        state = state.setValue(IEProperties.MULTIBLOCKSLAVE, true);
        assert level != null;
        level.setBlockAndUpdate(worldPosition.offset(0, 0, 1), state);
        CokeOvenHeaterBlockEntity dummy = (CokeOvenHeaterBlockEntity) Objects.requireNonNull(level.getBlockEntity(worldPosition.offset(0, 0, 1)));
        dummy.dummy = 1;
        dummy.setFacing(this.getFacing());
    }

    @Override
    public void breakDummies(@NotNull BlockPos pos, @NotNull BlockState state) {
        if (isDummy()) { Objects.requireNonNull(master()).breakDummies(pos, state); return; }
        assert level != null;
        if (level.getBlockEntity(getBlockPos().offset(0, 0, 1)) instanceof CokeOvenHeaterBlockEntity) { level.removeBlock(getBlockPos().offset(0, 0, 1), false); }
    }

    @Override
    public void readCustomNBT(CompoundTag nbt, boolean descPacket) {
        dummy = nbt.getInt("dummy");
        active = nbt.getBoolean("active");
        if (descPacket) { this.markContainingBlockForUpdate(null); }
        else { EnergyHelper.deserializeFrom(energyStorage, nbt); }
    }

    @Override
    public void writeCustomNBT(CompoundTag nbt, boolean descPacket) {
        nbt.putInt("dummy", dummy);
        nbt.putBoolean("active", active);
        if (!descPacket) { EnergyHelper.serializeTo(energyStorage, nbt); }
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap==ForgeCapabilities.ENERGY&&(side==null||(dummy==0&&side==Direction.UP))) { return energyCap.get().cast(); }
        return super.getCapability(cap, side);
    }

    @Override
    public @NotNull Property<Direction> getFacingProperty() { return IEProperties.FACING_HORIZONTAL; }

    @Override
    public @NotNull PlacementLimitation getFacingLimitation() { return PlacementLimitation.HORIZONTAL; }

    @Override
    public void afterRotation(@NotNull Direction oldDir, @NotNull Direction newDir) {
        for (int i = 0; i <= 1; i++) {
            assert level != null;
            BlockEntity te = level.getBlockEntity(getBlockPos().offset(0, -dummy+i, 0));
            if (te instanceof CokeOvenHeaterBlockEntity heater) {
                heater.setFacing(newDir);
                heater.setChanged();
                heater.markContainingBlockForUpdate(null);
            }
        }
    }

    @Override
    public BlockPos getModelOffset(BlockState state, @Nullable Vec3i size) { return new BlockPos(0, dummy, 0); }

    @Override
    public boolean shouldPlaySound(@NotNull String sound) { return active; }
}
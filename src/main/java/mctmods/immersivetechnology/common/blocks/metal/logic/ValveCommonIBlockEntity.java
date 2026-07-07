package mctmods.immersivetechnology.common.blocks.metal.logic;

import java.text.DecimalFormat;
import mctmods.immersivetechnology.common.blocks.helper.*;
import mctmods.immersivetechnology.core.network.ITPacketHandler;
import mctmods.immersivetechnology.core.network.ITOSDRequestMessage;
import mctmods.immersivetechnology.core.util.TranslationKey;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import static mctmods.immersivetechnology.common.blocks.metal.ValveFluidBlock.OPEN;

public abstract class ValveCommonIBlockEntity extends ITIBaseIBlockEntity implements ITIServerTickableBE, ITIClientTickableBE, MenuProvider, ITBlockInterfaces.IDirectionalBE, ITBlockInterfaces.IBlockOverlayText, ITBlockInterfaces.IHammerInteraction {
    final TranslationKey overlayNormal;
    final TranslationKey overlaySneakingFirstLine;
    final TranslationKey overlaySneakingSecondLine;
    final int GuiID;

    private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("#,##0.###");

    public ValveCommonIBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, TranslationKey overlayNormal, TranslationKey overlaySneakingFirstLine, TranslationKey overlaySneakingSecondLine, int GuiID) { super(type, pos, state); this.overlayNormal = overlayNormal; this.overlaySneakingFirstLine = overlaySneakingFirstLine; this.overlaySneakingSecondLine = overlaySneakingSecondLine; this.GuiID = GuiID; this.redstoneMode = 1; }

    public Direction facing = Direction.NORTH;

    public int packetLimit = 0;
    public int timeLimit = 0;
    public int keepSize = 0;
    public byte redstoneMode;

    public long acceptedAmount;
    public long lastAcceptedAmount;
    public int secondCounter;
    public int minuteCounter;
    public long average;
    public long lastAverage;
    public int packets;
    public int packetAverage;
    public int lastPacketAverage;

    public long[] averages = new long[60];
    public long[] packetTotals = new long[60];

    private int requestCooldown = 0;

    public void efficientSetChanged() { setChanged(); }

    public void calculateAverages() {
        long sum = 0;
        for (long avg : averages) { sum += avg; }
        average = sum / 60;
        sum = 0;
        for (long avg : packetTotals) { sum += avg; }
        packetAverage = (int)(sum / 60);
    }

    protected void updateBase() {
        if (level == null || level.isClientSide) return;
        efficientSetChanged();
        if (++secondCounter < 20) return;
        if (average == 0 && acceptedAmount > 0) for (int i = 0; i < 60; i++) { averages[i] = acceptedAmount; }
        if (packetAverage == 0 && packets > 0) for (int i = 0; i < 60; i++) { packetTotals[i] = packets; }
        if (averages[minuteCounter] != acceptedAmount || packetTotals[minuteCounter] != packets) {
            averages[minuteCounter] = acceptedAmount;
            packetTotals[minuteCounter] = packets;
            calculateAverages();
        }
        lastAcceptedAmount = acceptedAmount;
        acceptedAmount = 0;
        packets = 0;
        secondCounter = 0;
        if (++minuteCounter == 60) {
            lastPacketAverage = packetAverage;
            lastAverage = average;
            minuteCounter = 0;
        }
        markContainingBlockForUpdate(null);
    }

    @Override public void tickServer() { updateBase(); }

    @Override public void tickClient() { if (requestCooldown > 0) requestCooldown--; }

    @Override public void onLoad() {
        super.onLoad();
        facing = getBlockState().getValue(ITProperties.FACING_ALL);
    }

    @Override public Component[] getOverlayText(@NotNull Player player, @NotNull HitResult mop, boolean hammer) {
        if (level == null) { return new Component[0]; }
        if (level.isClientSide && requestCooldown == 0) {
            ITPacketHandler.sendToServer(new ITOSDRequestMessage(worldPosition));
            requestCooldown = 20;
        }
        boolean open = getBlockState().getValue(OPEN);
        if (player.isCrouching()) {
            double avg = open ? average / 20.0 : 0;
            int pa = open ? packetAverage : 0;
            String avgStr = NUMBER_FORMAT.format(avg);
            String paStr = NUMBER_FORMAT.format(pa);
            return new Component[] { Component.translatable(overlaySneakingFirstLine.getLocation(), avgStr), Component.translatable(overlaySneakingSecondLine.getLocation(), paStr) };
        } else {
            long la = open ? lastAcceptedAmount : 0;
            String laStr = NUMBER_FORMAT.format(la);
            return new Component[] { Component.translatable(overlayNormal.getLocation(), laStr) };
        }
    }

    @Override public void readCustomNBT(CompoundTag nbt, boolean descPacket) {
        packetLimit = nbt.getInt("packetLimit");
        timeLimit = nbt.getInt("timeLimit");
        keepSize = nbt.getInt("keepSize");
        redstoneMode = nbt.getByte("redstoneMode");
        lastAcceptedAmount = nbt.getLong("lastAcceptedAmount");
        average = nbt.getLong("average");
        packetAverage = nbt.getInt("packetAverage");
        if (!descPacket) {
            acceptedAmount = nbt.getLong("acceptedAmount");
            secondCounter = nbt.getInt("secondCounter");
            minuteCounter = nbt.getInt("minuteCounter") % 60;
            if (nbt.contains("averages", 12)) {
                averages = nbt.getLongArray("averages");
                if (averages.length != 60) averages = new long[60];
            } else if (nbt.contains("averages", 4)) {
                long avg = nbt.getLong("averages");
                for (int i = 0; i < 60; i++) averages[i] = avg;
            } else averages = new long[60];
            if (nbt.contains("packetTotals", 12)) {
                packetTotals = nbt.getLongArray("packetTotals");
                if (packetTotals.length != 60) packetTotals = new long[60];
            } else packetTotals = new long[60];
            calculateAverages();
        }
    }

    @Override public void writeCustomNBT(CompoundTag nbt, boolean descPacket) {
        nbt.putInt("packetLimit", packetLimit);
        nbt.putInt("timeLimit", timeLimit);
        nbt.putInt("keepSize", keepSize);
        nbt.putByte("redstoneMode", redstoneMode);
        nbt.putLong("lastAcceptedAmount", lastAcceptedAmount);
        nbt.putLong("average", average);
        nbt.putInt("packetAverage", packetAverage);
        if (!descPacket) {
            nbt.putLong("acceptedAmount", acceptedAmount);
            nbt.putInt("secondCounter", secondCounter);
            nbt.putInt("minuteCounter", minuteCounter);
            nbt.putLongArray("averages", averages);
            nbt.putLongArray("packetTotals", packetTotals);
        }
    }

    @Override @NotNull public Direction getFacing() { return this.facing; }

    @Override public void setFacing(@NotNull Direction facing) {
        this.facing = facing;
        invalidateCaps();
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            if (state.hasProperty(ITProperties.FACING_ALL)) level.setBlock(worldPosition, state.setValue(ITProperties.FACING_ALL, facing), 3);
            markContainingBlockForUpdate(null);
            for (Direction d : Direction.values()) { level.neighborChanged(worldPosition.relative(d), getBlockState().getBlock(), worldPosition); }
        }
        efficientSetChanged();
    }

    @Override @NotNull public ITPlacementLimitation getFacingLimitation() { return ITPlacementLimitation.SIDE_CLICKED; }

    @Override public boolean mirrorFacingOnPlacement(@NotNull LivingEntity placer) { return false; }

    @Override public boolean canHammerRotate(@NotNull Direction side, @NotNull Vec3 hit, LivingEntity entity) { return false; }

    @Override public boolean hammerUseSide(@NotNull Direction side, @NotNull Player player, @NotNull InteractionHand hand, @NotNull Vec3 hit) {
        if (level == null || level.isClientSide) return false;
        boolean counter = player.isShiftKeyDown() != (side == Direction.DOWN);
        Direction oldFacing = facing;
        Direction newFacing = counter ? oldFacing.getCounterClockWise(side.getAxis()) : oldFacing.getClockWise(side.getAxis());
        setFacing(newFacing);
        return true;
    }

    public int getRSPower() {
        if (level == null) return 0;
        return level.getBestNeighborSignal(worldPosition);
    }

    public void updateRedstoneState() {
        if (redstoneMode == 0) return;
        int rs = getRSPower();
        boolean shouldOpen = (redstoneMode == 1 ? rs == 0 : rs > 0);
        BlockState state = getBlockState();
        if (state.getValue(OPEN) != shouldOpen) {
            if (level != null) level.setBlock(worldPosition, state.setValue(OPEN, shouldOpen), 3);
            markContainingBlockForUpdate(null);
        }
    }

    @Override public void receiveMessageFromClient(CompoundTag nbt) {
        packetLimit = nbt.getInt("packetLimit");
        timeLimit = nbt.getInt("timeLimit");
        keepSize = nbt.getInt("keepSize");
        efficientSetChanged();
        markContainingBlockForUpdate(null);
    }

    public static int longToInt(long value) { return value > Integer.MAX_VALUE ? Integer.MAX_VALUE : value < Integer.MIN_VALUE ? Integer.MIN_VALUE : (int) value; }

    public abstract AbstractContainerMenu createMenu(int id, @NotNull Inventory playerInventory, @NotNull Player player);

    @NotNull public abstract Component getDisplayName();

    public abstract boolean stillValid(Player player);
}

package mctmods.immersivetechnology.common.blocks.metal.logic;

import java.text.DecimalFormat;
import mctmods.immersivetechnology.common.blocks.helper.ITIBaseIBlockEntity;
import mctmods.immersivetechnology.common.blocks.helper.ITBlockInterfaces;
import mctmods.immersivetechnology.common.blocks.helper.ITIServerTickableBE;
import mctmods.immersivetechnology.common.blocks.helper.ITIClientTickableBE;
import mctmods.immersivetechnology.core.network.ITPacketHandler;
import mctmods.immersivetechnology.core.network.ITOSDRequestMessage;
import mctmods.immersivetechnology.core.util.TranslationKey;
import mctmods.immersivetechnology.core.ITClientConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;

public abstract class OSDCommonIBlockEntity extends ITIBaseIBlockEntity implements ITIServerTickableBE, ITIClientTickableBE, ITBlockInterfaces.IBlockOverlayText {
    public long acceptedAmount = 0;
    public long lastAcceptedAmount = 0;
    public int secondCounter = 0;
    public int requestCooldown = 0;

    private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("#,##0.###");

    protected OSDCommonIBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) { super(type, pos, state); }

    @Override public void readCustomNBT(CompoundTag nbt, boolean descPacket) {
        if (descPacket) { lastAcceptedAmount = nbt.getLong("lastAcceptedAmount"); }
    }

    @Override public void writeCustomNBT(CompoundTag nbt, boolean descPacket) {
        if (descPacket) { nbt.putLong("lastAcceptedAmount", lastAcceptedAmount); }
    }

    @Override public void tickServer() {
        if (++secondCounter < 20) { return; }
        lastAcceptedAmount = acceptedAmount;
        acceptedAmount = 0;
        secondCounter = 0;
        markContainingBlockForUpdate(null);
    }

    @Override public void tickClient() { if (requestCooldown > 0) { requestCooldown--; } }

    abstract public TranslationKey text();

    @Override public Component[] getOverlayText(@NotNull Player player, @NotNull HitResult mop, boolean hammer) {
        if (level == null) { return new Component[0]; }
        if (level.isClientSide && requestCooldown == 0) {
            ITPacketHandler.sendToServer(new ITOSDRequestMessage(worldPosition));
            requestCooldown = 20;
        }
        double rawValue = ITClientConfig.perTickTrashCans ? (double)lastAcceptedAmount / 20.0 : lastAcceptedAmount;
        String valueStr = NUMBER_FORMAT.format(rawValue);
        return new Component[] { Component.translatable(text().getLocation(), valueStr) };
    }
}

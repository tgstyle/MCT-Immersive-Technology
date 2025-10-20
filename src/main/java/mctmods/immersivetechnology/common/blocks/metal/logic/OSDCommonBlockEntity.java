package mctmods.immersivetechnology.common.blocks.metal.logic;

import mctmods.immersivetechnology.common.blocks.helper.ITBaseBlockEntity;
import mctmods.immersivetechnology.common.blocks.helper.ITBlockInterfaces;
import mctmods.immersivetechnology.common.blocks.helper.ITServerTickableBE;
import mctmods.immersivetechnology.common.blocks.helper.ITClientTickableBE;
import mctmods.immersivetechnology.common.network.ITPacketHandler;
import mctmods.immersivetechnology.common.network.ITOSDRequestMessage;
import mctmods.immersivetechnology.common.util.TranslationKey;
import mctmods.immersivetechnology.core.ITClientConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;

public abstract class OSDCommonBlockEntity extends ITBaseBlockEntity implements ITServerTickableBE, ITClientTickableBE, ITBlockInterfaces.IBlockOverlayText {
    public long acceptedAmount = 0;
    public long lastAcceptedAmount = 0;
    public int secondCounter = 0;
    public int requestCooldown = 0;

    protected OSDCommonBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) { super(type, pos, state); }

    @Override
    public void readCustomNBT(CompoundTag nbt, boolean descPacket) {
        if (descPacket) { lastAcceptedAmount = nbt.getLong("lastAcceptedAmount"); }
    }

    @Override
    public void writeCustomNBT(CompoundTag nbt, boolean descPacket) {
        if (descPacket) { nbt.putLong("lastAcceptedAmount", lastAcceptedAmount); }
    }

    @Override
    public void tickServer() {
        if (++secondCounter < 20) { return; }
        lastAcceptedAmount = acceptedAmount;
        acceptedAmount = 0;
        secondCounter = 0;
    }

    @Override
    public void tickClient() { if (requestCooldown > 0) { requestCooldown--; } }

    abstract public TranslationKey text();

    @Override
    public Component[] getOverlayText(@NotNull Player player, @NotNull HitResult mop, boolean hammer) {
        assert level != null;
        if (level.isClientSide && requestCooldown == 0) {
            ITPacketHandler.sendToServer(new ITOSDRequestMessage(worldPosition));
            requestCooldown = 20;
        }
        float value = ITClientConfig.perTickTrashCans ? (float)lastAcceptedAmount / 20 : lastAcceptedAmount;
        return new Component[] { Component.literal(text().format(value)) };
    }

    @Override
    public boolean useNixieFont(@NotNull Player player, @NotNull HitResult mop) { return false; }
}

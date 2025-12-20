package mctmods.immersivetechnology.common.shared.tileentities;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockOverlayText;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import mctmods.immersivetechnology.common.Config.ITConfig.Settings;
import mctmods.immersivetechnology.common.util.TranslationKey;
import mctmods.immersivetechnology.common.util.network.BinaryMessageTileSync;
import mctmods.immersivetechnology.common.util.network.IBinaryMessageReceiver;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.RayTraceResult;

import javax.annotation.Nonnull;

public abstract class TileEntityCommonOSD extends TileEntityIEBase implements ITickable, IBlockOverlayText, IBinaryMessageReceiver {
    public long acceptedAmount = 0;
    public long lastAcceptedAmount = 0;
    public int secondCounter = 0;

    public void efficientMarkDirty() {
        if (world != null) world.getChunk(this.getPos()).markDirty();
    }

    @Override
    public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { lastAcceptedAmount = nbt.getLong("lastAcceptedAmount"); }

    @Override
    public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { nbt.setLong("lastAcceptedAmount", lastAcceptedAmount); }

    @Override
    public void onLoad() {
        if (!world.isRemote) this.markContainingBlockForUpdate(null);
    }

    @Override
    public void update() {
        if (world.isRemote) {
            if (requestCooldown > 0) { requestCooldown--; }
            return;
        }
        if (++secondCounter < 20) { return; }
        lastAcceptedAmount = acceptedAmount;
        acceptedAmount = 0;
        secondCounter = 0;
        efficientMarkDirty();
        this.markContainingBlockForUpdate(null);
    }

    abstract public TranslationKey text();

    public int requestCooldown = 0;

    @Override
    public @Nonnull String[] getOverlayText(@Nonnull EntityPlayer player, @Nonnull RayTraceResult mop, boolean hammer) {
        if (requestCooldown == 0) {
            ByteBuf message = Unpooled.copyBoolean(true);
            BinaryMessageTileSync.sendToServer(getPos(), message);
            requestCooldown = 20;
        }
        double value = Settings.experimental.per_tick_trash_cans ? lastAcceptedAmount / 20.0 : lastAcceptedAmount;
        String formattedValue = String.format("%.0f", value);
        return new String[]{ text().format(formattedValue) };
    }

    @Override
    public void receiveMessageFromClient(ByteBuf buf, EntityPlayerMP player) {
        ByteBuf message = Unpooled.copyLong(lastAcceptedAmount);
        BinaryMessageTileSync.sendToPlayer(player, getPos(), message);
    }

    @Override
    public void receiveMessageFromServer(ByteBuf buf) { lastAcceptedAmount = buf.readLong(); }

    @Override
    public boolean useNixieFont(@Nonnull EntityPlayer player, @Nonnull RayTraceResult mop) { return false; }
}

package mctmods.immersivetechnology.common.shared.tileentities;

import com.immersiveconvergence.api.network.BinaryTileSyncMessage;
import com.immersiveconvergence.api.network.IBinaryMessageReceiver;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockOverlayText;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import mctmods.immersivetechnology.common.Config.ITConfig.Settings;
import mctmods.immersivetechnology.common.util.TranslationKey;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.RayTraceResult;

import javax.annotation.Nonnull;

import java.text.DecimalFormat;

public abstract class TileEntityCommonOSD extends TileEntityIEBase implements ITickable, IBlockOverlayText, IBinaryMessageReceiver {
    private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("#,##0.###");
    public long acceptedAmount = 0;
    public long lastAcceptedAmount = 0;
    public int secondCounter = 0;

    public void efficientMarkDirty() {
        if (world != null) world.getChunk(this.getPos()).markDirty();
    }

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { lastAcceptedAmount = nbt.getLong("lastAcceptedAmount"); }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { nbt.setLong("lastAcceptedAmount", lastAcceptedAmount); }

    @Override public void onLoad() {
        if (world != null && world.isRemote) {
            markContainingBlockForUpdate(null);
        }
    }

    @Override public void update() {
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

    protected String formattedAmount() {
        double value = Settings.experimental.per_tick_trash_cans ? lastAcceptedAmount / 20.0 : lastAcceptedAmount;
        return NUMBER_FORMAT.format(value);
    }

    protected void requestOverlaySync() {
        if (requestCooldown > 0) { return; }
        BinaryTileSyncMessage.sendToServer(getPos(), Unpooled.copyBoolean(true));
        requestCooldown = 20;
    }

    public int requestCooldown = 0;

    @Override @Nonnull public String[] getOverlayText(@Nonnull EntityPlayer player, @Nonnull RayTraceResult mop, boolean hammer) {
        requestOverlaySync();
        return new String[]{ text().format(formattedAmount()) };
    }

    @Override public void receiveMessageFromClient(ByteBuf buf, EntityPlayerMP player) {
        ByteBuf message = Unpooled.copyLong(lastAcceptedAmount);
        BinaryTileSyncMessage.sendToPlayer(player, getPos(), message);
    }

    @Override public void receiveMessageFromServer(ByteBuf buf) { lastAcceptedAmount = buf.readLong(); }

    @Override public boolean useNixieFont(@Nonnull EntityPlayer player, @Nonnull RayTraceResult mop) { return false; }
}

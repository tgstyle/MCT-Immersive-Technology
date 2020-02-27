package mctmods.immersivetechnology.common.blocks.metal.tileentities;


import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.common.util.network.MessageTileSync;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fml.common.network.NetworkRegistry;

public abstract class TileEntityGenericTrash extends TileEntityIEBase implements ITickable, IEBlockInterfaces.IBlockOverlayText {

    public EnumFacing facing = EnumFacing.NORTH;

    public int acceptedAmount = 0;
    public int lastAcceptedAmount = 0;
    public int perSecond = 0;
    public int lastPerSecond = 0;
    public int updateClient = 0;

    public void efficientMarkDirty() { // !!!!!!! only use it within update() function !!!!!!!
        world.getChunkFromBlockCoords(this.getPos()).markDirty();
    }

    @Override
    public void update() {
        if(world.isRemote) return;
        if(++updateClient < 20) return;
        if (acceptedAmount != lastAcceptedAmount || lastPerSecond != perSecond) {
            efficientMarkDirty();
            notifyNearbyClients();
        }
        lastAcceptedAmount = acceptedAmount;
        lastPerSecond = perSecond;
        acceptedAmount = 0;
        perSecond = 0;
        updateClient = 0;
    }

    @Override
    public void readCustomNBT(NBTTagCompound nbt, boolean descPacket) {
        lastAcceptedAmount = acceptedAmount = nbt.getInteger("acceptedAmount");
        lastPerSecond = perSecond = nbt.getInteger("perSecond");
    }

    @Override
    public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket) {
        nbt.setInteger("acceptedAmount", acceptedAmount);
        nbt.setInteger("perSecond", perSecond);
    }

    @Override
    public void receiveMessageFromServer(NBTTagCompound message) {
        readCustomNBT(message, false);
    }

    public void notifyNearbyClients() {
        NBTTagCompound tag = new NBTTagCompound();
        BlockPos center = getPos();
        writeCustomNBT(tag, false);
        ImmersiveTechnology.packetHandler.sendToAllTracking(new MessageTileSync(this, tag), new NetworkRegistry.TargetPoint(world.provider.getDimension(), center.getX(), center.getY(), center.getZ(), 0));
    }

    @Override
    public boolean useNixieFont(EntityPlayer player, RayTraceResult mop) {
        return false;
    }

}

package mctmods.immersivetechnology.common.blocks.metal.tileentities;

import blusunrize.immersiveengineering.api.fluid.IFluidPipe;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityFluidPipe;
import blusunrize.immersiveengineering.common.util.Utils;

import mctmods.immersivetechnology.common.util.ITUtils;
import mctmods.immersivetechnology.common.Config.ITConfig.Settings;
import mctmods.immersivetechnology.common.ITContent;
import mctmods.immersivetechnology.common.util.ITIPipe;
import mctmods.immersivetechnology.mixin.MixinIETileEntityFluidPipe;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class TileEntityFluidPipeAlternative extends TileEntityFluidPipe implements ITIPipe, IEBlockInterfaces.IPlacementInteraction, IEBlockInterfaces.ITileDrop, IEBlockInterfaces.IColouredTile {
    private EnumDyeColor color = null;
    private final int transferRate = Settings.experimental.pipe_transfer_rate;
    private final int transferRatePressurized = Settings.experimental.pipe_pressurized_transfer_rate;
    private final PipeFluidHandler[] sidedHandlers = {new PipeFluidHandler(EnumFacing.DOWN), new PipeFluidHandler(EnumFacing.UP), new PipeFluidHandler(EnumFacing.NORTH), new PipeFluidHandler(EnumFacing.SOUTH), new PipeFluidHandler(EnumFacing.WEST), new PipeFluidHandler(EnumFacing.EAST)};
    public static HashMap<BlockPos, List<ITDirectionalFluidOutput>> indirectConnections = new HashMap<>();

    public EnumDyeColor getColor() { return color; }
    public void setColor(EnumDyeColor color) { this.color = color; }

    @SuppressWarnings("unchecked")
    @Override
    public @Nonnull <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing != null && (sideConfig[facing.ordinal()] == 0 || sideConfig[facing.ordinal()] == 1)) { return (T) sidedHandlers[facing.ordinal()]; }
        return super.getCapability(capability, facing);
    }

    @Override
    public void onLoad() {
        boolean changed = false;
        for (EnumFacing f : EnumFacing.VALUES) { if (world.isBlockLoaded(pos.offset(f))) { changed |= updateConnectionByte(f); } }
        if (changed || world.isRemote) { markContainingBlockForUpdate(null); }
        if (world.isRemote) {
            world.addBlockEvent(pos, getBlockType(), 0, 0);
            world.markBlockRangeForRenderUpdate(pos, pos);
        }
        if (!world.isRemote) {
            invalidateNetworkCache(pos);
            world.notifyNeighborsOfStateChange(getPos(), world.getBlockState(getPos()).getBlock(), true);
            if (changed) {
                markDirty();
                SPacketUpdateTileEntity pkt = getUpdatePacket();
                PlayerChunkMapEntry entry = ((WorldServer)world).getPlayerChunkMap().getEntry(pos.getX() >> 4, pos.getZ() >> 4);
                if (entry != null) { entry.sendPacket(pkt); }
            }
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (!world.isRemote) { invalidateNetworkCache(pos); }
    }

    @Override
    public void onNeighborBlockChange(@Nonnull BlockPos otherPos) {
        EnumFacing dir = EnumFacing.getFacingFromVector(otherPos.getX() - pos.getX(), otherPos.getY() - pos.getY(), otherPos.getZ() - pos.getZ());
        boolean changed = updateConnectionByte(dir);
        if (changed) {
            ITUtils.improvedMarkBlockForUpdate(world, pos, null, EnumSet.complementOf(EnumSet.of(dir)));
            invalidateNetworkCache(pos);
            markContainingBlockForUpdate(null);
            if (!world.isRemote) {
                markDirty();
                SPacketUpdateTileEntity pkt = getUpdatePacket();
                PlayerChunkMapEntry entry = ((WorldServer)world).getPlayerChunkMap().getEntry(pos.getX() >> 4, pos.getZ() >> 4);
                if (entry != null) { entry.sendPacket(pkt); }
            }
        }
    }

    public void invalidateNetworkCache(BlockPos node) {
        LinkedList<BlockPos> openList = new LinkedList<>();
        HashSet<BlockPos> closedSet = new HashSet<>();
        openList.add(node);
        closedSet.add(node);
        while (!openList.isEmpty() && closedSet.size() < 1024) {
            BlockPos next = openList.poll();
            TileEntity pipeTile = Utils.getExistingTileEntity(world, next);
            if (pipeTile instanceof TileEntityFluidPipeAlternative) { indirectConnections.remove(next); }
            if (pipeTile instanceof TileEntityFluidPipe) {
                MixinIETileEntityFluidPipe mixin = (MixinIETileEntityFluidPipe) pipeTile;
                for (int i = 0; i < 6; i++) {
                    EnumFacing fd = EnumFacing.byIndex(i);
                    if ((mixin.getConnections() & (1 << i)) != 0) {
                        BlockPos nextPos = next.offset(fd);
                        TileEntity adjacentTile = Utils.getExistingTileEntity(world, nextPos);
                        if (adjacentTile instanceof TileEntityFluidPipe && !closedSet.contains(nextPos)) {
                            openList.add(nextPos);
                            closedSet.add(nextPos);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        if (nbt.hasKey("color")) { color = EnumDyeColor.byMetadata(nbt.getInteger("color")); }
        else { color = null; }
    }

    @Override
    public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        if (color != null) { nbt.setInteger("color", color.getMetadata()); }
    }

    @Override
    public void readOnPlacement(@Nullable EntityLivingBase placer, ItemStack stack) {
        color = null;
        if (stack.hasTagCompound()) { assert stack.getTagCompound() != null; color = EnumDyeColor.byMetadata(stack.getTagCompound().getInteger("color")); }
        boolean changed = false;
        for (EnumFacing f : EnumFacing.VALUES) { changed |= updateConnectionByte(f); }
        if (changed) { invalidateNetworkCache(pos); }
        markDirty();
        markContainingBlockForUpdate(null);
        if (!world.isRemote && changed) {
            SPacketUpdateTileEntity pkt = getUpdatePacket();
            PlayerChunkMapEntry entry = ((WorldServer)world).getPlayerChunkMap().getEntry(pos.getX() >> 4, pos.getZ() >> 4);
            if (entry != null) { entry.sendPacket(pkt); }
        }
        if (!world.isRemote) {
            for (EnumFacing f : EnumFacing.VALUES) {
                TileEntity te = world.getTileEntity(pos.offset(f));
                if (te instanceof TileEntityFluidPipeAlternative) { ((TileEntityFluidPipeAlternative)te).onNeighborBlockChange(pos); }
                ((WorldServer)world).getPlayerChunkMap().markBlockForUpdate(pos.offset(f));
            }
        }
    }

    @Override
    public void onTilePlaced(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EnumFacing side, float hitX, float hitY, float hitZ, @Nonnull EntityLivingBase placer, @Nonnull ItemStack stack) {
        // Removed toggle loop to prevent forced connections on placement for dissimilar colors/plain pipes
    }

    @Override
    public void onDataPacket(@Nonnull NetworkManager net, @Nonnull SPacketUpdateTileEntity pkt) {
        super.onDataPacket(net, pkt);
        if (world.isRemote) { world.markBlockRangeForRenderUpdate(pos, pos); }
    }

    @Override
    public boolean interact(@Nonnull EnumFacing side, @Nonnull EntityPlayer player, @Nonnull EnumHand hand, @Nonnull ItemStack heldItem, float hitX, float hitY, float hitZ) {
        int heldDye = Utils.getDye(heldItem);
        if (heldDye != -1) {
            EnumDyeColor newColor = EnumDyeColor.byDyeDamage(heldDye);
            if (color != newColor) {
                color = newColor;
                boolean changed = false;
                byte conns = getAvailableConnectionByte();
                for (int i = 0; i < 6; i++) { if ((conns & (1 << i)) != 0) { changed |= updateConnectionByte(EnumFacing.byIndex(i)); } }
                if (changed) { invalidateNetworkCache(pos); }
            }
            markDirty();
            markContainingBlockForUpdate(null);
            if (!world.isRemote) {
                SPacketUpdateTileEntity pkt = getUpdatePacket();
                PlayerChunkMapEntry entry = ((WorldServer)world).getPlayerChunkMap().getEntry(pos.getX() >> 4, pos.getZ() >> 4);
                if (entry != null) { entry.sendPacket(pkt); }
                for (EnumFacing f : EnumFacing.VALUES) {
                    TileEntity te = world.getTileEntity(pos.offset(f));
                    if (te instanceof TileEntityFluidPipeAlternative) {
                        ((TileEntityFluidPipeAlternative)te).onNeighborBlockChange(pos);
                        SPacketUpdateTileEntity neighborPkt = te.getUpdatePacket();
                        PlayerChunkMapEntry neighborEntry = ((WorldServer)world).getPlayerChunkMap().getEntry(te.getPos().getX() >> 4, te.getPos().getZ() >> 4);
                        if (neighborEntry != null) { neighborEntry.sendPacket(neighborPkt); }
                    }
                    ((WorldServer)world).getPlayerChunkMap().markBlockForUpdate(pos.offset(f));
                }
                if (!player.isCreative()) { heldItem.shrink(1); }
            }
            return true;
        }
        return super.interact(side, player, hand, heldItem, hitX, hitY, hitZ);
    }

    @Override
    public void toggleSide(int side) {
        EnumFacing fd = EnumFacing.byIndex(side);
        BlockPos otherPos = pos.offset(fd);
        TileEntity te = world.getTileEntity(otherPos);
        int oldConfig = sideConfig[side];
        sideConfig[side]++;
        if (te instanceof TileEntityFluidPipe) {
            if (sideConfig[side] > 1) { sideConfig[side] = -1; }
            EnumFacing opp = fd.getOpposite();
            TileEntityFluidPipe other = (TileEntityFluidPipe) te;
            other.sideConfig[opp.ordinal()] = sideConfig[side];
            other.markDirty();
            boolean otherChanged = other.updateConnectionByte(opp);
            if (otherChanged && other instanceof TileEntityFluidPipeAlternative) { ((TileEntityFluidPipeAlternative)other).invalidateNetworkCache(other.getPos()); }
            world.notifyBlockUpdate(otherPos, world.getBlockState(otherPos), world.getBlockState(otherPos), 3);
        } else { if (sideConfig[side] > 0) { sideConfig[side] = -1; } }
        markDirty();
        boolean changed = updateConnectionByte(fd);
        if (oldConfig != sideConfig[side] || changed) { invalidateNetworkCache(pos); }
        markContainingBlockForUpdate(null);
        world.addBlockEvent(getPos(), getBlockType(), 0, 0);
    }

    @Override
    public boolean receiveClientEvent(int id, int arg) { return super.receiveClientEvent(id, arg); }

    @Override
    public void onEntityCollision(@Nonnull World world, @Nonnull Entity entity) { super.onEntityCollision(world, entity); }

    public void neighborPipeRemoved(EnumFacing direction) { onNeighborBlockChange(pos.offset(direction)); }

    @Override
    public boolean hasCover() { return !pipeCover.isEmpty(); }

    @Override
    public @Nonnull ItemStack getTileDrop(@Nullable EntityPlayer player, IBlockState state) {
        ItemStack stack = new ItemStack(state.getBlock(), 1, state.getBlock().damageDropped(state));
        if (color != null) {
            NBTTagCompound nbt = stack.getTagCompound();
            if (nbt == null) { nbt = new NBTTagCompound(); }
            nbt.setInteger("color", color.getMetadata());
            stack.setTagCompound(nbt);
        }
        return stack;
    }

    @Override
    public boolean canOutputPressurized(boolean consumePower) { return true; }

    @Override
    public int[] getSideConfig() { return sideConfig; }

    protected boolean isFluidRelated(TileEntity te, EnumFacing side) {
        if (te instanceof TileEntityFluidPipe) {
            EnumDyeColor other = (te instanceof TileEntityFluidPipeAlternative) ? ((TileEntityFluidPipeAlternative) te).getColor() : null;
            return color == other;
        }
        return te.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side.getOpposite()) || te instanceof IFluidPipe;
    }

    @Override
    public boolean updateConnectionByte(@Nonnull EnumFacing side) {
        if (world.isRemote) { return false; }
        MixinIETileEntityFluidPipe mixin = (MixinIETileEntityFluidPipe) this;
        byte old = mixin.getConnections();
        int i = side.ordinal();
        if (sideConfig[i] == -1) { mixin.setConnections((byte) (old & ~(1 << i))); }
        else {
            TileEntity con = Utils.getExistingTileEntity(world, getPos().offset(side));
            boolean related = con != null && isFluidRelated(con, side);
            if (related || sideConfig[i] == 1) { mixin.setConnections((byte) (old | (1 << i))); }
            else { mixin.setConnections((byte) (old & ~(1 << i))); }
        }
        return old != mixin.getConnections();
    }

    @Override
    public boolean hasOutputConnection(EnumFacing side) {
        int i = side.ordinal();
        return sideConfig[i] == 0 || sideConfig[i] == 1;
    }

    @Override
    public byte getAvailableConnectionByte() { return ((MixinIETileEntityFluidPipe)this).getConnections(); }

    @Override
    public int getConnectionStyle(int connection) {
        if (sideConfig[connection] == -1) { return 0; }
        byte conns = getAvailableConnectionByte();
        if ((conns & (1 << connection)) == 0) { return 0; }
        if (conns != 3 && conns != 12 && conns != 48) { return 1; }
        TileEntity con = world.getTileEntity(getPos().offset(EnumFacing.byIndex(connection)));
        if (con instanceof TileEntityFluidPipe) {
            EnumDyeColor neighborColor = (con instanceof TileEntityFluidPipeAlternative) ? ((TileEntityFluidPipeAlternative)con).color : null;
            if (color != neighborColor) { return 1; }
            byte tileConns = ((TileEntityFluidPipe) con).getAvailableConnectionByte();
            if (conns == tileConns) { return 0; }
        }
        return 1;
    }

    public static List<ITDirectionalFluidOutput> getITConnectedFluidHandlers(BlockPos node, World world) {
        if (indirectConnections.containsKey(node)) {
            List<ITDirectionalFluidOutput> res = indirectConnections.get(node);
            if (res.isEmpty()) { return res; }
            boolean valid = true;
            for (ITDirectionalFluidOutput sample : res) {
                TileEntity te = sample.containingTile;
                BlockPos samplePos = te.getPos();
                if (te.getWorld() != world || te.isInvalid() || te != Utils.getExistingTileEntity(world, samplePos)) {
                    valid = false;
                    break;
                }
            }
            if (valid) { return res; }
            indirectConnections.remove(node);
        }
        LinkedList<BlockPos> openList = new LinkedList<>();
        Set<BlockPos> visited = new HashSet<>();
        Map<BlockPos, Integer> pipeDist = new HashMap<>();
        openList.add(node);
        visited.add(node);
        pipeDist.put(node, 0);
        Map<BlockPos, DistOutput> handlerMap = new HashMap<>();
        while (!openList.isEmpty()) {
            BlockPos next = openList.pollFirst();
            int currentDist = pipeDist.get(next);
            TileEntity pipeTile = Utils.getExistingTileEntity(world, next);
            if (pipeTile instanceof TileEntityFluidPipe) {
                MixinIETileEntityFluidPipe mixin = (MixinIETileEntityFluidPipe) pipeTile;
                for (int i = 0; i < 6; i++) {
                    EnumFacing fd = EnumFacing.byIndex(i);
                    if ((mixin.getConnections() & (1 << i)) != 0) {
                        BlockPos nextPos = next.offset(fd);
                        TileEntity adjacentTile = Utils.getExistingTileEntity(world, nextPos);
                        if (adjacentTile != null) {
                            if (adjacentTile instanceof TileEntityFluidPipe) { if (visited.add(nextPos)) { openList.addLast(nextPos); pipeDist.put(nextPos, currentDist + 1); } }
                            else if (adjacentTile.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, fd.getOpposite())) {
                                IFluidHandler handler = adjacentTile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, fd.getOpposite());
                                if (handler != null) {
                                    IFluidTankProperties[] tankInfo = handler.getTankProperties();
                                    if (tankInfo != null && tankInfo.length > 0) {
                                        DistOutput existing = handlerMap.get(nextPos);
                                        int dist = currentDist + 1;
                                        if (existing == null || dist < existing.dist) {
                                            ITDirectionalFluidOutput out = new ITDirectionalFluidOutput(handler, adjacentTile, fd);
                                            handlerMap.put(nextPos, new DistOutput(out, dist));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        List<DistOutput> tempList = new ArrayList<>(handlerMap.values());
        tempList.sort(Comparator.comparingInt(o -> o.dist));
        List<ITDirectionalFluidOutput> fluidHandlers = tempList.stream().map(d -> d.out).collect(Collectors.toList());
        indirectConnections.put(node, fluidHandlers);
        return fluidHandlers;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public @Nonnull List<BakedQuad> modifyQuads(@Nonnull IBlockState object, @Nonnull List<BakedQuad> quads) {
        List<BakedQuad> modified = super.modifyQuads(object, quads);
        List<BakedQuad> newQuads = new ArrayList<>();
        for (BakedQuad quad : modified) {
            int newTint = quad.getTintIndex();
            if (color != null && newTint == -1) { newTint = 0; }
            newQuads.add(new BakedQuad(quad.getVertexData(), newTint, quad.getFace(), quad.getSprite(), quad.shouldApplyDiffuseLighting(), quad.getFormat()));
        }
        return newQuads;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public @Nonnull String getCacheKey(@Nonnull IBlockState object) { return getRenderCacheKey(); }

    @SideOnly(Side.CLIENT)
    @Override
    public @Nonnull Optional<TRSRTransformation> applyTransformations(@Nonnull IBlockState object, @Nonnull String group, @Nonnull Optional<TRSRTransformation> transform) { return super.applyTransformations(object, group, transform); }

    @SideOnly(Side.CLIENT)
    @Override
    public int getRenderColour(int tintIndex) { return color != null ? (color == EnumDyeColor.WHITE ? 0xffffffff : color.getColorValue() | 0xff000000) : 0xffffffff; }

    @SideOnly(Side.CLIENT)
    @Override
    public int getRenderColour(@Nonnull IBlockState object, @Nonnull String group) { return color != null ? (color == EnumDyeColor.WHITE ? 0xffffffff : color.getColorValue() | 0xff000000) : 0xffffffff; }

    @SideOnly(Side.CLIENT)
    @SuppressWarnings("deprecation")
    @Override
    public @Nonnull OBJModel.OBJState getOBJState() { return getStateFromKey(getRenderCacheKey()); }

    protected String getRenderCacheKey() {
        StringBuilder key = new StringBuilder();
        byte conns = getAvailableConnectionByte();
        for (int i = 0; i < 6; i++) { if ((conns & (1 << i)) != 0) { key.append(getConnectionStyle(i) == 1 ? "2" : "1"); } else { key.append("0"); } }
        if (!pipeCover.isEmpty()) { key.append("scaf:").append(pipeCover); }
        key.append(color);
        return key.toString();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public @Nonnull AxisAlignedBB getRenderBoundingBox() {
        if (!pipeCover.isEmpty()) { return new AxisAlignedBB(pos); }
        double minX = 0.25, maxX = 0.75, minY = 0.25, maxY = 0.75, minZ = 0.25, maxZ = 0.75;
        byte conns = getAvailableConnectionByte();
        for (int i = 0; i < 6; i++) {
            if ((conns & (1 << i)) != 0) {
                switch (EnumFacing.byIndex(i)) {
                    case DOWN: minY = 0; break;
                    case UP: maxY = 1; break;
                    case NORTH: minZ = 0; break;
                    case SOUTH: maxZ = 1; break;
                    case WEST: minX = 0; break;
                    case EAST: maxX = 1; break;
                }
            }
        }
        return new AxisAlignedBB(pos.getX() + minX, pos.getY() + minY, pos.getZ() + minZ, pos.getX() + maxX, pos.getY() + maxY, pos.getZ() + maxZ);
    }

    @Override
    public @Nonnull List<AxisAlignedBB> getAdvancedColisionBounds() { return super.getAdvancedColisionBounds(); }

    @Override
    public @Nonnull List<AxisAlignedBB> getAdvancedSelectionBounds() { return super.getAdvancedSelectionBounds(); }

    @Override
    public boolean isOverrideBox(@Nonnull AxisAlignedBB box, @Nonnull EntityPlayer player, @Nonnull RayTraceResult mop, @Nonnull ArrayList<AxisAlignedBB> list) { return super.isOverrideBox(box, player, mop, list); }

    static class DistOutput {
        ITDirectionalFluidOutput out;
        int dist;
        DistOutput(ITDirectionalFluidOutput o, int d) { this.out = o; this.dist = d; }
    }

    public static class ITDirectionalFluidOutput {
        IFluidHandler output;
        EnumFacing direction;
        TileEntity containingTile;

        public ITDirectionalFluidOutput(IFluidHandler output, TileEntity containingTile, EnumFacing direction) {
            this.output = output;
            this.direction = direction;
            this.containingTile = containingTile;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) { return true; }
            if (!(o instanceof ITDirectionalFluidOutput)) { return false; }
            ITDirectionalFluidOutput other = (ITDirectionalFluidOutput) o;
            return containingTile.getPos().equals(other.containingTile.getPos()) && direction == other.direction;
        }

        @Override
        public int hashCode() { return containingTile.getPos().hashCode() * 31 + direction.hashCode(); }
    }

    class PipeFluidHandler implements IFluidHandler {
        EnumFacing origin;
        private ITDirectionalFluidOutput lastSuccessfulOutput;

        public PipeFluidHandler(EnumFacing facing) { this.origin = facing; }

        @Override
        public IFluidTankProperties[] getTankProperties() { return new IFluidTankProperties[]{new FluidTankProperties(null, transferRatePressurized, true, false)}; }

        private int fastFill(FluidStack resource, boolean doFill) {
            int remaining = resource.amount;
            FluidStack fillStack = resource.copy();
            List<ITDirectionalFluidOutput> outputList = getITConnectedFluidHandlers(pos, world);
            if (outputList.isEmpty()) { return 0; }
            BlockPos ccFrom = pos.offset(origin);
            ArrayList<ITDirectionalFluidOutput> candidates = new ArrayList<>();
            for (ITDirectionalFluidOutput output : outputList) {
                BlockPos cc = output.containingTile.getPos();
                if (!cc.equals(ccFrom) && world.isBlockLoaded(cc) && !TileEntityFluidPipeAlternative.this.equals(output.containingTile)) {
                    fillStack.amount = remaining;
                    FluidStack tempStack = fillStack.copy();
                    if (!(output.containingTile instanceof IFluidPipe)) { if (tempStack.tag != null) { tempStack.tag.removeTag("pressurized"); if (tempStack.tag.isEmpty()) { tempStack.tag = null; } } }
                    int temp = output.output.fill(tempStack, false);
                    if (temp > 0) { candidates.add(output); }
                }
            }
            if (candidates.isEmpty()) { return 0; }
            int f = 0;
            if (Settings.experimental.pipe_last_served) {
                ArrayList<ITDirectionalFluidOutput> toTry = new ArrayList<>(candidates);
                if (lastSuccessfulOutput != null && toTry.contains(lastSuccessfulOutput)) {
                    toTry.remove(lastSuccessfulOutput);
                    toTry.add(0, lastSuccessfulOutput);
                }
                for (ITDirectionalFluidOutput output : toTry) {
                    fillStack.amount = remaining;
                    FluidStack tempStack = fillStack.copy();
                    if (!(output.containingTile instanceof IFluidPipe)) { if (tempStack.tag != null) { tempStack.tag.removeTag("pressurized"); if (tempStack.tag.isEmpty()) { tempStack.tag = null; } } }
                    int r = output.output.fill(tempStack, doFill);
                    if (r > 0 && doFill) { lastSuccessfulOutput = output; }
                    f += r;
                    remaining -= r;
                    if (remaining <= 0) { break; }
                }
            } else {
                List<ITDirectionalFluidOutput> active = new ArrayList<>(candidates);
                while (remaining > 0 && !active.isEmpty()) {
                    int num = active.size();
                    int per = remaining / num;
                    int rem = remaining % num;
                    List<ITDirectionalFluidOutput> nextActive = new ArrayList<>();
                    for (ITDirectionalFluidOutput output : active) {
                        int offer = per + (rem > 0 ? 1 : 0);
                        if (offer > 0) { rem--; }
                        if (offer == 0) {
                            nextActive.add(output);
                            continue;
                        }
                        FluidStack tempStack = fillStack.copy();
                        tempStack.amount = offer;
                        if (!(output.containingTile instanceof IFluidPipe)) { if (tempStack.tag != null) { tempStack.tag.removeTag("pressurized"); if (tempStack.tag.isEmpty()) { tempStack.tag = null; } } }
                        int r = output.output.fill(tempStack, doFill);
                        f += r;
                        remaining -= r;
                        if (r == offer) { nextActive.add(output); }
                        if (remaining <= 0) { break; }
                    }
                    active = nextActive;
                }
            }
            return f;
        }

        @Override
        public int fill(FluidStack resource, boolean doFill) {
            if (resource == null || resource.amount == 0) { return 0; }
            boolean isPressurized = resource.tag != null && resource.tag.hasKey("pressurized") || ITContent.normallyPressurized.contains(resource.getFluid());
            int transferable = isPressurized ? transferRatePressurized : transferRate;
            int maxTransfer = Math.min(resource.amount, transferable);
            FluidStack limited = resource.copy();
            limited.amount = maxTransfer;
            return fastFill(limited, doFill);
        }

        @Nullable
        @Override
        public FluidStack drain(FluidStack resource, boolean doDrain) { return null; }

        @Nullable
        @Override
        public FluidStack drain(int maxDrain, boolean doDrain) { return null; }
    }

    @SideOnly(Side.CLIENT)
    @SuppressWarnings({"deprecation", "unused"})
    @Nullable
    public Object getCacheKey(IBlockState ownerState, OBJModel.OBJState modelState) { return new PipeCacheKey(((MixinIETileEntityFluidPipe)this).getConnections(), color); }

    @SideOnly(Side.CLIENT)
    @SuppressWarnings("unused")
    private static class PipeCacheKey {
        private final byte connections;
        private final EnumDyeColor color;

        public PipeCacheKey(byte connections, EnumDyeColor color) {
            this.connections = connections;
            this.color = color;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) { return true; }
            if (obj == null || getClass() != obj.getClass()) { return false; }
            PipeCacheKey other = (PipeCacheKey)obj;
            return connections == other.connections && color == other.color;
        }

        @Override
        public int hashCode() { return Objects.hash(connections, color); }
    }
}

package mctmods.immersivetechnology.common.multiblocks.stone.tileentities;

import blusunrize.immersiveengineering.api.crafting.CokeOvenRecipe;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;

import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntityAdvancedCokeOvenBaseheater;
import mctmods.immersivetechnology.common.multiblocks.stone.shapes.AdvancedCokeOvenShape;
import mctmods.immersivetechnology.common.multiblocks.stone.tileentitiesmultiblockpart.TileEntityITMultiblockPartAdvancedCokeOven;
import mctmods.immersivetechnology.common.util.ITFluidTank;
import mctmods.immersivetechnology.common.util.ITSounds;
import mctmods.immersivetechnology.common.util.ITUtils;
import mctmods.immersivetechnology.common.util.multiblock.PoICache;
import mctmods.immersivetechnology.common.util.multiblock.PoIJSONSchema;
import mctmods.immersivetechnology.common.util.network.MessageStopSound;
import mctmods.immersivetechnology.common.util.network.MessageTileSync;
import mctmods.immersivetechnology.common.util.sound.ITSoundHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class TileEntityAdvancedCokeOvenMaster extends TileEntityAdvancedCokeOvenSlave implements ITFluidTank.TankListener {
    private static final int tankSize = Multiblocks.advancedCokeOven.advancedCokeOven_tankSize;
    public static float baseSpeed = Multiblocks.advancedCokeOven.advancedCokeOven_speed_base;
    public static float baseheaterAdd = Multiblocks.advancedCokeOven.advancedCokeOven_baseheater_speed_increase;
    public static float baseheaterMult = Multiblocks.advancedCokeOven.advancedCokeOven_baseheater_speed_multiplier;
    public ITFluidTank tank = new ITFluidTank(tankSize, this);
    public static int slotCount = 4;
    public NonNullList<ItemStack> inventory = NonNullList.withSize(slotCount, ItemStack.EMPTY);
    public float process = 0;
    public int processMax = 0;
    public boolean active = false;
    private float soundVolume;
    private CokeOvenRecipe processing;
    PoICache itemInput0;
    PoICache itemOutput0;
    PoICache fluidOutput0;
    PoICache baseheater0;
    PoICache baseheater1;
    private BlockPos itemOutputPos;
    private BlockPos fluidOutputPos;
    IItemHandler inputHandler = new IEInventoryHandler(1, this, 0, new boolean[]{true}, new boolean[]{false});
    IItemHandler outputHandler = new IEInventoryHandler(1, this, 1, new boolean[]{false}, new boolean[]{true});

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        process = nbt.getFloat("process");
        processMax = nbt.getInteger("processMax");
        active = nbt.getBoolean("active");
        tank.readFromNBT(nbt.getCompoundTag("tank"));
        if (!descPacket) { inventory = Utils.readInventory(nbt.getTagList("inventory", 10), slotCount); }
    }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        nbt.setFloat("process", process);
        nbt.setInteger("processMax", processMax);
        nbt.setBoolean("active", active);
        NBTTagCompound tankTag = tank.writeToNBT(new NBTTagCompound());
        nbt.setTag("tank", tankTag);
        if (!descPacket) { nbt.setTag("inventory", Utils.writeInventory(inventory)); }
    }

    void InitializePoIs() {
        for (PoIJSONSchema poi : TileEntityITMultiblockPartAdvancedCokeOven.instance.pointsOfInterest) {
            switch (poi.name) {
                case "item_input0":
                    itemInput0 = new PoICache(facing, poi, mirrored);
                    break;
                case "item_output0":
                    itemOutput0 = new PoICache(facing, poi, mirrored);
                    itemOutputPos = getBlockPosForPos(itemOutput0.position).offset(itemOutput0.facing);
                    break;
                case "fluid_output0":
                    fluidOutput0 = new PoICache(facing, poi, mirrored);
                    fluidOutputPos = getBlockPosForPos(fluidOutput0.position).offset(fluidOutput0.facing);
                    break;
                case "baseheater0":
                    baseheater0 = new PoICache(facing, poi, mirrored);
                    break;
                case "baseheater1":
                    baseheater1 = new PoICache(facing, poi, mirrored);
                    break;
            }
        }
        if (!world.isRemote) {
            notifyNeighbor(getBlockPosForPos(itemInput0.position));
            notifyNeighbor(getBlockPosForPos(itemOutput0.position));
            notifyNeighbor(getBlockPosForPos(fluidOutput0.position));
        }
    }

    private void notifyNeighbor(BlockPos pos) { world.notifyNeighborsOfStateChange(pos, world.getBlockState(pos).getBlock(), false); }

    private void pumpOutputOut() {
        if (tank.getFluidAmount() == 0) { return; }
        if (fluidOutput0 == null) { InitializePoIs(); }
        IFluidHandler output = FluidUtil.getFluidHandler(world, fluidOutputPos, fluidOutput0.facing.getOpposite());
        if (output == null) { return; }
        FluidStack out = tank.getFluid();
        int accepted = output.fill(out, false);
        if (accepted == 0) { return; }
        assert out != null;
        int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
        this.tank.drain(drained, true);
    }

    @SideOnly(Side.CLIENT) public void handleSounds() {
        if (active) {
            if (soundVolume < 1) { soundVolume += 0.01f; }
        }
        else if (soundVolume > 0) { soundVolume -= 0.01f; }
        BlockPos center = getPos();
        if (soundVolume == 0) { ITSoundHandler.StopSound(center); }
        else {
            EntityPlayerSP player = Minecraft.getMinecraft().player;
            float attenuation = Math.max((float)player.getDistanceSq(center.getX(), center.getY(), center.getZ()) / 8, 1);
            ITSounds.advancedCokeOven.PlayRepeating(center, soundVolume / attenuation, 1);
        }
    }

    @SideOnly(Side.CLIENT) @Override public void onChunkUnload() {
        ITSoundHandler.StopSound(getPos());
        super.onChunkUnload();
    }

    @Override public void disassemble() {
        BlockPos center = getPos();
        ImmersiveTechnology.packetHandler.sendToAllTracking(new MessageStopSound(center), new NetworkRegistry.TargetPoint(world.provider.getDimension(), center.getX(), center.getY(), center.getZ(), 0));
        super.disassemble();
    }

    public void notifyNearbyClients() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setBoolean("active", active);
        BlockPos center = getPos();
        ImmersiveTechnology.packetHandler.sendToAllTracking(new MessageTileSync(this, tag), new NetworkRegistry.TargetPoint(world.provider.getDimension(), center.getX(), center.getY(), center.getZ(), 0));
    }

    private void notifyProcessUpdate() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setFloat("process", process);
        tag.setInteger("processMax", processMax);
        BlockPos center = getPos();
        ImmersiveTechnology.packetHandler.sendToAllTracking(new MessageTileSync(this, tag), new NetworkRegistry.TargetPoint(world.provider.getDimension(), center.getX(), center.getY(), center.getZ(), 0));
    }

    @Override public void receiveMessageFromServer(NBTTagCompound message) {
        if (message.hasKey("active")) { active = message.getBoolean("active"); }
        else if (message.hasKey("process")) {
            process = message.getFloat("process");
            processMax = message.getInteger("processMax");
        }
    }

    public void efficientMarkDirty() { world.getChunk(this.getPos()).markDirty(); }

    @Override public void update() {
        if (!formed) { return; }
        if (fluidOutput0 == null) { InitializePoIs(); }
        if (world.isRemote) {
            handleSounds();
            return;
        }
        boolean update = false;
        if (!inventory.get(0).isEmpty()) {
            if (processing == null) {
                processing = getRecipe();
                if (processing == null) {
                    if (active) {
                        process = 0;
                        processMax = 0;
                        active = false;
                        update = true;
                        notifyNearbyClients();
                        setHeatersActive();
                    }
                }
                else {
                    if (!active) {
                        this.process = this.processMax = processing.time;
                        active = true;
                        update = true;
                        notifyNearbyClients();
                    }
                }
            }
            if (active && process > 0) {
                process -= getProcessSpeed();
                update = true;
                if (world.getTotalWorldTime() % 8 == 0) { notifyProcessUpdate(); }
            }
            if (processing != null && process <= 0) {
                if (tank.getFluidAmount() + processing.creosoteOutput <= tank.getCapacity() && inventory.get(1).getCount() + processing.output.getCount() <= inventory.get(1).getMaxStackSize()) {
                    Utils.modifyInvStackSize(inventory, 0, -1);
                    if (!inventory.get(1).isEmpty()) { inventory.get(1).grow(processing.output.copy().getCount()); }
                    else { inventory.set(1, processing.output.copy()); }
                    this.tank.fill(new FluidStack(IEContent.fluidCreosote, processing.creosoteOutput), true);
                    this.markContainingBlockForUpdate(null);
                    active = false;
                    update = true;
                    process = 0;
                    processMax = 0;
                    processing = null;
                    notifyNearbyClients();
                    setHeatersActive();
                }
                else {
                    if (active) {
                        update = true;
                        active = false;
                        notifyNearbyClients();
                        setHeatersActive();
                    }
                }
            }
        }
        else {
            if (active) {
                active = false;
                update = true;
                process = 0;
                processMax = 0;
                processing = null;
                notifyNearbyClients();
                setHeatersActive();
            }
        }
        if (tank.getFluidAmount() > 0 && tank.getFluid() != null && (inventory.get(3).isEmpty() || inventory.get(3).getCount() + 1 <= inventory.get(3).getMaxStackSize())) {
            ItemStack filledContainer = Utils.fillFluidContainer(tank, inventory.get(2), inventory.get(3), null);
            if (!filledContainer.isEmpty()) {
                if (inventory.get(2).getCount() == 1 && !Utils.isFluidContainerFull(filledContainer)) {
                    inventory.set(2, filledContainer.copy());
                    update = true;
                }
                else {
                    if (!inventory.get(3).isEmpty() && OreDictionary.itemMatches(inventory.get(3), filledContainer, true)) { inventory.get(3).grow(filledContainer.getCount()); }
                    else if (inventory.get(3).isEmpty()) {
                        inventory.set(3, filledContainer.copy());
                        Utils.modifyInvStackSize(inventory, 2, -filledContainer.getCount());
                        update = true;
                    }
                }
            }
        }
        if (!this.inventory.get(1).isEmpty()) {
            ItemStack stack = this.inventory.get(1);
            TileEntity te = world.getTileEntity(itemOutputPos);
            if (te != null) {
                IItemHandler handler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, itemOutput0.facing.getOpposite());
                if (handler != null) { stack = ItemHandlerHelper.insertItemStacked(handler, stack, false); }
            }
            this.inventory.set(1, stack);
        }
        pumpOutputOut();
        if (update) { efficientMarkDirty(); }
    }

    public CokeOvenRecipe getRecipe() {
        CokeOvenRecipe recipe = CokeOvenRecipe.findRecipe(inventory.get(0));
        if (recipe == null) { return null; }
        if (inventory.get(1).isEmpty() || (OreDictionary.itemMatches(inventory.get(1), recipe.output, false) && inventory.get(1).getCount() + recipe.output.getCount() <= getSlotLimit(1)))
            if (tank.getFluidAmount() + recipe.creosoteOutput <= tank.getCapacity()) { return recipe; }
        return null;
    }

    @Override public boolean isDummy() { return false; }

    @Override public TileEntityAdvancedCokeOvenMaster master() { return this; }

    @Override @Nonnull public int[] getCurrentProcessesStep() { return new int[]{Math.round(processMax - process)}; }

    @Override @Nonnull public int[] getCurrentProcessesMax() { return new int[]{processMax}; }

    private float getProcessSpeed() {
        if (baseheater0 == null) { InitializePoIs(); }
        PoICache[] heaters = {baseheater0, baseheater1};
        int activeBaseheaters = 0;
        for (PoICache poi : heaters) {
            BlockPos pos = getBlockPosForPos(poi.position).offset(poi.facing);
            TileEntity tile = Utils.getExistingTileEntity(world, pos);
            if (!(tile instanceof TileEntityAdvancedCokeOvenBaseheater)) { continue; }
            TileEntityAdvancedCokeOvenBaseheater baseheater = (TileEntityAdvancedCokeOvenBaseheater)tile;
            if (baseheater.facing != poi.facing.getOpposite() || !baseheater.doSpeedup()) { continue; }
            activeBaseheaters++;
        }
        return (baseSpeed + activeBaseheaters * baseheaterAdd) * (1 + activeBaseheaters * (baseheaterMult - 1));
    }

    private void setHeatersActive() {
        if (baseheater0 == null) { InitializePoIs(); }
        PoICache[] heaters = {baseheater0, baseheater1};
        for (PoICache poi : heaters) {
            BlockPos pos = getBlockPosForPos(poi.position).offset(poi.facing);
            TileEntity tile = Utils.getExistingTileEntity(world, pos);
            if (tile instanceof TileEntityAdvancedCokeOvenBaseheater) {
                TileEntityAdvancedCokeOvenBaseheater baseheater = (TileEntityAdvancedCokeOvenBaseheater)tile;
                if (baseheater.active) {
                    baseheater.active = false;
                    baseheater.markContainingBlockForUpdate(null);
                    baseheater.updateDummies();
                }
            }
        }
    }

    @Override public void TankContentsChanged() { this.markContainingBlockForUpdate(null); }

    @Override public NonNullList<ItemStack> getInventory() { return inventory; }

    @Override @Nonnull protected IFluidTank[] getAccessibleFluidTanks(EnumFacing side) {
        if (fluidOutput0 == null) { InitializePoIs(); }
        if (fluidOutput0.isPoI(side, this.pos)) { return new IFluidTank[]{tank}; }
        return new IFluidTank[0];
    }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side) {
        if (fluidOutput0 == null) { InitializePoIs(); }
        return fluidOutput0.isPoI(side, this.pos) && iTank == 0;
    }

    @Override public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && facing != null) {
            if (itemInput0 == null) { InitializePoIs(); }
            return itemInput0.isPoI(facing, this.pos) || itemOutput0.isPoI(facing, this.pos);
        }
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing != null) {
            if (fluidOutput0 == null) { InitializePoIs(); }
            return fluidOutput0.isPoI(facing, this.pos);
        }
        return super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override @Nonnull public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && facing != null) {
            if (itemInput0 == null) { InitializePoIs(); }
            if (itemInput0.isPoI(facing, this.pos)) { return (T)inputHandler; }
            if (itemOutput0.isPoI(facing, this.pos)) { return (T)outputHandler; }
        }
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing != null) {
            if (fluidOutput0 == null) { InitializePoIs(); }
            if (fluidOutput0.isPoI(facing, this.pos)) { return (T)new AdvancedCokeOvenFluidHandler(this, facing); }
        }
        return super.getCapability(capability, facing);
    }

    @Override public boolean getIsActive() { return active; }

    @Override public TileEntity getGuiMaster() { return this; }

    private List<AxisAlignedBB> getShape() {
        int width = TileEntityITMultiblockPartAdvancedCokeOven.instance.width;
        int length = TileEntityITMultiblockPartAdvancedCokeOven.instance.length;
        int y = pos / (length * width);
        int rem = pos % (length * width);
        int z = rem / width;
        int x = rem % width;
        BlockPos posInMultiblock = new BlockPos(x, y, z);
        List<AxisAlignedBB> list = AdvancedCokeOvenShape.GETTER.getShape(posInMultiblock);
        if (list.isEmpty()) { return new ArrayList<>(); }
        List<AxisAlignedBB> rotatedList = new ArrayList<>(list.size());
        for (AxisAlignedBB aabb : list) { rotatedList.add(ITUtils.rotateAABB(aabb, this.facing, this.mirrored)); }
        return rotatedList;
    }

    @Override public List<AxisAlignedBB> getAdvancedCollisionBounds() { return getShape(); }

    @Override public List<AxisAlignedBB> getAdvancedSelectionBounds() { return getShape(); }

    @Override @Nonnull public float[] getBlockBounds() {
        List<AxisAlignedBB> list = getShape();
        if (list.isEmpty()) { return new float[]{0f, 0f, 0f, 1f, 1f, 1f}; }
        AxisAlignedBB bb = list.get(0);
        for (int i = 1; i < list.size(); i++) { bb = bb.union(list.get(i)); }
        return new float[]{(float)bb.minX, (float)bb.minY, (float)bb.minZ, (float)bb.maxX, (float)bb.maxY, (float)bb.maxZ};
    }

    public static class AdvancedCokeOvenFluidHandler implements IFluidHandler {
        TileEntityAdvancedCokeOvenSlave te;
        EnumFacing facing;
        ITFluidTank tank;
        TileEntityAdvancedCokeOvenMaster master;

        public AdvancedCokeOvenFluidHandler(TileEntityAdvancedCokeOvenSlave te, EnumFacing facing) {
            this.te = te;
            this.facing = facing;
            master = te.master();
            assert master != null;
            tank = master.tank;
        }

        @Override public IFluidTankProperties[] getTankProperties() {
            if (tank == null) { return new FluidTankProperties[0]; }
            return new FluidTankProperties[]{new FluidTankProperties(tank.getFluid(), tank.getCapacity())};
        }

        @Override public int fill(FluidStack resource, boolean doFill) { return 0; }

        @Override public FluidStack drain(FluidStack resource, boolean doDrain) {
            if (resource == null || resource.amount <= 0 || tank == null) { return null; }
            FluidStack tankFluid = tank.getFluid();
            if (tankFluid != null && tankFluid.isFluidEqual(resource)) {
                FluidStack drained = tank.drain(resource.amount, doDrain);
                if (drained != null && doDrain) { master.TankContentsChanged(); }
                return drained;
            }
            return null;
        }

        @Override public FluidStack drain(int maxDrain, boolean doDrain) {
            if (maxDrain <= 0 || tank == null) { return null; }
            FluidStack drained = tank.drain(maxDrain, doDrain);
            if (drained != null && doDrain) { master.TankContentsChanged(); }
            return drained;
        }
    }
}

package mctmods.immersivetechnology.common.multiblocks.stone.tileentities;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.crafting.CokeOvenRecipe;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.blocks.TileEntityMultiblockPart;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;

import mctmods.immersivetechnology.api.ITGUI;
import mctmods.immersivetechnology.common.multiblocks.stone.shapes.AdvancedCokeOvenShape;
import mctmods.immersivetechnology.common.multiblocks.stone.tileentitiesmultiblockpart.TileEntityITMultiblockPartAdvancedCokeOven;
import mctmods.immersivetechnology.common.shared.interfaces.ITBlockInterfaces;
import mctmods.immersivetechnology.common.util.ITUtils;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TileEntityAdvancedCokeOvenSlave extends TileEntityMultiblockPart<TileEntityAdvancedCokeOvenSlave> implements IEBlockInterfaces.IActiveState, IEBlockInterfaces.IProcessTile, IIEInventory, IEBlockInterfaces.IGuiTile, ITBlockInterfaces.IBlockBounds, ITBlockInterfaces.IAdvancedCollisionBounds, ITBlockInterfaces.IAdvancedSelectionBounds {
    private static final int[] structureDimensions = {4, 3, 3};
    public TileEntityAdvancedCokeOvenSlave() { super(structureDimensions); }

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.readCustomNBT(nbt, descPacket); }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.writeCustomNBT(nbt, descPacket); }

    @Override public void update() { ITUtils.RemoveDummyFromTicking(this); }

    @Override @Nonnull public int[] getCurrentProcessesStep() {
        TileEntityAdvancedCokeOvenMaster m = master();
        return m != null ? m.getCurrentProcessesStep() : new int[0];
    }

    @Override @Nonnull public int[] getCurrentProcessesMax() {
        TileEntityAdvancedCokeOvenMaster m = master();
        return m != null ? m.getCurrentProcessesMax() : new int[0];
    }

    @Override public boolean getIsActive() {
        TileEntityAdvancedCokeOvenMaster m = master();
        return m != null && m.active;
    }

    @Override @Nonnull public IEProperties.PropertyBoolInverted getBoolProperty(@Nonnull Class<? extends IEBlockInterfaces.IUsesBooleanProperty> inf) { return IEProperties.BOOLEANS[0]; }

    @Override public NonNullList<ItemStack> getInventory() {
        TileEntityAdvancedCokeOvenMaster m = master();
        return m != null ? m.inventory : NonNullList.withSize(4, ItemStack.EMPTY);
    }

    @Override public boolean isStackValid(int slot, ItemStack stack) {
        if (stack.isEmpty()) { return false; }
        if (slot == 0) { return CokeOvenRecipe.findRecipe(stack) != null; }
        if (slot == 2) { return Utils.isFluidRelatedItemStack(stack); }
        return false;
    }

    @Override public int getSlotLimit(int slot) { return 64; }

    @Override public void doGraphicalUpdates(int slot) {}

    private TileEntityAdvancedCokeOvenMaster master;

    @Override public TileEntityAdvancedCokeOvenMaster master() {
        if (master != null && !master.isInvalid()) { return master; }
        BlockPos masterPos = getPos().add(-offset[0], -offset[1], -offset[2]);
        TileEntity te = Utils.getExistingTileEntity(world, masterPos);
        master = te instanceof TileEntityAdvancedCokeOvenMaster ? (TileEntityAdvancedCokeOvenMaster)te : null;
        return master;
    }

    @Override public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        TileEntityAdvancedCokeOvenMaster m = master();
        if (m == null) { return false; }
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && facing != null) {
            if (m.itemInput0 == null) { m.InitializePoIs(); }
            return m.itemInput0.isPoI(facing, pos) || m.itemOutput0.isPoI(facing, pos);
        }
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing != null) {
            if (m.fluidOutput0 == null) { m.InitializePoIs(); }
            return m.fluidOutput0.isPoI(facing, pos);
        }
        return super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override @Nonnull public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        TileEntityAdvancedCokeOvenMaster m = master();
        if (m == null) { return null; }
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && facing != null) {
            if (m.itemInput0 == null) { m.InitializePoIs(); }
            if (m.itemInput0.isPoI(facing, pos)) { return (T)m.inputHandler; }
            if (m.itemOutput0.isPoI(facing, pos)) { return (T)m.outputHandler; }
        }
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing != null) {
            if (m.fluidOutput0 == null) { m.InitializePoIs(); }
            if (m.fluidOutput0.isPoI(facing, pos)) { return (T)new AdvancedCokeOvenFluidHandler(this, facing); }
        }
        return super.getCapability(capability, facing);
    }

    @Override @Nonnull protected IFluidTank[] getAccessibleFluidTanks(EnumFacing side) {
        TileEntityAdvancedCokeOvenMaster m = master();
        if (m == null) { return new IFluidTank[0]; }
        if (m.fluidOutput0 == null) { m.InitializePoIs(); }
        if (m.fluidOutput0.isPoI(side, pos)) { return new IFluidTank[]{m.tank}; }
        return new IFluidTank[0];
    }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource) { return false; }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side) {
        TileEntityAdvancedCokeOvenMaster m = master();
        if (m == null) { return false; }
        if (m.fluidOutput0 == null) { m.InitializePoIs(); }
        return m.fluidOutput0.isPoI(side, pos) && iTank == 0;
    }

    @Override public boolean canOpenGui() { return formed; }

    @Override public int getGuiID() { return ITGUI.GUIID_Advanced_coke_oven; }

    @Override public TileEntity getGuiMaster() { return master(); }

    @Override @Nonnull public ItemStack getOriginalBlock() {
        if (pos < 0) { return ItemStack.EMPTY; }
        ItemStack s = TileEntityITMultiblockPartAdvancedCokeOven.instance.getStructureManual()[pos/9][pos%9/3][pos%3];
        return s.copy();
    }

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

    @Override public boolean isOverrideBox(AxisAlignedBB box, EntityPlayer player, RayTraceResult mop, List<AxisAlignedBB> list) { return false; }

    @Override @Nonnull public float[] getBlockBounds() {
        List<AxisAlignedBB> list = getShape();
        if (list.isEmpty()) { return new float[]{0f, 0f, 0f, 1f, 1f, 1f}; }
        AxisAlignedBB bb = list.get(0);
        for (int i = 1; i < list.size(); i++) { bb = bb.union(list.get(i)); }
        return new float[]{(float)bb.minX, (float)bb.minY, (float)bb.minZ, (float)bb.maxX, (float)bb.maxY, (float)bb.maxZ};
    }

    public static class AdvancedCokeOvenFluidHandler implements IFluidHandler {
        TileEntityMultiblockPart<TileEntityAdvancedCokeOvenSlave> te;
        EnumFacing facing;
        IFluidTank tank;

        public AdvancedCokeOvenFluidHandler(TileEntityMultiblockPart<TileEntityAdvancedCokeOvenSlave> te, EnumFacing facing) {
            this.te = te;
            this.facing = facing;
            TileEntityAdvancedCokeOvenMaster m = (TileEntityAdvancedCokeOvenMaster)te.master();
            tank = m != null ? m.tank : null;
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
                if (drained != null && doDrain) { ((TileEntityAdvancedCokeOvenMaster)Objects.requireNonNull(te.master())).TankContentsChanged(); }
                return drained;
            }
            return null;
        }

        @Override public FluidStack drain(int maxDrain, boolean doDrain) {
            if (maxDrain <= 0 || tank == null) { return null; }
            FluidStack drained = tank.drain(maxDrain, doDrain);
            if (drained != null && doDrain) { ((TileEntityAdvancedCokeOvenMaster)Objects.requireNonNull(te.master())).TankContentsChanged(); }
            return drained;
        }
    }
}

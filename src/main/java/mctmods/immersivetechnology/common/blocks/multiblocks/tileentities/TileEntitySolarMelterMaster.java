package mctmods.immersivetechnology.common.blocks.multiblocks.tileentities;

import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.common.util.ITUtils;
import mctmods.immersivetechnology.api.crafting.MeltingCrucibleRecipe;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.ITContent;
import mctmods.immersivetechnology.common.blocks.multiblocks.TileEntityITMultiblockPartSolarMelter;
import mctmods.immersivetechnology.common.util.ITFluidTank;
import mctmods.immersivetechnology.common.util.ITSounds;
import mctmods.immersivetechnology.common.util.compat.ITCompatModule;
import mctmods.immersivetechnology.common.util.compat.advancedrocketry.AdvancedRocketryHelper;
import mctmods.immersivetechnology.common.util.multiblock.PoICache;
import mctmods.immersivetechnology.common.util.multiblock.PoIJSONSchema;
import mctmods.immersivetechnology.common.util.network.BinaryMessageTileSync;
import mctmods.immersivetechnology.common.util.network.IBinaryMessageReceiver;
import mctmods.immersivetechnology.common.util.network.MessageStopSound;
import mctmods.immersivetechnology.common.util.sound.ITSoundHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

public class TileEntitySolarMelterMaster extends TileEntitySolarMelterSlave implements ITFluidTank.TankListener, IBinaryMessageReceiver {
    private static final int outputTankSize = Multiblocks.solarMelter.solarMelter_output_tankSize;
    private static final int solarMaxRange = Multiblocks.solarReflector.solarReflector_maxRange;
    private static final int solarMinRange = Multiblocks.solarReflector.solarReflector_minRange;
    private static final int energyLossPerTick = Multiblocks.solarMelter.solarMelter_progress_lossEnergy;
    private static final double maximumReflectorStrength = Multiblocks.solarMelter.solarMelter_maximum_reflector_strength;

    private BlockPos fluidOutputPos;

    public FluidTank[] tanks = new FluidTank[] { new ITFluidTank(outputTankSize, this) };

    public static int slotCount = 1;
    public NonNullList<ItemStack> inventory = NonNullList.withSize(slotCount, ItemStack.EMPTY);

    IItemHandler insertionHandler = new IEInventoryHandler(slotCount, this, 0, new boolean[]{true}, new boolean[1]);

    public int recipeEnergyRemaining = 0;
    public double reflectorStrength = 0;
    public int solarIncidenceAngleSection = 0;
    private float soundVolume;
    private boolean isRunning;
    private int gracePeriod = 60;
    private int clientUpdateCooldown = 1;

    private MeltingCrucibleRecipe cachedRecipe;

    private PoICache redstonePoI, output0;

    @Override
    public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        tanks[0].readFromNBT(nbt.getCompoundTag("tank0"));
        recipeEnergyRemaining = nbt.getInteger("recipeEnergyRemaining");
        reflectorStrength = nbt.getDouble("reflectorStrength");
        solarIncidenceAngleSection = nbt.getInteger("solarIncidenceAngleSection");
        if(!descPacket) { inventory = Utils.readInventory(nbt.getTagList("inventory", 10), slotCount); }
    }

    @Override
    public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        nbt.setTag("tank0", tanks[0].writeToNBT(new NBTTagCompound()));
        nbt.setInteger("recipeEnergyRemaining", recipeEnergyRemaining);
        nbt.setDouble("reflectorStrength", reflectorStrength);
        nbt.setInteger("solarIncidenceAngleSection", solarIncidenceAngleSection);
        if(!descPacket) { nbt.setTag("inventory", Utils.writeInventory(inventory)); }
    }

    protected void checkReflectorPositions() {
        double totalMirrorStrength = 0;
        for(int x = -(solarMaxRange + 1); x <= (solarMaxRange + 1); x++) for(int z = -(solarMaxRange + 1); z <= (solarMaxRange + 1); z++) {
            double distance = Math.sqrt(this.getPos().distanceSq(this.getPos().add(x, -1, z)));
            if(distance >= solarMinRange && distance <= solarMaxRange && Utils.isBlockAt(world, this.getPos().add(x, -1, z), ITContent.blockMetalMultiblock, 2)) {
                TileEntity tile = world.getTileEntity(this.getPos().add(x, -1, z));
                if(tile instanceof TileEntitySolarReflectorMaster && ((TileEntitySolarReflectorMaster)tile).setTowerCollectorPosition(this.getPos().add(0, 16, 0))) { totalMirrorStrength += ((TileEntitySolarReflectorMaster)tile).getSolarCollectorStrength(); }
            }
        }
        totalMirrorStrength *= (world.isRaining() ? 0.4f : 1f);
        if(ITCompatModule.isAdvancedRocketryLoaded) { totalMirrorStrength *= AdvancedRocketryHelper.getInsolation(world, this.getPos()); }
        double humidityBonus = 0.075 * totalMirrorStrength * -((world.getBiome(this.getPos()).getRainfall() - 0.5)/0.5);
        if(ITCompatModule.isAdvancedRocketryLoaded) { humidityBonus *= AdvancedRocketryHelper.getWaterPartialPressureMultiplier(world, this.getPos()); }
        totalMirrorStrength += humidityBonus;
        reflectorStrength = totalMirrorStrength;
    }

    private boolean loseProgress() {
        if(cachedRecipe == null || !cachedRecipe.itemInput.matches(inventory.get(0))) { cachedRecipe = null; recipeEnergyRemaining = 0; return true; }
        int previousProgress = recipeEnergyRemaining;
        recipeEnergyRemaining = (int)Math.min(recipeEnergyRemaining + energyLossPerTick * (ITCompatModule.isAdvancedRocketryLoaded ? AdvancedRocketryHelper.getHeatTransferCoefficient(world, this.getPos()) : 1), cachedRecipe.getTotalProcessEnergy());
        return previousProgress != recipeEnergyRemaining;
    }

    private boolean gainProgress() {
        if(cachedRecipe == null || !cachedRecipe.itemInput.matches(inventory.get(0))) { cachedRecipe = null; recipeEnergyRemaining = 0; return true; }
        int previousProgress = recipeEnergyRemaining;
        recipeEnergyRemaining -= (int)(solarIncidenceAngleSection * 7680 * (reflectorStrength / maximumReflectorStrength));
        boolean changed = previousProgress != recipeEnergyRemaining;
        if(recipeEnergyRemaining <= 0) {
            inventory.get(0).shrink(cachedRecipe.itemInput.inputSize);
            tanks[0].fillInternal(new FluidStack(cachedRecipe.fluidOutput.getFluid(), cachedRecipe.fluidOutput.amount), true);
            cachedRecipe = null;
            recipeEnergyRemaining = 0;
            markContainingBlockForUpdate(null);
            changed = true;
        }
        return changed;
    }

    private boolean pumpOutputOut() {
        if(tanks[0].getFluidAmount() == 0) return false;
        IFluidHandler output = FluidUtil.getFluidHandler(world, fluidOutputPos, output0.facing.getOpposite());
        if(output == null) return false;
        FluidStack out = tanks[0].getFluid();
        int accepted = output.fill(out, false);
        if(accepted == 0) return false;
        assert out != null;
        int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
        this.tanks[0].drain(drained, true);
        return drained > 0;
    }

    public void handleSounds() {
        BlockPos center = this.getPos();
        if(isRunning) { if(soundVolume < 1) soundVolume += 0.02f; } else { if(soundVolume > 0) soundVolume -= 0.02f; }
        if(soundVolume <= 0) { ITSoundHandler.StopSound(center); } else {
            float attenuation = Math.max((float)Minecraft.getMinecraft().player.getDistanceSq(center.getX(), center.getY(), center.getZ()) / 8, 1);
            ITSounds.heatExchanger.PlayRepeating(center, soundVolume / attenuation, 1);
        }
    }

    private int computeSolarIncidenceAngleSection() {
        int light = world.getSkylightSubtracted();
        if(light == 3) return 1; else if(light == 2) return 2; else if(light == 1) return 3; else if(light == 0) return 4;
        return 0;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void onChunkUnload() {
        ITSoundHandler.StopSound(this.getPos());
        super.onChunkUnload();
    }

    @Override
    public void disassemble() {
        BlockPos center = this.getPos();
        ImmersiveTechnology.packetHandler.sendToAllTracking(new MessageStopSound(center), new NetworkRegistry.TargetPoint(world.provider.getDimension(), center.getX(), center.getY(), center.getZ(), 0));
        super.disassemble();
    }

    public void efficientMarkDirty() { world.getChunk(this.getPos()).markDirty(); }

    private boolean recipeLogic() {
        boolean update = false;
        if(cachedRecipe != null && !cachedRecipe.itemInput.matches(inventory.get(0))) { cachedRecipe = null; recipeEnergyRemaining = 0; update = true; }
        if(!isRSDisabled()) {
            if(recipeEnergyRemaining > 0) { update |= gainProgress(); }
            else if(!inventory.get(0).isEmpty()) {
                MeltingCrucibleRecipe recipe = cachedRecipe;
                if(recipe == null || !recipe.itemInput.matches(inventory.get(0))) { recipe = MeltingCrucibleRecipe.findRecipe(inventory.get(0)); }
                if(recipe != null && recipe.fluidOutput.amount <= tanks[0].getCapacity() - tanks[0].getFluidAmount()) {
                    cachedRecipe = recipe;
                    recipeEnergyRemaining = recipe.getTotalProcessEnergy();
                    update |= gainProgress();
                }
            }
        } else if(recipeEnergyRemaining > 0) { update |= loseProgress(); }
        return update;
    }

    private boolean outputTankLogic() {
        if(this.tanks[0].getFluidAmount() > 0) return pumpOutputOut();
        return false;
    }

    @Override
    public void update() {
        super.update();
        if(!formed || world.isRemote) {
            if(world.isRemote) handleSounds();
            return;
        }
        solarIncidenceAngleSection = computeSolarIncidenceAngleSection();
        boolean update = false;
        if(world.getTotalWorldTime() % 600 == 0) checkReflectorPositions();
        if(solarIncidenceAngleSection != 0) update |= recipeLogic();
        update |= outputTankLogic();
        if(recipeEnergyRemaining > 0) { isRunning = true; gracePeriod = 60; } else { if(gracePeriod == 0) isRunning = false; else gracePeriod--; }
        if(clientUpdateCooldown > 1) clientUpdateCooldown--; else { notifyNearbyClients(); clientUpdateCooldown = 20; }
        if(update) {
            efficientMarkDirty();
            this.markContainingBlockForUpdate(null);
        }
    }

    @Override
    public void TankContentsChanged() {
        cachedRecipe = null;
        this.markContainingBlockForUpdate(null);
        efficientMarkDirty();
    }

    @Override
    public boolean isDummy() { return false; }

    @Override
    public TileEntitySolarMelterMaster master() {
        master = this;
        return this;
    }

    private AxisAlignedBB renderBoundingBox = null;

    @SideOnly(Side.CLIENT)
    @Override
    public @Nonnull AxisAlignedBB getRenderBoundingBox() {
        if(renderBoundingBox == null) {
            int h = TileEntityITMultiblockPartSolarMelter.instance.height;
            int l = TileEntityITMultiblockPartSolarMelter.instance.length;
            int w = TileEntityITMultiblockPartSolarMelter.instance.width;
            int total = h * l * w;
            double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, minZ = Double.MAX_VALUE;
            double maxX = Double.MIN_VALUE, maxY = Double.MIN_VALUE, maxZ = Double.MIN_VALUE;
            for(int pos = 0; pos < total; pos++) {
                BlockPos bp = getBlockPosForPos(pos);
                minX = Math.min(minX, bp.getX());
                minY = Math.min(minY, bp.getY());
                minZ = Math.min(minZ, bp.getZ());
                maxX = Math.max(maxX, bp.getX());
                maxY = Math.max(maxY, bp.getY());
                maxZ = Math.max(maxZ, bp.getZ());
            }
            renderBoundingBox = new AxisAlignedBB(minX, minY, minZ, maxX + 1, maxY + 1, maxZ + 1);
        }
        return renderBoundingBox;
    }

    @Override
    public void receiveMessageFromServer(ByteBuf message) { isRunning = message.readBoolean(); }

    public void notifyNearbyClients() {
        ByteBuf buffer = Unpooled.copyBoolean(isRunning);
        BlockPos center = getPos();
        ImmersiveTechnology.packetHandler.sendToAllAround(new BinaryMessageTileSync(center, buffer), new NetworkRegistry.TargetPoint(world.provider.getDimension(), center.getX(), center.getY(), center.getZ(), 40));
    }

    private void InitializePoIs() {
        for(PoIJSONSchema poi : TileEntityITMultiblockPartSolarMelter.instance.pointsOfInterest) {
            switch(poi.name) {
                case "redstone": redstonePoI = new PoICache(facing, poi, mirrored); break;
                case "fluid_output": output0 = new PoICache(facing, poi, mirrored); break;
            }
        }
        fluidOutputPos = getBlockPosForPos(output0.position).offset(output0.facing);
    }

    @Override
    public @Nonnull int[] getRedstonePos() { return new int[]{redstonePoI.position}; }

    protected @Nonnull IFluidTank[] getAccessibleFluidTanks(EnumFacing side, int position) {
        if(output0 == null) {
            InitializePoIs();
            if(!world.isRemote) notifyIONeighbors();
        }
        if(output0.isPoI(side, position)) return new IFluidTank[] {tanks[0]};
        return ITUtils.emptyIFluidTankList;
    }

    protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, int position) { return false; }

    protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, int position) { return output0.isPoI(side, position); }

    private void notifyIONeighbors() {
        if(world.isRemote) return;
        notifyNeighbor(getBlockPosForPos(output0.position));
        notifyNeighbor(getBlockPosForPos(redstonePoI.position));
    }

    private void notifyNeighbor(BlockPos pos) { world.notifyNeighborsOfStateChange(pos, world.getBlockState(pos).getBlock(), false); }
}

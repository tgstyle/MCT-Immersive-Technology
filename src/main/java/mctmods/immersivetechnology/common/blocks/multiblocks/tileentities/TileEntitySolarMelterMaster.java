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
import mctmods.immersivetechnology.common.blocks.multiblocks.tileentitiesmultiblockpart.TileEntityITMultiblockPartSolarMelter;
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
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
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
    public FluidTank[] tanks = new FluidTank[] { new ITFluidTank(outputTankSize, this) };
    public static int slotCount = 1;
    public NonNullList<ItemStack> inventory = NonNullList.withSize(slotCount, ItemStack.EMPTY);
    IItemHandler insertionHandler = new IEInventoryHandler(slotCount, this, 0, new boolean[]{true}, new boolean[]{false});
    public int recipeEnergyRemaining = 0;
    public double reflectorStrength = 0;
    public int solarIncidenceAngleSection = 0;
    private float soundVolume;
    private boolean isRunning;
    private int gracePeriod = 60;
    private int clientUpdateCooldown = 1;
    private MeltingCrucibleRecipe cachedRecipe;
    private PoICache redstone0;
    private PoICache fluidOutput0;
    private PoICache itemInput0;
    private BlockPos soundPos0;
    private BlockPos fluidOutputFront0;

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        tanks[0].readFromNBT(nbt.getCompoundTag("tank0"));
        recipeEnergyRemaining = nbt.getInteger("recipeEnergyRemaining");
        reflectorStrength = nbt.getDouble("reflectorStrength");
        solarIncidenceAngleSection = nbt.getInteger("solarIncidenceAngleSection");
        if(!descPacket) inventory = Utils.readInventory(nbt.getTagList("inventory", 10), slotCount);
    }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        nbt.setTag("tank0", tanks[0].writeToNBT(new NBTTagCompound()));
        nbt.setInteger("recipeEnergyRemaining", recipeEnergyRemaining);
        nbt.setDouble("reflectorStrength", reflectorStrength);
        nbt.setInteger("solarIncidenceAngleSection", solarIncidenceAngleSection);
        if(!descPacket) nbt.setTag("inventory", Utils.writeInventory(inventory));
    }

    protected void checkReflectorPositions() {
        double totalMirrorStrength = 0;
        for(int x = -(solarMaxRange + 1); x <= (solarMaxRange + 1); x++) for(int z = -(solarMaxRange + 1); z <= (solarMaxRange + 1); z++) {
            double distance = Math.sqrt(this.getPos().distanceSq(this.getPos().add(x, -1, z)));
            if(distance >= solarMinRange && distance <= solarMaxRange && Utils.isBlockAt(world, this.getPos().add(x, -1, z), ITContent.blockMetalMultiblock, 2)) {
                TileEntity tile = world.getTileEntity(this.getPos().add(x, -1, z));
                if(tile instanceof TileEntitySolarReflectorMaster && ((TileEntitySolarReflectorMaster)tile).setTowerCollectorPosition(this.getPos().add(0, 16, 0))) totalMirrorStrength += ((TileEntitySolarReflectorMaster)tile).getSolarCollectorStrength();
            }
        }
        totalMirrorStrength *= (world.isRaining() ? 0.4f : 1f);
        if(ITCompatModule.isAdvancedRocketryLoaded) totalMirrorStrength *= AdvancedRocketryHelper.getInsolation(world, this.getPos());
        double humidityBonus = 0.075 * totalMirrorStrength * -((world.getBiome(this.getPos()).getRainfall() - 0.5)/0.5);
        if(ITCompatModule.isAdvancedRocketryLoaded) humidityBonus *= AdvancedRocketryHelper.getWaterPartialPressureMultiplier(world, this.getPos());
        totalMirrorStrength += humidityBonus;
        reflectorStrength = totalMirrorStrength;
    }

    protected void detachMirrors() {
        for(int x = -(solarMaxRange + 1); x <= (solarMaxRange + 1); x++) for(int z = -(solarMaxRange + 1); z <= (solarMaxRange + 1); z++) {
            double distance = Math.sqrt(this.getPos().distanceSq(this.getPos().add(x, -1, z)));
            if(distance >= solarMinRange && distance <= solarMaxRange && Utils.isBlockAt(world, this.getPos().add(x, -1, z), ITContent.blockMetalMultiblock, 2)) {
                TileEntity tile = world.getTileEntity(this.getPos().add(x, -1, z));
                if(tile instanceof TileEntitySolarReflectorMaster) ((TileEntitySolarReflectorMaster)tile).detachTower(this.getPos().add(0, 16, 0));
            }
        }
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
        if (tanks[0].getFluidAmount() == 0) return false;
        IFluidHandler handler = FluidUtil.getFluidHandler(world, fluidOutputFront0, fluidOutput0.facing.getOpposite());
        if (handler == null) return false;
        FluidStack out = tanks[0].getFluid();
        int accepted = handler.fill(out, false);
        if (accepted == 0) return false;
        assert out != null;
        int drained = handler.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.amount, accepted), false), true);
        this.tanks[0].drain(drained, true);
        return drained > 0;
    }

    @SideOnly(Side.CLIENT)
    public void handleSounds() {
        if(isRunning) { soundVolume = Math.min(soundVolume + 0.02f, 1); } else { soundVolume = Math.max(soundVolume - 0.02f, 0); }
        if(soundVolume == 0) ITSoundHandler.StopSound(soundPos0);
        else {
            EntityPlayerSP player = Minecraft.getMinecraft().player;
            float attenuation = Math.max((float)player.getDistanceSq(soundPos0.getX(), soundPos0.getY(), soundPos0.getZ()) / 8, 1);
            ITSounds.heatExchanger.PlayRepeating(soundPos0, soundVolume / attenuation, 1);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override public void onChunkUnload() {
        ITSoundHandler.StopSound(soundPos0);
        super.onChunkUnload();
    }

    @Override public void disassemble() {
        super.disassemble();
        ImmersiveTechnology.packetHandler.sendToAllTracking(new MessageStopSound(soundPos0), new NetworkRegistry.TargetPoint(world.provider.getDimension(), soundPos0.getX(), soundPos0.getY(), soundPos0.getZ(), 0));
        detachMirrors();
    }

    public void efficientMarkDirty() { world.getChunk(this.getPos()).markDirty(); }

    private int computeSolarIncidenceAngleSection() {
        int light = world.getSkylightSubtracted();
        if(light == 3) return 1;
        else if(light == 2) return 2;
        else if(light == 1) return 3;
        else if(light == 0) return 4;
        return 0;
    }

    @Override public void update() {
        if (formed && redstone0 == null) InitializePoIs();
        super.update();
        if(!formed) return;
        if(world.isRemote) {
            handleSounds();
            return;
        }
        solarIncidenceAngleSection = computeSolarIncidenceAngleSection();
        boolean update = false;
        if(world.getTotalWorldTime() % 600 == 0) checkReflectorPositions();
        if(solarIncidenceAngleSection != 0) if(recipeLogic()) update = true;
        if(pumpOutputOut()) update = true;
        if(recipeEnergyRemaining > 0) { isRunning = true; gracePeriod = 60; } else { if(gracePeriod == 0) isRunning = false; else gracePeriod--; }
        clientUpdateCooldown--;
        if(clientUpdateCooldown <= 0) { notifyNearbyClients(); clientUpdateCooldown = 20; }
        if(update) {
            efficientMarkDirty();
            this.markContainingBlockForUpdate(null);
        }
    }

    @Override public void TankContentsChanged() {
        cachedRecipe = null;
        this.markContainingBlockForUpdate(null);
    }

    @Override public boolean isDummy() { return false; }

    @Override public TileEntitySolarMelterMaster master() { return this; }

    public void notifyNearbyClients() { ImmersiveTechnology.packetHandler.sendToAllAround(new BinaryMessageTileSync(getPos(), Unpooled.copyBoolean(isRunning)), new NetworkRegistry.TargetPoint(world.provider.getDimension(), getPos().getX(), getPos().getY(), getPos().getZ(), 40)); }

    @Override public void receiveMessageFromServer(ByteBuf buf) { isRunning = buf.readBoolean(); }

    private void InitializePoIs() {
        for(PoIJSONSchema poi : TileEntityITMultiblockPartSolarMelter.instance.pointsOfInterest) {
            switch(poi.name) {
                case "redstone": redstone0 = new PoICache(facing, poi, mirrored); break;
                case "fluid_output": fluidOutput0 = new PoICache(facing, poi, mirrored); fluidOutputFront0 = getBlockPosForPos(fluidOutput0.position).offset(fluidOutput0.facing); break;
                case "item_input": itemInput0 = new PoICache(facing, poi, mirrored); break;
                case "sound": soundPos0 = getBlockPosForPos(poi.position); break;
            }
        }
        if(!world.isRemote) notifyIONeighbors();
    }

    private void notifyIONeighbors() {
        notifyNeighbor(getBlockPosForPos(fluidOutput0.position));
        notifyNeighbor(getBlockPosForPos(redstone0.position));
        notifyNeighbor(getBlockPosForPos(itemInput0.position));
    }

    private void notifyNeighbor(BlockPos pos) { world.notifyNeighborsOfStateChange(pos, world.getBlockState(pos).getBlock(), false); }

    @Override public @Nonnull int[] getRedstonePos() {
        if (!formed) return new int[0];
        if(redstone0 == null) InitializePoIs();
        return new int[]{redstone0.position};
    }

    @Override protected @Nonnull IFluidTank[] getAccessibleFluidTanks(EnumFacing side, int position) {
        if(!formed) return ITUtils.emptyIFluidTankList;
        if(fluidOutput0.isPoI(side, position)) return new IFluidTank[] {tanks[0]};
        return ITUtils.emptyIFluidTankList;
    }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, int position) { return false; }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, int position) { return fluidOutput0.isPoI(side, position) && iTank == 0; }

    protected @Nonnull IItemHandler[] getAccessibleItemHandlers(EnumFacing side, int position) {
        if(!formed) return new IItemHandler[0];
        if(itemInput0.isPoI(side, position)) return new IItemHandler[] {insertionHandler};
        return new IItemHandler[0];
    }

    private boolean recipeLogic() {
        boolean update = false;
        if(cachedRecipe != null && !cachedRecipe.itemInput.matches(inventory.get(0))) { cachedRecipe = null; recipeEnergyRemaining = 0; update = true; }
        if(!isRSDisabled()) {
            if(recipeEnergyRemaining > 0) { update |= gainProgress(); }
            else if(!inventory.get(0).isEmpty()) {
                MeltingCrucibleRecipe recipe = cachedRecipe;
                if(recipe == null || !recipe.itemInput.matches(inventory.get(0))) recipe = MeltingCrucibleRecipe.findRecipe(inventory.get(0));
                if(recipe != null && recipe.fluidOutput.amount <= tanks[0].getCapacity() - tanks[0].getFluidAmount()) {
                    cachedRecipe = recipe;
                    recipeEnergyRemaining = recipe.getTotalProcessEnergy();
                    update |= gainProgress();
                }
            }
        } else if(recipeEnergyRemaining > 0) { update |= loseProgress(); }
        return update;
    }
}

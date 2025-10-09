package mctmods.immersivetechnology.common.blocks.multiblocks.logic;

import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IClientTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.RedstoneControl;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockBEHelper;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockBE;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.*;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.common.blocks.multiblocks.blockimpl.InitialMultiblockContext;
import blusunrize.immersiveengineering.common.util.Utils;
import mctmods.immersivetechnology.client.particles.ColoredSmoke;
import mctmods.immersivetechnology.common.blocks.multiblocks.helper.ITMultiBlockInventoryUtils;
import mctmods.immersivetechnology.common.blocks.multiblocks.helper.ITSlotwiseItemHandler;
import mctmods.immersivetechnology.common.blocks.multiblocks.helper.ITWrappingItemHandler;
import mctmods.immersivetechnology.common.blocks.multiblocks.logic.SolarMelterLogic.State;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.SolarMelterRecipe;
import mctmods.immersivetechnology.common.blocks.multiblocks.shapes.SolarMelterShape;
import mctmods.immersivetechnology.common.fluids.helper.ITArrayFluidHandler;
import mctmods.immersivetechnology.common.network.ITOSDSyncBlock;
import mctmods.immersivetechnology.common.network.ITPacketHandler;
import mctmods.immersivetechnology.common.util.multiblock.MultiblockData;
import mctmods.immersivetechnology.common.util.multiblock.PoIJSONSchema;
import mctmods.immersivetechnology.common.util.solarregistry.SolarRegistry;
import mctmods.immersivetechnology.common.util.TranslationKey;
import mctmods.immersivetechnology.core.lib.ITSound;
import mctmods.immersivetechnology.core.registration.ITSounds;
import mctmods.immersivetechnology.common.blocks.multiblocks.logic.helper.ITSolarTank;
import mctmods.immersivetechnology.common.blocks.multiblocks.logic.interfaces.ITISolarMultiblockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import com.google.common.collect.Lists;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static mctmods.immersivetechnology.common.util.solarregistry.SolarRegistry.SOLAR_MAX_RANGE;
import static mctmods.immersivetechnology.common.util.solarregistry.SolarRegistry.SOLAR_MIN_RANGE;

public class SolarMelterLogic implements IMultiblockLogic<State>, IServerTickableComponent<State>, IClientTickableComponent<SolarMelterLogic.State> {
    public static final int SLOT_INPUT_FILLED = 0;
    public static final int SLOT_INPUT_EMPTY = 1;
    public static final int SLOT_OUTPUT_EMPTY = 2;
    public static final int SLOT_OUTPUT_FILLED = 3;
    public static final double WORKING_HEAT_LEVEL = 1000.0;
    public static final double DAY_MIN_HEAT_LOSS = 0.0;
    public static final double LOSS_PER_SECTION_DROP = 0.035;
    public static final double TEMP_DEPENDENT_LOSS_FACTOR = 0.00036;
    public static final double HEAT_INCREASE_FACTOR = 0.00568;
    private static final double TEMP_TO_MIN_REFLECTORS_DIVISOR = 25.0;
    private static final double REFLECTOR_TIER_OFFSET = 4.0;
    public static final int PROGRESS_LOSS_OFF_TEMP = 2;
    public static final float SPEED_MULTIPLIER = 1.0f;

    private static final MultiblockData DATA = SolarMelterShape.DATA;

    private static PoIJSONSchema findPOI(String name) {
        for (PoIJSONSchema poi : DATA.pointsOfInterest) { if (poi.name.equals(name)) { return poi; } }
        throw new RuntimeException("Missing POI: " + name);
    }

    private static CapabilityPosition findCapPos(String name) {
        PoIJSONSchema poi = findPOI(name);
        return new CapabilityPosition(new BlockPos(poi.pos[0], poi.pos[1], poi.pos[2]), poi.relativeFace);
    }

    public static final CapabilityPosition INPUT_FLUID_POI = findCapPos("fluid_input");
    public static final CapabilityPosition OUTPUT_FLUID_POI = findCapPos("fluid_output");
    private static final PoIJSONSchema ITEM_OUTPUT_JSON_POI = findPOI("item_output");
    public static final MultiblockFace ITEM_OUTPUT_POI = new MultiblockFace(ITEM_OUTPUT_JSON_POI.relativeFace, new BlockPos(ITEM_OUTPUT_JSON_POI.pos[0], ITEM_OUTPUT_JSON_POI.pos[1], ITEM_OUTPUT_JSON_POI.pos[2]));
    public static final BlockPos REDSTONE_POI = new BlockPos(findPOI("redstone").pos[0], findPOI("redstone").pos[1], findPOI("redstone").pos[2]);
    public static final BlockPos RUNNING_SOUND_POI = new BlockPos(findPOI("sound").pos[0], findPOI("sound").pos[1], findPOI("sound").pos[2]);
    public static final BlockPos LINK_POI = new BlockPos(findPOI("link").pos[0], findPOI("link").pos[1], findPOI("link").pos[2]);
    public static final BlockPos PARTICLE_POI = new BlockPos(findPOI("particle").pos[0], findPOI("particle").pos[1], findPOI("particle").pos[2]);
    private static final BlockPos REFLECTOR_POI = new BlockPos(findPOI("reflector").pos[0], findPOI("reflector").pos[1], findPOI("reflector").pos[2]);
    private static final BlockPos SUN_POI = new BlockPos(findPOI("sun").pos[0], findPOI("sun").pos[1], findPOI("sun").pos[2]);

    @Override
    public void tickClient(IMultiblockContext<State> ctx) {
        State state = ctx.getState();
        if (!state.isSoundPlaying.getAsBoolean()) {
            Vec3 soundVec = ctx.getLevel().toAbsolute(new Vec3(RUNNING_SOUND_POI.getX() + 0.5, RUNNING_SOUND_POI.getY() + 0.5, RUNNING_SOUND_POI.getZ() + 0.5));
            FluidStack fs = state.tanks.input().getFluid();
            SolarMelterRecipe recipe = fs.getAmount() > 0 ? SolarMelterRecipe.findRecipe(ctx.getLevel().getRawLevel(), fs) : null;
            double maxHeat = recipe != null ? recipe.requiredTemp : WORKING_HEAT_LEVEL;
            boolean shouldPlay = state.heatLevel >= maxHeat && state.sunVisible && state.reflectorStrength > 0;
            if (shouldPlay) {
                state.soundId++;
                int thisId = state.soundId;
                state.isSoundPlaying = ITSound.startSound(
                        () -> {
                            FluidStack fsActive = state.tanks.input().getFluid();
                            SolarMelterRecipe recipeActive = fsActive.getAmount() > 0 ? SolarMelterRecipe.findRecipe(ctx.getLevel().getRawLevel(), fsActive) : null;
                            double maxHeatActive = recipeActive != null ? recipeActive.requiredTemp : WORKING_HEAT_LEVEL;
                            return state.heatLevel >= maxHeatActive && state.sunVisible && state.reflectorStrength > 0 && state.soundId == thisId;
                        },
                        ctx.isValid(), soundVec, ITSounds.solarMelter,
                        () -> {
                            LocalPlayer player = Minecraft.getInstance().player;
                            if (player == null) { return 0f; }
                            float a = (float) Math.max(player.distanceToSqr(soundVec) / 8, 1);
                            FluidStack fsVol = state.tanks.input().getFluid();
                            SolarMelterRecipe recipeVol = fsVol.getAmount() > 0 ? SolarMelterRecipe.findRecipe(ctx.getLevel().getRawLevel(), fsVol) : null;
                            double maxHeatVol = recipeVol != null ? recipeVol.requiredTemp : WORKING_HEAT_LEVEL;
                            float heatFactor = (float) (state.heatLevel / maxHeatVol);
                            return (2 * heatFactor) / a;
                        },
                        () -> 1f
                );
            }
        }
        FluidStack fsParticles = state.tanks.input().getFluid();
        SolarMelterRecipe recipeParticles = fsParticles.getAmount() > 0 ? SolarMelterRecipe.findRecipe(ctx.getLevel().getRawLevel(), fsParticles) : null;
        double maxHeatParticles = recipeParticles != null ? recipeParticles.requiredTemp : WORKING_HEAT_LEVEL;
        if (state.heatLevel >= maxHeatParticles && state.sunVisible && state.reflectorStrength > 0) {
            Level clientLevel = ctx.getLevel().getRawLevel();
            if (clientLevel != null && clientLevel.getGameTime() % 4 == 0) {
                BlockPos bottomPos = ctx.getLevel().toAbsolute(PARTICLE_POI);
                double py = bottomPos.getY() + 1;
                double baseX = bottomPos.getX() + 0.5;
                double baseZ = bottomPos.getZ() + 0.5;
                for (int i = 0; i < 3; i++) {
                    float g = clientLevel.random.nextFloat();
                    ColoredSmoke particleData = new ColoredSmoke(1.0F, g, 0.0F);
                    double px = baseX + (clientLevel.random.nextGaussian() * 0.1);
                    double pz = baseZ + (clientLevel.random.nextGaussian() * 0.1);
                    clientLevel.addParticle(particleData, px, py, pz, 0.0D, 0.0D, 0.0D);
                }
            }
            if (clientLevel != null && clientLevel.getGameTime() % 10 == 0) {
                BlockPos splashPos = ctx.getLevel().toAbsolute(PARTICLE_POI);
                double px = splashPos.getX() + 0.5 + (clientLevel.random.nextDouble() - 0.5) * 0.5;
                double py = splashPos.getY() + 1.0;
                double pz = splashPos.getZ() + 0.5 + (clientLevel.random.nextDouble() - 0.5) * 0.5;
                clientLevel.addParticle(ParticleTypes.LAVA, px, py, pz, 0.0D, 0.0D, 0.0D);
                for (int i = 0; i < 10; i++) {
                    double spx = splashPos.getX() + 0.5 + (clientLevel.random.nextDouble() - 0.5) * 0.3;
                    double spy = splashPos.getY() + 0.5 + clientLevel.random.nextDouble() * 0.5;
                    double spz = splashPos.getZ() + 0.5 + (clientLevel.random.nextDouble() - 0.5) * 0.3;
                    double vx = (clientLevel.random.nextDouble() - 0.5) * 0.1;
                    double vy = clientLevel.random.nextDouble() * 0.2 + 0.1;
                    double vz = (clientLevel.random.nextDouble() - 0.5) * 0.1;
                    clientLevel.addParticle(ParticleTypes.FIREWORK, spx, spy, spz, vx, vy, vz);
                }
            }
        }
    }

    @Override
    public void tickServer(IMultiblockContext<State> ctx) {
        State state = ctx.getState();
        IMultiblockLevel mlevel = ctx.getLevel();
        Level level = mlevel.getRawLevel();
        boolean update = false;
        if (!state.isLoaded && !level.isClientSide) { state.isLoaded = true; updatePortNeighbors(mlevel); SolarRegistry.RegisterResult result = SolarRegistry.registerTower(level, state.basePos); state.registered = result.success; state.failVertical = result.vertical; state.requiredMove = result.requiredMove; if (state.registered) { state.reflectorStrength = checkReflectorPositions(mlevel, state); } update = true; }
        if (!state.registered) { return; }
        boolean oldVisible = state.sunVisible;
        state.sunVisible = level.canSeeSky(state.sunPos);
        if (oldVisible != state.sunVisible) { update = true; }
        long time = level.getGameTime();
        boolean enabled = state.rsState.isEnabled(ctx);
        if (!enabled && state.reflectorStrength > 0) { detachReflectorPositions(state); state.reflectorStrength = 0; update = true; }
        if (enabled && (time % 60 == 0 || state.reflectorStrength == 0)) { state.reflectorStrength = checkReflectorPositions(mlevel, state); }
        boolean wasActive = state.active;
        update |= heatLogic(state, level, enabled);
        update |= recipeLogic(state, level, enabled);
        state.active = enabled && state.activeRecipe != null && state.heatLevel >= state.activeRecipe.requiredTemp;
        if (wasActive != state.active) { update = true; }
        ItemStack inputFilled = state.inventory.getStackInSlot(SLOT_INPUT_FILLED);
        if (!inputFilled.isEmpty()) {
            FluidActionResult res = FluidUtil.tryEmptyContainer(inputFilled, state.tanks.input(), Integer.MAX_VALUE, null, false);
            if (res.isSuccess()) {
                ItemStack resultItem = res.getResult();
                ItemStack inputEmpty = state.inventory.getStackInSlot(SLOT_INPUT_EMPTY);
                if (inputEmpty.isEmpty() || (ItemHandlerHelper.canItemStacksStack(resultItem, inputEmpty) && inputEmpty.getCount() + resultItem.getCount() <= inputEmpty.getMaxStackSize())) {
                    res = FluidUtil.tryEmptyContainer(inputFilled, state.tanks.input(), Integer.MAX_VALUE, null, true);
                    if (res.isSuccess()) { resultItem = res.getResult(); inputFilled.shrink(1); if (inputFilled.isEmpty()) { state.inventory.setStackInSlot(SLOT_INPUT_FILLED, ItemStack.EMPTY); } if (inputEmpty.isEmpty()) { state.inventory.setStackInSlot(SLOT_INPUT_EMPTY, resultItem); } else { inputEmpty.grow(resultItem.getCount()); } update = true; }
                }
            }
        }
        ItemStack outputEmpty = state.inventory.getStackInSlot(SLOT_OUTPUT_EMPTY);
        if (!outputEmpty.isEmpty()) {
            FluidActionResult res = FluidUtil.tryFillContainer(outputEmpty, state.tanks.output(), Integer.MAX_VALUE, null, false);
            if (res.isSuccess()) {
                ItemStack resultItem = res.getResult();
                ItemStack outputFilled = state.inventory.getStackInSlot(SLOT_OUTPUT_FILLED);
                if (outputFilled.isEmpty() || (ItemHandlerHelper.canItemStacksStack(resultItem, outputFilled) && outputFilled.getCount() + resultItem.getCount() <= outputFilled.getMaxStackSize())) {
                    res = FluidUtil.tryFillContainer(outputEmpty, state.tanks.output(), Integer.MAX_VALUE, null, true);
                    if (res.isSuccess()) { resultItem = res.getResult(); outputEmpty.shrink(1); if (outputEmpty.isEmpty()) { state.inventory.setStackInSlot(SLOT_OUTPUT_EMPTY, ItemStack.EMPTY); } if (outputFilled.isEmpty()) { state.inventory.setStackInSlot(SLOT_OUTPUT_FILLED, resultItem); } else { outputFilled.grow(resultItem.getCount()); } update = true; }
                }
            }
        }
        if (state.fluidOutput.isPresent()) {
            IFluidHandler outputHandler = state.fluidOutput.get();
            FluidStack fs = state.tanks.output().getFluid();
            if (fs.getAmount() > 0) {
                fs = fs.copy();
                int accepted = outputHandler.fill(fs, FluidAction.SIMULATE);
                if (accepted > 0) { int drained = outputHandler.fill(Utils.copyFluidStackWithAmount(fs, accepted, false), FluidAction.EXECUTE); state.tanks.output().drain(drained, FluidAction.EXECUTE); update = true; }
            }
        }
        IItemHandlerModifiable inventory = state.inventory;
        ItemStack drainedContainer = inventory.getStackInSlot(SLOT_INPUT_EMPTY);
        if (!drainedContainer.isEmpty()) { int origCount = drainedContainer.getCount(); drainedContainer = Utils.insertStackIntoInventory(state.outputRef, drainedContainer, false); if (drainedContainer.getCount() < origCount) { update = true; } inventory.setStackInSlot(SLOT_INPUT_EMPTY, drainedContainer); }
        ItemStack filledContainer = inventory.getStackInSlot(SLOT_OUTPUT_FILLED);
        if (!filledContainer.isEmpty()) { int origCount = filledContainer.getCount(); filledContainer = Utils.insertStackIntoInventory(state.outputRef, filledContainer, false); if (filledContainer.getCount() < origCount) { update = true; } inventory.setStackInSlot(SLOT_OUTPUT_FILLED, filledContainer); }
        if (update) { ctx.markMasterDirty(); ctx.requestMasterBESync(); }
    }

    private void updatePortNeighbors(IMultiblockLevel mlevel) { Level level = mlevel.getRawLevel(); BlockPos inputPos = mlevel.toAbsolute(INPUT_FLUID_POI.posInMultiblock()); level.updateNeighborsAt(inputPos, level.getBlockState(inputPos).getBlock()); BlockPos outputPos = mlevel.toAbsolute(OUTPUT_FLUID_POI.posInMultiblock()); level.updateNeighborsAt(outputPos, level.getBlockState(outputPos).getBlock()); }

    private double checkReflectorPositions(IMultiblockLevel mlevel, State state) {
        double totalMirrorStrength = 0;
        int count = 0;
        byte[] dirCountsTemp = new byte[4];
        final Level level = mlevel.getRawLevel();
        final BlockPos basePos = mlevel.toAbsolute(LINK_POI);
        final BlockPos collectorPos = mlevel.toAbsolute(REFLECTOR_POI);
        Set<BlockPos> reflectors = SolarRegistry.getReflectorsInRange(level, basePos, SOLAR_MIN_RANGE, SOLAR_MAX_RANGE);
        Set<BlockPos> unattached = new HashSet<>();
        for (BlockPos poiPos : reflectors) {
            BlockEntity be = level.getBlockEntity(poiPos);
            if (be instanceof IMultiblockBE<?> mbe) {
                IMultiblockBEHelper<?> helper = mbe.getHelper();
                if (helper != null && helper.getState() instanceof SolarReflectorLogic.State reflectorState) {
                    BlockPos currentTower = reflectorState.getTowerCollectorPosition();
                    if (currentTower.equals(collectorPos)) { if (reflectorState.setTowerCollectorPosition(collectorPos)) { totalMirrorStrength += reflectorState.getSolarCollectorStrength(); int dir = getReflectorDir(poiPos.getX() - basePos.getX(), poiPos.getZ() - basePos.getZ()); dirCountsTemp[dir]++; count++; } }
                    else { unattached.add(poiPos); }
                }
            }
        }
        for (BlockPos poiPos : unattached) {
            if (count >= 24) { break; }
            BlockEntity be = level.getBlockEntity(poiPos);
            if (be instanceof IMultiblockBE<?> mbe) {
                IMultiblockBEHelper<?> helper = mbe.getHelper();
                if (helper != null && helper.getState() instanceof SolarReflectorLogic.State reflectorState) {
                    if (!reflectorState.isMirrorTaken) { if (reflectorState.setTowerCollectorPosition(collectorPos)) { totalMirrorStrength += reflectorState.getSolarCollectorStrength(); int dir = getReflectorDir(poiPos.getX() - basePos.getX(), poiPos.getZ() - basePos.getZ()); dirCountsTemp[dir]++; count++; } }
                }
            }
        }
        state.dirCounts = dirCountsTemp;
        state.reflectorCount = (byte) count;
        return totalMirrorStrength;
    }

    private int getReflectorDir(int dx, int dz) { if (Math.abs(dx) > Math.abs(dz)) { if (dx > 0) { return 1; } else { return 3; } } else { if (dz > 0) { return 2; } else { return 0; } } }

    private boolean heatLogic(State state, Level level, boolean enabled) {
        double inc = enabled ? getTemperatureIncrease(state, level) : 0;
        double loss = getTemperatureLoss(state, level);
        double oldHeat = state.heatLevel;
        state.heatLevel = Math.max(0, state.heatLevel + inc - loss);
        double maxHeat = state.activeRecipe != null ? state.activeRecipe.requiredTemp : WORKING_HEAT_LEVEL;
        state.heatLevel = Math.min(maxHeat, state.heatLevel);
        return oldHeat != state.heatLevel;
    }

    private double getTemperatureIncrease(State state, Level level) {
        double inc = 0;
        if (state.registered && state.reflectorStrength > 0 && level.isDay() && !level.isRaining() && state.sunVisible) {
            double effectiveStrength = state.reflectorStrength;
            if (state.activeRecipe != null) {
                double minReflectors = state.activeRecipe.requiredTemp / TEMP_TO_MIN_REFLECTORS_DIVISOR;
                double bestReflectors = minReflectors + 2 * REFLECTOR_TIER_OFFSET;
                effectiveStrength = Math.min(state.reflectorStrength, bestReflectors);
            }
            inc = effectiveStrength * HEAT_INCREASE_FACTOR * getSolarIncidenceAngleSection(level);
        }
        return inc;
    }

    private double getTemperatureLoss(State state, Level level) {
        double loss = DAY_MIN_HEAT_LOSS;
        int section = getSolarIncidenceAngleSection(level);
        loss += LOSS_PER_SECTION_DROP * (4 - section);
        loss += state.heatLevel * TEMP_DEPENDENT_LOSS_FACTOR;
        return loss;
    }

    private boolean recipeLogic(State state, Level level, boolean enabled) {
        FluidStack fs = state.tanks.input().getFluid();
        if (fs.getAmount() <= 0) { state.activeRecipe = null; state.processProgress = 0; return false; }
        if (state.activeRecipe == null && state.activeRecipeId != null) { state.activeRecipe = SolarMelterRecipe.RECIPES.getById(level, state.activeRecipeId); state.activeRecipeId = null; }
        if (state.activeRecipe == null || !state.activeRecipe.input.testIgnoringAmount(fs)) { state.activeRecipe = SolarMelterRecipe.findRecipe(level, fs); state.processProgress = 0; if (state.activeRecipe == null) { return false; } }
        if (state.activeRecipe == null) { state.processProgress = 0; return false; }
        if (enabled && state.heatLevel >= state.activeRecipe.requiredTemp) { state.processProgress += (int) SPEED_MULTIPLIER; } else { state.processProgress = Math.max(0, state.processProgress - PROGRESS_LOSS_OFF_TEMP); }
        if (state.processProgress >= state.activeRecipe.getTotalProcessTime()) {
            assert state.activeRecipe.fluidOutput != null;
            FluidStack out = state.activeRecipe.fluidOutput.copy();
            if (state.tanks.output().fill(out, FluidAction.SIMULATE) == out.getAmount()) { state.tanks.input().drain(state.activeRecipe.input.getAmount(), FluidAction.EXECUTE); state.tanks.output().fill(out, FluidAction.EXECUTE); state.processProgress = 0; return true; }
        }
        return false;
    }

    private void detachReflectorPositions(State state) {
        Level level = state.levelSupplier.get();
        if (level == null || level.isClientSide) { return; }
        BlockPos collectorPos = state.collectorPos;
        Set<BlockPos> reflectors = SolarRegistry.getReflectorsInRange(level, state.basePos, SOLAR_MIN_RANGE, SOLAR_MAX_RANGE);
        for (BlockPos poiPos : reflectors) {
            BlockEntity be = level.getBlockEntity(poiPos);
            if (be instanceof IMultiblockBE<?> mbe) {
                IMultiblockBEHelper<?> helper = mbe.getHelper();
                if (helper != null && helper.getState() instanceof SolarReflectorLogic.State reflectorState) {
                    reflectorState.detachTower(collectorPos);
                }
            }
        }
    }

    public static int getSolarIncidenceAngleSection(Level level) { int skyDarken = level.getSkyDarken(); if (skyDarken == 3) { return 1; } else if (skyDarken == 2) { return 2; } else if (skyDarken == 1) { return 3; } else if (skyDarken == 0) { return 4; } return 0; }

    @Override
    public <T> LazyOptional<T> getCapability(IMultiblockContext<State> ctx, CapabilityPosition position, Capability<T> cap) {
        State state = ctx.getState();
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            if (position.posInMultiblock().equals(INPUT_FLUID_POI.posInMultiblock()) && (position.side() == null || position.side() == INPUT_FLUID_POI.side())) { return state.inputCap.cast(ctx); }
            if (position.posInMultiblock().equals(OUTPUT_FLUID_POI.posInMultiblock()) && (position.side() == null || position.side() == OUTPUT_FLUID_POI.side())) { return state.outputCap.cast(ctx); }
        }
        if (cap == ForgeCapabilities.ITEM_HANDLER) { if (ITEM_OUTPUT_POI.posInMultiblock().equals(position.posInMultiblock())) { return state.itemOutputCap.cast(ctx); } return state.invCap.cast(ctx); }
        return LazyOptional.empty();
    }

    @Override
    public Function<BlockPos, VoxelShape> shapeGetter(ShapeType shapeType) { return SolarMelterShape.GETTER; }

    @Override
    public State createInitialState(IInitialMultiblockContext<State> ctx) { return new State(ctx); }

    @Override
    public InteractionResult click(IMultiblockContext<State> ctx, BlockPos posInMultiblock, Player player, InteractionHand hand, BlockHitResult absoluteHit, boolean isClient) {
        if (isClient) { return InteractionResult.PASS; }
        State state = ctx.getState();
        if (state.registered) { return InteractionResult.PASS; }
        TranslationKey key = state.failVertical ? TranslationKey.SOLAR_VERTICAL_STACK : TranslationKey.SOLAR_TOO_CLOSE;
        int dist = state.failVertical ? -1 : state.requiredMove;
        ITPacketHandler.sendToPlayer(player, new ITOSDSyncBlock(key.location, dist));
        return InteractionResult.SUCCESS;
    }

    @Override
    public void dropExtraItems(State state, Consumer<ItemStack> drop) { Level level = state.levelSupplier.get(); if (level != null && !level.isClientSide) { detachReflectorPositions(state); SolarRegistry.unregisterTower(level, state.basePos); } ITMultiBlockInventoryUtils.dropItems(state.inventory, drop); }

    public static class State implements ITISolarMultiblockState {
        public final RedstoneControl.RSState rsState = RedstoneControl.RSState.enabledByDefault();
        public final ITSolarTank tanks;
        public final StoredCapability<IFluidHandler> inputCap;
        public final StoredCapability<IFluidHandler> outputCap;
        public final StoredCapability<IItemHandler> invCap;
        public final CapabilityReference<IFluidHandler> fluidOutput;
        public final StoredCapability<IItemHandler> itemOutputCap;
        public final CapabilityReference<IItemHandler> outputRef;
        public final ITSlotwiseItemHandler inventory;
        public double heatLevel = 0;
        public double reflectorStrength = 0;
        public byte reflectorCount = 0;
        public final BlockPos basePos;
        public final BlockPos collectorPos;
        public final BlockPos sunPos;
        public final Supplier<Level> levelSupplier;
        public byte[] dirCounts = new byte[4];
        public int processProgress = 0;
        public SolarMelterRecipe activeRecipe = null;
        private ResourceLocation activeRecipeId;
        public boolean isLoaded = false;
        public boolean registered;
        public boolean failVertical = false;
        public int requiredMove = 0;
        public boolean active;
        public BooleanSupplier isSoundPlaying = () -> false;
        private int soundId = 0;
        public boolean sunVisible = true;

        public State(IInitialMultiblockContext<State> ctx) {
            Runnable markDirty = ctx.getMarkDirtyRunnable();
            Runnable sync = ctx.getSyncRunnable();
            Runnable onChanged = () -> { markDirty.run(); sync.run(); };
            this.tanks = new ITSolarTank(v -> onChanged.run());
            inventory = new ITSlotwiseItemHandler(Lists.newArrayList(ITSlotwiseItemHandler.IOConstraint.FLUID_INPUT, ITSlotwiseItemHandler.IOConstraint.OUTPUT, ITSlotwiseItemHandler.IOConstraint.FLUID_INPUT, ITSlotwiseItemHandler.IOConstraint.OUTPUT), onChanged);
            this.inputCap = new StoredCapability<>(new ITArrayFluidHandler(tanks.input(), false, true, onChanged));
            this.outputCap = new StoredCapability<>(new ITArrayFluidHandler(tanks.output(), true, false, onChanged));
            this.invCap = new StoredCapability<>(inventory);
            MultiblockFace outputMBFace = new MultiblockFace(OUTPUT_FLUID_POI.side(), OUTPUT_FLUID_POI.posInMultiblock());
            CapabilityPosition opposingCP = CapabilityPosition.opposing(outputMBFace);
            MultiblockFace opposingMBFace = new MultiblockFace(opposingCP.side(), opposingCP.posInMultiblock());
            this.fluidOutput = ctx.getCapabilityAt(ForgeCapabilities.FLUID_HANDLER, opposingMBFace);
            this.itemOutputCap = new StoredCapability<>(new ITWrappingItemHandler(inventory, false, true, Lists.newArrayList(new ITWrappingItemHandler.IntRange(SLOT_INPUT_EMPTY, SLOT_INPUT_EMPTY + 1), new ITWrappingItemHandler.IntRange(SLOT_OUTPUT_FILLED, SLOT_OUTPUT_FILLED + 1))));
            this.outputRef = ctx.getCapabilityAt(ForgeCapabilities.ITEM_HANDLER, ITEM_OUTPUT_POI);
            InitialMultiblockContext<State> initialContext = (InitialMultiblockContext<State>) ctx;
            MultiblockOrientation orientation = initialContext.orientation();
            BlockPos masterOffset = initialContext.masterOffset();
            BlockPos masterPos = initialContext.masterBE().getBlockPos();
            BlockPos origin = masterPos.subtract(orientation.getAbsoluteOffset(masterOffset));
            this.basePos = origin.offset(orientation.getAbsoluteOffset(LINK_POI));
            this.collectorPos = origin.offset(orientation.getAbsoluteOffset(REFLECTOR_POI));
            this.sunPos = origin.offset(orientation.getAbsoluteOffset(SUN_POI));
            this.levelSupplier = ctx.levelSupplier();
            Level level = levelSupplier.get();
            SolarRegistry.RegisterResult result;
            if (level != null && !level.isClientSide) { result = SolarRegistry.registerTower(level, basePos); } else { result = new SolarRegistry.RegisterResult(); }
            this.registered = result.success;
            if (!this.registered) { this.failVertical = result.vertical; this.requiredMove = result.requiredMove; }
        }

        public ITSlotwiseItemHandler getInventory() { return inventory; }

        public ITSolarTank getTanks() { return tanks; }

        @Override
        public double getHeatLevel() { return heatLevel; }

        @Override
        public byte[] getDirCounts() { return dirCounts; }

        @Override
        public int getProcessProgress() { return processProgress; }

        @Override
        public boolean isSunVisible() { return sunVisible; }

        @Override
        public void writeSaveNBT(CompoundTag nbt) {
            nbt.put("tanks", this.tanks.toNBT());
            nbt.put("inventory", inventory.serializeNBT());
            nbt.putDouble("heatLevel", heatLevel);
            nbt.putDouble("reflectorStrength", reflectorStrength);
            nbt.putByte("reflectorCount", reflectorCount);
            nbt.putByteArray("dirCounts", dirCounts);
            nbt.putInt("processProgress", processProgress);
            if (activeRecipe != null) { nbt.putString("activeRecipe", activeRecipe.getId().toString()); }
            nbt.putBoolean("registered", registered);
            nbt.putBoolean("failVertical", failVertical);
            nbt.putInt("requiredMove", requiredMove);
            nbt.putBoolean("isLoaded", isLoaded);
            nbt.putBoolean("active", active);
        }

        @Override
        public void readSaveNBT(CompoundTag nbt) {
            this.tanks.readNBT(nbt.getCompound("tanks"));
            this.inventory.deserializeNBT(nbt.getCompound("inventory"));
            heatLevel = nbt.getDouble("heatLevel");
            reflectorStrength = nbt.getDouble("reflectorStrength");
            reflectorCount = nbt.getByte("reflectorCount");
            dirCounts = nbt.getByteArray("dirCounts");
            processProgress = nbt.getInt("processProgress");
            if (nbt.contains("activeRecipe")) { activeRecipeId = ResourceLocation.tryParse(nbt.getString("activeRecipe")); }
            registered = nbt.getBoolean("registered");
            failVertical = nbt.getBoolean("failVertical");
            requiredMove = nbt.getInt("requiredMove");
            isLoaded = nbt.getBoolean("isLoaded");
            active = nbt.getBoolean("active");
            Level level = levelSupplier.get();
            if (level != null && !level.isClientSide) {
                SolarRegistry.RegisterResult result = SolarRegistry.registerTower(level, basePos);
                this.registered = result.success;
                this.failVertical = result.vertical;
                this.requiredMove = result.requiredMove;
                if (!this.registered && nbt.getBoolean("registered")) {
                    int y = basePos.getY();
                    Set<BlockPos> towersAtY = SolarRegistry.getData(level).towerBasesByY.computeIfAbsent(y, k -> new HashSet<>());
                    towersAtY.add(basePos);
                    SolarRegistry.getData(level).setDirty();
                    this.registered = true;
                    this.failVertical = false;
                    this.requiredMove = 0;
                }
            }
        }

        @Override
        public void writeSyncNBT(CompoundTag nbt) {
            nbt.put("tanks", this.tanks.toNBT());
            nbt.putDouble("heatLevel", heatLevel);
            nbt.putDouble("reflectorStrength", reflectorStrength);
            nbt.putByteArray("dirCounts", dirCounts);
            nbt.putBoolean("sunVisible", sunVisible);
            nbt.putBoolean("active", active);
        }

        @Override
        public void readSyncNBT(CompoundTag nbt) {
            this.tanks.readNBT(nbt.getCompound("tanks"));
            heatLevel = nbt.getDouble("heatLevel");
            reflectorStrength = nbt.getDouble("reflectorStrength");
            dirCounts = nbt.getByteArray("dirCounts");
            sunVisible = nbt.getBoolean("sunVisible");
            active = nbt.getBoolean("active");
        }
    }
}

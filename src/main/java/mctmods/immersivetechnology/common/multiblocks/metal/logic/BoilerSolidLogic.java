package mctmods.immersivetechnology.common.multiblocks.metal.logic;

import com.immersiveconvergence.api.particles.ColoredSmoke;
import com.immersiveconvergence.api.block.ModProperties;
import com.immersiveconvergence.api.multiblock.IDisplayContext;
import com.immersiveconvergence.api.multiblock.MultiblockPOIHelper;
import com.immersiveconvergence.api.util.MultiBlockInventoryUtils;
import com.immersiveconvergence.api.util.ConstrainedItemHandler;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.BoilerSolidRecipe;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.BoilerSolidShape;
import mctmods.immersivetechnology.core.CommonConfig;
import mctmods.immersivetechnology.core.lib.Reference;
import com.immersiveconvergence.api.client.MachineSound;
import mctmods.immersivetechnology.core.registration.Sounds;
import mctmods.immersivetechnology.core.ServerConfig;
import mctmods.immersivetechnology.core.util.CachedRecipe;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IClientTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IServerTickableComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.RedstoneControl;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.*;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import com.google.common.collect.ImmutableList;
import com.immersiveconvergence.api.capability.HeatCapabilities;
import com.immersiveconvergence.api.capability.IHeatConsumer;
import com.immersiveconvergence.api.capability.IHeatProvider;
import com.immersiveconvergence.api.multiblock.PoIJSONSchema;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.BiFunction;

public class BoilerSolidLogic implements IMultiblockLogic<BoilerSolidLogic.State>, IServerTickableComponent<BoilerSolidLogic.State>, IClientTickableComponent<BoilerSolidLogic.State> {
    public static final int INPUT_FUEL_SLOT = 0;

    public static double heatLossPerTick() { return ServerConfig.boilerSolidHeatLossPerTick; }
    public static double defaultWorkingHeatLevel() { return CommonConfig.boilerDefaultWorkingHeat; }
    public static double pilotHeat() { return ServerConfig.boilerSolidPilotHeat; }
    public static int pilotMultiplier() { return ServerConfig.boilerSolidPilotMultiplier; }
    public static double defaultHeatPerTick() { return ServerConfig.boilerSolidDefaultHeatPerTick; }

    private static final List<PoIJSONSchema> RAW_POIS = ImmutableList.copyOf(BoilerSolidShape.DATA.pointsOfInterest);
    private static final int WIDTH = BoilerSolidShape.WIDTH;
    private static final int LENGTH = BoilerSolidShape.LENGTH;
    private static final int HEIGHT = BoilerSolidShape.HEIGHT;

    public static final BlockPos REDSTONE_POI = MultiblockPOIHelper.getPosList(RAW_POIS, "redstone0").get(0);
    private static final int[] COMPARATOR_POSITIONS_RAW = {0,0,0,0,0,1,0,0,2,0,1,0,0,1,1,0,1,2,0,2,0,0,2,1};
    public static final List<BlockPos> COMPARATOR_POSITIONS;
    static {
        ImmutableList.Builder<BlockPos> builder = ImmutableList.builder();
        for (int i = 0; i < COMPARATOR_POSITIONS_RAW.length; i += 3) { builder.add(new BlockPos(COMPARATOR_POSITIONS_RAW[i], COMPARATOR_POSITIONS_RAW[i + 1], COMPARATOR_POSITIONS_RAW[i + 2])); }
        COMPARATOR_POSITIONS = builder.build();
    }
    public static final List<BlockPos> IGNITION_POIS = MultiblockPOIHelper.getPosList(RAW_POIS, "ignition0");
    public static final List<BlockPos> ITEM_INPUT_POIS = MultiblockPOIHelper.getPosList(RAW_POIS, "item_input0");
    public static final List<BlockPos> HEAT_OUTPUT_POIS = MultiblockPOIHelper.getPosList(RAW_POIS, "heat_output0");
    public static final BlockPos SOUND_POI = MultiblockPOIHelper.getPosList(RAW_POIS, "sound0").get(0);
    public static final List<BlockPos> EXHAUST_POIS = MultiblockPOIHelper.getPosList(RAW_POIS, "exhaust0");
    private static final RelativeBlockFace ITEM_INPUT_FACING = MultiblockPOIHelper.getFacing(RAW_POIS, "item_input0");
    private static final RelativeBlockFace HEAT_OUTPUT_FACING = MultiblockPOIHelper.getFacing(RAW_POIS, "heat_output0");
    public static final RelativeBlockFace IGNITION_FACING = MultiblockPOIHelper.getFacing(RAW_POIS, "ignition0");

    @Override public void tickClient(IMultiblockContext<State> ctx) {
        final State state = ctx.getState();
        BlockPos soundAbs = ctx.getLevel().toAbsolute(SOUND_POI);
        Vec3 soundPos = new Vec3(soundAbs.getX() + 0.5, soundAbs.getY() + 0.5, soundAbs.getZ() + 0.5);
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) { return; }
        float attenuation = (float) Math.max(player.distanceToSqr(soundPos) / 8, 1);
        float currentLevel = (float) (state.heatLevel / state.workingHeatLevel);
        float vol = (2 * currentLevel) / attenuation;
        if (state.heatLevel > 0 && vol > 0.01f && !state.isSoundPlaying.getAsBoolean()) {
            state.isSoundPlaying = MachineSound.startSound(
                    () -> state.heatLevel > 0,
                    ctx.isValid(),
                    soundPos,
                    Sounds.boiler_solid,
                    () -> {
                        LocalPlayer p = Minecraft.getInstance().player;
                        if (p == null) { return 0f; }
                        float a = (float) Math.max(p.distanceToSqr(soundPos) / 8, 1);
                        return (2 * (float) (state.heatLevel / state.workingHeatLevel)) / a;
                    },
                    () -> (float) (state.heatLevel / state.workingHeatLevel)
            );
        }
        final Level level = ctx.getLevel().getRawLevel();
        if (state.pilotLit) {
            BlockPos exhaustAbs = ctx.getLevel().toAbsolute(EXHAUST_POIS.get(0));
            Vec3 flamePos = new Vec3(exhaustAbs.getX() + 0.5, exhaustAbs.getY() + 0.1, exhaustAbs.getZ() + 0.5);
            double velX = (level.random.nextFloat() * 0.0625 - 0.03125);
            double velY = 0.0625;
            double velZ = (level.random.nextFloat() * 0.0625 - 0.03125);
            level.addParticle(ParticleTypes.FLAME, flamePos.x, flamePos.y, flamePos.z, velX, velY, velZ);
        }
        boolean hasWater = state.boilerInput.isPresent() && state.boilerInput.get().getFluidAmount() > 0;
        if (state.pilotLit && state.heatLevel > pilotHeat() && state.rsState.isEnabled(ctx) && hasWater) {
            BlockPos exhaustAbs = ctx.getLevel().toAbsolute(EXHAUST_POIS.get(0));
            Vec3 smokePos = new Vec3(exhaustAbs.getX() + 0.5, exhaustAbs.getY() + 1.25, exhaustAbs.getZ() + 0.5);
            double velX = 0;
            double velY = 0.125;
            double velZ = 0;
            float r = 0.2F, g = 0.2F, b = 0.2F;
            level.addAlwaysVisibleParticle(new ColoredSmoke(r, g, b), smokePos.x, smokePos.y, smokePos.z, velX, velY, velZ);
        }
    }

    @Override public void tickServer(IMultiblockContext<State> ctx) {
        final State state = ctx.getState();
        final Level level = ctx.getLevel().getRawLevel();
        boolean update = false;
        double previousHeatLevel = state.heatLevel;
        boolean prevPilotLit = state.pilotLit;
        boolean hasWater = state.boilerInput.isPresent() && state.boilerInput.get().getFluidAmount() > 0;
        boolean fullMode = state.rsState.isEnabled(ctx) && hasWater;
        boolean valid = ctx.isValid().getAsBoolean();
        boolean isActive = state.pilotLit && fullMode && state.heatLevel >= state.workingHeatLevel && valid;
        if (state.active != isActive) {
            state.active = isActive;
            update = true;
            updateAllBlocks(ctx, level, state.active);
        }
        if (!state.pilotLit) {
            state.heatLevel = Math.max(state.heatLevel - heatLossPerTick(), 0);
            state.burnRemaining = 0;
            state.totalBurnTime = 0;
            state.workingHeatLevel = defaultWorkingHeatLevel();
        } else {
            if (state.burnRemaining > 0) {
                boolean consumeThisTick = fullMode || (level.getGameTime() % pilotMultiplier() == 0);
                if (consumeThisTick) { state.burnRemaining--; }
                if (fullMode) {
                    if (state.heatLevel < state.targetHeat) {
                        state.heatLevel = Math.min(state.heatLevel + state.heatPerTick, state.targetHeat);
                    } else {
                        state.heatLevel = Math.max(state.heatLevel - heatLossPerTick(), state.targetHeat);
                    }
                } else { state.heatLevel = Math.max(state.heatLevel - heatLossPerTick(), pilotHeat()); }
            } else {
                state.totalBurnTime = 0;
                ItemStack fuelStack = state.inventory.getStackInSlot(INPUT_FUEL_SLOT);
                BoilerSolidRecipe recipe = null;
                if (!fuelStack.isEmpty()) { recipe = state.recipeGetter.apply(level, fuelStack); }
                ItemStack single = fuelStack.copy();
                single.setCount(1);
                int burnTimePerItem = ForgeHooks.getBurnTime(single, RecipeType.SMELTING);
                double heatPerTick = defaultHeatPerTick();
                double targetHeat = defaultWorkingHeatLevel();
                int consumeAmount = 1;
                if (recipe != null) {
                    heatPerTick = recipe.getHeatPerTick();
                    targetHeat = recipe.getTargetHeat();
                    consumeAmount = recipe.input.getCount();
                    if (burnTimePerItem <= 0) { burnTimePerItem = 200; }
                }
                if (burnTimePerItem <= 0) {
                    state.pilotLit = false;
                    state.heatLevel = Math.max(state.heatLevel - heatLossPerTick(), 0);
                    state.workingHeatLevel = defaultWorkingHeatLevel();
                } else {
                    ItemStack consumed = state.inventory.getRawHandler().extractItem(INPUT_FUEL_SLOT, consumeAmount, false);
                    if (consumed.getCount() == consumeAmount) {
                        state.burnRemaining = (burnTimePerItem * consumeAmount) / ServerConfig.boilerSolidBurnTimeDivider;
                        state.totalBurnTime = state.burnRemaining;
                        state.heatPerTick = heatPerTick;
                        state.targetHeat = targetHeat;
                        state.workingHeatLevel = targetHeat;
                        state.pilotLit = true;
                    } else {
                        state.pilotLit = false;
                        state.heatLevel = Math.max(state.heatLevel - heatLossPerTick(), 0);
                        state.workingHeatLevel = defaultWorkingHeatLevel();
                    }
                }
            }
        }
        if (previousHeatLevel != state.heatLevel || prevPilotLit != state.pilotLit) { update = true; }
        int newComparatorValue = state.workingHeatLevel > 0 ? (int) Math.min(15, (15 * state.heatLevel) / state.workingHeatLevel) : 0;
        if (newComparatorValue != state.lastComparatorValue) { for (BlockPos pos : COMPARATOR_POSITIONS) { ctx.setComparatorOutputFor(pos, newComparatorValue); } state.lastComparatorValue = newComparatorValue; update = true; }
        if (update) {
            ctx.markMasterDirty();
            ctx.requestMasterBESync();
        }
    }

    private void updateAllBlocks(IMultiblockContext<State> ctx, Level level, boolean active) {
        if (level.isClientSide) { return; }
        ResourceLocation boilerRL = Reference.rl("boiler_solid");
        Block boilerBlock = ForgeRegistries.BLOCKS.getValue(boilerRL);
        if (boilerBlock == null) { return; }
        for (int y = 0; y < HEIGHT; y++) for (int z = 0; z < LENGTH; z++) for (int x = 0; x < WIDTH; x++) {
            BlockPos relPos = new BlockPos(x, y, z);
            BlockPos absPos = ctx.getLevel().toAbsolute(relPos);
            BlockState curr = level.getBlockState(absPos);
            if (curr.getBlock() == boilerBlock && curr.hasProperty(ModProperties.ACTIVE)) {
                BlockState newState = curr.setValue(ModProperties.ACTIVE, active);
                if (!curr.equals(newState)) { level.setBlock(absPos, newState, 3); }
            }
        }
    }

    @Override public <T> LazyOptional<T> getCapability(IMultiblockContext<State> ctx, CapabilityPosition position, Capability<T> cap) {
        BlockPos localPos = position.posInMultiblock();
        RelativeBlockFace side = position.side();
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            if (ITEM_INPUT_POIS.contains(localPos) && (side == null || side == ITEM_INPUT_FACING)) { return ctx.getState().inputFuelCap.cast(ctx); }
        } else if (cap == HeatCapabilities.HEAT_PROVIDER_CAPABILITY) {
            if (HEAT_OUTPUT_POIS.contains(localPos) && (side == null || side == HEAT_OUTPUT_FACING)) { return ctx.getState().heatSourceCap.cast(ctx); }
        }
        return LazyOptional.empty();
    }

    @Override public void dropExtraItems(State state, Consumer<ItemStack> drop) { MultiBlockInventoryUtils.dropItems(state.inventory, drop); }

    @Override public State createInitialState(IInitialMultiblockContext<State> ctx) { return new State(ctx); }

    @Override public Function<BlockPos, VoxelShape> shapeGetter(ShapeType shapeType) { return BoilerSolidShape.GETTER; }

    private static class FuelItemHandler extends ConstrainedItemHandler {
        private final Supplier<Level> levelSupplier;

        public FuelItemHandler(Supplier<Level> levelSupplier, List<IOConstraint> constraints, Runnable onChanged) {
            super(constraints, onChanged);
            this.levelSupplier = levelSupplier;
        }

        @Override public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (slot != INPUT_FUEL_SLOT || stack.isEmpty()) { return false; }
            ItemStack single = stack.copy(); single.setCount(1);
            Level l = levelSupplier != null ? levelSupplier.get() : null;
            if (l != null) {
                if (ForgeHooks.getBurnTime(single, RecipeType.SMELTING) > 0) { return true; }
                return BoilerSolidRecipe.findRecipe(l, single) != null;
            } else { return true; }
        }

        @Override @NotNull public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (!simulate && !isItemValid(slot, stack)) { return stack; }
            return super.insertItem(slot, stack, simulate);
        }
    }

    public static class State implements IMultiblockState, IDisplayContext {
        public final BiFunction<Level, ItemStack, BoilerSolidRecipe> recipeGetter = CachedRecipe.cached(BoilerSolidRecipe::findRecipe);
        public final RedstoneControl.RSState rsState = RedstoneControl.RSState.enabledByDefault();
        public StoredCapability<IItemHandlerModifiable> inputFuelCap;
        public StoredCapability<IHeatProvider> heatSourceCap;
        public CapabilityReference<IHeatConsumer> boilerInput;
        public ConstrainedItemHandler inventory;
        public double heatLevel = 0;
        public int burnRemaining = 0;
        public int totalBurnTime = 0;
        public double heatPerTick = 0;
        public double targetHeat = defaultWorkingHeatLevel();
        public double workingHeatLevel = defaultWorkingHeatLevel();
        public boolean pilotLit = false;
        public boolean active = false;
        public BooleanSupplier isSoundPlaying = () -> false;
        public boolean inventoryDirty = false;
        public int lastComparatorValue = -1;

        public State(IInitialMultiblockContext<State> ctx) {
            final Runnable markDirty = ctx.getMarkDirtyRunnable();
            final Runnable sync = ctx.getSyncRunnable();
            final Runnable onChanged = () -> { markDirty.run(); sync.run(); this.inventoryDirty = true; };
            inventory = new FuelItemHandler(ctx.levelSupplier(), List.of(ConstrainedItemHandler.IOConstraint.INPUT), onChanged);
            inputFuelCap = new StoredCapability<>(inventory);
            heatSourceCap = new StoredCapability<>(new HeatSourceImpl(this));
            MultiblockFace heatMBFace = new MultiblockFace(HEAT_OUTPUT_FACING, HEAT_OUTPUT_POIS.get(0));
            CapabilityPosition opposingCP = CapabilityPosition.opposing(heatMBFace);
            MultiblockFace opposingMBFace = new MultiblockFace(opposingCP.side(), opposingCP.posInMultiblock());
            boilerInput = ctx.getCapabilityAt(HeatCapabilities.HEAT_CONSUMER_CAPABILITY, opposingMBFace);
        }

        public double getWorkingHeatLevel() { return workingHeatLevel; }

        @Override public void writeSaveNBT(CompoundTag nbt) {
            nbt.putDouble("heatLevel", heatLevel);
            nbt.putInt("burnRemaining", burnRemaining);
            nbt.putInt("totalBurnTime", totalBurnTime);
            nbt.putDouble("heatPerTick", heatPerTick);
            nbt.putDouble("targetHeat", targetHeat);
            nbt.putBoolean("pilotLit", pilotLit);
            nbt.put("inventory", inventory.serializeNBT());
        }

        @Override public void readSaveNBT(CompoundTag nbt) {
            heatLevel = nbt.getDouble("heatLevel");
            burnRemaining = nbt.getInt("burnRemaining");
            totalBurnTime = nbt.getInt("totalBurnTime");
            heatPerTick = nbt.getDouble("heatPerTick");
            targetHeat = nbt.getDouble("targetHeat");
            pilotLit = nbt.getBoolean("pilotLit");
            inventory.deserializeNBT(nbt.getCompound("inventory"));
            inventoryDirty = false;
        }

        @Override public void writeSyncNBT(CompoundTag nbt) {
            CompoundTag display = new CompoundTag();
            writeDisplaySyncNBT(display);
            nbt.put("display", display);
        }

        @Override public void readSyncNBT(CompoundTag nbt) {
            if (nbt.contains("display", Tag.TAG_COMPOUND)) { readDisplaySyncNBT(nbt.getCompound("display")); }
        }

        @Override public boolean isActive() { return active; }

        @Override public IItemHandlerModifiable getInventory() { return inventory; }

        @Override public void writeDisplaySyncNBT(CompoundTag nbt) {
            nbt.putBoolean("active", active);
            nbt.putDouble("heatLevel", heatLevel);
            nbt.putBoolean("pilotLit", pilotLit);
            nbt.putInt("burnRemaining", burnRemaining);
            nbt.putInt("totalBurnTime", totalBurnTime);
            nbt.put("inventory", inventory.serializeNBT());
            nbt.putDouble("workingHeatLevel", workingHeatLevel);
        }

        @Override public void readDisplaySyncNBT(CompoundTag nbt) {
            active = nbt.getBoolean("active");
            heatLevel = nbt.getDouble("heatLevel");
            pilotLit = nbt.getBoolean("pilotLit");
            burnRemaining = nbt.getInt("burnRemaining");
            totalBurnTime = nbt.getInt("totalBurnTime");
            inventory.deserializeNBT(nbt.getCompound("inventory"));
            workingHeatLevel = nbt.getDouble("workingHeatLevel");
            inventoryDirty = false;
        }
    }

    private record HeatSourceImpl(State state) implements IHeatProvider {
        @Override public double getHeatLevel() { return state.heatLevel; }
    }
}

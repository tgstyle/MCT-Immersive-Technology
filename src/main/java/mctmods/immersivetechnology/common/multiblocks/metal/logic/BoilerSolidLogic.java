package mctmods.immersivetechnology.common.multiblocks.metal.logic;

import mctmods.immersivetechnology.client.util.ClientUtils;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.BoilerSolidRecipe;
import mctmods.immersivetechnology.core.CommonConfig;
import mctmods.immersivetechnology.core.ServerConfig;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.registration.Sounds;
import mctmods.immersivetechnology.core.util.HeatUtils;
import mctmods.immersivetechnology.core.util.IDisplaySyncState;

import com.immersiveconvergence.api.multiblock.blocks.component.IClientTickableComponent;
import com.immersiveconvergence.api.multiblock.blocks.component.IMultiblockComponent;
import com.immersiveconvergence.api.multiblock.blocks.component.IServerTickableComponent;
import com.immersiveconvergence.api.multiblock.blocks.component.RedstoneControl;
import com.immersiveconvergence.api.multiblock.blocks.env.IInitialMultiblockContext;
import com.immersiveconvergence.api.multiblock.blocks.env.IMultiblockContext;
import com.immersiveconvergence.api.multiblock.blocks.logic.IMultiblockLogic;
import com.immersiveconvergence.api.multiblock.blocks.util.*;
import com.google.common.collect.ImmutableList;
import com.immersiveconvergence.api.block.ModProperties;
import com.immersiveconvergence.api.capability.HeatCapabilities;
import com.immersiveconvergence.api.capability.IHeatConsumer;
import com.immersiveconvergence.api.capability.IHeatProvider;
import com.immersiveconvergence.api.client.MachineSound;
import com.immersiveconvergence.api.integration.DisplayLines;
import com.immersiveconvergence.api.multiblock.IDataReloadAware;
import com.immersiveconvergence.api.multiblock.MultiblockPOIHelper;
import com.immersiveconvergence.api.multiblock.PoIJSONSchema;
import com.immersiveconvergence.api.multiblock.ShapeData;
import com.immersiveconvergence.api.util.ConstrainedItemHandler;
import com.immersiveconvergence.api.util.MultiBlockInventoryUtils;
import com.immersiveconvergence.api.util.RecipeCache;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nonnull;

public class BoilerSolidLogic implements IMultiblockLogic<BoilerSolidLogic.State>, IServerTickableComponent<BoilerSolidLogic.State>, IClientTickableComponent<BoilerSolidLogic.State> {
    private static final ShapeData SHAPE = ITShapes.get("boiler_solid");
    public static final int INPUT_FUEL_SLOT = 0;
    private static final int WIDTH = SHAPE.width;
    private static final int LENGTH = SHAPE.length;
    private static final int HEIGHT = SHAPE.height;
    public static final List<BlockPos> COMPARATOR_POSITIONS = ImmutableList.of(new BlockPos(0, 0, 0), new BlockPos(0, 0, 1), new BlockPos(0, 0, 2), new BlockPos(0, 1, 0), new BlockPos(0, 1, 1), new BlockPos(0, 1, 2), new BlockPos(0, 2, 0), new BlockPos(0, 2, 1));
    public static BlockPos REDSTONE_POI;
    public static List<BlockPos> IGNITION_POIS;
    public static List<BlockPos> ITEM_INPUT_POIS;
    public static List<BlockPos> HEAT_OUTPUT_POIS;
    public static BlockPos SOUND_POI;
    public static List<BlockPos> EXHAUST_POIS;
    private static RelativeBlockFace ITEM_INPUT_FACING;
    public static RelativeBlockFace HEAT_OUTPUT_FACING;
    public static RelativeBlockFace IGNITION_FACING;

    static { ITShapes.readPois("boiler_solid", BoilerSolidLogic::loadPois); }

    private static double heatLossPerTick() { return ServerConfig.boilerSolidHeatLossPerTick; }

    public static double defaultWorkingHeatLevel() { return CommonConfig.boilerDefaultWorkingHeat(); }

    public static double pilotHeat() { return ServerConfig.boilerSolidPilotHeat; }

    @Override public void tickClient(IMultiblockContext<State> ctx) {
        State state = ctx.getState();
        if (Minecraft.getInstance().player == null) { return; }
        Vec3 soundPos = Vec3.atCenterOf(ctx.getLevel().toAbsolute(SOUND_POI));
        if (state.heatLevel > 0 && ClientUtils.attenuated(soundPos, 8, 2 * (float) (state.heatLevel / state.workingHeatLevel)) > 0.01f && !state.isSoundPlaying.getAsBoolean()) {
            state.isSoundPlaying = MachineSound.startSound(() -> state.heatLevel > 0, ctx.isValid(), soundPos, Sounds.boiler_solid, () -> ClientUtils.attenuated(soundPos, 8, 2 * (float) (state.heatLevel / state.workingHeatLevel)), () -> (float) (state.heatLevel / state.workingHeatLevel));
        }
        if (state.pilotLit) { ClientUtils.boilerExhaust(ctx.getLevel().getRawLevel(), ctx.getLevel().toAbsolute(EXHAUST_POIS.getFirst()), state.heatLevel > pilotHeat() && state.rsState.isEnabled(ctx) && HeatUtils.hasWater(state.boilerInput)); }
    }

    @Override public void tickServer(IMultiblockContext<State> ctx) {
        State state = ctx.getState();
        Level level = ctx.getLevel().getRawLevel();
        double previousHeatLevel = state.heatLevel;
        boolean prevPilotLit = state.pilotLit;
        boolean fullMode = state.rsState.isEnabled(ctx) && HeatUtils.hasWater(state.boilerInput);
        boolean isActive = state.pilotLit && fullMode && state.heatLevel >= state.workingHeatLevel && ctx.isValid().getAsBoolean();
        boolean update = state.active != isActive;
        if (update) {
            state.active = isActive;
            updateAllBlocks(ctx, level, isActive);
        }
        if (!state.pilotLit) {
            state.heatLevel = Math.max(state.heatLevel - heatLossPerTick(), 0);
            state.burnRemaining = 0;
            state.totalBurnTime = 0;
            state.workingHeatLevel = defaultWorkingHeatLevel();
        }
        else if (state.burnRemaining > 0) {
            if (fullMode || level.getGameTime() % ServerConfig.boilerSolidPilotMultiplier == 0) { state.burnRemaining--; }
            if (!fullMode) { state.heatLevel = Math.max(state.heatLevel - heatLossPerTick(), pilotHeat()); }
            else if (state.heatLevel < state.targetHeat) { state.heatLevel = Math.min(state.heatLevel + state.heatPerTick, state.targetHeat); }
            else { state.heatLevel = Math.max(state.heatLevel - heatLossPerTick(), state.targetHeat); }
        }
        else if (!refuel(state, level)) {
            state.pilotLit = false;
            state.heatLevel = Math.max(state.heatLevel - heatLossPerTick(), 0);
            state.workingHeatLevel = defaultWorkingHeatLevel();
        }
        if (previousHeatLevel != state.heatLevel || prevPilotLit != state.pilotLit) { update = true; }
        int newComparatorValue = state.workingHeatLevel > 0 ? (int) Math.min(15, (15 * state.heatLevel) / state.workingHeatLevel) : 0;
        if (ctx.updateComparatorOutputs(state.lastComparatorValue, newComparatorValue, COMPARATOR_POSITIONS)) { update = true; }
        state.lastComparatorValue = newComparatorValue;
        if (update) { ctx.markDirtyAndSync(); }
    }

    private static boolean refuel(State state, Level level) {
        state.totalBurnTime = 0;
        ItemStack fuelStack = state.inventory.getStackInSlot(INPUT_FUEL_SLOT);
        BoilerSolidRecipe recipe = fuelStack.isEmpty() ? null : state.recipeGetter.apply(level, fuelStack);
        int burnTimePerItem = fuelStack.copyWithCount(1).getBurnTime(RecipeType.SMELTING);
        double heatPerTick = ServerConfig.boilerSolidDefaultHeatPerTick;
        double targetHeat = defaultWorkingHeatLevel();
        int consumeAmount = 1;
        if (recipe != null) {
            heatPerTick = recipe.getHeatPerTick();
            targetHeat = recipe.getTargetHeat();
            consumeAmount = recipe.input().getCount();
            if (burnTimePerItem <= 0) { burnTimePerItem = 200; }
        }
        if (burnTimePerItem <= 0 || state.inventory.getRawHandler().extractItem(INPUT_FUEL_SLOT, consumeAmount, false).getCount() != consumeAmount) { return false; }
        state.burnRemaining = (burnTimePerItem * consumeAmount) / ServerConfig.boilerSolidBurnTimeDivider;
        state.totalBurnTime = state.burnRemaining;
        state.heatPerTick = heatPerTick;
        state.targetHeat = targetHeat;
        state.workingHeatLevel = targetHeat;
        state.pilotLit = true;
        return true;
    }

    private static void updateAllBlocks(IMultiblockContext<State> ctx, Level level, boolean active) {
        Block boilerBlock = BuiltInRegistries.BLOCK.get(Reference.rl("boiler_solid"));
        for (int y = 0; y < HEIGHT; y++) {
            for (int z = 0; z < LENGTH; z++) {
                for (int x = 0; x < WIDTH; x++) {
                    BlockPos absPos = ctx.getLevel().toAbsolute(new BlockPos(x, y, z));
                    BlockState curr = level.getBlockState(absPos);
                    if (curr.getBlock() == boilerBlock && curr.hasProperty(ModProperties.ACTIVE) && curr.getValue(ModProperties.ACTIVE) != active) { level.setBlock(absPos, curr.setValue(ModProperties.ACTIVE, active), 3); }
                }
            }
        }
    }

    @Override public void registerCapabilities(IMultiblockComponent.CapabilityRegistrar<State> register) {
        register.register(Capabilities.ItemHandler.BLOCK, (state, position) -> {
            RelativeBlockFace side = position.side();
            return ITEM_INPUT_POIS.contains(position.posInMultiblock()) && (side == null || side == ITEM_INPUT_FACING) ? state.inventory : null;
        });
        register.register(HeatCapabilities.HEAT_PROVIDER, (state, position) -> {
            RelativeBlockFace side = position.side();
            return HEAT_OUTPUT_POIS.contains(position.posInMultiblock()) && (side == null || side == HEAT_OUTPUT_FACING) ? state.heatProvider : null;
        });
    }

    @Override public void dropExtraItems(State state, Consumer<ItemStack> drop) { MultiBlockInventoryUtils.dropItems(state.inventory, drop); }

    @Override public State createInitialState(IInitialMultiblockContext<State> ctx) { return new State(ctx); }

    @Override public Function<BlockPos, VoxelShape> shapeGetter(ShapeType shapeType) { return ITShapes.get("boiler_solid").getter; }

    private static class FuelItemHandler extends ConstrainedItemHandler {
        private final Supplier<Level> levelSupplier;

        public FuelItemHandler(Supplier<Level> levelSupplier, Runnable onChanged) {
            super(List.of(IOConstraint.INPUT), onChanged);
            this.levelSupplier = levelSupplier;
        }

        @Override public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            if (slot != INPUT_FUEL_SLOT || stack.isEmpty()) { return false; }
            ItemStack single = stack.copyWithCount(1);
            Level level = levelSupplier.get();
            return level == null || single.getBurnTime(RecipeType.SMELTING) > 0 || BoilerSolidRecipe.findRecipe(level, single) != null;
        }

        @Override @Nonnull public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            if (!simulate && !isItemValid(slot, stack)) { return stack; }
            return super.insertItem(slot, stack, simulate);
        }
    }

    public static class State implements IDisplaySyncState, IDataReloadAware {
        public final BiFunction<Level, ItemStack, BoilerSolidRecipe> recipeGetter = RecipeCache.cached(BoilerSolidRecipe::findRecipe);
        public final RedstoneControl.RSState rsState = RedstoneControl.RSState.enabledByDefault();
        public final IHeatProvider heatProvider = () -> this.heatLevel;
        public Supplier<IHeatConsumer> boilerInput;
        public final ConstrainedItemHandler inventory;
        public double heatLevel = 0;
        public int burnRemaining = 0;
        public int totalBurnTime = 0;
        public double heatPerTick = 0;
        public double targetHeat = defaultWorkingHeatLevel();
        public double workingHeatLevel = defaultWorkingHeatLevel();
        public boolean pilotLit = false;
        public boolean active = false;
        public BooleanSupplier isSoundPlaying = () -> false;
        public int lastComparatorValue = -1;

        public State(IInitialMultiblockContext<State> ctx) {
            Runnable markDirty = ctx.getMarkDirtyRunnable();
            Runnable sync = ctx.getSyncRunnable();
            inventory = new FuelItemHandler(ctx.levelSupplier(), () -> { markDirty.run(); sync.run(); });
            bindOutputs(ctx);
        }

        @Override public void onDataReload(IInitialMultiblockContext<?> context) {
            lastComparatorValue = -1;
            bindOutputs(context);
        }

        private void bindOutputs(IInitialMultiblockContext<?> context) { boilerInput = context.getCapabilityAt(HeatCapabilities.HEAT_CONSUMER, MultiblockPOIHelper.opposing(HEAT_OUTPUT_FACING, HEAT_OUTPUT_POIS.getFirst())); }

        public double getWorkingHeatLevel() { return workingHeatLevel; }

        @Override public void writeSaveNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            rsState.writeSaveNBT(nbt, provider);
            nbt.putDouble("heatLevel", heatLevel);
            nbt.putInt("burnRemaining", burnRemaining);
            nbt.putInt("totalBurnTime", totalBurnTime);
            nbt.putDouble("heatPerTick", heatPerTick);
            nbt.putDouble("targetHeat", targetHeat);
            nbt.putBoolean("pilotLit", pilotLit);
            nbt.put("inventory", inventory.serializeNBT(provider));
        }

        @Override public void readSaveNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            rsState.readSaveNBT(nbt, provider);
            heatLevel = nbt.getDouble("heatLevel");
            burnRemaining = nbt.getInt("burnRemaining");
            totalBurnTime = nbt.getInt("totalBurnTime");
            heatPerTick = nbt.getDouble("heatPerTick");
            targetHeat = nbt.getDouble("targetHeat");
            pilotLit = nbt.getBoolean("pilotLit");
            inventory.deserializeNBT(provider, nbt.getCompound("inventory"));
        }

        @Override public boolean isActive() { return active; }

        @Override public IItemHandlerModifiable getInventory() { return inventory; }

        @Override public void writeDisplaySyncNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            nbt.putBoolean("active", active);
            nbt.putDouble("heatLevel", heatLevel);
            nbt.putBoolean("pilotLit", pilotLit);
            nbt.putInt("burnRemaining", burnRemaining);
            nbt.putInt("totalBurnTime", totalBurnTime);
            nbt.put("inventory", inventory.serializeNBT(provider));
            nbt.putDouble("workingHeatLevel", workingHeatLevel);
        }

        @Override public void readDisplaySyncNBT(CompoundTag nbt, HolderLookup.Provider provider) {
            active = nbt.getBoolean("active");
            heatLevel = nbt.getDouble("heatLevel");
            pilotLit = nbt.getBoolean("pilotLit");
            burnRemaining = nbt.getInt("burnRemaining");
            totalBurnTime = nbt.getInt("totalBurnTime");
            inventory.deserializeNBT(provider, nbt.getCompound("inventory"));
            workingHeatLevel = nbt.getDouble("workingHeatLevel");
        }

        @Override public void addDisplayLines(Level level, DisplayLines lines) {
            lines.temperature(heatLevel, workingHeatLevel).percent((totalBurnTime > 0 && burnRemaining > 0) ? (totalBurnTime - burnRemaining) * 100 / totalBurnTime : 0);
            if (inventory.getStackInSlot(INPUT_FUEL_SLOT).isEmpty()) { lines.fuelEmpty(); }
        }
    }

    private static void loadPois(List<PoIJSONSchema> pois) {
        REDSTONE_POI = MultiblockPOIHelper.getPosList(pois, "redstone0").getFirst();
        IGNITION_POIS = MultiblockPOIHelper.getPosList(pois, "ignition0");
        ITEM_INPUT_POIS = MultiblockPOIHelper.getPosList(pois, "item_input0");
        HEAT_OUTPUT_POIS = MultiblockPOIHelper.getPosList(pois, "heat_output0");
        SOUND_POI = MultiblockPOIHelper.getPosList(pois, "sound0").getFirst();
        EXHAUST_POIS = MultiblockPOIHelper.getPosList(pois, "exhaust0");
        ITEM_INPUT_FACING = MultiblockPOIHelper.getFacing(pois, "item_input0");
        HEAT_OUTPUT_FACING = MultiblockPOIHelper.getFacing(pois, "heat_output0");
        IGNITION_FACING = MultiblockPOIHelper.getFacing(pois, "ignition0");
    }
}

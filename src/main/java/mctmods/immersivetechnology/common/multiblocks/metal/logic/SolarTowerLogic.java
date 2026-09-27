package mctmods.immersivetechnology.common.multiblocks.metal.logic;

import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.SolarTowerRecipe;
import mctmods.immersivetechnology.core.CommonConfig;
import mctmods.immersivetechnology.core.ServerConfig;
import mctmods.immersivetechnology.core.registration.Sounds;

import blusunrize.immersiveengineering.api.multiblocks.blocks.component.RedstoneControl;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.RelativeBlockFace;
import com.immersiveconvergence.api.multiblock.MultiblockPOIHelper;
import com.immersiveconvergence.api.multiblock.PoIJSONSchema;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import java.util.List;
import java.util.function.Supplier;

public class SolarTowerLogic extends SolarCollectorLogic<SolarTowerRecipe, SolarTowerLogic.State> {
    public static BlockPos REDSTONE_POI;
    public static BlockPos RUNNING_SOUND_POI;
    public static BlockPos LINK_POI;
    public static BlockPos REFLECTOR_POI;
    public static BlockPos SUN_POI;
    public static List<BlockPos> INPUT_FLUID_POIS;
    public static List<BlockPos> OUTPUT_FLUID_POIS;
    private static RelativeBlockFace INPUT_FLUID_FACING;
    private static RelativeBlockFace OUTPUT_FLUID_FACING;

    static { ITShapes.readPois("solar_tower", SolarTowerLogic::loadPois); }

    public static double workingHeatLevel() { return CommonConfig.solarTowerWorkingHeatLevel; }

    @Override protected double dayMinHeatLoss() { return ServerConfig.solarTowerDayMinHeatLoss; }
    @Override protected double lossPerSectionDrop() { return ServerConfig.solarTowerLossPerSectionDrop; }
    @Override protected double tempDependentLossFactor() { return ServerConfig.solarTowerTempDependentLossFactor; }
    @Override protected double heatIncreaseFactor() { return ServerConfig.solarTowerHeatIncreaseFactor; }
    @Override protected double tempToMinReflectorsDivisor() { return ServerConfig.solarTowerTempToMinReflectorsDivisor; }
    @Override protected double reflectorTierOffset() { return ServerConfig.solarTowerReflectorTierOffset; }
    @Override protected int progressLossOffTemp() { return ServerConfig.solarTowerProgressLossOffTemp; }
    @Override protected float speedMultiplier() { return (float) ServerConfig.solarTowerSpeedMultiplier; }

    @Override protected String shapeName() { return "solar_tower"; }
    @Override protected BlockPos redstonePoi() { return REDSTONE_POI; }
    @Override protected boolean enabled(IMultiblockContext<State> ctx) { return ctx.getState().rsState.isEnabled(ctx); }
    @Override protected BlockPos soundPoi() { return RUNNING_SOUND_POI; }
    @Override protected BlockPos linkPoi() { return LINK_POI; }
    @Override protected BlockPos reflectorPoi() { return REFLECTOR_POI; }
    @Override protected List<BlockPos> inputPois() { return INPUT_FLUID_POIS; }
    @Override protected RelativeBlockFace inputFacing() { return INPUT_FLUID_FACING; }
    @Override protected RelativeBlockFace outputFacing() { return OUTPUT_FLUID_FACING; }
    @Override protected Supplier<SoundEvent> sound() { return Sounds.solarTower; }
    @Override protected double soundFalloff() { return 32; }

    @Override public List<BlockPos> getOutputPositions() { return OUTPUT_FLUID_POIS; }

    @Override public State createInitialState(IInitialMultiblockContext<State> ctx) { return new State(ctx); }

    public static class State extends CollectorState<SolarTowerRecipe> {
        public final RedstoneControl.RSState rsState = RedstoneControl.RSState.enabledByDefault();

        public State(IInitialMultiblockContext<State> ctx) { super(ctx, ServerConfig.solarTowerInputTankCapacity, ServerConfig.solarTowerOutputTankCapacity, SolarTowerRecipe::findRecipe, SolarTowerRecipe.RECIPES, LINK_POI, REFLECTOR_POI, SUN_POI); }

        @Override protected double workingHeat() { return workingHeatLevel(); }

        @Override public void writeSaveNBT(CompoundTag nbt) {
            super.writeSaveNBT(nbt);
            rsState.writeSaveNBT(nbt);
        }

        @Override public void readSaveNBT(CompoundTag nbt) {
            super.readSaveNBT(nbt);
            rsState.readSaveNBT(nbt);
        }
    }

    private static void loadPois(List<PoIJSONSchema> pois) {
        REDSTONE_POI = MultiblockPOIHelper.getPosList(pois, "redstone0").get(0);
        RUNNING_SOUND_POI = MultiblockPOIHelper.getPosList(pois, "sound0").get(0);
        LINK_POI = MultiblockPOIHelper.getPosList(pois, "link0").get(0);
        REFLECTOR_POI = MultiblockPOIHelper.getPosList(pois, "reflector0").get(0);
        SUN_POI = MultiblockPOIHelper.getPosList(pois, "sun0").get(0);
        INPUT_FLUID_POIS = MultiblockPOIHelper.getPosList(pois, "fluid_input0");
        OUTPUT_FLUID_POIS = MultiblockPOIHelper.getPosList(pois, "fluid_output0");
        INPUT_FLUID_FACING = MultiblockPOIHelper.getFacing(pois, "fluid_input0");
        OUTPUT_FLUID_FACING = MultiblockPOIHelper.getFacing(pois, "fluid_output0");
    }
}

package mctmods.immersivetechnology.common.multiblocks.metal.logic;

import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.MeltingRecipe;
import mctmods.immersivetechnology.core.CommonConfig;
import mctmods.immersivetechnology.core.ServerConfig;
import mctmods.immersivetechnology.core.registration.Sounds;
import mctmods.immersivetechnology.core.util.RedstoneInput;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.RelativeBlockFace;
import com.immersiveconvergence.api.multiblock.MultiblockPOIHelper;
import com.immersiveconvergence.api.multiblock.PoIJSONSchema;
import com.immersiveconvergence.api.particles.ColoredSmoke;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.Level;
import java.util.List;
import java.util.function.Supplier;

public class SolarMelterLogic extends SolarCollectorLogic<MeltingRecipe, SolarMelterLogic.State> {
    public static BlockPos REDSTONE_POI;
    public static BlockPos RUNNING_SOUND_POI;
    public static BlockPos LINK_POI;
    public static BlockPos PARTICLE_POI;
    public static BlockPos REFLECTOR_POI;
    public static BlockPos SUN_POI;
    public static List<BlockPos> INPUT_FLUID_POIS;
    public static List<BlockPos> OUTPUT_FLUID_POIS;
    private static RelativeBlockFace INPUT_FLUID_FACING;
    private static RelativeBlockFace OUTPUT_FLUID_FACING;

    static { ITShapes.readPois("solar_melter", SolarMelterLogic::loadPois); }

    public static double workingHeatLevel() { return CommonConfig.solarMelterWorkingHeatLevel; }

    @Override protected double dayMinHeatLoss() { return ServerConfig.solarMelterDayMinHeatLoss; }
    @Override protected double lossPerSectionDrop() { return ServerConfig.solarMelterLossPerSectionDrop; }
    @Override protected double tempDependentLossFactor() { return ServerConfig.solarMelterTempDependentLossFactor; }
    @Override protected double heatIncreaseFactor() { return ServerConfig.solarMelterHeatIncreaseFactor; }
    @Override protected double tempToMinReflectorsDivisor() { return ServerConfig.solarMelterTempToMinReflectorsDivisor; }
    @Override protected double reflectorTierOffset() { return ServerConfig.solarMelterReflectorTierOffset; }
    @Override protected int progressLossOffTemp() { return ServerConfig.solarMelterProgressLossOffTemp; }
    @Override protected float speedMultiplier() { return (float) ServerConfig.solarMelterSpeedMultiplier; }

    @Override protected String shapeName() { return "solar_melter"; }
    @Override protected BlockPos redstonePoi() { return REDSTONE_POI; }
    @Override protected boolean enabled(IMultiblockContext<State> ctx) { return RedstoneInput.unpowered(ctx, REDSTONE_POI); }
    @Override protected BlockPos soundPoi() { return RUNNING_SOUND_POI; }
    @Override protected BlockPos linkPoi() { return LINK_POI; }
    @Override protected BlockPos reflectorPoi() { return REFLECTOR_POI; }
    @Override protected List<BlockPos> inputPois() { return INPUT_FLUID_POIS; }
    @Override protected RelativeBlockFace inputFacing() { return INPUT_FLUID_FACING; }
    @Override protected RelativeBlockFace outputFacing() { return OUTPUT_FLUID_FACING; }
    @Override protected Supplier<SoundEvent> sound() { return Sounds.solarMelter; }
    @Override protected double soundFalloff() { return 8; }

    @Override public List<BlockPos> getOutputPositions() { return OUTPUT_FLUID_POIS; }

    @Override public State createInitialState(IInitialMultiblockContext<State> ctx) { return new State(ctx); }

    @Override public void tickClient(IMultiblockContext<State> ctx) {
        super.tickClient(ctx);
        State state = ctx.getState();
        Level clientLevel = ctx.getLevel().getRawLevel();
        if (state.isHeated(clientLevel)) {
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
                    clientLevel.addParticle(particleData, px, py, pz, 0.0D, 0.21D, 0.0D);
                }
            }
            if (clientLevel != null && clientLevel.getGameTime() % 10 == 0) {
                BlockPos splashPos = ctx.getLevel().toAbsolute(PARTICLE_POI);
                double px = splashPos.getX() + 0.5 + (clientLevel.random.nextDouble() - 0.5) * 0.5;
                double py = splashPos.getY() + 1.0;
                double pz = splashPos.getZ() + 0.5 + (clientLevel.random.nextDouble() - 0.5) * 0.3;
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

    public static class State extends CollectorState<MeltingRecipe> {
        public State(IInitialMultiblockContext<State> ctx) { super(ctx, ServerConfig.solarMelterInputTankCapacity, ServerConfig.solarMelterOutputTankCapacity, MeltingRecipe::findRecipe, MeltingRecipe.RECIPES, LINK_POI, REFLECTOR_POI, SUN_POI); }

        @Override protected double workingHeat() { return workingHeatLevel(); }
    }

    private static void loadPois(List<PoIJSONSchema> pois) {
        REDSTONE_POI = MultiblockPOIHelper.getPosList(pois, "redstone0").getFirst();
        RUNNING_SOUND_POI = MultiblockPOIHelper.getPosList(pois, "sound0").getFirst();
        LINK_POI = MultiblockPOIHelper.getPosList(pois, "link0").getFirst();
        PARTICLE_POI = MultiblockPOIHelper.getPosList(pois, "particle0").getFirst();
        REFLECTOR_POI = MultiblockPOIHelper.getPosList(pois, "reflector0").getFirst();
        SUN_POI = MultiblockPOIHelper.getPosList(pois, "sun0").getFirst();
        INPUT_FLUID_POIS = MultiblockPOIHelper.getPosList(pois, "fluid_input0");
        OUTPUT_FLUID_POIS = MultiblockPOIHelper.getPosList(pois, "fluid_output0");
        INPUT_FLUID_FACING = MultiblockPOIHelper.getFacing(pois, "fluid_input0");
        OUTPUT_FLUID_FACING = MultiblockPOIHelper.getFacing(pois, "fluid_output0");
    }
}

package mctmods.immersivetechnology.common.data.generators.blockstate;

import com.google.common.collect.ImmutableMap;
import mctmods.immersivetechnology.client.models.ModelConfigurableSides;
import mctmods.immersivetechnology.common.blocks.connectors.ConnectorTimerBlock;
import com.immersiveconvergence.api.block.Enums.IOSideConfig;
import com.immersiveconvergence.api.block.ModProperties;
import mctmods.immersivetechnology.common.blocks.metal.BarrelOpenBlock;
import mctmods.immersivetechnology.common.blocks.metal.ValveFluidBlock;
import mctmods.immersivetechnology.common.blocks.metal.ValveLoadBlock;
import mctmods.immersivetechnology.common.data.util.GeneratorUtils;
import mctmods.immersivetechnology.common.data.generators.ModBlockState;
import mctmods.immersivetechnology.common.data.models.ModelProviderUtils;
import mctmods.immersivetechnology.common.data.builders.SideConfigBuilder;
import mctmods.immersivetechnology.core.registration.ModBlocks;
import mctmods.immersivetechnology.core.registration.ModFluids;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraftforge.client.model.generators.*;
import net.minecraftforge.client.model.generators.VariantBlockStateBuilder.PartialBlockstate;
import net.minecraftforge.client.model.generators.loaders.ObjModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import com.immersiveconvergence.api.block.Enums;

public class BasicStateGenerator {
    private final ModBlockState main;
    private final ExistingFileHelper existingFileHelper;

    private record ValveRotationConfig(int horizontalXRot, int verticalDownXRot, int verticalUpXRot, Function<Direction, Integer> yRotOffsetSupplier) {}


    private static final Map<String, ValveRotationConfig> VALVE_CONFIGS = Map.of(
            "fluid", new ValveRotationConfig(0, 90, 270, facing -> 2),
            "limiter", new ValveRotationConfig(90, 180, 0, facing -> 2),
            "load", new ValveRotationConfig(270, 180, 0, facing -> facing.getAxis().isHorizontal() ? 0 : 1),
            "timer", new ValveRotationConfig(270, 180, 0, facing -> 0)
    );

    public BasicStateGenerator(ModBlockState main, ExistingFileHelper helper) {
        this.main = main;
        this.existingFileHelper = helper;
    }

    private int[] calculateValveRotations(Direction facing, int rotationVal, boolean mirrored, ValveRotationConfig config) {
        int xRot;
        int yRot;
        int yRotOffset = config.yRotOffsetSupplier.apply(facing);
        if (facing.getAxis().isHorizontal()) {
            yRot = ((facing.get2DDataValue() + yRotOffset) % 4) * 90;
            xRot = config.horizontalXRot;
        } else {
            Direction hFacing = Direction.from2DDataValue(rotationVal);
            yRot = ((hFacing.get2DDataValue() + yRotOffset) % 4) * 90;
            xRot = (facing == Direction.DOWN) ? config.verticalDownXRot : config.verticalUpXRot;
        }
        if (mirrored) { yRot = (yRot + 180) % 360; }
        return new int[]{xRot, yRot};
    }

    public void generate() {
        createSimpleBlock(ModBlocks.getBlock.apply("reinforced_coke_brick"), main.models().cubeAll("block/stone/reinforced_coke_brick", main.modLoc("block/stone/reinforced_coke_brick")));
        createSimpleBlock(ModBlocks.getBlock.apply("technology_engineering"), main.models().cubeAll("block/metal/technology_engineering", main.modLoc("block/metal/technology_engineering")));
        createSimpleBlock(ModBlocks.getBlock.apply("crate_creative"), main.models().cubeAll("block/wooden/crate_creative", main.modLoc("block/wooden/crate_creative")));
        ResourceLocation reinforcedTexture = main.modLoc("block/stone/reinforced_coke_brick");
        createSlabModels(ModBlocks.Stone.SLAB_REINFORCED_COKE_BRICK.get(), reinforcedTexture, reinforcedTexture, reinforcedTexture);
        createSimpleBlock(ModBlocks.getBlock.apply("barrel_creative"), main.models().cubeAll("block/metal/barrel_creative", main.modLoc("block/metal/barrel_creative")));
        createSimpleBlock(ModBlocks.getBlock.apply("heat_creative"), main.models().cubeAll("block/metal/heat_creative", main.modLoc("block/metal/heat_creative")));

        VariantBlockStateBuilder steelBuilder = main.getVariantBuilder(ModBlocks.getBlock.apply("barrel_steel"));
        BlockModelBuilder steelModel = main.models().getBuilder("block/metal/barrel_steel").customLoader(SideConfigBuilder::begin).type(ModelConfigurableSides.Type.VERTICAL).baseName(main.modLoc("block/metal/barrel_steel")).end();
        steelBuilder.partialState().setModels(new ConfiguredModel(steelModel));

        VariantBlockStateBuilder openBuilder = main.getVariantBuilder(ModBlocks.getBlock.apply("barrel_open"));
        Map<IOSideConfig, String> suffixes = ImmutableMap.of(IOSideConfig.NONE, "", IOSideConfig.INPUT, "_in", IOSideConfig.OUTPUT, "_out");
        for (IOSideConfig config : IOSideConfig.VALUES) {
            String suffix = suffixes.get(config);
            BlockModelBuilder openModel = main.models().getBuilder("block/metal/barrel_open" + suffix).texture("up", main.modLoc("block/metal/barrel_open_up")).texture("down", main.modLoc("block/metal/barrel_open_down" + suffix)).texture("side", main.modLoc("block/metal/barrel_open_side")).texture("particle", main.modLoc("block/metal/barrel_open_side"));
            openModel.element().from(0, 0, 0).to(16, 1, 16).face(Direction.UP).texture("#up").uvs(0, 0, 16, 16).end().face(Direction.DOWN).texture("#down").uvs(0, 0, 16, 16).end();
            openModel.element().from(0, 0, 0).to(16, 16, 0).face(Direction.NORTH).texture("#side").uvs(0, 0, 16, 16).end();
            openModel.element().from(0, 0, 16).to(16, 16, 16).face(Direction.SOUTH).texture("#side").uvs(0, 0, 16, 16).end();
            openModel.element().from(16, 0, 0).to(16, 16, 16).face(Direction.EAST).texture("#side").uvs(0, 0, 16, 16).end();
            openModel.element().from(0, 0, 0).to(0, 16, 16).face(Direction.WEST).texture("#side").uvs(0, 0, 16, 16).end();
            openModel.element().from(0, 16, 0).to(1, 16, 1).face(Direction.UP).texture("#up").uvs(0, 0, 1, 1).end();
            openModel.element().from(15, 16, 0).to(16, 16, 1).face(Direction.UP).texture("#up").uvs(0, 0, 1, 1).end();
            openModel.element().from(0, 16, 15).to(1, 16, 16).face(Direction.UP).texture("#up").uvs(0, 0, 1, 1).end();
            openModel.element().from(15, 16, 15).to(16, 16, 16).face(Direction.UP).texture("#up").uvs(0, 0, 1, 1).end();
            openModel.element().from(1, 1, 0).to(15, 16, 1).face(Direction.SOUTH).texture("#side").uvs(0, 0, 14, 15).end().face(Direction.UP).texture("#up").uvs(0, 0, 14, 1).end();
            openModel.element().from(1, 1, 15).to(15, 16, 16).face(Direction.NORTH).texture("#side").uvs(0, 0, 14, 15).end().face(Direction.UP).texture("#up").uvs(0, 0, 14, 1).end();
            openModel.element().from(15, 1, 1).to(16, 16, 15).face(Direction.WEST).texture("#side").uvs(0, 0, 14, 15).end().face(Direction.UP).texture("#up").uvs(0, 0, 1, 14).end();
            openModel.element().from(0, 1, 1).to(1, 16, 15).face(Direction.EAST).texture("#side").uvs(0, 0, 14, 15).end().face(Direction.UP).texture("#up").uvs(0, 0, 1, 14).end();
            openBuilder.partialState().with(BarrelOpenBlock.BOTTOM_CONFIG, config).setModels(new ConfiguredModel(openModel));
        }
        BlockModelBuilder trashItemModel = createTrashModel("item");
        createRotatedBlock(ModBlocks.Metal.TRASH_ITEM, state -> trashItemModel, List.of());
        BlockModelBuilder trashFluidModel = createTrashModel("fluid");
        createRotatedBlock(ModBlocks.Metal.TRASH_FLUID, state -> trashFluidModel, List.of());
        BlockModelBuilder trashEnergyModel = createTrashModel("energy");
        createRotatedBlock(ModBlocks.Metal.TRASH_ENERGY, state -> trashEnergyModel, List.of());
        ModelFile emptyModel = main.models().withExistingParent("empty", main.mcLoc("block/block")).renderType("cutout").texture("particle", "#missingno");
        for (ModBlocks.BlockEntry<?> fluidEntry : ModFluids.ALL_FLUID_BLOCKS) {
            Block fluidBlock = fluidEntry.get();
            VariantBlockStateBuilder builder = main.getVariantBuilder(fluidBlock);
            for (int level = 0; level < 16; level++) {
                builder.partialState().with(LiquidBlock.LEVEL, level).modelForState().modelFile(emptyModel).addModel();
            }
        }

        createValveVariants();
        createBaseHeaterVariants();

        VariantBlockStateBuilder rotorBuilder = main.getVariantBuilder(ModBlocks.Metal.ROTOR_CREATIVE.get());
        ModelFile rotorNS = new ModelFile.UncheckedModelFile(main.modLoc("dynamic/rotor"));
        ModelFile rotorEW = new ModelFile.UncheckedModelFile(main.modLoc("dynamic/rotor_east_west"));
        rotorBuilder.forAllStates(state -> {
            Direction facing = state.getValue(ModProperties.FACING_HORIZONTAL);
            ModelFile modelFile = (facing == Direction.NORTH || facing == Direction.SOUTH) ? rotorNS : rotorEW;
            int yRot = 0;
            if (facing == Direction.SOUTH || facing == Direction.WEST) yRot = 180;
            return ConfiguredModel.builder().modelFile(modelFile).rotationY(yRot).build();
        });
    }

    private void createValveVariants() {
        ModelFile valveClosed = createValveObjModel("valve_fluid", "valve_fluid", false, "Pipe");
        ModelFile valveOpen = createValveObjModel("valve_fluid", "valve_fluid", true, "Pipe");
        createValveState(ModBlocks.Metal.VALVE_FLUID.get(), valveClosed, valveOpen, VALVE_CONFIGS.get("fluid"), ValveFluidBlock.OPEN);
        setRenderType(RenderType.cutout(), (BlockModelBuilder) valveClosed, (BlockModelBuilder) valveOpen);

        BlockModelBuilder valveLimiterBuilder = main.models().cubeBottomTop("block/metal/valve_limiter", main.modLoc("block/metal/valve_limiter_side"), main.modLoc("block/metal/valve_limiter_bottom"), main.modLoc("block/metal/valve_limiter_top"));
        valveLimiterBuilder.texture("particle", main.modLoc("block/metal/valve_limiter_side"));
        createValveState(ModBlocks.Metal.VALVE_LIMITER.get(), valveLimiterBuilder, valveLimiterBuilder, VALVE_CONFIGS.get("limiter"), null);
        setRenderType(RenderType.cutout(), valveLimiterBuilder);

        ModelFile valveLoadClosed = createValveObjModel("valve_load", "valve_load", false, "Base");
        ModelFile valveLoadOpen = createValveObjModel("valve_load", "valve_load", true, "Base");
        createValveState(ModBlocks.Metal.VALVE_LOAD.get(), valveLoadClosed, valveLoadOpen, VALVE_CONFIGS.get("load"), ValveLoadBlock.OPEN);
        setRenderType(RenderType.cutout(), (BlockModelBuilder) valveLoadClosed, (BlockModelBuilder) valveLoadOpen);

        ModelFile timerModel = createTimerObjModel("connector_timer", "connector_timer", "Base");
        createValveState(ModBlocks.Connector.CONNECTOR_TIMER.get(), timerModel, timerModel, VALVE_CONFIGS.get("timer"), null);
        setRenderType(RenderType.translucent(), (BlockModelBuilder) timerModel);
        main.itemModels().getBuilder(ModBlocks.Connector.CONNECTOR_TIMER.getId().getPath()).parent(timerModel);
    }

    private void createValveState(Block block, ModelFile closedModel, ModelFile openModel, ValveRotationConfig config, @Nullable Property<Boolean> openProperty) {
        VariantBlockStateBuilder builder = main.getVariantBuilder(block);
        boolean hasMirrored = block.getStateDefinition().getProperties().contains(ModProperties.MIRRORED);
        builder.forAllStates(state -> {
            Direction facing = state.getValue(ModProperties.FACING_ALL);
            boolean open = openProperty != null && state.getValue(openProperty);
            Property<Integer> rotationProp = openProperty != null ? (openProperty == ValveFluidBlock.OPEN ? ValveFluidBlock.ROTATION : ValveLoadBlock.ROTATION) : ConnectorTimerBlock.ROTATION;
            int rotationVal = state.getValue(rotationProp);
            boolean mirrored = hasMirrored && state.getValue(ModProperties.MIRRORED);
            ModelFile modelFile = open ? openModel : closedModel;
            int[] rotations = calculateValveRotations(facing, rotationVal, mirrored, config);
            return ConfiguredModel.builder().modelFile(modelFile).rotationX(rotations[0]).rotationY(rotations[1]).build();
        });
    }

    private void createBaseHeaterVariants() {
        BlockModelBuilder baseHeaterNormalSplit = createBaseHeaterUnsplit(false);
        BlockModelBuilder baseHeaterActiveSplit = createBaseHeaterUnsplit(true);
        setRenderType(RenderType.cutout(), baseHeaterNormalSplit, baseHeaterActiveSplit);

        VariantBlockStateBuilder heaterBuilder = main.getVariantBuilder(ModBlocks.Metal.ADVANCED_COKE_OVEN_BASEHEATER.get());
        heaterBuilder.forAllStatesExcept(state -> {
            Direction facing = state.getValue(ModProperties.FACING_HORIZONTAL);
            boolean active = state.getValue(ModProperties.ACTIVE);
            ModelFile modelFile = active ? baseHeaterActiveSplit : baseHeaterNormalSplit;
            int yRot = (int) facing.toYRot();
            return ConfiguredModel.builder().modelFile(modelFile).rotationY(yRot).uvLock(true).build();
        }, ModProperties.MULTIBLOCKSLAVE, BlockStateProperties.WATERLOGGED);
    }

    private ModelFile createValveObjModel(String baseName, String objFolder, boolean isOpen, String baseVisibility) {
        String modelName = "block/metal/" + baseName + (isOpen ? "_open" : "_closed");
        BlockModelBuilder builder = main.models().getBuilder(modelName);
        CustomLoaderBuilder<BlockModelBuilder> loader = builder.customLoader(ObjModelBuilder::begin)
                .modelLocation(main.modLoc("models/block/metal/" + objFolder + "/" + objFolder + ".obj"))
                .automaticCulling(true)
                .shadeQuads(true)
                .flipV(true)
                .emissiveAmbient(true)
                .visibility(baseVisibility, true)
                .visibility("Handle_Open", isOpen)
                .visibility("Handle_Closed", !isOpen);
        ModelFile model = loader.end();
        String particleTex = GeneratorUtils.getTextureFromObj(main.modLoc("block/metal/" + objFolder + "/" + objFolder + ".obj"), existingFileHelper);
        builder.texture("particle", particleTex);
        return model;
    }

    @SuppressWarnings("SameParameterValue")
    private ModelFile createTimerObjModel(String baseName, String objFolder, String baseVisibility) {
        String modelName = "block/connector/" + baseName;
        BlockModelBuilder builder = main.models().getBuilder(modelName);
        builder.renderType("translucent");
        CustomLoaderBuilder<BlockModelBuilder> loader = builder.customLoader(ObjModelBuilder::begin)
                .modelLocation(main.modLoc("models/block/connector/" + objFolder + "/" + objFolder + ".obj"))
                .automaticCulling(false)
                .shadeQuads(false)
                .flipV(true)
                .emissiveAmbient(true)
                .visibility(baseVisibility, true)
                .visibility("cube", true)
                .visibility("glass", true);
        ModelFile model = loader.end();
        String particleTex = GeneratorUtils.getTextureFromObj(main.modLoc("block/connector/" + objFolder + "/" + objFolder + ".obj"), existingFileHelper);
        builder.texture("particle", particleTex);
        return model;
    }

    private BlockModelBuilder createBaseHeaterUnsplit(boolean active) {
        String suffix = active ? "_active" : "";
        String modelName = "block/metal/advanced_coke_oven_baseheater" + suffix;
        BlockModelBuilder base = main.models().withExistingParent(modelName, main.mcLoc("block"));
        CustomLoaderBuilder<BlockModelBuilder> loader = base.customLoader(ObjModelBuilder::begin).modelLocation(main.modLoc("models/block/metal/advanced_coke_oven_baseheater/advanced_coke_oven_baseheater" + suffix + ".obj")).automaticCulling(false).shadeQuads(true).flipV(true).emissiveAmbient(active).visibility("Fan", false).visibility("fan", false).visibility("Rotor", false).visibility("rotor", false);
        BlockModelBuilder ret = loader.end();
        ret.ao(false);
        String particleTex = main.modLoc("block/metal/advanced_coke_oven_baseheater").toString();
        ret.texture("particle", particleTex);
        ModBlockState.generatedParticleTextures.put(ret.getLocation(), particleTex);
        return ret;
    }

    private BlockModelBuilder createTrashModel(String subtype) {
        String base = "block/metal/trash_" + subtype;
        BlockModelBuilder model = main.models().getBuilder(base).texture("particle", main.modLoc(base + "_side")).texture("side", main.modLoc(base + "_side")).texture("bottom", main.modLoc(base + "_bottom")).texture("top_side", main.modLoc(base + "_top_side")).texture("top", main.modLoc(base + "_top"));
        model.element().from(2, 0, 2).to(14, 13, 14).face(Direction.NORTH).texture("#side").uvs(2, 3, 14, 16).end().face(Direction.EAST).texture("#side").uvs(2, 3, 14, 16).end().face(Direction.SOUTH).texture("#side").uvs(2, 3, 14, 16).end().face(Direction.WEST).texture("#side").uvs(2, 3, 14, 16).end().face(Direction.UP).texture("#bottom").uvs(2, 2, 14, 14).end().face(Direction.DOWN).texture("#bottom").uvs(2, 2, 14, 14).end();
        model.element().from(0.5f, 13, 0.5f).to(15.5f, 16, 15.5f).face(Direction.NORTH).texture("#top_side").uvs(0, 0, 15, 3).end().face(Direction.EAST).texture("#top_side").uvs(0, 0, 15, 3).end().face(Direction.SOUTH).texture("#top_side").uvs(0, 0, 15, 3).end().face(Direction.WEST).texture("#top_side").uvs(0, 0, 15, 3).end().face(Direction.UP).texture("#top").uvs(0, 0, 15, 15).end().face(Direction.DOWN).texture("#top").uvs(0, 0, 15, 15).end();
        return model;
    }

    private void createSlabModels(Block block, ResourceLocation side, ResourceLocation bottom, ResourceLocation top) {
        String baseName = "block/stone/slab_reinforced_coke_brick";
        ModelFile bottomModel = main.models().slab(baseName, side, bottom, top);
        ModelFile topModel = main.models().slabTop(baseName + "_top", side, bottom, top);
        ModelFile doubleModel = main.models().cubeAll(baseName + "_double", top);
        main.getVariantBuilder(block).forAllStatesExcept(state -> {
            SlabType type = state.getValue(SlabBlock.TYPE);
            if (type == SlabType.BOTTOM) { return ConfiguredModel.builder().modelFile(bottomModel).build(); }
            else if (type == SlabType.TOP) { return ConfiguredModel.builder().modelFile(topModel).build(); }
            else { return ConfiguredModel.builder().modelFile(doubleModel).build(); }
        }, SlabBlock.WATERLOGGED);
    }

    private void createSimpleBlock(Block block, ModelFile model) { main.getVariantBuilder(block).partialState().setModels(new ConfiguredModel(model)); }

    protected void addParticleTextureFrom(BlockModelBuilder result, ModelBuilder<?> model) {
        String particles = ModBlockState.generatedParticleTextures.get(model.getLocation());
        if (particles != null) {
            result.texture("particle", particles);
            ModBlockState.generatedParticleTextures.put(result.getLocation(), particles);
        }
    }

    protected int getAngle(Direction dir) { return (int) ((dir.toYRot() + 180) % 360); }

    protected void createRotatedBlock(Supplier<? extends Block> block, Function<PartialBlockstate, ModelFile> model, List<Property<?>> additionalProps) {
        VariantBlockStateBuilder stateBuilder = main.getVariantBuilder(block.get());
        forEachState(stateBuilder.partialState(), additionalProps, state -> {
            ModelFile modelLoc = model.apply(state);
            EnumProperty<Direction> facing = ModProperties.FACING_HORIZONTAL;
            for (Direction d : facing.getPossibleValues()) {
                int x = 0;
                int y = getAngle(d);
                state.with(facing, d).setModels(new ConfiguredModel(modelLoc, x, y, false));
            }
        });
    }

    public static <T extends Comparable<T>> void forEach(PartialBlockstate base, Property<T> prop, List<Property<?>> remaining, Consumer<PartialBlockstate> out) { for (T value : prop.getPossibleValues()) { forEachState(base.with(prop, value), remaining, out); } }

    public static void forEachState(PartialBlockstate base, List<Property<?>> props, Consumer<PartialBlockstate> out) {
        if (!props.isEmpty()) { List<Property<?>> remaining = props.subList(1, props.size()); Property<?> mainProp = props.get(0); forEach(base, mainProp, remaining, out); }
        else { out.accept(base); }
    }

    protected void setRenderType(@Nullable RenderType type, ModelBuilder<?>... builders) {
        if (type != null) {
            final String typeName = ModelProviderUtils.getName(type);
            for (final ModelBuilder<?> model : builders) { model.renderType(typeName); }
        }
    }
}

package mctmods.immersivetechnology.common.data.generators;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import mctmods.immersivetechnology.client.models.helper.ITModelConfigurableSides;
import mctmods.immersivetechnology.common.blocks.helper.ITEnums.IOSideConfig;
import mctmods.immersivetechnology.common.blocks.helper.ITProperties;
import mctmods.immersivetechnology.common.blocks.metal.BarrelOpenBlock;
import mctmods.immersivetechnology.common.blocks.metal.ValveFluidBlock;
import mctmods.immersivetechnology.common.blocks.metal.ValveLimiterBlock;
import mctmods.immersivetechnology.common.blocks.metal.ValveLoadBlock;
import mctmods.immersivetechnology.common.multiblocks.helper.ITTemplateMultiblock;
import mctmods.immersivetechnology.common.data.ITDataGenUtils;
import mctmods.immersivetechnology.common.data.models.ITModelProviderUtils;
import mctmods.immersivetechnology.common.data.models.ITNongeneratedModels;
import mctmods.immersivetechnology.common.data.models.ITNongeneratedModels.ITNongeneratedModel;
import mctmods.immersivetechnology.common.data.models.ITSideConfigBuilder;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.registration.ITBlocks;
import mctmods.immersivetechnology.core.registration.ITFluids;
import mctmods.immersivetechnology.core.registration.ITMultiblockProvider;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.client.model.generators.*;
import net.minecraftforge.client.model.generators.VariantBlockStateBuilder.PartialBlockstate;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import mctmods.immersivetechnology.common.data.loaders.ITSplitModelBuilder;
import org.jetbrains.annotations.NotNull;

public class ITBlockStateProvider extends BlockStateProvider {
    public final Map<Block, ModelFile> unsplitModels = new HashMap<>();
    protected static final Map<ResourceLocation, String> generatedParticleTextures = new HashMap<>();
    protected ExistingFileHelper existingFileHelper;
    protected ITNongeneratedModels innerModels;
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    private static final Logger LOGGER = LogManager.getLogger();
    private final PackOutput packOutput;

    private final ClearableBlockModelProvider blockModels;
    private final ClearableItemModelProvider itemModels;

    private static class ClearableBlockModelProvider extends BlockModelProvider {
        public ClearableBlockModelProvider(PackOutput output, String modid, ExistingFileHelper existingFileHelper) {
            super(output, modid, existingFileHelper);
        }

        @Override protected void registerModels() {}

        public void clearModels() { clear(); }

        public CompletableFuture<?> genAll(CachedOutput cache) { return generateAll(cache); }
    }

    private static class ClearableItemModelProvider extends ItemModelProvider {
        public ClearableItemModelProvider(PackOutput output, String modid, ExistingFileHelper existingFileHelper) {
            super(output, modid, existingFileHelper);
        }

        @Override protected void registerModels() {}

        public void clearModels() { clear(); }

        public CompletableFuture<?> genAll(CachedOutput cache) { return generateAll(cache); }
    }

    public ITBlockStateProvider(DataGenerator generator, ExistingFileHelper helper) {
        super(generator.getPackOutput(), ITLib.MODID, helper);
        this.packOutput = generator.getPackOutput();
        this.existingFileHelper = helper;
        this.innerModels = new ITNongeneratedModels(generator.getPackOutput(), existingFileHelper);
        this.blockModels = new ClearableBlockModelProvider(generator.getPackOutput(), ITLib.MODID, helper);
        this.itemModels = new ClearableItemModelProvider(generator.getPackOutput(), ITLib.MODID, this.blockModels.existingFileHelper);
    }

    @Override
    public BlockModelProvider models() { return blockModels; }

    @Override public ItemModelProvider itemModels() { return itemModels; }

    public static class ITObjModelBuilder<T extends ModelBuilder<T>> extends CustomLoaderBuilder<T> {
        private ResourceLocation modelLocation;
        private boolean automaticCulling = true;
        private boolean shadeQuads = true;
        private boolean flipV = false;
        private boolean emissiveAmbient = true;
        private String mtlOverride;
        private final Map<String, Boolean> visibility = new HashMap<>();

        public ITObjModelBuilder(T parent, ExistingFileHelper existingFileHelper) { super(ITLib.rl("obj"), parent, existingFileHelper); }

        public ITObjModelBuilder<T> modelLocation(ResourceLocation modelLocation) { this.modelLocation = modelLocation; return this; }

        public ITObjModelBuilder<T> automaticCulling(boolean automaticCulling) { this.automaticCulling = automaticCulling; return this; }

        public ITObjModelBuilder<T> shadeQuads(boolean shadeQuads) { this.shadeQuads = shadeQuads; return this; }

        public ITObjModelBuilder<T> flipV(boolean flipV) { this.flipV = flipV; return this; }

        public ITObjModelBuilder<T> emissiveAmbient(boolean emissiveAmbient) { this.emissiveAmbient = emissiveAmbient; return this; }

        public ITObjModelBuilder<T> mtlOverride(String mtlOverride) { this.mtlOverride = mtlOverride; return this; }

        public ITObjModelBuilder<T> visibility(String part, boolean show) { visibility.put(part, show); return this; }

        @Override public JsonObject toJson(JsonObject json) {
            json = super.toJson(json);
            Preconditions.checkNotNull(modelLocation, "model must be set on obj model");
            json.addProperty("model", modelLocation.toString());
            json.addProperty("automatic_culling", automaticCulling);
            json.addProperty("shade_quads", shadeQuads);
            json.addProperty("flip_v", flipV);
            json.addProperty("emissive_ambient", emissiveAmbient);
            if (mtlOverride != null) { json.addProperty("mtl_override", mtlOverride); }
            if (!visibility.isEmpty()) {
                JsonObject visJson = new JsonObject();
                visibility.forEach(visJson::addProperty);
                json.add("visibility", visJson);
            }
            return json;
        }
    }

    private record ValveRotationConfig(int horizontalXRot, int verticalDownXRot, int verticalUpXRot, Function<Direction, Integer> yRotOffsetSupplier) {}

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

    @Override @NotNull public CompletableFuture<?> run(CachedOutput cache) {
        ((ClearableBlockModelProvider)models()).clearModels();
        ((ClearableItemModelProvider)itemModels()).clearModels();
        registeredBlocks.clear();
        registerStatesAndModels();
        CompletableFuture<?>[] futures = new CompletableFuture[registeredBlocks.size() + 2];
        int i = 0;
        futures[i++] = ((ClearableBlockModelProvider)models()).genAll(cache);
        futures[i++] = ((ClearableItemModelProvider)itemModels()).genAll(cache);
        for (Map.Entry<Block, IGeneratedBlockState> entry : registeredBlocks.entrySet()) {
            futures[i++] = saveBlockState(entry.getKey(), entry.getValue().toJson(), cache);
        }
        return CompletableFuture.allOf(futures);
    }

    private CompletableFuture<?> saveBlockState(Block owner, JsonObject stateJson, CachedOutput cache) {
        return CompletableFuture.runAsync(() -> {
            ResourceLocation blockName = Preconditions.checkNotNull(ForgeRegistries.BLOCKS.getKey(owner));
            ResourceLocation outputLocation = extendWithFolder(blockName);
            Path path = packOutput.getOutputFolder().resolve("assets/" + outputLocation.getNamespace() + "/" + outputLocation.getPath() + ".json");
            try {
                String jsonStr = GSON.toJson(stateJson);
                byte[] bytes = jsonStr.getBytes(StandardCharsets.UTF_8);
                com.google.common.hash.HashCode hash = Hashing.sha256().hashBytes(bytes);
                cache.writeIfNeeded(path, bytes, hash);
            } catch (IOException e) { LOGGER.error("Couldn't save blockstate to {}", path, e); }
        }, Util.backgroundExecutor());
    }

    protected ResourceLocation extendWithFolder(ResourceLocation rl) { return ResourceLocation.fromNamespaceAndPath(rl.getNamespace(), "blockstates/" + rl.getPath()); }

    @Override protected void registerStatesAndModels() {
        ITLib.IT_LOGGER.info("Generating Multiblock Splits");

        generateMultiblockConfig("alternator", "metal", false, false, false, ImmutableMap.of(), ImmutableMap.of());
        generateMultiblockConfig("boiler_liquid", "metal", false, false, false, ImmutableMap.of(), ImmutableMap.of());
        generateMultiblockConfig("boiler_solid", "metal", false, true, false, ImmutableMap.of("cube_front", modLoc("multiblock/metal/boiler_solid")), ImmutableMap.of("cube_front", modLoc("multiblock/metal/boiler_solid_active")));
        generateMultiblockConfig("boiler_tank", "metal", false, false, false, ImmutableMap.of(), ImmutableMap.of());
        generateMultiblockConfig("cooling_tower", "stone", false, false, false, ImmutableMap.of(), ImmutableMap.of());
        generateMultiblockConfig("distiller", "metal", true, false, false, ImmutableMap.of(), ImmutableMap.of());
        generateMultiblockConfig("gas_turbine", "metal", true, false, false, ImmutableMap.of(), ImmutableMap.of());
        generateMultiblockConfig("heat_exchanger", "metal", false, false, false, ImmutableMap.of(), ImmutableMap.of());
        generateMultiblockConfig("solar_melter", "metal", false, false, false, ImmutableMap.of(), ImmutableMap.of());
        generateMultiblockConfig("solar_reflector", "metal", false, false, false, ImmutableMap.of(), ImmutableMap.of());
        generateMultiblockConfig("solar_tower", "metal", false, false, false, ImmutableMap.of(), ImmutableMap.of());
        generateMultiblockConfig("steam_turbine", "metal", true, false, false, ImmutableMap.of(), ImmutableMap.of());
        generateMultiblockConfig("steel_sheetmetal_tank", "metal", false, false, true, ImmutableMap.of(), ImmutableMap.of());

        createSimpleBlock(ITBlocks.getBlock.apply("reinforced_coke_brick"), models().cubeAll("block/stone/reinforced_coke_brick", modLoc("block/stone/reinforced_coke_brick")));
        createSimpleBlock(ITBlocks.getBlock.apply("technology_engineering"), models().cubeAll("block/metal/technology_engineering", modLoc("block/metal/technology_engineering")));
        createSimpleBlock(ITBlocks.getBlock.apply("crate_creative"), models().cubeAll("block/wooden/crate_creative", modLoc("block/wooden/crate_creative")));
        ResourceLocation reinforcedTexture = modLoc("block/stone/reinforced_coke_brick");
        createSlabModels(ITBlocks.Stone.SLAB_REINFORCED_COKE_BRICK.get(), reinforcedTexture, reinforcedTexture, reinforcedTexture);
        createSimpleBlock(ITBlocks.getBlock.apply("barrel_creative"), models().cubeAll("block/metal/barrel_creative", modLoc("block/metal/barrel_creative")));
        createSimpleBlock(ITBlocks.getBlock.apply("heat_creative"), models().cubeAll("block/metal/heat_creative", modLoc("block/metal/heat_creative")));

        VariantBlockStateBuilder steelBuilder = getVariantBuilder(ITBlocks.getBlock.apply("barrel_steel"));

        BlockModelBuilder steelModel = models().getBuilder("block/metal/barrel_steel")
                .customLoader(ITSideConfigBuilder::begin)
                .type(ITModelConfigurableSides.Type.VERTICAL)
                .baseName(modLoc("block/metal/barrel_steel"))
                .end();
        steelBuilder.partialState().setModels(new ConfiguredModel(steelModel));

        VariantBlockStateBuilder openBuilder = getVariantBuilder(ITBlocks.getBlock.apply("barrel_open"));
        Map<IOSideConfig, String> suffixes = ImmutableMap.of(
                IOSideConfig.NONE, "",
                IOSideConfig.INPUT, "_in",
                IOSideConfig.OUTPUT, "_out"
        );
        for (IOSideConfig config : IOSideConfig.VALUES) {
            String suffix = suffixes.get(config);
            BlockModelBuilder openModel = models().getBuilder("block/metal/barrel_open" + suffix)
                    .texture("up", modLoc("block/metal/barrel_open_up"))
                    .texture("down", modLoc("block/metal/barrel_open_down" + suffix))
                    .texture("side", modLoc("block/metal/barrel_open_side"))
                    .texture("particle", modLoc("block/metal/barrel_open_side"));
            openModel.element().from(0, 0, 0).to(16, 1, 16)
                    .face(Direction.UP).texture("#up").uvs(0, 0, 16, 16).end()
                    .face(Direction.DOWN).texture("#down").uvs(0, 0, 16, 16).end();
            openModel.element().from(0, 0, 0).to(16, 16, 0)
                    .face(Direction.NORTH).texture("#side").uvs(0, 0, 16, 16).end();
            openModel.element().from(0, 0, 16).to(16, 16, 16)
                    .face(Direction.SOUTH).texture("#side").uvs(0, 0, 16, 16).end();
            openModel.element().from(16, 0, 0).to(16, 16, 16)
                    .face(Direction.EAST).texture("#side").uvs(0, 0, 16, 16).end();
            openModel.element().from(0, 0, 0).to(0, 16, 16)
                    .face(Direction.WEST).texture("#side").uvs(0, 0, 16, 16).end();
            openModel.element().from(0, 16, 0).to(1, 16, 1)
                    .face(Direction.UP).texture("#up").uvs(0, 0, 1, 1).end();
            openModel.element().from(15, 16, 0).to(16, 16, 1)
                    .face(Direction.UP).texture("#up").uvs(0, 0, 1, 1).end();
            openModel.element().from(0, 16, 15).to(1, 16, 16)
                    .face(Direction.UP).texture("#up").uvs(0, 0, 1, 1).end();
            openModel.element().from(15, 16, 15).to(16, 16, 16)
                    .face(Direction.UP).texture("#up").uvs(0, 0, 1, 1).end();
            openModel.element().from(1, 1, 0).to(15, 16, 1)
                    .face(Direction.SOUTH).texture("#side").uvs(0, 0, 14, 15).end()
                    .face(Direction.UP).texture("#up").uvs(0, 0, 14, 1).end();
            openModel.element().from(1, 1, 15).to(15, 16, 16)
                    .face(Direction.NORTH).texture("#side").uvs(0, 0, 14, 15).end()
                    .face(Direction.UP).texture("#up").uvs(0, 0, 14, 1).end();
            openModel.element().from(15, 1, 1).to(16, 16, 15)
                    .face(Direction.WEST).texture("#side").uvs(0, 0, 14, 15).end()
                    .face(Direction.UP).texture("#up").uvs(0, 0, 1, 14).end();
            openModel.element().from(0, 1, 1).to(1, 16, 15)
                    .face(Direction.EAST).texture("#side").uvs(0, 0, 14, 15).end()
                    .face(Direction.UP).texture("#up").uvs(0, 0, 1, 14).end();
            openBuilder.partialState().with(BarrelOpenBlock.BOTTOM_CONFIG, config).setModels(new ConfiguredModel(openModel));
        }
        BlockModelBuilder trashItemModel = createTrashModel("item");
        createRotatedBlock(ITBlocks.Metal.TRASH_ITEM, state -> trashItemModel, List.of());
        BlockModelBuilder trashFluidModel = createTrashModel("fluid");
        createRotatedBlock(ITBlocks.Metal.TRASH_FLUID, state -> trashFluidModel, List.of());
        BlockModelBuilder trashEnergyModel = createTrashModel("energy");
        createRotatedBlock(ITBlocks.Metal.TRASH_ENERGY, state -> trashEnergyModel, List.of());
        ModelFile emptyModel = models().withExistingParent("empty", mcLoc("block/block"))
                .renderType("cutout")
                .texture("particle", "#missingno");
        for (ITBlocks.BlockEntry<?> fluidEntry : ITFluids.ALL_FLUID_BLOCKS) {
            Block fluidBlock = fluidEntry.get();
            VariantBlockStateBuilder builder = getVariantBuilder(fluidBlock);
            for (int level = 0; level < 16; level++) {
                builder.partialState()
                        .with(LiquidBlock.LEVEL, level)
                        .modelForState()
                        .modelFile(emptyModel)
                        .addModel();
            }
        }

        ModelFile valveClosed = createValveObjModel("valve_fluid", "valve_fluid", false, "Pipe");
        ModelFile valveOpen = createValveObjModel("valve_fluid", "valve_fluid", true, "Pipe");

        ValveRotationConfig fluidConfig = new ValveRotationConfig(0, 90, 270, facing -> 2);
        VariantBlockStateBuilder valveFluidBuilder = getVariantBuilder(ITBlocks.Metal.VALVE_FLUID.get());
        valveFluidBuilder.forAllStates(state -> {
            Direction facing = state.getValue(ITProperties.FACING_ALL);
            boolean open = state.getValue(ValveFluidBlock.OPEN);
            int rotationVal = state.getValue(ValveFluidBlock.ROTATION);
            boolean mirrored = state.getValue(ITProperties.MIRRORED);
            ModelFile modelFile = open ? valveOpen : valveClosed;
            int[] rotations = calculateValveRotations(facing, rotationVal, mirrored, fluidConfig);
            return ConfiguredModel.builder().modelFile(modelFile).rotationX(rotations[0]).rotationY(rotations[1]).build();
        });
        setRenderType(RenderType.cutout(), (BlockModelBuilder) valveClosed, (BlockModelBuilder) valveOpen);

        BlockModelBuilder valveLimiterBuilder = models().cubeBottomTop("block/metal/valve_limiter",
                modLoc("block/metal/valve_limiter_side"),
                modLoc("block/metal/valve_limiter_bottom"),
                modLoc("block/metal/valve_limiter_top"));
        valveLimiterBuilder.texture("particle", modLoc("block/metal/valve_limiter_side"));

        ValveRotationConfig limiterConfig = new ValveRotationConfig(90, 180, 0, facing -> 2);
        VariantBlockStateBuilder valveLimiterStateBuilder = getVariantBuilder(ITBlocks.Metal.VALVE_LIMITER.get());
        valveLimiterStateBuilder.forAllStates(state -> {
            Direction facing = state.getValue(ITProperties.FACING_ALL);
            int rotationVal = state.getValue(ValveLimiterBlock.ROTATION);
            boolean mirrored = state.getValue(ITProperties.MIRRORED);
            int[] rotations = calculateValveRotations(facing, rotationVal, mirrored, limiterConfig);
            return ConfiguredModel.builder().modelFile(valveLimiterBuilder).rotationX(rotations[0]).rotationY(rotations[1]).build();
        });
        setRenderType(RenderType.cutout(), valveLimiterBuilder);

        ModelFile valveLoadClosed = createValveObjModel("valve_load", "valve_load", false, "Base");
        ModelFile valveLoadOpen = createValveObjModel("valve_load", "valve_load", true, "Base");

        ValveRotationConfig loadConfig = new ValveRotationConfig(270, 180, 0, facing -> facing.getAxis().isHorizontal() ? 0 : 1);
        VariantBlockStateBuilder valveLoadBuilder = getVariantBuilder(ITBlocks.Metal.VALVE_LOAD.get());
        valveLoadBuilder.forAllStates(state -> {
            Direction facing = state.getValue(ITProperties.FACING_ALL);
            boolean open = state.getValue(ValveLoadBlock.OPEN);
            int rotationVal = state.getValue(ValveLoadBlock.ROTATION);
            boolean mirrored = state.getValue(ITProperties.MIRRORED);
            ModelFile modelFile = open ? valveLoadOpen : valveLoadClosed;
            int[] rotations = calculateValveRotations(facing, rotationVal, mirrored, loadConfig);
            return ConfiguredModel.builder().modelFile(modelFile).rotationX(rotations[0]).rotationY(rotations[1]).build();
        });
        setRenderType(RenderType.cutout(), (BlockModelBuilder) valveLoadClosed, (BlockModelBuilder) valveLoadOpen);

        VariantBlockStateBuilder rotorBuilder = getVariantBuilder(ITBlocks.Metal.ROTOR_CREATIVE.get());
        ModelFile rotorNS = new ModelFile.UncheckedModelFile(modLoc("dynamic/rotor"));
        ModelFile rotorEW = new ModelFile.UncheckedModelFile(modLoc("dynamic/rotor_east_west"));
        rotorBuilder.forAllStates(state -> {
            Direction facing = state.getValue(ITProperties.FACING_HORIZONTAL);
            ModelFile modelFile = (facing == Direction.NORTH || facing == Direction.SOUTH) ? rotorNS : rotorEW;
            int yRot = 0;
            if (facing == Direction.SOUTH || facing == Direction.WEST) yRot = 180;
            return ConfiguredModel.builder().modelFile(modelFile).rotationY(yRot).build();
        });
    }

    private void generateMultiblockConfig(String registry_name, String block_type, boolean useSeparateMirror, boolean hasActive, boolean automaticCulling, Map<String, ResourceLocation> defaultTextures, Map<String, ResourceLocation> activeTextures) {
        if (!hasActive) {
            defaultTextures = ImmutableMap.of();
            activeTextures = ImmutableMap.of();
        }
        ITLib.IT_LOGGER.info("Generating [{}] Multiblock Model Data", registry_name);
        ITTemplateMultiblock multiblock = (ITTemplateMultiblock) ITMultiblockProvider.getMBTemplate.apply(registry_name);
        boolean hasMirror = multiblock.getBlock().getStateDefinition().getProperties().contains(ITProperties.MIRRORED);
        boolean flipMirror = hasMirror && useSeparateMirror;
        String baseObjPath = "multiblock/" + block_type + "/obj/" + registry_name + "/" + registry_name + ".obj";
        String mirroredObjPath = baseObjPath.replace(".obj", "_mirrored.obj");
        ITNongeneratedModel defaultUnsplit = createUnsplitModel(registry_name, baseObjPath, defaultTextures, automaticCulling);
        ITNongeneratedModel activeUnsplit = hasActive ? createUnsplitModel(registry_name + "_active", baseObjPath, activeTextures, automaticCulling) : null;
        ITNongeneratedModel mirroredUnsplit = null;
        ITNongeneratedModel activeMirroredUnsplit = null;
        if (hasMirror) {
            String useObjPath = flipMirror ? mirroredObjPath : baseObjPath;
            mirroredUnsplit = createUnsplitModel(registry_name + "_mirrored", useObjPath, defaultTextures, automaticCulling);
            if (hasActive) activeMirroredUnsplit = createUnsplitModel(registry_name + "_active_mirrored", useObjPath, activeTextures, automaticCulling);
        }
        ModelFile defaultMain = split(defaultUnsplit, multiblock, false, block_type);
        ModelFile activeMain = hasActive ? split(activeUnsplit, multiblock, false, block_type) : null;
        ModelFile defaultMirrored = hasMirror ? split(mirroredUnsplit, multiblock, flipMirror, block_type) : null;
        ModelFile activeMirrored = hasActive && hasMirror ? split(activeMirroredUnsplit, multiblock, flipMirror, block_type) : null;
        createMultiblockVariant(multiblock::getBlock, defaultMain, activeMain, defaultMirrored, activeMirrored, hasMirror ? ITProperties.MIRRORED : null, hasActive ? ITProperties.ACTIVE : null);
    }

    private ITNongeneratedModel createUnsplitModel(String name, String objPathStr, Map<String, ResourceLocation> textures, boolean automaticCulling) {
        ResourceLocation objPath = modLoc(objPathStr);
        ITNongeneratedModel base = innerModels.withExistingParent(name, mcLoc("block"));
        ITObjModelBuilder<ITNongeneratedModel> loader = base.customLoader(ITObjModelBuilder::new);
        loader.modelLocation(addModelsPrefix(objPath));
        loader.flipV(true);
        loader.automaticCulling(automaticCulling);
        loader.shadeQuads(true);
        loader.emissiveAmbient(true);
        String path = objPath.getPath();
        ResourceLocation textureModel = objPath;
        if (path.endsWith("_mirrored.obj")) {
            textureModel = ResourceLocation.fromNamespaceAndPath(objPath.getNamespace(), path.replace("_mirrored.obj", ".obj"));
            String fileName = path.substring(path.lastIndexOf('/') + 1);
            String originalMtl = fileName.replace("_mirrored.obj", ".mtl");
            loader.mtlOverride(originalMtl);
        }
        ITNongeneratedModel ret = loader.end();
        ret.ao(false);
        String particleTex = ITDataGenUtils.getTextureFromObj(textureModel, existingFileHelper);
        if (particleTex.charAt(0) == '#') { particleTex = textures.getOrDefault(particleTex.substring(1), modLoc("block/metal/technology_engineering")).toString(); }
        ret.texture("particle", particleTex);
        generatedParticleTextures.put(ret.getLocation(), particleTex);
        textures.forEach(ret::texture);
        return ret;
    }

    private BlockModelBuilder createTrashModel(String subtype) {
        String base = "block/metal/trash_" + subtype;
        BlockModelBuilder model = models().getBuilder(base)
                .texture("particle", modLoc(base + "_side"))
                .texture("side", modLoc(base + "_side"))
                .texture("bottom", modLoc(base + "_bottom"))
                .texture("top_side", modLoc(base + "_top_side"))
                .texture("top", modLoc(base + "_top"));
        model.element()
                .from(2, 0, 2).to(14, 13, 14)
                .face(Direction.NORTH).texture("#side").uvs(2, 3, 14, 16).end()
                .face(Direction.EAST).texture("#side").uvs(2, 3, 14, 16).end()
                .face(Direction.SOUTH).texture("#side").uvs(2, 3, 14, 16).end()
                .face(Direction.WEST).texture("#side").uvs(2, 3, 14, 16).end()
                .face(Direction.UP).texture("#bottom").uvs(2, 2, 14, 14).end()
                .face(Direction.DOWN).texture("#bottom").uvs(2, 2, 14, 14).end();
        model.element()
                .from(0.5f, 13, 0.5f).to(15.5f, 16, 15.5f)
                .face(Direction.NORTH).texture("#top_side").uvs(0, 0, 15, 3).end()
                .face(Direction.EAST).texture("#top_side").uvs(0, 0, 15, 3).end()
                .face(Direction.SOUTH).texture("#top_side").uvs(0, 0, 15, 3).end()
                .face(Direction.WEST).texture("#top_side").uvs(0, 0, 15, 3).end()
                .face(Direction.UP).texture("#top").uvs(0, 0, 15, 15).end()
                .face(Direction.DOWN).texture("#top").uvs(0, 0, 15, 15).end();
        return model;
    }

    private void createSlabModels(Block block, ResourceLocation side, ResourceLocation bottom, ResourceLocation top) {
        String baseName = "block/stone/slab_reinforced_coke_brick";
        ModelFile bottomModel = models().slab(baseName, side, bottom, top);
        ModelFile topModel = models().slabTop(baseName + "_top", side, bottom, top);
        ModelFile doubleModel = models().cubeAll(baseName + "_double", top);
        getVariantBuilder(block).forAllStatesExcept(state -> {
            SlabType type = state.getValue(SlabBlock.TYPE);
            if (type == SlabType.BOTTOM) { return ConfiguredModel.builder().modelFile(bottomModel).build(); }
            else if (type == SlabType.TOP) { return ConfiguredModel.builder().modelFile(topModel).build(); }
            else { return ConfiguredModel.builder().modelFile(doubleModel).build(); }
        }, SlabBlock.WATERLOGGED);
    }

    private void createSimpleBlock(Block block, ModelFile model) { getVariantBuilder(block).partialState().setModels(new ConfiguredModel(model)); }

    private void loadTemplateFor(ITTemplateMultiblock multiblock) {
        final ResourceLocation name = multiblock.getUniqueName();
        if (ITTemplateMultiblock.SYNCED_CLIENT_TEMPLATES.containsKey(name)) { return; }
        final String filePath = "structures/" + name.getPath() + ".nbt";
        int slash = filePath.indexOf('/');
        String prefix = filePath.substring(0, slash);
        ResourceLocation shortLoc = ResourceLocation.fromNamespaceAndPath(name.getNamespace(), filePath.substring(slash + 1));
        try {
            final Resource resource = existingFileHelper.getResource(shortLoc, PackType.SERVER_DATA, "", prefix);
            try (final InputStream input = resource.open()) {
                final CompoundTag nbt = NbtIo.readCompressed(input);
                final StructureTemplate template = new StructureTemplate();
                template.load(VanillaRegistries.createLookup().lookupOrThrow(Registries.BLOCK), nbt);
                ITTemplateMultiblock.SYNCED_CLIENT_TEMPLATES.put(name, template);
            }
        }
        catch (IOException e) { throw new RuntimeException("Failed on " + name, e); }
    }

    private ModelFile split(ITNongeneratedModel unsplit, ITTemplateMultiblock multiblock, boolean mirror, String block_type) {
        UnaryOperator<BlockPos> transform = UnaryOperator.identity();
        if (mirror) {
            loadTemplateFor(multiblock);
            Vec3i size = multiblock.getSize(null);
            transform = p -> new BlockPos(size.getX() - p.getX() - 1, p.getY(), p.getZ());
        }
        return split(unsplit, multiblock, transform, block_type);
    }

    private ModelFile split(ITNongeneratedModel unsplit, ITTemplateMultiblock multiblock, UnaryOperator<BlockPos> transform, String block_type) {
        loadTemplateFor(multiblock);
        final Vec3i offset = multiblock.getMasterFromOriginOffset();
        Stream<Vec3i> partsStream = multiblock.getTemplate(null).blocksWithoutAir().stream().map(StructureTemplate.StructureBlockInfo::pos).map(transform).map(p -> p.subtract(offset));
        String baseName = unsplit.getLocation().getPath().substring(unsplit.getLocation().getPath().lastIndexOf("/") + 1).replace(".obj", "");
        return splitModel("multiblock/" + block_type + "/split/" + baseName + "_split", unsplit, partsStream.collect(Collectors.toList()));
    }

    protected BlockModelBuilder splitModel(String name, ModelBuilder<?> model, List<Vec3i> parts) {
        BlockModelBuilder result = models().withExistingParent(name, mcLoc("block")).customLoader(ITSplitModelBuilder::begin).innerModel(model).parts(parts).dynamic(false).end();
        addParticleTextureFrom(result, model);
        return result;
    }

    protected void addParticleTextureFrom(BlockModelBuilder result, ModelBuilder<?> model) {
        String particles = generatedParticleTextures.get(model.getLocation());
        if (particles != null) {
            result.texture("particle", particles);
            generatedParticleTextures.put(result.getLocation(), particles);
        }
    }

    protected int getAngle(Direction dir) { return (int) ((dir.toYRot() + 180) % 360); }

    protected void createRotatedBlock(Supplier<? extends Block> block, Function<PartialBlockstate, ModelFile> model, List<Property<?>> additionalProps) {
        VariantBlockStateBuilder stateBuilder = getVariantBuilder(block.get());
        forEachState(stateBuilder.partialState(), additionalProps, state -> {
            ModelFile modelLoc = model.apply(state);
            EnumProperty<Direction> facing = ITProperties.FACING_HORIZONTAL;
            for (Direction d : facing.getPossibleValues()) {
                int x = 0;
                int y = getAngle(d);
                state.with(facing, d).setModels(new ConfiguredModel(modelLoc, x, y, false));
            }
        });
    }

    public static <T extends Comparable<T>> void forEach(PartialBlockstate base, Property<T> prop, List<Property<?>> remaining, Consumer<PartialBlockstate> out) { for (T value : prop.getPossibleValues()) { forEachState(base.with(prop, value), remaining, out); } }

    public static void forEachState(PartialBlockstate base, List<Property<?>> props, Consumer<PartialBlockstate> out) {
        if (!props.isEmpty()) { List<Property<?>> remaining = props.subList(1, props.size()); Property<?> main = props.get(0); forEach(base, main, remaining, out); }
        else { out.accept(base); }
    }

    protected ResourceLocation addModelsPrefix(ResourceLocation in) { return ResourceLocation.fromNamespaceAndPath(in.getNamespace(), "models/" + in.getPath()); }

    protected void setRenderType(@Nullable RenderType type, ModelBuilder<?>... builders) {
        if (type != null) {
            final String typeName = ITModelProviderUtils.getName(type);
            for (final ModelBuilder<?> model : builders) { model.renderType(typeName); }
        }
    }

    protected <T extends ModelBuilder<T>> T obj(T base, ResourceLocation model, Map<String, ResourceLocation> textures) {
        ITObjModelBuilder<T> loader = base.customLoader(ITObjModelBuilder::new);
        loader.modelLocation(addModelsPrefix(model));
        loader.flipV(true);
        loader.automaticCulling(true);
        loader.shadeQuads(true);
        loader.emissiveAmbient(true);
        String path = model.getPath();
        ResourceLocation textureModel = model;
        if (path.endsWith("_mirrored.obj")) {
            textureModel = ResourceLocation.fromNamespaceAndPath(model.getNamespace(), path.replace("_mirrored.obj", ".obj"));
            String fileName = path.substring(path.lastIndexOf('/') + 1);
            String originalMtl = fileName.replace("_mirrored.obj", ".mtl");
            loader.mtlOverride(originalMtl);
        }
        T ret = loader.end();
        ret.ao(false);
        String particleTex = ITDataGenUtils.getTextureFromObj(textureModel, existingFileHelper);
        if (particleTex.charAt(0) == '#') { particleTex = textures.get(particleTex.substring(1)).toString(); }
        ret.texture("particle", particleTex);
        generatedParticleTextures.put(ret.getLocation(), particleTex);
        for (Map.Entry<String, ResourceLocation> e : textures.entrySet()) { ret.texture(e.getKey(), e.getValue()); }
        return ret;
    }

    protected ITNongeneratedModel obj(String name, ResourceLocation model, Map<String, ResourceLocation> textures, ITNongeneratedModels provider) {
        ITNongeneratedModel base = provider.getBuilder(name);
        return obj(base, model, textures);
    }

    protected ModelFile createValveObjModel(String baseName, String objFolder, boolean isOpen, String baseVisibility) {
        String modelName = "block/metal/" + baseName + (isOpen ? "_open" : "_closed");
        BlockModelBuilder builder = models().getBuilder(modelName);
        ITObjModelBuilder<BlockModelBuilder> loader = builder.customLoader(ITObjModelBuilder::new)
                .modelLocation(modLoc("models/block/metal/obj/" + objFolder + "/" + objFolder + ".obj"))
                .automaticCulling(true)
                .shadeQuads(true)
                .flipV(true)
                .emissiveAmbient(true)
                .mtlOverride(null)
                .visibility(baseVisibility, true)
                .visibility("Handle_Open", isOpen)
                .visibility("Handle_Closed", !isOpen);
        ModelFile model = loader.end();
        String particleTex = ITDataGenUtils.getTextureFromObj(modLoc("block/metal/obj/" + objFolder + "/" + objFolder + ".obj"), existingFileHelper);
        builder.texture("particle", particleTex);
        return model;
    }

    private void createMultiblockVariant(Supplier<? extends Block> b, ModelFile defaultMaster, @Nullable ModelFile activeMaster, @Nullable ModelFile defaultMirrored, @Nullable ModelFile activeMirrored, @Nullable Property<Boolean> mirroredState, @Nullable Property<Boolean> activeState) {
        unsplitModels.put(b.get(), defaultMaster);
        Preconditions.checkArgument((defaultMirrored == null) == (mirroredState == null));
        Preconditions.checkArgument((activeMaster == null) == (activeState == null));
        VariantBlockStateBuilder builder = getVariantBuilder(b.get());
        EnumProperty<Direction> facing = ITProperties.FACING_HORIZONTAL;
        builder.forAllStates(state -> {
            Direction dir = state.getValue(facing);
            int angleY = getAngle(dir);
            int angleX = 0;
            if (facing.getPossibleValues().contains(Direction.UP)) { angleX = -90 * dir.getStepY(); angleY = dir.getAxis() != Direction.Axis.Y ? getAngle(dir) : 0; }
            boolean mirrored = (mirroredState != null) ? state.getValue(mirroredState) : false;
            boolean active = (activeState != null) ? state.getValue(activeState) : false;
            ModelFile baseModel = active ? activeMaster : defaultMaster;
            ModelFile model = mirrored ? (active ? activeMirrored : defaultMirrored) : baseModel;
            assert model != null;
            return new ConfiguredModel[]{new ConfiguredModel(model, angleX, angleY, true)};
        });
    }
}

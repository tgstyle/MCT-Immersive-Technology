package mctmods.immersivetechnology.common.data.generators;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import mctmods.immersivetechnology.client.models.helper.ITModelConfigurableSides;
import mctmods.immersivetechnology.common.blocks.helper.ITEnums.IOSideConfig;
import mctmods.immersivetechnology.common.blocks.helper.ITProperties;
import mctmods.immersivetechnology.common.blocks.metal.BarrelOpenBlock;
import mctmods.immersivetechnology.common.blocks.metal.ValveFluidBlock;
import mctmods.immersivetechnology.common.blocks.metal.ValveLimiterBlock;
import mctmods.immersivetechnology.common.blocks.metal.ValveLoadBlock;
import mctmods.immersivetechnology.common.multiblocks.helper.ITTemplateMultiblock;
import mctmods.immersivetechnology.common.data.ITDataGenUtils;
import mctmods.immersivetechnology.common.data.models.ITMirroredModelBuilder;
import mctmods.immersivetechnology.common.data.models.ITModelProviderUtils;
import mctmods.immersivetechnology.common.data.models.ITNongeneratedModels;
import mctmods.immersivetechnology.common.data.models.ITNongeneratedModels.ITNongeneratedModel;
import mctmods.immersivetechnology.common.data.models.ITSideConfigBuilder;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.registration.ITBlocks;
import mctmods.immersivetechnology.core.registration.ITFluids;
import mctmods.immersivetechnology.core.registration.ITMultiblockProvider;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
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
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.CustomLoaderBuilder;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.ModelProvider;
import net.minecraftforge.client.model.generators.VariantBlockStateBuilder;
import net.minecraftforge.client.model.generators.VariantBlockStateBuilder.PartialBlockstate;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import com.google.gson.JsonObject;

import mctmods.immersivetechnology.common.data.loaders.ITSplitModelBuilder;

public class ITBlockStateProvider extends BlockStateProvider {
    public final Map<Block, ModelFile> unsplitModels = new HashMap<>();
    protected static final Map<ResourceLocation, String> generatedParticleTextures = new HashMap<>();
    protected ExistingFileHelper existingFileHelper;
    protected ITNongeneratedModels innerModels;

    public ITBlockStateProvider(DataGenerator generator, ExistingFileHelper helper) { super(generator.getPackOutput(), ITLib.MODID, helper); this.existingFileHelper = helper; this.innerModels = new ITNongeneratedModels(generator.getPackOutput(), existingFileHelper); }

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

        @Override
        public JsonObject toJson(JsonObject json) {
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

    @Override
    protected void registerStatesAndModels() {
        ITLib.IT_LOGGER.info("Generating Multiblock Splits");

        genericMultiblock("alternator", "metal");
        genericMultiblock("solar_reflector", "metal");
        genericMultiblock("cooling_tower", "stone");
        genericMultiblock("steel_sheetmetal_tank", "metal");
        genericMultiblock("boiler_liquid", "metal");
        genericMultiblock("boiler_tank", "metal");
        genericMultiblock("solar_melter", "metal");
        genericMultiblock("solar_tower", "metal");

        specialActiveMultiblockNoMirror("boiler_solid", "metal", "boiler_solid");

        genericMultiblockMirror("distiller", "metal");
        genericMultiblockMirror("gas_turbine", "metal");
        genericMultiblockMirror("steam_turbine", "metal");

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

        VariantBlockStateBuilder valveFluidBuilder = getVariantBuilder(ITBlocks.Metal.VALVE_FLUID.get());
        valveFluidBuilder.forAllStates(state -> {
            Direction facing = state.getValue(ITProperties.FACING_ALL);
            boolean open = state.getValue(ValveFluidBlock.OPEN);
            int rotationVal = state.getValue(ValveFluidBlock.ROTATION);
            boolean mirrored = state.getValue(ITProperties.MIRRORED);
            ModelFile modelFile = open ? valveOpen : valveClosed;
            int xRot = 0;
            int yRot;
            if (facing.getAxis().isHorizontal()) {
                yRot = ((facing.get2DDataValue() + 2) % 4) * 90;
            } else {
                Direction hFacing = Direction.from2DDataValue(rotationVal);
                yRot = ((hFacing.get2DDataValue() + 2) % 4) * 90;
                xRot = facing == Direction.DOWN ? 90 : 270;
            }
            if (mirrored) yRot = (yRot + 180) % 360;
            return ConfiguredModel.builder().modelFile(modelFile).rotationX(xRot).rotationY(yRot).build();
        });
        setRenderType(RenderType.cutout(), (BlockModelBuilder) valveClosed, (BlockModelBuilder) valveOpen);

        BlockModelBuilder valveLimiterBuilder = models().cubeBottomTop("block/metal/valve_limiter",
                modLoc("block/metal/valve_limiter_side"),
                modLoc("block/metal/valve_limiter_bottom"),
                modLoc("block/metal/valve_limiter_top"));
        valveLimiterBuilder.texture("particle", modLoc("block/metal/valve_limiter_side"));

        VariantBlockStateBuilder valveLimiterStateBuilder = getVariantBuilder(ITBlocks.Metal.VALVE_LIMITER.get());
        valveLimiterStateBuilder.forAllStates(state -> {
            Direction facing = state.getValue(ITProperties.FACING_ALL);
            int rotationVal = state.getValue(ValveLimiterBlock.ROTATION);
            boolean mirrored = state.getValue(ITProperties.MIRRORED);
            int xRot;
            int yRot;
            if (facing.getAxis().isHorizontal()) {
                yRot = ((facing.get2DDataValue() + 2) % 4) * 90;
                xRot = 90;
            } else {
                Direction hFacing = Direction.from2DDataValue(rotationVal);
                yRot = ((hFacing.get2DDataValue() + 2) % 4) * 90;
                xRot = facing == Direction.DOWN ? 180 : 0;
            }
            if (mirrored) yRot = (yRot + 180) % 360;
            return ConfiguredModel.builder().modelFile(valveLimiterBuilder).rotationX(xRot).rotationY(yRot).build();
        });
        setRenderType(RenderType.cutout(), valveLimiterBuilder);

        ModelFile valveLoadClosed = createValveObjModel("valve_load", "valve_load", false, "Base");
        ModelFile valveLoadOpen = createValveObjModel("valve_load", "valve_load", true, "Base");

        VariantBlockStateBuilder valveLoadBuilder = getVariantBuilder(ITBlocks.Metal.VALVE_LOAD.get());
        valveLoadBuilder.forAllStates(state -> {
            Direction facing = state.getValue(ITProperties.FACING_ALL);
            boolean open = state.getValue(ValveLoadBlock.OPEN);
            int rotationVal = state.getValue(ValveLoadBlock.ROTATION);
            boolean mirrored = state.getValue(ITProperties.MIRRORED);
            ModelFile modelFile = open ? valveLoadOpen : valveLoadClosed;
            int xRot;
            int yRot;
            if (facing.getAxis().isHorizontal()) {
                yRot = (facing.get2DDataValue() % 4) * 90;
                xRot = 270;
            } else {
                Direction hFacing = Direction.from2DDataValue(rotationVal);
                yRot = ((hFacing.get2DDataValue() + 1) % 4) * 90;
                xRot = facing == Direction.DOWN ? 180 : 0;
            }
            if (mirrored) yRot = (yRot + 180) % 360;
            return ConfiguredModel.builder().modelFile(modelFile).rotationX(xRot).rotationY(yRot).build();
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

    private ModelFile createValveObjModel(String baseName, String objFolder, boolean isOpen, String baseVisibility) {
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

    private void genericMultiblock(String registry_name, String block_type) {
        ITLib.IT_LOGGER.info("Generating [{}] Multiblock Model Data", registry_name);
        createMultiblock(innerObj("multiblock/" + block_type + "/obj/" + registry_name + "/" + registry_name + ".obj"), (ITTemplateMultiblock) ITMultiblockProvider.getMBTemplate.apply(registry_name), block_type);
    }

    @SuppressWarnings("SameParameterValue")
    private void genericMultiblockMirror(String registry_name, String block_type) {
        ITLib.IT_LOGGER.info("Generating [{}] with Custom Mirror Multiblock Model Data", registry_name);
        createMirroredMultiblock(innerObj("multiblock/" + block_type + "/obj/" + registry_name + "/" + registry_name + ".obj"), innerObj("multiblock/" + block_type + "/obj/" + registry_name  + "/" + registry_name + "_mirrored.obj"), (ITTemplateMultiblock) ITMultiblockProvider.getMBTemplate.apply(registry_name), block_type);
    }

    @SuppressWarnings("SameParameterValue")
    private void specialActiveMultiblockNoMirror(String registry_name, String block_type, String baseTextureName) {
        ITLib.IT_LOGGER.info("Generating [{}] with Active Multiblock Model Data", registry_name);
        String objPath = "multiblock/" + block_type + "/obj/" + registry_name + "/" + registry_name + ".obj";
        Map<String, ResourceLocation> defaultTextures = ImmutableMap.of("cube_front", modLoc("multiblock/" + block_type + "/" + baseTextureName));
        Map<String, ResourceLocation> activeTextures = ImmutableMap.of("cube_front", modLoc("multiblock/" + block_type + "/" + baseTextureName + "_active"));
        ITNongeneratedModel defaultUnsplit = obj(registry_name, modLoc(objPath), defaultTextures, innerModels);
        ITNongeneratedModel activeUnsplit = obj(registry_name + "_active", modLoc(objPath), activeTextures, innerModels);
        ITTemplateMultiblock multiblock = (ITTemplateMultiblock) ITMultiblockProvider.getMBTemplate.apply(registry_name);
        ModelFile defaultMain = split(defaultUnsplit, multiblock, false, block_type);
        ModelFile activeMain = split(activeUnsplit, multiblock, false, block_type);
        createActiveMultiblock(multiblock::getBlock, defaultMain, activeMain, null, null, null, ITProperties.ACTIVE);
    }

    private void createMirroredMultiblock(ITNongeneratedModel unsplitModel, ITNongeneratedModel mirror_model, ITTemplateMultiblock multiblock, String block_type) {
        final ModelFile mainModel = split(unsplitModel, multiblock, false, block_type);
        final ModelFile mirrorModel = split(mirror_model, multiblock, true, block_type);
        if (multiblock.getBlock().getStateDefinition().getProperties().contains(ITProperties.MIRRORED)) { createMultiblock(multiblock::getBlock, mainModel, mirrorModel, ITProperties.MIRRORED); }
        else { createMultiblock(multiblock::getBlock, mainModel, null, null); }
    }

    private void createMultiblock(ITNongeneratedModel unsplitModel, ITTemplateMultiblock multiblock, String block_type) {
        final ModelFile mainModel = split(unsplitModel, multiblock, false, block_type);
        if (multiblock.getBlock().getStateDefinition().getProperties().contains(ITProperties.MIRRORED)) { createMultiblock(multiblock::getBlock, mainModel, split(mirror(unsplitModel, innerModels), multiblock, true, block_type), ITProperties.MIRRORED); }
        else { createMultiblock(multiblock::getBlock, mainModel, null, null); }
    }

    private void createMultiblock(Supplier<? extends Block> b, ModelFile masterModel, @Nullable ModelFile mirroredModel, @Nullable Property<Boolean> mirroredState) {
        unsplitModels.put(b.get(), masterModel);
        Preconditions.checkArgument((mirroredModel == null) == (mirroredState == null));
        VariantBlockStateBuilder builder = getVariantBuilder(b.get());
        EnumProperty<Direction> facing = ITProperties.FACING_HORIZONTAL;
        builder.forAllStates(state -> {
            Direction dir = state.getValue(facing);
            int angleY = getAngle(dir);
            int angleX = 0;
            if (facing.getPossibleValues().contains(Direction.UP)) { angleX = -90 * dir.getStepY(); angleY = dir.getAxis() != Direction.Axis.Y ? getAngle(dir) : 0; }
            boolean mirrored = (mirroredState != null) ? state.getValue(mirroredState) : false;
            ModelFile model = mirrored ? mirroredModel : masterModel;
            return new ConfiguredModel[]{new ConfiguredModel(model, angleX, angleY, true)};
        });
    }

    @SuppressWarnings("SameParameterValue")
    private void createActiveMultiblock(Supplier<? extends Block> b, ModelFile defaultMaster, ModelFile activeMaster, ModelFile defaultMirrored, ModelFile activeMirrored, @Nullable Property<Boolean> mirroredState, @Nullable Property<Boolean> activeState) {
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
            return new ConfiguredModel[]{new ConfiguredModel(model, angleX, angleY, true)};
        });
    }

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

    protected ITNongeneratedModel innerObj(String loc) {
        Preconditions.checkArgument(loc.endsWith(".obj"));
        String name = loc.substring(0, loc.length() - 4);
        ResourceLocation model = modLoc(loc);
        Map<String, ResourceLocation> textures = ImmutableMap.of();
        return obj(innerModels.withExistingParent(name, mcLoc("block")), model, textures);
    }

    protected <T extends ModelBuilder<T>> T obj(T base, ResourceLocation model, Map<String, ResourceLocation> textures) {
        ITObjModelBuilder<T> loader = base.customLoader(ITObjModelBuilder::new);
        loader.modelLocation(addModelsPrefix(model));
        loader.flipV(true);
        loader.automaticCulling(false);
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

    protected <T extends ModelBuilder<T>> T mirror(ITNongeneratedModel inner, ModelProvider<T> provider) {
        String path = inner.getLocation().getPath() + "_mirrored";
        return provider.getBuilder(path).customLoader(ITMirroredModelBuilder::begin).inner(inner).end();
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

    public static <T extends Comparable<T>> void forEach(PartialBlockstate base, Property<T> prop, List<Property<?>> remaining, Consumer<PartialBlockstate> out) {
        for (T value : prop.getPossibleValues()) { forEachState(base.with(prop, value), remaining, out); }
    }

    public static void forEachState(PartialBlockstate base, List<Property<?>> props, Consumer<PartialBlockstate> out) {
        if (!props.isEmpty()) { List<Property<?>> remaining = props.subList(1, props.size()); Property<?> main = props.get(0); forEach(base, main, remaining, out); }
        else { out.accept(base); }
    }

    protected ResourceLocation addModelsPrefix(ResourceLocation in) { return ResourceLocation.fromNamespaceAndPath(in.getNamespace(), "models/" + in.getPath()); }

    protected void setRenderType(@Nullable RenderType type, ModelBuilder<?>... builders) {
        if (type != null) { final String typeName = ITModelProviderUtils.getName(type); for (final ModelBuilder<?> model : builders) { model.renderType(typeName); } }
    }
}

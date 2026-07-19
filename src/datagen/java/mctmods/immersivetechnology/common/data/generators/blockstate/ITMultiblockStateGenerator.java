package mctmods.immersivetechnology.common.data.generators.blockstate;

import mctmods.immersivetechnology.common.blocks.helper.ITProperties;
import mctmods.immersivetechnology.common.data.ITDataGenUtils;
import mctmods.immersivetechnology.common.data.builders.ITSplitModelBuilder;
import mctmods.immersivetechnology.common.data.builders.ITObjModelBuilder;
import mctmods.immersivetechnology.common.data.builders.ITMirroredModelBuilder;
import mctmods.immersivetechnology.common.data.models.ITNongeneratedModels;
import mctmods.immersivetechnology.common.data.models.ITNongeneratedModels.ITNongeneratedModel;
import mctmods.immersivetechnology.common.multiblocks.helper.ITTemplateMultiblock;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.registration.ITMultiblockRegistry;
import mctmods.immersivetechnology.common.data.generators.ITBlockState;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelBuilder;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.VariantBlockStateBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ITMultiblockStateGenerator {
    private final ITBlockState main;
    private final ExistingFileHelper existingFileHelper;
    private final ITNongeneratedModels innerModels;

    public final Map<Block, ModelFile> unsplitModels = new HashMap<>();

    public ITMultiblockStateGenerator(ITBlockState main, ExistingFileHelper helper) {
        this.main = main;
        this.existingFileHelper = helper;
        this.innerModels = new ITNongeneratedModels(main.getPackOutput(), existingFileHelper);
    }

    public void generate() {
        ITLib.IT_LOGGER.info("Generating Multiblock Splits");
        generateMultiblockConfig("advanced_coke_oven", "stone", false, false, ImmutableMap.of(), ImmutableMap.of(), null);
        generateMultiblockConfig("alternator", "metal", false, false, ImmutableMap.of(), ImmutableMap.of(), null);
        generateMultiblockConfig("boiler_liquid", "metal", false, false, ImmutableMap.of(), ImmutableMap.of(), "cutout_mipped");
        generateMultiblockConfig("boiler_solid", "metal", false, true, ImmutableMap.of("cube_front", main.modLoc("multiblock/metal/boiler_solid")), ImmutableMap.of("cube_front", main.modLoc("multiblock/metal/boiler_solid_active")), "cutout_mipped");
        generateMultiblockConfig("boiler_tank", "metal", false, false, ImmutableMap.of(), ImmutableMap.of(), null);
        generateMultiblockConfig("cooling_tower", "stone", true, false, ImmutableMap.of(), ImmutableMap.of(), "cutout_mipped");
        generateMultiblockConfig("distiller", "metal", true, false, ImmutableMap.of(), ImmutableMap.of(), null);
        generateMultiblockConfig("electrolytic_crucible_battery", "metal", true, false, ImmutableMap.of(), ImmutableMap.of(), null);
        generateMultiblockConfig("gas_turbine", "metal", true, false, ImmutableMap.of(), ImmutableMap.of(), null);
        generateMultiblockConfig("heat_exchanger", "metal", false, false, ImmutableMap.of(), ImmutableMap.of(), null);
        generateMultiblockConfig("melting_crucible", "metal", true, false, ImmutableMap.of(), ImmutableMap.of(), null);
        generateMultiblockConfig("radiator", "metal", false, false, ImmutableMap.of(), ImmutableMap.of(), null);
        generateMultiblockConfig("radiator_horizontal", "metal", false, false, ImmutableMap.of(), ImmutableMap.of(), null);
        generateMultiblockConfig("solar_melter", "metal", true, false, ImmutableMap.of(), ImmutableMap.of(), null);
        generateMultiblockConfig("solar_reflector", "metal", false, false, ImmutableMap.of(), ImmutableMap.of(), null);
        generateMultiblockConfig("solar_tower", "metal", true, false, ImmutableMap.of(), ImmutableMap.of(), null);
        generateMultiblockConfig("steam_turbine", "metal", true, false, ImmutableMap.of(), ImmutableMap.of(), null);
        generateMultiblockConfig("steel_sheetmetal_tank", "metal", true, false, ImmutableMap.of(), ImmutableMap.of(), "cutout_mipped");
    }

    private ITNongeneratedModel createMirrorWrappedModel(String name, ITNongeneratedModel inner) {
        ITNongeneratedModel base = innerModels.withExistingParent(name, main.mcLoc("block"));
        ITNongeneratedModel ret = base.customLoader(ITMirroredModelBuilder::begin).inner(inner).end();
        ret.ao(false);
        String particleTex = ITBlockState.generatedParticleTextures.get(inner.getLocation());
        if (particleTex != null) {
            ret.texture("particle", particleTex);
            ITBlockState.generatedParticleTextures.put(ret.getLocation(), particleTex);
        }
        return ret;
    }

    private void generateMultiblockConfig(String registry_name, String block_type, boolean useSeparateMirror, boolean hasActive, Map<String, ResourceLocation> defaultTextures, Map<String, ResourceLocation> activeTextures, @Nullable String renderType) {
        if (!hasActive) { defaultTextures = ImmutableMap.of(); activeTextures = ImmutableMap.of(); }
        ITLib.IT_LOGGER.info("Generating [{}] Multiblock Model Data", registry_name);
        ITTemplateMultiblock multiblock = (ITTemplateMultiblock) ITMultiblockRegistry.getMBTemplate.apply(registry_name);
        boolean hasMirror = multiblock.getBlock().getStateDefinition().getProperties().contains(ITProperties.MIRRORED);
        boolean flipMirror = hasMirror && useSeparateMirror;
        String baseObjPath = "multiblock/" + block_type + "/obj/" + registry_name + "/" + registry_name + ".obj";
        String mirroredObjPath = baseObjPath.replace(".obj", "_mirrored.obj");
        ITNongeneratedModel defaultUnsplit = createUnsplitModel(registry_name, baseObjPath, defaultTextures, renderType);
        ITNongeneratedModel activeUnsplit = hasActive ? createUnsplitModel(registry_name + "_active", baseObjPath, activeTextures, renderType) : null;
        ITNongeneratedModel mirroredUnsplit = null;
        ITNongeneratedModel activeMirroredUnsplit = null;
        if (hasMirror) {
            if (flipMirror) {
                mirroredUnsplit = createUnsplitModel(registry_name + "_mirrored", mirroredObjPath, defaultTextures, renderType);
                if (hasActive) { activeMirroredUnsplit = createUnsplitModel(registry_name + "_active_mirrored", mirroredObjPath, activeTextures, renderType); }
            } else {
                mirroredUnsplit = createMirrorWrappedModel(registry_name + "_mirrored", defaultUnsplit);
                if (hasActive) { activeMirroredUnsplit = createMirrorWrappedModel(registry_name + "_active_mirrored", activeUnsplit); }
            }
        }
        ModelFile defaultMain = split(defaultUnsplit, multiblock, false, block_type);
        ModelFile activeMain = hasActive ? split(activeUnsplit, multiblock, false, block_type) : null;
        ModelFile defaultMirrored = hasMirror ? split(mirroredUnsplit, multiblock, true, block_type) : null;
        ModelFile activeMirrored = hasActive && hasMirror ? split(activeMirroredUnsplit, multiblock, true, block_type) : null;
        createMultiblockVariant(multiblock::getBlock, defaultMain, activeMain, defaultMirrored, activeMirrored, hasMirror ? ITProperties.MIRRORED : null, hasActive ? ITProperties.ACTIVE : null);
    }

    private ITNongeneratedModel createUnsplitModel(String name, String objPathStr, Map<String, ResourceLocation> textures, @Nullable String renderType) {
        ResourceLocation objPath = main.modLoc(objPathStr);
        ITNongeneratedModel base = innerModels.withExistingParent(name, main.mcLoc("block"));
        ITObjModelBuilder<ITNongeneratedModel> loader = base.customLoader(ITObjModelBuilder::begin)
                .modelLocation(addModelsPrefix(objPath))
                .flipV(true)
                .automaticCulling(false)
                .shadeQuads(true)
                .emissiveAmbient(true);
        String path = objPath.getPath();
        if (path.endsWith("_mirrored.obj")) {
            String fileName = path.substring(path.lastIndexOf('/') + 1);
            String originalMtl = fileName.replace("_mirrored.obj", ".mtl");
            loader = loader.mtlOverride(originalMtl);
        }
        ITNongeneratedModel ret = loader.end();
        if (renderType != null) { ((ModelBuilder<?>) ret).renderType(renderType); }
        ret.ao(false);
        ResourceLocation textureModel = objPath;
        if (path.endsWith("_mirrored.obj")) {
            textureModel = ResourceLocation.fromNamespaceAndPath(objPath.getNamespace(), path.replace("_mirrored.obj", ".obj"));
        }
        String particleTex = ITDataGenUtils.getTextureFromObj(textureModel, existingFileHelper);
        if (particleTex.charAt(0) == '#') { particleTex = textures.getOrDefault(particleTex.substring(1), main.modLoc("block/metal/technology_engineering")).toString(); }
        ret.texture("particle", particleTex);
        ITBlockState.generatedParticleTextures.put(ret.getLocation(), particleTex);
        textures.forEach(ret::texture);
        return ret;
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
        Stream<Vec3i> partsStream = multiblock.getTemplate(null).blocksWithoutAir().stream().map(StructureBlockInfo::pos).map(transform).map(p -> p.subtract(offset));
        String baseName = unsplit.getLocation().getPath().substring(unsplit.getLocation().getPath().lastIndexOf("/") + 1).replace(".obj", "");
        return splitModel("multiblock/" + block_type + "/split/" + baseName + "_split", unsplit, partsStream.collect(Collectors.toList()));
    }

    protected BlockModelBuilder splitModel(String name, ModelBuilder<?> model, List<Vec3i> parts) {
        if (parts == null || parts.isEmpty()) { parts = List.of(new Vec3i(0, 0, 0)); }
        BlockModelBuilder result = main.models().withExistingParent(name, main.mcLoc("block"))
                .customLoader(ITSplitModelBuilder::begin)
                .innerModel(model)
                .parts(parts)
                .dynamic(false)
                .end();

        addParticleTextureFrom(result, model);
        return result;
    }

    protected void addParticleTextureFrom(BlockModelBuilder result, ModelBuilder<?> model) {
        String particles = ITBlockState.generatedParticleTextures.get(model.getLocation());
        if (particles != null) { result.texture("particle", particles); ITBlockState.generatedParticleTextures.put(result.getLocation(), particles); }
    }

    private void loadTemplateFor(ITTemplateMultiblock multiblock) {
        final ResourceLocation name = multiblock.getUniqueName();
        if (ITTemplateMultiblock.SYNCED_CLIENT_TEMPLATES.containsKey(name)) {
            return;
        }
        ITLib.IT_LOGGER.info("Loading structure template for {} during datagen", name);
        final String filePath = "structures/" + name.getPath() + ".nbt";
        int slash = filePath.indexOf('/');
        String prefix = filePath.substring(0, slash);
        ResourceLocation shortLoc = ResourceLocation.fromNamespaceAndPath(name.getNamespace(), filePath.substring(slash + 1));
        try {
            final Resource resource = existingFileHelper.getResource(shortLoc, PackType.SERVER_DATA, "", prefix);
            try (final InputStream input = resource.open()) {
                final CompoundTag nbt = NbtIo.readCompressed(input, NbtAccounter.unlimitedHeap());
                final StructureTemplate template = new StructureTemplate();
                template.load(BuiltInRegistries.BLOCK.asLookup(), nbt);
                ITTemplateMultiblock.SYNCED_CLIENT_TEMPLATES.put(name, template);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed on " + name, e);
        }
    }

    private void createMultiblockVariant(Supplier<? extends Block> b, ModelFile defaultMaster, @Nullable ModelFile activeMaster, @Nullable ModelFile defaultMirrored, @Nullable ModelFile activeMirrored, @Nullable Property<Boolean> mirroredState, @Nullable Property<Boolean> activeState) {
        unsplitModels.put(b.get(), defaultMaster);
        Preconditions.checkArgument((defaultMirrored == null) == (mirroredState == null));
        Preconditions.checkArgument((activeMaster == null) == (activeState == null));
        VariantBlockStateBuilder builder = main.getVariantBuilder(b.get());
        ITLib.IT_LOGGER.info("Registered blockstate for multiblock: {}", BuiltInRegistries.BLOCK.getKey(b.get()));
        StateDefinition<?, ?> def = b.get().getStateDefinition();
        EnumProperty<Direction> facing = def.getProperties().contains(ITProperties.FACING_HORIZONTAL) ? ITProperties.FACING_HORIZONTAL : null;
        try {
            builder.forAllStates(state -> {
                int angleY = 0;
                int angleX = 0;
                if (facing != null && state.hasProperty(facing)) {
                    Direction dir = state.getValue(facing);
                    angleY = getAngle(dir);
                    if (facing.getPossibleValues().contains(Direction.UP)) { angleX = -90 * dir.getStepY(); angleY = dir.getAxis() != Direction.Axis.Y ? getAngle(dir) : 0; }
                }
                boolean mirrored = (mirroredState != null && state.hasProperty(mirroredState)) ? state.getValue(mirroredState) : false;
                boolean active = (activeState != null && state.hasProperty(activeState)) ? state.getValue(activeState) : false;
                ModelFile baseModel = active ? activeMaster : defaultMaster;
                ModelFile model = mirrored ? (active ? activeMirrored : defaultMirrored) : baseModel;
                assert model != null;
                return new ConfiguredModel[]{new ConfiguredModel(model, angleX, angleY, true)};
            });
        } catch (IllegalArgumentException e) { if (e.getMessage() == null || !e.getMessage().contains("already been configured")) { throw e; } }
    }

    protected int getAngle(Direction dir) { return (int) ((dir.toYRot() + 180) % 360); }

    protected ResourceLocation addModelsPrefix(ResourceLocation in) {
        return ResourceLocation.fromNamespaceAndPath(in.getNamespace(), "models/" + in.getPath());
    }
}

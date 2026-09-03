package mctmods.immersivetechnology.common.data.generators.blockstate;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.immersiveconvergence.api.block.ModProperties;
import mctmods.immersivetechnology.common.data.util.GeneratorUtils;
import mctmods.immersivetechnology.common.data.generators.ModBlockState;
import mctmods.immersivetechnology.common.data.builders.MirroredModelBuilder;
import com.immersiveconvergence.api.multiblock.MachineTemplateMultiblock;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.core.Direction;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.VariantBlockStateBuilder;
import net.minecraftforge.client.model.generators.loaders.ObjModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class MultiblockStateGenerator {
    private final ModBlockState main;
    private final ExistingFileHelper existingFileHelper;

    public final Map<Block, ModelFile> unsplitModels = new HashMap<>();

    public MultiblockStateGenerator(ModBlockState main, ExistingFileHelper helper) {
        this.main = main;
        this.existingFileHelper = helper;
    }

    public void generate() {
        Reference.IT_LOGGER.info("Generating Multiblock Models");
        generateMultiblockConfig("advanced_coke_oven", "stone", false, false, ImmutableMap.of(), ImmutableMap.of(), null);
        generateMultiblockConfig("alternator", "metal", false, false, ImmutableMap.of(), ImmutableMap.of(), null);
        generateMultiblockConfig("boiler_liquid", "metal", false, false, ImmutableMap.of(), ImmutableMap.of(), "cutout_mipped");
        generateMultiblockConfig("boiler_solid", "metal", false, true, ImmutableMap.of("cube_front", main.modLoc("multiblock/metal/boiler_solid")), ImmutableMap.of("cube_front", main.modLoc("multiblock/metal/boiler_solid_active")), "cutout_mipped");
        generateMultiblockConfig("boiler_tank", "metal", false, false, ImmutableMap.of(), ImmutableMap.of(), null);
        generateMultiblockConfig("cooling_tower", "stone", false, false, ImmutableMap.of(), ImmutableMap.of(), null);
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
        generateMultiblockConfig("steel_sheetmetal_tank", "metal", false, false, ImmutableMap.of(), ImmutableMap.of(), "cutout_mipped");
    }

    private BlockModelBuilder createMirrorWrappedModel(String name, BlockModelBuilder inner) {
        BlockModelBuilder base = main.models().withExistingParent(name, main.mcLoc("block"));
        BlockModelBuilder ret = base.customLoader(MirroredModelBuilder::begin).inner(inner).end();
        ret.ao(false);
        String particleTex = ModBlockState.generatedParticleTextures.get(inner.getLocation());
        if (particleTex != null) {
            ret.texture("particle", particleTex);
            ModBlockState.generatedParticleTextures.put(ret.getLocation(), particleTex);
        }
        return ret;
    }

    private void generateMultiblockConfig(String registry_name, String block_type, boolean useSeparateMirror, boolean hasActive, Map<String, ResourceLocation> defaultTextures, Map<String, ResourceLocation> activeTextures, @Nullable String renderType) {
        if (!hasActive) { defaultTextures = ImmutableMap.of(); activeTextures = ImmutableMap.of(); }
        Reference.IT_LOGGER.info("Generating [{}] Multiblock Model Data", registry_name);
        MachineTemplateMultiblock multiblock = (MachineTemplateMultiblock) MultiblockRegistry.getMBTemplate.apply(registry_name);
        boolean hasMirror = multiblock.getBlock().getStateDefinition().getProperties().contains(ModProperties.MIRRORED);
        boolean flipMirror = hasMirror && useSeparateMirror;
        String baseObjPath = "multiblock/" + block_type + "/" + registry_name + "/" + registry_name + ".obj";
        String mirroredObjPath = baseObjPath.replace(".obj", "_mirrored.obj");
        String modelPrefix = "multiblock/" + block_type + "/";
        BlockModelBuilder defaultUnsplit = createUnsplitModel(modelPrefix + registry_name, baseObjPath, defaultTextures, renderType);
        BlockModelBuilder activeUnsplit = hasActive ? createUnsplitModel(modelPrefix + registry_name + "_active", baseObjPath, activeTextures, renderType) : null;
        BlockModelBuilder mirroredUnsplit = null;
        BlockModelBuilder activeMirroredUnsplit = null;
        if (hasMirror) {
            if (flipMirror) {
                mirroredUnsplit = createUnsplitModel(modelPrefix + registry_name + "_mirrored", mirroredObjPath, defaultTextures, renderType);
                if (hasActive) { activeMirroredUnsplit = createUnsplitModel(modelPrefix + registry_name + "_active_mirrored", mirroredObjPath, activeTextures, renderType); }
            } else {
                mirroredUnsplit = createMirrorWrappedModel(modelPrefix + registry_name + "_mirrored", defaultUnsplit);
                if (hasActive) { activeMirroredUnsplit = createMirrorWrappedModel(modelPrefix + registry_name + "_active_mirrored", activeUnsplit); }
            }
        }
        createMultiblockVariant(multiblock::getBlock, defaultUnsplit, activeUnsplit, mirroredUnsplit, activeMirroredUnsplit, hasMirror ? ModProperties.MIRRORED : null, hasActive ? ModProperties.ACTIVE : null);
    }

    private BlockModelBuilder createUnsplitModel(String name, String objPathStr, Map<String, ResourceLocation> textures, @Nullable String renderType) {
        ResourceLocation objPath = main.modLoc(objPathStr);
        BlockModelBuilder base = main.models().withExistingParent(name, main.mcLoc("block"));
        ObjModelBuilder<BlockModelBuilder> loader = base.customLoader(ObjModelBuilder::begin);
        ResourceLocation modelLocation = addModelsPrefix(objPath);
        loader.modelLocation(modelLocation);
        if (renderType != null) { base.renderType(renderType); }
        loader.flipV(true);
        loader.automaticCulling(false);
        loader.shadeQuads(true);
        loader.emissiveAmbient(true);
        String path = objPath.getPath();
        ResourceLocation textureModel = objPath;
        if (path.endsWith("_mirrored.obj")) {
            textureModel = ResourceLocation.fromNamespaceAndPath(objPath.getNamespace(), path.replace("_mirrored.obj", ".obj"));
            loader.overrideMaterialLibrary(ResourceLocation.fromNamespaceAndPath(modelLocation.getNamespace(), modelLocation.getPath().replace("_mirrored.obj", ".mtl")));
        }
        BlockModelBuilder ret = loader.end();
        ret.ao(false);
        String particleTex = GeneratorUtils.getTextureFromObj(textureModel, existingFileHelper);
        if (particleTex.charAt(0) == '#') { particleTex = textures.getOrDefault(particleTex.substring(1), main.modLoc("block/metal/technology_engineering")).toString(); }
        ret.texture("particle", particleTex);
        ModBlockState.generatedParticleTextures.put(ret.getLocation(), particleTex);
        textures.forEach(ret::texture);
        return ret;
    }

    protected ResourceLocation addModelsPrefix(ResourceLocation in) { return ResourceLocation.fromNamespaceAndPath(in.getNamespace(), "models/" + in.getPath()); }

    protected int getAngle(Direction dir) { return (int) ((dir.toYRot() + 180) % 360); }

    private void createMultiblockVariant(Supplier<? extends Block> b, ModelFile defaultMaster, @Nullable ModelFile activeMaster, @Nullable ModelFile defaultMirrored, @Nullable ModelFile activeMirrored, @Nullable Property<Boolean> mirroredState, @Nullable Property<Boolean> activeState) {
        unsplitModels.put(b.get(), defaultMaster);
        Preconditions.checkArgument((defaultMirrored == null) == (mirroredState == null));
        Preconditions.checkArgument((activeMaster == null) == (activeState == null));
        VariantBlockStateBuilder builder = main.getVariantBuilder(b.get());
        EnumProperty<Direction> facing = ModProperties.FACING_HORIZONTAL;
        builder.forAllStates(state -> {
            Direction dir = state.getValue(facing);
            int angleY = getAngle(dir);
            int angleX = 0;
            if (facing.getPossibleValues().contains(Direction.UP)) { angleX = -90 * dir.getStepY(); angleY = dir.getAxis() != Direction.Axis.Y ? getAngle(dir) : 0; }
            boolean mirrored = mirroredState != null && state.getValue(mirroredState);
            boolean active = activeState != null && state.getValue(activeState);
            ModelFile baseModel = active ? activeMaster : defaultMaster;
            ModelFile model = mirrored ? (active ? activeMirrored : defaultMirrored) : baseModel;
            assert model != null;
            return new ConfiguredModel[]{new ConfiguredModel(model, angleX, angleY, true)};
        });
    }
}

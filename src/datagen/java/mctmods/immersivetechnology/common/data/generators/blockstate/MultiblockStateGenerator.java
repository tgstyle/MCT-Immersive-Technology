package mctmods.immersivetechnology.common.data.generators.blockstate;

import com.immersiveconvergence.api.block.ModProperties;
import mctmods.immersivetechnology.common.data.util.GeneratorUtils;
import mctmods.immersivetechnology.common.data.builders.MirroredModelBuilder;
import com.immersiveconvergence.api.multiblock.MachineTemplateMultiblock;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;
import mctmods.immersivetechnology.common.data.generators.ModBlockState;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelBuilder;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.VariantBlockStateBuilder;
import net.neoforged.neoforge.client.model.generators.loaders.ObjModelBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class MultiblockStateGenerator {
    private final ModBlockState main;
    private final ExistingFileHelper existingFileHelper;

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
        generateMultiblockConfig("stylenox", "metal", true, false, ImmutableMap.of(), ImmutableMap.of(), null);
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
        ResourceLocation modelLocation = addModelsPrefix(objPath);
        ObjModelBuilder<BlockModelBuilder> loader = base.customLoader(ObjModelBuilder::begin)
                .modelLocation(modelLocation)
                .flipV(true)
                .automaticCulling(false)
                .shadeQuads(true)
                .emissiveAmbient(true);
        String path = objPath.getPath();
        if (path.endsWith("_mirrored.obj")) {
            loader.overrideMaterialLibrary(ResourceLocation.fromNamespaceAndPath(modelLocation.getNamespace(), modelLocation.getPath().replace("_mirrored.obj", ".mtl")));
        }
        BlockModelBuilder ret = loader.end();
        if (renderType != null) { ret.renderType(renderType); }
        ret.ao(false);
        ResourceLocation textureModel = objPath;
        if (path.endsWith("_mirrored.obj")) {
            textureModel = ResourceLocation.fromNamespaceAndPath(objPath.getNamespace(), path.replace("_mirrored.obj", ".obj"));
        }
        String particleTex = GeneratorUtils.getTextureFromObj(textureModel, existingFileHelper);
        if (particleTex.charAt(0) == '#') { particleTex = textures.getOrDefault(particleTex.substring(1), main.modLoc("block/metal/technology_engineering")).toString(); }
        ret.texture("particle", particleTex);
        ModBlockState.generatedParticleTextures.put(ret.getLocation(), particleTex);
        textures.forEach(ret::texture);
        return ret;
    }

    private void createMultiblockVariant(Supplier<? extends Block> b, ModelFile defaultMaster, @Nullable ModelFile activeMaster, @Nullable ModelFile defaultMirrored, @Nullable ModelFile activeMirrored, @Nullable Property<Boolean> mirroredState, @Nullable Property<Boolean> activeState) {
        Preconditions.checkArgument((defaultMirrored == null) == (mirroredState == null));
        Preconditions.checkArgument((activeMaster == null) == (activeState == null));
        VariantBlockStateBuilder builder = main.getVariantBuilder(b.get());
        Reference.IT_LOGGER.info("Registered blockstate for multiblock: {}", BuiltInRegistries.BLOCK.getKey(b.get()));
        StateDefinition<?, ?> def = b.get().getStateDefinition();
        EnumProperty<Direction> facing = def.getProperties().contains(ModProperties.FACING_HORIZONTAL) ? ModProperties.FACING_HORIZONTAL : null;
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

package mctmods.immersivetechnology.common.data.generators;

import blusunrize.immersiveengineering.api.IEEnums.IOSideConfig;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.multiblocks.TemplateMultiblock;
import blusunrize.immersiveengineering.client.models.ModelConfigurableSides;
import blusunrize.immersiveengineering.data.DataGenUtils;
import blusunrize.immersiveengineering.data.models.*;
import blusunrize.immersiveengineering.data.models.NongeneratedModels.NongeneratedModel;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import mctmods.immersivetechnology.common.blocks.metal.SteelBarrelBlock;
import mctmods.immersivetechnology.common.blocks.multiblocks.ITTemplateMultiblock;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.registration.ITBlocks;
import mctmods.immersivetechnology.core.registration.ITFluids;
import mctmods.immersivetechnology.core.registration.ITMultiblockProvider;
import net.minecraft.client.renderer.RenderStateShard;
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
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.client.model.generators.*;
import net.minecraftforge.client.model.generators.VariantBlockStateBuilder.PartialBlockstate;
import net.minecraftforge.client.model.generators.loaders.ObjModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.slf4j.Logger;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ITBlockStateProvider extends BlockStateProvider {
    protected static final List<Vec3i> COLUMN_THREE = ImmutableList.of(BlockPos.ZERO.north(1), BlockPos.ZERO, BlockPos.ZERO.south(1));
    public final Map<Block, ModelFile> unsplitModels = new HashMap<>();
    protected static final Map<ResourceLocation, String> generatedParticleTextures = new HashMap<>();
    protected final ExistingFileHelper existingFileHelper;
    protected final NongeneratedModels innerModels;
    protected Logger logger = ITLib.getNewLogger();

    public ITBlockStateProvider(DataGenerator generator, ExistingFileHelper helper) {
        super(generator.getPackOutput(), ITLib.MODID, helper);
        this.existingFileHelper = helper;
        this.innerModels = new NongeneratedModels(generator.getPackOutput(), existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        ITLib.IT_LOGGER.info("Generating Multiblock Splits");
        genericmultiblockMirror("boiler");
        genericmultiblockMirror("distiller");
        genericmultiblockMirror("steam_turbine");
        genericmultiblockMirror("gas_turbine");
        genericmultiblock("alternator");
        genericmultiblock("coke_oven_advanced");
        genericmultiblock("solar_tower");

        createSimpleBlock(ITBlocks.getBlock.apply("reinforced_coke_brick"), models().cubeAll("block/stone/reinforced_coke_brick", modLoc("block/stone/reinforced_coke_brick")));
        createSimpleBlock(ITBlocks.getBlock.apply("creative_barrel"), models().cubeAll("block/metal/creative_barrel", modLoc("block/metal/creative_barrel")));

        VariantBlockStateBuilder steelBuilder = getVariantBuilder(ITBlocks.MetalDevices.STEEL_BARREL.get());
        BlockModelBuilder steelModel = models().getBuilder("block/metal/steel_barrel")
                .customLoader(SideConfigBuilder::begin)
                .type(ModelConfigurableSides.Type.VERTICAL)
                .baseName(modLoc("block/metal/steel_barrel"))
                .end();
        steelBuilder.partialState().setModels(new ConfiguredModel(steelModel));

        BlockModelBuilder openModel = models().getBuilder("block/metal/open_barrel")
                .texture("up", modLoc("block/metal/open_barrel_up"))
                .texture("down", modLoc("block/metal/open_barrel_down"))
                .texture("side", modLoc("block/metal/open_barrel_side"))
                .texture("particle", modLoc("block/metal/open_barrel_side"));
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
        createSimpleBlock(ITBlocks.getBlock.apply("open_barrel"), openModel);

        createMultiblock(ITBlocks.MetalDevices.COKE_OVEN_PREHEATER, split(innerObj("block/metal/obj/coke_oven_preheater.obj", RenderType.cutout())));

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
    }

    private void createSimpleBlock(Block block, ModelFile model) { getVariantBuilder(block).partialState().setModels(new ConfiguredModel(model)); }

    private void genericmultiblock(String registry_name) { ITLib.IT_LOGGER.info("Generating [" + registry_name + "] Multiblock Model Data"); createMultiblock(innerObj("block/multiblock/obj/" + registry_name + "/" + registry_name + ".obj"), ITMultiblockProvider.getMBTemplate.apply(registry_name)); }

    private void genericmultiblock(String registry_name, boolean dynamic) { ITLib.IT_LOGGER.info("Generating [" + registry_name + "] Multiblock Model Data"); createMultiblock(innerObj("block/multiblock/obj/" + registry_name + "/" + registry_name + ".obj"), ITMultiblockProvider.getMBTemplate.apply(registry_name), dynamic); }

    private void genericmultiblockMirror(String registry_name) { ITLib.IT_LOGGER.info("Generating [" + registry_name + "] with Custom Mirror Multiblock Model Data"); testCreateMultiblock(innerObj("block/multiblock/obj/" + registry_name + "/" + registry_name + ".obj"), innerObj("block/multiblock/obj/" + registry_name + "/" + registry_name + "_mirrored.obj"), (ITTemplateMultiblock) ITMultiblockProvider.getMBTemplate.apply(registry_name)); }

    private void createMultiblock(NongeneratedModel unsplitModel, TemplateMultiblock multiblock) { createMultiblock(unsplitModel, (ITTemplateMultiblock) multiblock, false); }

    private void createMultiblock(NongeneratedModel unsplitModel, TemplateMultiblock multiblock, boolean dynamic) { createMultiblock(unsplitModel, (ITTemplateMultiblock) multiblock, dynamic); }

    private void createMultiblock(NongeneratedModel unsplitModel, ITTemplateMultiblock multiblock, boolean dynamic) {
        final ModelFile mainModel = split(unsplitModel, multiblock, false, dynamic);
        if (multiblock.getBlock().getStateDefinition().getProperties().contains(IEProperties.MIRRORED)) {
            createMultiblock(multiblock::getBlock, mainModel, split(mirror(unsplitModel, innerModels), multiblock, true, dynamic), IEProperties.MIRRORED);
        }
        else { createMultiblock(multiblock::getBlock, mainModel, null, null); }
    }

    private void testCreateMultiblock(NongeneratedModel unsplitModel, NongeneratedModel mirror_model, ITTemplateMultiblock multiblock) {
        final ModelFile mainModel = split(unsplitModel, multiblock, false, false);
        final ModelFile mirrorModel = split(mirror_model, multiblock, true, false);
        if (multiblock.getBlock().getStateDefinition().getProperties().contains(IEProperties.MIRRORED)) {
            createMultiblock(multiblock::getBlock, mainModel, mirrorModel, IEProperties.MIRRORED);
        }
        else { createMultiblock(multiblock::getBlock, mainModel, null, null); }
    }

    private void createMultiblock(Supplier<? extends Block> b, ModelFile masterModel) { createMultiblock(b, masterModel, null, null); }

    private void createMultiblock(Supplier<? extends Block> b, ModelFile masterModel, @Nullable ModelFile mirroredModel, @Nullable Property<Boolean> mirroredState) {
        unsplitModels.put(b.get(), masterModel);
        Preconditions.checkArgument((mirroredModel == null) == (mirroredState == null));
        VariantBlockStateBuilder builder = getVariantBuilder(b.get());
        boolean[] possibleMirrorStates = mirroredState != null ? new boolean[]{false, true} : new boolean[1];
        EnumProperty<Direction> facing = IEProperties.FACING_HORIZONTAL;
        for (boolean mirrored : possibleMirrorStates) {
            for (Direction dir : facing.getPossibleValues()) {
                final int angleY;
                final int angleX;
                if (facing.getPossibleValues().contains(Direction.UP)) {
                    angleX = -90 * dir.getStepY();
                    angleY = dir.getAxis() != Direction.Axis.Y ? getAngle(dir, 180) : 0;
                }
                else {
                    angleY = getAngle(dir, 180);
                    angleX = 0;
                }
                ModelFile model = mirrored ? mirroredModel : masterModel;
                PartialBlockstate partialState = builder.partialState().with(facing, dir);
                if (mirroredState != null) { partialState = partialState.with(mirroredState, mirrored); }
                partialState.setModels(new ConfiguredModel(model, angleX, angleY, true));
            }
        }
    }

    private ModelFile split(NongeneratedModel loc, ITTemplateMultiblock mb) { return split(loc, mb, false, false); }

    private ModelFile split(NongeneratedModel loc, ITTemplateMultiblock mb, boolean mirror, boolean dynamic) {
        UnaryOperator<BlockPos> transform = UnaryOperator.identity();
        if (mirror) {
            loadTemplateFor(mb);
            Vec3i size = mb.getSize(null);
            transform = p -> new BlockPos(size.getX() - p.getX() - 1, p.getY(), p.getZ());
        }
        return split(loc, mb, transform, dynamic);
    }

    private ModelFile split(NongeneratedModel name, ITTemplateMultiblock multiblock, UnaryOperator<BlockPos> transform, boolean dynamic) {
        loadTemplateFor(multiblock);
        final Vec3i offset = multiblock.getMasterFromOriginOffset();
        Stream<Vec3i> partsStream = multiblock.getTemplate(null).blocksWithoutAir().stream().map(StructureTemplate.StructureBlockInfo::pos).map(transform).map(p -> p.subtract(offset));
        return split(name, partsStream.collect(Collectors.toList()), dynamic);
    }

    private void loadTemplateFor(TemplateMultiblock multiblock) {
        final ResourceLocation name = multiblock.getUniqueName();
        if (TemplateMultiblock.SYNCED_CLIENT_TEMPLATES.containsKey(name)) { return; }
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
                TemplateMultiblock.SYNCED_CLIENT_TEMPLATES.put(name, template);
            }
        }
        catch (IOException e) { throw new RuntimeException("Failed on " + name, e); }
    }

    protected NongeneratedModel innerObj(String loc, @Nullable RenderType layer) {
        Preconditions.checkArgument(loc.endsWith(".obj"));
        final var result = obj(loc.substring(0, loc.length() - 4), modLoc(loc), innerModels);
        setRenderType(layer, result);
        return result;
    }

    protected NongeneratedModel innerObj(String loc) { return innerObj(loc, null); }

    protected BlockModelBuilder obj(String loc) { return obj(loc, (RenderType) null); }

    protected BlockModelBuilder obj(String loc, @Nullable RenderType layer) {
        final var model = obj(loc, models());
        setRenderType(layer, model);
        return model;
    }

    protected <T extends ModelBuilder<T>> T obj(String loc, ModelProvider<T> modelProvider) {
        Preconditions.checkArgument(loc.endsWith(".obj"));
        return obj(loc.substring(0, loc.length() - 4), modLoc(loc), modelProvider);
    }

    protected <T extends ModelBuilder<T>> T obj(String name, ResourceLocation model, ModelProvider<T> provider) {
        return obj(name, model, ImmutableMap.of(), provider);
    }

    protected <T extends ModelBuilder<T>> T obj(String name, ResourceLocation model, Map<String, ResourceLocation> textures, ModelProvider<T> provider) {
        return obj(provider.withExistingParent(name, mcLoc("block")), model, textures);
    }

    protected <T extends ModelBuilder<T>> T obj(T base, ResourceLocation model, Map<String, ResourceLocation> textures) {
        assertModelExists(model);
        T ret = base.customLoader(ObjModelBuilder::begin).automaticCulling(false).modelLocation(addModelsPrefix(model)).flipV(true).end();
        String particleTex = DataGenUtils.getTextureFromObj(model, existingFileHelper);
        if (particleTex.charAt(0) == '#') { particleTex = textures.get(particleTex.substring(1)).toString(); }
        ret.texture("particle", particleTex);
        generatedParticleTextures.put(ret.getLocation(), particleTex);
        for (Map.Entry<String, ResourceLocation> e : textures.entrySet()) { ret.texture(e.getKey(), e.getValue()); }
        return ret;
    }

    protected BlockModelBuilder splitModel(String name, NongeneratedModel model, List<Vec3i> parts, boolean dynamic) {
        BlockModelBuilder result = models().withExistingParent(name, mcLoc("block")).customLoader(SplitModelBuilder::begin).innerModel(model).parts(parts).dynamic(dynamic).end();
        addParticleTextureFrom(result, model);
        return result;
    }

    protected ModelFile split(NongeneratedModel baseModel, List<Vec3i> parts, boolean dynamic) {
        return splitModel(baseModel.getLocation().getPath() + "_split", baseModel, parts, dynamic);
    }

    protected ModelFile split(NongeneratedModel baseModel) { return split(baseModel, COLUMN_THREE, false); }

    protected ModelFile splitDynamic(NongeneratedModel baseModel, List<Vec3i> parts) { return split(baseModel, parts, true); }

    protected void addParticleTextureFrom(BlockModelBuilder result, ModelFile model) {
        String particles = generatedParticleTextures.get(model.getLocation());
        if (particles != null) {
            result.texture("particle", particles);
            generatedParticleTextures.put(result.getLocation(), particles);
        }
    }

    protected ConfiguredModel emptyWithParticles(String name, String particleTexture) {
        ModelFile model = models().withExistingParent(name, modLoc("block/ie_empty")).texture("particle", particleTexture);
        generatedParticleTextures.put(modLoc(name), particleTexture);
        return new ConfiguredModel(model);
    }

    public void assertModelExists(ResourceLocation name) {
        String suffix = name.getPath().contains(".") ? "" : ".json";
        Preconditions.checkState(existingFileHelper.exists(name, PackType.CLIENT_RESOURCES, suffix, "models"), "Model \"" + name + "\" does not exist");
    }

    protected IEOBJBuilder<BlockModelBuilder> ieObjBuilder(String loc) { return ieObjBuilder(getAutoNameIEOBJ(loc), modLoc(loc)); }

    protected IEOBJBuilder<BlockModelBuilder> ieObjBuilder(String name, ResourceLocation model) {
        return ieObjBuilder(name, model, models());
    }

    protected <T extends ModelBuilder<T>> IEOBJBuilder<T> ieObjBuilder(String loc, ModelProvider<T> modelProvider) {
        return ieObjBuilder(getAutoNameIEOBJ(loc), modLoc(loc), modelProvider);
    }

    private static String getAutoNameIEOBJ(String loc) {
        Preconditions.checkArgument(loc.endsWith(".obj.ie"));
        return loc.substring(0, loc.length() - 7);
    }

    protected <T extends ModelBuilder<T>> IEOBJBuilder<T> ieObjBuilder(String name, ResourceLocation model, ModelProvider<T> modelProvider) {
        final String particle = DataGenUtils.getTextureFromObj(model, existingFileHelper);
        generatedParticleTextures.put(modLoc(name), particle);
        return modelProvider.withExistingParent(name, mcLoc("block")).texture("particle", particle).customLoader(IEOBJBuilder::begin).modelLocation(addModelsPrefix(model));
    }

    protected <T extends ModelBuilder<T>> T mirror(NongeneratedModel inner, ModelProvider<T> provider) {
        String path = inner.getLocation().getPath() + "_mirrored";
        return provider.getBuilder(path).customLoader(MirroredModelBuilder::begin).inner(inner).end();
    }

    protected int getAngle(Direction dir, int offset) { return (int) ((dir.toYRot() + offset) % 360); }

    protected void createHorizontalRotatedBlock(Supplier<? extends Block> block, ModelFile model) { createHorizontalRotatedBlock(block, $ -> model, List.of()); }

    protected void createHorizontalRotatedBlock(Supplier<? extends Block> block, ModelFile model, int offsetRotY) {
        createRotatedBlock(block, $ -> model, IEProperties.FACING_HORIZONTAL, List.of(), 0, offsetRotY);
    }

    protected void createHorizontalRotatedBlock(Supplier<? extends Block> block, Function<PartialBlockstate, ModelFile> model, List<Property<?>> additionalProps) {
        createRotatedBlock(block, model, IEProperties.FACING_HORIZONTAL, additionalProps, 0, 180);
    }

    protected void createAllRotatedBlock(Supplier<? extends Block> block, ModelFile model) {
        createAllRotatedBlock(block, $ -> model, List.of());
    }

    protected void createAllRotatedBlock(Supplier<? extends Block> block, Function<PartialBlockstate, ModelFile> model, List<Property<?>> additionalProps) {
        createRotatedBlock(block, model, IEProperties.FACING_ALL, additionalProps, 90, 0);
    }

    protected void createRotatedBlock(Supplier<? extends Block> block, ModelFile model, Property<Direction> facing, List<Property<?>> additionalProps, int offsetRotX, int offsetRotY) {
        createRotatedBlock(block, $ -> model, facing, additionalProps, offsetRotX, offsetRotY);
    }

    protected void createRotatedBlock(Supplier<? extends Block> block, Function<PartialBlockstate, ModelFile> model, Property<Direction> facing, List<Property<?>> additionalProps, int offsetRotX, int offsetRotY) {
        VariantBlockStateBuilder stateBuilder = getVariantBuilder(block.get());
        forEachState(stateBuilder.partialState(), additionalProps, state -> {
            ModelFile modelLoc = model.apply(state);
            for (Direction d : facing.getPossibleValues()) {
                int x;
                int y;
                switch (d) {
                    case UP: {
                        x = 90;
                        y = 0;
                        break;
                    }
                    case DOWN: {
                        x = -90;
                        y = 0;
                        break;
                    }
                    default: {
                        y = getAngle(d, offsetRotY);
                        x = 0;
                    }
                }
                state.with(facing, d).setModels(new ConfiguredModel(modelLoc, x + offsetRotX, y, false));
            }
        });
    }

    protected static String getName(RenderStateShard state) {
        try {
            Field f = RenderStateShard.class.getDeclaredField("name");
            f.setAccessible(true);
            return (String) f.get(state);
        }
        catch (Exception e) { throw new RuntimeException(e); }
    }

    public static <T extends Comparable<T>> void forEach(PartialBlockstate base, Property<T> prop, List<Property<?>> remaining, Consumer<PartialBlockstate> out) {
        for (T value : prop.getPossibleValues()) {
            forEachState(base, remaining, map -> {
                map = map.with(prop, value);
                out.accept(map);
            });
        }
    }

    public static void forEachState(PartialBlockstate base, List<Property<?>> props, Consumer<PartialBlockstate> out) {
        if (!props.isEmpty()) {
            List<Property<?>> remaining = props.subList(1, props.size());
            Property<?> main = props.get(0);
            forEach(base, main, remaining, out);
        }
        else { out.accept(base); }
    }

    protected ResourceLocation addModelsPrefix(ResourceLocation in) {
        return ResourceLocation.fromNamespaceAndPath(in.getNamespace(), "models/" + in.getPath());
    }

    protected void setRenderType(@Nullable RenderType type, ModelBuilder<?>... builders) {
        if (type != null) {
            final String typeName = ModelProviderUtils.getName(type);
            for (final ModelBuilder<?> model : builders) { model.renderType(typeName); }
        }
    }
}

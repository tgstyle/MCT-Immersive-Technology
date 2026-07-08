package mctmods.immersivetechnology.client.models;

import blusunrize.immersiveengineering.api.utils.DirectionUtils;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import mctmods.immersivetechnology.client.models.util.ITModelUtils;
import mctmods.immersivetechnology.common.blocks.helper.ITIBlockInterfaces;
import mctmods.immersivetechnology.common.blocks.helper.ITEnums;
import mctmods.immersivetechnology.common.blocks.helper.ITProperties;
import mctmods.immersivetechnology.common.blocks.metal.BarrelSteelBlock;
import mctmods.immersivetechnology.core.lib.ITLib;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.RenderTypeGroup;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class ITModelConfigurableSides extends ITBakedModel {
    private static final HashMap<String, ITextureNamer> TYPES = new HashMap<>();
    private final LoadingCache<Map<Direction, ITEnums.IOSideConfig>, Map<Direction, BakedQuad>> modelCache;
    private final RenderTypeGroup renderTypes;

    final String name;

    public Map<Direction, Map<ITEnums.IOSideConfig, TextureAtlasSprite>> textures;

    static final ItemTransforms defaultTransforms;

    public ITModelConfigurableSides(String name, Map<Direction, Map<ITEnums.IOSideConfig, TextureAtlasSprite>> textures, RenderTypeGroup renderTypes) {
        this.modelCache = CacheBuilder.newBuilder().expireAfterAccess(60L, TimeUnit.SECONDS).build(CacheLoader.from((key) -> {
            Map<Direction, TextureAtlasSprite> tex = new EnumMap<>(Direction.class);
            for (Direction d : DirectionUtils.VALUES) {
                TextureAtlasSprite s = this.textures.get(d).get(key.get(d));
                if (s == null) { s = this.textures.get(d).get(ITEnums.IOSideConfig.NONE); }
                tex.put(d, s);
            }
            return bakeQuads(tex);
        }));
        this.name = name;
        this.textures = textures;
        this.renderTypes = renderTypes != null ? renderTypes : RenderTypeGroup.EMPTY;
    }

    @Override @NotNull public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData extraData, @Nullable RenderType renderType) {
        if (side == null) { return ImmutableList.of(); }
        Map<Direction, ITEnums.IOSideConfig> config = extraData.get(ITProperties.Model.SIDECONFIG);
        if (config == null && state != null && state.getBlock() instanceof BarrelSteelBlock) {
            ITEnums.IOSideConfig top = state.hasProperty(BarrelSteelBlock.TOP_CONFIG) ? state.getValue(BarrelSteelBlock.TOP_CONFIG) : ITEnums.IOSideConfig.INPUT;
            ITEnums.IOSideConfig bottom = state.hasProperty(BarrelSteelBlock.BOTTOM_CONFIG) ? state.getValue(BarrelSteelBlock.BOTTOM_CONFIG) : ITEnums.IOSideConfig.OUTPUT;
            config = new EnumMap<>(Direction.class);
            for (Direction d : DirectionUtils.VALUES) { config.put(d, ITEnums.IOSideConfig.NONE); }
            config.put(Direction.UP, top);
            config.put(Direction.DOWN, bottom);
        }
        if (config == null) { config = defaultConfig(); }
        Map<Direction, ITEnums.IOSideConfig> key = normalizeConfig(config);
        return ImmutableList.of(this.modelCache.getUnchecked(key).get(side));
    }

    private static Map<Direction, ITEnums.IOSideConfig> defaultConfig() {
        Map<Direction, ITEnums.IOSideConfig> config = new EnumMap<>(Direction.class);
        for (Direction d : DirectionUtils.VALUES) { config.put(d, ITEnums.IOSideConfig.NONE); }
        return config;
    }

    private static Map<Direction, ITEnums.IOSideConfig> normalizeConfig(Map<Direction, ITEnums.IOSideConfig> cfg) {
        EnumMap<Direction, ITEnums.IOSideConfig> normalized = new EnumMap<>(Direction.class);
        for (Direction d : DirectionUtils.VALUES) {
            normalized.put(d, cfg != null ? cfg.getOrDefault(d, ITEnums.IOSideConfig.NONE) : ITEnums.IOSideConfig.NONE);
        }
        return ImmutableMap.copyOf(normalized);
    }

    @Override @NotNull public ModelData getModelData(@NotNull BlockAndTintGetter world, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ModelData tileData) {
        ModelData.Builder data = super.getModelData(world, pos, state, tileData).derive();
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof ITIBlockInterfaces.IConfigurableSides confBE) {
            Map<Direction, ITEnums.IOSideConfig> conf = new EnumMap<>(Direction.class);
            for (Direction d : DirectionUtils.VALUES) { conf.put(d, confBE.getSideConfig(d)); }
            data.with(ITProperties.Model.SIDECONFIG, conf);
        } else if (state.hasProperty(BarrelSteelBlock.TOP_CONFIG) && state.hasProperty(BarrelSteelBlock.BOTTOM_CONFIG)) {
            Map<Direction, ITEnums.IOSideConfig> conf = new EnumMap<>(Direction.class);
            for (Direction d : DirectionUtils.VALUES) { conf.put(d, ITEnums.IOSideConfig.NONE); }
            conf.put(Direction.UP, state.getValue(BarrelSteelBlock.TOP_CONFIG));
            conf.put(Direction.DOWN, state.getValue(BarrelSteelBlock.BOTTOM_CONFIG));
            data.with(ITProperties.Model.SIDECONFIG, conf);
        }
        return data.build();
    }

    private static Map<Direction, BakedQuad> bakeQuads(Map<Direction, TextureAtlasSprite> sprites) {
        Map<Direction, BakedQuad> quads = new EnumMap<>(Direction.class);
        Vec3[] vertices = new Vec3[]{new Vec3(0.0F, 0.0F, 0.0F), new Vec3(0.0F, 0.0F, 1.0F), new Vec3(1.0F, 0.0F, 1.0F), new Vec3(1.0F, 0.0F, 0.0F)};
        addQuad(quads, sprites, Direction.DOWN, vertices, new double[]{0, 16, 16, 0});
        vertices = new Vec3[]{new Vec3(0.0F, 1.0F, 0.0F), new Vec3(0.0F, 1.0F, 1.0F), new Vec3(1.0F, 1.0F, 1.0F), new Vec3(1.0F, 1.0F, 0.0F)};
        addQuad(quads, sprites, Direction.UP, vertices, new double[]{0, 0, 16, 16});
        vertices = new Vec3[]{new Vec3(1.0F, 0.0F, 0.0F), new Vec3(1.0F, 1.0F, 0.0F), new Vec3(0.0F, 1.0F, 0.0F), new Vec3(0.0F, 0.0F, 0.0F)};
        addQuad(quads, sprites, Direction.NORTH, vertices, new double[]{0, 16, 16, 0});
        vertices = new Vec3[]{new Vec3(1.0F, 0.0F, 1.0F), new Vec3(1.0F, 1.0F, 1.0F), new Vec3(0.0F, 1.0F, 1.0F), new Vec3(0.0F, 0.0F, 1.0F)};
        addQuad(quads, sprites, Direction.SOUTH, vertices, new double[]{16, 16, 0, 0});
        vertices = new Vec3[]{new Vec3(0.0F, 0.0F, 0.0F), new Vec3(0.0F, 1.0F, 0.0F), new Vec3(0.0F, 1.0F, 1.0F), new Vec3(0.0F, 0.0F, 1.0F)};
        addQuad(quads, sprites, Direction.WEST, vertices, new double[]{0, 16, 16, 0});
        vertices = new Vec3[]{new Vec3(1.0F, 0.0F, 0.0F), new Vec3(1.0F, 1.0F, 0.0F), new Vec3(1.0F, 1.0F, 1.0F), new Vec3(1.0F, 0.0F, 1.0F)};
        addQuad(quads, sprites, Direction.EAST, vertices, new double[]{16, 16, 0, 0});
        return quads;
    }

    private static void addQuad(Map<Direction, BakedQuad> out, Map<Direction, TextureAtlasSprite> sprites, Direction side, Vec3[] vertices, double[] uv) {
        TextureAtlasSprite sprite = sprites.get(side);
        if (sprite == null) { return; }
        out.put(side, ITModelUtils.createBakedQuad(vertices, side, sprite, uv, new float[]{1.0F, 1.0F, 1.0F, 1.0F}, side.getAxisDirection() == Direction.AxisDirection.NEGATIVE));
    }

    @Override @NotNull public TextureAtlasSprite getParticleIcon(@NotNull ModelData data) { return this.textures.get(Direction.DOWN).get(ITEnums.IOSideConfig.NONE); }

    @NotNull public ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand, @NotNull ModelData data) { return ChunkRenderTypeSet.of(this.renderTypes.block()); }

    @NotNull public List<RenderType> getRenderTypes(@NotNull ItemStack itemStack, boolean fabulous) { return List.of(fabulous ? this.renderTypes.entityFabulous() : this.renderTypes.entity()); }

    @Override @NotNull public BakedModel applyTransform(@NotNull ItemDisplayContext transformType, @NotNull PoseStack poseStack, boolean applyLeftHandTransform) {
        defaultTransforms.getTransform(transformType).apply(applyLeftHandTransform, poseStack);
        return this;
    }

    static {
        for (Type type : Type.values()) { TYPES.put(type.getName(), type.nameMapper); }
        defaultTransforms = new ItemTransforms(
                new ItemTransform(new Vector3f(75.0F, 45.0F, 0.0F), new Vector3f(0.0F, 0.25F, 0.0F), new Vector3f(0.375F, 0.375F, 0.375F)),
                new ItemTransform(new Vector3f(75.0F, 45.0F, 0.0F), new Vector3f(0.0F, 0.15625F, 0.0F), new Vector3f(0.375F, 0.375F, 0.375F)),
                new ItemTransform(new Vector3f(0.0F, 45.0F, 0.0F), new Vector3f(0.0F, 0.0F, 0.0F), new Vector3f(0.4F, 0.4F, 0.4F)),
                new ItemTransform(new Vector3f(0.0F, 225.0F, 0.0F), new Vector3f(0.0F, 0.0F, 0.0F), new Vector3f(0.4F, 0.4F, 0.4F)),
                new ItemTransform(new Vector3f(0.0F, 0.0F, 0.0F), new Vector3f(0.0F, 0.0F, 0.0F), new Vector3f(1.0F, 1.0F, 1.0F)),
                new ItemTransform(new Vector3f(30.0F, 225.0F, 0.0F), new Vector3f(0.0F, 0.0F, 0.0F), new Vector3f(0.625F, 0.625F, 0.625F)),
                new ItemTransform(new Vector3f(0.0F, 0.0F, 0.0F), new Vector3f(0.0F, 0.1875F, 0.0F), new Vector3f(0.25F, 0.25F, 0.25F)),
                new ItemTransform(new Vector3f(0.0F, 0.0F, 0.0F), new Vector3f(0.0F, 0.0F, 0.0F), new Vector3f(0.5F, 0.5F, 0.5F)),
                ImmutableMap.of()
        );
    }

    public enum Type {
        VERTICAL(new ITextureNamer() {
            public String nameFromSide(Direction side, ITEnums.IOSideConfig cfg) { return side.getAxis() == Direction.Axis.Y ? cfg.getTextureName() : "side"; }
            public String nameFromCfg(Direction side, ITEnums.IOSideConfig cfg) { return null; }
        });
        private final ITextureNamer nameMapper;
        Type(ITextureNamer nameMapper) { this.nameMapper = nameMapper; }
        public String getName() { return this.name().toLowerCase(Locale.US); }
    }

    public static class Loader implements IGeometryLoader<ConfigSidesModelBase> {
        public static final ResourceLocation NAME = ITLib.rl("conf_sides");
        public static final Loader INSTANCE = new Loader();
        private Loader() {}
        @NotNull public ConfigSidesModelBase read(JsonObject modelContents, @NotNull JsonDeserializationContext deserializationContext) {
            String name = modelContents.has("base_name") ? modelContents.get("base_name").getAsString() : "unknown";
            String type = modelContents.get("type").getAsString();
            Map<String, Material> texMap = new HashMap<>();
            if (modelContents.has("textures")) {
                JsonObject t = modelContents.getAsJsonObject("textures");
                t.entrySet().forEach(e -> {
                    String key = e.getKey();
                    String path = e.getValue().getAsString();
                    texMap.put(key, new Material(InventoryMenu.BLOCK_ATLAS, ResourceLocation.parse(path)));
                });
            }
            if (modelContents.has("base_name")) {
                String base = modelContents.get("base_name").getAsString();
                ITextureNamer namer = TYPES.get(type);
                for (Direction f : DirectionUtils.VALUES) {
                    for (ITEnums.IOSideConfig cfg : ITEnums.IOSideConfig.values()) {
                        String key = namer.getTextureName(f, cfg);
                        if (!texMap.containsKey(key)) {
                            String texPath = base + "_" + key;
                            texMap.put(key, new Material(InventoryMenu.BLOCK_ATLAS, ResourceLocation.parse(texPath)));
                        }
                    }
                }
            }
            return new ConfigSidesModelBase(name, type, ImmutableMap.copyOf(texMap));
        }
    }

    public record ConfigSidesModelBase(String name, String type, Map<String, Material> textures) implements IUnbakedGeometry<ConfigSidesModelBase> {
        public @NotNull BakedModel bake(@NotNull IGeometryBakingContext owner, @NotNull ModelBaker bakery, @NotNull Function<Material, TextureAtlasSprite> spriteGetter, @NotNull ModelState modelTransform, @NotNull ItemOverrides overrides) {
            Map<Direction, Map<ITEnums.IOSideConfig, TextureAtlasSprite>> tex = new EnumMap<>(Direction.class);
            for (Direction f : DirectionUtils.VALUES) {
                Map<ITEnums.IOSideConfig, TextureAtlasSprite> forSide = new EnumMap<>(ITEnums.IOSideConfig.class);
                ITextureNamer namer = TYPES.get(this.type);
                if (namer == null) { namer = new ITextureNamer() {}; }
                for (ITEnums.IOSideConfig cfg : ITEnums.IOSideConfig.values()) {
                    String key = namer.getTextureName(f, cfg);
                    Material rl = this.textures.get(key);
                    if (rl == null) { rl = owner.getMaterial(key); }
                    TextureAtlasSprite sprite = spriteGetter.apply(rl);
                    forSide.put(cfg, sprite);
                }
                tex.put(f, forSide);
            }
            ResourceLocation renderTypeName = Objects.requireNonNullElseGet(owner.getRenderTypeHint(), () -> ResourceLocation.withDefaultNamespace("solid"));
            RenderTypeGroup rtg = owner.getRenderType(renderTypeName);
            return new ITModelConfigurableSides(this.name, tex, rtg);
        }
    }

    interface ITextureNamer {
        default String getTextureName(Direction side, ITEnums.IOSideConfig cfg) {
            String s = nameFromSide(side, cfg);
            String c = nameFromCfg(side, cfg);
            if (s != null && c != null) { return s + "_" + c; }
            return Objects.requireNonNullElseGet(s, () -> c != null ? c : "");
        }
        default String nameFromSide(Direction side, ITEnums.IOSideConfig cfg) { return side.getSerializedName(); }
        default String nameFromCfg(Direction side, ITEnums.IOSideConfig cfg) { return cfg.getTextureName(); }
    }
}

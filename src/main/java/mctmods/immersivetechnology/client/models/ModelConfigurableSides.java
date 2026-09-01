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
import com.immersiveconvergence.api.client.BakedQuadUtils;
import com.immersiveconvergence.api.block.BlockInterfaces;
import com.immersiveconvergence.api.block.Enums;
import com.immersiveconvergence.api.block.ModProperties;
import mctmods.immersivetechnology.core.lib.Reference;
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
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.RenderTypeGroup;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;
import org.joml.Vector3f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class ModelConfigurableSides extends ModBakedModel {
    private static final HashMap<String, ITextureNamer> TYPES = new HashMap<>();
    private static final Map<Direction, Enums.IOSideConfig> DEFAULT_KEY = defaultConfig();
    private final LoadingCache<Map<Direction, Enums.IOSideConfig>, Map<Direction, BakedQuad>> modelCache;
    final String name;
    public Map<Direction, Map<Enums.IOSideConfig, TextureAtlasSprite>> textures;
    private final RenderTypeGroup renderTypes;
    static final ItemTransforms defaultTransforms;

    public ModelConfigurableSides(String name, Map<Direction, Map<Enums.IOSideConfig, TextureAtlasSprite>> textures, RenderTypeGroup renderTypes) {
        this.modelCache = CacheBuilder.newBuilder().expireAfterAccess(60L, TimeUnit.SECONDS).build(CacheLoader.from((key) -> {
            Map<Direction, TextureAtlasSprite> tex = new EnumMap<>(Direction.class);
            for (Direction d : DirectionUtils.VALUES) { tex.put(d, this.textures.get(d).get(key.get(d))); }
            return bakeQuads(tex);
        }));
        this.name = name;
        this.textures = textures;
        this.renderTypes = renderTypes;
    }

    @Override @Nonnull public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull RandomSource rand, @Nonnull ModelData extraData, @Nullable RenderType layer) {
        if (side == null) { return ImmutableList.of(); }
        Map<Direction, Enums.IOSideConfig> config = extraData.get(ModProperties.Model.SIDECONFIG);
        if (config == null) { config = DEFAULT_KEY; }
        return ImmutableList.of(this.modelCache.getUnchecked(config).get(side));
    }

    private static Map<Direction, Enums.IOSideConfig> defaultConfig() {
        Map<Direction, Enums.IOSideConfig> config = new EnumMap<>(Direction.class);
        for (Direction d : DirectionUtils.VALUES) { config.put(d, Enums.IOSideConfig.NONE); }
        return ImmutableMap.copyOf(config);
    }

    @Override @Nonnull public ModelData getModelData(@Nonnull BlockAndTintGetter world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull ModelData tileData) {
        ModelData.Builder data = super.getModelData(world, pos, state, tileData).derive();
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof BlockInterfaces.IConfigurableSides confBE) {
            Map<Direction, Enums.IOSideConfig> conf = new EnumMap<>(Direction.class);
            for (Direction d : DirectionUtils.VALUES) { conf.put(d, confBE.getSideConfig(d)); }
            data.with(ModProperties.Model.SIDECONFIG, conf);
        }
        return data.build();
    }

    private static Map<Direction, BakedQuad> bakeQuads(Map<Direction, TextureAtlasSprite> sprites) {
        Map<Direction, BakedQuad> quads = new EnumMap<>(Direction.class);
        Vec3[] vertices = new Vec3[]{new Vec3(0.0F, 0.0F, 0.0F), new Vec3(0.0F, 0.0F, 1.0F), new Vec3(1.0F, 0.0F, 1.0F), new Vec3(1.0F, 0.0F, 0.0F)};
        addQuad(quads, sprites, Direction.DOWN, vertices, new double[]{0.0F, 16.0F, 16.0F, 0.0F});
        vertices = new Vec3[]{new Vec3(0.0F, 1.0F, 0.0F), new Vec3(0.0F, 1.0F, 1.0F), new Vec3(1.0F, 1.0F, 1.0F), new Vec3(1.0F, 1.0F, 0.0F)};
        addQuad(quads, sprites, Direction.UP, vertices, new double[]{0.0F, 0.0F, 16.0F, 16.0F});
        vertices = new Vec3[]{new Vec3(1.0F, 0.0F, 0.0F), new Vec3(1.0F, 1.0F, 0.0F), new Vec3(0.0F, 1.0F, 0.0F), new Vec3(0.0F, 0.0F, 0.0F)};
        addQuad(quads, sprites, Direction.NORTH, vertices, new double[]{0.0F, 16.0F, 16.0F, 0.0F});
        vertices = new Vec3[]{new Vec3(1.0F, 0.0F, 1.0F), new Vec3(1.0F, 1.0F, 1.0F), new Vec3(0.0F, 1.0F, 1.0F), new Vec3(0.0F, 0.0F, 1.0F)};
        addQuad(quads, sprites, Direction.SOUTH, vertices, new double[]{16.0F, 16.0F, 0.0F, 0.0F});
        vertices = new Vec3[]{new Vec3(0.0F, 0.0F, 0.0F), new Vec3(0.0F, 1.0F, 0.0F), new Vec3(0.0F, 1.0F, 1.0F), new Vec3(0.0F, 0.0F, 1.0F)};
        addQuad(quads, sprites, Direction.WEST, vertices, new double[]{0.0F, 16.0F, 16.0F, 0.0F});
        vertices = new Vec3[]{new Vec3(1.0F, 0.0F, 0.0F), new Vec3(1.0F, 1.0F, 0.0F), new Vec3(1.0F, 1.0F, 1.0F), new Vec3(1.0F, 0.0F, 1.0F)};
        addQuad(quads, sprites, Direction.EAST, vertices, new double[]{16.0F, 16.0F, 0.0F, 0.0F});
        return quads;
    }

    private static void addQuad(Map<Direction, BakedQuad> out, Map<Direction, TextureAtlasSprite> sprites, Direction side, Vec3[] vertices, double[] uv) {
        out.put(side, BakedQuadUtils.createBakedQuad(vertices, side, sprites.get(side), uv, new float[]{1.0F, 1.0F, 1.0F, 1.0F}, side.getAxisDirection() == Direction.AxisDirection.NEGATIVE));
    }

    @Override @Nonnull public TextureAtlasSprite getParticleIcon(@Nonnull ModelData data) { return this.textures.get(Direction.DOWN).get(Enums.IOSideConfig.NONE); }

    @Override @Nonnull public ItemOverrides getOverrides() { return ItemOverrides.EMPTY; }

    @Nonnull public ChunkRenderTypeSet getRenderTypes(@Nonnull BlockState state, @Nonnull RandomSource rand, @Nonnull ModelData data) { return ChunkRenderTypeSet.of(this.renderTypes.block()); }

    @Nonnull public List<RenderType> getRenderTypes(@Nonnull ItemStack itemStack, boolean fabulous) { return List.of(fabulous ? this.renderTypes.entityFabulous() : this.renderTypes.entity()); }

    @Override @Nonnull public BakedModel applyTransform(@Nonnull ItemDisplayContext transformType, @Nonnull PoseStack poseStack, boolean applyLeftHandTransform) {
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
        SIDE_TOP_BOTTOM(new ITextureNamer() { public String nameFromSide(Direction side, Enums.IOSideConfig cfg) { return side.getAxis() == Direction.Axis.Y ? side.getSerializedName() : "side"; } }),
        SIDE_VERTICAL(new ITextureNamer() { public String nameFromSide(Direction side, Enums.IOSideConfig cfg) { return side.getAxis() == Direction.Axis.Y ? "up" : "side"; } }),
        VERTICAL(new ITextureNamer() {
            public String nameFromSide(Direction side, Enums.IOSideConfig cfg) { return side.getAxis() == Direction.Axis.Y ? "up" : "side"; }
            public String nameFromCfg(Direction side, Enums.IOSideConfig cfg) { return side.getAxis() == Direction.Axis.Y ? cfg.getTextureName() : null; }
        }),
        ALL_SAME_TEXTURE(new ITextureNamer() { public String nameFromSide(Direction side, Enums.IOSideConfig cfg) { return "side"; } });

        private final ITextureNamer nameMapper;

        Type(ITextureNamer nameMapper) { this.nameMapper = nameMapper; }

        public String getName() { return this.name().toLowerCase(Locale.US); }
    }

    public static class Loader implements IGeometryLoader<ConfigSidesModelBase> {
        public static ResourceLocation NAME = Reference.rl("conf_sides");

        @Nonnull public ConfigSidesModelBase read(JsonObject modelContents, @Nonnull JsonDeserializationContext deserializationContext) {
            String name = modelContents.get("base_name").getAsString();
            String type = modelContents.get("type").getAsString();
            ImmutableMap.Builder<String, Material> builder = ImmutableMap.builder();
            ITextureNamer namer = TYPES.get(type);
            for (Direction f : DirectionUtils.VALUES) for (Enums.IOSideConfig cfg : Enums.IOSideConfig.values()) {
                String key = f.getSerializedName() + "_" + cfg.getTextureName();
                String tex = name + "_" + namer.getTextureName(f, cfg);
                builder.put(key, new Material(InventoryMenu.BLOCK_ATLAS, ResourceLocation.parse(tex)));
            }
            return new ConfigSidesModelBase(name, type, builder.build());
        }
    }

    public record ConfigSidesModelBase(String name, String type, Map<String, Material> textures) implements IUnbakedGeometry<ConfigSidesModelBase> {
        public BakedModel bake(IGeometryBakingContext owner, ModelBaker bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform, ItemOverrides overrides, ResourceLocation modelLocation) {
            Map<Direction, Map<Enums.IOSideConfig, TextureAtlasSprite>> tex = new EnumMap<>(Direction.class);
            for (Direction f : DirectionUtils.VALUES) {
                Map<Enums.IOSideConfig, TextureAtlasSprite> forSide = new EnumMap<>(Enums.IOSideConfig.class);
                for (Enums.IOSideConfig cfg : Enums.IOSideConfig.values()) {
                    Material rl = this.textures.get(f.getSerializedName() + "_" + cfg.getTextureName());
                    if (rl != null) { forSide.put(cfg, spriteGetter.apply(rl)); }
                }
                tex.put(f, forSide);
            }
            ResourceLocation renderTypeName = Objects.requireNonNullElseGet(owner.getRenderTypeHint(), () -> ResourceLocation.withDefaultNamespace("solid"));
            return new ModelConfigurableSides(this.name, tex, owner.getRenderType(renderTypeName));
        }
    }

    interface ITextureNamer {
        default String getTextureName(Direction side, Enums.IOSideConfig cfg) {
            String s = this.nameFromSide(side, cfg);
            String c = this.nameFromCfg(side, cfg);
            if (s != null && c != null) { return s + "_" + c; }
            else { return Objects.requireNonNullElseGet(s, () -> c != null ? c : ""); }
        }

        default String nameFromSide(Direction side, Enums.IOSideConfig cfg) { return side.getSerializedName(); }

        default String nameFromCfg(Direction side, Enums.IOSideConfig cfg) { return cfg.getTextureName(); }
    }
}

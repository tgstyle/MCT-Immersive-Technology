package mctmods.immersivetechnology.client.models.helper;

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
import mctmods.immersivetechnology.common.blocks.helper.ITBlockInterfaces;
import mctmods.immersivetechnology.common.blocks.helper.ITEnums;
import mctmods.immersivetechnology.common.blocks.helper.ITProperties;
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
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.RenderTypeGroup;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class ITModelConfigurableSides extends ITBakedModel {
    private static final HashMap<String, ITModelConfigurableSides.ITextureNamer> TYPES = new HashMap<>();
    private final LoadingCache<Map<Direction, ITEnums.IOSideConfig>, Map<Direction, BakedQuad>> modelCache;
    final String name;
    public Map<Direction, Map<ITEnums.IOSideConfig, TextureAtlasSprite>> textures;
    private final RenderTypeGroup renderTypes;
    static final ItemTransforms defaultTransforms;

    public ITModelConfigurableSides(String name, Map<Direction, Map<ITEnums.IOSideConfig, TextureAtlasSprite>> textures, RenderTypeGroup renderTypes) {
        this.modelCache = CacheBuilder.newBuilder().expireAfterAccess(60L, TimeUnit.SECONDS).build(CacheLoader.from((key) -> {
            Map<Direction, TextureAtlasSprite> tex = new EnumMap<>(Direction.class);
            for (Direction d : DirectionUtils.VALUES) {
                assert key != null;
                tex.put(d, this.textures.get(d).get(key.get(d)));
            }
            return bakeQuads(tex);
        }));
        this.name = name;
        this.textures = textures;
        this.renderTypes = renderTypes;
    }

    @Nonnull
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull RandomSource rand, @Nonnull ModelData extraData, @Nullable RenderType layer) {
        if (side == null) { return ImmutableList.of(); }
        Map<Direction, ITEnums.IOSideConfig> config;
        if (extraData.has(ITProperties.Model.SIDECONFIG)) { config = extraData.get(ITProperties.Model.SIDECONFIG); }
        else {
            config = new EnumMap<>(Direction.class);
            for (Direction d : DirectionUtils.VALUES) { config.put(d, ITEnums.IOSideConfig.NONE); }
        }
        assert config != null;
        return ImmutableList.of(this.modelCache.getUnchecked(config).get(side));
    }

    @Nonnull
    public ModelData getModelData(@Nonnull BlockAndTintGetter world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull ModelData tileData) {
        ModelData.Builder data = super.getModelData(world, pos, state, tileData).derive();
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof ITBlockInterfaces.IConfigurableSides confBE) {
            Map<Direction, ITEnums.IOSideConfig> conf = new EnumMap<>(Direction.class);
            for (Direction d : DirectionUtils.VALUES) { conf.put(d, confBE.getSideConfig(d)); }
            data.with(ITProperties.Model.SIDECONFIG, conf);
        }
        return data.build();
    }

    private static Map<Direction, BakedQuad> bakeQuads(Map<Direction, TextureAtlasSprite> sprites) {
        Map<Direction, BakedQuad> quads = new EnumMap<>(Direction.class);
        Vec3[] vertices = new Vec3[] { new Vec3(0.0F, 0.0F, 0.0F), new Vec3(0.0F, 0.0F, 1.0F), new Vec3(1.0F, 0.0F, 1.0F), new Vec3(1.0F, 0.0F, 0.0F) };
        addQuad(quads, sprites, Direction.DOWN, vertices, new double[] { 0.0F, 16.0F, 16.0F, 0.0F });
        vertices = new Vec3[] { new Vec3(0.0F, 1.0F, 0.0F), new Vec3(0.0F, 1.0F, 1.0F), new Vec3(1.0F, 1.0F, 1.0F), new Vec3(1.0F, 1.0F, 0.0F) };
        addQuad(quads, sprites, Direction.UP, vertices, new double[] { 0.0F, 0.0F, 16.0F, 16.0F });
        vertices = new Vec3[] { new Vec3(1.0F, 0.0F, 0.0F), new Vec3(1.0F, 1.0F, 0.0F), new Vec3(0.0F, 1.0F, 0.0F), new Vec3(0.0F, 0.0F, 0.0F) };
        addQuad(quads, sprites, Direction.NORTH, vertices, new double[] { 0.0F, 16.0F, 16.0F, 0.0F });
        vertices = new Vec3[] { new Vec3(1.0F, 0.0F, 1.0F), new Vec3(1.0F, 1.0F, 1.0F), new Vec3(0.0F, 1.0F, 1.0F), new Vec3(0.0F, 0.0F, 1.0F) };
        addQuad(quads, sprites, Direction.SOUTH, vertices, new double[] { 16.0F, 16.0F, 0.0F, 0.0F });
        vertices = new Vec3[] { new Vec3(0.0F, 0.0F, 0.0F), new Vec3(0.0F, 1.0F, 0.0F), new Vec3(0.0F, 1.0F, 1.0F), new Vec3(0.0F, 0.0F, 1.0F) };
        addQuad(quads, sprites, Direction.WEST, vertices, new double[] { 0.0F, 16.0F, 16.0F, 0.0F });
        vertices = new Vec3[] { new Vec3(1.0F, 0.0F, 0.0F), new Vec3(1.0F, 1.0F, 0.0F), new Vec3(1.0F, 1.0F, 1.0F), new Vec3(1.0F, 0.0F, 1.0F) };
        addQuad(quads, sprites, Direction.EAST, vertices, new double[] { 16.0F, 16.0F, 0.0F, 0.0F });
        return quads;
    }

    private static void addQuad(Map<Direction, BakedQuad> out, Map<Direction, TextureAtlasSprite> sprites, Direction side, Vec3[] vertices, double[] uv) {
        out.put(side, ITModelUtils.createBakedQuad(vertices, side, sprites.get(side), uv, new float[] { 1.0F, 1.0F, 1.0F, 1.0F }, side.getAxisDirection() == Direction.AxisDirection.NEGATIVE));
    }

    @Override
    @NotNull
    public TextureAtlasSprite getParticleIcon(@NotNull ModelData data) { return this.textures.get(Direction.DOWN).get(ITEnums.IOSideConfig.NONE); }

    @Nonnull
    public ItemOverrides getOverrides() { return ItemOverrides.EMPTY; }

    public @NotNull ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand, @NotNull ModelData data) { return ChunkRenderTypeSet.of(this.renderTypes.block()); }

    public @NotNull List<RenderType> getRenderTypes(@NotNull ItemStack itemStack, boolean fabulous) { return List.of(fabulous ? this.renderTypes.entityFabulous() : this.renderTypes.entity()); }

    @Override
    public @NotNull BakedModel applyTransform(@NotNull ItemDisplayContext transformType, @NotNull PoseStack poseStack, boolean applyLeftHandTransform) {
        defaultTransforms.getTransform(transformType).apply(applyLeftHandTransform, poseStack);
        return this;
    }

    static {
        for (ITModelConfigurableSides.Type type : ITModelConfigurableSides.Type.values()) { TYPES.put(type.getName(), type.nameMapper); }
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
        SIDE_TOP_BOTTOM(new ITModelConfigurableSides.ITextureNamer() {
            public String nameFromSide(Direction side, ITEnums.IOSideConfig cfg) { return side.getAxis() == Direction.Axis.Y ? side.getSerializedName() : "side"; }
        }),
        SIDE_VERTICAL(new ITModelConfigurableSides.ITextureNamer() {
            public String nameFromSide(Direction side, ITEnums.IOSideConfig cfg) { return side.getAxis() == Direction.Axis.Y ? "up" : "side"; }
        }),
        VERTICAL(new ITModelConfigurableSides.ITextureNamer() {
            public String nameFromSide(Direction side, ITEnums.IOSideConfig cfg) { return side.getAxis() == Direction.Axis.Y ? "up" : "side"; }
            public String nameFromCfg(Direction side, ITEnums.IOSideConfig cfg) { return side.getAxis() == Direction.Axis.Y ? cfg.getTextureName() : null; }
        }),
        ALL_SAME_TEXTURE(new ITModelConfigurableSides.ITextureNamer() {
            public String nameFromSide(Direction side, ITEnums.IOSideConfig cfg) { return "side"; }
        });

        private final ITModelConfigurableSides.ITextureNamer nameMapper;

        Type(ITModelConfigurableSides.ITextureNamer nameMapper) { this.nameMapper = nameMapper; }

        public String getName() { return this.name().toLowerCase(Locale.US); }
    }

    public static class Loader implements IGeometryLoader<ITModelConfigurableSides.ConfigSidesModelBase> {
        public static ResourceLocation NAME = ITLib.rl("conf_sides");

        @Nonnull
        public ITModelConfigurableSides.ConfigSidesModelBase read(JsonObject modelContents, @Nonnull JsonDeserializationContext deserializationContext) {
            String name = modelContents.get("base_name").getAsString();
            String type = modelContents.get("type").getAsString();
            ImmutableMap.Builder<String, Material> builder = ImmutableMap.builder();
            ITModelConfigurableSides.ITextureNamer namer = ITModelConfigurableSides.TYPES.get(type);
            for (Direction f : DirectionUtils.VALUES) for (ITEnums.IOSideConfig cfg : ITEnums.IOSideConfig.values()) {
                String key = f.getSerializedName() + "_" + cfg.getTextureName();
                String tex = name + "_" + namer.getTextureName(f, cfg);
                builder.put(key, new Material(InventoryMenu.BLOCK_ATLAS, ResourceLocation.parse(tex)));
            }
            return new ITModelConfigurableSides.ConfigSidesModelBase(name, type, builder.build());
        }
    }

    public record ConfigSidesModelBase(String name, String type, Map<String, Material> textures) implements IUnbakedGeometry<ITModelConfigurableSides.ConfigSidesModelBase> {
        public BakedModel bake(IGeometryBakingContext owner, ModelBaker bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform, ItemOverrides overrides, ResourceLocation modelLocation) {
            Map<Direction, Map<ITEnums.IOSideConfig, TextureAtlasSprite>> tex = new EnumMap<>(Direction.class);
            for (Direction f : DirectionUtils.VALUES) {
                Map<ITEnums.IOSideConfig, TextureAtlasSprite> forSide = new EnumMap<>(ITEnums.IOSideConfig.class);
                for (ITEnums.IOSideConfig cfg : ITEnums.IOSideConfig.values()) {
                    Material rl = this.textures.get(f.getSerializedName() + "_" + cfg.getTextureName());
                    if (rl != null) { forSide.put(cfg, spriteGetter.apply(rl)); }
                }
                tex.put(f, forSide);
            }
            ResourceLocation renderTypeName = Objects.requireNonNullElseGet(owner.getRenderTypeHint(), () -> ResourceLocation.withDefaultNamespace("solid"));
            return new ITModelConfigurableSides(this.name, tex, owner.getRenderType(renderTypeName));
        }
    }

    interface ITextureNamer {
        default String getTextureName(Direction side, ITEnums.IOSideConfig cfg) {
            String s = this.nameFromSide(side, cfg);
            String c = this.nameFromCfg(side, cfg);
            if (s != null && c != null) { return s + "_" + c; }
            else { return Objects.requireNonNullElseGet(s, () -> c != null ? c : ""); }
        }

        default String nameFromSide(Direction side, ITEnums.IOSideConfig cfg) { return side.getSerializedName(); }

        default String nameFromCfg(Direction side, ITEnums.IOSideConfig cfg) { return cfg.getTextureName(); }
    }
}

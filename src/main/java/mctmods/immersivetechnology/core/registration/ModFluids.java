package mctmods.immersivetechnology.core.registration;

import com.google.common.collect.ImmutableList;
import mctmods.immersivetechnology.common.fluids.ModFluid;
import mctmods.immersivetechnology.common.fluids.FluidBlock;
import mctmods.immersivetechnology.core.lib.Reference;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static mctmods.immersivetechnology.core.lib.Reference.rl;

public class ModFluids {
    public static final DeferredRegister<Fluid> REGISTER = DeferredRegister.create(BuiltInRegistries.FLUID, Reference.MODID);
    public static final DeferredRegister<FluidType> TYPE_REGISTER = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, Reference.MODID);
    public static final List<ModFluids.FluidEntry> ALL_ENTRIES = new ArrayList<>();
    public static final Set<ModBlocks.BlockEntry<? extends LiquidBlock>> ALL_FLUID_BLOCKS = new HashSet<>();
    private static final HashMap<String, Supplier<? extends Fluid>> FLUID_REGISTRY_MAP = new HashMap<>();
    public static Function<String, Fluid> getFluid = (key) -> FLUID_REGISTRY_MAP.get(key).get();

    public static final ModFluids.FluidEntry CHLORINE = FluidEntry.make(
            "chlorine", 0, rl("block/fluid/fluid_gas_still"), rl("block/fluid/fluid_gas_flowing"),
            props -> props.density(-100).viscosity(1000), 0xFFC0E67B
    );

    public static final ModFluids.FluidEntry EXHAUST_STEAM = FluidEntry.make(
            "exhaust_steam", 0, rl("block/fluid/fluid_gas_still"), rl("block/fluid/fluid_gas_flowing"),
            props -> props.density(-100).viscosity(500).temperature(500), 0xFFC1C1C5
    );

    public static final ModFluids.FluidEntry GRAVEL_SLURRY = FluidEntry.make(
            "gravel_slurry", 0, rl("block/fluid/fluid_still"), rl("block/fluid/fluid_flowing"),
            props -> props.density(1000).viscosity(1000), 0xFF6D6565
    );

    public static final ModFluids.FluidEntry DISTILLED_WATER = FluidEntry.make(
            "distilled_water", 0, rl("block/fluid/fluid_still"), rl("block/fluid/fluid_flowing"),
            props -> props.density(1000).viscosity(1000), 0xFF7079E0
    );

    public static final ModFluids.FluidEntry FLUE_GAS = FluidEntry.make(
            "flue_gas", 0, rl("block/fluid/fluid_gas_still"), rl("block/fluid/fluid_gas_flowing"),
            props -> props.density(-100).viscosity(500), 0xFFFFFFFF
    );

    public static final ModFluids.FluidEntry HEATED_GRAVEL = FluidEntry.make(
            "heated_gravel_slurry", 0, rl("block/fluid/molten_still"), rl("block/fluid/molten_flowing"),
            props -> props.density(930).viscosity(10000).temperature(400), 0xFFA8A6A6
    );

    public static final ModFluids.FluidEntry HEATED_SALT = FluidEntry.make(
            "heated_salt_slurry", 0, rl("block/fluid/molten_still"), rl("block/fluid/molten_flowing"),
            props -> props.density(930).viscosity(10000).temperature(400), 0xFFC2C2C2
    );

    public static final ModFluids.FluidEntry HOT_WATER = FluidEntry.make(
            "hot_water", 0, rl("block/fluid/fluid_still"), rl("block/fluid/fluid_flowing"),
            props -> props.density(1000).viscosity(1000).temperature(400), 0xFF0DFFFF
    );

    public static final ModFluids.FluidEntry HYDROGEN = FluidEntry.make(
            "hydrogen", 0, rl("block/fluid/fluid_gas_still"), rl("block/fluid/fluid_gas_flowing"),
            props -> props.density(-100).viscosity(1000), 0xFF00BFFF
    );

    public static final ModFluids.FluidEntry MOLTEN_SALT = FluidEntry.make(
            "molten_salt", 0, rl("block/fluid/molten_still"), rl("block/fluid/molten_flowing"),
            props -> props.density(1000).viscosity(10000).temperature(1000), 0xFFAEA0A2
    );

    public static final ModFluids.FluidEntry SALT_SLURRY = FluidEntry.make(
            "salt_slurry", 0, rl("block/fluid/fluid_still"), rl("block/fluid/fluid_flowing"),
            props -> props.density(1000).viscosity(1000), 0xFFBB6528
    );

    public static final ModFluids.FluidEntry STEAM = FluidEntry.make(
            "steam", 0, rl("block/fluid/fluid_gas_still"), rl("block/fluid/fluid_gas_flowing"),
            props -> props.density(-100).viscosity(500).temperature(1000), 0xFF3E444F
    );

    static {
        FLUID_REGISTRY_MAP.put("chlorine", CHLORINE.getStillGetter());
        FLUID_REGISTRY_MAP.put("distilled_water", DISTILLED_WATER.getStillGetter());
        FLUID_REGISTRY_MAP.put("exhaust_steam", EXHAUST_STEAM.getStillGetter());
        FLUID_REGISTRY_MAP.put("flue_gas", FLUE_GAS.getStillGetter());
        FLUID_REGISTRY_MAP.put("gravel_slurry", GRAVEL_SLURRY.getStillGetter());
        FLUID_REGISTRY_MAP.put("heated_salt_slurry", HEATED_SALT.getStillGetter());
        FLUID_REGISTRY_MAP.put("hot_water", HOT_WATER.getStillGetter());
        FLUID_REGISTRY_MAP.put("hydrogen", HYDROGEN.getStillGetter());
        FLUID_REGISTRY_MAP.put("molten_salt", MOLTEN_SALT.getStillGetter());
        FLUID_REGISTRY_MAP.put("salt_slurry", SALT_SLURRY.getStillGetter());
        FLUID_REGISTRY_MAP.put("steam", STEAM.getStillGetter());
    }

    public static List<? extends Fluid> getITFluids() { return REGISTER.getEntries().stream().map(Supplier::get).collect(Collectors.toList()); }

    public record FluidEntry(Supplier<ModFluid> flowing, Supplier<ModFluid> still, ModBlocks.BlockEntry<FluidBlock> block, Supplier<BucketItem> bucket, Supplier<FluidType> type, List<Property<?>> properties, int tintColor) {
        @SuppressWarnings("unused")
        private static ModFluids.FluidEntry make(String name, ResourceLocation stillTex, ResourceLocation flowingTex) { return make(name, 0, stillTex, flowingTex, null, -1); }

        @SuppressWarnings("unused")
        private static ModFluids.FluidEntry make(String name, ResourceLocation stillTex, ResourceLocation flowingTex, Consumer<FluidType.Properties> buildAttributes) { return make(name, 0, stillTex, flowingTex, null, -1); }

        @SuppressWarnings("unused")
        private static ModFluids.FluidEntry make(String name, int burnTime, ResourceLocation stillTex, ResourceLocation flowingTex) { return make(name, burnTime, stillTex, flowingTex, null, -1); }

        private static ModFluids.FluidEntry make(String name, int burnTime, ResourceLocation stillTex, ResourceLocation flowingTex, @Nullable Consumer<FluidType.Properties> buildAttributes, int tintColor) {return make(name, burnTime, stillTex, flowingTex, ModFluid::new, ModFluid.Flowing::new, buildAttributes, ImmutableList.of(), tintColor); }

        @SuppressWarnings("unused")
        private static ModFluids.FluidEntry make(String name, ResourceLocation stillTex, ResourceLocation flowingTex, Function<ModFluids.FluidEntry, ? extends ModFluid> makeStill, Function<ModFluids.FluidEntry, ? extends ModFluid> makeFlowing, @Nullable Consumer<FluidType.Properties> buildAttributes, ImmutableList<Property<?>> properties, int tintColor) { return make(name, 0, stillTex, flowingTex, makeStill, makeFlowing, buildAttributes, properties, tintColor); }

        private static ModFluids.FluidEntry make(String name, int burnTime, ResourceLocation stillTex, ResourceLocation flowingTex, Function<ModFluids.FluidEntry, ? extends ModFluid> makeStill, Function<ModFluids.FluidEntry, ? extends ModFluid> makeFlowing, @Nullable Consumer<FluidType.Properties> buildAttributes, List<Property<?>> properties, int tintColor) { FluidType.Properties builder = FluidType.Properties.create().sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL).sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY);
            if (buildAttributes != null) { buildAttributes.accept(builder); }
            Supplier<FluidType> type = TYPE_REGISTER.register(name, () -> makeTypeWithTextures(builder, stillTex, flowingTex, tintColor));
            Mutable<ModFluids.FluidEntry> thisMutable = new MutableObject<>();
            Supplier<ModFluid> still = REGISTER.register(name, () -> ModFluid.makeFluid(makeStill, thisMutable.getValue()));
            Supplier<ModFluid> flowing = REGISTER.register(name+"_flowing", () -> ModFluid.makeFluid(makeFlowing, thisMutable.getValue()));
            ModBlocks.BlockEntry<FluidBlock> block = new ModBlocks.BlockEntry<>(name+"_fluid_block", () -> BlockBehaviour.Properties.ofFullCopy(Blocks.WATER), p -> new FluidBlock(thisMutable.getValue(), p));
            Supplier<BucketItem> bucket = ModItems.REGISTER.register(name+"_bucket", () -> makeBucket(still, burnTime));
            ModFluids.FluidEntry entry = new ModFluids.FluidEntry(flowing, still, block, bucket, type, properties, tintColor);
            thisMutable.setValue(entry);
            ALL_FLUID_BLOCKS.add(block);
            ALL_ENTRIES.add(entry);
            return entry;
        }

        private static FluidType makeTypeWithTextures(FluidType.Properties builder, ResourceLocation stillTex, ResourceLocation flowingTex, int tintColor) { return new FluidType(builder) {
            @SuppressWarnings("removal")
            @Override public void initializeClient(@NotNull Consumer<IClientFluidTypeExtensions> consumer) { consumer.accept(new IClientFluidTypeExtensions() {
                @Override public @NotNull ResourceLocation getStillTexture() { return stillTex; }

                @Override public @NotNull ResourceLocation getFlowingTexture() { return flowingTex; }

                @Override public int getTintColor() { return tintColor; }
            });
            }
        };
        }

        public ModFluid getFlowing() { return flowing.get(); }

        public ModFluid getStill() { return still.get(); }

        public FluidBlock getBlock() { return block.get(); }

        public BucketItem getBucket() { return bucket.get(); }

        private static BucketItem makeBucket(Supplier<ModFluid> still, int burnTime) {
            return new BucketItem(still.get(), new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET)) {
                @Override public int getBurnTime(@NotNull ItemStack itemStack, RecipeType<?> type) {return burnTime;}

                @SuppressWarnings("unused")
                public boolean emptyContents(@Nullable Player player, Level level, BlockPos pos, @Nullable HitResult target) {
                    boolean result;
                    if (target == null) { result = super.emptyContents(player, level, pos, null, null); }
                    else if (target instanceof BlockHitResult blockHitResult) { result = super.emptyContents(player, level, pos, blockHitResult, null); }
                    else { return false; }
                    if (result) {
                        FluidState placedState = level.getFluidState(pos);
                        if (placedState.getType().getFluidType().getDensity() < 0) { level.scheduleTick(pos, placedState.getType(), 100); }
                    }
                    return result;
                }
            };
        }

        public Supplier<ModFluid> getStillGetter() { return still; }
    }
}

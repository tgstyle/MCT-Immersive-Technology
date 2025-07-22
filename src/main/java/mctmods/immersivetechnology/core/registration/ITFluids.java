package mctmods.immersivetechnology.core.registration;

import com.google.common.collect.ImmutableList;
import mctmods.immersivetechnology.common.fluids.ITFluid;
import mctmods.immersivetechnology.common.fluids.ITFluidBlock;
import mctmods.immersivetechnology.core.lib.ITLib;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.NotNull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static mctmods.immersivetechnology.core.lib.ITLib.rl;

public class ITFluids {
    public static final DeferredRegister<Fluid> REGISTER = DeferredRegister.create(ForgeRegistries.FLUIDS, ITLib.MODID);
    public static final DeferredRegister<FluidType> TYPE_REGISTER = DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, ITLib.MODID);
    public static final List<ITFluids.FluidEntry> ALL_ENTRIES = new ArrayList<>();
    public static final Set<ITBlocks.BlockEntry<? extends LiquidBlock>> ALL_FLUID_BLOCKS = new HashSet<>();
    private static final HashMap<String, RegistryObject<? extends Fluid>> FLUID_REGISTRY_MAP = new HashMap<>();
    public static Function<String, Fluid> getFluid = (key) -> FLUID_REGISTRY_MAP.get(key).get();

    public static final ITFluids.FluidEntry STEAM = FluidEntry.make(
            "steam", 0, rl("block/fluid/fluid_gas_still"), rl("block/fluid/fluid_gas_flowing"),
            props -> { props.density(-1000).viscosity(200); }, 0xFFFFFFFF
    );

    public static final ITFluids.FluidEntry STEAM_EXHAUST = FluidEntry.make(
            "steam_exhaust", 0, rl("block/fluid/fluid_gas_still"), rl("block/fluid/fluid_gas_flowing"),
            props -> { props.density(-1000).viscosity(200); }, 0xFFC0C0C0
    );

    public static final ITFluids.FluidEntry FLUE_GAS = FluidEntry.make(
            "flue_gas", 0, rl("block/fluid/fluid_gas_still"), rl("block/fluid/fluid_gas_flowing"),
            props -> { props.density(-1000).viscosity(200); }, 0xFF808080
    );

    static {
        FLUID_REGISTRY_MAP.put("steam", STEAM.getStillGetter());
        FLUID_REGISTRY_MAP.put("steam_exhaust", STEAM_EXHAUST.getStillGetter());
        FLUID_REGISTRY_MAP.put("flue_gas", FLUE_GAS.getStillGetter());
    }

    public static List<? extends Fluid> getITFluids() { return REGISTER.getEntries().stream().map(RegistryObject::get).collect(Collectors.toList()); }

    public record FluidEntry(RegistryObject<ITFluid> flowing, RegistryObject<ITFluid> still, ITBlocks.BlockEntry<ITFluidBlock> block, RegistryObject<BucketItem> bucket, RegistryObject<FluidType> type, List<Property<?>> properties, int tintColor) {
        private static ITFluids.FluidEntry make(String name, ResourceLocation stillTex, ResourceLocation flowingTex) { return make(name, 0, stillTex, flowingTex, null, -1); }

        private static ITFluids.FluidEntry make(String name, ResourceLocation stillTex, ResourceLocation flowingTex, Consumer<FluidType.Properties> buildAttributes) { return make(name, 0, stillTex, flowingTex, buildAttributes, -1); }

        private static ITFluids.FluidEntry make(String name, int burnTime, ResourceLocation stillTex, ResourceLocation flowingTex) { return make(name, burnTime, stillTex, flowingTex, null, -1); }

        private static ITFluids.FluidEntry make(String name, int burnTime, ResourceLocation stillTex, ResourceLocation flowingTex, @Nullable Consumer<FluidType.Properties> buildAttributes, int tintColor) {return make(name, burnTime, stillTex, flowingTex, ITFluid::new, ITFluid.Flowing::new, buildAttributes, ImmutableList.of(), tintColor); }

        private static ITFluids.FluidEntry make(String name, ResourceLocation stillTex, ResourceLocation flowingTex, Function<ITFluids.FluidEntry, ? extends ITFluid> makeStill, Function<ITFluids.FluidEntry, ? extends ITFluid> makeFlowing, @Nullable Consumer<FluidType.Properties> buildAttributes, ImmutableList<Property<?>> properties, int tintColor) { return make(name, 0, stillTex, flowingTex, makeStill, makeFlowing, buildAttributes, properties, tintColor); }

        private static ITFluids.FluidEntry make(String name, int burnTime, ResourceLocation stillTex, ResourceLocation flowingTex, Function<ITFluids.FluidEntry, ? extends ITFluid> makeStill, Function<ITFluids.FluidEntry, ? extends ITFluid> makeFlowing, @Nullable Consumer<FluidType.Properties> buildAttributes, List<Property<?>> properties, int tintColor) { FluidType.Properties builder = FluidType.Properties.create().sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL).sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY);
            if (buildAttributes != null) { buildAttributes.accept(builder); }
            RegistryObject<FluidType> type = TYPE_REGISTER.register(name, () -> makeTypeWithTextures(builder, stillTex, flowingTex, tintColor));
            Mutable<ITFluids.FluidEntry> thisMutable = new MutableObject<>();
            RegistryObject<ITFluid> still = REGISTER.register(name, () -> ITFluid.makeFluid(makeStill, thisMutable.getValue()));
            RegistryObject<ITFluid> flowing = REGISTER.register(name+"_flowing", () -> ITFluid.makeFluid(makeFlowing, thisMutable.getValue()));
            ITBlocks.BlockEntry<ITFluidBlock> block = new ITBlocks.BlockEntry<>(name+"_fluid_block", () -> BlockBehaviour.Properties.copy(Blocks.WATER), p -> new ITFluidBlock(thisMutable.getValue(), p));
            RegistryObject<BucketItem> bucket = ITItems.REGISTER.register(name+"_bucket", () -> makeBucket(still, burnTime));
            ITFluids.FluidEntry entry = new ITFluids.FluidEntry(flowing, still, block, bucket, type, properties, tintColor);
            thisMutable.setValue(entry);
            ALL_FLUID_BLOCKS.add(block);
            ALL_ENTRIES.add(entry);
            return entry;
        }

        private static FluidType makeTypeWithTextures(FluidType.Properties builder, ResourceLocation stillTex, ResourceLocation flowingTex, int tintColor) { return new FluidType(builder) {
            @Override
            public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) { consumer.accept(new IClientFluidTypeExtensions() {
                @Override
                public ResourceLocation getStillTexture() { return stillTex; }

                @Override
                public ResourceLocation getFlowingTexture() { return flowingTex; }

                @Override
                public int getTintColor() { return tintColor; }
            });
            }
        };
        }

        public ITFluid getFlowing() { return flowing.get(); }

        public ITFluid getStill() { return still.get(); }

        public ITFluidBlock getBlock() { return block.get(); }

        public BucketItem getBucket() { return bucket.get(); }

        private static BucketItem makeBucket(RegistryObject<ITFluid> still, int burnTime) { return new BucketItem(still, new Item.Properties().stacksTo(1).craftRemainder(Items.BUCKET)) {
            @Override
            public @NotNull ICapabilityProvider initCapabilities(@NotNull ItemStack stack, @Nullable CompoundTag nbt) { return new FluidBucketWrapper(stack); }

            @Override
            public int getBurnTime(ItemStack itemStack, RecipeType<?> type) { return burnTime; }

            public boolean emptyContents(@Nullable Player player, Level level, BlockPos pos, @Nullable HitResult target) {
                boolean result;
                if (target == null) {
                    result = super.emptyContents(player, level, pos, null);
                } else if (target instanceof BlockHitResult blockHitResult) {
                    result = super.emptyContents(player, level, pos, blockHitResult);
                } else {
                    return false;
                }
                if (result) {
                    FluidState placedState = level.getFluidState(pos);
                    if (placedState.getType().getFluidType().getDensity() < 0) {
                        level.scheduleTick(pos, placedState.getType(), 100);  // Schedule dissipation after 5 seconds for gaseous sources
                    }
                }
                return result;
            }
        };
        }

        public RegistryObject<ITFluid> getStillGetter() { return still; }
    }
}

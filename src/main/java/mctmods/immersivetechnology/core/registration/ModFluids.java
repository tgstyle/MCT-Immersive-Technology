package mctmods.immersivetechnology.core.registration;

import mctmods.immersivetechnology.core.lib.Reference;

import com.immersiveconvergence.api.fluid.FluidEntry;
import com.immersiveconvergence.api.fluid.FluidRegisters;
import com.immersiveconvergence.api.registration.BlockEntry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static mctmods.immersivetechnology.core.lib.Reference.rl;

public class ModFluids {
    public static final DeferredRegister<Fluid> REGISTER = DeferredRegister.create(ForgeRegistries.FLUIDS, Reference.MODID);
    public static final DeferredRegister<FluidType> TYPE_REGISTER = DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, Reference.MODID);
    public static final List<FluidEntry> ALL_ENTRIES = new ArrayList<>();
    public static final Set<BlockEntry<? extends LiquidBlock>> ALL_FLUID_BLOCKS = new HashSet<>();
    private static final HashMap<String, RegistryObject<? extends Fluid>> FLUID_REGISTRY_MAP = new HashMap<>();
    public static Function<String, Fluid> getFluid = (key) -> FLUID_REGISTRY_MAP.get(key).get();
    private static final FluidRegisters REGISTERS = new FluidRegisters() {
        @Override public DeferredRegister<Fluid> fluids() { return REGISTER; }

        @Override public DeferredRegister<FluidType> fluidTypes() { return TYPE_REGISTER; }

        @Override public DeferredRegister<Item> items() { return ModItems.REGISTER; }

        @Override public <T extends Block> BlockEntry<T> block(String name, Supplier<BlockBehaviour.Properties> properties, Function<BlockBehaviour.Properties, T> make) { return new ModBlocks.BlockEntry<>(name, properties, make); }
    };

    public static final FluidEntry CHLORINE = make(
            "chlorine", 0, rl("block/fluid/fluid_gas_still"), rl("block/fluid/fluid_gas_flowing"),
            props -> props.density(-100).viscosity(1000), 0xFFC0E67B
    );

    public static final FluidEntry EXHAUST_STEAM = make(
            "exhaust_steam", 0, rl("block/fluid/fluid_gas_still"), rl("block/fluid/fluid_gas_flowing"),
            props -> props.density(-100).viscosity(500).temperature(500), 0xFFC1C1C5
    );

    public static final FluidEntry GRAVEL_SLURRY = make(
            "gravel_slurry", 0, rl("block/fluid/fluid_still"), rl("block/fluid/fluid_flowing"),
            props -> props.density(1000).viscosity(1000), 0xFF6D6565
    );

    public static final FluidEntry DISTILLED_WATER = make(
            "distilled_water", 0, rl("block/fluid/fluid_still"), rl("block/fluid/fluid_flowing"),
            props -> props.density(1000).viscosity(1000), 0xFF7079E0
    );

    public static final FluidEntry FLUE_GAS = make(
            "flue_gas", 0, rl("block/fluid/fluid_gas_still"), rl("block/fluid/fluid_gas_flowing"),
            props -> props.density(-100).viscosity(500), 0xFFFFFFFF
    );

    public static final FluidEntry HEATED_GRAVEL = make(
            "heated_gravel_slurry", 0, rl("block/fluid/molten_still"), rl("block/fluid/molten_flowing"),
            props -> props.density(930).viscosity(10000).temperature(400), 0xFFA8A6A6
    );

    public static final FluidEntry HEATED_SALT = make(
            "heated_salt_slurry", 0, rl("block/fluid/molten_still"), rl("block/fluid/molten_flowing"),
            props -> props.density(930).viscosity(10000).temperature(400), 0xFFC2C2C2
    );

    public static final FluidEntry HOT_WATER = make(
            "hot_water", 0, rl("block/fluid/fluid_still"), rl("block/fluid/fluid_flowing"),
            props -> props.density(1000).viscosity(1000).temperature(400), 0xFF0DFFFF
    );

    public static final FluidEntry HYDROGEN = make(
            "hydrogen", 0, rl("block/fluid/fluid_gas_still"), rl("block/fluid/fluid_gas_flowing"),
            props -> props.density(-100).viscosity(1000), 0xFF00BFFF
    );

    public static final FluidEntry MOLTEN_SALT = make(
            "molten_salt", 0, rl("block/fluid/molten_still"), rl("block/fluid/molten_flowing"),
            props -> props.density(1000).viscosity(10000).temperature(1000), 0xFFAEA0A2
    );

    public static final FluidEntry SALT_SLURRY = make(
            "salt_slurry", 0, rl("block/fluid/fluid_still"), rl("block/fluid/fluid_flowing"),
            props -> props.density(1000).viscosity(1000), 0xFFBB6528
    );

    public static final FluidEntry STEAM = make(
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

    public static List<? extends Fluid> getITFluids() { return REGISTER.getEntries().stream().map(RegistryObject::get).collect(Collectors.toList()); }

    private static FluidEntry make(String name, int burnTime, ResourceLocation stillTex, ResourceLocation flowingTex, @Nullable Consumer<FluidType.Properties> buildAttributes, int tintColor) {
        FluidEntry entry = FluidEntry.make(REGISTERS, name, burnTime, stillTex, flowingTex, buildAttributes, tintColor);
        ALL_FLUID_BLOCKS.add(entry.block());
        ALL_ENTRIES.add(entry);
        return entry;
    }
}

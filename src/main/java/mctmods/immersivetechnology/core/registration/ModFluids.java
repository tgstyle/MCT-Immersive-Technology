package mctmods.immersivetechnology.core.registration;

import mctmods.immersivetechnology.core.lib.Reference;

import com.immersiveconvergence.api.fluid.FluidEntry;
import com.immersiveconvergence.api.fluid.FluidRegisters;
import com.immersiveconvergence.api.registration.BlockEntry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static mctmods.immersivetechnology.core.lib.Reference.rl;

public class ModFluids {
    public static final DeferredRegister<Fluid> REGISTER = DeferredRegister.create(BuiltInRegistries.FLUID, Reference.MODID);
    public static final DeferredRegister<FluidType> TYPE_REGISTER = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, Reference.MODID);
    public static final List<FluidEntry> ALL_ENTRIES = new ArrayList<>();
    public static final Set<BlockEntry<? extends LiquidBlock>> ALL_FLUID_BLOCKS = new HashSet<>();
    private static final HashMap<String, Supplier<? extends Fluid>> FLUID_REGISTRY_MAP = new HashMap<>();
    public static Function<String, Fluid> getFluid = (key) -> FLUID_REGISTRY_MAP.get(key).get();
    private static final FluidRegisters REGISTERS = new FluidRegisters() {
        @Override public DeferredRegister<Fluid> fluids() { return REGISTER; }

        @Override public DeferredRegister<FluidType> fluidTypes() { return TYPE_REGISTER; }

        @Override public DeferredRegister.Items items() { return ModItems.REGISTER; }

        @Override public <T extends Block> BlockEntry<T> block(String name, Supplier<BlockBehaviour.Properties> properties, Function<BlockBehaviour.Properties, T> make) { return new ModBlocks.BlockEntry<>(name, properties, make); }
    };

    public static final FluidEntry CARBON_DIOXIDE = make(
            "carbon_dioxide", rl("block/fluid/fluid_gas_still"), rl("block/fluid/fluid_gas_flowing"),
            props -> props.density(-100).viscosity(500), 0xFFBFC8C3
    );

    public static final FluidEntry CHLORINE = make(
            "chlorine", rl("block/fluid/fluid_gas_still"), rl("block/fluid/fluid_gas_flowing"),
            props -> props.density(-100).viscosity(1000), 0xFFC0E67B
    );

    public static final FluidEntry EXHAUST_STEAM = make(
            "exhaust_steam", rl("block/fluid/fluid_gas_still"), rl("block/fluid/fluid_gas_flowing"),
            props -> props.density(-100).viscosity(500).temperature(500), 0xFFC1C1C5
    );

    public static final FluidEntry GRAVEL_SLURRY = make(
            "gravel_slurry", rl("block/fluid/fluid_still"), rl("block/fluid/fluid_flowing"),
            props -> props.density(1000).viscosity(1000), 0xFF6D6565
    );

    public static final FluidEntry CRUDE_NITRATE = make(
            "crude_nitrate", rl("block/fluid/fluid_still"), rl("block/fluid/fluid_flowing"),
            props -> props.density(1000).viscosity(1500), 0xFF8A6A3C
    );

    public static final FluidEntry DISTILLED_WATER = make(
            "distilled_water", rl("block/fluid/fluid_still"), rl("block/fluid/fluid_flowing"),
            props -> props.density(1000).viscosity(1000), 0xFF7079E0
    );

    public static final FluidEntry FLUE_GAS = make(
            "flue_gas", rl("block/fluid/fluid_gas_still"), rl("block/fluid/fluid_gas_flowing"),
            props -> props.density(-100).viscosity(500), 0xFFFFFFFF
    );

    public static final FluidEntry FUEL_SLURRY = make(
            "fuel_slurry", rl("block/fluid/fluid_still"), rl("block/fluid/fluid_flowing"),
            props -> props.density(1000).viscosity(2000), 0xFF5A5F3A
    );

    public static final FluidEntry HEATED_GRAVEL = make(
            "heated_gravel_slurry", rl("block/fluid/molten_still"), rl("block/fluid/molten_flowing"),
            props -> props.density(930).viscosity(10000).temperature(400), 0xFFA8A6A6
    );

    public static final FluidEntry HEATED_SALT = make(
            "heated_salt_slurry", rl("block/fluid/molten_still"), rl("block/fluid/molten_flowing"),
            props -> props.density(930).viscosity(10000).temperature(400), 0xFFC2C2C2
    );

    public static final FluidEntry HOT_WATER = make(
            "hot_water", rl("block/fluid/fluid_still"), rl("block/fluid/fluid_flowing"),
            props -> props.density(1000).viscosity(1000).temperature(400), 0xFF0DFFFF
    );

    public static final FluidEntry HYDROGEN = make(
            "hydrogen", rl("block/fluid/fluid_gas_still"), rl("block/fluid/fluid_gas_flowing"),
            props -> props.density(-100).viscosity(1000), 0xFF00BFFF
    );

    public static final FluidEntry MOLTEN_SALT = make(
            "molten_salt", rl("block/fluid/molten_still"), rl("block/fluid/molten_flowing"),
            props -> props.density(1000).viscosity(10000).temperature(1000), 0xFFAEA0A2
    );

    public static final FluidEntry NITRIC_ACID = make(
            "nitric_acid", rl("block/fluid/fluid_still"), rl("block/fluid/fluid_flowing"),
            props -> props.density(1000).viscosity(1200), 0xFFD8A93F
    );

    public static final FluidEntry NUCLEAR_WASTE = make(
            "nuclear_waste", rl("block/fluid/fluid_still"), rl("block/fluid/fluid_flowing"),
            props -> props.density(1000).viscosity(2000), 0xFF3E4A2E
    );

    public static final FluidEntry SALT_SLURRY = make(
            "salt_slurry", rl("block/fluid/fluid_still"), rl("block/fluid/fluid_flowing"),
            props -> props.density(1000).viscosity(1000), 0xFFBB6528
    );

    public static final FluidEntry SULFURIC_ACID = make(
            "sulfuric_acid", rl("block/fluid/fluid_still"), rl("block/fluid/fluid_flowing"),
            props -> props.density(1830).viscosity(1200), 0xFFC9B247
    );

    public static final FluidEntry STEAM = make(
            "steam", rl("block/fluid/fluid_gas_still"), rl("block/fluid/fluid_gas_flowing"),
            props -> props.density(-100).viscosity(500).temperature(1000), 0xFF3E444F
    );

    static {
        FLUID_REGISTRY_MAP.put("carbon_dioxide", CARBON_DIOXIDE.getStillGetter());
        FLUID_REGISTRY_MAP.put("chlorine", CHLORINE.getStillGetter());
        FLUID_REGISTRY_MAP.put("crude_nitrate", CRUDE_NITRATE.getStillGetter());
        FLUID_REGISTRY_MAP.put("distilled_water", DISTILLED_WATER.getStillGetter());
        FLUID_REGISTRY_MAP.put("exhaust_steam", EXHAUST_STEAM.getStillGetter());
        FLUID_REGISTRY_MAP.put("flue_gas", FLUE_GAS.getStillGetter());
        FLUID_REGISTRY_MAP.put("fuel_slurry", FUEL_SLURRY.getStillGetter());
        FLUID_REGISTRY_MAP.put("gravel_slurry", GRAVEL_SLURRY.getStillGetter());
        FLUID_REGISTRY_MAP.put("heated_gravel_slurry", HEATED_GRAVEL.getStillGetter());
        FLUID_REGISTRY_MAP.put("heated_salt_slurry", HEATED_SALT.getStillGetter());
        FLUID_REGISTRY_MAP.put("hot_water", HOT_WATER.getStillGetter());
        FLUID_REGISTRY_MAP.put("hydrogen", HYDROGEN.getStillGetter());
        FLUID_REGISTRY_MAP.put("molten_salt", MOLTEN_SALT.getStillGetter());
        FLUID_REGISTRY_MAP.put("nitric_acid", NITRIC_ACID.getStillGetter());
        FLUID_REGISTRY_MAP.put("nuclear_waste", NUCLEAR_WASTE.getStillGetter());
        FLUID_REGISTRY_MAP.put("salt_slurry", SALT_SLURRY.getStillGetter());
        FLUID_REGISTRY_MAP.put("steam", STEAM.getStillGetter());
        FLUID_REGISTRY_MAP.put("sulfuric_acid", SULFURIC_ACID.getStillGetter());
    }

    private static FluidEntry make(String name, ResourceLocation stillTex, ResourceLocation flowingTex, @Nullable Consumer<FluidType.Properties> buildAttributes, int tintColor) {
        FluidEntry entry = FluidEntry.make(REGISTERS, name, 0, stillTex, flowingTex, buildAttributes, tintColor);
        ALL_FLUID_BLOCKS.add(entry.block());
        ALL_ENTRIES.add(entry);
        return entry;
    }
}

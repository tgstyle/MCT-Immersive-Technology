package mctmods.immersivetechnology.common.multiblocks.stone.recipe.serializer;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import malte0811.dualcodecs.DualMapCodec;
import mctmods.immersivetechnology.common.multiblocks.stone.recipe.CoolingTowerRecipe;
import mctmods.immersivetechnology.core.registration.ITMultiblockRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public class CoolingTowerRecipeSerializer extends IERecipeSerializer<CoolingTowerRecipe> {
    @Override public ItemStack getIcon() { return ITMultiblockRegistry.COOLING_TOWER.iconStack(); }

    @Override
    protected DualMapCodec<RegistryFriendlyByteBuf, CoolingTowerRecipe> codecs() {
        MapCodec<TagKey<Fluid>> inputTag0Codec = ResourceLocation.CODEC
                .xmap(rl -> TagKey.create(Registries.FLUID, rl), TagKey::location)
                .fieldOf("inputTag0");
        MapCodec<Integer> amount0Codec = Codec.INT.fieldOf("amount0");

        MapCodec<TagKey<Fluid>> inputTag1Codec = ResourceLocation.CODEC
                .xmap(rl -> TagKey.create(Registries.FLUID, rl), TagKey::location)
                .fieldOf("inputTag1");
        MapCodec<Integer> amount1Codec = Codec.INT.fieldOf("amount1");

        MapCodec<FluidStack> output0Codec = FluidStack.OPTIONAL_CODEC.optionalFieldOf("output0", FluidStack.EMPTY);
        MapCodec<FluidStack> output1Codec = FluidStack.OPTIONAL_CODEC.optionalFieldOf("output1", FluidStack.EMPTY);
        MapCodec<FluidStack> output2Codec = FluidStack.OPTIONAL_CODEC.optionalFieldOf("output2", FluidStack.EMPTY);

        MapCodec<Integer> timeCodec = Codec.INT.fieldOf("time");

        MapCodec<CoolingTowerRecipe> mapCodec = RecordCodecBuilder.mapCodec(instance -> instance.group(
                inputTag0Codec.forGetter(CoolingTowerRecipe::inputTag0),
                amount0Codec.forGetter(CoolingTowerRecipe::amount0),
                inputTag1Codec.forGetter(CoolingTowerRecipe::inputTag1),
                amount1Codec.forGetter(CoolingTowerRecipe::amount1),
                output0Codec.forGetter(r -> java.util.Objects.requireNonNullElse(r.fluidOutput0(), FluidStack.EMPTY)),
                output1Codec.forGetter(r -> java.util.Objects.requireNonNullElse(r.fluidOutput1(), FluidStack.EMPTY)),
                output2Codec.forGetter(r -> java.util.Objects.requireNonNullElse(r.fluidOutput2(), FluidStack.EMPTY)),
                timeCodec.forGetter(CoolingTowerRecipe::getTotalProcessTime)
        ).apply(instance, (tag0, amt0, tag1, amt1, out0, out1, out2, t) -> {
            FluidStack o0 = out0.isEmpty() ? FluidStack.EMPTY : out0;
            FluidStack o1 = out1.isEmpty() ? FluidStack.EMPTY : out1;
            FluidStack o2 = out2.isEmpty() ? FluidStack.EMPTY : out2;
            return new CoolingTowerRecipe(o0, o1, o2, tag0, amt0, tag1, amt1, t);
        }));

        StreamCodec<RegistryFriendlyByteBuf, CoolingTowerRecipe> streamCodec = new StreamCodec<>() {
            @Override
            public @NotNull CoolingTowerRecipe decode(@NotNull RegistryFriendlyByteBuf buf) {
                TagKey<Fluid> tag0 = ResourceLocation.STREAM_CODEC.map(rl -> TagKey.create(Registries.FLUID, rl), TagKey::location).decode(buf);
                int amt0 = buf.readVarInt();
                TagKey<Fluid> tag1 = ResourceLocation.STREAM_CODEC.map(rl -> TagKey.create(Registries.FLUID, rl), TagKey::location).decode(buf);
                int amt1 = buf.readVarInt();
                FluidStack out0 = FluidStack.OPTIONAL_STREAM_CODEC.decode(buf);
                FluidStack out1 = FluidStack.OPTIONAL_STREAM_CODEC.decode(buf);
                FluidStack out2 = FluidStack.OPTIONAL_STREAM_CODEC.decode(buf);
                int time = buf.readVarInt();
                FluidStack o0 = out0.isEmpty() ? FluidStack.EMPTY : out0;
                FluidStack o1 = out1.isEmpty() ? FluidStack.EMPTY : out1;
                FluidStack o2 = out2.isEmpty() ? FluidStack.EMPTY : out2;
                return new CoolingTowerRecipe(o0, o1, o2, tag0, amt0, tag1, amt1, time);
            }

            @Override
            public void encode(@NotNull RegistryFriendlyByteBuf buf, CoolingTowerRecipe recipe) {
                ResourceLocation.STREAM_CODEC.map(rl -> TagKey.create(Registries.FLUID, rl), TagKey::location).encode(buf, recipe.inputTag0());
                buf.writeVarInt(recipe.amount0());
                ResourceLocation.STREAM_CODEC.map(rl -> TagKey.create(Registries.FLUID, rl), TagKey::location).encode(buf, recipe.inputTag1());
                buf.writeVarInt(recipe.amount1());
                FluidStack o0 = java.util.Objects.requireNonNullElse(recipe.fluidOutput0(), FluidStack.EMPTY);
                FluidStack.OPTIONAL_STREAM_CODEC.encode(buf, o0);
                FluidStack o1 = java.util.Objects.requireNonNullElse(recipe.fluidOutput1(), FluidStack.EMPTY);
                FluidStack.OPTIONAL_STREAM_CODEC.encode(buf, o1);
                FluidStack o2 = java.util.Objects.requireNonNullElse(recipe.fluidOutput2(), FluidStack.EMPTY);
                FluidStack.OPTIONAL_STREAM_CODEC.encode(buf, o2);
                buf.writeVarInt(recipe.getTotalProcessTime());
            }
        };
        return new DualMapCodec<>(mapCodec, streamCodec);
    }
}

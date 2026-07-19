package mctmods.immersivetechnology.common.multiblocks.metal.recipe.serializer;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import malte0811.dualcodecs.DualMapCodec;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.HeatExchangerRecipe;
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

import java.util.Optional;

public class HeatExchangerRecipeSerializer extends IERecipeSerializer<HeatExchangerRecipe> {
    @Override public ItemStack getIcon() { return ITMultiblockRegistry.HEAT_EXCHANGER.iconStack(); }

    @Override
    protected DualMapCodec<RegistryFriendlyByteBuf, HeatExchangerRecipe> codecs() {
        MapCodec<TagKey<Fluid>> input0TagCodec = ResourceLocation.CODEC
                .xmap(rl -> TagKey.create(Registries.FLUID, rl), TagKey::location)
                .fieldOf("input0Tag");

        MapCodec<Integer> input0AmountCodec = Codec.INT.fieldOf("input0Amount");

        MapCodec<Optional<TagKey<Fluid>>> input1TagCodec = ResourceLocation.CODEC
                .xmap(rl -> TagKey.create(Registries.FLUID, rl), TagKey::location)
                .optionalFieldOf("input1Tag");

        MapCodec<Optional<Integer>> input1AmountCodec = Codec.INT.optionalFieldOf("input1Amount");

        MapCodec<FluidStack> output0Codec = FluidStack.CODEC.fieldOf("output0");

        MapCodec<FluidStack> output1Codec = FluidStack.OPTIONAL_CODEC.optionalFieldOf("output1", FluidStack.EMPTY);

        MapCodec<Integer> energyCodec = Codec.INT.fieldOf("energy");
        MapCodec<Integer> timeCodec = Codec.INT.fieldOf("time");

        MapCodec<HeatExchangerRecipe> mapCodec = RecordCodecBuilder.mapCodec(instance -> instance.group(
                input0TagCodec.forGetter(HeatExchangerRecipe::input0Tag),
                input0AmountCodec.forGetter(HeatExchangerRecipe::input0Amount),
                input1TagCodec.forGetter(r -> Optional.ofNullable(r.input1Tag())),
                input1AmountCodec.forGetter(r -> Optional.ofNullable(r.getInput1Amount() > 0 ? r.getInput1Amount() : null)),
                output0Codec.forGetter(r -> java.util.Objects.requireNonNullElse(r.output0(), FluidStack.EMPTY)),
                output1Codec.forGetter(r -> java.util.Objects.requireNonNullElse(r.output1(), FluidStack.EMPTY)),
                energyCodec.forGetter(HeatExchangerRecipe::getTotalProcessEnergy),
                timeCodec.forGetter(HeatExchangerRecipe::getTotalProcessTime)
        ).apply(instance, (in0Tag, in0Amt, in1TagOpt, in1AmtOpt, out0, out1, energy, time) -> {
            TagKey<Fluid> in1Tag = in1TagOpt.orElse(null);
            int in1Amt = in1AmtOpt.orElse(0);
            FluidStack o1 = (out1 == null || out1.isEmpty()) ? null : out1;
            return new HeatExchangerRecipe(in0Tag, in0Amt, in1Tag, in1Amt, out0, o1, energy, time);
        }));

        StreamCodec<RegistryFriendlyByteBuf, HeatExchangerRecipe> streamCodec = new StreamCodec<>() {
            @Override
            public @NotNull HeatExchangerRecipe decode(@NotNull RegistryFriendlyByteBuf buf) {
                TagKey<Fluid> input0Tag = ResourceLocation.STREAM_CODEC.map(rl -> TagKey.create(Registries.FLUID, rl), TagKey::location).decode(buf);
                int input0Amount = buf.readVarInt();
                boolean hasInput1 = buf.readBoolean();
                TagKey<Fluid> input1Tag = hasInput1 ? ResourceLocation.STREAM_CODEC.map(rl -> TagKey.create(Registries.FLUID, rl), TagKey::location).decode(buf) : null;
                int input1Amount = hasInput1 ? buf.readVarInt() : 0;
                FluidStack output0 = FluidStack.STREAM_CODEC.decode(buf);
                boolean hasOutput1 = buf.readBoolean();
                FluidStack output1 = hasOutput1 ? FluidStack.OPTIONAL_STREAM_CODEC.decode(buf) : null;
                int energy = buf.readVarInt();
                int time = buf.readVarInt();
                return new HeatExchangerRecipe(input0Tag, input0Amount, input1Tag, input1Amount, output0, output1, energy, time);
            }

            @Override
            public void encode(@NotNull RegistryFriendlyByteBuf buf, HeatExchangerRecipe recipe) {
                ResourceLocation.STREAM_CODEC.map(rl -> TagKey.create(Registries.FLUID, rl), TagKey::location).encode(buf, recipe.input0Tag());
                buf.writeVarInt(recipe.input0Amount());
                boolean hasInput1 = recipe.input1Tag() != null;
                buf.writeBoolean(hasInput1);
                if (hasInput1) {
                    TagKey<Fluid> in1Tag = recipe.input1Tag();
                    ResourceLocation.STREAM_CODEC.map(rl -> TagKey.create(Registries.FLUID, rl), TagKey::location).encode(buf, java.util.Objects.requireNonNull(in1Tag));
                    buf.writeVarInt(recipe.input1Amount());
                }
                FluidStack out0 = java.util.Objects.requireNonNullElse(recipe.output0(), FluidStack.EMPTY);
                FluidStack.STREAM_CODEC.encode(buf, out0);
                FluidStack out1ForFlag = recipe.output1();
                boolean hasOutput1 = out1ForFlag != null && !out1ForFlag.isEmpty();
                buf.writeBoolean(hasOutput1);
                if (hasOutput1) {
                    FluidStack out1 = java.util.Objects.requireNonNullElse(recipe.output1(), FluidStack.EMPTY);
                    FluidStack.OPTIONAL_STREAM_CODEC.encode(buf, out1);
                }
                buf.writeVarInt(recipe.getTotalProcessEnergy());
                buf.writeVarInt(recipe.getTotalProcessTime());
            }
        };
        return new DualMapCodec<>(mapCodec, streamCodec);
    }
}

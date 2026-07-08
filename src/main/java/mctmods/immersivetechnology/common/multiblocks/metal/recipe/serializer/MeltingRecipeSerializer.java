package mctmods.immersivetechnology.common.multiblocks.metal.recipe.serializer;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import malte0811.dualcodecs.DualMapCodec;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.MeltingRecipe;
import mctmods.immersivetechnology.core.registration.ITMultiblockProvider;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public class MeltingRecipeSerializer extends IERecipeSerializer<MeltingRecipe> {
    @Override public ItemStack getIcon() { return ITMultiblockProvider.MELTING_CRUCIBLE.iconStack(); }

    @Override
    protected DualMapCodec<RegistryFriendlyByteBuf, MeltingRecipe> codecs() {
        MapCodec<TagKey<Fluid>> inputTagCodec = ResourceLocation.CODEC
                .xmap(rl -> TagKey.create(Registries.FLUID, rl), TagKey::location)
                .fieldOf("inputTag");

        MapCodec<Integer> inputAmountCodec = Codec.INT.fieldOf("inputAmount");

        MapCodec<FluidStack> fluidOutputCodec = FluidStack.OPTIONAL_CODEC.optionalFieldOf("output", FluidStack.EMPTY);

        MapCodec<Integer> timeCodec = Codec.INT.fieldOf("time");
        MapCodec<Double> requiredTempCodec = Codec.DOUBLE.fieldOf("requiredTemp");

        MapCodec<MeltingRecipe> mapCodec = RecordCodecBuilder.mapCodec(instance -> instance.group(
                inputTagCodec.forGetter(MeltingRecipe::inputTag),
                inputAmountCodec.forGetter(MeltingRecipe::inputAmount),
                fluidOutputCodec.forGetter(r -> java.util.Objects.requireNonNullElse(r.fluidOutput(), FluidStack.EMPTY)),
                timeCodec.forGetter(MeltingRecipe::getTotalProcessTime),
                requiredTempCodec.forGetter(r -> r.requiredTemp)
        ).apply(instance, (tag, amount, output, time, temp) -> {
            FluidStack out = output.isEmpty() ? null : output;
            return new MeltingRecipe(tag, amount, out, time, temp);
        }));

        StreamCodec<RegistryFriendlyByteBuf, MeltingRecipe> streamCodec = new StreamCodec<>() {
            @Override
            public @NotNull MeltingRecipe decode(@NotNull RegistryFriendlyByteBuf buf) {
                TagKey<Fluid> inputTag = ResourceLocation.STREAM_CODEC.map(rl -> TagKey.create(Registries.FLUID, rl), TagKey::location).decode(buf);
                int inputAmount = buf.readVarInt();
                FluidStack fluidOutput = FluidStack.OPTIONAL_STREAM_CODEC.decode(buf);
                int time = buf.readVarInt();
                double requiredTemp = buf.readDouble();
                FluidStack out = fluidOutput.isEmpty() ? null : fluidOutput;
                return new MeltingRecipe(inputTag, inputAmount, out, time, requiredTemp);
            }

            @Override
            public void encode(@NotNull RegistryFriendlyByteBuf buf, MeltingRecipe recipe) {
                ResourceLocation.STREAM_CODEC.map(rl -> TagKey.create(Registries.FLUID, rl), TagKey::location).encode(buf, recipe.inputTag());
                buf.writeVarInt(recipe.inputAmount());
                FluidStack out = java.util.Objects.requireNonNullElse(recipe.fluidOutput(), FluidStack.EMPTY);
                FluidStack.OPTIONAL_STREAM_CODEC.encode(buf, out);
                buf.writeVarInt(recipe.getTotalProcessTime());
                buf.writeDouble(recipe.requiredTemp);
            }
        };
        return new DualMapCodec<>(mapCodec, streamCodec);
    }
}

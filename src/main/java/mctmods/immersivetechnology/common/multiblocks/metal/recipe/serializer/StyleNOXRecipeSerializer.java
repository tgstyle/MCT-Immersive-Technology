package mctmods.immersivetechnology.common.multiblocks.metal.recipe.serializer;

import mctmods.immersivetechnology.common.multiblocks.metal.recipe.StyleNOXRecipe;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;

import com.immersiveconvergence.api.crafting.ICIngredientWithSize;
import com.immersiveconvergence.api.crafting.ICRecipeSerializer;
import com.immersiveconvergence.api.crafting.ICTagOutput;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import malte0811.dualcodecs.DualMapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nonnull;

public class StyleNOXRecipeSerializer extends ICRecipeSerializer<StyleNOXRecipe> {
    @Override public ItemStack getIcon() { return MultiblockRegistry.STYLENOX.iconStack(); }

    @Override protected DualMapCodec<RegistryFriendlyByteBuf, StyleNOXRecipe> codecs() {
        MapCodec<TagKey<Fluid>> waterTagCodec = ResourceLocation.CODEC
                .xmap(rl -> TagKey.create(Registries.FLUID, rl), TagKey::location)
                .fieldOf("waterTag");
        MapCodec<TagKey<Fluid>> co2TagCodec = ResourceLocation.CODEC
                .xmap(rl -> TagKey.create(Registries.FLUID, rl), TagKey::location)
                .fieldOf("co2Tag");

        MapCodec<StyleNOXRecipe> mapCodec = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ICIngredientWithSize.CODECS.codec().fieldOf("input").forGetter(StyleNOXRecipe::input),
                ICTagOutput.CODECS.codec().fieldOf("result").forGetter(StyleNOXRecipe::output),
                waterTagCodec.forGetter(StyleNOXRecipe::waterTag),
                Codec.INT.fieldOf("waterAmount").forGetter(StyleNOXRecipe::waterAmount),
                co2TagCodec.forGetter(StyleNOXRecipe::co2Tag),
                Codec.INT.fieldOf("co2Amount").forGetter(StyleNOXRecipe::co2Amount),
                FluidStack.CODEC.fieldOf("steamOutput").forGetter(StyleNOXRecipe::steamOutput),
                Codec.INT.fieldOf("time").forGetter(StyleNOXRecipe::getBaseTime),
                Codec.INT.fieldOf("energy").forGetter(StyleNOXRecipe::getBaseEnergy)
        ).apply(instance, StyleNOXRecipe::new));

        StreamCodec<RegistryFriendlyByteBuf, StyleNOXRecipe> streamCodec = new StreamCodec<>() {
            @Override public @Nonnull StyleNOXRecipe decode(@Nonnull RegistryFriendlyByteBuf buf) {
                ICIngredientWithSize input = ICIngredientWithSize.CODECS.streamCodec().decode(buf);
                ICTagOutput output = ICTagOutput.CODECS.streamCodec().decode(buf);
                TagKey<Fluid> waterTag = ResourceLocation.STREAM_CODEC.map(rl -> TagKey.create(Registries.FLUID, rl), TagKey::location).decode(buf);
                int waterAmount = buf.readVarInt();
                TagKey<Fluid> co2Tag = ResourceLocation.STREAM_CODEC.map(rl -> TagKey.create(Registries.FLUID, rl), TagKey::location).decode(buf);
                int co2Amount = buf.readVarInt();
                FluidStack steamOutput = FluidStack.STREAM_CODEC.decode(buf);
                int time = buf.readVarInt();
                int energy = buf.readVarInt();
                return new StyleNOXRecipe(input, output, waterTag, waterAmount, co2Tag, co2Amount, steamOutput, time, energy);
            }

            @Override public void encode(@Nonnull RegistryFriendlyByteBuf buf, StyleNOXRecipe recipe) {
                ICIngredientWithSize.CODECS.streamCodec().encode(buf, recipe.input());
                ICTagOutput.CODECS.streamCodec().encode(buf, recipe.output());
                ResourceLocation.STREAM_CODEC.map(rl -> TagKey.create(Registries.FLUID, rl), TagKey::location).encode(buf, recipe.waterTag());
                buf.writeVarInt(recipe.waterAmount());
                ResourceLocation.STREAM_CODEC.map(rl -> TagKey.create(Registries.FLUID, rl), TagKey::location).encode(buf, recipe.co2Tag());
                buf.writeVarInt(recipe.co2Amount());
                FluidStack.STREAM_CODEC.encode(buf, recipe.steamOutput());
                buf.writeVarInt(recipe.getBaseTime());
                buf.writeVarInt(recipe.getBaseEnergy());
            }
        };
        return new DualMapCodec<>(mapCodec, streamCodec);
    }
}

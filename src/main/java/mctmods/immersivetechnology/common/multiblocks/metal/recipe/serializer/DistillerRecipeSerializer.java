package mctmods.immersivetechnology.common.multiblocks.metal.recipe.serializer;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import malte0811.dualcodecs.DualMapCodec;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.DistillerRecipe;
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

public class DistillerRecipeSerializer extends IERecipeSerializer<DistillerRecipe> {
    @Override public ItemStack getIcon() { return ITMultiblockProvider.DISTILLER.iconStack(); }

    @Override
    protected DualMapCodec<RegistryFriendlyByteBuf, DistillerRecipe> codecs() {
        MapCodec<TagKey<Fluid>> fluidTagCodec = ResourceLocation.CODEC
                .xmap(rl -> TagKey.create(Registries.FLUID, rl), TagKey::location)
                .fieldOf("inputTag");

        MapCodec<FluidStack> fluidOutputCodec = FluidStack.OPTIONAL_CODEC.optionalFieldOf("result", FluidStack.EMPTY);

        MapCodec<ItemStack> itemOutputCodec = ItemStack.OPTIONAL_CODEC.optionalFieldOf("itemOutput", ItemStack.EMPTY);

        MapCodec<DistillerRecipe> mapCodec = RecordCodecBuilder.mapCodec(instance -> instance.group(
                fluidTagCodec.forGetter(DistillerRecipe::fluidTag),
                Codec.INT.fieldOf("inputAmount").forGetter(DistillerRecipe::amount),
                fluidOutputCodec.forGetter(DistillerRecipe::fluidOutput),
                itemOutputCodec.forGetter(DistillerRecipe::itemOutput),
                Codec.FLOAT.optionalFieldOf("chance", 0.0f).forGetter(DistillerRecipe::chance),
                Codec.INT.fieldOf("time").forGetter(DistillerRecipe::time),
                Codec.INT.fieldOf("energy").forGetter(DistillerRecipe::energy)
        ).apply(instance, DistillerRecipe::new));

        StreamCodec<RegistryFriendlyByteBuf, DistillerRecipe> streamCodec = new StreamCodec<>() {
            @Override
            public @NotNull DistillerRecipe decode(@NotNull RegistryFriendlyByteBuf buf) {
                TagKey<Fluid> fluidTag = ResourceLocation.STREAM_CODEC.map(rl -> TagKey.create(Registries.FLUID, rl), TagKey::location).decode(buf);
                int amount = buf.readVarInt();
                FluidStack fluidOutput = FluidStack.OPTIONAL_STREAM_CODEC.decode(buf);
                ItemStack itemOutput = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
                float chance = buf.readFloat();
                int time = buf.readVarInt();
                int energy = buf.readVarInt();
                return new DistillerRecipe(fluidTag, amount, fluidOutput, itemOutput, chance, time, energy);
            }

            @Override
            public void encode(@NotNull RegistryFriendlyByteBuf buf, DistillerRecipe recipe) {
                ResourceLocation.STREAM_CODEC.map(rl -> TagKey.create(Registries.FLUID, rl), TagKey::location).encode(buf, recipe.fluidTag());
                buf.writeVarInt(recipe.amount());
                FluidStack fluidOut = java.util.Objects.requireNonNullElse(recipe.fluidOutput(), FluidStack.EMPTY);
                FluidStack.OPTIONAL_STREAM_CODEC.encode(buf, fluidOut);
                ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, recipe.itemOutput());
                buf.writeFloat(recipe.chance());
                buf.writeVarInt(recipe.time());
                buf.writeVarInt(recipe.energy());
            }
        };
        return new DualMapCodec<>(mapCodec, streamCodec);
    }
}

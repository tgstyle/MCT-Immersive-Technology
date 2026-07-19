package mctmods.immersivetechnology.common.multiblocks.metal.recipe.serializer;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import malte0811.dualcodecs.DualMapCodec;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.RadiatorRecipe;
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

public class RadiatorRecipeSerializer extends IERecipeSerializer<RadiatorRecipe> {
    @Override public ItemStack getIcon() { return ITMultiblockRegistry.RADIATOR.iconStack(); }

    @Override
    protected DualMapCodec<RegistryFriendlyByteBuf, RadiatorRecipe> codecs() {
        MapCodec<TagKey<Fluid>> fluidTagCodec = ResourceLocation.CODEC
                .xmap(rl -> TagKey.create(Registries.FLUID, rl), TagKey::location)
                .fieldOf("fluidTag");

        MapCodec<Integer> amountCodec = Codec.INT.fieldOf("amount");

        MapCodec<FluidStack> fluidOutputCodec = FluidStack.OPTIONAL_CODEC.optionalFieldOf("output", FluidStack.EMPTY);

        MapCodec<Integer> timeCodec = Codec.INT.fieldOf("time");

        MapCodec<RadiatorRecipe> mapCodec = RecordCodecBuilder.mapCodec(instance -> instance.group(
                fluidTagCodec.forGetter(RadiatorRecipe::fluidTag),
                amountCodec.forGetter(RadiatorRecipe::amount),
                fluidOutputCodec.forGetter(r -> java.util.Objects.requireNonNullElse(r.fluidOutput(), FluidStack.EMPTY)),
                timeCodec.forGetter(RadiatorRecipe::getTotalProcessTime)
        ).apply(instance, (tag, amt, output, t) -> {
            FluidStack out = output.isEmpty() ? null : output;
            return new RadiatorRecipe(tag, amt, out, t);
        }));

        StreamCodec<RegistryFriendlyByteBuf, RadiatorRecipe> streamCodec = new StreamCodec<>() {
            @Override
            public @NotNull RadiatorRecipe decode(@NotNull RegistryFriendlyByteBuf buf) {
                TagKey<Fluid> fluidTag = ResourceLocation.STREAM_CODEC.map(rl -> TagKey.create(Registries.FLUID, rl), TagKey::location).decode(buf);
                int amount = buf.readVarInt();
                FluidStack fluidOutput = FluidStack.OPTIONAL_STREAM_CODEC.decode(buf);
                int time = buf.readVarInt();
                FluidStack out = fluidOutput.isEmpty() ? null : fluidOutput;
                return new RadiatorRecipe(fluidTag, amount, out, time);
            }

            @Override
            public void encode(@NotNull RegistryFriendlyByteBuf buf, RadiatorRecipe recipe) {
                ResourceLocation.STREAM_CODEC.map(rl -> TagKey.create(Registries.FLUID, rl), TagKey::location).encode(buf, recipe.fluidTag());
                buf.writeVarInt(recipe.amount());
                FluidStack out = java.util.Objects.requireNonNullElse(recipe.fluidOutput(), FluidStack.EMPTY);
                FluidStack.OPTIONAL_STREAM_CODEC.encode(buf, out);
                buf.writeVarInt(recipe.getTotalProcessTime());
            }
        };
        return new DualMapCodec<>(mapCodec, streamCodec);
    }
}

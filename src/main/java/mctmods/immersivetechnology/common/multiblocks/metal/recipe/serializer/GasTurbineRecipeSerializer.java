package mctmods.immersivetechnology.common.multiblocks.metal.recipe.serializer;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import malte0811.dualcodecs.DualMapCodec;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.GasTurbineRecipe;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public class GasTurbineRecipeSerializer extends IERecipeSerializer<GasTurbineRecipe> {
    @Override public ItemStack getIcon() { return MultiblockRegistry.GAS_TURBINE.iconStack(); }

    @Override
    protected DualMapCodec<RegistryFriendlyByteBuf, GasTurbineRecipe> codecs() {
        MapCodec<TagKey<Fluid>> fluidTagCodec = ResourceLocation.CODEC
                .xmap(rl -> TagKey.create(Registries.FLUID, rl), TagKey::location)
                .fieldOf("inputTag");

        MapCodec<Integer> amountCodec = Codec.INT.fieldOf("inputAmount");
        MapCodec<FluidStack> fluidOutputCodec = FluidStack.OPTIONAL_CODEC.optionalFieldOf("output", FluidStack.EMPTY);
        MapCodec<Integer> timeCodec = Codec.INT.fieldOf("time");
        MapCodec<Float> torqueCodec = Codec.FLOAT.optionalFieldOf("torque", 1.0f);

        MapCodec<GasTurbineRecipe> mapCodec = RecordCodecBuilder.mapCodec(instance -> instance.group(
                fluidTagCodec.forGetter(GasTurbineRecipe::fluidTag),
                amountCodec.forGetter(GasTurbineRecipe::amount),
                fluidOutputCodec.forGetter(r -> java.util.Objects.requireNonNullElse(r.fluidOutput(), FluidStack.EMPTY)),
                timeCodec.forGetter(GasTurbineRecipe::time),
                torqueCodec.forGetter(GasTurbineRecipe::torque)
        ).apply(instance, GasTurbineRecipe::new));

        StreamCodec<RegistryFriendlyByteBuf, GasTurbineRecipe> streamCodec = new StreamCodec<>() {
            @Override
            public @NotNull GasTurbineRecipe decode(@NotNull RegistryFriendlyByteBuf buf) {
                TagKey<Fluid> fluidTag = ResourceLocation.STREAM_CODEC.map(rl -> TagKey.create(Registries.FLUID, rl), TagKey::location).decode(buf);
                int amount = buf.readVarInt();
                FluidStack fluidOutput = FluidStack.OPTIONAL_STREAM_CODEC.decode(buf);
                int time = buf.readVarInt();
                float torque = buf.readFloat();
                return new GasTurbineRecipe(fluidTag, amount, fluidOutput.isEmpty() ? null : fluidOutput, time, torque);
            }

            @Override
            public void encode(@NotNull RegistryFriendlyByteBuf buf, GasTurbineRecipe recipe) {
                ResourceLocation.STREAM_CODEC.map(rl -> TagKey.create(Registries.FLUID, rl), TagKey::location).encode(buf, recipe.fluidTag());
                buf.writeVarInt(recipe.amount());
                FluidStack.OPTIONAL_STREAM_CODEC.encode(buf, java.util.Objects.requireNonNullElse(recipe.fluidOutput(), FluidStack.EMPTY));
                buf.writeVarInt(recipe.time());
                buf.writeFloat(recipe.torque());
            }
        };
        return new DualMapCodec<>(mapCodec, streamCodec);
    }
}

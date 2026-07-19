package mctmods.immersivetechnology.common.multiblocks.metal.recipe.serializer;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import malte0811.dualcodecs.DualMapCodec;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.SteamTurbineRecipe;
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

public class SteamTurbineRecipeSerializer extends IERecipeSerializer<SteamTurbineRecipe> {
    @Override public ItemStack getIcon() { return ITMultiblockRegistry.STEAM_TURBINE.iconStack(); }

    @Override
    protected DualMapCodec<RegistryFriendlyByteBuf, SteamTurbineRecipe> codecs() {
        MapCodec<TagKey<Fluid>> fluidTagCodec = ResourceLocation.CODEC
                .xmap(rl -> TagKey.create(Registries.FLUID, rl), TagKey::location)
                .fieldOf("inputTag");

        MapCodec<FluidStack> fluidOutputCodec = FluidStack.OPTIONAL_CODEC.optionalFieldOf("output", FluidStack.EMPTY);

        MapCodec<SteamTurbineRecipe> mapCodec = RecordCodecBuilder.mapCodec(instance -> instance.group(
                fluidTagCodec.forGetter(SteamTurbineRecipe::fluidTag),
                Codec.INT.fieldOf("inputAmount").forGetter(SteamTurbineRecipe::amount),
                fluidOutputCodec.forGetter(r -> java.util.Objects.requireNonNullElse(r.fluidOutput(), FluidStack.EMPTY)),
                Codec.INT.fieldOf("time").forGetter(SteamTurbineRecipe::time),
                Codec.FLOAT.fieldOf("torque").forGetter(SteamTurbineRecipe::torque)
        ).apply(instance, SteamTurbineRecipe::new));

        StreamCodec<RegistryFriendlyByteBuf, SteamTurbineRecipe> streamCodec = new StreamCodec<>() {
            @Override
            public @NotNull SteamTurbineRecipe decode(@NotNull RegistryFriendlyByteBuf buf) {
                TagKey<Fluid> fluidTag = ResourceLocation.STREAM_CODEC.map(rl -> TagKey.create(Registries.FLUID, rl), TagKey::location).decode(buf);
                int amount = buf.readVarInt();
                FluidStack fluidOutput = FluidStack.OPTIONAL_STREAM_CODEC.decode(buf);
                int time = buf.readVarInt();
                float torque = buf.readFloat();
                return new SteamTurbineRecipe(fluidTag, amount, fluidOutput.isEmpty() ? null : fluidOutput, time, torque);
            }

            @Override
            public void encode(@NotNull RegistryFriendlyByteBuf buf, SteamTurbineRecipe recipe) {
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

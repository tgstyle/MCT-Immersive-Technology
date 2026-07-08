package mctmods.immersivetechnology.common.multiblocks.metal.recipe.serializer;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import malte0811.dualcodecs.DualMapCodec;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.BoilerTankRecipe;
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

public class BoilerTankRecipeSerializer extends IERecipeSerializer<BoilerTankRecipe> {
    @Override public ItemStack getIcon() { return ITMultiblockProvider.BOILER_TANK.iconStack(); }

    @Override
    protected DualMapCodec<RegistryFriendlyByteBuf, BoilerTankRecipe> codecs() {
        MapCodec<TagKey<Fluid>> fluidTagCodec = ResourceLocation.CODEC
                .xmap(rl -> TagKey.create(Registries.FLUID, rl), TagKey::location)
                .fieldOf("inputTag");

        MapCodec<FluidStack> outputCodec = FluidStack.CODEC.fieldOf("result");

        MapCodec<BoilerTankRecipe> mapCodec = RecordCodecBuilder.mapCodec(instance -> instance.group(
                fluidTagCodec.forGetter(BoilerTankRecipe::fluidTag),
                Codec.INT.fieldOf("inputAmount").forGetter(BoilerTankRecipe::amount),
                outputCodec.forGetter(BoilerTankRecipe::output),
                Codec.INT.fieldOf("time").forGetter(BoilerTankRecipe::time),
                Codec.DOUBLE.fieldOf("requiredHeat").forGetter(BoilerTankRecipe::requiredHeat)
        ).apply(instance, BoilerTankRecipe::new));

        StreamCodec<RegistryFriendlyByteBuf, BoilerTankRecipe> streamCodec = new StreamCodec<>() {
            @Override
            public @NotNull BoilerTankRecipe decode(@NotNull RegistryFriendlyByteBuf buf) {
                TagKey<Fluid> fluidTag = ResourceLocation.STREAM_CODEC.map(rl -> TagKey.create(Registries.FLUID, rl), TagKey::location).decode(buf);
                int amount = buf.readVarInt();
                FluidStack output = FluidStack.STREAM_CODEC.decode(buf);
                int time = buf.readVarInt();
                double requiredHeat = buf.readDouble();
                return new BoilerTankRecipe(fluidTag, amount, output, time, requiredHeat);
            }

            @Override
            public void encode(@NotNull RegistryFriendlyByteBuf buf, BoilerTankRecipe recipe) {
                ResourceLocation.STREAM_CODEC.map(rl -> TagKey.create(Registries.FLUID, rl), TagKey::location).encode(buf, recipe.fluidTag());
                buf.writeVarInt(recipe.amount());
                FluidStack.STREAM_CODEC.encode(buf, recipe.output());
                buf.writeVarInt(recipe.time());
                buf.writeDouble(recipe.requiredHeat());
            }
        };
        return new DualMapCodec<>(mapCodec, streamCodec);
    }
}

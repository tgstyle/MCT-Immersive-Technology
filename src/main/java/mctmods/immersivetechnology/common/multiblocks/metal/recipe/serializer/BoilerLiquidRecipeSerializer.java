package mctmods.immersivetechnology.common.multiblocks.metal.recipe.serializer;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import malte0811.dualcodecs.DualMapCodec;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.BoilerLiquidRecipe;
import mctmods.immersivetechnology.core.registration.ITMultiblockRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

public class BoilerLiquidRecipeSerializer extends IERecipeSerializer<BoilerLiquidRecipe> {
    @Override public ItemStack getIcon() { return ITMultiblockRegistry.BOILER_LIQUID.iconStack(); }

    @Override
    protected DualMapCodec<RegistryFriendlyByteBuf, BoilerLiquidRecipe> codecs() {
        MapCodec<TagKey<Fluid>> fluidTagCodec = ResourceLocation.CODEC
                .xmap(rl -> TagKey.create(Registries.FLUID, rl), TagKey::location)
                .fieldOf("inputTag");

        MapCodec<BoilerLiquidRecipe> mapCodec = RecordCodecBuilder.mapCodec(instance -> instance.group(
                fluidTagCodec.forGetter(BoilerLiquidRecipe::fluidTag),
                Codec.INT.fieldOf("inputAmount").forGetter(BoilerLiquidRecipe::amount),
                Codec.INT.fieldOf("time").forGetter(BoilerLiquidRecipe::time),
                Codec.DOUBLE.fieldOf("heatPerTick").forGetter(BoilerLiquidRecipe::heatPerTick),
                Codec.DOUBLE.fieldOf("targetHeat").forGetter(BoilerLiquidRecipe::targetHeat)
        ).apply(instance, BoilerLiquidRecipe::new));

        StreamCodec<RegistryFriendlyByteBuf, BoilerLiquidRecipe> streamCodec = StreamCodec.composite(
                ResourceLocation.STREAM_CODEC.map(rl -> TagKey.create(Registries.FLUID, rl), TagKey::location),
                BoilerLiquidRecipe::fluidTag,
                StreamCodec.of(RegistryFriendlyByteBuf::writeVarInt, RegistryFriendlyByteBuf::readVarInt),
                BoilerLiquidRecipe::amount,
                StreamCodec.of(RegistryFriendlyByteBuf::writeVarInt, RegistryFriendlyByteBuf::readVarInt),
                BoilerLiquidRecipe::time,
                StreamCodec.of(RegistryFriendlyByteBuf::writeDouble, RegistryFriendlyByteBuf::readDouble),
                BoilerLiquidRecipe::heatPerTick,
                StreamCodec.of(RegistryFriendlyByteBuf::writeDouble, RegistryFriendlyByteBuf::readDouble),
                BoilerLiquidRecipe::targetHeat,
                BoilerLiquidRecipe::new
        );
        return new DualMapCodec<>(mapCodec, streamCodec);
    }
}

package mctmods.immersivetechnology.common.multiblocks.metal.recipe.serializer;

import com.immersiveconvergence.api.crafting.ICIngredientWithSize;
import com.immersiveconvergence.api.crafting.ICRecipeSerializer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import malte0811.dualcodecs.DualMapCodec;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.BoilerSolidRecipe;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class BoilerSolidRecipeSerializer extends ICRecipeSerializer<BoilerSolidRecipe> {
    @Override public ItemStack getIcon() { return MultiblockRegistry.BOILER_SOLID.iconStack(); }

    @Override
    protected DualMapCodec<RegistryFriendlyByteBuf, BoilerSolidRecipe> codecs() {
        MapCodec<ICIngredientWithSize> inputCodec = ICIngredientWithSize.CODEC.fieldOf("input");

        MapCodec<BoilerSolidRecipe> mapCodec = RecordCodecBuilder.mapCodec(instance -> instance.group(
                inputCodec.forGetter(BoilerSolidRecipe::input),
                Codec.DOUBLE.fieldOf("heatPerTick").forGetter(BoilerSolidRecipe::heatPerTick),
                Codec.DOUBLE.fieldOf("targetHeat").forGetter(BoilerSolidRecipe::targetHeat)
        ).apply(instance, BoilerSolidRecipe::new));

        StreamCodec<RegistryFriendlyByteBuf, BoilerSolidRecipe> streamCodec = new StreamCodec<>() {
            @Override
            public @Nonnull BoilerSolidRecipe decode(@Nonnull RegistryFriendlyByteBuf buf) {
                ICIngredientWithSize input = ICIngredientWithSize.STREAM_CODEC.decode(buf);
                double heatPerTick = buf.readDouble();
                double targetHeat = buf.readDouble();
                return new BoilerSolidRecipe(input, heatPerTick, targetHeat);
            }

            @Override
            public void encode(@Nonnull RegistryFriendlyByteBuf buf, BoilerSolidRecipe recipe) {
                ICIngredientWithSize.STREAM_CODEC.encode(buf, recipe.input());
                buf.writeDouble(recipe.heatPerTick());
                buf.writeDouble(recipe.targetHeat());
            }
        };
        return new DualMapCodec<>(mapCodec, streamCodec);
    }
}

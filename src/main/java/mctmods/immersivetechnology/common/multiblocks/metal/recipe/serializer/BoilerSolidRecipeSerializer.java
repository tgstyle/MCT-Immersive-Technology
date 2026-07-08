package mctmods.immersivetechnology.common.multiblocks.metal.recipe.serializer;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import malte0811.dualcodecs.DualMapCodec;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.BoilerSolidRecipe;
import mctmods.immersivetechnology.core.registration.ITMultiblockProvider;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class BoilerSolidRecipeSerializer extends IERecipeSerializer<BoilerSolidRecipe> {
    @Override public ItemStack getIcon() { return ITMultiblockProvider.BOILER_SOLID.iconStack(); }

    @Override
    protected DualMapCodec<RegistryFriendlyByteBuf, BoilerSolidRecipe> codecs() {
        MapCodec<IngredientWithSize> inputCodec = IngredientWithSize.CODEC.fieldOf("input");

        MapCodec<BoilerSolidRecipe> mapCodec = RecordCodecBuilder.mapCodec(instance -> instance.group(
                inputCodec.forGetter(BoilerSolidRecipe::input),
                Codec.DOUBLE.fieldOf("heatPerTick").forGetter(BoilerSolidRecipe::heatPerTick),
                Codec.DOUBLE.fieldOf("targetHeat").forGetter(BoilerSolidRecipe::targetHeat)
        ).apply(instance, BoilerSolidRecipe::new));

        StreamCodec<RegistryFriendlyByteBuf, BoilerSolidRecipe> streamCodec = new StreamCodec<>() {
            @Override
            public @NotNull BoilerSolidRecipe decode(@NotNull RegistryFriendlyByteBuf buf) {
                IngredientWithSize input = IngredientWithSize.STREAM_CODEC.decode(buf);
                double heatPerTick = buf.readDouble();
                double targetHeat = buf.readDouble();
                return new BoilerSolidRecipe(input, heatPerTick, targetHeat);
            }

            @Override
            public void encode(@NotNull RegistryFriendlyByteBuf buf, BoilerSolidRecipe recipe) {
                IngredientWithSize.STREAM_CODEC.encode(buf, recipe.input());
                buf.writeDouble(recipe.heatPerTick());
                buf.writeDouble(recipe.targetHeat());
            }
        };
        return new DualMapCodec<>(mapCodec, streamCodec);
    }
}

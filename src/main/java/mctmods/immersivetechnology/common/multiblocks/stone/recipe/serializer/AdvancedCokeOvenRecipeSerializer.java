package mctmods.immersivetechnology.common.multiblocks.stone.recipe.serializer;

import com.immersiveconvergence.api.crafting.ICIngredientWithSize;
import com.immersiveconvergence.api.crafting.ICRecipeSerializer;
import com.immersiveconvergence.api.crafting.ICTagOutput;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import malte0811.dualcodecs.DualMapCodec;
import mctmods.immersivetechnology.common.multiblocks.stone.recipe.AdvancedCokeOvenRecipe;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class AdvancedCokeOvenRecipeSerializer extends ICRecipeSerializer<AdvancedCokeOvenRecipe> {
    @Override
    public ItemStack getIcon() { return MultiblockRegistry.ADVANCED_COKE_OVEN.iconStack(); }

    @Override
    protected DualMapCodec<RegistryFriendlyByteBuf, AdvancedCokeOvenRecipe> codecs() {
        MapCodec<AdvancedCokeOvenRecipe> mapCodec = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ICIngredientWithSize.CODECS.codec().fieldOf("input").forGetter(AdvancedCokeOvenRecipe::input),
                ICTagOutput.CODECS.codec().fieldOf("result").forGetter(AdvancedCokeOvenRecipe::itemOutput),
                Codec.INT.fieldOf("time").forGetter(AdvancedCokeOvenRecipe::getBaseTime),
                Codec.INT.fieldOf("creosote").forGetter(AdvancedCokeOvenRecipe::creosoteOutput)
        ).apply(instance, AdvancedCokeOvenRecipe::new));

        StreamCodec<RegistryFriendlyByteBuf, AdvancedCokeOvenRecipe> streamCodec = new StreamCodec<>() {
            @Override
            public @Nonnull AdvancedCokeOvenRecipe decode(@Nonnull RegistryFriendlyByteBuf buf) {
                ICIngredientWithSize input = ICIngredientWithSize.CODECS.streamCodec().decode(buf);
                ICTagOutput itemOutput = ICTagOutput.CODECS.streamCodec().decode(buf);
                int time = buf.readVarInt();
                int creosoteOutput = buf.readVarInt();
                return new AdvancedCokeOvenRecipe(input, itemOutput, time, creosoteOutput);
            }

            @Override
            public void encode(@Nonnull RegistryFriendlyByteBuf buf, AdvancedCokeOvenRecipe recipe) {
                ICIngredientWithSize.CODECS.streamCodec().encode(buf, recipe.input());
                ICTagOutput.CODECS.streamCodec().encode(buf, recipe.itemOutput());
                buf.writeVarInt(recipe.getBaseTime());
                buf.writeVarInt(recipe.creosoteOutput());
            }
        };
        return new DualMapCodec<>(mapCodec, streamCodec);
    }
}

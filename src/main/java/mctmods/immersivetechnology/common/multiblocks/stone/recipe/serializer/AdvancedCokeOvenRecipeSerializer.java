package mctmods.immersivetechnology.common.multiblocks.stone.recipe.serializer;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.TagOutput;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import malte0811.dualcodecs.DualMapCodec;
import mctmods.immersivetechnology.common.multiblocks.stone.recipe.AdvancedCokeOvenRecipe;
import mctmods.immersivetechnology.core.registration.ITMultiblockProvider;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class AdvancedCokeOvenRecipeSerializer extends IERecipeSerializer<AdvancedCokeOvenRecipe> {
    @Override
    public ItemStack getIcon() { return ITMultiblockProvider.ADVANCED_COKE_OVEN.iconStack(); }

    @Override
    protected DualMapCodec<RegistryFriendlyByteBuf, AdvancedCokeOvenRecipe> codecs() {
        MapCodec<AdvancedCokeOvenRecipe> mapCodec = RecordCodecBuilder.mapCodec(instance -> instance.group(
                IngredientWithSize.CODECS.codec().fieldOf("input").forGetter(AdvancedCokeOvenRecipe::input),
                TagOutput.CODECS.codec().fieldOf("result").forGetter(AdvancedCokeOvenRecipe::itemOutput),
                Codec.INT.fieldOf("time").forGetter(AdvancedCokeOvenRecipe::time),
                Codec.INT.fieldOf("creosote").forGetter(AdvancedCokeOvenRecipe::creosoteOutput)
        ).apply(instance, AdvancedCokeOvenRecipe::new));

        StreamCodec<RegistryFriendlyByteBuf, AdvancedCokeOvenRecipe> streamCodec = new StreamCodec<>() {
            @Override
            public @NotNull AdvancedCokeOvenRecipe decode(@NotNull RegistryFriendlyByteBuf buf) {
                IngredientWithSize input = IngredientWithSize.CODECS.streamCodec().decode(buf);
                TagOutput itemOutput = TagOutput.CODECS.streamCodec().decode(buf);
                int time = buf.readVarInt();
                int creosoteOutput = buf.readVarInt();
                return new AdvancedCokeOvenRecipe(input, itemOutput, time, creosoteOutput);
            }

            @Override
            public void encode(@NotNull RegistryFriendlyByteBuf buf, AdvancedCokeOvenRecipe recipe) {
                IngredientWithSize.CODECS.streamCodec().encode(buf, recipe.input());
                TagOutput.CODECS.streamCodec().encode(buf, recipe.itemOutput());
                buf.writeVarInt(recipe.time());
                buf.writeVarInt(recipe.creosoteOutput());
            }
        };
        return new DualMapCodec<>(mapCodec, streamCodec);
    }
}

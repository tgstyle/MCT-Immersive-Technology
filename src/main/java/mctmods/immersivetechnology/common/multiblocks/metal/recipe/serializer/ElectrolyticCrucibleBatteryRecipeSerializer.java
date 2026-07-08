package mctmods.immersivetechnology.common.multiblocks.metal.recipe.serializer;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import malte0811.dualcodecs.DualMapCodec;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.ElectrolyticCrucibleBatteryRecipe;
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

public class ElectrolyticCrucibleBatteryRecipeSerializer extends IERecipeSerializer<ElectrolyticCrucibleBatteryRecipe> {
    @Override public ItemStack getIcon() { return ITMultiblockProvider.ELECTROLYTIC_CRUCIBLE_BATTERY.iconStack(); }

    @Override
    protected DualMapCodec<RegistryFriendlyByteBuf, ElectrolyticCrucibleBatteryRecipe> codecs() {
        MapCodec<TagKey<Fluid>> fluidTagCodec = ResourceLocation.CODEC
                .xmap(rl -> TagKey.create(Registries.FLUID, rl), TagKey::location)
                .fieldOf("inputTag");

        MapCodec<Integer> amountCodec = Codec.INT.fieldOf("inputAmount");

        MapCodec<FluidStack> fluidOutput0Codec = FluidStack.OPTIONAL_CODEC.optionalFieldOf("result0", FluidStack.EMPTY);
        MapCodec<FluidStack> fluidOutput1Codec = FluidStack.OPTIONAL_CODEC.optionalFieldOf("result1", FluidStack.EMPTY);
        MapCodec<FluidStack> fluidOutput2Codec = FluidStack.OPTIONAL_CODEC.optionalFieldOf("result2", FluidStack.EMPTY);

        MapCodec<ItemStack> itemOutputCodec = ItemStack.OPTIONAL_CODEC.optionalFieldOf("itemOutput", ItemStack.EMPTY);

        MapCodec<ElectrolyticCrucibleBatteryRecipe> mapCodec = RecordCodecBuilder.mapCodec(instance -> instance.group(
                fluidTagCodec.forGetter(ElectrolyticCrucibleBatteryRecipe::fluidTag),
                amountCodec.forGetter(ElectrolyticCrucibleBatteryRecipe::amount),
                fluidOutput0Codec.forGetter(r -> java.util.Objects.requireNonNullElse(r.fluidOutput0(), FluidStack.EMPTY)),
                fluidOutput1Codec.forGetter(r -> java.util.Objects.requireNonNullElse(r.fluidOutput1(), FluidStack.EMPTY)),
                fluidOutput2Codec.forGetter(r -> java.util.Objects.requireNonNullElse(r.fluidOutput2(), FluidStack.EMPTY)),
                itemOutputCodec.forGetter(ElectrolyticCrucibleBatteryRecipe::itemOutput),
                Codec.INT.fieldOf("energy").forGetter(ElectrolyticCrucibleBatteryRecipe::energy),
                Codec.INT.fieldOf("time").forGetter(ElectrolyticCrucibleBatteryRecipe::time)
        ).apply(instance, ElectrolyticCrucibleBatteryRecipe::new));

        StreamCodec<RegistryFriendlyByteBuf, ElectrolyticCrucibleBatteryRecipe> streamCodec = new StreamCodec<>() {
            @Override
            public @NotNull ElectrolyticCrucibleBatteryRecipe decode(@NotNull RegistryFriendlyByteBuf buf) {
                TagKey<Fluid> fluidTag = ResourceLocation.STREAM_CODEC.map(rl -> TagKey.create(Registries.FLUID, rl), TagKey::location).decode(buf);
                int amount = buf.readVarInt();
                FluidStack out0 = FluidStack.OPTIONAL_STREAM_CODEC.decode(buf);
                FluidStack out1 = FluidStack.OPTIONAL_STREAM_CODEC.decode(buf);
                FluidStack out2 = FluidStack.OPTIONAL_STREAM_CODEC.decode(buf);
                ItemStack itemOut = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
                int energy = buf.readVarInt();
                int time = buf.readVarInt();
                return new ElectrolyticCrucibleBatteryRecipe(fluidTag, amount, out0.isEmpty() ? null : out0, out1.isEmpty() ? null : out1, out2.isEmpty() ? null : out2, itemOut, energy, time);
            }

            @Override
            public void encode(@NotNull RegistryFriendlyByteBuf buf, ElectrolyticCrucibleBatteryRecipe recipe) {
                ResourceLocation.STREAM_CODEC.map(rl -> TagKey.create(Registries.FLUID, rl), TagKey::location).encode(buf, recipe.fluidTag());
                buf.writeVarInt(recipe.amount());
                FluidStack.OPTIONAL_STREAM_CODEC.encode(buf, java.util.Objects.requireNonNullElse(recipe.fluidOutput0(), FluidStack.EMPTY));
                FluidStack.OPTIONAL_STREAM_CODEC.encode(buf, java.util.Objects.requireNonNullElse(recipe.fluidOutput1(), FluidStack.EMPTY));
                FluidStack.OPTIONAL_STREAM_CODEC.encode(buf, java.util.Objects.requireNonNullElse(recipe.fluidOutput2(), FluidStack.EMPTY));
                ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, recipe.itemOutput());
                buf.writeVarInt(recipe.energy());
                buf.writeVarInt(recipe.time());
            }
        };
        return new DualMapCodec<>(mapCodec, streamCodec);
    }
}

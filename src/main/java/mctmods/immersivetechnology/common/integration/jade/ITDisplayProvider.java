package mctmods.immersivetechnology.common.integration.jade;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum ITDisplayProvider implements IBlockComponentProvider {
    INSTANCE;

    private static long lastAddTime = 0;

    @Override
    public ResourceLocation getUid() { return ResourceLocation.fromNamespaceAndPath("immersivetechnology", "display"); }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (System.currentTimeMillis() - lastAddTime > 10) {
            lastAddTime = System.currentTimeMillis();
            CompoundTag data = accessor.getServerData();
            if (data.contains("ITActive")) {
                boolean active = data.getBoolean("ITActive");
                tooltip.add(Component.literal("Status: " + (active ? "Active" : "Inactive")).withStyle(active ? ChatFormatting.GREEN : ChatFormatting.RED));
            }
            if (data.contains("ITFuelEmpty")) {
                tooltip.add(Component.literal("Fuel: Empty").withStyle(ChatFormatting.GRAY));
            }
        }
    }
}

package mctmods.immersivetechnology.core;

import com.electronwill.nightconfig.core.Config;
import com.google.common.base.Preconditions;
import mctmods.immersivetechnology.core.lib.ITLib;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = ITLib.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ITServerConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.EnumValue<DisassemblyMode> DISASSEMBLY_MODE;

    static {
        BUILDER.push("multiblocks");
        DISASSEMBLY_MODE = BUILDER.comment("Mode for multiblock disassembly. PROCESS_QUEUE: Use gradual queue with fake player. TEMPLATE_BLOCKS: Revert to template blocks like IE.").defineEnum("disassemblyMode", DisassemblyMode.PROCESS_QUEUE);
        BUILDER.pop();
    }

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    private static Config rawConfig;

    public static Config getRawConfig() { return Preconditions.checkNotNull(rawConfig); }

    @SubscribeEvent
    public static void onConfigChanged(ModConfigEvent event) {
        if (event.getConfig().getModId().equals(ITLib.MODID)) { rawConfig = event.getConfig().getConfigData(); }
    }

    public enum DisassemblyMode {
        PROCESS_QUEUE,
        TEMPLATE_BLOCKS
    }
}

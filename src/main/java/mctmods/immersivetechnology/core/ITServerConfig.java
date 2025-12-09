package mctmods.immersivetechnology.core;

import mctmods.immersivetechnology.core.lib.ITLib;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = ITLib.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ITServerConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.EnumValue<DisassemblyMode> DISASSEMBLY_MODE;

    public static DisassemblyMode disassemblyMode;

    static {
        BUILDER.push("multiblocks");
        DISASSEMBLY_MODE = BUILDER.comment("Mode for multiblock disassembly. PROCESS_QUEUE: Use gradual queue with fake player. TEMPLATE_BLOCKS: Revert to template blocks like IE.").defineEnum("disassemblyMode", DisassemblyMode.PROCESS_QUEUE);
        BUILDER.pop();
    }

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    @SubscribeEvent public static void onConfig(final ModConfigEvent event) { if (event.getConfig().getSpec() == SPEC) { disassemblyMode = DISASSEMBLY_MODE.get(); } }

    public enum DisassemblyMode {
        PROCESS_QUEUE,
        TEMPLATE_BLOCKS
    }
}

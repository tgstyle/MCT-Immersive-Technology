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
    /**
     * Multiplies the Alternator FE output. 1.0 = vanilla IT balance.
     *
     * Effective FE/t = (RPM/MAX_RPM) * torqueMultiplier * MAX_OUTPUT * alternatorPowerFactor
     */
    public static final ForgeConfigSpec.DoubleValue ALTERNATOR_POWER_FACTOR;

    public static DisassemblyMode disassemblyMode;
    public static double alternatorPowerFactor;

    static {
        BUILDER.push("multiblocks");
        DISASSEMBLY_MODE = BUILDER.comment("Mode for multiblock disassembly. PROCESS_QUEUE: Use gradual queue with fake player. TEMPLATE_BLOCKS: Revert to template blocks like IE.").defineEnum("disassemblyMode", DisassemblyMode.PROCESS_QUEUE);

        ALTERNATOR_POWER_FACTOR = BUILDER
                .comment(
                        "Multiplier for Alternator power output.",
                        "1.0 = default output, 2.0 = double output, 0.5 = half output."
                )
                .defineInRange("alternator_power_factor", 1.0D, 0.0D, 1000.0D);
        BUILDER.pop();
    }

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    @SubscribeEvent
    public static void onConfig(final ModConfigEvent event) {
        if (event.getConfig().getSpec() == SPEC) {
            disassemblyMode = DISASSEMBLY_MODE.get();
            alternatorPowerFactor = ALTERNATOR_POWER_FACTOR.get();
        }
    }

    public enum DisassemblyMode {
        PROCESS_QUEUE,
        TEMPLATE_BLOCKS
    }
}

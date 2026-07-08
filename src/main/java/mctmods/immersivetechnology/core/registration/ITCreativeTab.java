package mctmods.immersivetechnology.core.registration;

import mctmods.immersivetechnology.core.util.TranslationKey;
import mctmods.immersivetechnology.core.lib.ITLib;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

@SuppressWarnings("unused")
public class ITCreativeTab {
    public static final DeferredRegister<CreativeModeTab> REGISTER = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ITLib.MODID);

    public static Supplier<CreativeModeTab> MAIN = REGISTER.register(
            "main",
            () -> new CreativeModeTab.Builder(CreativeModeTab.Row.TOP, 0)
                    .icon(() -> ITItems.FORMATION_TOOL.get().getDefaultInstance())
                    .title(Component.translatable(TranslationKey.CREATIVE_TAB.getLocation()))
                    .displayItems(ITCreativeTab::fillITTab)
                    .build()
    );

    private static void fillITTab(CreativeModeTab.ItemDisplayParameters parms, CreativeModeTab.Output out) {
        ITItems.getITItems().forEach(item -> out.accept(item.getDefaultInstance()));
    }
}

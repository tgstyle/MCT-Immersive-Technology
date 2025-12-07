package mctmods.immersivetechnology.core.registration;

import mctmods.immersivetechnology.common.util.TranslationKey;
import mctmods.immersivetechnology.core.lib.ITLib;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

@SuppressWarnings("unused")
public class ITCreativeTab {
    public static final DeferredRegister<CreativeModeTab> REGISTER = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ITLib.MODID);

    public static RegistryObject<CreativeModeTab> MAIN = REGISTER.register(
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

package mctmods.immersivetechnology.core.registration;

import mctmods.immersivetechnology.core.lib.ITLib;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

@SuppressWarnings("all")
public class ITCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> REGISTER = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ITLib.MODID);

    public static RegistryObject<CreativeModeTab> MAIN = REGISTER.register(
            "main",
            () -> new CreativeModeTab.Builder(CreativeModeTab.Row.TOP, 0)
                    .icon(() -> ITItems.IT_FORMATION_TOOL.get().getDefaultInstance())
                    .title(Component.translatable("itemGroup.immersivetechnology"))
                    .displayItems(ITCreativeTabs::fillITTab)
                    .build()
    );

    private static void fillITTab(CreativeModeTab.ItemDisplayParameters parms, CreativeModeTab.Output out) {
        ITItems.getITItems().forEach(item -> {
            out.accept(item.getDefaultInstance());
        });
    }
}

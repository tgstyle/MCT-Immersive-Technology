package mctmods.immersivetechnology.core.proxy;

import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.registration.ITBlockEntities;
import mctmods.immersivetechnology.core.registration.ITBlocks;
import mctmods.immersivetechnology.core.registration.ITCreativeTabs;
import mctmods.immersivetechnology.core.registration.ITFluids;
import mctmods.immersivetechnology.core.registration.ITItems;
import mctmods.immersivetechnology.core.registration.ITMenuTypes;
import mctmods.immersivetechnology.core.registration.ITMultiblockProvider;
import mctmods.immersivetechnology.core.registration.ITParticles;
import mctmods.immersivetechnology.core.registration.ITRecipeTypes;
import mctmods.immersivetechnology.core.registration.ITSounds;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.IEventBus;

@SuppressWarnings("unused")
public class CommonProxy {
    public static void modConstruction(IEventBus event) {
        ITLib.IT_LOGGER.info("Registering IT Content!");
        ITMultiblockProvider.forceClassLoad();
        ITMultiblockProvider.init();
        ITMenuTypes.REGISTER.register(event);
        ITRecipeTypes.init(event);
        ITSounds.init(event);
        ITParticles.REGISTER.register(event);
        ITBlockEntities.init(event);
        ITBlocks.init(event);
        ITItems.init(event);
        ITFluids.REGISTER.register(event);
        ITFluids.TYPE_REGISTER.register(event);
        ITCreativeTabs.REGISTER.register(event);
    }

    public void reinitializeGUI() {}

    public Level getClientWorld() { return null; }

    public Player getClientPlayer() { return null; }
}

package mctmods.immersivetechnology.core.proxy;

import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.registration.BlockEntities;
import mctmods.immersivetechnology.core.registration.ModBlocks;
import mctmods.immersivetechnology.core.registration.CreativeTab;
import mctmods.immersivetechnology.core.registration.ModFluids;
import mctmods.immersivetechnology.core.registration.ModItems;
import mctmods.immersivetechnology.core.registration.MenuTypes;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;
import mctmods.immersivetechnology.core.registration.Particles;
import mctmods.immersivetechnology.core.registration.RecipeTypes;
import mctmods.immersivetechnology.core.registration.Sounds;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;

@SuppressWarnings("unused")
public class CommonProxy {
    public static void modConstruction(IEventBus event) {
        Reference.IT_LOGGER.info("Registering IT Content!");
        MultiblockRegistry.forceClassLoad();
        MenuTypes.REGISTER.register(event);
        RecipeTypes.init(event);
        Sounds.init(event);
        Particles.REGISTER.register(event);
        BlockEntities.init(event);
        ModBlocks.init(event);
        ModItems.init(event);
        ModFluids.REGISTER.register(event);
        ModFluids.TYPE_REGISTER.register(event);
        CreativeTab.REGISTER.register(event);
    }

    public void reinitializeGUI() {}

    public Level getClientWorld() { return null; }

    public Player getClientPlayer() { return null; }
}

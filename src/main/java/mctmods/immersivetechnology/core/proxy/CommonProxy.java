package mctmods.immersivetechnology.core.proxy;

import mctmods.immersivetechnology.core.registration.ITContent;
import mctmods.immersivetechnology.core.lib.ITLib;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.IEventBus;

public class CommonProxy
{
    public static void modConstruction(IEventBus event)
    {
        ITLib.IT_LOGGER.info("Registering IT Content!");

        ITContent.initialize(event);
    }

    public void reinitializeGUI(){}

    public Level getClientWorld()
    {
        return null;
    }

    public Player getClientPlayer()
    {
        return null;
    }
}

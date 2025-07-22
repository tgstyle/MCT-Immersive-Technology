package mctmods.immersivetechnology.core.registration;

import blusunrize.immersiveengineering.api.ManualHelper;
import blusunrize.lib.manual.ManualEntry;
import blusunrize.lib.manual.ManualInstance;
import blusunrize.lib.manual.Tree.InnerNode;
import mctmods.immersivetechnology.client.menu.multiblock.AdvCokeOvenScreen;
import mctmods.immersivetechnology.client.menu.multiblock.BoilerScreen;
import mctmods.immersivetechnology.common.ITTags;
import mctmods.immersivetechnology.core.lib.ITLib;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;

public class ITContent {
    public static void initializeManualEntries() {
        ManualInstance instance = ManualHelper.getManual();
        InnerNode<ResourceLocation, ManualEntry> parent_category = instance.getRoot().getOrCreateSubnode(new ResourceLocation(ITLib.MODID, "main"), 99);

        ManualEntry.ManualEntryBuilder builder = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
        builder.readFromFile(new ResourceLocation(ITLib.MODID, "intro"));
        instance.addEntry(parent_category, builder.create());

        InnerNode<ResourceLocation, ManualEntry> multiblock_category = parent_category.getOrCreateSubnode(new ResourceLocation(ITLib.MODID, "it_multiblocks"), 0);
        //multiblockEntry(instance, multiblock_category, "example");
        multiblockEntry(instance, multiblock_category, "boiler");
        //multiblockEntry(instance, multiblock_category, "distiller");
        multiblockEntry(instance, multiblock_category, "alternator");
        multiblockEntry(instance, multiblock_category, "coke_oven_advanced");
        multiblockEntry(instance, multiblock_category, "steam_turbine");
        multiblockEntry(instance, multiblock_category, "gas_turbine");
        //multiblockEntry(instance, multiblock_category, "solar_tower");
    }

    private static void multiblockEntry(ManualInstance instance, InnerNode<ResourceLocation, ManualEntry> category, String id) {
        ManualEntry.ManualEntryBuilder multiblock = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
        multiblock.readFromFile(new ResourceLocation(ITLib.MODID, id));
        instance.addEntry(category, multiblock.create());
    }

    public static void registerContainersAndScreens() {
        MenuScreens.register(ITMenuTypes.ADVANCED_COKE_OVEN_MENU.getType(), AdvCokeOvenScreen::new);
        MenuScreens.register(ITMenuTypes.BOILER_MENU.getType(), BoilerScreen::new);
    }

    public static void initialize(IEventBus event) {
        ITMultiblockProvider.forceClassLoad();
        ITRegistrationHolder.initialize();
        ITTags.initialize();
        ITRecipeTypes.init();
        ITSounds.init(event);
        ITParticles.REGISTER.register(event);
    }
}

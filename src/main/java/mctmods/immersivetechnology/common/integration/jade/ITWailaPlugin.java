package mctmods.immersivetechnology.common.integration.jade;

import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityDummy;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityMaster;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockPartBlock;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.registration.ITMultiblockProvider;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@SuppressWarnings({"unused", "unchecked", "rawtypes"})
@WailaPlugin
public class ITWailaPlugin implements IWailaPlugin {
    @Override
    public void registerClient(IWailaClientRegistration registration) {
        ITLib.IT_LOGGER.info("ITWailaPlugin: Client registration");
        for (var blockClass : ITMultiblockProvider.getAllBlockClasses()) { registration.registerBlockComponent(ITDisplayProvider.INSTANCE, blockClass); }
        registration.registerEnergyStorageClient(new ITMultiblockEnergyDataProvider());
        registration.registerFluidStorageClient(new ITMultiblockFluidDataProvider());
        registration.registerBlockComponent(ITDisplayProvider.INSTANCE, MultiblockPartBlock.class);
    }

    @Override
    public void register(IWailaCommonRegistration registration) {
        ITLib.IT_LOGGER.info("ITWailaPlugin: Common registration");
        registration.registerBlockDataProvider(ITStatusDataProvider.INSTANCE, MultiblockBlockEntityMaster.class);
        registration.registerBlockDataProvider(ITStatusDataProvider.INSTANCE, MultiblockBlockEntityDummy.class);
        registration.registerEnergyStorage(new ITMultiblockEnergyDataProvider(), MultiblockBlockEntityDummy.class);
        registration.registerEnergyStorage(new ITMultiblockEnergyDataProvider(), MultiblockBlockEntityMaster.class);
        registration.registerFluidStorage(new ITMultiblockFluidDataProvider(), MultiblockBlockEntityDummy.class);
        registration.registerFluidStorage(new ITMultiblockFluidDataProvider(), MultiblockBlockEntityMaster.class);
    }
}

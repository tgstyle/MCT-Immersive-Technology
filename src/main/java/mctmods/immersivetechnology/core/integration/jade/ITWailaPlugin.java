package mctmods.immersivetechnology.core.integration.jade;

import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityDummy;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityMaster;
import mctmods.immersivetechnology.core.lib.ITLib;
import net.minecraft.world.level.block.Block;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@SuppressWarnings("unused")
@WailaPlugin
public class ITWailaPlugin implements IWailaPlugin {

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        ITLib.IT_LOGGER.info("ITWailaPlugin: Client registration");
        registration.registerBlockComponent(ITDisplayProvider.INSTANCE, Block.class);
        registration.registerEnergyStorageClient(new ITMultiblockEnergyDataProvider());
        registration.registerFluidStorageClient(new ITMultiblockFluidDataProvider());
    }

    @Override
    public void register(IWailaCommonRegistration registration) {
        ITLib.IT_LOGGER.info("ITWailaPlugin: Common registration");
        registration.registerBlockDataProvider(ITStatusDataProvider.INSTANCE, MultiblockBlockEntityMaster.class);
        registration.registerBlockDataProvider(ITStatusDataProvider.INSTANCE, MultiblockBlockEntityDummy.class);
        registration.registerEnergyStorage(new ITMultiblockEnergyDataProvider(), MultiblockBlockEntityMaster.class);
        registration.registerEnergyStorage(new ITMultiblockEnergyDataProvider(), MultiblockBlockEntityDummy.class);
        registration.registerFluidStorage(new ITMultiblockFluidDataProvider(), MultiblockBlockEntityMaster.class);
        registration.registerFluidStorage(new ITMultiblockFluidDataProvider(), MultiblockBlockEntityDummy.class);

        registration.registerFluidStorage(new ITMultiblockFluidDataProvider(), mctmods.immersivetechnology.common.blocks.metal.logic.BarrelCommonBlockEntity.class);
    }
}

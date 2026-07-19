package mctmods.immersivetechnology.core.integration.jade;

import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityDummy;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityMaster;
import mctmods.immersivetechnology.core.lib.Reference;
import net.minecraft.world.level.block.Block;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@SuppressWarnings("unused")
@WailaPlugin
public class ModWailaPlugin implements IWailaPlugin {

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        Reference.IT_LOGGER.info("ModWailaPlugin: Client registration");
        registration.registerBlockComponent(DisplayProvider.INSTANCE, Block.class);
        registration.registerEnergyStorageClient(new MultiblockEnergyDataProvider());
        registration.registerFluidStorageClient(new MultiblockFluidDataProvider());
    }

    @Override
    public void register(IWailaCommonRegistration registration) {
        Reference.IT_LOGGER.info("ModWailaPlugin: Common registration");
        registration.registerBlockDataProvider(StatusDataProvider.INSTANCE, MultiblockBlockEntityMaster.class);
        registration.registerBlockDataProvider(StatusDataProvider.INSTANCE, MultiblockBlockEntityDummy.class);
        registration.registerEnergyStorage(new MultiblockEnergyDataProvider(), MultiblockBlockEntityMaster.class);
        registration.registerEnergyStorage(new MultiblockEnergyDataProvider(), MultiblockBlockEntityDummy.class);
        registration.registerFluidStorage(new MultiblockFluidDataProvider(), MultiblockBlockEntityMaster.class);
        registration.registerFluidStorage(new MultiblockFluidDataProvider(), MultiblockBlockEntityDummy.class);

        registration.registerFluidStorage(new MultiblockFluidDataProvider(), mctmods.immersivetechnology.common.blocks.metal.logic.BarrelCommonBlockEntity.class);
    }
}

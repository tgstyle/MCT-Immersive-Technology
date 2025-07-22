package mctmods.immersivetechnology.common.blocks.multiblocks;

import blusunrize.immersiveengineering.api.multiblocks.ClientMultiblocks;
import mctmods.immersivetechnology.common.blocks.multiblocks.helper.ITClientMultiblockProperties;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.registration.ITMultiblockProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class ITAdvancedCokeOven extends ITTemplateMultiblock
{
    public static final ITAdvancedCokeOven INSTANCE = new ITAdvancedCokeOven();

    public ITAdvancedCokeOven()
    {
        super(new ResourceLocation(ITLib.MODID, "multiblocks/coke_oven_advanced"), new BlockPos(0,0,0), new BlockPos(1,1,2), new BlockPos(3,4,3), ITMultiblockProvider.ADV_COKE_OVEN);
    }

    @Override
    public void disassemble(Level world, BlockPos origin, boolean mirrored, Direction clickDirectionAtCreation)
    {
        super.disassemble(world, origin, mirrored, clickDirectionAtCreation);
    }

    @Override
    public float getManualScale()
    {
        return 16;
    }

    @Override
    public void initializeClient(Consumer<ClientMultiblocks.MultiblockManualData> consumer)
    {
        consumer.accept(new ITClientMultiblockProperties(this, 0, 0, 0));
    }
}

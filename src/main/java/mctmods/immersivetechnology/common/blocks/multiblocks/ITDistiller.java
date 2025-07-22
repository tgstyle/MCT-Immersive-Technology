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

public class ITDistiller extends ITTemplateMultiblock
{
    public static final ITDistiller INSTANCE = new ITDistiller();

    public ITDistiller()
    {
        super(new ResourceLocation(ITLib.MODID, "multiblocks/solar_tower"), new BlockPos(0,0,0), new BlockPos(1,2,1), new BlockPos(3,21,3), ITMultiblockProvider.SOLAR_TOWER);
    }

    @Override
    public float getManualScale() {
        return 16;
    }

    @Override
    public void disassemble(Level world, BlockPos origin, boolean mirrored, Direction clickDirectionAtCreation)
    {
        super.disassemble(world, origin, mirrored, clickDirectionAtCreation);
    }

    @Override
    public void initializeClient(Consumer<ClientMultiblocks.MultiblockManualData> consumer)
    {
        consumer.accept(new ITClientMultiblockProperties(this, 0, 0, 0));
    }
}

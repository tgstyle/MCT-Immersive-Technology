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

@SuppressWarnings("all")
public class ITAlternator extends ITTemplateMultiblock
{
    public static final ITAlternator INSTANCE = new ITAlternator();
    public ITAlternator()
    {
        super(new ResourceLocation(ITLib.MODID, "multiblocks/alternator"), new BlockPos(0,0,0), new BlockPos(1,1,3), new BlockPos(3,3,4), ITMultiblockProvider.ALTERNATOR);
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

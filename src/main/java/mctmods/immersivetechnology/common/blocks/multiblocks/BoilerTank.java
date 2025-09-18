package mctmods.immersivetechnology.common.blocks.multiblocks;

import blusunrize.immersiveengineering.api.multiblocks.ClientMultiblocks;
import mctmods.immersivetechnology.common.blocks.multiblocks.helper.ITClientMultiblockProperties;
import mctmods.immersivetechnology.common.blocks.multiblocks.helper.ITTemplateMultiblock;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.registration.ITMultiblockProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class BoilerTank extends ITTemplateMultiblock {
    public static final BoilerTank INSTANCE = new BoilerTank();

    public BoilerTank() { super(ResourceLocation.fromNamespaceAndPath(ITLib.MODID, "multiblocks/boiler_tank"), new BlockPos(2,1,2), new BlockPos(2,1,2), new BlockPos(4,3,3), ITMultiblockProvider.BOILER_TANK); }

    @Override
    public void disassemble(Level world, BlockPos origin, boolean mirrored, Direction clickDirectionAtCreation) { super.disassemble(world, origin, mirrored, clickDirectionAtCreation); }

    @Override
    public float getManualScale() { return 16; }

    @Override
    public void initializeClient(Consumer<ClientMultiblocks.MultiblockManualData> consumer) { consumer.accept(new ITClientMultiblockProperties(this, 2, 1, 2)); }
}

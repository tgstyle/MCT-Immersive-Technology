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

public class SolarReflector extends ITTemplateMultiblock {
    public static final SolarReflector INSTANCE = new SolarReflector();

    public SolarReflector() { super(ResourceLocation.fromNamespaceAndPath(ITLib.MODID, "multiblocks/solar_reflector"), new BlockPos(0,0,0), new BlockPos(1,1,1), new BlockPos(3,3,3), ITMultiblockProvider.SOLAR_REFLECTOR); }

    @Override
    public float getManualScale() { return 16; }

    @Override
    public void disassemble(Level world, BlockPos origin, boolean mirrored, Direction clickDirectionAtCreation) { super.disassemble(world, origin, mirrored, clickDirectionAtCreation); }

    @Override
    public void initializeClient(Consumer<ClientMultiblocks.MultiblockManualData> consumer) { consumer.accept(new ITClientMultiblockProperties(this, 0, 0, 0)); }
}

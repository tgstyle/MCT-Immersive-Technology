package mctmods.immersivetechnology.common.blocks.multiblocks;

import blusunrize.immersiveengineering.api.multiblocks.ClientMultiblocks;
import mctmods.immersivetechnology.common.blocks.multiblocks.helper.ITClientMultiblockProperties;
import mctmods.immersivetechnology.common.blocks.multiblocks.helper.ITTemplateMultiblock;
import mctmods.immersivetechnology.common.blocks.multiblocks.shapes.BoilerSolidShape;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.registration.ITMultiblockProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class BoilerSolid extends ITTemplateMultiblock {
    public static final BoilerSolid INSTANCE = new BoilerSolid();

    public BoilerSolid() { super(ResourceLocation.fromNamespaceAndPath(ITLib.MODID, "multiblocks/boiler_solid"), BoilerSolidShape.MASTER_POS, BoilerSolidShape.TRIGGER_POS, new BlockPos(BoilerSolidShape.WIDTH,BoilerSolidShape.HEIGHT,BoilerSolidShape.LENGTH), ITMultiblockProvider.BOILER_SOLID); }

    @Override
    public void disassemble(Level world, BlockPos origin, boolean mirrored, Direction clickDirectionAtCreation) { super.disassemble(world, origin, mirrored, clickDirectionAtCreation); }

    @Override
    public float getManualScale() { return BoilerSolidShape.MANUAL_SCALE; }

    @Override
    public void initializeClient(Consumer<ClientMultiblocks.MultiblockManualData> consumer) { consumer.accept(new ITClientMultiblockProperties(this, BoilerSolidShape.CLIENT_OFFSET.getX(), BoilerSolidShape.CLIENT_OFFSET.getY(), BoilerSolidShape.CLIENT_OFFSET.getZ())); }
}

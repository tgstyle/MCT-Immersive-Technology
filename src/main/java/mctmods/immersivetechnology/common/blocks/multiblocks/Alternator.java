package mctmods.immersivetechnology.common.blocks.multiblocks;

import blusunrize.immersiveengineering.api.multiblocks.ClientMultiblocks;
import mctmods.immersivetechnology.common.blocks.multiblocks.helper.ITClientMultiblockProperties;
import mctmods.immersivetechnology.common.blocks.multiblocks.helper.ITTemplateMultiblock;
import mctmods.immersivetechnology.common.blocks.multiblocks.shapes.AlternatorShape;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.registration.ITMultiblockProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class Alternator extends ITTemplateMultiblock {
    public static final Alternator INSTANCE = new Alternator();

    public Alternator() { super(ResourceLocation.fromNamespaceAndPath(ITLib.MODID, "multiblocks/alternator"), AlternatorShape.MASTER_POS, AlternatorShape.TRIGGER_POS, new BlockPos(AlternatorShape.WIDTH,AlternatorShape.HEIGHT,AlternatorShape.LENGTH), ITMultiblockProvider.ALTERNATOR); }

    @Override
    public void disassemble(Level world, BlockPos origin, boolean mirrored, Direction clickDirectionAtCreation) { super.disassemble(world, origin, mirrored, clickDirectionAtCreation); }

    @Override
    public float getManualScale() { return AlternatorShape.MANUAL_SCALE; }

    @Override
    public void initializeClient(Consumer<ClientMultiblocks.MultiblockManualData> consumer) { consumer.accept(new ITClientMultiblockProperties(this, AlternatorShape.CLIENT_OFFSET.getX(), AlternatorShape.CLIENT_OFFSET.getY(), AlternatorShape.CLIENT_OFFSET.getZ())); }
}

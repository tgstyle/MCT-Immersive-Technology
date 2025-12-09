package mctmods.immersivetechnology.common.multiblocks.metal;

import blusunrize.immersiveengineering.api.multiblocks.ClientMultiblocks;
import mctmods.immersivetechnology.common.multiblocks.helper.ITClientMultiblockProperties;
import mctmods.immersivetechnology.common.multiblocks.helper.ITTemplateMultiblock;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.DistillerShape;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.registration.ITMultiblockProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class Distiller extends ITTemplateMultiblock {
    public static final Distiller INSTANCE = new Distiller();

    public Distiller() { super(ITLib.rl("multiblocks/distiller"), DistillerShape.MASTER_POS, DistillerShape.TRIGGER_POS, new BlockPos(DistillerShape.WIDTH,DistillerShape.HEIGHT,DistillerShape.LENGTH), ITMultiblockProvider.DISTILLER); }

    @Override
    public void disassemble(Level world, BlockPos origin, boolean mirrored, Direction clickDirectionAtCreation) { super.disassemble(world, origin, mirrored, clickDirectionAtCreation); }

    @Override
    public float getManualScale() { return DistillerShape.MANUAL_SCALE; }

    @Override
    public void initializeClient(Consumer<ClientMultiblocks.MultiblockManualData> consumer) { consumer.accept(new ITClientMultiblockProperties(this, DistillerShape.CLIENT_OFFSET.getX(), DistillerShape.CLIENT_OFFSET.getY(), DistillerShape.CLIENT_OFFSET.getZ())); }
}

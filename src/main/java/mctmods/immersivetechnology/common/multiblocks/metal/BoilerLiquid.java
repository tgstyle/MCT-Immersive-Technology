package mctmods.immersivetechnology.common.multiblocks.metal;

import blusunrize.immersiveengineering.api.multiblocks.ClientMultiblocks;
import mctmods.immersivetechnology.common.multiblocks.helper.ITClientMultiblockProperties;
import mctmods.immersivetechnology.common.multiblocks.helper.ITTemplateMultiblock;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.BoilerLiquidShape;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.registration.ITMultiblockProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class BoilerLiquid extends ITTemplateMultiblock {
    public static final BoilerLiquid INSTANCE = new BoilerLiquid();

    public BoilerLiquid() { super(ResourceLocation.fromNamespaceAndPath(ITLib.MODID, "multiblocks/boiler_liquid"), BoilerLiquidShape.MASTER_POS, BoilerLiquidShape.TRIGGER_POS, new BlockPos(BoilerLiquidShape.WIDTH,BoilerLiquidShape.HEIGHT,BoilerLiquidShape.LENGTH), ITMultiblockProvider.BOILER_LIQUID); }

    @Override
    public void disassemble(Level world, BlockPos origin, boolean mirrored, Direction clickDirectionAtCreation) { super.disassemble(world, origin, mirrored, clickDirectionAtCreation); }

    @Override
    public float getManualScale() { return BoilerLiquidShape.MANUAL_SCALE; }

    @Override
    public void initializeClient(Consumer<ClientMultiblocks.MultiblockManualData> consumer) { consumer.accept(new ITClientMultiblockProperties(this, BoilerLiquidShape.CLIENT_OFFSET.getX(), BoilerLiquidShape.CLIENT_OFFSET.getY(), BoilerLiquidShape.CLIENT_OFFSET.getZ())); }
}

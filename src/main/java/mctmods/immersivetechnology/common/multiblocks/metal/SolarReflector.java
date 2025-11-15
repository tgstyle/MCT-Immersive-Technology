package mctmods.immersivetechnology.common.multiblocks.metal;

import blusunrize.immersiveengineering.api.multiblocks.ClientMultiblocks;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MultiblockOrientation;
import mctmods.immersivetechnology.common.multiblocks.helper.ITClientMultiblockProperties;
import mctmods.immersivetechnology.common.multiblocks.helper.ITTemplateMultiblock;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.SolarReflectorLogic;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.SolarReflectorShape;
import mctmods.immersivetechnology.common.util.solarregistry.SolarRegistry;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.registration.ITMultiblockProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class SolarReflector extends ITTemplateMultiblock {
    public static final SolarReflector INSTANCE = new SolarReflector();

    public SolarReflector() { super(ResourceLocation.fromNamespaceAndPath(ITLib.MODID, "multiblocks/solar_reflector"), SolarReflectorShape.MASTER_POS, SolarReflectorShape.TRIGGER_POS, new BlockPos(SolarReflectorShape.WIDTH,SolarReflectorShape.HEIGHT,SolarReflectorShape.LENGTH), ITMultiblockProvider.SOLAR_REFLECTOR); }

    @Override
    public void disassemble(Level world, BlockPos origin, boolean mirrored, Direction clickDirectionAtCreation) {
        MultiblockOrientation orientation = new MultiblockOrientation(clickDirectionAtCreation, mirrored);
        BlockPos base = origin.offset(orientation.getAbsoluteOffset(SolarReflectorLogic.LINK_POI));
        SolarRegistry.unregisterReflector(world, base);
        super.disassemble(world, origin, mirrored, clickDirectionAtCreation);
    }

    @Override
    public float getManualScale() { return SolarReflectorShape.MANUAL_SCALE; }

    @Override
    public void initializeClient(Consumer<ClientMultiblocks.MultiblockManualData> consumer) { consumer.accept(new ITClientMultiblockProperties(this, SolarReflectorShape.CLIENT_OFFSET.getX(), SolarReflectorShape.CLIENT_OFFSET.getY(), SolarReflectorShape.CLIENT_OFFSET.getZ())); }

    @Override
    public boolean canBeMirrored() { return false; }
}

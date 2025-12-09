package mctmods.immersivetechnology.common.multiblocks.stone;

import blusunrize.immersiveengineering.api.multiblocks.ClientMultiblocks;
import mctmods.immersivetechnology.common.multiblocks.helper.ITClientMultiblockProperties;
import mctmods.immersivetechnology.common.multiblocks.helper.ITTemplateMultiblock;
import mctmods.immersivetechnology.common.multiblocks.stone.shapes.CoolingTowerShape;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.registration.ITMultiblockProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class CoolingTower extends ITTemplateMultiblock {
    public static final CoolingTower INSTANCE = new CoolingTower();

    public CoolingTower() { super(ITLib.rl("multiblocks/cooling_tower"), CoolingTowerShape.MASTER_POS, CoolingTowerShape.TRIGGER_POS, new BlockPos(CoolingTowerShape.WIDTH,CoolingTowerShape.HEIGHT,CoolingTowerShape.LENGTH), ITMultiblockProvider.COOLING_TOWER); }

    @Override
    public void disassemble(Level world, BlockPos origin, boolean mirrored, Direction clickDirectionAtCreation) { super.disassemble(world, origin, mirrored, clickDirectionAtCreation); }

    @Override
    public float getManualScale() { return CoolingTowerShape.MANUAL_SCALE; }

    @Override
    public void initializeClient(Consumer<ClientMultiblocks.MultiblockManualData> consumer) { consumer.accept(new ITClientMultiblockProperties(this, CoolingTowerShape.CLIENT_OFFSET.getX(), CoolingTowerShape.CLIENT_OFFSET.getY(), CoolingTowerShape.CLIENT_OFFSET.getZ())); }

    @Override
    public boolean canBeMirrored() { return false; }
}

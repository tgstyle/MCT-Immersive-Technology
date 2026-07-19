package mctmods.immersivetechnology.common.multiblocks.metal;

import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MultiblockOrientation;
import mctmods.immersivetechnology.common.blocks.helper.ITProperties;
import mctmods.immersivetechnology.common.multiblocks.helper.ITTemplateMultiblock;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.SolarTowerLogic;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.SolarTowerShape;
import mctmods.immersivetechnology.core.network.ITOSDSyncBlock;
import mctmods.immersivetechnology.core.network.ITPacketHandler;
import mctmods.immersivetechnology.core.util.solarregistry.SolarRegistry;
import mctmods.immersivetechnology.core.util.TranslationKey;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.registration.ITMultiblockRegistry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class SolarTower extends ITTemplateMultiblock {
    public static final SolarTower INSTANCE = new SolarTower();

    public SolarTower() { super(ITLib.rl("multiblocks/solar_tower"), SolarTowerShape.MASTER_POS, SolarTowerShape.TRIGGER_POS, new BlockPos(SolarTowerShape.WIDTH,SolarTowerShape.HEIGHT,SolarTowerShape.LENGTH), SolarTowerShape.CLIENT_OFFSET, SolarTowerShape.MANUAL_SCALE, ITMultiblockRegistry.SOLAR_TOWER); }

    @Override public boolean createStructure(Level world, BlockPos pos, Direction side, Player player) {
        if (world.isClientSide) { return false; }
        boolean formed = super.createStructure(world, pos, side, player);
        if (formed) {
            BlockState placed = world.getBlockState(pos);
            Direction front = placed.hasProperty(ITProperties.FACING_HORIZONTAL) ? placed.getValue(ITProperties.FACING_HORIZONTAL) : side.getOpposite();
            boolean mirrored = placed.hasProperty(ITProperties.MIRRORED) && placed.getValue(ITProperties.MIRRORED);
            MultiblockOrientation orientation = new MultiblockOrientation(front, mirrored);
            BlockPos origin = pos.subtract(orientation.getAbsoluteOffset(getTriggerOffset()));
            BlockPos base = origin.offset(orientation.getAbsoluteOffset(SolarTowerLogic.LINK_POI));
            SolarRegistry.RegisterResult result = SolarRegistry.registerTower(world, base);
            if (!result.success) {
                TranslationKey key = result.vertical ? TranslationKey.SOLAR_VERTICAL_STACK : TranslationKey.SOLAR_TOO_CLOSE;
                int dist = result.vertical ? -1 : result.requiredMove;
                ITPacketHandler.sendToPlayer(player, new ITOSDSyncBlock(key.name(), dist));
                disassemble(world, origin, mirrored, front);
                return false;
            }
        }
        return formed;
    }

    @Override public void disassemble(Level world, BlockPos origin, boolean mirrored, Direction clickDirectionAtCreation) {
        MultiblockOrientation orientation = new MultiblockOrientation(clickDirectionAtCreation, mirrored);
        BlockPos base = origin.offset(orientation.getAbsoluteOffset(SolarTowerLogic.LINK_POI));
        SolarRegistry.unregisterTower(world, base);
        super.disassemble(world, origin, mirrored, clickDirectionAtCreation);
    }
}

package mctmods.immersivetechnology.common.multiblocks.metal;

import com.immersiveconvergence.api.block.ModProperties;
import com.immersiveconvergence.api.multiblock.MachineTemplateMultiblock;
import com.immersiveconvergence.api.multiblock.ShapeData;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.SolarTowerLogic;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.core.network.OSDSyncBlock;
import mctmods.immersivetechnology.core.network.PacketHandler;
import mctmods.immersivetechnology.core.util.solarregistry.SolarRegistry;
import mctmods.immersivetechnology.core.util.TranslationKey;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;

import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MultiblockOrientation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class SolarTower extends MachineTemplateMultiblock {
    private static final ShapeData SHAPE = ITShapes.get("solar_tower");
    public static final SolarTower INSTANCE = new SolarTower();

    public SolarTower() { super(Reference.rl("multiblocks/solar_tower"), SHAPE.masterPos, SHAPE.triggerPos, new BlockPos(SHAPE.width,SHAPE.height,SHAPE.length), SHAPE.clientOffset, SHAPE.manualScale, MultiblockRegistry.SOLAR_TOWER); }

    @Override public boolean createStructure(Level world, BlockPos pos, Direction side, Player player) {
        if (world.isClientSide) { return false; }
        boolean formed = super.createStructure(world, pos, side, player);
        if (formed) {
            BlockState placed = world.getBlockState(pos);
            Direction front = placed.hasProperty(ModProperties.FACING_HORIZONTAL) ? placed.getValue(ModProperties.FACING_HORIZONTAL) : side.getOpposite();
            boolean mirrored = placed.hasProperty(ModProperties.MIRRORED) && placed.getValue(ModProperties.MIRRORED);
            MultiblockOrientation orientation = new MultiblockOrientation(front, mirrored);
            BlockPos origin = pos.subtract(orientation.getAbsoluteOffset(getTriggerOffset()));
            BlockPos base = origin.offset(orientation.getAbsoluteOffset(SolarTowerLogic.LINK_POI));
            SolarRegistry.RegisterResult result = SolarRegistry.registerTower(world, base);
            if (!result.success) {
                TranslationKey key = result.vertical ? TranslationKey.SOLAR_VERTICAL_STACK : TranslationKey.SOLAR_TOO_CLOSE;
                int dist = result.vertical ? -1 : result.requiredMove;
                PacketHandler.sendToPlayer(player, new OSDSyncBlock(key.name(), dist));
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

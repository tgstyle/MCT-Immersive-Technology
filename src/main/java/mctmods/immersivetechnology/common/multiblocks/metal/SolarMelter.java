package mctmods.immersivetechnology.common.multiblocks.metal;

import blusunrize.immersiveengineering.api.multiblocks.ClientMultiblocks;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MultiblockOrientation;
import mctmods.immersivetechnology.common.blocks.helper.ITProperties;
import mctmods.immersivetechnology.common.multiblocks.helper.ITClientMultiblockProperties;
import mctmods.immersivetechnology.common.multiblocks.helper.ITTemplateMultiblock;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.SolarMelterLogic;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.SolarMelterShape;
import mctmods.immersivetechnology.core.network.ITOSDSyncBlock;
import mctmods.immersivetechnology.core.network.ITPacketHandler;
import mctmods.immersivetechnology.core.util.solarregistry.SolarRegistry;
import mctmods.immersivetechnology.core.util.TranslationKey;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.registration.ITMultiblockProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Consumer;

public class SolarMelter extends ITTemplateMultiblock {
    public static final SolarMelter INSTANCE = new SolarMelter();

    public SolarMelter() { super(ITLib.rl("multiblocks/solar_melter"), SolarMelterShape.MASTER_POS, SolarMelterShape.TRIGGER_POS, new BlockPos(SolarMelterShape.WIDTH,SolarMelterShape.HEIGHT,SolarMelterShape.LENGTH), ITMultiblockProvider.SOLAR_MELTER); }

    @Override public boolean createStructure(Level world, BlockPos pos, Direction side, Player player) {
        if (world.isClientSide) { return false; }
        boolean formed = super.createStructure(world, pos, side, player);
        if (formed) {
            BlockState placed = world.getBlockState(pos);
            Direction front = placed.hasProperty(ITProperties.FACING_HORIZONTAL) ? placed.getValue(ITProperties.FACING_HORIZONTAL) : side.getOpposite();
            boolean mirrored = placed.hasProperty(ITProperties.MIRRORED) && placed.getValue(ITProperties.MIRRORED);
            MultiblockOrientation orientation = new MultiblockOrientation(front, mirrored);
            BlockPos origin = pos.subtract(orientation.getAbsoluteOffset(getTriggerOffset()));
            BlockPos base = origin.offset(orientation.getAbsoluteOffset(SolarMelterLogic.LINK_POI));
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
        BlockPos base = origin.offset(orientation.getAbsoluteOffset(SolarMelterLogic.LINK_POI));
        SolarRegistry.unregisterTower(world, base);
        super.disassemble(world, origin, mirrored, clickDirectionAtCreation);
    }

    @Override public float getManualScale() { return SolarMelterShape.MANUAL_SCALE; }

    @Override public void initializeClient(Consumer<ClientMultiblocks.MultiblockManualData> consumer) { consumer.accept(new ITClientMultiblockProperties(this, SolarMelterShape.CLIENT_OFFSET.getX(), SolarMelterShape.CLIENT_OFFSET.getY(), SolarMelterShape.CLIENT_OFFSET.getZ())); }

    @Override public boolean canBeMirrored() { return true; }
}

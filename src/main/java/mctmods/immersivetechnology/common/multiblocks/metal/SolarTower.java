package mctmods.immersivetechnology.common.multiblocks.metal;

import blusunrize.immersiveengineering.api.multiblocks.ClientMultiblocks;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MultiblockOrientation;
import mctmods.immersivetechnology.common.multiblocks.helper.ITClientMultiblockProperties;
import mctmods.immersivetechnology.common.multiblocks.helper.ITTemplateMultiblock;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.SolarMelterLogic;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.SolarTowerLogic;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.SolarTowerShape;
import mctmods.immersivetechnology.common.network.ITOSDSyncBlock;
import mctmods.immersivetechnology.common.network.ITPacketHandler;
import mctmods.immersivetechnology.common.util.solarregistry.SolarRegistry;
import mctmods.immersivetechnology.common.util.TranslationKey;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.registration.ITMultiblockProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class SolarTower extends ITTemplateMultiblock {
    public static final SolarTower INSTANCE = new SolarTower();

    public SolarTower() { super(ITLib.rl("multiblocks/solar_tower"), SolarTowerShape.MASTER_POS, SolarTowerShape.TRIGGER_POS, new BlockPos(SolarTowerShape.WIDTH,SolarTowerShape.HEIGHT,SolarTowerShape.LENGTH), ITMultiblockProvider.SOLAR_TOWER); }

    @Override
    public boolean createStructure(Level world, BlockPos pos, Direction side, Player player) {
        if (world.isClientSide) return false;
        Direction front = player.getDirection();
        boolean mirrored = false;
        MultiblockOrientation orientation = new MultiblockOrientation(front, mirrored);
        boolean formed = super.createStructure(world, pos, side, player);
        if (formed) {
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

    @Override
    public void disassemble(Level world, BlockPos origin, boolean mirrored, Direction clickDirectionAtCreation) {
        MultiblockOrientation orientation = new MultiblockOrientation(clickDirectionAtCreation, mirrored);
        BlockPos base = origin.offset(orientation.getAbsoluteOffset(SolarTowerLogic.LINK_POI));
        SolarRegistry.unregisterTower(world, base);
        super.disassemble(world, origin, mirrored, clickDirectionAtCreation);
    }

    @Override
    public float getManualScale() { return SolarTowerShape.MANUAL_SCALE; }

    @Override
    public void initializeClient(Consumer<ClientMultiblocks.MultiblockManualData> consumer) { consumer.accept(new ITClientMultiblockProperties(this, SolarTowerShape.CLIENT_OFFSET.getX(), SolarTowerShape.CLIENT_OFFSET.getY(), SolarTowerShape.CLIENT_OFFSET.getZ())); }

    @Override
    public boolean canBeMirrored() { return false; }
}

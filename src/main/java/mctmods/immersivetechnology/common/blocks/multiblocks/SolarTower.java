package mctmods.immersivetechnology.common.blocks.multiblocks;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.multiblocks.BlockMatcher;
import blusunrize.immersiveengineering.api.multiblocks.ClientMultiblocks;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MultiblockOrientation;
import blusunrize.immersiveengineering.api.utils.DirectionUtils;
import mctmods.immersivetechnology.common.blocks.multiblocks.helper.ITClientMultiblockProperties;
import mctmods.immersivetechnology.common.blocks.multiblocks.helper.ITTemplateMultiblock;
import mctmods.immersivetechnology.common.blocks.multiblocks.logic.SolarTowerLogic;
import mctmods.immersivetechnology.common.blocks.multiblocks.shapes.SolarTowerShape;
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
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

import java.util.List;
import java.util.function.Consumer;

public class SolarTower extends ITTemplateMultiblock {
    public static final SolarTower INSTANCE = new SolarTower();

    public SolarTower() { super(ResourceLocation.fromNamespaceAndPath(ITLib.MODID, "multiblocks/solar_tower"), SolarTowerShape.MASTER_POS, SolarTowerShape.TRIGGER_POS, new BlockPos(SolarTowerShape.WIDTH,SolarTowerShape.HEIGHT,SolarTowerShape.LENGTH), ITMultiblockProvider.SOLAR_TOWER); }

    @Override
    public boolean createStructure(Level world, BlockPos pos, Direction side, Player player) {
        if (world.isClientSide) return false;
        Direction front = player.getDirection().getOpposite();
        boolean mirrored = false;
        MultiblockOrientation orientation = new MultiblockOrientation(front, mirrored);
        BlockPos origin = pos.subtract(orientation.getAbsoluteOffset(getTriggerOffset()));
        BlockPos base = origin.offset(orientation.getAbsoluteOffset(SolarTowerLogic.LINK_POI));
        SolarRegistry.RegisterResult result = SolarRegistry.registerTower(world, base);
        if (!result.success) {
            if (canForm(world, origin, orientation)) {
                TranslationKey key = result.vertical ? TranslationKey.SOLAR_VERTICAL_STACK : TranslationKey.SOLAR_TOO_CLOSE;
                int dist = result.vertical ? -1 : result.requiredMove;
                ITPacketHandler.sendToPlayer(player, new ITOSDSyncBlock(key.name(), dist));
            }
            return false;
        }
        boolean formed = super.createStructure(world, pos, side, player);
        if (!formed) { SolarRegistry.unregisterTower(world, base); }
        return formed;
    }

    @SuppressWarnings("deprecation")
    private boolean canForm(Level world, BlockPos origin, MultiblockOrientation orientation) {
        Rotation rot = DirectionUtils.getRotationBetweenFacings(Direction.NORTH, orientation.front());
        if (rot == null) return false;
        StructurePlaceSettings placeSet = new StructurePlaceSettings()
                .setMirror(orientation.mirrored() ? Mirror.FRONT_BACK : Mirror.NONE)
                .setRotation(rot);
        List<StructureBlockInfo> structure = getStructure(world);
        for (StructureBlockInfo info : structure) {
            BlockPos realRelPos = StructureTemplate.calculateRelativePosition(placeSet, info.pos());
            BlockPos here = origin.offset(realRelPos);
            BlockState expected = info.state().mirror(placeSet.getMirror()).rotate(placeSet.getRotation());
            BlockState inWorld = world.getBlockState(here);
            if (!BlockMatcher.matches(expected, inWorld, world, here, additionalPredicates).isAllow()) { return false; }
        }
        return true;
    }

    @Override
    public void disassemble(Level world, BlockPos origin, boolean mirrored, Direction clickDirectionAtCreation) {
        BlockState masterState = world.getBlockState(origin);
        if (masterState.getBlock() != getBlock()) { super.disassemble(world, origin, mirrored, clickDirectionAtCreation); return; }
        Direction facing = masterState.getValue(IEProperties.FACING_HORIZONTAL);
        boolean actualMirrored = masterState.getValue(IEProperties.MIRRORED);
        MultiblockOrientation orientation = new MultiblockOrientation(facing, actualMirrored);
        BlockPos base = origin.offset(orientation.getAbsoluteOffset(SolarTowerLogic.LINK_POI));
        SolarRegistry.unregisterTower(world, base);
        super.disassemble(world, origin, actualMirrored, facing);
    }

    @Override
    public float getManualScale() { return SolarTowerShape.MANUAL_SCALE; }

    @Override
    public void initializeClient(Consumer<ClientMultiblocks.MultiblockManualData> consumer) { consumer.accept(new ITClientMultiblockProperties(this, SolarTowerShape.CLIENT_OFFSET.getX(), SolarTowerShape.CLIENT_OFFSET.getY(), SolarTowerShape.CLIENT_OFFSET.getZ())); }
}

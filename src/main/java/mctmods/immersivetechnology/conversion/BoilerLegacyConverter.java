package mctmods.immersivetechnology.conversion;

import blusunrize.immersiveengineering.api.IEProperties;

import com.immersiveconvergence.api.multiblock.TemplateData;

import mctmods.immersivetechnology.common.ITContent;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.BoilerLiquidShape;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.BoilerTankShape;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityBoilerLiquidMaster;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityBoilerLiquidSlave;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityBoilerTankMaster;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityBoilerTankSlave;
import mctmods.immersivetechnology.common.multiblocks.metal.types.BlockType_MetalMultiblock1;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BoilerLegacyConverter {
    private static final int LEGACY_WIDTH = 5, LEGACY_HEIGHT = 3, LEGACY_MASTER_X = 2, LEGACY_MASTER_Y = 1, LEGACY_BURNER_X = 4;
    private static final double LEGACY_WORKING_HEAT = 12000.0;
    private static final double TARGET_HEAT = 600.0;

    public static void convert(TileEntityBoilerTankMaster master, NBTTagCompound legacyNbt) {
        World world = master.getWorld();
        BlockPos masterPos = master.getPos();
        EnumFacing facing = master.facing;
        boolean mirrored = master.mirrored;

        TemplateData tankTemplate = BoilerTankShape.SHAPE.template;
        for (int h = 0; h < tankTemplate.height; h++) for (int l = 0; l < tankTemplate.length; l++) for (int w = 0; w < tankTemplate.width; w++) {
            if (tankTemplate.getState(w, h, l) == null) { continue; }
            BlockPos worldPos = worldFromLegacyLocal(masterPos, facing, mirrored, w, h, l);
            TileEntity te = world.getTileEntity(worldPos);
            if (!(te instanceof TileEntityBoilerTankSlave)) { continue; }
            TileEntityBoilerTankSlave part = (TileEntityBoilerTankSlave)te;
            part.pos = h * (tankTemplate.width * tankTemplate.length) + l * tankTemplate.width + w;
            part.markDirty();
        }

        TemplateData liquidTemplate = BoilerLiquidShape.SHAPE.template;
        BlockPos liquidMasterPos = worldFromLegacyLocal(masterPos, facing, mirrored, LEGACY_BURNER_X, 1, 0);
        for (int h = 0; h < liquidTemplate.height; h++) for (int l = 0; l < liquidTemplate.length; l++) {
            BlockPos worldPos = worldFromLegacyLocal(masterPos, facing, mirrored, LEGACY_BURNER_X, h, l);
            if (world.getBlockState(worldPos).getBlock() != ITContent.blockMetalMultiblock) { continue; }
            TileEntity te = world.getTileEntity(worldPos);
            if (!(te instanceof TileEntityBoilerTankSlave)) { continue; }
            ((TileEntityBoilerTankSlave)te).formed = false;
            if (liquidTemplate.getState(0, h, l) == null) {
                if (h < LEGACY_HEIGHT) { world.setBlockToAir(worldPos); }
                continue;
            }
            boolean isMaster = worldPos.equals(liquidMasterPos);
            IBlockState state = ITContent.blockMetalMultiblock1.getStateFromMeta((isMaster ? BlockType_MetalMultiblock1.BOILER_LIQUID : BlockType_MetalMultiblock1.BOILER_LIQUID_SLAVE).getMeta()).withProperty(IEProperties.FACING_HORIZONTAL, facing).withProperty(IEProperties.MULTIBLOCKSLAVE, !isMaster);
            world.setBlockState(worldPos, state, 2);
            TileEntity converted = world.getTileEntity(worldPos);
            if (converted instanceof TileEntityBoilerLiquidSlave) {
                TileEntityBoilerLiquidSlave part = (TileEntityBoilerLiquidSlave)converted;
                part.facing = facing;
                part.formed = true;
                part.pos = h * liquidTemplate.length + l;
                part.offset = new int[]{worldPos.getX() - liquidMasterPos.getX(), worldPos.getY() - liquidMasterPos.getY(), worldPos.getZ() - liquidMasterPos.getZ()};
                part.mirrored = mirrored;
                if (isMaster) { ((TileEntityBoilerLiquidMaster)part).applyLegacyBoiler(legacyNbt, Math.min(legacyNbt.getDouble("heatLevel") / LEGACY_WORKING_HEAT, 1) * TARGET_HEAT); }
                part.markDirty();
                world.notifyBlockUpdate(worldPos, state, state, 2);
                world.addBlockEvent(worldPos, state.getBlock(), 255, 0);
            }
        }
        master.markDirty();
    }

    private static BlockPos worldFromLegacyLocal(BlockPos masterPos, EnumFacing facing, boolean mirrored, int x, int y, int z) {
        int dx = (mirrored ? LEGACY_WIDTH - 1 - x : x) - LEGACY_MASTER_X;
        int dy = y - LEGACY_MASTER_Y;
        switch (facing) {
            case SOUTH: return masterPos.add(-dx, dy, z);
            case NORTH: return masterPos.add(dx, dy, -z);
            case EAST: return masterPos.add(z, dy, dx);
            case WEST: return masterPos.add(-z, dy, -dx);
            default: return masterPos;
        }
    }
}

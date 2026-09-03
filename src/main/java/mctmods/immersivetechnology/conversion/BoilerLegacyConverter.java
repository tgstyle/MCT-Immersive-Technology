package mctmods.immersivetechnology.conversion;

import blusunrize.immersiveengineering.api.IEProperties;

import com.immersiveconvergence.api.multiblock.TemplateData;

import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.common.ITContent;
import mctmods.immersivetechnology.common.util.ITLogger;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityBoilerLiquidMaster;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityBoilerLiquidSlave;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityBoilerTankMaster;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityBoilerTankSlave;
import mctmods.immersivetechnology.common.multiblocks.metal.types.BlockType_MetalMultiblock1;
import mctmods.immersivetechnology.common.util.ITUtils;

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

    public static final String MISSING_ENTRY_NOTICE =
            "Immersive Technology: this is expected.\n"
            + "The Boiler is now a Boiler Tank plus a separate Liquid Boiler,\n"
            + "so the old entry no longer exists.\n"
            + "Boilers already placed in this world become a Liquid Boiler\n"
            + "the first time each one loads, keeping their fluids,\n"
            + "inventory and heat. Nothing needs to be rebuilt.\n\n";

    public static void logMissingEntries(String forgeText) {
        if (ITLogger.logger == null) { return; }
        StringBuilder entries = new StringBuilder();
        for (String line : forgeText.split("\n")) {
            String trimmed = line.trim();
            if (trimmed.startsWith(ImmersiveTechnology.MODID + ":")) { entries.append(entries.length() == 0 ? "" : ", ").append(trimmed); }
        }
        ITLogger.warn("This save reports missing registry entries for Immersive Technology" + (entries.length() == 0 ? "" : ": " + entries) + ".");
        ITLogger.warn("This is expected. The Boiler is now a Boiler Tank plus a separate Liquid Boiler, so the old entry no longer exists.");
        ITLogger.warn("Boilers already placed in this world become a Liquid Boiler the first time each one loads, keeping their fluids, inventory and heat. Nothing needs to be rebuilt.");
    }

    public static String annotate(String forgeText) {
        int listStart = forgeText.indexOf("Missing ");
        return listStart < 0 ? forgeText + "\n" + MISSING_ENTRY_NOTICE : forgeText.substring(0, listStart) + MISSING_ENTRY_NOTICE + forgeText.substring(listStart);
    }

    public static void convert(TileEntityBoilerTankMaster master, NBTTagCompound legacyNbt) {
        World world = master.getWorld();
        BlockPos masterPos = master.getPos();
        EnumFacing facing = master.facing;
        boolean mirrored = master.mirrored;

        TemplateData tankTemplate = ITShapes.get("boiler_tank").template;
        for (int h = 0; h < tankTemplate.height; h++) for (int l = 0; l < tankTemplate.length; l++) for (int w = 0; w < tankTemplate.width; w++) {
            if (tankTemplate.getState(w, h, l) == null) { continue; }
            BlockPos worldPos = worldFromLegacyLocal(masterPos, facing, mirrored, w, h, l);
            TileEntity te = world.getTileEntity(worldPos);
            if (!(te instanceof TileEntityBoilerTankSlave)) { continue; }
            TileEntityBoilerTankSlave part = (TileEntityBoilerTankSlave)te;
            part.pos = h * (tankTemplate.width * tankTemplate.length) + l * tankTemplate.width + w;
            part.markDirty();
        }

        TemplateData liquidTemplate = ITShapes.get("boiler_liquid").template;
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
            IBlockState state = ITUtils.stateOf(ITContent.blockMetalMultiblock1, isMaster ? BlockType_MetalMultiblock1.BOILER_LIQUID : BlockType_MetalMultiblock1.BOILER_LIQUID_SLAVE).withProperty(IEProperties.FACING_HORIZONTAL, facing).withProperty(IEProperties.MULTIBLOCKSLAVE, !isMaster);
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

package mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart;

import mctmods.immersivetechnology.common.ITContent;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntitySolarMelterSlave;
import mctmods.immersivetechnology.common.multiblocks.metal.types.BlockType_MetalMultiblock1;

import com.immersiveconvergence.api.ICLib;
import com.immersiveconvergence.api.util.ICUtils;
import com.immersiveconvergence.api.multiblock.MultiblockRegistry;
import com.immersiveconvergence.api.block.ICProperties;
import com.immersiveconvergence.api.multiblock.*;
import com.immersiveconvergence.api.multiblock.MachineTemplateMultiblock;
import mctmods.immersivetechnology.common.util.solarregistry.SolarRegistry;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class TileEntityITMultiblockPartSolarMelter extends MachineTemplateMultiblock<TileEntitySolarMelterSlave> {
    public static TileEntityITMultiblockPartSolarMelter instance = new TileEntityITMultiblockPartSolarMelter();

    public TileEntityITMultiblockPartSolarMelter() { super("IT:SolarMelter", ITShapes.get("solar_melter"), ICUtils.stateOf(ITContent.blockMetalMultiblock1, BlockType_MetalMultiblock1.SOLAR_MELTER), ICUtils.stateOf(ITContent.blockMetalMultiblock1, BlockType_MetalMultiblock1.SOLAR_MELTER_SLAVE), 1.1, 3.75, 2.125, 8); }

    @Override public boolean createStructure(World world, BlockPos pos, EnumFacing side, EntityPlayer player) {
        side = (side == EnumFacing.UP || side == EnumFacing.DOWN)? EnumFacing.fromAngle(player.rotationYaw) : side.getOpposite();
        boolean mirror;
        if (isValid(world, pos, side, triggerPos, false)) { mirror = false; }
        else if (isValid(world, pos, side, triggerPos, true)) { mirror = true; }
        else { return false; }
        BlockPos origin = pos.offset(side, -masterZ).offset(side.rotateY(), mirror ? -(width - 1 - masterX) : -masterX).offset(EnumFacing.DOWN, masterY);
        BlockPos link = null;
        for (PoIJSONSchema poi : pointsOfInterest) {
            if ("link0".equals(poi.name)) { link = poi.position; break; }
        }
        if (link == null) { return false; }
        BlockPos basePos = localToWorld(origin, mirror ? (width - 1 - link.getX()) : link.getX(), link.getY(), link.getZ(), side);
        SolarRegistry.RegisterResult result = SolarRegistry.canRegisterTower(world, basePos);
        if (!result.success) {
            if (result.vertical) { player.sendMessage(new TextComponentTranslation("chat.immersivetech.solar_tower_vertical_fail")); }
            else if (result.requiredMove > 0) { player.sendMessage(new TextComponentTranslation("chat.immersivetech.solar_tower_too_close", result.requiredMove)); }
            return false;
        }
        BlockPos masterPos = localToWorld(origin, mirror ? (width - 1 - masterX) : masterX, masterY, masterZ, side);
        ItemStack hammer = player.getHeldItemMainhand().getItem().getToolClasses(player.getHeldItemMainhand()).contains(ICLib.TOOL_HAMMER)?player.getHeldItemMainhand(): player.getHeldItemOffhand();
        if (MultiblockRegistry.formationCancelled(player, this, pos, hammer)) return false;
        IBlockState masterState = masterBlockState.withProperty(ICProperties.FACING_HORIZONTAL, side).withProperty(ICProperties.MULTIBLOCKSLAVE, false);
        IBlockState slaveState = slaveBlockState.withProperty(ICProperties.FACING_HORIZONTAL, side).withProperty(ICProperties.MULTIBLOCKSLAVE, true);
        for (int h = 0; h < height; h++) for (int l = 0; l < length; l++) for (int w = 0; w < width; w++) {
            if (template.getState(w, h, l) == null) continue;
            int position = h * (width * length) + l * width + w;
            BlockPos pos2 = localToWorld(origin, mirror ? (width - 1 - w) : w, h, l, side);
            world.setBlockState(pos2, pos2.equals(masterPos) ? masterState : slaveState);
            TileEntitySolarMelterSlave tile = (TileEntitySolarMelterSlave)world.getTileEntity(pos2);
            if (tile != null) {
                tile.facing = side;
                tile.formed = true;
                tile.pos = position;
                tile.offset = new int[] { pos2.getX() - masterPos.getX(), pos2.getY() - masterPos.getY(), pos2.getZ() - masterPos.getZ() };
                tile.mirrored = mirror;
                tile.markDirty();
                tile.markContainingBlockForUpdate(null);
                world.addBlockEvent(pos2, slaveBlockState.getBlock(), 255, 0);
            }
        }
        MultiblockRegistry.formationDone(player, this, pos, hammer);
        return true;
    }
}

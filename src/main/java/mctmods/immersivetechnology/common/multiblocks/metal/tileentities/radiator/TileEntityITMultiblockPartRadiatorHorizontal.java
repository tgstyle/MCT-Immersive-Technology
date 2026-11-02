package mctmods.immersivetechnology.common.multiblocks.metal.tileentities.radiator;

import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.common.ITContent;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.common.multiblocks.metal.types.BlockType_MetalMultiblock1;

import com.immersiveconvergence.api.multiblock.MachineTemplateMultiblock;
import com.immersiveconvergence.api.util.ICUtils;
import java.util.Set;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityITMultiblockPartRadiatorHorizontal extends MachineTemplateMultiblock<TileEntityRadiatorSlave> {
    public static TileEntityITMultiblockPartRadiatorHorizontal instance = new TileEntityITMultiblockPartRadiatorHorizontal();
    private ItemStack[][][] horizontalManual;
    private Integer horizontalTriggerIndex;

    public TileEntityITMultiblockPartRadiatorHorizontal() { super("IT:RadiatorHorizontal", ITShapes.get("radiator"), ICUtils.stateOf(ITContent.blockMetalMultiblock1, BlockType_MetalMultiblock1.RADIATOR), ICUtils.stateOf(ITContent.blockMetalMultiblock1, BlockType_MetalMultiblock1.RADIATOR_SLAVE), 1.1, 3.75, 2.125, 8); }

    @Override public boolean isBlockTrigger(IBlockState state) { return TileEntityITMultiblockPartRadiator.instance.isBlockTrigger(state); }

    @Override public boolean createStructure(World world, BlockPos pos, EnumFacing side, EntityPlayer player) { return TileEntityITMultiblockPartRadiator.instance.createStructure(world, pos, side, player); }

    @Override @SideOnly(Side.CLIENT) public void renderFormedStructure() { ImmersiveTechnology.proxy.renderFormedRadiatorPart(masterBlockState, masterX, masterY, masterZ); }

    @Override public Set<BlockPos> worldOffsetsFromMaster(EnumFacing facing, boolean mirrored) { return TileEntityITMultiblockPartRadiator.instance.worldOffsetsFromMaster(facing, true); }

    @Override public ItemStack[][][] getStructureManual() {
        if (horizontalManual == null) {
            ItemStack[][][] vertical = TileEntityITMultiblockPartRadiator.instance.getStructureManual();
            ItemStack[][][] flat = new ItemStack[width][length][height];
            for (int h = 0; h < height; h++) {
                for (int l = 0; l < length; l++) {
                    for (int w = 0; w < width; w++) { flat[w][l][h] = vertical[h][l][w]; }
                }
            }
            horizontalManual = flat;
        }
        return horizontalManual;
    }

    @Override public int primaryTriggerRenderIndex() {
        if (horizontalTriggerIndex == null) {
            BlockPos trigger = primaryTrigger();
            ItemStack[][][] flat = getStructureManual();
            int found = -1;
            int index = 0;
            for (int h = 0; h < flat.length && found < 0; h++) {
                for (int l = 0; l < flat[h].length && found < 0; l++) {
                    for (int w = 0; w < flat[h][l].length; w++) {
                        if (flat[h][l][w].isEmpty()) { continue; }
                        if (trigger != null && trigger.getX() == h && trigger.getY() == w && trigger.getZ() == l) { found = index; break; }
                        index++;
                    }
                }
            }
            horizontalTriggerIndex = found;
        }
        return horizontalTriggerIndex;
    }
}

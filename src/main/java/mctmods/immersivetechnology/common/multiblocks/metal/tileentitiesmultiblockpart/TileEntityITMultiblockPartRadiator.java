package mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.MultiblockHandler;
import blusunrize.immersiveengineering.client.ClientUtils;

import com.immersiveconvergence.api.multiblock.*;

import mctmods.immersivetechnology.common.ITContent;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityRadiatorSlave;
import mctmods.immersivetechnology.common.multiblocks.metal.types.BlockType_MetalMultiblock1;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.RadiatorShape;
import mctmods.immersivetechnology.common.shared.tileentities.TileEntityITMultiblockPart;
import mctmods.immersivetechnology.common.util.ITUtils;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityITMultiblockPartRadiator extends TileEntityITMultiblockPart<TileEntityRadiatorSlave> {
    public static TileEntityITMultiblockPartRadiator instance = new TileEntityITMultiblockPartRadiator();

    @SideOnly(Side.CLIENT)
    static ItemStack renderStack;

    public TileEntityITMultiblockPartRadiator() { super("IT:Radiator", RadiatorShape.SHAPE, ITContent.blockMetalMultiblock1.getStateFromMeta(BlockType_MetalMultiblock1.RADIATOR.getMeta()), ITContent.blockMetalMultiblock1.getStateFromMeta(BlockType_MetalMultiblock1.RADIATOR_SLAVE.getMeta())); }

    @Override public boolean overwriteBlockRender(ItemStack stack, int iterator) { return false; }

    @Override public boolean canRenderFormedStructure() { return true; }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderFormedStructure() {
        if (renderStack == null) renderStack = new ItemStack(ITContent.blockMetalMultiblock1, 1, BlockType_MetalMultiblock1.RADIATOR.getMeta());
        GlStateManager.translate(0.1, 0.25, 0.125);
        GlStateManager.translate(1, 3.5, 2);
        GlStateManager.rotate(-45, 0, 1, 0);
        GlStateManager.rotate(-20, 1, 0, 0);
        GlStateManager.scale(8, 8, 8);
        ClientUtils.mc().getRenderItem().renderItem(renderStack, ItemCameraTransforms.TransformType.GUI);
    }

    private static final class Orientation {
        final boolean transposed;
        final int width, height, masterX, masterY, masterZ;
        final BlockPos origin;

        Orientation(boolean transposed, int width, int height, int masterX, int masterY, int masterZ, BlockPos origin) {
            this.transposed = transposed;
            this.width = width;
            this.height = height;
            this.masterX = masterX;
            this.masterY = masterY;
            this.masterZ = masterZ;
            this.origin = origin;
        }

        BlockPos worldPos(int w, int h, int l, EnumFacing side) {
            return ITUtils.LocalOffsetToWorldBlockPos(origin, transposed ? -w : w, h, l, side, transposed);
        }
    }

    private Orientation buildOrientation(boolean transposed, BlockPos pos, EnumFacing side) {
        int eff_width = transposed ? height : width;
        int eff_height = transposed ? width : height;
        int eff_masterX = transposed ? masterY : masterX;
        int eff_masterY = transposed ? masterX : masterY;
        int eff_masterZ = masterZ;
        BlockPos origin = pos.offset(side, -eff_masterZ).offset(side.rotateY(), -eff_masterX).offset(EnumFacing.DOWN, eff_masterY);
        return new Orientation(transposed, eff_width, eff_height, eff_masterX, eff_masterY, eff_masterZ, origin);
    }

    private Orientation resolveOrientation(World world, BlockPos pos, EnumFacing side) {
        Orientation normal = buildOrientation(false, pos, side);
        if (isValid(world, normal, side)) { return normal; }
        Orientation transposed = buildOrientation(true, pos, side);
        if (isValid(world, transposed, side)) { return transposed; }
        return null;
    }

    @Override public boolean createStructure(World world, BlockPos pos, EnumFacing side, EntityPlayer player) {
        side = (side == EnumFacing.UP || side == EnumFacing.DOWN) ? EnumFacing.fromAngle(player.rotationYaw) : side.getOpposite();

        Orientation orientation = resolveOrientation(world, pos, side);
        if (orientation == null) { return false; }

        BlockPos masterPos = ITUtils.LocalOffsetToWorldBlockPos(orientation.origin, orientation.transposed ? -orientation.masterX : orientation.masterX, orientation.masterY, orientation.masterZ, side, orientation.transposed);

        ItemStack hammer = player.getHeldItemMainhand().getItem().getToolClasses(player.getHeldItemMainhand()).contains(Lib.TOOL_HAMMER) ? player.getHeldItemMainhand() : player.getHeldItemOffhand();
        if (MultiblockHandler.fireMultiblockFormationEventPre(player, this, pos, hammer).isCanceled()) return false;

        IBlockState masterState = masterBlockState.withProperty(IEProperties.FACING_HORIZONTAL, side).withProperty(IEProperties.MULTIBLOCKSLAVE, false);
        IBlockState slaveState = slaveBlockState.withProperty(IEProperties.FACING_HORIZONTAL, side).withProperty(IEProperties.MULTIBLOCKSLAVE, true);

        for (int eff_h = 0; eff_h < orientation.height; eff_h++) for (int l = 0; l < length; l++) for (int eff_w = 0; eff_w < orientation.width; eff_w++) {
            int orig_h = orientation.transposed ? eff_w : eff_h;
            int orig_w = orientation.transposed ? eff_h : eff_w;
            if (template.getState(orig_w, orig_h, l) == null) continue;
            int position = orig_h * (width * length) + l * width + orig_w;
            BlockPos pos2 = orientation.worldPos(eff_w, eff_h, l, side);
            world.setBlockState(pos2, pos2.equals(masterPos) ? masterState : slaveState);
            TileEntityRadiatorSlave tile = (TileEntityRadiatorSlave)world.getTileEntity(pos2);
            if (tile != null) {
                tile.facing = side;
                tile.formed = true;
                tile.pos = position;
                tile.offset = new int[] { pos2.getX() - masterPos.getX(), pos2.getY() - masterPos.getY(), pos2.getZ() - masterPos.getZ() };
                tile.mirrored = orientation.transposed;
                tile.markDirty();
                tile.markContainingBlockForUpdate(null);
                world.addBlockEvent(pos2, slaveBlockState.getBlock(), 255, 0);
            }
        }
        MultiblockHandler.fireMultiblockFormationEventPost(player, this, pos, hammer);
        return true;
    }

    private boolean isValid(World world, Orientation orientation, EnumFacing side) {
        for (int eff_h = 0; eff_h < orientation.height; eff_h++) for (int l = 0; l < length; l++) for (int eff_w = 0; eff_w < orientation.width; eff_w++) {
            int orig_h = orientation.transposed ? eff_w : eff_h;
            int orig_w = orientation.transposed ? eff_h : eff_w;
            IBlockState expected = template.getState(orig_w, orig_h, l);
            if (expected == null) continue;
            BlockPos blockPos = orientation.worldPos(eff_w, eff_h, l, side);
            if (!BlockMatcher.matches(expected, world.getBlockState(blockPos))) return false;
        }
        return true;
    }
}

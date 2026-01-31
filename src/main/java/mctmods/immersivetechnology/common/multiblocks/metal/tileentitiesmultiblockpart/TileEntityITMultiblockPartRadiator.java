package mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.MultiblockHandler;
import blusunrize.immersiveengineering.client.ClientUtils;

import mctmods.immersivetechnology.common.ITContent;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityRadiatorSlave;
import mctmods.immersivetechnology.common.multiblocks.metal.types.BlockType_MetalMultiblock1;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.RadiatorShape;
import mctmods.immersivetechnology.common.shared.tileentities.TileEntityITMultiblockPart;
import mctmods.immersivetechnology.common.util.ITUtils;
import mctmods.immersivetechnology.common.util.multiblock.*;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityITMultiblockPartRadiator extends TileEntityITMultiblockPart<TileEntityRadiatorSlave> implements MultiblockHandler.IMultiblock {
    public static TileEntityITMultiblockPartRadiator instance = new TileEntityITMultiblockPartRadiator();

    @SideOnly(Side.CLIENT)
    static ItemStack renderStack;

    public TileEntityITMultiblockPartRadiator() {
        super(ITContent.blockMetalMultiblock1.getStateFromMeta(BlockType_MetalMultiblock1.RADIATOR.getMeta()),
                ITContent.blockMetalMultiblock1.getStateFromMeta(BlockType_MetalMultiblock1.RADIATOR_SLAVE.getMeta()));
        MultiblockJSONSchema data = RadiatorShape.DATA;
        if (data == null) return;
        this.uniqueName = data.uniqueName;
        this.width = data.width;
        this.height = data.height;
        this.length = data.length;
        this.pointsOfInterest = data.pointsOfInterest != null ? data.pointsOfInterest : new PoIJSONSchema[0];
        this.masterX = data.master.x;
        this.masterY = data.master.y;
        this.masterZ = data.master.z;
        this.structure = MultiblockUtils.GetStructure(data, width, length, height);
        this.materials = MultiblockUtils.GetMaterials(data);
        this.structureExport = MultiblockUtils.Convert(this.structure);
        if (data.master.mod.equals("ore")) { this.trigger = new OreDictRef(data.master.name); }
        else {
            Item item = Item.getByNameOrId(data.master.mod + ":" + data.master.name);
            if (item == null) throw new IllegalArgumentException(String.format("Invalid item %s:%s", data.master.mod, data.master.name));
            this.trigger = new ItemStackRef(new ItemStack(item, 1, data.master.meta));
        }
    }

    @Override public boolean overwriteBlockRender(ItemStack stack, int iterator) { return false; }

    @Override public float getManualScale() { return 6; }

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

    @Override public String getUniqueName() { return uniqueName; }

    @Override public boolean createStructure(World world, BlockPos pos, EnumFacing side, EntityPlayer player) {
        side = (side == EnumFacing.UP || side == EnumFacing.DOWN) ? EnumFacing.fromAngle(player.rotationYaw) : side.getOpposite();

        boolean mirrored = false;
        if (isInvalid(world, pos, side, false)) {
            mirrored = true;
            if (isInvalid(world, pos, side, true)) return false;
        }

        int eff_width = mirrored ? height : width;
        int eff_height = mirrored ? width : height;
        int eff_masterX = mirrored ? masterY : masterX;
        int eff_masterY = mirrored ? masterX : masterY;
        int eff_masterZ = masterZ;

        BlockPos origin = pos.offset(side, -eff_masterZ).offset(side.rotateY(), -eff_masterX).offset(EnumFacing.DOWN, eff_masterY);
        BlockPos masterPos = ITUtils.LocalOffsetToWorldBlockPos(origin, mirrored ? -eff_masterX : eff_masterX, eff_masterY, eff_masterZ, side, mirrored);

        ItemStack hammer = player.getHeldItemMainhand().getItem().getToolClasses(player.getHeldItemMainhand()).contains(Lib.TOOL_HAMMER) ? player.getHeldItemMainhand() : player.getHeldItemOffhand();
        if (MultiblockHandler.fireMultiblockFormationEventPre(player, this, pos, hammer).isCanceled()) return false;

        IBlockState masterState = masterBlockState.withProperty(IEProperties.FACING_HORIZONTAL, side).withProperty(IEProperties.MULTIBLOCKSLAVE, false);
        IBlockState slaveState = slaveBlockState.withProperty(IEProperties.FACING_HORIZONTAL, side).withProperty(IEProperties.MULTIBLOCKSLAVE, true);

        for (int eff_h = 0; eff_h < eff_height; eff_h++) for (int l = 0; l < length; l++) for (int eff_w = 0; eff_w < eff_width; eff_w++) {
            int orig_h = mirrored ? eff_w : eff_h;
            int orig_w = mirrored ? eff_h : eff_w;
            if (structure[orig_h][l][orig_w] == AirRef.instance) continue;
            int position = orig_h * (width * length) + l * width + orig_w;
            BlockPos pos2 = ITUtils.LocalOffsetToWorldBlockPos(origin, mirrored ? -eff_w : eff_w, eff_h, l, side, mirrored);
            world.setBlockState(pos2, pos2.equals(masterPos) ? masterState : slaveState);
            TileEntityRadiatorSlave tile = (TileEntityRadiatorSlave)world.getTileEntity(pos2);
            if (tile != null) {
                tile.facing = side;
                tile.formed = true;
                tile.pos = position;
                tile.offset = new int[] { pos2.getX() - masterPos.getX(), pos2.getY() - masterPos.getY(), pos2.getZ() - masterPos.getZ() };
                tile.mirrored = mirrored;
                tile.markDirty();
                tile.markContainingBlockForUpdate(null);
                world.addBlockEvent(pos2, slaveBlockState.getBlock(), 255, 0);
            }
        }
        MultiblockHandler.fireMultiblockFormationEventPost(player, this, pos, hammer);
        return true;
    }

    protected boolean isInvalid(World world, BlockPos pos, EnumFacing side, boolean mirrored) {
        int eff_width = mirrored ? height : width;
        int eff_height = mirrored ? width : height;
        int eff_masterX = mirrored ? masterY : masterX;
        int eff_masterY = mirrored ? masterX : masterY;
        int eff_masterZ = masterZ;
        BlockPos origin = pos.offset(side, -eff_masterZ).offset(side.rotateY(), -eff_masterX).offset(EnumFacing.DOWN, eff_masterY);
        for (int eff_h = 0; eff_h < eff_height; eff_h++) for (int l = 0; l < length; l++) for (int eff_w = 0; eff_w < eff_width; eff_w++) {
            int orig_h = mirrored ? eff_w : eff_h;
            int orig_w = mirrored ? eff_h : eff_w;
            if (structure[orig_h][l][orig_w] == AirRef.instance) continue;
            BlockPos blockPos = ITUtils.LocalOffsetToWorldBlockPos(origin, mirrored ? -eff_w : eff_w, eff_h, l, side, mirrored);
            IBlockState state = world.getBlockState(blockPos);
            ItemStack found = new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state));
            IRefComparable expected = structure[orig_h][l][orig_w];
            if (!expected.isEquals(found)) return true;
        }
        return false;
    }
}

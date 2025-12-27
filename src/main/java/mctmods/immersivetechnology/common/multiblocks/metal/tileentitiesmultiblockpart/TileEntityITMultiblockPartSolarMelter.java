package mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart;

import blusunrize.immersiveengineering.api.MultiblockHandler;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.client.ClientUtils;
import mctmods.immersivetechnology.common.ITContent;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.SolarMelterShape;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntitySolarMelterSlave;
import mctmods.immersivetechnology.common.multiblocks.types.BlockType_MetalMultiblock1;
import mctmods.immersivetechnology.common.shared.tileentities.TileEntityITMultiblockPart;
import mctmods.immersivetechnology.common.util.ITLogger;
import mctmods.immersivetechnology.common.util.ITUtils;
import mctmods.immersivetechnology.common.util.multiblock.*;
import mctmods.immersivetechnology.common.util.solarregistry.SolarRegistry;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityITMultiblockPartSolarMelter extends TileEntityITMultiblockPart<TileEntitySolarMelterSlave> implements MultiblockHandler.IMultiblock {
    public static TileEntityITMultiblockPartSolarMelter instance = new TileEntityITMultiblockPartSolarMelter();

    @SideOnly(Side.CLIENT)
    static ItemStack renderStack;

    public TileEntityITMultiblockPartSolarMelter() {
        super(ITContent.blockMetalMultiblock1.getStateFromMeta(BlockType_MetalMultiblock1.SOLAR_MELTER.getMeta()),
                ITContent.blockMetalMultiblock1.getStateFromMeta(BlockType_MetalMultiblock1.SOLAR_MELTER_SLAVE.getMeta()));
        MultiblockJSONSchema data = SolarMelterShape.DATA;
        if (data == null) { ITLogger.error("No data for solar_melter"); return; }
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

    @Override public float getManualScale() { return 4; }

    @Override public boolean canRenderFormedStructure() { return true; }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderFormedStructure() {
        if (renderStack == null) renderStack = new ItemStack(ITContent.blockMetalMultiblock1, 1, BlockType_MetalMultiblock1.SOLAR_MELTER.getMeta());
        GlStateManager.translate(0.1, 0.25, 0.125);
        GlStateManager.translate(1, 3.5, 2);
        GlStateManager.rotate(-45, 0, 1, 0);
        GlStateManager.rotate(-20, 1, 0, 0);
        GlStateManager.scale(8, 8, 8);
        ClientUtils.mc().getRenderItem().renderItem(renderStack, ItemCameraTransforms.TransformType.GUI);
    }

    @Override public boolean createStructure(World world, BlockPos pos, EnumFacing side, EntityPlayer player) {
        side = (side == EnumFacing.UP || side == EnumFacing.DOWN)? EnumFacing.fromAngle(player.rotationYaw) : side.getOpposite();
        boolean mirror = false;
        if (isInvalid(world, pos, side, false)) { mirror = true; if (isInvalid(world, pos, side, true)) return false; }
        BlockPos origin = pos.offset(side, -masterZ).offset(side.rotateY(), mirror ? width-1-masterX : -masterX).offset(EnumFacing.DOWN, masterY);
        int linkPosition = -1;
        for (PoIJSONSchema poi : pointsOfInterest) {
            if ("link0".equals(poi.name)) { linkPosition = poi.position; break; }
        }
        if (linkPosition == -1) { return false; }
        int linkH = linkPosition / (width * length);
        int linkL = (linkPosition % (width * length)) / width;
        int linkW = linkPosition % width;
        BlockPos basePos = ITUtils.LocalOffsetToWorldBlockPos(origin, mirror ? (width - 1 - linkW) : linkW, linkH, linkL, side);
        SolarRegistry.RegisterResult result = SolarRegistry.canRegisterTower(world, basePos);
        if (!result.success) {
            if (result.vertical) { player.sendMessage(new TextComponentTranslation("chat.immersivetech.solar_tower_vertical_fail")); }
            else if (result.requiredMove > 0) { player.sendMessage(new TextComponentTranslation("chat.immersivetech.solar_tower_too_close", result.requiredMove)); }
            return false;
        }
        BlockPos masterPos = ITUtils.LocalOffsetToWorldBlockPos(origin, mirror ? (width - 1 - masterX) : masterX, masterY, masterZ, side);
        ItemStack hammer = player.getHeldItemMainhand().getItem().getToolClasses(player.getHeldItemMainhand()).contains(Lib.TOOL_HAMMER)?player.getHeldItemMainhand(): player.getHeldItemOffhand();
        if (MultiblockHandler.fireMultiblockFormationEventPre(player, this, pos, hammer).isCanceled()) return false;
        IBlockState masterState = masterBlockState.withProperty(IEProperties.FACING_HORIZONTAL, side).withProperty(IEProperties.MULTIBLOCKSLAVE, false);
        IBlockState slaveState = slaveBlockState.withProperty(IEProperties.FACING_HORIZONTAL, side).withProperty(IEProperties.MULTIBLOCKSLAVE, true);
        for (int h = 0; h < height; h++) for (int l = 0; l < length; l++) for (int w = 0; w < width; w++) {
            if (structure[h][l][w] == AirRef.instance) continue;
            int position = h * (width * length) + l * width + w;
            BlockPos pos2 = ITUtils.LocalOffsetToWorldBlockPos(origin, mirror ? (width - 1 - w) : w, h, l, side);
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
        MultiblockHandler.fireMultiblockFormationEventPost(player, this, pos, hammer);
        return true;
    }
}

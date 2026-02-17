package mctmods.immersivetechnology.common.conveyors;

import blusunrize.immersiveengineering.api.tool.ConveyorHandler.ConveyorDirection;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration1;
import blusunrize.immersiveengineering.common.blocks.wooden.BlockTypes_WoodenDecoration;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraftforge.oredict.OreDictionary;
import org.lwjgl.util.vector.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ConveyorCoveredHelper {
    public static final ArrayList<com.google.common.base.Function<ItemStack, Boolean>> validConveyorCovers = new ArrayList<>();

    public static final ItemStack defaultCover;

    static {
        final List<ItemStack> scaffolds = Lists.newArrayList(
                new ItemStack(IEContent.blockWoodenDecoration, 1, BlockTypes_WoodenDecoration.SCAFFOLDING.getMeta()),
                new ItemStack(IEContent.blockMetalDecoration1, 1, BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_1.getMeta()),
                new ItemStack(IEContent.blockMetalDecoration1, 1, BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_2.getMeta()),
                new ItemStack(IEContent.blockMetalDecoration1, 1, BlockTypes_MetalDecoration1.ALUMINUM_SCAFFOLDING_0.getMeta()),
                new ItemStack(IEContent.blockMetalDecoration1, 1, BlockTypes_MetalDecoration1.ALUMINUM_SCAFFOLDING_1.getMeta()),
                new ItemStack(IEContent.blockMetalDecoration1, 1, BlockTypes_MetalDecoration1.ALUMINUM_SCAFFOLDING_2.getMeta())
        );
        validConveyorCovers.add(input -> {
            if (input == null) return false;
            for (ItemStack stack : scaffolds) {
                if (OreDictionary.itemMatches(stack, input, false)) return true;
            }
            return false;
        });
        validConveyorCovers.add(input -> input != null && Utils.compareToOreName(input, "blockGlass"));

        defaultCover = new ItemStack(IEContent.blockMetalDecoration1, 1, BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_0.getMeta());
    }

    @SuppressWarnings("deprecation")
    public static void addCoverToQuads(List<BakedQuad> baseModel, EnumFacing facing, Supplier<ItemStack> coverGet, ConveyorDirection conDir, boolean[] walls) {
        ItemStack cover = coverGet.get().isEmpty() ? defaultCover : coverGet.get();
        Block b = Block.getBlockFromItem(cover.getItem());
        IBlockState state = cover.isEmpty() ? Blocks.STONE.getDefaultState() : b.getStateFromMeta(cover.getMetadata());
        IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelForState(state);

        TextureAtlasSprite sprite = model.getParticleTexture();
        HashMap<EnumFacing, TextureAtlasSprite> sprites = new HashMap<>();
        for (EnumFacing f : EnumFacing.VALUES) {
            for (BakedQuad q : model.getQuads(state, f, 0L)) {
                if (q != null) sprites.put(f, q.getSprite());
            }
        }
        for (BakedQuad q : model.getQuads(state, null, 0L)) {
            if (q != null) sprites.put(q.getFace(), q.getSprite());
        }
        Function<EnumFacing, TextureAtlasSprite> getSprite = f -> sprites.getOrDefault(f, sprite);
        Function<EnumFacing, TextureAtlasSprite> getSpriteHorizontal = f -> f.getAxis() == Axis.Y ? null : sprites.getOrDefault(f, sprite);
        float[] colour = {1.0F, 1.0F, 1.0F, 1.0F};
        Matrix4 matrix = new Matrix4(facing);
        Function<Vector3f[], Vector3f[]> vertexTransformer = conDir == ConveyorDirection.HORIZONTAL ? v -> v : v -> {
            Vector3f[] ret = new Vector3f[v.length];
            for (int i = 0; i < ret.length; ++i) {
                ret[i] = new Vector3f(v[i].x, v[i].y + (v[i].z == (conDir == ConveyorDirection.UP ? 0 : 1) ? 1 : 0), v[i].z);
            }
            return ret;
        };
        baseModel.addAll(ClientUtils.createBakedBox(new Vector3f(0, 0.75F, 0), new Vector3f(1, 1, 1), matrix, facing, vertexTransformer, getSprite, colour));

        if (walls[0]) {
            baseModel.addAll(ClientUtils.createBakedBox(new Vector3f(0, 0.1875F, 0), new Vector3f(0.0625F, 0.75F, 1), matrix, facing, vertexTransformer, getSpriteHorizontal, colour));
        } else {
            baseModel.addAll(ClientUtils.createBakedBox(new Vector3f(0, 0.1875F, 0), new Vector3f(0.0625F, 0.75F, 0.0625F), matrix, facing, vertexTransformer, getSpriteHorizontal, colour));
            baseModel.addAll(ClientUtils.createBakedBox(new Vector3f(0, 0.1875F, 0.9375F), new Vector3f(0.0625F, 0.75F, 1), matrix, facing, vertexTransformer, getSpriteHorizontal, colour));
        }
        if (walls[1]) {
            baseModel.addAll(ClientUtils.createBakedBox(new Vector3f(0.9375F, 0.1875F, 0), new Vector3f(1, 0.75F, 1), matrix, facing, vertexTransformer, getSpriteHorizontal, colour));
        } else {
            baseModel.addAll(ClientUtils.createBakedBox(new Vector3f(0.9375F, 0.1875F, 0), new Vector3f(1, 0.75F, 0.0625F), matrix, facing, vertexTransformer, getSpriteHorizontal, colour));
            baseModel.addAll(ClientUtils.createBakedBox(new Vector3f(0.9375F, 0.1875F, 0.9375F), new Vector3f(1, 0.75F, 1), matrix, facing, vertexTransformer, getSpriteHorizontal, colour));
        }
    }

    public static boolean handleCoverInteraction(TileEntity tile, EntityPlayer player, EnumHand hand, ItemStack heldItem, Supplier<ItemStack> coverGet, Consumer<ItemStack> coverSet) {
        ItemStack cover = coverGet.get();
        if (heldItem.isEmpty() && player.isSneaking() && !cover.isEmpty()) {
            if (!tile.getWorld().isRemote && tile.getWorld().getGameRules().getBoolean("doTileDrops")) {
                EntityItem entityitem = player.dropItem(cover.copy(), false);
                if (entityitem != null) entityitem.setNoPickupDelay();
            }
            coverSet.accept(ItemStack.EMPTY);
            return true;
        }
        if (!heldItem.isEmpty() && !player.isSneaking()) {
            for (com.google.common.base.Function<ItemStack, Boolean> func : validConveyorCovers) {
                if (func.apply(heldItem) == Boolean.TRUE && !OreDictionary.itemMatches(cover, heldItem, true)) {
                    if (!tile.getWorld().isRemote && !cover.isEmpty() && tile.getWorld().getGameRules().getBoolean("doTileDrops")) {
                        EntityItem entityitem = player.dropItem(cover.copy(), false);
                        if (entityitem != null) entityitem.setNoPickupDelay();
                    }
                    coverSet.accept(Utils.copyStackWithAmount(heldItem, 1));
                    heldItem.shrink(1);
                    if (heldItem.getCount() <= 0) player.setHeldItem(hand, ItemStack.EMPTY);
                    return true;
                }
            }
        }
        return false;
    }
}

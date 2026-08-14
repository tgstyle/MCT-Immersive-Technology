package mctmods.immersivetechnology.common.items;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.MultiblockHandler;
import blusunrize.immersiveengineering.api.tool.ITool;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.RotationUtil;
import blusunrize.immersiveengineering.common.util.advancements.IEAdvancements;
import com.google.common.collect.ImmutableSet;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ItemFormationTool extends ItemITBase implements ITool {
    public ItemFormationTool() { super("formation_tool", 1); }

    @SideOnly(Side.CLIENT)
    @Override public void addInformation(@Nonnull ItemStack stack, @Nullable World world, @Nonnull List<String> tooltip, @Nonnull ITooltipFlag flag) {
        addInfo(tooltip, Lib.DESC_INFO + "multiblocksAllowed", stack, "multiblockPermission");
        addInfo(tooltip, Lib.DESC_INFO + "multiblockForbidden", stack, "multiblockInterdiction");
    }

    @SideOnly(Side.CLIENT)
    private void addInfo(List<String> tooltip, String titleKey, ItemStack stack, String nbtKey) {
        if (!ItemNBTHelper.hasKey(stack, nbtKey)) { return; }
        NBTTagList tagList = ItemNBTHelper.getTag(stack).getTagList(nbtKey, Constants.NBT.TAG_STRING);
        String title = I18n.format(titleKey);
        if (!GuiScreen.isShiftKeyDown()) { tooltip.add(title + " " + I18n.format(Lib.DESC_INFO + "holdShift")); }
        else {
            tooltip.add(title);
            for (int i = 0; i < tagList.tagCount(); i++) { tooltip.add(TextFormatting.DARK_GRAY + " " + I18n.format(Lib.DESC_INFO + "multiblock." + tagList.getStringTagAt(i))); }
        }
    }

    @Nonnull @Override public EnumActionResult onItemUseFirst(EntityPlayer player, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull EnumFacing side, float hitX, float hitY, float hitZ, @Nonnull EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        List<String> permittedMultiblocks = null;
        List<String> interdictedMultiblocks = null;
        if (ItemNBTHelper.hasKey(stack, "multiblockPermission")) {
            permittedMultiblocks = parseMultiblockNames(ItemNBTHelper.getTag(stack).getTagList("multiblockPermission", Constants.NBT.TAG_STRING), player, "permission");
            if (permittedMultiblocks == null) { return EnumActionResult.FAIL; }
        }
        if (ItemNBTHelper.hasKey(stack, "multiblockInterdiction")) {
            interdictedMultiblocks = parseMultiblockNames(ItemNBTHelper.getTag(stack).getTagList("multiblockInterdiction", Constants.NBT.TAG_STRING), player, "interdiction");
            if (interdictedMultiblocks == null) { return EnumActionResult.FAIL; }
        }
        EnumFacing multiblockSide = side.getAxis() == EnumFacing.Axis.Y ? EnumFacing.fromAngle(player.rotationYaw).getOpposite() : side;
        for (MultiblockHandler.IMultiblock multiblock : MultiblockHandler.getMultiblocks()) {
            if (!multiblock.isBlockTrigger(world.getBlockState(pos))) { continue; }
            boolean isAllowed;
            if (permittedMultiblocks != null) { isAllowed = containsIgnoreCase(permittedMultiblocks, multiblock.getUniqueName()); }
            else if (interdictedMultiblocks != null) { isAllowed = !containsIgnoreCase(interdictedMultiblocks, multiblock.getUniqueName()); }
            else { isAllowed = true; }
            if (!isAllowed) { continue; }
            if (MultiblockHandler.fireMultiblockFormationEventPre(player, multiblock, pos, stack).isCanceled()) { continue; }
            if (multiblock.createStructure(world, pos, multiblockSide, player)) {
                if (player instanceof EntityPlayerMP) { IEAdvancements.TRIGGER_MULTIBLOCK.trigger((EntityPlayerMP)player, multiblock, stack); }
                return EnumActionResult.SUCCESS;
            }
        }
        return EnumActionResult.PASS;
    }

    @Nonnull @Override public EnumActionResult onItemUse(@Nonnull EntityPlayer player, World world, @Nonnull BlockPos pos, @Nonnull EnumHand hand, @Nonnull EnumFacing side, float hitX, float hitY, float hitZ) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof IEBlockInterfaces.IDirectionalTile || tile instanceof IEBlockInterfaces.IHammerInteraction || tile instanceof IEBlockInterfaces.IConfigurableSides) { return EnumActionResult.PASS; }
        return RotationUtil.rotateBlock(world, pos, side) ? EnumActionResult.SUCCESS : EnumActionResult.PASS;
    }

    @Nullable private static List<String> parseMultiblockNames(NBTTagList data, EntityPlayer player, String prefix) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < data.tagCount(); i++) {
            String entry = data.getStringTagAt(i);
            if (findMultiblock(entry) == null) {
                if (!player.getEntityWorld().isRemote) { player.sendMessage(new TextComponentString("Invalid " + prefix + " entry: " + entry)); }
                return null;
            }
            result.add(entry);
        }
        return result;
    }

    @Nullable private static MultiblockHandler.IMultiblock findMultiblock(String uniqueName) {
        for (MultiblockHandler.IMultiblock multiblock : MultiblockHandler.getMultiblocks()) {
            if (multiblock.getUniqueName().equalsIgnoreCase(uniqueName)) { return multiblock; }
        }
        return null;
    }

    private static boolean containsIgnoreCase(List<String> names, String uniqueName) {
        for (String name : names) {
            if (name.equalsIgnoreCase(uniqueName)) { return true; }
        }
        return false;
    }

    @Override public boolean doesSneakBypassUse(@Nonnull ItemStack stack, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EntityPlayer player) { return true; }

    @Override public boolean hasContainerItem(@Nonnull ItemStack stack) { return true; }

    @Nonnull @Override public ItemStack getContainerItem(@Nonnull ItemStack stack) { return stack.copy(); }

    @Override public boolean isEnchantable(@Nonnull ItemStack stack) { return false; }

    @Override public int getItemEnchantability(@Nonnull ItemStack stack) { return 0; }

    @Override public boolean isBookEnchantable(@Nonnull ItemStack stack, @Nonnull ItemStack book) { return false; }

    @Override public boolean canApplyAtEnchantingTable(@Nonnull ItemStack stack, @Nonnull Enchantment enchantment) { return false; }

    @Nonnull @Override public Set<String> getToolClasses(@Nonnull ItemStack stack) { return ImmutableSet.of(Lib.TOOL_HAMMER); }

    @Override public boolean isTool(ItemStack item) { return true; }
}

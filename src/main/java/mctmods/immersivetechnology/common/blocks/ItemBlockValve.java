package mctmods.immersivetechnology.common.blocks;


import com.immersiveconvergence.api.client.IICOBJModelCallback;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

public class ItemBlockValve extends ItemBlockITBase implements IICOBJModelCallback<ItemStack> {

	public ItemBlockValve(Block b) { super(b); }

	@SideOnly(Side.CLIENT)
	@Override public boolean shouldRenderGroup(@Nonnull ItemStack object, @Nonnull String group) { return !"Handle_Closed".equals(group); }
}

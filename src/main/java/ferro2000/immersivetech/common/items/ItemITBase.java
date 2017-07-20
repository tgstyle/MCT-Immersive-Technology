package ferro2000.immersivetech.common.items;

import java.util.ArrayList;
import java.util.List;

import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IColouredItem;
import ferro2000.immersivetech.ImmersiveTech;
import ferro2000.immersivetech.common.ITContent;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemITBase extends Item implements IColouredItem
{
	public String itemName;
	protected String[] subNames;
	boolean[] isMetaHidden;
	public boolean registerSubModels=true;

	public ItemITBase(String name, int stackSize, String... subNames)
	{
		this.setUnlocalizedName(ImmersiveTech.MODID+"."+name);
		this.setHasSubtypes(subNames!=null&&subNames.length>0);
		this.setCreativeTab(ImmersiveTech.creativeTab);
		this.setMaxStackSize(stackSize);
		this.itemName = name;
		this.subNames = subNames!=null&&subNames.length>0?subNames:null;
		this.isMetaHidden = new boolean[this.subNames!=null?this.subNames.length:1];
		ImmersiveTech.register(this, name);
		ITContent.registeredITItems.add(this);
	}

	public String[] getSubNames()
	{
		return subNames;
	}
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs tab, List list)
	{
		if(getSubNames()!=null)
		{
			for(int i=0;i<getSubNames().length;i++)
				if(!isMetaHidden(i))
					list.add(new ItemStack(this,1,i));
		}
		else
			list.add(new ItemStack(this));

	}
	@Override
	public String getUnlocalizedName(ItemStack stack)
	{
		if(getSubNames()!=null)
		{
			String subName = stack.getItemDamage()<getSubNames().length?getSubNames()[stack.getItemDamage()]:"";
			return this.getUnlocalizedName()+"."+subName;
		}
		return this.getUnlocalizedName();
	}

	public ItemITBase setMetaHidden(int... meta)
	{
		for(int i : meta)
			if(i>=0 && i<this.isMetaHidden.length)
				this.isMetaHidden[i] = true;
		return this;
	}
	public ItemITBase setMetaUnhidden(int... meta)
	{
		for(int i : meta)
			if(i>=0 && i<this.isMetaHidden.length)
				this.isMetaHidden[i] = false;
		return this;
	}
	public boolean isMetaHidden(int meta)
	{
		return this.isMetaHidden[Math.max(0, Math.min(meta, this.isMetaHidden.length-1))];
	}
	
	public ItemITBase setRegisterSubModels(boolean register)
	{
		this.registerSubModels = register;
		return this;
	}

}

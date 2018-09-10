package ferro2000.immersivetech.common.items.wires;

import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.api.energy.wires.WireApi;
import blusunrize.immersiveengineering.api.energy.wires.WireType;
import ferro2000.immersivetech.common.ITContent;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/*public class ITWireType extends WireType {
	
	private final int type;
	public static final String NET_CATEGORY = "NET";
	private final int[] netColors = {0x6ed7ef};
	private static String[] netNames = {"net"};
	private final double[] netRenderDiameter = {0.3125};
	
	public static final ITWireType NET = new ITWireType(0);
	
	public ITWireType(int ord) {
		super();
		this.type = ord;
		WireApi.registerWireType(this);
	}

	@Override
	public String getUniqueName() {
		return netNames[type];
	}

	@Override
	public double getLossRatio() {
		return 0;
	}

	@Override
	public int getTransferRate() {
		return 0;
	}

	@Override
	public int getColour(Connection connection) {
		return netColors[type];
	}

	@Override
	public double getSlack() {
		return 1.005;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public TextureAtlasSprite getIcon(Connection connection) {
		return iconDefaultWire;
	}
	
	//TODO CONFIG
	
	@Override
	public int getMaxLength() {
		return 32;
	}

	@Override
	public ItemStack getWireCoil() {
		return new ItemStack(ITContent.netCoil,1,type);
	}

	@Override
	public double getRenderDiameter() {
		return renderDiameter[type];
	}

	@Override
	public boolean isEnergyWire() {
		return true;
	}

}*/

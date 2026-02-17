package mctmods.immersivetechnology.common.blocks.metal.tileentities;

import blusunrize.immersiveengineering.api.tool.ConveyorHandler;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.IConveyorBelt;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityConveyorBelt;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.Map;

public class TileEntityConveyorBeltAlternative extends TileEntityConveyorBelt {

    private static final ResourceLocation DEFAULT_BASIC = new ResourceLocation("immersiveengineering", "conveyor");

    public TileEntityConveyorBeltAlternative() {}

    @Override @Nonnull public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound compound) {
        IConveyorBelt conveyor = getConveyorSubtype();
        if (conveyor != null) {
            NBTTagCompound subNBT = conveyor.writeConveyorNBT();
            if (!subNBT.isEmpty()) { compound.setTag("conveyorBeltSubtypeNBT", subNBT); }
            try {
                Field reverseField = ConveyorHandler.class.getDeclaredField("reverseClassRegistry");
                reverseField.setAccessible(true);
                @SuppressWarnings("unchecked")
                Map<Class<?>, ResourceLocation> reverse = (Map<Class<?>, ResourceLocation>) reverseField.get(null);
                ResourceLocation rl = reverse.get(conveyor.getClass());
                if (rl != null) { compound.setString("conveyorBeltSubtype", rl.toString()); }
            } catch (Exception ignored) {}
        }
        return super.writeToNBT(compound);
    }

    @Override public void readFromNBT(@Nonnull NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (compound.hasKey("conveyorBeltSubtype")) {
            ResourceLocation rl = new ResourceLocation(compound.getString("conveyorBeltSubtype"));
            IConveyorBelt subtype = ConveyorHandler.getConveyor(rl, this);
            setConveyorSubtype(subtype);
            if (compound.hasKey("conveyorBeltSubtypeNBT")) {
                subtype.readConveyorNBT(compound.getCompoundTag("conveyorBeltSubtypeNBT"));
            }
        } else if (getConveyorSubtype() == null) {
            setConveyorSubtype(ConveyorHandler.getConveyor(DEFAULT_BASIC, this));
        }
    }
}

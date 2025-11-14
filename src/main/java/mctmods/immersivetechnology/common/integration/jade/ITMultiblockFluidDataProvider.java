package mctmods.immersivetechnology.common.integration.jade;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockBEHelper;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockBE;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import mctmods.immersivetechnology.common.multiblocks.helper.ITDisplayContext;
import mctmods.immersivetechnology.core.lib.ITLib;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import snownee.jade.api.Accessor;
import snownee.jade.api.view.ClientViewGroup;
import snownee.jade.api.view.IClientExtensionProvider;
import snownee.jade.api.view.IServerExtensionProvider;
import snownee.jade.api.view.ViewGroup;
import snownee.jade.api.fluid.JadeFluidObject;
import snownee.jade.api.view.FluidView;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ITMultiblockFluidDataProvider<T extends IMultiblockState> implements IServerExtensionProvider<IMultiblockBE<T>, CompoundTag>, IClientExtensionProvider<CompoundTag, FluidView> {
    @Override
    public @Nullable List<ViewGroup<CompoundTag>> getGroups(ServerPlayer serverPlayer, ServerLevel serverLevel, IMultiblockBE<T> multiblockBE, boolean b) {
        final IMultiblockBEHelper<T> helper = multiblockBE.getHelper();
        if (helper.getState() instanceof ITDisplayContext dc) {
            IFluidTank[] tanks = dc.getInternalTanks();
            if (tanks != null && tanks.length > 0) {
                List<CompoundTag> list = new ArrayList<>();
                for (IFluidTank tank : tanks) {
                    FluidStack fs = tank.getFluid();
                    JadeFluidObject fluidObject = JadeFluidObject.of(fs.getFluid(), fs.getAmount(), fs.getTag());
                    CompoundTag tag = FluidView.writeDefault(fluidObject, tank.getCapacity());
                    list.add(tag);
                }
                return List.of(new ViewGroup<>(list));
            }
        }
        return null;
    }

    @Override
    public List<ClientViewGroup<FluidView>> getClientGroups(Accessor<?> accessor, List<ViewGroup<CompoundTag>> list) { return ClientViewGroup.map(list, FluidView::readDefault, null); }

    @Override
    public ResourceLocation getUid() { return ITLib.rl("multiblock_fluid"); }
}

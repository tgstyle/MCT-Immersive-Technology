package mctmods.immersivetechnology.core.integration.jade;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockBEHelper;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockBE;
import mctmods.immersivetechnology.common.multiblocks.helper.IDisplayContext;
import mctmods.immersivetechnology.core.lib.Reference;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.Accessor;
import snownee.jade.api.view.*;
import snownee.jade.api.fluid.JadeFluidObject;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.fluids.FluidStack;

import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;

public class MultiblockFluidDataProvider implements IServerExtensionProvider<CompoundTag>, IClientExtensionProvider<CompoundTag, FluidView> {

    @Override @Nullable public List<ViewGroup<CompoundTag>> getGroups(Accessor<?> accessor) {
        Object target = accessor.getTarget();
        if (!(target instanceof IMultiblockBE<?> multiblockBE)) { return null; }
        final IMultiblockBEHelper<?> helper = multiblockBE.getHelper();
        if (helper.getState() instanceof IDisplayContext dc) {
            Object tanksObj = dc.getInternalTanks();
            if (tanksObj instanceof Object[] tanks && tanks.length > 0) {
                List<CompoundTag> list = new ArrayList<>();
                for (Object t : tanks) {
                    if (t instanceof FluidTank tank) {
                        FluidStack fs = tank.getFluid();
                        if (fs.isEmpty()) continue;
                        JadeFluidObject fluidObject = JadeFluidObject.of(fs.getFluid(), fs.getAmount());
                        CompoundTag tag = FluidView.writeDefault(fluidObject, tank.getCapacity());
                        list.add(tag);
                    }
                }
                if (!list.isEmpty()) {
                    return List.of(new ViewGroup<>(list));
                }
            }
        }
        return null;
    }

    @Override
    public List<ClientViewGroup<FluidView>> getClientGroups(Accessor<?> accessor, List<ViewGroup<CompoundTag>> list) { return ClientViewGroup.map(list, FluidView::readDefault, null); }

    @Override
    public ResourceLocation getUid() { return Reference.rl("multiblock_fluid"); }
}

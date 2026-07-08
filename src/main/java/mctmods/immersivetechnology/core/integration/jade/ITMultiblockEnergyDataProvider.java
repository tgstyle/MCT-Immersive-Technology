package mctmods.immersivetechnology.core.integration.jade;

import blusunrize.immersiveengineering.api.energy.AveragingEnergyStorage;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockBEHelper;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockBE;
import mctmods.immersivetechnology.common.multiblocks.helper.ITIDisplayContext;
import mctmods.immersivetechnology.core.lib.ITLib;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.Accessor;
import snownee.jade.api.view.*;

import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ITMultiblockEnergyDataProvider implements IServerExtensionProvider<CompoundTag>, IClientExtensionProvider<CompoundTag, EnergyView> {

    @Override @Nullable public List<ViewGroup<CompoundTag>> getGroups(Accessor<?> accessor) {
        Object target = accessor.getTarget();
        if (!(target instanceof IMultiblockBE<?> multiblockBE)) {
            return null;
        }
        final IMultiblockBEHelper<?> helper = multiblockBE.getHelper();
        if (helper.getState() instanceof ITIDisplayContext dc) {
            List<AveragingEnergyStorage> energies = dc.getEnergies();
            if (!energies.isEmpty()) {
                List<CompoundTag> list = new ArrayList<>();
                for (AveragingEnergyStorage energy : energies) {
                    list.add(EnergyView.of(energy.getEnergyStored(), energy.getMaxEnergyStored()));
                }
                return List.of(new ViewGroup<>(list));
            }
        }
        return null;
    }

    @Override public List<ClientViewGroup<EnergyView>> getClientGroups(Accessor<?> accessor, List<ViewGroup<CompoundTag>> list) {return ClientViewGroup.map(list, tag -> EnergyView.read(tag, "RF"), null); }

    @Override public ResourceLocation getUid() {
        return ITLib.rl("multiblock_energy");
    }
}

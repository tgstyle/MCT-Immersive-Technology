package mctmods.immersivetechnology.core.integration.jade;

import blusunrize.immersiveengineering.api.energy.AveragingEnergyStorage;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockBEHelper;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockBE;
import mctmods.immersivetechnology.common.multiblocks.helper.IDisplayContext;
import mctmods.immersivetechnology.core.lib.Reference;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import snownee.jade.api.Accessor;
import snownee.jade.api.view.*;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class MultiblockEnergyDataProvider implements IServerExtensionProvider<Object, CompoundTag>, IClientExtensionProvider<CompoundTag, EnergyView> {

    @Override
    @Nullable
    public List<ViewGroup<CompoundTag>> getGroups(ServerPlayer serverPlayer, ServerLevel serverLevel, Object target, boolean b) {
        if (!(target instanceof IMultiblockBE<?> multiblockBE)) {
            return null;
        }
        final IMultiblockBEHelper<?> helper = multiblockBE.getHelper();
        if (helper.getState() instanceof IDisplayContext dc) {
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

    @Override
    public List<ClientViewGroup<EnergyView>> getClientGroups(Accessor<?> accessor, List<ViewGroup<CompoundTag>> list) {
        return ClientViewGroup.map(list, tag -> EnergyView.read(tag, "RF"), null);
    }

    @Override
    public ResourceLocation getUid() {
        return Reference.rl("multiblock_energy");
    }
}

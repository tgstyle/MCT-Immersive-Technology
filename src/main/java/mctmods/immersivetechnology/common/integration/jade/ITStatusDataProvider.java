package mctmods.immersivetechnology.common.integration.jade;

import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockBE;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.AlternatorLogic;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.BoilerLiquidLogic;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.BoilerSolidLogic;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.BoilerTankLogic;
import mctmods.immersivetechnology.common.multiblocks.stone.logic.CoolingTowerLogic;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.DistillerLogic;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.GasTurbineLogic;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.SolarMelterLogic;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.SolarReflectorLogic;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.SolarTowerLogic;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.SteamTurbineLogic;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.SteelSheetmetalTankLogic;
import mctmods.immersivetechnology.core.lib.ITLib;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

public enum ITStatusDataProvider implements IServerDataProvider<BlockAccessor> {
    INSTANCE;

    @Override
    public ResourceLocation getUid() { return ITLib.rl("status"); }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (accessor.getBlockEntity() instanceof IMultiblockBE<?> mbe) {
            IMultiblockState state = mbe.getHelper().getState();
            if (state == null) { return; }
            boolean active = false;
            boolean fuelEmpty = false;
            if (state instanceof DistillerLogic.State distiller) { active = distiller.isActive(); }
            else if (state instanceof BoilerLiquidLogic.State boiler) { active = boiler.active; fuelEmpty = boiler.tanks.input1().getFluid().isEmpty(); }
            else if (state instanceof AlternatorLogic.State alternator) { active = alternator.speed > 0; }
            else if (state instanceof BoilerSolidLogic.State boilerSolid) { active = boilerSolid.active; fuelEmpty = boilerSolid.inventory.getStackInSlot(BoilerSolidLogic.INPUT_FUEL_SLOT).isEmpty(); }
            else if (state instanceof BoilerTankLogic.State boilerTank) { active = boilerTank.active; }
            else if (state instanceof CoolingTowerLogic.State coolingTower) { active = coolingTower.active; }
            else if (state instanceof GasTurbineLogic.State gas) { active = gas.active; fuelEmpty = gas.tanks.input().getFluid().isEmpty(); }
            else if (state instanceof SolarMelterLogic.State melter) { active = melter.active; fuelEmpty = melter.tanks.input().getFluid().isEmpty(); }
            else if (state instanceof SolarReflectorLogic.State reflector) { active = reflector.active; }
            else if (state instanceof SolarTowerLogic.State tower) { active = tower.active; fuelEmpty = tower.tanks.input().getFluid().isEmpty(); }
            else if (state instanceof SteamTurbineLogic.State steam) { active = steam.active; fuelEmpty = steam.tanks.input().getFluid().isEmpty(); }
            else if (state instanceof SteelSheetmetalTankLogic.State tank) { active = tank.active; }
            data.putBoolean("ITActive", active);
            if (fuelEmpty) { data.putBoolean("ITFuelEmpty", true); }
        }
    }
}

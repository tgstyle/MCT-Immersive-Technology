package mctmods.immersivetechnology.common.util.compat.top;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.blocks.TileEntityMultiblockPart;

import mcjty.theoneprobe.api.*;

import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.shared.interfaces.ITBlockInterfaces.IMechanicalEnergy;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityBoilerMaster;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityBoilerSlave;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityCoolingTowerMaster;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityCoolingTowerSlave;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityDistillerMaster;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityDistillerSlave;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityElectrolyticCrucibleBatteryMaster;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityElectrolyticCrucibleBatterySlave;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityGasTurbineMaster;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityGasTurbineSlave;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityHeatExchangerMaster;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityHeatExchangerSlave;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityHighPressureSteamTurbineMaster;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityHighPressureSteamTurbineSlave;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityMeltingCrucibleMaster;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityMeltingCrucibleSlave;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityRadiatorMaster;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityRadiatorSlave;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntitySolarMelterMaster;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntitySolarMelterSlave;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntitySolarTowerMaster;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntitySolarTowerSlave;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntitySteamTurbineMaster;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntitySteamTurbineSlave;
import mctmods.immersivetechnology.common.util.compat.ITCompatModule;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.common.event.FMLInterModComms;

import javax.annotation.Nullable;
import java.util.function.Function;

public class OneProbeHelper extends ITCompatModule implements Function<ITheOneProbe, Void> {
    private static final int maxSpeed = Multiblocks.mechanicalEnergy.mechanicalEnergy_speed_max;
    private static final double boilerWorkingHeatLevel = Multiblocks.boiler.boiler_heat_workingLevel;
    private static final double solarWorkingHeatLevel = Multiblocks.solarTower.solarTower_heat_workingLevel;

    @Override public void preInit() { FMLInterModComms.sendFunctionMessage("theoneprobe", "getTheOneProbe", this.getClass().getName()); }

    @Override public void init() { }

    @Override public void postInit() { }

    @Nullable
    @Override public Void apply(@Nullable ITheOneProbe input) {
        assert input != null;
        input.registerProvider(new BoilerProvider());
        input.registerProvider(new CoolingTowerProvider());
        input.registerProvider(new DistillerProvider());
        input.registerProvider(new ElectrolyticCrucibleBatteryProvider());
        input.registerProvider(new GasTurbineProvider());
        input.registerProvider(new HeatExchangerProvider());
        input.registerProvider(new HighPressureSteamTurbineProvider());
        input.registerProvider(new MechanicalEnergyProvider());
        input.registerProvider(new MeltingCrucibleProvider());
        input.registerProvider(new RadiatorProvider());
        input.registerProvider(new SolarMelterProvider());
        input.registerProvider(new SolarTowerProvider());
        input.registerProvider(new SteamTurbineProvider());
        return null;
    }

    public static class BoilerProvider implements IProbeInfoProvider {
        @Override public String getID() { return ImmersiveTechnology.MODID + ":" + "BoilerInfo"; }

        @Override public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
            TileEntity te = world.getTileEntity(data.getPos());
            if (!(te instanceof TileEntityBoilerSlave)) return;
            TileEntityBoilerMaster master = ((TileEntityBoilerSlave)te).master();
            if (master == null) return;
            EnumFacing facing = data.getSideHit();
            int pos = ((TileEntityMultiblockPart<?>)te).pos;
            IFluidTank[] accessible = master.getAccessibleFluidTanks(facing, pos);
            if (accessible.length > 0) {
                IFluidTank tank = accessible[0];
                FluidStack fluid = tank.getFluid();
                int amount = fluid != null ? fluid.amount : 0;
                String fluidName = fluid != null ? fluid.getLocalizedName() : "Empty";
                int color = OneProbeHelper.getFluidColor(fluid);
                if (amount > 0) {
                    probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER).spacing(2))
                            .progress(amount, tank.getCapacity(), probeInfo.defaultProgressStyle().suffix(" mB").numberFormat(NumberFormat.COMPACT).filledColor(color).alternateFilledColor(color).backgroundColor(0xff000000).borderColor(0xffffffff))
                            .text(fluidName);
                }
            } else {
                for (FluidTank tank : master.tanks) {
                    FluidStack fluid = tank.getFluid();
                    int amount = fluid != null ? fluid.amount : 0;
                    String fluidName = fluid != null ? fluid.getLocalizedName() : "Empty";
                    int color = OneProbeHelper.getFluidColor(fluid);
                    if (amount > 0) {
                        probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER).spacing(2))
                                .progress(amount, tank.getCapacity(), probeInfo.defaultProgressStyle().suffix(" mB").numberFormat(NumberFormat.COMPACT).filledColor(color).alternateFilledColor(color).backgroundColor(0xff000000).borderColor(0xffffffff))
                                .text(fluidName);
                    }
                }
            }
            double displayHeat = master.heatLevel / 20 + 30;
            double displayMax = boilerWorkingHeatLevel / 20 + 30;
            int currentTemp = (int)displayHeat;
            int maxTemp = (int)displayMax;
            probeInfo.progress(currentTemp, maxTemp, probeInfo.defaultProgressStyle()
                    .suffix("/" + maxTemp + "°C")
                    .filledColor(0xffcc0000)
                    .alternateFilledColor(0xff990000)
                    .borderColor(0xffff6666)
                    .numberFormat(NumberFormat.FULL));
            int currentProg = (master.recipeTimeRemaining > 0 && master.lastRecipe != null) ? (master.lastRecipe.getTotalProcessTime() - master.recipeTimeRemaining) * 100 / master.lastRecipe.getTotalProcessTime() : 0;
            if (currentProg > 0) {
                probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER).spacing(2))
                        .progress(currentProg, 100, probeInfo.defaultProgressStyle().numberFormat(NumberFormat.FULL).suffix("%"));
            }
        }
    }

    public static class CoolingTowerProvider implements IProbeInfoProvider {
        @Override public String getID() { return ImmersiveTechnology.MODID + ":" + "CoolingTowerInfo"; }

        @Override public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
            TileEntity te = world.getTileEntity(data.getPos());
            if (!(te instanceof TileEntityCoolingTowerSlave)) return;
            TileEntityCoolingTowerMaster master = ((TileEntityCoolingTowerSlave)te).master();
            if (master == null) return;
            EnumFacing facing = data.getSideHit();
            int pos = ((TileEntityMultiblockPart<?>)te).pos;
            IFluidTank[] accessible = master.getAccessibleFluidTanks(facing, pos);
            if (accessible.length > 0) {
                IFluidTank tank = accessible[0];
                FluidStack fluid = tank.getFluid();
                int amount = fluid != null ? fluid.amount : 0;
                String fluidName = fluid != null ? fluid.getLocalizedName() : "Empty";
                int color = OneProbeHelper.getFluidColor(fluid);
                if (amount > 0) {
                    probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER).spacing(2))
                            .progress(amount, tank.getCapacity(), probeInfo.defaultProgressStyle().suffix(" mB").numberFormat(NumberFormat.COMPACT).filledColor(color).alternateFilledColor(color).backgroundColor(0xff000000).borderColor(0xffffffff))
                            .text(fluidName);
                }
            } else {
                for (FluidTank tank : master.tanks) {
                    FluidStack fluid = tank.getFluid();
                    int amount = fluid != null ? fluid.amount : 0;
                    String fluidName = fluid != null ? fluid.getLocalizedName() : "Empty";
                    int color = OneProbeHelper.getFluidColor(fluid);
                    if (amount > 0) {
                        probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER).spacing(2))
                                .progress(amount, tank.getCapacity(), probeInfo.defaultProgressStyle().suffix(" mB").numberFormat(NumberFormat.COMPACT).filledColor(color).alternateFilledColor(color).backgroundColor(0xff000000).borderColor(0xffffffff))
                                .text(fluidName);
                    }
                }
            }
            int currentProg = (!master.processQueue.isEmpty()) ? master.processQueue.get(0).processTick * 100 / master.processQueue.get(0).maxTicks : 0;
            if (currentProg > 0) {
                probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER).spacing(2))
                        .progress(currentProg, 100, probeInfo.defaultProgressStyle().numberFormat(NumberFormat.FULL).suffix("%"));
            }
        }
    }

    public static class DistillerProvider implements IProbeInfoProvider {
        @Override public String getID() { return ImmersiveTechnology.MODID + ":" + "DistillerInfo"; }

        @Override public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
            TileEntity te = world.getTileEntity(data.getPos());
            if (!(te instanceof TileEntityDistillerSlave)) return;
            TileEntityDistillerMaster master = ((TileEntityDistillerSlave)te).master();
            if (master == null) return;
            EnumFacing facing = data.getSideHit();
            int pos = ((TileEntityMultiblockPart<?>)te).pos;
            IFluidTank[] accessible = master.getAccessibleFluidTanks(facing, pos);
            if (accessible.length > 0) {
                IFluidTank tank = accessible[0];
                FluidStack fluid = tank.getFluid();
                int amount = fluid != null ? fluid.amount : 0;
                String fluidName = fluid != null ? fluid.getLocalizedName() : "Empty";
                int color = OneProbeHelper.getFluidColor(fluid);
                if (amount > 0) {
                    probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER).spacing(2))
                            .progress(amount, tank.getCapacity(), probeInfo.defaultProgressStyle().suffix(" mB").numberFormat(NumberFormat.COMPACT).filledColor(color).alternateFilledColor(color).backgroundColor(0xff000000).borderColor(0xffffffff))
                            .text(fluidName);
                }
            } else {
                for (FluidTank tank : master.tanks) {
                    FluidStack fluid = tank.getFluid();
                    int amount = fluid != null ? fluid.amount : 0;
                    String fluidName = fluid != null ? fluid.getLocalizedName() : "Empty";
                    int color = OneProbeHelper.getFluidColor(fluid);
                    if (amount > 0) {
                        probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER).spacing(2))
                                .progress(amount, tank.getCapacity(), probeInfo.defaultProgressStyle().suffix(" mB").numberFormat(NumberFormat.COMPACT).filledColor(color).alternateFilledColor(color).backgroundColor(0xff000000).borderColor(0xffffffff))
                                .text(fluidName);
                    }
                }
            }
        }
    }

    public static class ElectrolyticCrucibleBatteryProvider implements IProbeInfoProvider {
        @Override public String getID() { return ImmersiveTechnology.MODID + ":" + "ElectrolyticCrucibleBatteryInfo"; }

        @Override public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
            TileEntity te = world.getTileEntity(data.getPos());
            if (!(te instanceof TileEntityElectrolyticCrucibleBatterySlave)) return;
            TileEntityElectrolyticCrucibleBatteryMaster master = ((TileEntityElectrolyticCrucibleBatterySlave)te).master();
            if (master == null) return;
            EnumFacing facing = data.getSideHit();
            int pos = ((TileEntityMultiblockPart<?>)te).pos;
            IFluidTank[] accessible = master.getAccessibleFluidTanks(facing, pos);
            if (accessible.length > 0) {
                IFluidTank tank = accessible[0];
                FluidStack fluid = tank.getFluid();
                int amount = fluid != null ? fluid.amount : 0;
                String fluidName = fluid != null ? fluid.getLocalizedName() : "Empty";
                int color = OneProbeHelper.getFluidColor(fluid);
                if (amount > 0) {
                    probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER).spacing(2))
                            .progress(amount, tank.getCapacity(), probeInfo.defaultProgressStyle().suffix(" mB").numberFormat(NumberFormat.COMPACT).filledColor(color).alternateFilledColor(color).backgroundColor(0xff000000).borderColor(0xffffffff))
                            .text(fluidName);
                }
            } else {
                for (FluidTank tank : master.tanks) {
                    FluidStack fluid = tank.getFluid();
                    int amount = fluid != null ? fluid.amount : 0;
                    String fluidName = fluid != null ? fluid.getLocalizedName() : "Empty";
                    int color = OneProbeHelper.getFluidColor(fluid);
                    if (amount > 0) {
                        probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER).spacing(2))
                                .progress(amount, tank.getCapacity(), probeInfo.defaultProgressStyle().suffix(" mB").numberFormat(NumberFormat.COMPACT).filledColor(color).alternateFilledColor(color).backgroundColor(0xff000000).borderColor(0xffffffff))
                                .text(fluidName);
                    }
                }
            }
        }
    }

    public static class GasTurbineProvider implements IProbeInfoProvider {
        @Override public String getID() { return ImmersiveTechnology.MODID + ":" + "GasTurbineInfo"; }

        @Override public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
            TileEntity te = world.getTileEntity(data.getPos());
            if (!(te instanceof TileEntityGasTurbineSlave)) return;
            TileEntityGasTurbineMaster master = ((TileEntityGasTurbineSlave)te).master();
            if (master == null) return;
            EnumFacing facing = data.getSideHit();
            int pos = ((TileEntityMultiblockPart<?>)te).pos;
            IFluidTank[] accessible = master.getAccessibleFluidTanks(facing, pos);
            if (accessible.length > 0) {
                IFluidTank tank = accessible[0];
                FluidStack fluid = tank.getFluid();
                int amount = fluid != null ? fluid.amount : 0;
                String fluidName = fluid != null ? fluid.getLocalizedName() : "Empty";
                int color = OneProbeHelper.getFluidColor(fluid);
                if (!master.isFluidInputPosition(facing, pos) && !master.isFluidOutputPosition(facing, pos)) return;
                if (amount > 0) {
                    probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER).spacing(2))
                            .progress(amount, tank.getCapacity(), probeInfo.defaultProgressStyle().suffix(" mB").numberFormat(NumberFormat.COMPACT).filledColor(color).alternateFilledColor(color).backgroundColor(0xff000000).borderColor(0xffffffff))
                            .text(fluidName);
                }
            } else {
                for (FluidTank tank : master.tanks) {
                    FluidStack fluid = tank.getFluid();
                    int amount = fluid != null ? fluid.amount : 0;
                    String fluidName = fluid != null ? fluid.getLocalizedName() : "Empty";
                    int color = OneProbeHelper.getFluidColor(fluid);
                    if (amount > 0) {
                        probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER).spacing(2))
                                .progress(amount, tank.getCapacity(), probeInfo.defaultProgressStyle().suffix(" mB").numberFormat(NumberFormat.COMPACT).filledColor(color).alternateFilledColor(color).backgroundColor(0xff000000).borderColor(0xffffffff))
                                .text(fluidName);
                    }
                }
            }
            if (master.isEnergyPosition(facing, pos)) {
                if (!master.isStarterPosition(facing, pos) && !master.isSparkplugPosition(facing, pos)) return;
                IEnergyStorage storage = master.getEnergyAtPosition(facing, pos);
                if (storage == null) return;
                probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER).spacing(2))
                        .progress(storage.getEnergyStored(), storage.getMaxEnergyStored(), probeInfo.defaultProgressStyle().suffix(" IF").filledColor(Lib.COLOUR_I_ImmersiveOrange).alternateFilledColor(0xff994f20).borderColor(Lib.COLOUR_I_ImmersiveOrangeShadow).numberFormat(NumberFormat.COMPACT));
            } else {
                if (master.starterStorage.getEnergyStored() > 0) {
                    probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER).spacing(2))
                            .progress(master.starterStorage.getEnergyStored(), master.starterStorage.getMaxEnergyStored(), probeInfo.defaultProgressStyle().suffix(" IF").filledColor(Lib.COLOUR_I_ImmersiveOrange).alternateFilledColor(0xff994f20).borderColor(Lib.COLOUR_I_ImmersiveOrangeShadow).numberFormat(NumberFormat.COMPACT));
                }
                if (master.sparkplugStorage.getEnergyStored() > 0) {
                    probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER).spacing(2))
                            .progress(master.sparkplugStorage.getEnergyStored(), master.sparkplugStorage.getMaxEnergyStored(), probeInfo.defaultProgressStyle().suffix(" IF").filledColor(Lib.COLOUR_I_ImmersiveOrange).alternateFilledColor(0xff994f20).borderColor(Lib.COLOUR_I_ImmersiveOrangeShadow).numberFormat(NumberFormat.COMPACT));
                }
            }
        }
    }

    public static class HeatExchangerProvider implements IProbeInfoProvider {
        @Override public String getID() { return ImmersiveTechnology.MODID + ":" + "HeatExchangerInfo"; }

        @Override public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
            TileEntity te = world.getTileEntity(data.getPos());
            if (!(te instanceof TileEntityHeatExchangerSlave)) return;
            TileEntityHeatExchangerMaster master = ((TileEntityHeatExchangerSlave)te).master();
            if (master == null) return;
            EnumFacing facing = data.getSideHit();
            int pos = ((TileEntityMultiblockPart<?>)te).pos;
            IFluidTank[] accessible = master.getAccessibleFluidTanks(facing, pos);
            if (accessible.length > 0) {
                IFluidTank tank = accessible[0];
                FluidStack fluid = tank.getFluid();
                int amount = fluid != null ? fluid.amount : 0;
                String fluidName = fluid != null ? fluid.getLocalizedName() : "Empty";
                int color = OneProbeHelper.getFluidColor(fluid);
                if (amount > 0) {
                    probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER).spacing(2))
                            .progress(amount, tank.getCapacity(), probeInfo.defaultProgressStyle().suffix(" mB").numberFormat(NumberFormat.COMPACT).filledColor(color).alternateFilledColor(color).backgroundColor(0xff000000).borderColor(0xffffffff))
                            .text(fluidName);
                }
            } else {
                for (FluidTank tank : master.tanks) {
                    FluidStack fluid = tank.getFluid();
                    int amount = fluid != null ? fluid.amount : 0;
                    String fluidName = fluid != null ? fluid.getLocalizedName() : "Empty";
                    int color = OneProbeHelper.getFluidColor(fluid);
                    if (amount > 0) {
                        probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER).spacing(2))
                                .progress(amount, tank.getCapacity(), probeInfo.defaultProgressStyle().suffix(" mB").numberFormat(NumberFormat.COMPACT).filledColor(color).alternateFilledColor(color).backgroundColor(0xff000000).borderColor(0xffffffff))
                                .text(fluidName);
                    }
                }
            }
            int currentProg = (!master.processQueue.isEmpty()) ? master.processQueue.get(0).processTick * 100 / master.processQueue.get(0).maxTicks : 0;
            if (currentProg > 0) {
                probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER).spacing(2))
                        .progress(currentProg, 100, probeInfo.defaultProgressStyle().numberFormat(NumberFormat.FULL).suffix("%"));
            }
        }
    }

    public static class HighPressureSteamTurbineProvider implements IProbeInfoProvider {
        @Override public String getID() { return ImmersiveTechnology.MODID + ":" + "HighPressureSteamTurbineInfo"; }

        @Override public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
            TileEntity te = world.getTileEntity(data.getPos());
            if (!(te instanceof TileEntityHighPressureSteamTurbineSlave)) return;
            TileEntityHighPressureSteamTurbineMaster master = ((TileEntityHighPressureSteamTurbineSlave)te).master();
            if (master == null) return;
            EnumFacing facing = data.getSideHit();
            int pos = ((TileEntityMultiblockPart<?>)te).pos;
            IFluidTank[] accessible = master.getAccessibleFluidTanks(facing, pos);
            if (accessible.length > 0) {
                IFluidTank tank = accessible[0];
                FluidStack fluid = tank.getFluid();
                int amount = fluid != null ? fluid.amount : 0;
                String fluidName = fluid != null ? fluid.getLocalizedName() : "Empty";
                int color = OneProbeHelper.getFluidColor(fluid);
                if (amount > 0) {
                    probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER).spacing(2))
                            .progress(amount, tank.getCapacity(), probeInfo.defaultProgressStyle().suffix(" mB").numberFormat(NumberFormat.COMPACT).filledColor(color).alternateFilledColor(color).backgroundColor(0xff000000).borderColor(0xffffffff))
                            .text(fluidName);
                }
            } else {
                for (FluidTank tank : master.tanks) {
                    FluidStack fluid = tank.getFluid();
                    int amount = fluid != null ? fluid.amount : 0;
                    String fluidName = fluid != null ? fluid.getLocalizedName() : "Empty";
                    int color = OneProbeHelper.getFluidColor(fluid);
                    if (amount > 0) {
                        probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER).spacing(2))
                                .progress(amount, tank.getCapacity(), probeInfo.defaultProgressStyle().suffix(" mB").numberFormat(NumberFormat.COMPACT).filledColor(color).alternateFilledColor(color).backgroundColor(0xff000000).borderColor(0xffffffff))
                                .text(fluidName);
                    }
                }
            }
        }
    }

    public static class MechanicalEnergyProvider implements IProbeInfoProvider {
        @Override public String getID() { return ImmersiveTechnology.MODID + ":" + "MechanicalEnergyInfo"; }

        @Override public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
            TileEntity te = world.getTileEntity(data.getPos());
            if (te instanceof IMechanicalEnergy) {
                TileEntityMultiblockPart<?> multiblock = ((TileEntityMultiblockPart<?>)te);
                TileEntity master = multiblock.master();
                if (master == null) return;
                int current = ((IMechanicalEnergy)master).getSpeed();
                if (current > 0) {
                    probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER).spacing(2))
                            .progress(current, maxSpeed, probeInfo.defaultProgressStyle().numberFormat(NumberFormat.FULL).suffix(" RPM"));
                }
            }
        }
    }

    public static class MeltingCrucibleProvider implements IProbeInfoProvider {
        @Override public String getID() { return ImmersiveTechnology.MODID + ":" + "MeltingCrucibleInfo"; }

        @Override public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
            TileEntity te = world.getTileEntity(data.getPos());
            if (!(te instanceof TileEntityMeltingCrucibleSlave)) return;
            TileEntityMeltingCrucibleMaster master = ((TileEntityMeltingCrucibleSlave)te).master();
            if (master == null) return;
            EnumFacing facing = data.getSideHit();
            int pos = ((TileEntityMultiblockPart<?>)te).pos;
            IFluidTank[] accessible = master.getAccessibleFluidTanks(facing, pos);
            if (accessible.length > 0) {
                IFluidTank tank = accessible[0];
                FluidStack fluid = tank.getFluid();
                int amount = fluid != null ? fluid.amount : 0;
                String fluidName = fluid != null ? fluid.getLocalizedName() : "Empty";
                int color = OneProbeHelper.getFluidColor(fluid);
                if (amount > 0) {
                    probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER).spacing(2))
                            .progress(amount, tank.getCapacity(), probeInfo.defaultProgressStyle().suffix(" mB").numberFormat(NumberFormat.COMPACT).filledColor(color).alternateFilledColor(color).backgroundColor(0xff000000).borderColor(0xffffffff))
                            .text(fluidName);
                }
            } else {
                FluidTank tank = master.tanks[0];
                FluidStack fluid = tank.getFluid();
                int amount = fluid != null ? fluid.amount : 0;
                String fluidName = fluid != null ? fluid.getLocalizedName() : "Empty";
                int color = OneProbeHelper.getFluidColor(fluid);
                if (amount > 0) {
                    probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER).spacing(2))
                            .progress(amount, tank.getCapacity(), probeInfo.defaultProgressStyle().suffix(" mB").numberFormat(NumberFormat.COMPACT).filledColor(color).alternateFilledColor(color).backgroundColor(0xff000000).borderColor(0xffffffff))
                            .text(fluidName);
                }
            }
            int currentProg = (!master.processQueue.isEmpty()) ? master.processQueue.get(0).processTick * 100 / master.processQueue.get(0).maxTicks : 0;
            if (currentProg > 0) {
                probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER).spacing(2))
                        .progress(currentProg, 100, probeInfo.defaultProgressStyle().numberFormat(NumberFormat.FULL).suffix("%"));
            }
        }
    }

    public static class RadiatorProvider implements IProbeInfoProvider {
        @Override public String getID() { return ImmersiveTechnology.MODID + ":" + "RadiatorInfo"; }

        @Override public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
            TileEntity te = world.getTileEntity(data.getPos());
            if (!(te instanceof TileEntityRadiatorSlave)) return;
            TileEntityRadiatorMaster master = ((TileEntityRadiatorSlave)te).master();
            if (master == null) return;
            EnumFacing facing = data.getSideHit();
            int pos = ((TileEntityMultiblockPart<?>)te).pos;
            IFluidTank[] accessible = master.getAccessibleFluidTanks(facing, pos);
            if (accessible.length > 0) {
                IFluidTank tank = accessible[0];
                FluidStack fluid = tank.getFluid();
                int amount = fluid != null ? fluid.amount : 0;
                String fluidName = fluid != null ? fluid.getLocalizedName() : "Empty";
                int color = OneProbeHelper.getFluidColor(fluid);
                if (amount > 0) {
                    probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER).spacing(2))
                            .progress(amount, tank.getCapacity(), probeInfo.defaultProgressStyle().suffix(" mB").numberFormat(NumberFormat.COMPACT).filledColor(color).alternateFilledColor(color).backgroundColor(0xff000000).borderColor(0xffffffff))
                            .text(fluidName);
                }
            } else {
                for (FluidTank tank : master.tanks) {
                    FluidStack fluid = tank.getFluid();
                    int amount = fluid != null ? fluid.amount : 0;
                    String fluidName = fluid != null ? fluid.getLocalizedName() : "Empty";
                    int color = OneProbeHelper.getFluidColor(fluid);
                    if (amount > 0) {
                        probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER).spacing(2))
                                .progress(amount, tank.getCapacity(), probeInfo.defaultProgressStyle().suffix(" mB").numberFormat(NumberFormat.COMPACT).filledColor(color).alternateFilledColor(color).backgroundColor(0xff000000).borderColor(0xffffffff))
                                .text(fluidName);
                    }
                }
            }
            int currentProg = (master.recipeTimeTotal > 0) ? (master.recipeTimeTotal - master.recipeTimeRemaining) * 100 / master.recipeTimeTotal : 0;
            if (currentProg > 0) {
                probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER).spacing(2))
                        .progress(currentProg, 100, probeInfo.defaultProgressStyle().numberFormat(NumberFormat.FULL).suffix("%"));
            }
        }
    }

    public static class SolarMelterProvider implements IProbeInfoProvider {
        @Override public String getID() { return ImmersiveTechnology.MODID + ":" + "SolarMelterInfo"; }

        @Override public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
            TileEntity te = world.getTileEntity(data.getPos());
            if (!(te instanceof TileEntitySolarMelterSlave)) return;
            TileEntitySolarMelterMaster master = ((TileEntitySolarMelterSlave)te).master();
            if (master == null) return;
            EnumFacing facing = data.getSideHit();
            int pos = ((TileEntityMultiblockPart<?>)te).pos;
            IFluidTank[] accessible = master.getAccessibleFluidTanks(facing, pos);
            if (accessible.length > 0) {
                IFluidTank tank = accessible[0];
                FluidStack fluid = tank.getFluid();
                int amount = fluid != null ? fluid.amount : 0;
                String fluidName = fluid != null ? fluid.getLocalizedName() : "Empty";
                int color = OneProbeHelper.getFluidColor(fluid);
                if (amount > 0) {
                    probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER).spacing(2))
                            .progress(amount, tank.getCapacity(), probeInfo.defaultProgressStyle().suffix(" mB").numberFormat(NumberFormat.COMPACT).filledColor(color).alternateFilledColor(color).backgroundColor(0xff000000).borderColor(0xffffffff))
                            .text(fluidName);
                }
            } else {
                FluidTank tank = master.tanks[0];
                FluidStack fluid = tank.getFluid();
                int amount = fluid != null ? fluid.amount : 0;
                String fluidName = fluid != null ? fluid.getLocalizedName() : "Empty";
                int color = OneProbeHelper.getFluidColor(fluid);
                if (amount > 0) {
                    probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER).spacing(2))
                            .progress(amount, tank.getCapacity(), probeInfo.defaultProgressStyle().suffix(" mB").numberFormat(NumberFormat.COMPACT).filledColor(color).alternateFilledColor(color).backgroundColor(0xff000000).borderColor(0xffffffff))
                            .text(fluidName);
                }
            }
            int currentProg = (master.recipeEnergyRemaining > 0 && master.cachedRecipe != null) ? (master.cachedRecipe.getTotalProcessEnergy() - master.recipeEnergyRemaining) * 100 / master.cachedRecipe.getTotalProcessEnergy() : 0;
            if (currentProg > 0) {
                probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER).spacing(2))
                        .progress(currentProg, 100, probeInfo.defaultProgressStyle().numberFormat(NumberFormat.FULL).suffix("%"));
            }
        }
    }

    public static class SolarTowerProvider implements IProbeInfoProvider {
        @Override public String getID() { return ImmersiveTechnology.MODID + ":" + "SolarTowerInfo"; }

        @Override public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
            TileEntity te = world.getTileEntity(data.getPos());
            if (!(te instanceof TileEntitySolarTowerSlave)) return;
            TileEntitySolarTowerMaster master = ((TileEntitySolarTowerSlave)te).master();
            if (master == null) return;
            EnumFacing facing = data.getSideHit();
            int pos = ((TileEntityMultiblockPart<?>)te).pos;
            IFluidTank[] accessible = master.getAccessibleFluidTanks(facing, pos);
            if (accessible.length > 0) {
                IFluidTank tank = accessible[0];
                FluidStack fluid = tank.getFluid();
                int amount = fluid != null ? fluid.amount : 0;
                String fluidName = fluid != null ? fluid.getLocalizedName() : "Empty";
                int color = OneProbeHelper.getFluidColor(fluid);
                if (amount > 0) {
                    probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER).spacing(2))
                            .progress(amount, tank.getCapacity(), probeInfo.defaultProgressStyle().suffix(" mB").numberFormat(NumberFormat.COMPACT).filledColor(color).alternateFilledColor(color).backgroundColor(0xff000000).borderColor(0xffffffff))
                            .text(fluidName);
                }
            } else {
                for (FluidTank tank : master.tanks) {
                    FluidStack fluid = tank.getFluid();
                    int amount = fluid != null ? fluid.amount : 0;
                    String fluidName = fluid != null ? fluid.getLocalizedName() : "Empty";
                    int color = OneProbeHelper.getFluidColor(fluid);
                    if (amount > 0) {
                        probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER).spacing(2))
                                .progress(amount, tank.getCapacity(), probeInfo.defaultProgressStyle().suffix(" mB").numberFormat(NumberFormat.COMPACT).filledColor(color).alternateFilledColor(color).backgroundColor(0xff000000).borderColor(0xffffffff))
                                .text(fluidName);
                    }
                }
            }
            double displayHeat = master.heatLevel / 20 + 30;
            double displayMax = solarWorkingHeatLevel / 20 + 30;
            int currentTemp = (int)displayHeat;
            int maxTemp = (int)displayMax;
            probeInfo.progress(currentTemp, maxTemp, probeInfo.defaultProgressStyle()
                    .suffix("/" + maxTemp + "°C")
                    .filledColor(0xffcc0000)
                    .alternateFilledColor(0xff990000)
                    .borderColor(0xffff6666)
                    .numberFormat(NumberFormat.FULL));
            int currentProg = (master.recipeTimeRemaining > 0 && master.cachedRecipe != null) ? (master.cachedRecipe.getTotalProcessTime() - master.recipeTimeRemaining) * 100 / master.cachedRecipe.getTotalProcessTime() : 0;
            if (currentProg > 0) {
                probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER).spacing(2))
                        .progress(currentProg, 100, probeInfo.defaultProgressStyle().numberFormat(NumberFormat.FULL).suffix("%"));
            }
        }
    }

    public static class SteamTurbineProvider implements IProbeInfoProvider {
        @Override public String getID() { return ImmersiveTechnology.MODID + ":" + "SteamTurbineInfo"; }

        @Override public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
            TileEntity te = world.getTileEntity(data.getPos());
            if (!(te instanceof TileEntitySteamTurbineSlave)) return;
            TileEntitySteamTurbineMaster master = ((TileEntitySteamTurbineSlave)te).master();
            if (master == null) return;
            EnumFacing facing = data.getSideHit();
            int pos = ((TileEntityMultiblockPart<?>)te).pos;
            IFluidTank[] accessible = master.getAccessibleFluidTanks(facing, pos);
            if (accessible.length > 0) {
                IFluidTank tank = accessible[0];
                FluidStack fluid = tank.getFluid();
                int amount = fluid != null ? fluid.amount : 0;
                String fluidName = fluid != null ? fluid.getLocalizedName() : "Empty";
                int color = OneProbeHelper.getFluidColor(fluid);
                if (amount > 0) {
                    probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER).spacing(2))
                            .progress(amount, tank.getCapacity(), probeInfo.defaultProgressStyle().suffix(" mB").numberFormat(NumberFormat.COMPACT).filledColor(color).alternateFilledColor(color).backgroundColor(0xff000000).borderColor(0xffffffff))
                            .text(fluidName);
                }
            } else {
                for (FluidTank tank : master.tanks) {
                    FluidStack fluid = tank.getFluid();
                    int amount = fluid != null ? fluid.amount : 0;
                    String fluidName = fluid != null ? fluid.getLocalizedName() : "Empty";
                    int color = OneProbeHelper.getFluidColor(fluid);
                    if (amount > 0) {
                        probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER).spacing(2))
                                .progress(amount, tank.getCapacity(), probeInfo.defaultProgressStyle().suffix(" mB").numberFormat(NumberFormat.COMPACT).filledColor(color).alternateFilledColor(color).backgroundColor(0xff000000).borderColor(0xffffffff))
                                .text(fluidName);
                    }
                }
            }
        }
    }

    private static int getFluidColor(@Nullable FluidStack fluid) {
        if (fluid == null) return 0xff555555;
        int tint = fluid.getFluid().getColor(fluid);
        ResourceLocation still = fluid.getFluid().getStill(fluid);
        TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(still.toString());
        int[] pixels = sprite.getFrameTextureData(0)[0];
        int textureColor = getTextureColor(pixels);
        int r = ((textureColor >> 16 & 0xff) * (tint >> 16 & 0xff)) / 255;
        int g = ((textureColor >> 8 & 0xff) * (tint >> 8 & 0xff)) / 255;
        int b = ((textureColor & 0xff) * (tint & 0xff)) / 255;
        return 0xff000000 | r << 16 | g << 8 | b;
    }

    private static int getTextureColor(int[] pixels) {
        int avgR = 0, avgG = 0, avgB = 0, count = 0;
        for (int p : pixels) {
            int alpha = p >> 24 & 0xff;
            if (alpha > 0) {
                avgR += (p >> 16 & 0xff) * alpha / 255;
                avgG += (p >> 8 & 0xff) * alpha / 255;
                avgB += (p & 0xff) * alpha / 255;
                count++;
            }
        }
        if (count > 0) {
            avgR /= count;
            avgG /= count;
            avgB /= count;
        }
        return 0xff000000 | avgR << 16 | avgG << 8 | avgB;
    }
}

package mctmods.immersivetechnology.common.util.compat.top;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.blocks.TileEntityMultiblockPart;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMultiblockMetal;

import mcjty.theoneprobe.api.*;

import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.api.crafting.CoolingTowerRecipe;
import mctmods.immersivetechnology.api.crafting.ElectrolyticCrucibleBatteryRecipe;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.multiblocks.stone.tileentities.TileEntityAdvancedCokeOvenMaster;
import mctmods.immersivetechnology.common.multiblocks.stone.tileentities.TileEntityAdvancedCokeOvenSlave;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.*;
import mctmods.immersivetechnology.common.util.compat.ITCompatModule;

import com.immersiveconvergence.api.capability.IMechanicalEnergyConsumer;
import com.immersiveconvergence.api.capability.IMechanicalEnergyProvider;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fml.common.event.FMLInterModComms;

import javax.annotation.Nullable;
import java.util.function.Function;

public class OneProbeHelper extends ITCompatModule implements Function<ITheOneProbe, Void> {
    private static int maxSpeed() { return Multiblocks.mechanicalEnergy.mechanicalEnergy_speed_max; }
    private static double boilerWorkingHeatLevel() { return Multiblocks.boiler.boiler_heat_workingLevel; }
    private static double solarWorkingHeatLevel() { return Multiblocks.solarTower.solarTower_heat_workingLevel; }
    private static double meltingCrucibleWorkingHeatLevel() { return Multiblocks.meltingCrucible.meltingCrucible_heat_workingLevel; }
    private static double solarMelterWorkingHeatLevel() { return Multiblocks.solarMelter.solarMelter_heat_workingLevel; }

    @Override public void preInit() { FMLInterModComms.sendFunctionMessage("theoneprobe", "getTheOneProbe", this.getClass().getName()); }

    @Override public void init() { }

    @Override public void postInit() { }

    @Override @Nullable public Void apply(@Nullable ITheOneProbe input) {
        assert input != null;
        input.registerProvider(new MechanicalEnergyProvider());
        input.registerProvider(new AdvancedCokeOvenProvider());
        input.registerProvider(new AlternatorProvider());
        input.registerProvider(new BoilerProvider());
        input.registerProvider(new CoolingTowerProvider());
        input.registerProvider(new DistillerProvider());
        input.registerProvider(new ElectrolyticCrucibleBatteryProvider());
        input.registerProvider(new GasTurbineProvider());
        input.registerProvider(new HeatExchangerProvider());
        input.registerProvider(new HighPressureSteamTurbineProvider());
        input.registerProvider(new MeltingCrucibleProvider());
        input.registerProvider(new RadiatorProvider());
        input.registerProvider(new SolarMelterProvider());
        input.registerProvider(new SolarTowerProvider());
        input.registerProvider(new SteamTurbineProvider());
        input.registerProvider(new SteelSheetmetalTankProvider());
        return null;
    }

    private static void addFluidTankDisplay(IProbeInfo probeInfo, FluidTank tank) {
        FluidStack fluid = tank.getFluid();
        int amount = fluid != null ? fluid.amount : 0;
        String fluidName = fluid != null ? fluid.getLocalizedName() : "Empty";
        int color = getFluidColor(fluid);
        probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER).spacing(2))
                .progress(amount, tank.getCapacity(), probeInfo.defaultProgressStyle().suffix(" mB").numberFormat(NumberFormat.COMPACT).filledColor(color).alternateFilledColor(color).backgroundColor(0xff000000).borderColor(0xffffffff))
                .text(fluidName);
    }

    private static void addEnergyDisplay(IProbeInfo probeInfo, int stored, int max) {
        probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER).spacing(2))
                .progress(stored, max, probeInfo.defaultProgressStyle().suffix(" IF").filledColor(Lib.COLOUR_I_ImmersiveOrange).alternateFilledColor(0xff994f20).borderColor(Lib.COLOUR_I_ImmersiveOrangeShadow).numberFormat(NumberFormat.COMPACT));
    }

    private static void addRPMDisplay(IProbeInfo probeInfo, int speed) {
        probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER).spacing(2))
                .progress(speed, maxSpeed(), probeInfo.defaultProgressStyle().numberFormat(NumberFormat.FULL).suffix(" RPM"));
    }

    private static void addProcessPercent(IProbeInfo probeInfo, int percent) {
        probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER).spacing(2))
                .progress(percent, 100, probeInfo.defaultProgressStyle().numberFormat(NumberFormat.FULL).suffix("%"));
    }

    private static void addTemperature(IProbeInfo probeInfo, double heatLevel, double workingLevel) {
        double displayHeat = heatLevel / 20.0 + 30;
        double displayMax = workingLevel / 20.0 + 30;
        int current = (int)displayHeat;
        int max = (int)displayMax;
        probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER).spacing(2))
                .progress(current, max, probeInfo.defaultProgressStyle()
                        .suffix(" °C")
                        .filledColor(0xffcc0000)
                        .alternateFilledColor(0xffcc0000)
                        .borderColor(0xffff6666)
                        .numberFormat(NumberFormat.FULL));
    }

    public static class MechanicalEnergyProvider implements IProbeInfoProvider {
        @Override public String getID() { return ImmersiveTechnology.MODID + ":" + "MechanicalEnergyInfo"; }

        @Override public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
            TileEntity te = world.getTileEntity(data.getPos());
            if (te instanceof TileEntityMultiblockPart<?>) {
                TileEntityMultiblockPart<?> part = (TileEntityMultiblockPart<?>)te;
                TileEntity master = part.master();
                if (master instanceof IMechanicalEnergyProvider) {
                    int speed = ((IMechanicalEnergyProvider)master).getSpeed();
                    addRPMDisplay(probeInfo, speed);
                }
                else if (master instanceof IMechanicalEnergyConsumer) {
                    int speed = ((IMechanicalEnergyConsumer)master).getSpeed();
                    addRPMDisplay(probeInfo, speed);
                }
            }
        }
    }

    public static class AdvancedCokeOvenProvider implements IProbeInfoProvider {
        @Override public String getID() { return ImmersiveTechnology.MODID + ":" + "AdvancedCokeOvenInfo"; }

        @Override public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
            TileEntity te = world.getTileEntity(data.getPos());
            TileEntityAdvancedCokeOvenMaster master;
            if (te instanceof TileEntityAdvancedCokeOvenMaster) {
                master = (TileEntityAdvancedCokeOvenMaster)te;
            } else if (te instanceof TileEntityAdvancedCokeOvenSlave) {
                master = ((TileEntityAdvancedCokeOvenSlave)te).master();
                if (master == null) return;
            } else return;
            addFluidTankDisplay(probeInfo, master.tank);
            int currentProg = (master.processTimeRemaining > 0 && master.processTimeMax > 0) ? (master.processTimeMax - master.processTimeRemaining) * 100 / master.processTimeMax : 0;
            addProcessPercent(probeInfo, currentProg);
        }
    }

    public static class AlternatorProvider implements IProbeInfoProvider {
        @Override public String getID() { return ImmersiveTechnology.MODID + ":" + "AlternatorInfo"; }

        @Override public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
            TileEntity te = world.getTileEntity(data.getPos());
            TileEntityAlternatorMaster master;
            if (te instanceof TileEntityAlternatorMaster) {
                master = (TileEntityAlternatorMaster)te;
            } else if (te instanceof TileEntityAlternatorSlave) {
                master = ((TileEntityAlternatorSlave)te).master();
                if (master == null) return;
            } else return;
            if (mode == ProbeMode.EXTENDED) addEnergyDisplay(probeInfo, master.energyStorage.getEnergyStored(), master.energyStorage.getMaxEnergyStored());
        }
    }

    public static class BoilerProvider implements IProbeInfoProvider {
        @Override public String getID() { return ImmersiveTechnology.MODID + ":" + "BoilerInfo"; }

        @Override public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
            TileEntity te = world.getTileEntity(data.getPos());
            TileEntityBoilerMaster master;
            if (te instanceof TileEntityBoilerMaster) {
                master = (TileEntityBoilerMaster)te;
            } else if (te instanceof TileEntityBoilerSlave) {
                master = ((TileEntityBoilerSlave)te).master();
                if (master == null) return;
            } else return;
            for (FluidTank tank : master.tanks) addFluidTankDisplay(probeInfo, tank);
            addTemperature(probeInfo, master.heatLevel, boilerWorkingHeatLevel());
            int currentProg = (master.processTimeRemaining > 0 && master.processTimeMax > 0) ? (master.processTimeMax - master.processTimeRemaining) * 100 / master.processTimeMax : 0;
            addProcessPercent(probeInfo, currentProg);
        }
    }

    public static class CoolingTowerProvider implements IProbeInfoProvider {
        @Override public String getID() { return ImmersiveTechnology.MODID + ":" + "CoolingTowerInfo"; }

        @Override public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
            TileEntity te = world.getTileEntity(data.getPos());
            TileEntityCoolingTowerMaster master;
            if (te instanceof TileEntityCoolingTowerMaster) {
                master = (TileEntityCoolingTowerMaster)te;
            } else if (te instanceof TileEntityCoolingTowerSlave) {
                master = ((TileEntityCoolingTowerSlave)te).master();
                if (master == null) return;
            } else return;
            for (FluidTank tank : master.tanks) addFluidTankDisplay(probeInfo, tank);
            for (TileEntityMultiblockMetal.MultiblockProcess<CoolingTowerRecipe> process : master.processQueue) {
                if (process.maxTicks <= 0) continue;
                int currentProg = process.processTick * 100 / process.maxTicks;
                addProcessPercent(probeInfo, currentProg);
            }
        }
    }

    public static class DistillerProvider implements IProbeInfoProvider {
        @Override public String getID() { return ImmersiveTechnology.MODID + ":" + "DistillerInfo"; }

        @Override public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
            TileEntity te = world.getTileEntity(data.getPos());
            TileEntityDistillerMaster master;
            if (te instanceof TileEntityDistillerMaster) {
                master = (TileEntityDistillerMaster)te;
            } else if (te instanceof TileEntityDistillerSlave) {
                master = ((TileEntityDistillerSlave)te).master();
                if (master == null) return;
            } else return;
            for (FluidTank tank : master.tanks) addFluidTankDisplay(probeInfo, tank);
            int currentProg = (master.processTimeRemaining > 0 && master.processTimeMax > 0) ? (master.processTimeMax - master.processTimeRemaining) * 100 / master.processTimeMax : 0;
            addProcessPercent(probeInfo, currentProg);
        }
    }

    public static class ElectrolyticCrucibleBatteryProvider implements IProbeInfoProvider {
        @Override public String getID() { return ImmersiveTechnology.MODID + ":" + "ElectrolyticCrucibleBatteryInfo"; }

        @Override public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
            TileEntity te = world.getTileEntity(data.getPos());
            TileEntityElectrolyticCrucibleBatteryMaster master;
            int pos;
            if (te instanceof TileEntityElectrolyticCrucibleBatteryMaster) {
                master = (TileEntityElectrolyticCrucibleBatteryMaster)te;
                pos = ((TileEntityMultiblockPart<?>)te).pos;
            } else if (te instanceof TileEntityElectrolyticCrucibleBatterySlave) {
                TileEntityElectrolyticCrucibleBatterySlave slave = (TileEntityElectrolyticCrucibleBatterySlave)te;
                master = slave.master();
                if (master == null) return;
                pos = slave.pos;
            } else return;
            EnumFacing facing = data.getSideHit();
            for (FluidTank tank : master.tanks) addFluidTankDisplay(probeInfo, tank);
            boolean showEnergy = mode == ProbeMode.EXTENDED || master.isEnergyPosition(facing, pos);
            if (showEnergy) addEnergyDisplay(probeInfo, master.energyStorage.getEnergyStored(), master.energyStorage.getMaxEnergyStored());
            for (TileEntityMultiblockMetal.MultiblockProcess<ElectrolyticCrucibleBatteryRecipe> process : master.processQueue) {
                if (process.maxTicks <= 0) continue;
                int currentProg = process.processTick * 100 / process.maxTicks;
                addProcessPercent(probeInfo, currentProg);
            }
        }
    }

    public static class GasTurbineProvider implements IProbeInfoProvider {
        @Override public String getID() { return ImmersiveTechnology.MODID + ":" + "GasTurbineInfo"; }

        @Override public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
            TileEntity te = world.getTileEntity(data.getPos());
            TileEntityGasTurbineMaster master;
            if (te instanceof TileEntityGasTurbineMaster) {
                master = (TileEntityGasTurbineMaster)te;
            } else if (te instanceof TileEntityGasTurbineSlave) {
                master = ((TileEntityGasTurbineSlave)te).master();
                if (master == null) return;
            } else return;

            for (FluidTank tank : master.tanks) addFluidTankDisplay(probeInfo, tank);

            addEnergyDisplay(probeInfo, master.starterStorage.getEnergyStored(), master.starterStorage.getMaxEnergyStored());
            addEnergyDisplay(probeInfo, master.sparkplugStorage.getEnergyStored(), master.sparkplugStorage.getMaxEnergyStored());
        }
    }

    public static class HeatExchangerProvider implements IProbeInfoProvider {
        @Override public String getID() { return ImmersiveTechnology.MODID + ":" + "HeatExchangerInfo"; }

        @Override public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
            TileEntity te = world.getTileEntity(data.getPos());
            TileEntityHeatExchangerMaster master;
            int pos;
            if (te instanceof TileEntityHeatExchangerMaster) {
                master = (TileEntityHeatExchangerMaster)te;
                pos = ((TileEntityMultiblockPart<?>)te).pos;
            } else if (te instanceof TileEntityHeatExchangerSlave) {
                master = ((TileEntityHeatExchangerSlave)te).master();
                if (master == null) return;
                pos = ((TileEntityHeatExchangerSlave)te).pos;
            } else return;
            EnumFacing facing = data.getSideHit();
            for (FluidTank tank : master.tanks) addFluidTankDisplay(probeInfo, tank);
            boolean showEnergy = mode == ProbeMode.EXTENDED || master.isEnergyPosition(facing, pos);
            if (showEnergy) addEnergyDisplay(probeInfo, master.energyStorage.getEnergyStored(), master.energyStorage.getMaxEnergyStored());
            int currentProg = (master.processTimeRemaining > 0 && master.processTimeMax > 0) ? (master.processTimeMax - master.processTimeRemaining) * 100 / master.processTimeMax : 0;
            addProcessPercent(probeInfo, currentProg);
        }
    }

    public static class HighPressureSteamTurbineProvider implements IProbeInfoProvider {
        @Override public String getID() { return ImmersiveTechnology.MODID + ":" + "HighPressureSteamTurbineInfo"; }

        @Override public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
            TileEntity te = world.getTileEntity(data.getPos());
            TileEntityHighPressureSteamTurbineMaster master;
            if (te instanceof TileEntityHighPressureSteamTurbineMaster) {
                master = (TileEntityHighPressureSteamTurbineMaster)te;
            } else if (te instanceof TileEntityHighPressureSteamTurbineSlave) {
                master = ((TileEntityHighPressureSteamTurbineSlave)te).master();
                if (master == null) return;
            } else return;
            for (FluidTank tank : master.tanks) addFluidTankDisplay(probeInfo, tank);
        }
    }

    public static class MeltingCrucibleProvider implements IProbeInfoProvider {
        @Override public String getID() { return ImmersiveTechnology.MODID + ":" + "MeltingCrucibleInfo"; }

        @Override public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
            TileEntity te = world.getTileEntity(data.getPos());
            TileEntityMeltingCrucibleMaster master;
            if (te instanceof TileEntityMeltingCrucibleMaster) {
                master = (TileEntityMeltingCrucibleMaster)te;
            } else if (te instanceof TileEntityMeltingCrucibleSlave) {
                master = ((TileEntityMeltingCrucibleSlave)te).master();
                if (master == null) return;
            } else return;
            for (FluidTank tank : master.tanks) addFluidTankDisplay(probeInfo, tank);
            addTemperature(probeInfo, master.heatLevel, meltingCrucibleWorkingHeatLevel());
            int maxProg = master.processTimeMax;
            int currentProg = maxProg - master.processTimeRemaining;
            int percent = maxProg > 0 ? currentProg * 100 / maxProg : 0;
            addProcessPercent(probeInfo, percent);
        }
    }

    public static class RadiatorProvider implements IProbeInfoProvider {
        @Override public String getID() { return ImmersiveTechnology.MODID + ":" + "RadiatorInfo"; }

        @Override public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
            TileEntity te = world.getTileEntity(data.getPos());
            TileEntityRadiatorMaster master;
            if (te instanceof TileEntityRadiatorMaster) {
                master = (TileEntityRadiatorMaster)te;
            } else if (te instanceof TileEntityRadiatorSlave) {
                master = ((TileEntityRadiatorSlave)te).master();
                if (master == null) return;
            } else return;
            for (FluidTank tank : master.tanks) addFluidTankDisplay(probeInfo, tank);
            int currentProg = (master.processTimeRemaining > 0 && master.processTimeTotal > 0) ? (master.processTimeTotal - master.processTimeRemaining) * 100 / master.processTimeTotal : 0;
            addProcessPercent(probeInfo, currentProg);
            probeInfo.text("Reflector efficiency");
            addProcessPercent(probeInfo, (int)Math.round(master.getRadiationEfficiency() * 100.0));
        }
    }

    public static class SolarMelterProvider implements IProbeInfoProvider {
        @Override public String getID() { return ImmersiveTechnology.MODID + ":" + "SolarMelterInfo"; }

        @Override public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
            TileEntity te = world.getTileEntity(data.getPos());
            TileEntitySolarMelterMaster master;
            if (te instanceof TileEntitySolarMelterMaster) {
                master = (TileEntitySolarMelterMaster)te;
            } else if (te instanceof TileEntitySolarMelterSlave) {
                master = ((TileEntitySolarMelterSlave)te).master();
                if (master == null) return;
            } else return;
            for (FluidTank tank : master.tanks) addFluidTankDisplay(probeInfo, tank);
            addTemperature(probeInfo, master.heatLevel, solarMelterWorkingHeatLevel());
            int maxProg = master.processTimeMax;
            int currentProg = maxProg - master.processTimeRemaining;
            int percent = maxProg > 0 ? currentProg * 100 / maxProg : 0;
            addProcessPercent(probeInfo, percent);
        }
    }

    public static class SolarTowerProvider implements IProbeInfoProvider {
        @Override public String getID() { return ImmersiveTechnology.MODID + ":" + "SolarTowerInfo"; }

        @Override public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
            TileEntity te = world.getTileEntity(data.getPos());
            TileEntitySolarTowerMaster master;
            if (te instanceof TileEntitySolarTowerMaster) {
                master = (TileEntitySolarTowerMaster)te;
            } else if (te instanceof TileEntitySolarTowerSlave) {
                master = ((TileEntitySolarTowerSlave)te).master();
                if (master == null) return;
            } else return;
            for (FluidTank tank : master.tanks) addFluidTankDisplay(probeInfo, tank);
            addTemperature(probeInfo, master.heatLevel, solarWorkingHeatLevel());
            int currentProg = 0;
            if (master.processTimeRemaining > 0 && master.processTimeMax > 0) {
                currentProg = (master.processTimeMax - master.processTimeRemaining) * 100 / master.processTimeMax;
                currentProg = Math.max(0, Math.min(100, currentProg));
            }
            addProcessPercent(probeInfo, currentProg);
        }
    }

    public static class SteamTurbineProvider implements IProbeInfoProvider {
        @Override public String getID() { return ImmersiveTechnology.MODID + ":" + "SteamTurbineInfo"; }

        @Override public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
            TileEntity te = world.getTileEntity(data.getPos());
            TileEntitySteamTurbineMaster master;
            if (te instanceof TileEntitySteamTurbineMaster) {
                master = (TileEntitySteamTurbineMaster)te;
            } else if (te instanceof TileEntitySteamTurbineSlave) {
                master = ((TileEntitySteamTurbineSlave)te).master();
                if (master == null) return;
            } else return;
            for (FluidTank tank : master.tanks) addFluidTankDisplay(probeInfo, tank);
        }
    }

    public static class SteelSheetmetalTankProvider implements IProbeInfoProvider {
        @Override public String getID() { return ImmersiveTechnology.MODID + ":" + "SteelSheetmetalTankInfo"; }

        @Override public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
            TileEntity te = world.getTileEntity(data.getPos());
            TileEntitySteelSheetmetalTankMaster master;
            if (te instanceof TileEntitySteelSheetmetalTankMaster) {
                master = (TileEntitySteelSheetmetalTankMaster)te;
            } else if (te instanceof TileEntitySteelSheetmetalTankSlave) {
                master = ((TileEntitySteelSheetmetalTankSlave)te).master();
                if (master == null) return;
            } else return;
            addFluidTankDisplay(probeInfo, master.tank);
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

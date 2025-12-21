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
    private static final double workingHeatLevel = Multiblocks.boiler.boiler_heat_workingLevel;

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
        input.registerProvider(new MiscProvider());
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
                int color = getFluidColor(fluid);
                probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER).spacing(2))
                        .progress(amount, tank.getCapacity(), probeInfo.defaultProgressStyle().suffix(" mB").numberFormat(NumberFormat.COMPACT).filledColor(color).alternateFilledColor(color).backgroundColor(0xff000000).borderColor(0xffffffff))
                        .text(fluidName);
            } else {
                addTankInfo(probeInfo, master.tanks[0]);
                addTankInfo(probeInfo, master.tanks[1]);
                addTankInfo(probeInfo, master.tanks[2]);
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

        private void addTankInfo(IProbeInfo probeInfo, FluidTank tank) {
            FluidStack fluid = tank.getFluid();
            int amount = fluid != null ? fluid.amount : 0;
            String fluidName = fluid != null ? fluid.getLocalizedName() : "Empty";
            int color = getFluidColor(fluid);
            probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER).spacing(2))
                    .progress(amount, tank.getCapacity(), probeInfo.defaultProgressStyle().suffix(" mB").numberFormat(NumberFormat.COMPACT).filledColor(color).alternateFilledColor(color).backgroundColor(0xff000000).borderColor(0xffffffff))
                    .text(fluidName);
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
                int color = getFluidColor(fluid);
                probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER).spacing(2))
                        .progress(amount, tank.getCapacity(), probeInfo.defaultProgressStyle().suffix(" mB").numberFormat(NumberFormat.COMPACT).filledColor(color).alternateFilledColor(color).backgroundColor(0xff000000).borderColor(0xffffffff))
                        .text(fluidName);
            } else {
                addTankInfo(probeInfo, master.tanks[0]);
                addTankInfo(probeInfo, master.tanks[1]);
                addTankInfo(probeInfo, master.tanks[2]);
                addTankInfo(probeInfo, master.tanks[3]);
                addTankInfo(probeInfo, master.tanks[4]);
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

        private void addTankInfo(IProbeInfo probeInfo, FluidTank tank) {
            FluidStack fluid = tank.getFluid();
            int amount = fluid != null ? fluid.amount : 0;
            String fluidName = fluid != null ? fluid.getLocalizedName() : "Empty";
            int color = getFluidColor(fluid);
            probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER).spacing(2))
                    .progress(amount, tank.getCapacity(), probeInfo.defaultProgressStyle().suffix(" mB").numberFormat(NumberFormat.COMPACT).filledColor(color).alternateFilledColor(color).backgroundColor(0xff000000).borderColor(0xffffffff))
                    .text(fluidName);
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
                int color = getFluidColor(fluid);
                probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER).spacing(2))
                        .progress(amount, tank.getCapacity(), probeInfo.defaultProgressStyle().suffix(" mB").numberFormat(NumberFormat.COMPACT).filledColor(color).alternateFilledColor(color).backgroundColor(0xff000000).borderColor(0xffffffff))
                        .text(fluidName);
            } else {
                addTankInfo(probeInfo, master.tanks[0]);
                addTankInfo(probeInfo, master.tanks[1]);
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

        private void addTankInfo(IProbeInfo probeInfo, FluidTank tank) {
            FluidStack fluid = tank.getFluid();
            int amount = fluid != null ? fluid.amount : 0;
            String fluidName = fluid != null ? fluid.getLocalizedName() : "Empty";
            int color = getFluidColor(fluid);
            probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER).spacing(2))
                    .progress(amount, tank.getCapacity(), probeInfo.defaultProgressStyle().suffix(" mB").numberFormat(NumberFormat.COMPACT).filledColor(color).alternateFilledColor(color).backgroundColor(0xff000000).borderColor(0xffffffff))
                    .text(fluidName);
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
                int color = getFluidColor(fluid);
                probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER).spacing(2))
                        .progress(amount, tank.getCapacity(), probeInfo.defaultProgressStyle().suffix(" mB").numberFormat(NumberFormat.COMPACT).filledColor(color).alternateFilledColor(color).backgroundColor(0xff000000).borderColor(0xffffffff))
                        .text(fluidName);
            } else {
                addTankInfo(probeInfo, master.tanks[0]);
                addTankInfo(probeInfo, master.tanks[1]);
                addTankInfo(probeInfo, master.tanks[2]);
                addTankInfo(probeInfo, master.tanks[3]);
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

        private void addTankInfo(IProbeInfo probeInfo, FluidTank tank) {
            FluidStack fluid = tank.getFluid();
            int amount = fluid != null ? fluid.amount : 0;
            String fluidName = fluid != null ? fluid.getLocalizedName() : "Empty";
            int color = getFluidColor(fluid);
            probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER).spacing(2))
                    .progress(amount, tank.getCapacity(), probeInfo.defaultProgressStyle().suffix(" mB").numberFormat(NumberFormat.COMPACT).filledColor(color).alternateFilledColor(color).backgroundColor(0xff000000).borderColor(0xffffffff))
                    .text(fluidName);
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
                int color = getFluidColor(fluid);
                if (!master.isFluidInputPosition(facing, pos) && !master.isFluidOutputPosition(facing, pos)) return;
                probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER).spacing(2))
                        .progress(amount, tank.getCapacity(), probeInfo.defaultProgressStyle().suffix(" mB").numberFormat(NumberFormat.COMPACT).filledColor(color).alternateFilledColor(color).backgroundColor(0xff000000).borderColor(0xffffffff))
                        .text(fluidName);
            } else {
                addTankInfo(probeInfo, master.tanks[0]);
                addTankInfo(probeInfo, master.tanks[1]);
            }
            if (master.isEnergyPosition(facing, pos)) {
                if (!master.isStarterPosition(facing, pos) && !master.isSparkplugPosition(facing, pos)) return;
                IEnergyStorage storage = master.getEnergyAtPosition(facing, pos);
                if (storage == null) return;
                probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER).spacing(2))
                        .progress(storage.getEnergyStored(), storage.getMaxEnergyStored(), probeInfo.defaultProgressStyle().suffix(" IF").filledColor(Lib.COLOUR_I_ImmersiveOrange).alternateFilledColor(0xff994f20).borderColor(Lib.COLOUR_I_ImmersiveOrangeShadow).numberFormat(NumberFormat.COMPACT));
            } else {
                addEnergyInfo(probeInfo, master.starterStorage);
                addEnergyInfo(probeInfo, master.sparkplugStorage);
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

        private void addTankInfo(IProbeInfo probeInfo, FluidTank tank) {
            FluidStack fluid = tank.getFluid();
            int amount = fluid != null ? fluid.amount : 0;
            String fluidName = fluid != null ? fluid.getLocalizedName() : "Empty";
            int color = getFluidColor(fluid);
            probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER).spacing(2))
                    .progress(amount, tank.getCapacity(), probeInfo.defaultProgressStyle().suffix(" mB").numberFormat(NumberFormat.COMPACT).filledColor(color).alternateFilledColor(color).backgroundColor(0xff000000).borderColor(0xffffffff))
                    .text(fluidName);
        }

        private void addEnergyInfo(IProbeInfo probeInfo, IEnergyStorage storage) {
            probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER).spacing(2))
                    .progress(storage.getEnergyStored(), storage.getMaxEnergyStored(), probeInfo.defaultProgressStyle().suffix(" IF").filledColor(Lib.COLOUR_I_ImmersiveOrange).alternateFilledColor(0xff994f20).borderColor(Lib.COLOUR_I_ImmersiveOrangeShadow).numberFormat(NumberFormat.COMPACT));
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
                int color = getFluidColor(fluid);
                probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER).spacing(2))
                        .progress(amount, tank.getCapacity(), probeInfo.defaultProgressStyle().suffix(" mB").numberFormat(NumberFormat.COMPACT).filledColor(color).alternateFilledColor(color).backgroundColor(0xff000000).borderColor(0xffffffff))
                        .text(fluidName);
            } else {
                addTankInfo(probeInfo, master.tanks[0]);
                addTankInfo(probeInfo, master.tanks[1]);
                addTankInfo(probeInfo, master.tanks[2]);
                addTankInfo(probeInfo, master.tanks[3]);
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

        private void addTankInfo(IProbeInfo probeInfo, FluidTank tank) {
            FluidStack fluid = tank.getFluid();
            int amount = fluid != null ? fluid.amount : 0;
            String fluidName = fluid != null ? fluid.getLocalizedName() : "Empty";
            int color = getFluidColor(fluid);
            probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER).spacing(2))
                    .progress(amount, tank.getCapacity(), probeInfo.defaultProgressStyle().suffix(" mB").numberFormat(NumberFormat.COMPACT).filledColor(color).alternateFilledColor(color).backgroundColor(0xff000000).borderColor(0xffffffff))
                    .text(fluidName);
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
                int color = getFluidColor(fluid);
                probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER).spacing(2))
                        .progress(amount, tank.getCapacity(), probeInfo.defaultProgressStyle().suffix(" mB").numberFormat(NumberFormat.COMPACT).filledColor(color).alternateFilledColor(color).backgroundColor(0xff000000).borderColor(0xffffffff))
                        .text(fluidName);
            } else {
                addTankInfo(probeInfo, master.tanks[0]);
                addTankInfo(probeInfo, master.tanks[1]);
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

        private void addTankInfo(IProbeInfo probeInfo, FluidTank tank) {
            FluidStack fluid = tank.getFluid();
            int amount = fluid != null ? fluid.amount : 0;
            String fluidName = fluid != null ? fluid.getLocalizedName() : "Empty";
            int color = getFluidColor(fluid);
            probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER).spacing(2))
                    .progress(amount, tank.getCapacity(), probeInfo.defaultProgressStyle().suffix(" mB").numberFormat(NumberFormat.COMPACT).filledColor(color).alternateFilledColor(color).backgroundColor(0xff000000).borderColor(0xffffffff))
                    .text(fluidName);
        }
    }

    public static class MechanicalEnergyProvider implements IProbeInfoProvider {
        @Override public String getID() { return ImmersiveTechnology.MODID + ":" + "MechanicalEnergyInfo"; }

        @Override public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
            TileEntity te = world.getTileEntity(data.getPos());
            if (te instanceof IMechanicalEnergy) {
                TileEntityMultiblockPart<?> master = ((TileEntityMultiblockPart<?>)te).master();
                if (master == null) return;
                int current = ((IMechanicalEnergy)master).getSpeed();
                probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER).spacing(2))
                        .progress(current, maxSpeed, probeInfo.defaultProgressStyle().numberFormat(NumberFormat.FULL).suffix(" RPM"));
            }
        }
    }

    public static class MiscProvider implements IProbeInfoProvider {
        @Override public String getID() { return ImmersiveTechnology.MODID + ":" + "MiscInfo"; }

        @Override public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
            TileEntity te = world.getTileEntity(data.getPos());
            if (te instanceof TileEntityBoilerSlave) {
                TileEntityBoilerMaster master = ((TileEntityBoilerSlave)te).master();
                if (master == null) return;
                int current = (int)(master.heatLevel / workingHeatLevel * 100);
                probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER).spacing(2))
                        .progress(current, 100, probeInfo.defaultProgressStyle().numberFormat(NumberFormat.FULL).suffix("%"));
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
                int color = getFluidColor(fluid);
                probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER).spacing(2))
                        .progress(amount, tank.getCapacity(), probeInfo.defaultProgressStyle().suffix(" mB").numberFormat(NumberFormat.COMPACT).filledColor(color).alternateFilledColor(color).backgroundColor(0xff000000).borderColor(0xffffffff))
                        .text(fluidName);
            } else {
                addTankInfo(probeInfo, master.tanks[0]);
                addTankInfo(probeInfo, master.tanks[1]);
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

        private void addTankInfo(IProbeInfo probeInfo, FluidTank tank) {
            FluidStack fluid = tank.getFluid();
            int amount = fluid != null ? fluid.amount : 0;
            String fluidName = fluid != null ? fluid.getLocalizedName() : "Empty";
            int color = getFluidColor(fluid);
            probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER).spacing(2))
                    .progress(amount, tank.getCapacity(), probeInfo.defaultProgressStyle().suffix(" mB").numberFormat(NumberFormat.COMPACT).filledColor(color).alternateFilledColor(color).backgroundColor(0xff000000).borderColor(0xffffffff))
                    .text(fluidName);
        }
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

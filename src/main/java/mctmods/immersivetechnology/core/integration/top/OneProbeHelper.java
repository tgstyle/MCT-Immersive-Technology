package mctmods.immersivetechnology.core.integration.top;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockBE;
import javax.annotation.Nullable;
import mcjty.theoneprobe.api.*;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.*;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.MeltingRecipe;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.SolarTowerRecipe;
import mctmods.immersivetechnology.common.multiblocks.stone.logic.CoolingTowerLogic;
import mctmods.immersivetechnology.core.CommonConfig;
import mctmods.immersivetechnology.core.lib.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class OneProbeHelper {
    private static final double solarWorkingHeatLevel = CommonConfig.solarTowerWorkingHeatLevel;
    private static final double solarMelterWorkingHeatLevel = CommonConfig.solarMelterWorkingHeatLevel;

    public static void register(ITheOneProbe top) {
        top.registerProvider(new AlternatorProvider());
        top.registerProvider(new BoilerLiquidProvider());
        top.registerProvider(new BoilerSolidProvider());
        top.registerProvider(new BoilerTankProvider());
        top.registerProvider(new CoolingTowerProvider());
        top.registerProvider(new DistillerProvider());
        top.registerProvider(new ElectrolyticCrucibleBatteryProvider());
        top.registerProvider(new GasTurbineProvider());
        top.registerProvider(new HeatExchangerProvider());
        top.registerProvider(new MeltingCrucibleProvider());
        top.registerProvider(new RadiatorProvider());
        top.registerProvider(new SolarMelterProvider());
        top.registerProvider(new SolarTowerProvider());
        top.registerProvider(new SteamTurbineProvider());
        top.registerProvider(new SteelSheetmetalTankProvider());
    }

    private static void addFluidTankDisplay(IProbeInfo probeInfo, FluidTank tank) {
        FluidStack fluid = tank.getFluid();
        int amount = !fluid.isEmpty() ? fluid.getAmount() : 0;
        String fluidName = !fluid.isEmpty() ? fluid.getDisplayName().getString() : "Empty";
        int color = getFluidColor(fluid);
        probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER).spacing(2))
                .progress(amount, tank.getCapacity(), probeInfo.defaultProgressStyle().suffix(" mB").numberFormat(NumberFormat.COMPACT).filledColor(color).alternateFilledColor(color).backgroundColor(0xff000000).borderColor(0xffffffff))
                .text(fluidName);
    }

    private static void addFluidTanks(IProbeInfo probeInfo, FluidTank... tanks) {
        for (FluidTank tank : tanks) {
            if (tank != null) { addFluidTankDisplay(probeInfo, tank); }
        }
    }

    private static void addEnergyDisplay(IProbeInfo probeInfo, int stored, int max) {
        probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER).spacing(2))
                .progress(stored, max, probeInfo.defaultProgressStyle().suffix(" IF").filledColor(Lib.COLOUR_I_ImmersiveOrange).alternateFilledColor(0xff994f20).borderColor(Lib.COLOUR_I_ImmersiveOrangeShadow).numberFormat(NumberFormat.COMPACT));
    }

    private static void addRPMDisplay(IProbeInfo probeInfo, int speed, int maxRpm) {
        probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER).spacing(2))
                .progress(speed, maxRpm, probeInfo.defaultProgressStyle().numberFormat(NumberFormat.FULL).suffix(" RPM"));
    }

    private static void addProcessPercent(IProbeInfo probeInfo, int percent) {
        probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER).spacing(2))
                .progress(percent, 100, probeInfo.defaultProgressStyle().numberFormat(NumberFormat.FULL).suffix("%"));
    }

    private static void addTemperature(IProbeInfo probeInfo, double heatLevel, double workingLevel) {
        int current = (int)heatLevel;
        int max = (int)workingLevel;
        probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER).spacing(2))
                .progress(current, max, probeInfo.defaultProgressStyle()
                        .suffix(" °C")
                        .filledColor(0xffcc0000)
                        .alternateFilledColor(0xffcc0000)
                        .borderColor(0xffff6666)
                        .numberFormat(NumberFormat.FULL));
    }

    private static IMultiblockContext<?> getContext(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof IMultiblockBE<?> mbbe) { return mbbe.getHelper().getContext(); }
        return null;
    }

    public static class AlternatorProvider implements IProbeInfoProvider {
        @Override public ResourceLocation getID() { return ResourceLocation.fromNamespaceAndPath(Reference.MODID, "alternator"); }

        @Override public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level level, BlockState blockState, IProbeHitData data) {
            IMultiblockContext<?> ctx = getContext(level, data.getPos());
            if (ctx == null) { return; }
            if (ctx.getState() instanceof AlternatorLogic.State state) {
                addEnergyDisplay(probeInfo, state.energy.getEnergyStored(), state.energy.getMaxEnergyStored());
                addRPMDisplay(probeInfo, state.speed, state.effectiveMaxSpeed);
            }
        }
    }

    public static class BoilerLiquidProvider implements IProbeInfoProvider {
        @Override public ResourceLocation getID() { return ResourceLocation.fromNamespaceAndPath(Reference.MODID, "boiler_liquid"); }

        @Override public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level level, BlockState blockState, IProbeHitData data) {
            IMultiblockContext<?> ctx = getContext(level, data.getPos());
            if (ctx == null) { return; }
            if (ctx.getState() instanceof BoilerLiquidLogic.State state) {
                addFluidTankDisplay(probeInfo, state.tanks.input1());
                addTemperature(probeInfo, state.heatLevel, state.getWorkingHeatLevel());
                addProcessPercent(probeInfo, state.burnPercent);
            }
        }
    }

    public static class BoilerSolidProvider implements IProbeInfoProvider {
        @Override public ResourceLocation getID() { return ResourceLocation.fromNamespaceAndPath(Reference.MODID, "boiler_solid"); }

        @Override public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level level, BlockState blockState, IProbeHitData data) {
            IMultiblockContext<?> ctx = getContext(level, data.getPos());
            if (ctx == null) { return; }
            if (ctx.getState() instanceof BoilerSolidLogic.State state) {
                addTemperature(probeInfo, state.heatLevel, state.getWorkingHeatLevel());
                int currentProg = (state.totalBurnTime > 0 && state.burnRemaining > 0) ? (state.totalBurnTime - state.burnRemaining) * 100 / state.totalBurnTime : 0;
                addProcessPercent(probeInfo, currentProg);
            }
        }
    }

    public static class BoilerTankProvider implements IProbeInfoProvider {
        @Override public ResourceLocation getID() { return ResourceLocation.fromNamespaceAndPath(Reference.MODID, "boiler_tank"); }

        @Override public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level level, BlockState blockState, IProbeHitData data) {
            IMultiblockContext<?> ctx = getContext(level, data.getPos());
            if (ctx == null) { return; }
            if (ctx.getState() instanceof BoilerTankLogic.State state) {
                addFluidTankDisplay(probeInfo, state.tanks.input());
                addFluidTankDisplay(probeInfo, state.tanks.output());
                addTemperature(probeInfo, state.heatLevel, state.getWorkingHeatLevel());
                int currentProg = (state.totalProcessTime > 0 && state.recipeTimeRemaining > 0) ? (state.totalProcessTime - state.recipeTimeRemaining) * 100 / state.totalProcessTime : 0;
                addProcessPercent(probeInfo, currentProg);
            }
        }
    }

    public static class CoolingTowerProvider implements IProbeInfoProvider {
        @Override public ResourceLocation getID() { return ResourceLocation.fromNamespaceAndPath(Reference.MODID, "cooling_tower"); }

        @Override public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level level, BlockState blockState, IProbeHitData data) {
            IMultiblockContext<?> ctx = getContext(level, data.getPos());
            if (ctx == null) { return; }
            if (ctx.getState() instanceof CoolingTowerLogic.State state) {
                addFluidTanks(probeInfo, state.tanks.input0(), state.tanks.input1(), state.tanks.output0(), state.tanks.output1(), state.tanks.output2());
                for (int percent : state.processPercents) {
                    if (percent >= 0) { addProcessPercent(probeInfo, percent); }
                }
            }
        }
    }

    public static class DistillerProvider implements IProbeInfoProvider {
        @Override public ResourceLocation getID() { return ResourceLocation.fromNamespaceAndPath(Reference.MODID, "distiller"); }

        @Override public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level level, BlockState blockState, IProbeHitData data) {
            IMultiblockContext<?> ctx = getContext(level, data.getPos());
            if (ctx == null) { return; }
            if (ctx.getState() instanceof DistillerLogic.State state) {
                addFluidTankDisplay(probeInfo, state.tanks.input());
                addFluidTankDisplay(probeInfo, state.tanks.output());
                addEnergyDisplay(probeInfo, state.energy.getEnergyStored(), state.energy.getMaxEnergyStored());
                if (state.queueSize > 0) {
                    probeInfo.text("Processing (" + state.queueSize + " queued)");
                }
            }
        }
    }

    public static class ElectrolyticCrucibleBatteryProvider implements IProbeInfoProvider {
        @Override public ResourceLocation getID() { return ResourceLocation.fromNamespaceAndPath(Reference.MODID, "electrolytic_crucible_battery"); }

        @Override public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level level, BlockState blockState, IProbeHitData data) {
            IMultiblockContext<?> ctx = getContext(level, data.getPos());
            if (ctx == null) { return; }
            if (ctx.getState() instanceof ElectrolyticCrucibleBatteryLogic.State state) {
                addFluidTanks(probeInfo, state.tanks.input(), state.tanks.output0(), state.tanks.output1(), state.tanks.output2());
                addEnergyDisplay(probeInfo, state.energy.getEnergyStored(), state.energy.getMaxEnergyStored());
                for (int percent : state.processPercents) {
                    if (percent >= 0) { addProcessPercent(probeInfo, percent); }
                }
            }
        }
    }

    public static class GasTurbineProvider implements IProbeInfoProvider {
        @Override public ResourceLocation getID() { return ResourceLocation.fromNamespaceAndPath(Reference.MODID, "gas_turbine"); }

        @Override public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level level, BlockState blockState, IProbeHitData data) {
            IMultiblockContext<?> ctx = getContext(level, data.getPos());
            if (ctx == null) { return; }
            if (ctx.getState() instanceof GasTurbineLogic.State state) {
                addFluidTankDisplay(probeInfo, state.tanks.input());
                addFluidTankDisplay(probeInfo, state.tanks.output());
                addRPMDisplay(probeInfo, state.speed, state.effectiveMaxSpeed);
            }
        }
    }

    public static class HeatExchangerProvider implements IProbeInfoProvider {
        @Override public ResourceLocation getID() { return ResourceLocation.fromNamespaceAndPath(Reference.MODID, "heat_exchanger"); }

        @Override public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level level, BlockState blockState, IProbeHitData data) {
            BlockEntity be = level.getBlockEntity(data.getPos());
            if (!(be instanceof IMultiblockBE<?> mbbe)) { return; }
            IMultiblockContext<?> ctx = mbbe.getHelper().getContext();
            if (ctx == null) { return; }
            if (ctx.getState() instanceof HeatExchangerLogic.State state) {
                addFluidTanks(probeInfo, state.tanks.input0(), state.tanks.input1(), state.tanks.output0(), state.tanks.output1());
                addEnergyDisplay(probeInfo, state.energy.getEnergyStored(), state.energy.getMaxEnergyStored());
                if (state.queueSize > 0) {
                    int percent = state.totalProcessTime > 0 ? state.processProgress * 100 / state.totalProcessTime : 0;
                    addProcessPercent(probeInfo, percent);
                    if (state.queueSize > 1) {
                        probeInfo.text((state.queueSize - 1) + " queued");
                    }
                }
            }
        }
    }

    public static class MeltingCrucibleProvider implements IProbeInfoProvider {
        @Override public ResourceLocation getID() { return ResourceLocation.fromNamespaceAndPath(Reference.MODID, "melting_crucible"); }

        @Override public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level level, BlockState blockState, IProbeHitData data) {
            IMultiblockContext<?> ctx = getContext(level, data.getPos());
            if (ctx == null) { return; }
            if (ctx.getState() instanceof MeltingCrucibleLogic.State state) {
                addFluidTankDisplay(probeInfo, state.tanks.input());
                addFluidTankDisplay(probeInfo, state.tanks.output());
                addEnergyDisplay(probeInfo, state.energy.getEnergyStored(), state.energy.getMaxEnergyStored());
                FluidStack input = state.tanks.input().getFluid();
                MeltingRecipe recipe = input.isEmpty() ? null : MeltingRecipe.findRecipe(level, input);
                double workingLevel = recipe != null ? recipe.requiredTemp : MeltingCrucibleLogic.WORKING_HEAT_LEVEL;
                addTemperature(probeInfo, state.heatLevel, workingLevel);

                if (state.queueSize > 0) {
                    probeInfo.text("Processing (" + state.queueSize + " queued)");
                }
            }
        }
    }

    public static class RadiatorProvider implements IProbeInfoProvider {
        @Override public ResourceLocation getID() { return ResourceLocation.fromNamespaceAndPath(Reference.MODID, "radiator"); }

        @Override public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level level, BlockState blockState, IProbeHitData data) {
            IMultiblockContext<?> ctx = getContext(level, data.getPos());
            if (ctx == null) { return; }
            if (ctx.getState() instanceof RadiatorLogic.State state) {
                addFluidTanks(probeInfo, state.tanks.input(), state.tanks.output());
                if (state.active) {
                    int percent = state.totalProcessTime > 0 ? state.processProgress * 100 / state.totalProcessTime : 0;
                    addProcessPercent(probeInfo, percent);
                }
                probeInfo.text("Active processes: " + state.queueSize);
                probeInfo.text("Reflector efficiency");
                addProcessPercent(probeInfo, (int) Math.round(state.radiationEfficiency * 100.0D));
            } else if (ctx.getState() instanceof RadiatorHorizontalLogic.State state) {
                addFluidTanks(probeInfo, state.tanks.input(), state.tanks.output());
                if (state.active) {
                    int percent = state.totalProcessTime > 0 ? state.processProgress * 100 / state.totalProcessTime : 0;
                    addProcessPercent(probeInfo, percent);
                }
                probeInfo.text("Active processes: " + state.queueSize);
                probeInfo.text("Reflector efficiency");
                addProcessPercent(probeInfo, (int) Math.round(state.radiationEfficiency * 100.0D));
            }
        }
    }

    public static class SolarMelterProvider implements IProbeInfoProvider {
        @Override public ResourceLocation getID() { return ResourceLocation.fromNamespaceAndPath(Reference.MODID, "solar_melter"); }

        @Override public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level level, BlockState blockState, IProbeHitData data) {
            IMultiblockContext<?> ctx = getContext(level, data.getPos());
            if (ctx == null) { return; }
            if (ctx.getState() instanceof SolarMelterLogic.State state) {
                addFluidTankDisplay(probeInfo, state.tanks.input());
                addFluidTankDisplay(probeInfo, state.tanks.output());
                FluidStack input = state.tanks.input().getFluid();
                MeltingRecipe recipe = input.isEmpty() ? null : MeltingRecipe.findRecipe(level, input);
                double workingLevel = recipe != null ? recipe.requiredTemp : solarMelterWorkingHeatLevel;
                addTemperature(probeInfo, state.heatLevel, workingLevel);
                int percent = (state.totalProcessTime > 0) ? state.processProgress * 100 / state.totalProcessTime : 0;
                addProcessPercent(probeInfo, percent);
            }
        }
    }

    public static class SolarTowerProvider implements IProbeInfoProvider {
        @Override public ResourceLocation getID() { return ResourceLocation.fromNamespaceAndPath(Reference.MODID, "solar_tower"); }

        @Override public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level level, BlockState blockState, IProbeHitData data) {
            IMultiblockContext<?> ctx = getContext(level, data.getPos());
            if (ctx == null) { return; }
            if (ctx.getState() instanceof SolarTowerLogic.State state) {
                addFluidTankDisplay(probeInfo, state.tanks.input());
                addFluidTankDisplay(probeInfo, state.tanks.output());
                FluidStack input = state.tanks.input().getFluid();
                SolarTowerRecipe recipe = input.isEmpty() ? null : SolarTowerRecipe.findRecipe(level, input);
                double workingLevel = recipe != null ? recipe.requiredTemp : solarWorkingHeatLevel;
                addTemperature(probeInfo, state.heatLevel, workingLevel);
                int percent = (state.totalProcessTime > 0) ? state.processProgress * 100 / state.totalProcessTime : 0;
                addProcessPercent(probeInfo, percent);
            }
        }
    }

    public static class SteamTurbineProvider implements IProbeInfoProvider {
        @Override public ResourceLocation getID() { return ResourceLocation.fromNamespaceAndPath(Reference.MODID, "steam_turbine"); }

        @Override public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level level, BlockState blockState, IProbeHitData data) {
            IMultiblockContext<?> ctx = getContext(level, data.getPos());
            if (ctx == null) { return; }
            if (ctx.getState() instanceof SteamTurbineLogic.State state) {
                addFluidTankDisplay(probeInfo, state.tanks.input());
                addFluidTankDisplay(probeInfo, state.tanks.output());
                addRPMDisplay(probeInfo, state.speed, state.effectiveMaxSpeed);
            }
        }
    }

    public static class SteelSheetmetalTankProvider implements IProbeInfoProvider {
        @Override public ResourceLocation getID() { return ResourceLocation.fromNamespaceAndPath(Reference.MODID, "steel_tank"); }

        @Override public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level level, BlockState blockState, IProbeHitData data) {
            IMultiblockContext<?> ctx = getContext(level, data.getPos());
            if (ctx == null) { return; }
            if (ctx.getState() instanceof SteelSheetmetalTankLogic.State state) {
                addFluidTankDisplay(probeInfo, state.tank);
            }
        }
    }

    private static int getFluidColor(@Nullable FluidStack fluid) {
        if (fluid == null || fluid.isEmpty()) { return 0xff555555; }
        IClientFluidTypeExtensions ext = IClientFluidTypeExtensions.of(fluid.getFluid());
        int tint = ext.getTintColor(fluid);
        ResourceLocation still = ext.getStillTexture(fluid);
        var sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(still);
        if (sprite == null) { return 0xff000000 | (tint & 0x00ffffff); }
        int width = sprite.contents().width();
        int height = sprite.contents().height();
        int avgR = 0, avgG = 0, avgB = 0, count = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int p = sprite.getPixelRGBA(0, x, y);
                int alpha = (p >> 24) & 0xff;
                if (alpha > 0) {
                    int r = p & 0xff;
                    int g = (p >> 8) & 0xff;
                    int b = (p >> 16) & 0xff;
                    avgR += (r * alpha) / 255;
                    avgG += (g * alpha) / 255;
                    avgB += (b * alpha) / 255;
                    count++;
                }
            }
        }
        if (count > 0) {
            avgR /= count;
            avgG /= count;
            avgB /= count;
        } else {
            return 0xff000000 | (tint & 0x00ffffff);
        }
        int r = (avgR * ((tint >> 16) & 0xff)) / 255;
        int g = (avgG * ((tint >> 8) & 0xff)) / 255;
        int b = (avgB * (tint & 0xff)) / 255;
        return 0xff000000 | r << 16 | g << 8 | b;
    }
}

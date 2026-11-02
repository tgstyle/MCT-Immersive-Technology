package mctmods.immersivetechnology.core.registration;

import mctmods.immersivetechnology.common.blocks.metal.logic.TrashItemBlockEntity;
import mctmods.immersivetechnology.common.blocks.metal.logic.ValveFluidBlockEntity;
import mctmods.immersivetechnology.common.blocks.metal.logic.ValveLimiterBlockEntity;
import mctmods.immersivetechnology.common.blocks.metal.logic.ValveLoadBlockEntity;
import mctmods.immersivetechnology.common.blocks.wooden.logic.CrateCreativeBlockEntity;
import mctmods.immersivetechnology.common.gui.AdvancedCokeOvenMenu;
import mctmods.immersivetechnology.common.gui.BoilerLiquidMenu;
import mctmods.immersivetechnology.common.gui.BoilerSolidMenu;
import mctmods.immersivetechnology.common.gui.BoilerTankMenu;
import mctmods.immersivetechnology.common.gui.CrateMenu;
import mctmods.immersivetechnology.common.gui.DistillerMenu;
import mctmods.immersivetechnology.common.gui.FluidValveMenu;
import mctmods.immersivetechnology.common.gui.LoadControllerMenu;
import mctmods.immersivetechnology.common.gui.MeltingCrucibleMenu;
import mctmods.immersivetechnology.common.gui.SolarMenu;
import mctmods.immersivetechnology.common.gui.StackLimiterMenu;
import mctmods.immersivetechnology.common.gui.StyleNOXMenu;
import mctmods.immersivetechnology.common.gui.TrashItemMenu;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.*;
import mctmods.immersivetechnology.common.multiblocks.stone.logic.AdvancedCokeOvenLogic;

import com.immersiveconvergence.api.ICMods;
import com.immersiveconvergence.api.gui.ArgContainer;
import com.immersiveconvergence.api.gui.BaseContainerMenu;
import com.immersiveconvergence.api.multiblock.blocks.env.IMultiblockContext;
import com.immersiveconvergence.api.multiblock.blocks.logic.IMultiblockState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.function.Supplier;

public class MenuTypes {
    public static final DeferredRegister<MenuType<?>> REGISTER = DeferredRegister.create(Registries.MENU, "immersivetechnology");
    private static final DeferredRegister<MenuType<?>> REGISTER_CONTENT = DeferredRegister.create(Registries.MENU, "immersivetechnology");
    public static final MultiblockContainer<AdvancedCokeOvenLogic.State, AdvancedCokeOvenMenu> ADVANCED_COKE_OVEN_MENU = registerMultiblock("gui_advanced_coke_oven", AdvancedCokeOvenMenu::makeServer, (type, id, inv, buffer) -> AdvancedCokeOvenMenu.makeClient(type, id, inv));
    public static final MultiblockContainer<BoilerLiquidLogic.State, BoilerLiquidMenu> BOILER_LIQUID_MENU = registerMultiblock("gui_boiler", BoilerLiquidMenu::makeServer, (type, id, inv, buffer) -> BoilerLiquidMenu.makeClient(type, id, inv));
    public static final MultiblockContainer<BoilerSolidLogic.State, BoilerSolidMenu> BOILER_SOLID_MENU = registerMultiblock("gui_solid", BoilerSolidMenu::makeServer, (type, id, inv, buffer) -> BoilerSolidMenu.makeClient(type, id, inv));
    public static final MultiblockContainer<BoilerTankLogic.State, BoilerTankMenu> BOILER_TANK_MENU = registerMultiblock("gui_boiler_tank", BoilerTankMenu::makeServer, (type, id, inv, buffer) -> BoilerTankMenu.makeClient(type, id, inv));
    public static final MultiblockContainer<DistillerLogic.State, DistillerMenu> DISTILLER_MENU = registerMultiblock("gui_distiller", DistillerMenu::makeServer, (type, id, inv, buffer) -> DistillerMenu.makeClient(type, id, inv));
    public static final MultiblockContainer<MeltingCrucibleLogic.State, MeltingCrucibleMenu> MELTING_CRUCIBLE_MENU = registerMultiblock("gui_melting_crucible", MeltingCrucibleMenu::makeServer, (type, id, inv, buffer) -> MeltingCrucibleMenu.makeClient(type, id, inv));
    public static final MultiblockContainer<SolarMelterLogic.State, SolarMenu> SOLAR_MELTER_MENU = registerMultiblock("gui_solar_melter", SolarMenu::makeServer, (type, id, inv, buffer) -> SolarMenu.makeClient(type, id, inv));
    public static final MultiblockContainer<SolarTowerLogic.State, SolarMenu> SOLAR_TOWER_MENU = registerMultiblock("gui_solar_tower", SolarMenu::makeServer, (type, id, inv, buffer) -> SolarMenu.makeClient(type, id, inv));
    public static final MultiblockContainer<StyleNOXLogic.State, StyleNOXMenu> STYLENOX_MENU = registerMultiblock("gui_stylenox", StyleNOXMenu::makeServer, (type, id, inv, buffer) -> StyleNOXMenu.makeClient(type, id, inv));
    public static final ArgContainer<CrateCreativeBlockEntity, CrateMenu> CRATE_CREATIVE = registerArg("crate_creative", CrateMenu::makeServer, (type, id, inv, buffer) -> CrateMenu.makeClient(type, id, inv));
    public static final ArgContainer<TrashItemBlockEntity, TrashItemMenu> TRASH_ITEM = registerArg("trash_item", TrashItemMenu::makeServer, (type, id, inv, buffer) -> TrashItemMenu.makeClient(type, id, inv));
    public static final ArgContainer<ValveFluidBlockEntity, FluidValveMenu> VALVE_FLUID = ArgContainer.register(REGISTER_CONTENT, "valve_fluid", FluidValveMenu::makeServer, FluidValveMenu::makeClient);
    public static final ArgContainer<ValveLoadBlockEntity, LoadControllerMenu> VALVE_LOAD = ArgContainer.register(REGISTER_CONTENT, "valve_load", LoadControllerMenu::makeServer, LoadControllerMenu::makeClient);
    public static final ArgContainer<ValveLimiterBlockEntity, StackLimiterMenu> VALVE_LIMITER = ArgContainer.register(REGISTER_CONTENT, "valve_limiter", StackLimiterMenu::makeServer, StackLimiterMenu::makeClient);

    public static void init(IEventBus event) {
        REGISTER.register(event);
        if (ICMods.ieContent()) { REGISTER_CONTENT.register(event); }
    }

    public static <T, C extends BaseContainerMenu> ArgContainer<T, C> registerArg(String name, ArgContainer.IArgContainerConstructor<T, C> container, ArgContainer.IClientContainerConstructor<C> client) { return ArgContainer.register(REGISTER, name, container, client); }

    public static <S extends IMultiblockState, C extends BaseContainerMenu> MultiblockContainer<S, C> registerMultiblock(String name, ArgContainer.IArgContainerConstructor<BaseContainerMenu.MultiblockMenuContext<S>, C> container, ArgContainer.IClientContainerConstructor<C> client) { return new MultiblockContainer<>(ArgContainer.registerType(REGISTER, name, client), container); }

    public static class MultiblockContainer<S extends IMultiblockState, C extends BaseContainerMenu> extends ArgContainer<BaseContainerMenu.MultiblockMenuContext<S>, C> {
        private MultiblockContainer(Supplier<MenuType<C>> type, ArgContainer.IArgContainerConstructor<BaseContainerMenu.MultiblockMenuContext<S>, C> factory) { super(type, factory); }

        public MenuProvider provide(IMultiblockContext<S> ctx, BlockPos relativeClicked) { return this.provide(new BaseContainerMenu.MultiblockMenuContext<>(ctx, ctx.getLevel().toAbsolute(relativeClicked))); }
    }
}

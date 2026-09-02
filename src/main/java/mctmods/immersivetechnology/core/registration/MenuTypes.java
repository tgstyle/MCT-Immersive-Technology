package mctmods.immersivetechnology.core.registration;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import mctmods.immersivetechnology.common.blocks.connectors.logic.ConnectorTimerBlockEntity;
import mctmods.immersivetechnology.common.blocks.metal.gui.*;
import mctmods.immersivetechnology.common.blocks.metal.logic.TrashItemBlockEntity;
import mctmods.immersivetechnology.common.blocks.metal.logic.ValveFluidBlockEntity;
import mctmods.immersivetechnology.common.blocks.metal.logic.ValveLimiterBlockEntity;
import mctmods.immersivetechnology.common.blocks.metal.logic.ValveLoadBlockEntity;
import mctmods.immersivetechnology.common.blocks.wooden.gui.CrateCreativeMenu;
import mctmods.immersivetechnology.common.blocks.wooden.logic.CrateCreativeBlockEntity;
import mctmods.immersivetechnology.common.blocks.connectors.gui.ConnectorTimerMenu;
import com.immersiveconvergence.api.gui.ArgContainer;
import com.immersiveconvergence.api.gui.BaseContainerMenu;
import mctmods.immersivetechnology.common.multiblocks.gui.*;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.*;
import mctmods.immersivetechnology.common.multiblocks.stone.logic.AdvancedCokeOvenLogic;
import net.minecraft.core.BlockPos;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class MenuTypes {
    public static final DeferredRegister<MenuType<?>> REGISTER = DeferredRegister.create(ForgeRegistries.MENU_TYPES, "immersivetechnology");

    public static final MultiblockContainer<AdvancedCokeOvenLogic.State, AdvancedCokeOvenMenu> ADVANCED_COKE_OVEN_MENU = registerMultiblock("gui_advanced_coke_oven", AdvancedCokeOvenMenu::makeServer, (type, id, inv, buffer) -> AdvancedCokeOvenMenu.makeClient(type, id, inv));
    public static final MultiblockContainer<BoilerLiquidLogic.State, BoilerLiquidMenu> BOILER_LIQUID_MENU = registerMultiblock("gui_boiler", BoilerLiquidMenu::makeServer, (type, id, inv, buffer) -> BoilerLiquidMenu.makeClient(type, id, inv));
    public static final MultiblockContainer<BoilerSolidLogic.State, BoilerSolidMenu> BOILER_SOLID_MENU = registerMultiblock("gui_solid", BoilerSolidMenu::makeServer, (type, id, inv, buffer) -> BoilerSolidMenu.makeClient(type, id, inv));
    public static final MultiblockContainer<BoilerTankLogic.State, BoilerTankMenu> BOILER_TANK_MENU = registerMultiblock("gui_boiler_tank", BoilerTankMenu::makeServer, (type, id, inv, buffer) -> BoilerTankMenu.makeClient(type, id, inv));
    public static final MultiblockContainer<DistillerLogic.State, DistillerMenu> DISTILLER_MENU = registerMultiblock("gui_distiller", DistillerMenu::makeServer, (type, id, inv, buffer) -> DistillerMenu.makeClient(type, id, inv));

    public static final MultiblockContainer<MeltingCrucibleLogic.State, MeltingCrucibleMenu> MELTING_CRUCIBLE_MENU = registerMultiblock("gui_melting_crucible", MeltingCrucibleMenu::makeServer, (type, id, inv, buffer) -> MeltingCrucibleMenu.makeClient(type, id, inv));

    public static final MultiblockContainer<SolarMelterLogic.State, SolarMenu> SOLAR_MELTER_MENU = registerMultiblock("gui_solar_melter", SolarMenu::makeServer, (type, id, inv, buffer) -> SolarMenu.makeClient(type, id, inv));
    public static final MultiblockContainer<SolarTowerLogic.State, SolarMenu> SOLAR_TOWER_MENU = registerMultiblock("gui_solar_tower", SolarMenu::makeServer, (type, id, inv, buffer) -> SolarMenu.makeClient(type, id, inv));

    public static final ArgContainer<CrateCreativeBlockEntity, CrateCreativeMenu> CRATE_CREATIVE = registerArg("crate_creative", CrateCreativeMenu::makeServer, (type, id, inv, buffer) -> CrateCreativeMenu.makeClient(type, id, inv));
    public static final ArgContainer<TrashItemBlockEntity, TrashItemMenu> TRASH_ITEM = registerArg("trash_item", TrashItemMenu::makeServer, (type, id, inv, buffer) -> TrashItemMenu.makeClient(type, id, inv));

    public static final ArgContainer<ValveFluidBlockEntity, ValveFluidMenu> VALVE_FLUID = registerArg("valve_fluid", ValveFluidMenu::makeServer, ValveFluidMenu::makeClient);
    public static final ArgContainer<ValveLoadBlockEntity, ValveLoadMenu> VALVE_LOAD = registerArg("valve_load", ValveLoadMenu::makeServer, ValveLoadMenu::makeClient);
    public static final ArgContainer<ValveLimiterBlockEntity, ValveLimiterMenu> VALVE_LIMITER = registerArg("valve_limiter", ValveLimiterMenu::makeServer, ValveLimiterMenu::makeClient);

    public static final ArgContainer<ConnectorTimerBlockEntity, ConnectorTimerMenu> CONNECTOR_TIMER = registerArg("connector_timer", ConnectorTimerMenu::makeServer, ConnectorTimerMenu::makeClient);

    public static <T, C extends BaseContainerMenu> ArgContainer<T, C> registerArg(String name, ArgContainer.IArgContainerConstructor<T, C> container, ArgContainer.IClientContainerConstructor<C> client) { return ArgContainer.register(REGISTER, name, container, client); }

    public static <S extends IMultiblockState, C extends BaseContainerMenu> MultiblockContainer<S, C> registerMultiblock(String name, ArgContainer.IArgContainerConstructor<BaseContainerMenu.MultiblockMenuContext<S>, C> container, ArgContainer.IClientContainerConstructor<C> client) { return new MultiblockContainer<>(ArgContainer.registerType(REGISTER, name, client), container); }

    public static class MultiblockContainer<S extends IMultiblockState, C extends BaseContainerMenu> extends ArgContainer<BaseContainerMenu.MultiblockMenuContext<S>, C> {
        private MultiblockContainer(Supplier<MenuType<C>> type, ArgContainer.IArgContainerConstructor<BaseContainerMenu.MultiblockMenuContext<S>, C> factory) { super(type, factory); }

        public MenuProvider provide(IMultiblockContext<S> ctx, BlockPos relativeClicked) { return this.provide(new BaseContainerMenu.MultiblockMenuContext<>(ctx, ctx.getLevel().toAbsolute(relativeClicked))); }
    }
}

package mctmods.immersivetechnology.core.registration;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import mctmods.immersivetechnology.common.blocks.connectors.logic.ConnectorTimerBlockEntity;
import mctmods.immersivetechnology.common.blocks.metal.gui.*;
import mctmods.immersivetechnology.common.blocks.metal.logic.RotorCreativeBlockEntity;
import mctmods.immersivetechnology.common.blocks.metal.logic.TrashItemBlockEntity;
import mctmods.immersivetechnology.common.blocks.metal.logic.ValveFluidBlockEntity;
import mctmods.immersivetechnology.common.blocks.metal.logic.ValveLimiterBlockEntity;
import mctmods.immersivetechnology.common.blocks.metal.logic.ValveLoadBlockEntity;
import mctmods.immersivetechnology.common.blocks.wooden.gui.CrateCreativeMenu;
import mctmods.immersivetechnology.common.blocks.wooden.logic.CrateCreativeBlockEntity;
import mctmods.immersivetechnology.common.blocks.connectors.gui.ConnectorTimerMenu;
import mctmods.immersivetechnology.common.gui.helper.ITContainerMenu;
import mctmods.immersivetechnology.common.multiblocks.gui.*;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.*;
import mctmods.immersivetechnology.common.multiblocks.stone.logic.AdvancedCokeOvenLogic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ITMenuTypes {
    public static final DeferredRegister<MenuType<?>> REGISTER = DeferredRegister.create(Registries.MENU, "immersivetechnology");

    public static final MultiblockContainer<AdvancedCokeOvenLogic.State, AdvancedCokeOvenMenu> ADVANCED_COKE_OVEN_MENU = registerMultiblock("gui_advanced_coke_oven", AdvancedCokeOvenMenu::makeServer, (type, id, inv, buffer) -> AdvancedCokeOvenMenu.makeClient(type, id, inv));
    public static final MultiblockContainer<BoilerLiquidLogic.State, BoilerLiquidMenu> BOILER_LIQUID_MENU = registerMultiblock("gui_boiler", BoilerLiquidMenu::makeServer, (type, id, inv, buffer) -> BoilerLiquidMenu.makeClient(type, id, inv));
    public static final MultiblockContainer<BoilerSolidLogic.State, BoilerSolidMenu> BOILER_SOLID_MENU = registerMultiblock("gui_solid", BoilerSolidMenu::makeServer, (type, id, inv, buffer) -> BoilerSolidMenu.makeClient(type, id, inv));
    public static final MultiblockContainer<BoilerTankLogic.State, BoilerTankMenu> BOILER_TANK_MENU = registerMultiblock("gui_boiler_tank", BoilerTankMenu::makeServer, (type, id, inv, buffer) -> BoilerTankMenu.makeClient(type, id, inv));
    public static final MultiblockContainer<DistillerLogic.State, DistillerMenu> DISTILLER_MENU = registerMultiblock("gui_distiller", DistillerMenu::makeServer, (type, id, inv, buffer) -> DistillerMenu.makeClient(type, id, inv));

    public static final MultiblockContainer<MeltingCrucibleLogic.State, MeltingCrucibleMenu> MELTING_CRUCIBLE_MENU = registerMultiblock("gui_melting_crucible", MeltingCrucibleMenu::makeServer, (type, id, inv, buffer) -> MeltingCrucibleMenu.makeClient(type, id, inv));

    public static final MultiblockContainer<SolarMelterLogic.State, SolarMenu> SOLAR_MELTER_MENU = registerMultiblock("gui_solar_melter", SolarMenu::makeServer, (type, id, inv, buffer) -> SolarMenu.makeClient(type, id, inv));
    public static final MultiblockContainer<SolarTowerLogic.State, SolarMenu> SOLAR_TOWER_MENU = registerMultiblock("gui_solar_tower", SolarMenu::makeServer, (type, id, inv, buffer) -> SolarMenu.makeClient(type, id, inv));

    public static final ArgContainer<RotorCreativeBlockEntity, RotorCreativeMenu> ROTOR_CREATIVE = registerArg("rotor_creative", RotorCreativeMenu::makeServer, RotorCreativeMenu::makeClient);
    public static final ArgContainer<CrateCreativeBlockEntity, CrateCreativeMenu> CRATE_CREATIVE = registerArg("crate_creative", CrateCreativeMenu::makeServer, (type, id, inv, buffer) -> CrateCreativeMenu.makeClient(type, id, inv));
    public static final ArgContainer<TrashItemBlockEntity, TrashItemMenu> TRASH_ITEM = registerArg("trash_item", TrashItemMenu::makeServer, (type, id, inv, buffer) -> TrashItemMenu.makeClient(type, id, inv));

    public static final ArgContainer<ValveFluidBlockEntity, ValveFluidMenu> VALVE_FLUID = registerArg("valve_fluid", ValveFluidMenu::makeServer, ValveFluidMenu::makeClient);
    public static final ArgContainer<ValveLoadBlockEntity, ValveLoadMenu> VALVE_LOAD = registerArg("valve_load", ValveLoadMenu::makeServer, ValveLoadMenu::makeClient);
    public static final ArgContainer<ValveLimiterBlockEntity, ValveLimiterMenu> VALVE_LIMITER = registerArg("valve_limiter", ValveLimiterMenu::makeServer, ValveLimiterMenu::makeClient);

    public static final ArgContainer<ConnectorTimerBlockEntity, ConnectorTimerMenu> CONNECTOR_TIMER = registerArg("connector_timer", ConnectorTimerMenu::makeServer, ConnectorTimerMenu::makeClient);

    public static <T, C extends ITContainerMenu> ArgContainer<T, C> registerArg(String name, IArgContainerConstructor<T, C> container, IClientContainerConstructor<C> client) {
        java.util.function.Supplier<MenuType<C>> typeRef = registerType(name, client);
        return new ArgContainer<>(typeRef, container);
    }

    public static <S extends IMultiblockState, C extends ITContainerMenu> MultiblockContainer<S, C> registerMultiblock(String name, IArgContainerConstructor<ITContainerMenu.MultiblockMenuContext<S>, C> container, IClientContainerConstructor<C> client) {
        java.util.function.Supplier<MenuType<C>> typeRef = registerType(name, client);
        return new MultiblockContainer<>(typeRef, container);
    }

    public static class MultiblockContainer<S extends IMultiblockState, C extends ITContainerMenu> extends ArgContainer<ITContainerMenu.MultiblockMenuContext<S>, C> {
        private MultiblockContainer(java.util.function.Supplier<MenuType<C>> type, IArgContainerConstructor<ITContainerMenu.MultiblockMenuContext<S>, C> factory) { super(type, factory); }

        public MenuProvider provide(IMultiblockContext<S> ctx, BlockPos relativeClicked) { return this.provide(new ITContainerMenu.MultiblockMenuContext<>(ctx, ctx.getLevel().toAbsolute(relativeClicked))); }
    }

    public static class ArgContainer<T, C extends ITContainerMenu> {
        private final java.util.function.Supplier<MenuType<C>> type;
        private final IArgContainerConstructor<T, C> factory;

        private ArgContainer(java.util.function.Supplier<MenuType<C>> type, IArgContainerConstructor<T, C> factory) {
            this.type = type;
            this.factory = factory;
        }

        public C create(int windowId, Inventory playerInv, T tile) { return this.factory.construct(this.getType(), windowId, playerInv, tile); }

        public MenuProvider provide(final T arg) {
            return new MenuProvider() {
                @Nonnull public Component getDisplayName() { return Component.empty(); }

                @Nullable public AbstractContainerMenu createMenu(int containerId, @Nonnull Inventory inventory, @Nonnull Player player) { return ArgContainer.this.create(containerId, inventory, arg); }
            };
        }

        public MenuType<C> getType() { return this.type.get(); }
    }

    private static <C extends ITContainerMenu> java.util.function.Supplier<MenuType<C>> registerType(String name, IClientContainerConstructor<C> client) {
        return REGISTER.register(name, () -> {
            Mutable<MenuType<C>> typeBox = new MutableObject<>();
            MenuType<C> type = net.neoforged.neoforge.common.extensions.IMenuTypeExtension.create((id, inv, buffer) -> client.construct(typeBox.getValue(), id, inv, buffer));
            typeBox.setValue(type);
            return type;
        });
    }

    @FunctionalInterface public interface IArgContainerConstructor<T, C extends ITContainerMenu> { C construct(MenuType<C> type, int windowId, Inventory invPlayer, T arg); }

    @FunctionalInterface public interface IClientContainerConstructor<C extends ITContainerMenu> { C construct(MenuType<C> type, int windowId, Inventory invPlayer, FriendlyByteBuf buffer); }
}

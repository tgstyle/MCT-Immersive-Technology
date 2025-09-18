package mctmods.immersivetechnology.core.registration;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import mctmods.immersivetechnology.common.blocks.multiblocks.gui.*;
import mctmods.immersivetechnology.common.blocks.gui.helper.ITContainerMenu;
import mctmods.immersivetechnology.common.blocks.multiblocks.logic.*;
import mctmods.immersivetechnology.common.blocks.metal.TrashItemBlockEntity;
import mctmods.immersivetechnology.common.blocks.metal.gui.TrashItemMenu;
import mctmods.immersivetechnology.core.lib.ITLib;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ITMenuTypes {
    public static final DeferredRegister<MenuType<?>> REGISTER = DeferredRegister.create(ForgeRegistries.MENU_TYPES, ITLib.MODID);

    public static final MultiblockContainer<AdvancedCokeOvenLogic.State, AdvancedCokeOvenMenu> ADVANCED_COKE_OVEN_MENU = registerMultiblock(ITLib.GUIID_Advanced_Coke_Oven, AdvancedCokeOvenMenu::makeServer, AdvancedCokeOvenMenu::makeClient);
    public static final MultiblockContainer<BoilerLiquidLogic.State, BoilerLiquidMenu> BOILER_LIQUID_MENU = registerMultiblock(ITLib.GUIID_Boiler_Liquid, BoilerLiquidMenu::makeServer, BoilerLiquidMenu::makeClient);
    public static final MultiblockContainer<BoilerSolidLogic.State, BoilerSolidMenu> BOILER_SOLID_MENU = registerMultiblock(ITLib.GUIID_Boiler_Solid, BoilerSolidMenu::makeServer, BoilerSolidMenu::makeClient);
    public static final MultiblockContainer<BoilerTankLogic.State, BoilerTankMenu> BOILER_TANK_MENU = registerMultiblock(ITLib.GUIID_Boiler_Tank, BoilerTankMenu::makeServer, BoilerTankMenu::makeClient);
    public static final MultiblockContainer<DistillerLogic.State, DistillerMenu> DISTILLER_MENU = registerMultiblock(ITLib.GUIID_Distiller, DistillerMenu::makeServer, DistillerMenu::makeClient);
    public static final MultiblockContainer<SolarMelterLogic.State, SolarMenu> SOLAR_MELTER_MENU = registerMultiblock(ITLib.GUIID_SolarMelter, SolarMenu::makeServer, SolarMenu::makeClient);
    public static final MultiblockContainer<SolarTowerLogic.State, SolarMenu> SOLAR_TOWER_MENU = registerMultiblock(ITLib.GUIID_SolarTower, SolarMenu::makeServer, SolarMenu::makeClient);

    public static final ArgContainer<TrashItemBlockEntity, TrashItemMenu> TRASH_ITEM = registerArg("trash_item", TrashItemMenu::makeServer, TrashItemMenu::makeClient);

    public static <T, C extends ITContainerMenu> ArgContainer<T, C> registerArg(String name, ArgContainerConstructor<T, C> container, ClientContainerConstructor<C> client) {
        RegistryObject<MenuType<C>> typeRef = registerType(name, client);
        return new ArgContainer<>(typeRef, container);
    }

    public static <S extends IMultiblockState, C extends ITContainerMenu> MultiblockContainer<S, C> registerMultiblock(String name, ArgContainerConstructor<ITContainerMenu.MultiblockMenuContext<S>, C> container, ClientContainerConstructor<C> client) {
        RegistryObject<MenuType<C>> typeRef = registerType(name, client);
        return new MultiblockContainer<>(typeRef, container);
    }

    public static class MultiblockContainer<S extends IMultiblockState, C extends ITContainerMenu> extends ArgContainer<ITContainerMenu.MultiblockMenuContext<S>, C> {
        private MultiblockContainer(RegistryObject<MenuType<C>> type, ArgContainerConstructor<ITContainerMenu.MultiblockMenuContext<S>, C> factory) { super(type, factory); }
        public MenuProvider provide(IMultiblockContext<S> ctx, BlockPos relativeClicked) { return this.provide(new ITContainerMenu.MultiblockMenuContext<>(ctx, ctx.getLevel().toAbsolute(relativeClicked))); }
    }

    public static class ArgContainer<T, C extends ITContainerMenu> {
        private final RegistryObject<MenuType<C>> type;
        private final ArgContainerConstructor<T, C> factory;

        private ArgContainer(RegistryObject<MenuType<C>> type, ArgContainerConstructor<T, C> factory) {
            this.type = type;
            this.factory = factory;
        }

        public C create(int windowId, Inventory playerInv, T tile) { return this.factory.construct(this.getType(), windowId, playerInv, tile); }

        public MenuProvider provide(final T arg) {
            return new MenuProvider() {
                @Nonnull
                public Component getDisplayName() { return Component.empty(); }

                @Nullable
                public AbstractContainerMenu createMenu(int containerId, @Nonnull Inventory inventory, @Nonnull Player player) { return ArgContainer.this.create(containerId, inventory, arg); }
            };
        }

        public MenuType<C> getType() { return this.type.get(); }
    }

    private static <C extends ITContainerMenu> RegistryObject<MenuType<C>> registerType(String name, ClientContainerConstructor<C> client) {
        return REGISTER.register(
                name, () -> {
                    Mutable<MenuType<C>> typeBox = new MutableObject<>();
                    MenuType<C> type = new MenuType<>((id, inv) -> client.construct(typeBox.getValue(), id, inv), FeatureFlagSet.of());
                    typeBox.setValue(type);
                    return type;
                }
        );
    }

    @FunctionalInterface
    public interface ArgContainerConstructor<T, C extends ITContainerMenu> { C construct(MenuType<C> type, int windowId, Inventory invPlayer, T arg); }

    @FunctionalInterface
    public interface ClientContainerConstructor<C extends ITContainerMenu> { C construct(MenuType<C> type, int windowId, Inventory invPlayer); }
}

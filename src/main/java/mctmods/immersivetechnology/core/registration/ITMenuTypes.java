package mctmods.immersivetechnology.core.registration;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.common.gui.IEBaseContainerOld;
import blusunrize.immersiveengineering.common.gui.IEContainerMenu;
import blusunrize.immersiveengineering.common.register.IEMenuTypes;
import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.common.blocks.multiblocks.gui.AdvancedCokeOvenMenu;
import mctmods.immersivetechnology.common.blocks.multiblocks.gui.BoilerMenu;
import mctmods.immersivetechnology.common.blocks.multiblocks.logic.ITAdvancedCokeOvenLogic;
import mctmods.immersivetechnology.common.blocks.multiblocks.logic.ITBoilerLogic;
import mctmods.immersivetechnology.core.lib.ITLib;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ITMenuTypes
{
    public static final DeferredRegister<MenuType<?>> REGISTER = DeferredRegister.create(ForgeRegistries.MENU_TYPES, ITLib.MODID);
    public static final MultiblockContainer<ITAdvancedCokeOvenLogic.State, AdvancedCokeOvenMenu> ADVANCED_COKE_OVEN_MENU =
            registerMultiblock(ITLib.GUIID_AdvCokeOven, AdvancedCokeOvenMenu::makeServer, AdvancedCokeOvenMenu::makeClient);

    public static final MultiblockContainer<ITBoilerLogic.State, BoilerMenu> BOILER_MENU =
            registerMultiblock(ITLib.GUIID_Boiler, BoilerMenu::makeServer, BoilerMenu::makeClient);

    public static <M extends AbstractContainerMenu>
    RegistryObject<MenuType<M>> registerSimple(String name, IEMenuTypes.SimpleContainerConstructor<M> factory)
    {
        return REGISTER.register(
                name, () -> {
                    Mutable<MenuType<M>> typeBox = new MutableObject<>();
                    MenuType<M> type = new MenuType<>((id, inv) -> factory.construct(typeBox.getValue(), id, inv), FeatureFlagSet.of());
                    typeBox.setValue(type);
                    return type;
                }
        );
    }

    public static <T, C extends IEContainerMenu>
    ITMenuTypes.ArgContainer<T, C> registerArg(
            String name, IEMenuTypes.ArgContainerConstructor<T, C> container, IEMenuTypes.ClientContainerConstructor<C> client
    )
    {
        RegistryObject<MenuType<C>> typeRef = registerType(name, client);
        return new ITMenuTypes.ArgContainer<>(typeRef, container);
    }


    public static <T extends BlockEntity, C extends IEBaseContainerOld<? super T>>
    ITMenuTypes.ArgContainer<T, C> register(String name, IEMenuTypes.ArgContainerConstructor<T, C> container)
    {
        RegistryObject<MenuType<C>> typeRef = REGISTER.register(
                name, () -> {
                    Mutable<MenuType<C>> typeBox = new MutableObject<>();
                    MenuType<C> type = new MenuType<>((IContainerFactory<C>)(windowId, inv, data) -> {
                        Level world = ImmersiveTechnology.proxy.getClientWorld();
                        BlockPos pos = data.readBlockPos();
                        BlockEntity te = world.getBlockEntity(pos);
                        return container.construct(typeBox.getValue(), windowId, inv, (T)te);
                    }, FeatureFlagSet.of());
                    typeBox.setValue(type);
                    return type;
                }
        );
        return new ITMenuTypes.ArgContainer<>(typeRef, container);
    }

    public static <S extends IMultiblockState, C extends IEContainerMenu> ITMenuTypes.MultiblockContainer<S, C> registerMultiblock(String name, IEMenuTypes.ArgContainerConstructor<IEContainerMenu.MultiblockMenuContext<S>, C> container, IEMenuTypes.ClientContainerConstructor<C> client) {
        RegistryObject<MenuType<C>> typeRef = registerType(name, client);
        return new ITMenuTypes.MultiblockContainer<>(typeRef, container);
    }

    public static class MultiblockContainer<S extends IMultiblockState, C extends IEContainerMenu> extends ITMenuTypes.ArgContainer<IEContainerMenu.MultiblockMenuContext<S>, C>
    {
        private MultiblockContainer(RegistryObject<MenuType<C>> type, IEMenuTypes.ArgContainerConstructor<IEContainerMenu.MultiblockMenuContext<S>, C> factory) {
            super(type, factory);
        }

        public MenuProvider provide(IMultiblockContext<S> ctx, BlockPos relativeClicked) {
            return this.provide(new IEContainerMenu.MultiblockMenuContext<>(ctx, ctx.getLevel().toAbsolute(relativeClicked)));
        }
    }

    public static class ArgContainer<T, C extends IEContainerMenu> {
        private final RegistryObject<MenuType<C>> type;
        private final IEMenuTypes.ArgContainerConstructor<T, C> factory;

        private ArgContainer(RegistryObject<MenuType<C>> type, IEMenuTypes.ArgContainerConstructor<T, C> factory) {
            this.type = type;
            this.factory = factory;
        }

        public C create(int windowId, Inventory playerInv, T tile) {
            return this.factory.construct(this.getType(), windowId, playerInv, tile);
        }

        public MenuProvider provide(final T arg) {
            return new MenuProvider() {
                @Nonnull
                public Component getDisplayName() {
                    return Component.empty();
                }

                @Nullable
                public AbstractContainerMenu createMenu(int containerId, @Nonnull Inventory inventory, @Nonnull Player player) {
                    return ITMenuTypes.ArgContainer.this.create(containerId, inventory, arg);
                }
            };
        }

        public MenuType<C> getType() {
            return this.type.get();
        }
    }

    private static <C extends IEContainerMenu> RegistryObject<MenuType<C>> registerType(String name, IEMenuTypes.ClientContainerConstructor<C> client) {
        return REGISTER.register(name, () -> {
            Mutable<MenuType<C>> typeBox = new MutableObject<>();
            MenuType<C> type = new MenuType<>((id, inv) -> {
                return client.construct(typeBox.getValue(), id, inv);
            }, FeatureFlagSet.of());
            typeBox.setValue(type);
            return type;
        });
    }
}

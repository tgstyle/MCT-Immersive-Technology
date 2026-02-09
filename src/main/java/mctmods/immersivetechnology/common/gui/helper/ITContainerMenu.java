package mctmods.immersivetechnology.common.gui.helper;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import com.mojang.datafixers.util.Pair;
import mctmods.immersivetechnology.common.blocks.helper.ITBaseBlockEntity;
import mctmods.immersivetechnology.common.multiblocks.gui.helper.ITSlot;
import mctmods.immersivetechnology.core.network.ITMessageContainerData;
import mctmods.immersivetechnology.core.network.ITPacketHandler;
import mctmods.immersivetechnology.core.lib.ITLib;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@EventBusSubscriber(modid = ITLib.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public abstract class ITContainerMenu extends AbstractContainerMenu {
    protected final List<ITGenericContainerData<?>> genericData = new ArrayList<>();
    protected final List<ServerPlayer> usingPlayers = new ArrayList<>();
    private final Runnable setChanged;
    private final Predicate<Player> isValid;
    public int ownSlotCount;

    protected ITContainerMenu(MenuContext ctx) {
        super(ctx.type, ctx.id);
        this.setChanged = ctx.setChanged;
        this.isValid = ctx.isValid;
    }

    public void addGenericData(ITGenericContainerData<?> newData) { genericData.add(newData); }

    @Override public void broadcastChanges() {
        super.broadcastChanges();
        List<Pair<Integer, ITGenericDataSerializers.DataPair<?>>> toSync = new ArrayList<>();
        for (int i = 0; i < genericData.size(); i++) {
            ITGenericContainerData<?> data = genericData.get(i);
            if (data.needsUpdate()) toSync.add(Pair.of(i, data.dataPair()));
        }
        if (!toSync.isEmpty()) for (ServerPlayer player : usingPlayers) {
            ITPacketHandler.sendToPlayer(player, new ITMessageContainerData(toSync));
        }
    }

    public void receiveSync(List<Pair<Integer, ITGenericDataSerializers.DataPair<?>>> synced) {
        for (Pair<Integer, ITGenericDataSerializers.DataPair<?>> syncElement : synced) {
            genericData.get(syncElement.getFirst()).processSync(syncElement.getSecond().data());
        }
    }

    @Override public void clicked(int id, int dragType, @NotNull ClickType clickType, @NotNull Player player) {
        Slot slot = id < 0 ? null : this.slots.get(id);
        if (!(slot instanceof ITSlot.ItemHandlerGhost)) { super.clicked(id, dragType, clickType, player); return; }
        ItemStack stackSlot = slot.getItem();
        if (dragType == 2) { slot.set(ItemStack.EMPTY); }
        else if (dragType == 0 || dragType == 1) {
            ItemStack stackHeld = getCarried();
            int amount = Math.min(slot.getMaxStackSize(), stackHeld.getCount());
            if (dragType == 1) amount = 1;
            if (stackSlot.isEmpty()) {
                if (!stackHeld.isEmpty() && slot.mayPlace(stackHeld)) {
                    slot.set(ItemHandlerHelper.copyStackWithSize(stackHeld, amount));
                }
            } else if (stackHeld.isEmpty()) { slot.set(ItemStack.EMPTY); }
            else if (slot.mayPlace(stackHeld)) {
                if (ItemStack.isSameItem(stackSlot, stackHeld)) {
                    stackSlot.grow(amount);
                    slot.set(stackSlot);
                } else slot.set(ItemHandlerHelper.copyStackWithSize(stackHeld, amount));
            }
            if (stackSlot.getCount() > slot.getMaxStackSize()) stackSlot.setCount(slot.getMaxStackSize());
        } else if (dragType == 5) {
            ItemStack stackHeld = getCarried();
            int amount = Math.min(slot.getMaxStackSize(), stackHeld.getCount());
            if (!slot.hasItem()) slot.set(ItemHandlerHelper.copyStackWithSize(stackHeld, amount));
        }
    }

    @Override @Nonnull public ItemStack quickMoveStack(@NotNull Player player, int slot) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slotObject = this.slots.get(slot);
        if (slotObject.hasItem()) {
            ItemStack itemStack1 = slotObject.getItem();
            itemStack = itemStack1.copy();
            if (slot < ownSlotCount) {
                if (!this.moveItemStackTo(itemStack1, ownSlotCount, this.slots.size(), true)) return ItemStack.EMPTY;
            } else if (!this.moveItemStackToWithMayPlace(itemStack1, ownSlotCount)) return ItemStack.EMPTY;
            if (itemStack1.isEmpty()) slotObject.set(ItemStack.EMPTY); else slotObject.setChanged();
        }
        return itemStack;
    }

    protected boolean moveItemStackToWithMayPlace(ItemStack pStack, int pEndIndex) {
        return moveItemStackToWithMayPlace(slots, this::moveItemStackTo, pStack, 0, pEndIndex);
    }

    public static boolean moveItemStackToWithMayPlace(List<Slot> slots, MoveItemsFunc move, ItemStack pStack, int pStartIndex, int pEndIndex) {
        boolean inAllowedRange = true;
        int allowedStart = pStartIndex;
        for (int i = pStartIndex; i < pEndIndex; i++) {
            boolean mayPlace = slots.get(i).mayPlace(pStack);
            if (inAllowedRange && !mayPlace) {
                if (move.moveItemStackTo(pStack, allowedStart, i, false)) return true;
                inAllowedRange = false;
            } else if (!inAllowedRange && mayPlace) {
                allowedStart = i;
                inAllowedRange = true;
            }
        }
        return inAllowedRange && move.moveItemStackTo(pStack, allowedStart, pEndIndex, false);
    }

    @SuppressWarnings("unused")
    public void receiveMessageFromScreen(CompoundTag nbt) {}

    @Override public void removed(@Nonnull Player player) { super.removed(player); setChanged.run(); }

    @Override public boolean stillValid(@Nonnull Player pPlayer) { return isValid.test(pPlayer); }

    @SubscribeEvent public static void onContainerOpened(PlayerContainerEvent.Open ev) {
        if (ev.getContainer() instanceof ITContainerMenu itContainer && ev.getEntity() instanceof ServerPlayer serverPlayer) {
            itContainer.usingPlayers.add(serverPlayer);
            List<Pair<Integer, ITGenericDataSerializers.DataPair<?>>> list = new ArrayList<>();
            for (int i = 0; i < itContainer.genericData.size(); i++) {
                list.add(Pair.of(i, itContainer.genericData.get(i).dataPair()));
            }
            ITPacketHandler.sendToPlayer(serverPlayer, new ITMessageContainerData(list));
        }
    }

    @SubscribeEvent public static void onContainerClosed(PlayerContainerEvent.Close ev) {
        if (ev.getContainer() instanceof ITContainerMenu itContainer && ev.getEntity() instanceof ServerPlayer serverPlayer) {
            itContainer.usingPlayers.remove(serverPlayer);
        }
    }

    public static MenuContext multiblockCtx(MenuType<?> pMenuType, int pContainerId, MultiblockMenuContext<?> ctx) {
        return new MenuContext(pMenuType, pContainerId, ctx.mbContext()::markMasterDirty, p -> {
            if (!ctx.mbContext().isValid().getAsBoolean()) return false;
            return p.distanceToSqr(Vec3.atCenterOf(ctx.clickedPos)) <= 64.0D;
        });
    }

    public static MenuContext blockCtx(MenuType<?> pMenuType, int pContainerId, BlockEntity be) {
        return new MenuContext(pMenuType, pContainerId, () -> {
            be.setChanged();
            if (be instanceof ITBaseBlockEntity itBE) itBE.markContainingBlockForUpdate(null);
        }, p -> {
            BlockPos pos = be.getBlockPos();
            Level level = be.getLevel();
            if (level == null || level.getBlockEntity(pos) != be) return false;
            return !(p.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) > 64.0D);
        });
    }

    public static MenuContext clientCtx(MenuType<?> pMenuType, int pContainerId) {
        return new MenuContext(pMenuType, pContainerId, () -> {}, $ -> true);
    }

    public record MenuContext(MenuType<?> type, int id, Runnable setChanged, Predicate<Player> isValid) {}

    public record MultiblockMenuContext<S extends IMultiblockState>(IMultiblockContext<S> mbContext, BlockPos clickedPos) {}

    public interface MoveItemsFunc {
        boolean moveItemStackTo(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection);
    }
}

package mctmods.immersivetechnology.common.blocks.multiblocks.gui.helper;

import blusunrize.immersiveengineering.common.util.inventory.EmptyContainer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Predicate;

public abstract class ITSlot extends Slot
{
    final AbstractContainerMenu containerMenu;

    public ITSlot(AbstractContainerMenu containerMenu, Container inv, int id, int x, int y)
    {
        super(inv, id, x, y);
        this.containerMenu = containerMenu;
    }

    @Override
    public boolean mayPlace(ItemStack itemStack)
    {
        return true;
    }

    public static class Output extends ITSlot
    {
        public Output(AbstractContainerMenu container, Container inv, int id, int x, int y)
        {
            super(container, inv, id, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack itemStack)
        {
            return false;
        }
    }

    public static class NewOutput extends ITSlot.SlotItemHandlerIT
    {
        public NewOutput(IItemHandler inv, int id, int x, int y)
        {
            super(inv, id, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack itemStack)
        {
            return false;
        }
    }

    public static class ITFurnaceSFuelSlot extends ITSlot.SlotItemHandlerIT
    {
        public ITFurnaceSFuelSlot(IItemHandler inv, int id, int x, int y)
        {
            super(inv, id, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack)
        {
            return AbstractFurnaceBlockEntity.isFuel(stack)||isBucket(stack);
        }

        @Override
        public int getMaxStackSize(@NotNull ItemStack stack)
        {
            return isBucket(stack)?1: super.getMaxStackSize(stack);
        }

        public static boolean isBucket(ItemStack stack)
        {
            return stack.getItem()== Items.BUCKET;
        }
    }

    public static class NewFluidContainer extends ITSlot.SlotItemHandlerIT
    {
        private final ITSlot.NewFluidContainer.Filter filter;

        public NewFluidContainer(IItemHandler inv, int id, int x, int y, ITSlot.NewFluidContainer.Filter filter)
        {
            super(inv, id, x, y);
            this.filter = filter;
        }

        @Override
        public boolean mayPlace(ItemStack itemStack)
        {
            LazyOptional<IFluidHandlerItem> handlerCap = FluidUtil.getFluidHandler(itemStack);
            return handlerCap.map(handler -> {
                if(handler.getTanks() <= 0)
                    return false;
                return switch(filter)
                {
                    case ANY -> true;
                    case EMPTY -> handler.getFluidInTank(0).isEmpty();
                    case FULL -> !handler.getFluidInTank(0).isEmpty();
                };
            }).orElse(false);
        }

        public enum Filter
        {
            ANY, EMPTY, FULL;
        }
    }

    public static class FluidContainer extends ITSlot
    {
        int filter; //0 = any, 1 = empty, 2 = full

        public FluidContainer(AbstractContainerMenu container, Container inv, int id, int x, int y, int filter)
        {
            super(container, inv, id, x, y);
            this.filter = filter;
        }

        @Override
        public boolean mayPlace(ItemStack itemStack)
        {
            LazyOptional<IFluidHandlerItem> handlerCap = FluidUtil.getFluidHandler(itemStack);
            return handlerCap.map(handler -> {
                if(handler.getTanks() <= 0)
                    return false;

                if(filter==1)
                    return handler.getFluidInTank(0).isEmpty();
                else if(filter==2)
                    return !handler.getFluidInTank(0).isEmpty();
                return true;
            }).orElse(false);
        }
    }

    public static class WithPredicate extends ITSlot.SlotItemHandlerIT
    {
        final Predicate<ItemStack> predicate;
        final Consumer<ItemStack> onChange;

        public WithPredicate(IItemHandler inv, int id, int x, int y, Predicate<ItemStack> predicate)
        {
            this(inv, id, x, y, predicate, s -> {
            });
        }

        public WithPredicate(IItemHandler inv, int id, int x, int y, Predicate<ItemStack> predicate, Consumer<ItemStack> onChange)
        {
            super(inv, id, x, y);
            this.predicate = predicate;
            this.onChange = onChange;
        }

        @Override
        public boolean mayPlace(ItemStack itemStack)
        {
            return !itemStack.isEmpty()&&this.predicate.test(itemStack);
        }

        @Override
        public int getMaxStackSize()
        {
            return 1;
        }

        @Override
        public void setChanged()
        {
            super.setChanged();
            onChange.accept(getItem());
        }
    }

    public static class ItemHandlerGhost extends ITSlot.SlotItemHandlerIT
    {

        public ItemHandlerGhost(IItemHandler itemHandler, int index, int xPosition, int yPosition)
        {
            super(itemHandler, index, xPosition, yPosition);
        }

        @Override
        public boolean mayPickup(Player playerIn)
        {
            return false;
        }
    }

    public static class ItemDisplay extends ITSlot
    {
        public ItemDisplay(AbstractContainerMenu container, Container inv, int id, int x, int y)
        {
            super(container, inv, id, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack itemStack)
        {
            return false;
        }

        @Override
        public boolean mayPickup(Player player)
        {
            return false;
        }
    }

    public static class Tagged extends ITSlot.SlotItemHandlerIT
    {
        private final TagKey<Item> tag;

        public Tagged(IItemHandler inv, int id, int x, int y, TagKey<Item> tag)
        {
            super(inv, id, x, y);
            this.tag = tag;
        }

        @Override
        public boolean mayPlace(ItemStack itemStack)
        {
            return itemStack.is(tag);
        }
    }

    // Only used to "fill up slot IDs" to keep the IDs of later slots stable when adding/removing "real" slots
    public static class AlwaysEmptySlot extends ITSlot
    {
        public AlwaysEmptySlot(AbstractContainerMenu containerMenu)
        {
            super(containerMenu, EmptyContainer.INSTANCE, 0, 0, 0);
        }

        @Override
        public boolean mayPlace(ItemStack itemStack)
        {
            return false;
        }

        @Override
        public boolean isActive()
        {
            return false;
        }
    }

    public static class ContainerCallback extends ITSlot.SlotItemHandlerIT
    {
        ITSlot.ICallbackContainer container;

        public ContainerCallback(ITSlot.ICallbackContainer container, IItemHandler inv, int id, int x, int y)
        {
            super(inv, id, x, y);
            this.container = container;
        }

        @Override
        public boolean mayPlace(ItemStack itemStack)
        {
            return this.container.canInsert(itemStack, getSlotIndex(), this);
        }

        @Override
        public boolean mayPickup(Player player)
        {
            return this.container.canTake(this.getItem(), getSlotIndex(), this);
        }
    }

    private static class SlotItemHandlerIT extends SlotItemHandler
    {
        public SlotItemHandlerIT(IItemHandler itemHandler, int index, int xPosition, int yPosition)
        {
            super(itemHandler, index, xPosition, yPosition);
        }

        @Override
        public int getMaxStackSize(@NotNull ItemStack stack)
        {
            return Math.min(Math.min(this.getMaxStackSize(), stack.getMaxStackSize()), super.getMaxStackSize(stack));
        }
    }

    public interface ICallbackContainer
    {
        boolean canInsert(ItemStack stack, int slotNumer, Slot slotObject);

        boolean canTake(ItemStack stack, int slotNumer, Slot slotObject);
    }
}

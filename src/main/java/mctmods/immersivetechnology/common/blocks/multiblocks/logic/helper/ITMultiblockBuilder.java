package mctmods.immersivetechnology.common.blocks.multiblocks.logic.helper;

import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistrationBuilder;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.ComparatorManager;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.IMultiblockComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.RedstoneControl;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import com.google.common.base.Preconditions;
import mctmods.immersivetechnology.common.blocks.multiblocks.gui.helper.ITMultiblockGui;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.registration.ITMenuTypes;
import net.minecraft.core.BlockPos;

public class ITMultiblockBuilder<S extends IMultiblockState>
        extends MultiblockRegistrationBuilder<S, ITMultiblockBuilder<S>>
{
    public ITMultiblockBuilder(IMultiblockLogic<S> logic, String name)
    {
        super(logic, ITLib.rl(name));
    }

    public ITMultiblockBuilder<S> gui(ITMenuTypes.MultiblockContainer<S, ?> menu)
    {
        return component(new ITMultiblockGui<>(menu));
    }

    public ITMultiblockBuilder<S> redstoneNoComputer(IMultiblockComponent.StateWrapper<S, RedstoneControl.RSState> getState, BlockPos... positions)
    {
        redstoneAware();
        return selfWrappingComponent(new RedstoneControl<>(getState, false, positions));
    }

    public ITMultiblockBuilder<S> redstone(IMultiblockComponent.StateWrapper<S, RedstoneControl.RSState> getState, BlockPos... positions)
    {
        redstoneAware();
        return selfWrappingComponent(new RedstoneControl<>(getState, positions));
    }

    public ITMultiblockBuilder<S> comparator(ComparatorManager<S> comparator)
    {
        withComparator();
        return super.selfWrappingComponent(comparator);
    }

    @Override
    public <CS, C extends IMultiblockComponent<CS> & IMultiblockComponent.StateWrapper<S, CS>>
    ITMultiblockBuilder<S> selfWrappingComponent(C extraComponent)
    {
        Preconditions.checkArgument(!(extraComponent instanceof ComparatorManager<?>));
        return super.selfWrappingComponent(extraComponent);
    }

    @Override
    protected ITMultiblockBuilder<S> self()
    {
        return this;
    }
}

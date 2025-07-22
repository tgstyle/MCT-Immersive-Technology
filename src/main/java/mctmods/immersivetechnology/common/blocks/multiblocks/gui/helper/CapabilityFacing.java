package mctmods.immersivetechnology.common.blocks.multiblocks.gui.helper;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.ICommonMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.CapabilityPosition;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MultiblockFace;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.RelativeBlockFace;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import net.minecraft.core.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
/**
 * CapabilityFacing
 * A convienice class for capablity and position handling
 * Credit: TeamMoeg
 * */
public record CapabilityFacing(MultiblockFace pos,CapabilityPosition capPos) {

    public CapabilityFacing(MultiblockFace pos) {
        this(pos,CapabilityPosition.opposing(pos));
    }
    public CapabilityFacing(CapabilityPosition pos) {
        this(new MultiblockFace(pos.side().getOpposite(),pos.side().offsetRelative(pos.posInMultiblock(), 1)),pos);
    }
    public CapabilityFacing(int x,int y,int z,RelativeBlockFace side) {
        this(new CapabilityPosition(x,y,z,side));
    }
    public <T> CapabilityReference<T> getFacingCapability(ICommonMultiblockContext ctx,Capability<T> cap) {
        return ctx.getCapabilityAt(cap, pos);
    }
    public boolean isCapabilityPosition(CapabilityPosition pos) {
        return capPos.equalsOrNullFace(pos);
    }
    public boolean isPosEquals(BlockPos pos) {
        return capPos.posInMultiblock().equals(pos);
    }
}

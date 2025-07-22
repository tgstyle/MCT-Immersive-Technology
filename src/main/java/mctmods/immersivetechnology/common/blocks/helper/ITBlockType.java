package mctmods.immersivetechnology.common.blocks.helper;

import net.minecraft.world.level.block.Block;

public interface ITBlockType
{
    Block getBlock();

    int getColor(int index);
}

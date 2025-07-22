package mctmods.immersivetechnology.common.blocks.multiblocks;

import blusunrize.immersiveengineering.api.multiblocks.ClientMultiblocks;
import mctmods.immersivetechnology.common.blocks.multiblocks.helper.ITClientMultiblockProperties;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.registration.ITMultiblockProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class ITSteamTurbine extends ITTemplateMultiblock {
    public static final ITSteamTurbine INSTANCE = new ITSteamTurbine();

    public ITSteamTurbine() { super(new ResourceLocation(ITLib.MODID, "multiblocks/steam_turbine"), new BlockPos(1, 1, 0), new BlockPos(1, 1, 9), new BlockPos(3, 4, 10), ITMultiblockProvider.STEAM_TURBINE); }

    @Override
    public void disassemble(Level world, BlockPos origin, boolean mirrored, Direction clickDirectionAtCreation) {
        super.disassemble(world, origin, mirrored, clickDirectionAtCreation);
    }

    @Override
    public float getManualScale() {
        return 10;
    }

    @Override
    public void initializeClient(Consumer<ClientMultiblocks.MultiblockManualData> consumer) {
        consumer.accept(new ITClientMultiblockProperties(this, 1, 1, 0));
    }
}

package mctmods.immersivetechnology.common.multiblocks.metal;

import blusunrize.immersiveengineering.api.multiblocks.ClientMultiblocks;
import mctmods.immersivetechnology.common.multiblocks.helper.ITClientMultiblockProperties;
import mctmods.immersivetechnology.common.multiblocks.helper.ITTemplateMultiblock;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.SteamTurbineShape;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.registration.ITMultiblockProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class SteamTurbine extends ITTemplateMultiblock {
    public static final SteamTurbine INSTANCE = new SteamTurbine();

    public SteamTurbine() { super(ITLib.rl("multiblocks/steam_turbine"), SteamTurbineShape.MASTER_POS, SteamTurbineShape.TRIGGER_POS, new BlockPos(SteamTurbineShape.WIDTH,SteamTurbineShape.HEIGHT,SteamTurbineShape.LENGTH), ITMultiblockProvider.STEAM_TURBINE); }

    @Override
    public void disassemble(Level world, BlockPos origin, boolean mirrored, Direction clickDirectionAtCreation) { super.disassemble(world, origin, mirrored, clickDirectionAtCreation); }

    @Override
    public float getManualScale() { return SteamTurbineShape.MANUAL_SCALE; }

    @Override
    public void initializeClient(Consumer<ClientMultiblocks.MultiblockManualData> consumer) { consumer.accept(new ITClientMultiblockProperties(this, SteamTurbineShape.CLIENT_OFFSET.getX(), SteamTurbineShape.CLIENT_OFFSET.getY(), SteamTurbineShape.CLIENT_OFFSET.getZ())); }
}

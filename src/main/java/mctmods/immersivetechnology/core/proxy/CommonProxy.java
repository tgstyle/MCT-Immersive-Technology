package mctmods.immersivetechnology.core.proxy;

import mctmods.immersivetechnology.core.integration.computer.ComputerIntegration;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.registration.BlockEntities;
import mctmods.immersivetechnology.core.registration.ModBlocks;
import mctmods.immersivetechnology.core.registration.CreativeTab;
import mctmods.immersivetechnology.core.registration.ModFluids;
import mctmods.immersivetechnology.core.registration.ModItems;
import mctmods.immersivetechnology.core.registration.MenuTypes;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;
import mctmods.immersivetechnology.core.registration.Particles;
import mctmods.immersivetechnology.core.registration.RecipeTypes;
import mctmods.immersivetechnology.core.registration.Sounds;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.FluidStack;

@SuppressWarnings("unused") public class CommonProxy {
    public static void modConstruction(IEventBus event) {
        Reference.IT_LOGGER.info("Registering IT Content!");
        MultiblockRegistry.forceClassLoad();
        ComputerIntegration.register();
        MenuTypes.init(event);
        RecipeTypes.init(event);
        Sounds.init(event);
        Particles.REGISTER.register(event);
        BlockEntities.init(event);
        ModBlocks.init(event);
        ModItems.init(event);
        ModFluids.REGISTER.register(event);
        ModFluids.TYPE_REGISTER.register(event);
        CreativeTab.REGISTER.register(event);
    }

    public void reinitializeGUI() {}

    public Level getClientWorld() { return null; }

    public Player getClientPlayer() { return null; }

    public boolean isShiftDown() { return false; }

    public void addChatMessage(Component message) {}

    public boolean particlesVisible(Vec3 pos) { return false; }

    public float attenuated(Vec3 pos, double falloff, float volume) { return 0f; }

    public float linearFalloff(Vec3 pos, double range) { return 0f; }

    public void exhaust(Level level, BlockPos pos, Direction facing, float normSpeed, FluidStack fluid) {}

    public void exhaust(Level level, Vec3 smokePos, Direction facing, float normSpeed, FluidStack fluid, double speedScale, float sizeScale) {}

    public void boilerExhaust(Level level, BlockPos exhaust, boolean smoking) {}
}

package mctmods.immersivetechnology.core.util;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import blusunrize.immersiveengineering.api.IEApiDataComponents;
import java.util.function.Supplier;

public class Utils {
    public static void dropStackAtPos(Level world, BlockPos pos, ItemStack stack) { Containers.dropItemStack(world, pos.getX(), pos.getY(), pos.getZ(), stack); }

    public static FluidStack copyFluidStackWithAmount(FluidStack stack, int amount, boolean stripPressure) {
        if (stack == null || stack.isEmpty()) { return FluidStack.EMPTY; }
        FluidStack fs = stack.copyWithAmount(amount);
        if (stripPressure) { fs.remove(IEApiDataComponents.FLUID_PRESSURIZED); }
        return fs;
    }

    public static boolean isFluidRelatedItemStack(ItemStack stack) {
        if (stack.isEmpty()) { return false; }
        return stack.getCapability(Capabilities.FluidHandler.ITEM) != null;
    }

    public static ItemStack insertStackIntoInventory(Supplier<IItemHandler> ref, ItemStack stack, boolean simulate) { return insertStackIntoInventory(ref.get(), stack, simulate); }

    public static ItemStack insertStackIntoInventory(IItemHandler handler, ItemStack stack, boolean simulate) {
        if (handler == null || stack.isEmpty()) { return stack; }
        return ItemHandlerHelper.insertItem(handler, stack.copy(), simulate);
    }

    public static void applyLadderLogic(Entity entity) {
        if (entity instanceof LivingEntity living && !living.onClimbable()) {
            Vec3 motion = entity.getDeltaMovement();
            float maxMotion = 0.15F;
            motion = new Vec3(Mth.clamp(motion.x, -maxMotion, maxMotion), Math.max(motion.y, -maxMotion), Mth.clamp(motion.z, -maxMotion, maxMotion));
            entity.fallDistance = 0.0F;
            if (motion.y < 0 && entity instanceof Player && entity.isShiftKeyDown()) { motion = new Vec3(motion.x, 0, motion.z); }
            else if (entity.horizontalCollision) { motion = new Vec3(motion.x, 0.2, motion.z); }
            entity.setDeltaMovement(motion);
        }
    }
}

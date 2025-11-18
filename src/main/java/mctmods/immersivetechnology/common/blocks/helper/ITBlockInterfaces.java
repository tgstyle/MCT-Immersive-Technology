package mctmods.immersivetechnology.common.blocks.helper;

import com.google.common.base.Preconditions;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.registration.ITMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;

public class ITBlockInterfaces {
    public ITBlockInterfaces() { }

    public interface IInteractionObjectIT<T extends BlockEntity & ITBlockInterfaces.IInteractionObjectIT<T>> extends MenuProvider {
        @Nullable
        T getGuiMaster();

        ITMenuTypes.ArgContainer<? super T, ?> getContainerType();

        boolean canUseGui(Player var1);

        default boolean isValid() { return getGuiMaster() != null; }

        @Nonnull
        default AbstractContainerMenu createMenu(int id, @NotNull Inventory playerInventory, @NotNull Player playerEntity) {
            T master = getGuiMaster();
            Preconditions.checkNotNull(master);
            ITMenuTypes.ArgContainer<? super T, ?> type = getContainerType();
            return type.create(id, playerInventory, master);
        }

        @Nonnull
        default Component getDisplayName() { return Component.literal(""); }
    }

    public interface IGeneralMultiblock extends ITBlockInterfaces.BlockStateProvider {
        @Nullable
        IGeneralMultiblock master();
    }

    public interface IHasDummyBlocks extends IGeneralMultiblock {
        void placeDummies(BlockPlaceContext var1, BlockState var2);
        void breakDummies(BlockPos var1, BlockState var2);
    }

    public interface ICollisionBounds {
        @Nonnull
        VoxelShape getCollisionShape(CollisionContext var1);
    }

    public interface ISelectionBounds {
        @Nonnull
        VoxelShape getSelectionShape(@Nullable CollisionContext var1);
    }

    public interface IBlockBounds extends ISelectionBounds, ICollisionBounds {
        @Nonnull
        VoxelShape getBlockBounds(@Nullable CollisionContext var1);

        @Nonnull
        default VoxelShape getCollisionShape(CollisionContext ctx) { return getBlockBounds(ctx); }

        @Nonnull
        default VoxelShape getSelectionShape(@Nullable CollisionContext ctx) { return getBlockBounds(ctx); }
    }

    public interface IMirrorAble extends BlockStateProvider {
        default boolean getIsMirrored() {
            BlockState state = getState();
            if (state.hasProperty(ITProperties.MIRRORED)) { return state.getValue(ITProperties.MIRRORED); }
            else { return false; }
        }

        default void setMirrored(boolean mirrored) {
            BlockState oldState = getState();
            BlockState newState = oldState.setValue(ITProperties.MIRRORED, mirrored);
            setState(newState);
        }
    }

    public interface IPlacementInteraction {
        void onBEPlaced(BlockPlaceContext var1);
    }

    public interface IScrewdriverInteraction {
        InteractionResult screwdriverUseSide(Direction var1, Player var2, InteractionHand var3, Vec3 var4);
    }

    public interface IHammerInteraction {
        boolean hammerUseSide(Direction var1, Player var2, InteractionHand var3, Vec3 var4);
    }

    public interface IPlayerInteraction {
        boolean interact(Direction var1, Player var2, InteractionHand var3, ItemStack var4, float var5, float var6, float var7);
    }

    public interface IEntityProof {
        boolean canEntityDestroy(Entity var1);
    }

    public interface BlockStateProvider {
        BlockState getState();

        void setState(BlockState var1);
    }

    public interface IAdditionalDrops {
        Collection<ItemStack> getExtraDrops(Player var1, BlockState var2);
    }

    public interface IBlockEntityDrop extends IPlacementInteraction {
        void getBlockEntityDrop(LootContext var1, Consumer<ItemStack> var2);

        default ItemStack getPickBlock(BlockState state) {
            BlockEntity tile = (BlockEntity) this;
            MutableObject<ItemStack> drop = new MutableObject<>(new ItemStack(state.getBlock()));
            Level var7 = tile.getLevel();
            if (var7 instanceof ServerLevel world) {
                LootParams parms = (new LootParams.Builder(world))
                        .withOptionalParameter(LootContextParams.TOOL, ItemStack.EMPTY)
                        .withOptionalParameter(LootContextParams.BLOCK_STATE, world.getBlockState(tile.getBlockPos()))
                        .withOptionalParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(tile.getBlockPos()))
                        .create(LootContextParamSets.BLOCK);
                LootContext var10001 = (new LootContext.Builder(parms)).create(ResourceLocation.fromNamespaceAndPath(ITLib.MODID, "pick_block"));
                Objects.requireNonNull(drop);
                getBlockEntityDrop(var10001, drop::setValue);
            }
            return drop.getValue();
        }
    }

    public interface IConfigurableSides {
        ITEnums.IOSideConfig getSideConfig(Direction var1);
        boolean toggleSide(Direction var1, Player var2);
    }

    public interface IAdvancedDirectionalBE extends IDirectionalBE {
        void onDirectionalPlacement(Direction var1, float var2, float var3, float var4, LivingEntity var5);
    }

    public interface IDirectionalBE {
        Direction getFacing();
        void setFacing(Direction var1);
        ITPlacementLimitation getFacingLimitation();

        default Direction getFacingForPlacement(BlockPlaceContext ctx) {
            Direction f = getFacingLimitation().getDirectionForPlacement(ctx);
            if (mirrorFacingOnPlacement(ctx.getPlayer())) { return f.getOpposite(); }
            else { return f; }
        }

        default boolean mirrorFacingOnPlacement(LivingEntity placer) { return false; }

        default boolean canHammerRotate(Direction side, Vec3 hit, LivingEntity entity) { return true; }

        default void afterRotation() { }
    }

    public interface IRedstoneOutput {
        default int getWeakRSOutput(Direction side) { return getStrongRSOutput(side); }
        int getStrongRSOutput(Direction var1);
        boolean canConnectRedstone(Direction var1);
    }

    public interface IComparatorOverride {
        int getComparatorInputOverride();
    }

    public interface IBlockOverlayText {
        @Nullable
        Component[] getOverlayText(Player player, HitResult mop, boolean hammer);
    }
}

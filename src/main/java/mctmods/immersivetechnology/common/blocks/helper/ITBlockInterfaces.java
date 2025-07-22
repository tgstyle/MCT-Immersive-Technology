package mctmods.immersivetechnology.common.blocks.helper;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.IEEnums;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.PlacementLimitation;
import com.google.common.base.Preconditions;
import mctmods.immersivetechnology.core.registration.ITMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;

public class ITBlockInterfaces
{
    public ITBlockInterfaces() {
    }

    public interface IProcessBE {
        int[] getCurrentProcessesStep();

        int[] getCurrentProcessesMax();
    }

    public interface IInteractionObjectIE<T extends BlockEntity & ITBlockInterfaces.IInteractionObjectIE<T>> extends MenuProvider {
        @Nullable
        T getGuiMaster();

        ITMenuTypes.ArgContainer<? super T, ?> getContainerType();

        boolean canUseGui(Player var1);

        default boolean isValid() {
            return this.getGuiMaster() != null;
        }

        @Nonnull
        default AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player playerEntity) {
            T master = this.getGuiMaster();
            Preconditions.checkNotNull(master);
            ITMenuTypes.ArgContainer<? super T, ?> type = this.getContainerType();
            return type.create(id, playerInventory, master);
        }

        default Component getDisplayName() {
            return Component.literal("");
        }
    }

    public interface IGeneralMultiblock extends ITBlockInterfaces.BlockstateProvider {
        @Nullable
        ITBlockInterfaces.IGeneralMultiblock master();

        default boolean isDummy()
        {
            BlockState state = getState();
            if(state.hasProperty(IEProperties.MULTIBLOCKSLAVE))
                return state.getValue(IEProperties.MULTIBLOCKSLAVE);
            else
                return true;
        }
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

    public interface IBlockBounds extends ITBlockInterfaces.ISelectionBounds, ITBlockInterfaces.ICollisionBounds {
        @Nonnull
        VoxelShape getBlockBounds(@Nullable CollisionContext var1);

        @Nonnull
        default VoxelShape getCollisionShape(CollisionContext ctx) {
            return this.getBlockBounds(ctx);
        }

        @Nonnull
        default VoxelShape getSelectionShape(@Nullable CollisionContext ctx) {
            return this.getBlockBounds(ctx);
        }
    }

    public interface IMirrorAble extends ITBlockInterfaces.BlockstateProvider {
        default boolean getIsMirrored() {
            BlockState state = this.getState();
            return state.hasProperty(IEProperties.MIRRORED) ? (Boolean)state.getValue(IEProperties.MIRRORED) : false;
        }

        default void setMirrored(boolean mirrored) {
            BlockState state = this.getState();
            BlockState newState = (BlockState)state.setValue(IEProperties.MIRRORED, mirrored);
            this.setState(newState);
        }
    }

    public interface IActiveState extends ITBlockInterfaces.BlockstateProvider {
        default boolean getIsActive() {
            BlockState state = this.getState();
            return state.hasProperty(IEProperties.ACTIVE) ? (Boolean)state.getValue(IEProperties.ACTIVE) : false;
        }

        default void setActive(boolean active) {
            BlockState state = this.getState();
            BlockState newState = (BlockState)state.setValue(IEProperties.ACTIVE, active);
            this.setState(newState);
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

    public interface IAdditionalDrops {
        Collection<ItemStack> getExtraDrops(Player var1, BlockState var2);
    }

    public interface IBlockEntityDrop extends ITBlockInterfaces.IPlacementInteraction {
        void getBlockEntityDrop(LootContext var1, Consumer<ItemStack> var2);

        default ItemStack getPickBlock(@Nullable Player player, BlockState state, HitResult rayRes) {
            BlockEntity tile = (BlockEntity)this;
            Mutable<ItemStack> drop = new MutableObject(new ItemStack(state.getBlock()));
            Level var7 = tile.getLevel();
            if (var7 instanceof ServerLevel world) {
                LootParams parms = (new LootParams.Builder(world)).withOptionalParameter(LootContextParams.TOOL, ItemStack.EMPTY).withOptionalParameter(LootContextParams.BLOCK_STATE, world.getBlockState(tile.getBlockPos())).withOptionalParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(tile.getBlockPos())).create(LootContextParamSets.BLOCK);
                LootContext var10001 = (new LootContext.Builder(parms)).create(IEApi.ieLoc("pick_block"));
                Objects.requireNonNull(drop);
                this.getBlockEntityDrop(var10001, drop::setValue);
            }

            return (ItemStack)drop.getValue();
        }
    }

    public interface IConfigurableSides {
        IEEnums.IOSideConfig getSideConfig(Direction var1);

        boolean toggleSide(Direction var1, Player var2);
    }

    public interface IAdvancedDirectionalBE extends ITBlockInterfaces.IDirectionalBE {
        void onDirectionalPlacement(Direction var1, float var2, float var3, float var4, LivingEntity var5);
    }

    public interface IStateBasedDirectional extends ITBlockInterfaces.IDirectionalBE, ITBlockInterfaces.BlockstateProvider {
        Property<Direction> getFacingProperty();

        default Direction getFacing() {
            BlockState state = this.getState();
            return state.hasProperty(this.getFacingProperty()) ? (Direction)state.getValue(this.getFacingProperty()) : Direction.NORTH;
        }

        default void setFacing(Direction facing) {
            BlockState oldState = this.getState();
            BlockState newState = (BlockState)oldState.setValue(this.getFacingProperty(), facing);
            this.setState(newState);
        }
    }

    public interface BlockstateProvider {
        BlockState getState();

        void setState(BlockState var1);
    }

    public interface IDirectionalBE {
        Direction getFacing();

        void setFacing(Direction var1);

        PlacementLimitation getFacingLimitation();

        default Direction getFacingForPlacement(BlockPlaceContext ctx) {
            Direction f = this.getFacingLimitation().getDirectionForPlacement(ctx);
            return this.mirrorFacingOnPlacement(ctx.getPlayer()) ? f.getOpposite() : f;
        }

        default boolean mirrorFacingOnPlacement(LivingEntity placer) {
            return false;
        }

        default boolean canHammerRotate(Direction side, Vec3 hit, LivingEntity entity) {
            return true;
        }

        default void afterRotation(Direction oldDir, Direction newDir) {
        }
    }

    public interface IColouredBE {
        int getRenderColour(int var1);
    }

    public interface IColouredBlock {
        boolean hasCustomBlockColours();

        int getRenderColour(BlockState var1, @Nullable BlockGetter var2, @Nullable BlockPos var3, int var4);
    }

    public interface IRedstoneOutput {
        default int getWeakRSOutput(Direction side) {
            return this.getStrongRSOutput(side);
        }

        int getStrongRSOutput(Direction var1);

        boolean canConnectRedstone(Direction var1);
    }

    public interface IComparatorOverride {
        int getComparatorInputOverride();
    }

    public interface ISpawnInterdiction {
        double getInterdictionRangeSquared();
    }

    public interface ISoundBE {
        boolean shouldPlaySound(String var1);

        default float getSoundRadiusSq() {
            return 256.0F;
        }
    }

    public interface IBlockOverlayText {
        @Nullable
        Component[] getOverlayText(Player var1, HitResult var2, boolean var3);

        /** @deprecated */
        @Deprecated
        boolean useNixieFont(Player var1, HitResult var2);
    }
}


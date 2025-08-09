package mctmods.immersivetechnology.common.blocks.multiblocks.shapes;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class BoilerShape extends GenericShape {
    public static final BoilerShape GETTER = new BoilerShape();

    @NotNull
    @Override
    protected List<AABB> getShape(BlockPos posInMultiblock) {
        final int bX = posInMultiblock.getX();
        final int bY = posInMultiblock.getY();
        final int bZ = posInMultiblock.getZ();

        List<AABB> main = new ArrayList<>();

        if (bX == 0 && bY == 0 && bZ == 0) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.5625D, 0.1250D));
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.0625D, 1.0000D));
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 0.0625D, 0.5625D, 1.0000D));
            main.add(new AABB(0.0000D, 0.4375D, 0.0000D, 1.0000D, 0.5625D, 1.0000D));
            main.add(new AABB(0.4375D, 0.4375D, 0.8750D, 1.0000D, 1.0000D, 1.0000D));
            main.add(new AABB(0.4375D, 0.6250D, 0.8125D, 1.0000D, 1.0000D, 1.0000D));
            main.add(new AABB(0.4375D, 0.6875D, 0.7500D, 1.0000D, 1.0000D, 1.0000D));
            main.add(new AABB(0.4375D, 0.7500D, 0.6875D, 1.0000D, 1.0000D, 1.0000D));
            main.add(new AABB(0.4375D, 0.8125D, 0.6250D, 1.0000D, 1.0000D, 1.0000D));
            main.add(new AABB(0.4375D, 0.8750D, 0.5625D, 1.0000D, 1.0000D, 1.0000D));
            main.add(new AABB(0.4375D, 0.9375D, 0.5000D, 1.0000D, 1.0000D, 1.0000D));
            main.add(new AABB(0.6875D, 0.4375D, 0.3750D, 1.0000D, 1.0000D, 0.7500D));
        }
        if (bX == 0 && bY == 0 && bZ == 1) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.0625D, 1.0000D));
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 0.0625D, 1.0000D, 1.0000D));
            main.add(new AABB(0.0000D, 0.4375D, 0.0000D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 0 && bY == 0 && bZ == 2) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.0625D, 1.0000D));
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 0.0625D, 0.5625D, 1.0000D));
            main.add(new AABB(0.0000D, 0.4375D, 0.0000D, 1.0000D, 1.0000D, 0.1250D));
            main.add(new AABB(0.0000D, 0.4375D, 0.0000D, 1.0000D, 0.5625D, 1.0000D));
            main.add(new AABB(0.4375D, 0.4375D, 0.0000D, 1.0000D, 1.0000D, 0.2500D));
            main.add(new AABB(0.4375D, 0.6250D, 0.0000D, 1.0000D, 1.0000D, 0.3125D));
            main.add(new AABB(0.4375D, 0.6875D, 0.0000D, 1.0000D, 1.0000D, 0.3750D));
            main.add(new AABB(0.4375D, 0.7500D, 0.0000D, 1.0000D, 1.0000D, 0.4375D));
            main.add(new AABB(0.4375D, 0.8125D, 0.0000D, 1.0000D, 1.0000D, 0.5000D));
            main.add(new AABB(0.4375D, 0.8750D, 0.0000D, 1.0000D, 1.0000D, 0.5625D));
            main.add(new AABB(0.4375D, 0.9375D, 0.0000D, 1.0000D, 1.0000D, 0.6250D));
            main.add(new AABB(0.6875D, 0.4375D, 0.3750D, 1.0000D, 1.0000D, 0.7500D));
        }
        if (bX == 0 && bY == 1 && bZ == 0) {
            main.add(new AABB(0.0625D, 0.0000D, 0.9375D, 1.0000D, 1.0000D, 1.0000D));
            main.add(new AABB(0.0625D, 0.0625D, 0.8750D, 1.0000D, 1.0000D, 1.0000D));
            main.add(new AABB(0.0625D, 0.1250D, 0.8125D, 1.0000D, 1.0000D, 1.0000D));
            main.add(new AABB(0.0625D, 0.1875D, 0.7500D, 1.0000D, 1.0000D, 1.0000D));
            main.add(new AABB(0.0625D, 0.2500D, 0.6875D, 1.0000D, 1.0000D, 1.0000D));
            main.add(new AABB(0.0625D, 0.3125D, 0.6250D, 1.0000D, 1.0000D, 1.0000D));
            main.add(new AABB(0.0625D, 0.3750D, 0.5625D, 1.0000D, 1.0000D, 1.0000D));
            main.add(new AABB(0.0625D, 0.4375D, 0.5000D, 1.0000D, 1.0000D, 1.0000D));
            main.add(new AABB(0.4375D, 0.0000D, 0.4375D, 1.0000D, 1.0000D, 1.0000D));
            main.add(new AABB(0.4375D, 0.0625D, 0.3750D, 1.0000D, 1.0000D, 1.0000D));
            main.add(new AABB(0.4375D, 0.1250D, 0.3125D, 1.0000D, 1.0000D, 1.0000D));
            main.add(new AABB(0.4375D, 0.1875D, 0.2500D, 1.0000D, 1.0000D, 1.0000D));
            main.add(new AABB(0.6875D, 0.0000D, 0.3750D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 0 && bY == 1 && bZ == 1) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.2500D, 1.0000D));
            main.add(new AABB(0.0625D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 0 && bY == 1 && bZ == 2) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.2500D, 0.1250D));
            main.add(new AABB(0.0625D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 0.1875D));
            main.add(new AABB(0.0625D, 0.0625D, 0.0000D, 1.0000D, 1.0000D, 0.2500D));
            main.add(new AABB(0.0625D, 0.1250D, 0.0000D, 1.0000D, 1.0000D, 0.3125D));
            main.add(new AABB(0.0625D, 0.1875D, 0.0000D, 1.0000D, 1.0000D, 0.3750D));
            main.add(new AABB(0.0625D, 0.2500D, 0.0000D, 1.0000D, 1.0000D, 0.4375D));
            main.add(new AABB(0.0625D, 0.3125D, 0.0000D, 1.0000D, 1.0000D, 0.5000D));
            main.add(new AABB(0.0625D, 0.3750D, 0.0000D, 1.0000D, 1.0000D, 0.5625D));
            main.add(new AABB(0.0625D, 0.4375D, 0.0000D, 1.0000D, 1.0000D, 0.6250D));
            main.add(new AABB(0.4375D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 0.6875D));
            main.add(new AABB(0.4375D, 0.0625D, 0.0000D, 1.0000D, 1.0000D, 0.7500D));
            main.add(new AABB(0.4375D, 0.1250D, 0.0000D, 1.0000D, 1.0000D, 0.8125D));
            main.add(new AABB(0.4375D, 0.1875D, 0.0000D, 1.0000D, 1.0000D, 0.8750D));
            main.add(new AABB(0.6875D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 0.7500D));
        }
        if (bX == 0 && bY == 2 && bZ == 0) {
            main.add(new AABB(0.0625D, 0.0000D, 0.5000D, 1.0000D, 0.0625D, 1.0000D));
            main.add(new AABB(0.0625D, 0.0000D, 0.5625D, 1.0000D, 0.1250D, 1.0000D));
            main.add(new AABB(0.0625D, 0.0000D, 0.6250D, 1.0000D, 0.1875D, 1.0000D));
            main.add(new AABB(0.0625D, 0.0000D, 0.6875D, 1.0000D, 0.2500D, 1.0000D));
            main.add(new AABB(0.0625D, 0.0000D, 0.7500D, 1.0000D, 0.3125D, 1.0000D));
            main.add(new AABB(0.0625D, 0.0000D, 0.8125D, 1.0000D, 0.3750D, 1.0000D));
            main.add(new AABB(0.0625D, 0.0000D, 0.8750D, 1.0000D, 0.4375D, 1.0000D));
            main.add(new AABB(0.0625D, 0.0000D, 0.9375D, 1.0000D, 0.5000D, 1.0000D));
            main.add(new AABB(0.4375D, 0.0000D, 0.2500D, 1.0000D, 0.3125D, 1.0000D));
            main.add(new AABB(0.4375D, 0.0000D, 0.3125D, 1.0000D, 0.3750D, 1.0000D));
            main.add(new AABB(0.4375D, 0.0000D, 0.3750D, 1.0000D, 0.4375D, 1.0000D));
            main.add(new AABB(0.4375D, 0.0000D, 0.4375D, 1.0000D, 0.5000D, 1.0000D));
            main.add(new AABB(0.4375D, 0.0000D, 0.5000D, 1.0000D, 0.5625D, 1.0000D));
            main.add(new AABB(0.4375D, 0.0000D, 0.5625D, 1.0000D, 0.6250D, 1.0000D));
            main.add(new AABB(0.4375D, 0.0000D, 0.6250D, 1.0000D, 0.6875D, 1.0000D));
            main.add(new AABB(0.4375D, 0.0000D, 0.6875D, 1.0000D, 0.7500D, 1.0000D));
            main.add(new AABB(0.4375D, 0.0000D, 0.7500D, 1.0000D, 0.8125D, 1.0000D));
            main.add(new AABB(0.4375D, 0.0000D, 0.8125D, 1.0000D, 0.8750D, 1.0000D));
            main.add(new AABB(0.4375D, 0.0000D, 0.8750D, 1.0000D, 0.9375D, 1.0000D));
            main.add(new AABB(0.4375D, 0.0000D, 0.9375D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 0 && bY == 2 && bZ == 1) {
            main.add(new AABB(0.0000D, 0.2500D, 0.0000D, 1.0000D, 1.0000D, 1.0000D));
            main.add(new AABB(0.0625D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 0 && bY == 2 && bZ == 2) {
            main.add(new AABB(0.0000D, 0.2500D, 0.0000D, 1.0000D, 1.0000D, 0.1250D));
            main.add(new AABB(0.0625D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 0.1250D));
            main.add(new AABB(0.0625D, 0.0000D, 0.0000D, 1.0000D, 0.5000D, 0.1875D));
            main.add(new AABB(0.0625D, 0.0000D, 0.0000D, 1.0000D, 0.4375D, 0.2500D));
            main.add(new AABB(0.0625D, 0.0000D, 0.0000D, 1.0000D, 0.3750D, 0.3125D));
            main.add(new AABB(0.0625D, 0.0000D, 0.0000D, 1.0000D, 0.3125D, 0.3750D));
            main.add(new AABB(0.0625D, 0.0000D, 0.0000D, 1.0000D, 0.2500D, 0.4375D));
            main.add(new AABB(0.0625D, 0.0000D, 0.0000D, 1.0000D, 0.1875D, 0.5000D));
            main.add(new AABB(0.0625D, 0.0000D, 0.0000D, 1.0000D, 0.1250D, 0.5625D));
            main.add(new AABB(0.0625D, 0.0000D, 0.0000D, 1.0000D, 0.0625D, 0.6250D));
            main.add(new AABB(0.4375D, 0.0000D, 0.0000D, 1.0000D, 0.5000D, 0.6875D));
            main.add(new AABB(0.4375D, 0.0000D, 0.0000D, 1.0000D, 0.4375D, 0.7500D));
            main.add(new AABB(0.4375D, 0.0000D, 0.0000D, 1.0000D, 0.3750D, 0.8125D));
            main.add(new AABB(0.4375D, 0.0000D, 0.0000D, 1.0000D, 0.3125D, 0.8750D));
            main.add(new AABB(0.4375D, 0.0000D, 0.0000D, 1.0000D, 0.5625D, 0.6250D));
            main.add(new AABB(0.4375D, 0.0000D, 0.0000D, 1.0000D, 0.6250D, 0.5625D));
            main.add(new AABB(0.4375D, 0.0000D, 0.0000D, 1.0000D, 0.6875D, 0.5000D));
            main.add(new AABB(0.4375D, 0.0000D, 0.0000D, 1.0000D, 0.7500D, 0.4375D));
            main.add(new AABB(0.4375D, 0.0000D, 0.0000D, 1.0000D, 0.8125D, 0.3750D));
            main.add(new AABB(0.4375D, 0.0000D, 0.0000D, 1.0000D, 0.8750D, 0.3125D));
            main.add(new AABB(0.4375D, 0.0000D, 0.0000D, 1.0000D, 0.9375D, 0.2500D));
            main.add(new AABB(0.4375D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 0.1875D));
        }
        if (bX == 1 && bY == 0 && bZ == 0) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.5625D, 0.1250D));
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.0625D, 1.0000D));
            main.add(new AABB(0.0000D, 0.4375D, 0.0000D, 1.0000D, 0.5625D, 1.0000D));
            main.add(new AABB(0.0000D, 0.4375D, 0.3750D, 0.0625D, 1.0000D, 0.7500D));
            main.add(new AABB(0.0000D, 0.4375D, 0.8750D, 1.0000D, 1.0000D, 1.0000D));
            main.add(new AABB(0.0000D, 0.6250D, 0.8125D, 1.0000D, 1.0000D, 1.0000D));
            main.add(new AABB(0.0000D, 0.6875D, 0.7500D, 1.0000D, 1.0000D, 1.0000D));
            main.add(new AABB(0.0000D, 0.6250D, 0.4375D, 1.0000D, 1.0000D, 0.6875D));
            main.add(new AABB(0.0000D, 0.7500D, 0.4375D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 1 && bY == 0 && bZ == 1) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.0625D, 1.0000D));
            main.add(new AABB(0.0000D, 0.4375D, 0.0000D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 1 && bY == 0 && bZ == 2) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.0625D, 1.0000D));
            main.add(new AABB(0.0000D, 0.4375D, 0.0000D, 1.0000D, 1.0000D, 0.2500D));
            main.add(new AABB(0.0000D, 0.4375D, 0.0000D, 1.0000D, 0.5625D, 1.0000D));
            main.add(new AABB(0.0000D, 0.4375D, 0.3750D, 0.0625D, 1.0000D, 0.7500D));
            main.add(new AABB(0.0000D, 0.6250D, 0.0000D, 1.0000D, 1.0000D, 0.3125D));
            main.add(new AABB(0.0000D, 0.6875D, 0.0000D, 1.0000D, 1.0000D, 0.3750D));
            main.add(new AABB(0.0000D, 0.6250D, 0.4375D, 1.0000D, 1.0000D, 0.6875D));
            main.add(new AABB(0.0000D, 0.7500D, 0.0000D, 1.0000D, 1.0000D, 0.6875D));
        }
        if (bX == 1 && bY == 1 && bZ == 0) {
            main.add(new AABB(0.0000D, 0.0000D, 0.3750D, 0.0625D, 1.0000D, 1.0000D));
            main.add(new AABB(0.0000D, 0.1250D, 0.3125D, 1.0000D, 1.0000D, 1.0000D));
            main.add(new AABB(0.0000D, 0.1875D, 0.2500D, 1.0000D, 1.0000D, 1.0000D));
            main.add(new AABB(0.0000D, 0.0000D, 0.4375D, 1.0000D, 1.0000D, 1.0000D));
            main.add(new AABB(0.0000D, 0.0625D, 0.3750D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 1 && bY == 1 && bZ == 1) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 1 && bY == 1 && bZ == 2) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 0.6875D));
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 0.0625D, 1.0000D, 0.7500D));
            main.add(new AABB(0.0000D, 0.1250D, 0.0000D, 1.0000D, 1.0000D, 0.8125D));
            main.add(new AABB(0.0000D, 0.1875D, 0.0000D, 1.0000D, 1.0000D, 0.8750D));
            main.add(new AABB(0.0000D, 0.0625D, 0.0000D, 1.0000D, 1.0000D, 0.7500D));
        }
        if (bX == 1 && bY == 2 && bZ == 0) {
            main.add(new AABB(0.0000D, 0.0000D, 0.2500D, 1.0000D, 0.3125D, 1.0000D));
            main.add(new AABB(0.0000D, 0.0000D, 0.3125D, 1.0000D, 0.3750D, 1.0000D));
            main.add(new AABB(0.0000D, 0.0000D, 0.3750D, 1.0000D, 0.4375D, 1.0000D));
            main.add(new AABB(0.0000D, 0.0000D, 0.4375D, 1.0000D, 0.5000D, 1.0000D));
            main.add(new AABB(0.0000D, 0.0000D, 0.5000D, 1.0000D, 0.5625D, 1.0000D));
            main.add(new AABB(0.0000D, 0.0000D, 0.5625D, 1.0000D, 0.6250D, 1.0000D));
            main.add(new AABB(0.0000D, 0.0000D, 0.6250D, 1.0000D, 0.6875D, 1.0000D));
            main.add(new AABB(0.0000D, 0.0000D, 0.6875D, 1.0000D, 0.7500D, 1.0000D));
            main.add(new AABB(0.0000D, 0.0000D, 0.7500D, 1.0000D, 0.8125D, 1.0000D));
            main.add(new AABB(0.0000D, 0.0000D, 0.8125D, 1.0000D, 0.8750D, 1.0000D));
            main.add(new AABB(0.0000D, 0.0000D, 0.8750D, 1.0000D, 0.9375D, 1.0000D));
            main.add(new AABB(0.0000D, 0.0000D, 0.9375D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 1 && bY == 2 && bZ == 1) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 1 && bY == 2 && bZ == 2) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.3125D, 0.8750D));
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.3750D, 0.8125D));
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.4375D, 0.7500D));
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.5000D, 0.6875D));
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.5625D, 0.6250D));
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.6250D, 0.5625D));
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.6875D, 0.5000D));
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.7500D, 0.4375D));
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.8125D, 0.3750D));
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.8750D, 0.3125D));
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.9375D, 0.2500D));
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 0.1875D));
        }
        if (bX == 2 && bY == 0 && bZ == 0) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.5625D, 0.1250D));
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.0625D, 1.0000D));
            main.add(new AABB(0.0000D, 0.4375D, 0.0000D, 1.0000D, 0.5625D, 1.0000D));
            main.add(new AABB(0.0000D, 0.4375D, 0.8750D, 1.0000D, 1.0000D, 1.0000D));
            main.add(new AABB(0.0000D, 0.6250D, 0.4375D, 1.0000D, 1.0000D, 0.6875D));
            main.add(new AABB(0.0000D, 0.6250D, 0.8125D, 1.0000D, 1.0000D, 1.0000D));
            main.add(new AABB(0.0000D, 0.6875D, 0.7500D, 1.0000D, 1.0000D, 1.0000D));
            main.add(new AABB(0.0000D, 0.7500D, 0.4375D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 2 && bY == 0 && bZ == 1) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.0625D, 1.0000D));
            main.add(new AABB(0.0000D, 0.4375D, 0.0000D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 2 && bY == 0 && bZ == 2) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.0625D, 1.0000D));
            main.add(new AABB(0.0000D, 0.4375D, 0.0000D, 1.0000D, 1.0000D, 0.2500D));
            main.add(new AABB(0.0000D, 0.4375D, 0.0000D, 1.0000D, 0.5625D, 1.0000D));
            main.add(new AABB(0.0000D, 0.6250D, 0.0000D, 1.0000D, 1.0000D, 0.3125D));
            main.add(new AABB(0.0000D, 0.6250D, 0.4375D, 1.0000D, 1.0000D, 0.6875D));
            main.add(new AABB(0.0000D, 0.6875D, 0.0000D, 1.0000D, 1.0000D, 0.3750D));
            main.add(new AABB(0.0000D, 0.7500D, 0.0000D, 1.0000D, 1.0000D, 0.6875D));
        }
        if (bX == 2 && bY == 1 && bZ == 0) {
            main.add(new AABB(0.0000D, 0.0000D, 0.4375D, 1.0000D, 1.0000D, 1.0000D));
            main.add(new AABB(0.0000D, 0.0625D, 0.3750D, 1.0000D, 1.0000D, 1.0000D));
            main.add(new AABB(0.0000D, 0.1250D, 0.3125D, 1.0000D, 1.0000D, 1.0000D));
            main.add(new AABB(0.0000D, 0.1875D, 0.2500D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 2 && bY == 1 && bZ == 1) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 2 && bY == 1 && bZ == 2) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 0.6875D));
            main.add(new AABB(0.0000D, 0.0625D, 0.0000D, 1.0000D, 1.0000D, 0.7500D));
            main.add(new AABB(0.0000D, 0.1250D, 0.0000D, 1.0000D, 1.0000D, 0.8125D));
            main.add(new AABB(0.0000D, 0.1875D, 0.0000D, 1.0000D, 1.0000D, 0.8750D));
        }
        if (bX == 2 && bY == 2 && bZ == 0) {
            main.add(new AABB(0.0000D, 0.0000D, 0.2500D, 1.0000D, 0.3125D, 1.0000D));
            main.add(new AABB(0.0000D, 0.0000D, 0.3125D, 1.0000D, 0.3750D, 1.0000D));
            main.add(new AABB(0.0000D, 0.0000D, 0.3750D, 1.0000D, 0.4375D, 1.0000D));
            main.add(new AABB(0.0000D, 0.0000D, 0.4375D, 1.0000D, 0.5000D, 1.0000D));
            main.add(new AABB(0.0000D, 0.0000D, 0.5000D, 1.0000D, 0.5625D, 1.0000D));
            main.add(new AABB(0.0000D, 0.0000D, 0.5625D, 1.0000D, 0.6250D, 1.0000D));
            main.add(new AABB(0.0000D, 0.0000D, 0.6250D, 1.0000D, 0.6875D, 1.0000D));
            main.add(new AABB(0.0000D, 0.0000D, 0.6875D, 1.0000D, 0.7500D, 1.0000D));
            main.add(new AABB(0.0000D, 0.0000D, 0.7500D, 1.0000D, 0.8125D, 1.0000D));
            main.add(new AABB(0.0000D, 0.0000D, 0.8125D, 1.0000D, 0.8750D, 1.0000D));
            main.add(new AABB(0.0000D, 0.0000D, 0.8750D, 1.0000D, 0.9375D, 1.0000D));
            main.add(new AABB(0.0000D, 0.0000D, 0.9375D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 2 && bY == 2 && bZ == 1) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 2 && bY == 2 && bZ == 2) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.3125D, 0.8750D));
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.3750D, 0.8125D));
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.4375D, 0.7500D));
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.5000D, 0.6875D));
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.5625D, 0.6250D));
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.6250D, 0.5625D));
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.6875D, 0.5000D));
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.7500D, 0.4375D));
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.8125D, 0.3750D));
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.8750D, 0.3125D));
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.9375D, 0.2500D));
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 0.1875D));
        }
        if (bX == 3 && bY == 0 && bZ == 0) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.5625D, 0.1250D));
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.0625D, 1.0000D));
            main.add(new AABB(0.0000D, 0.4375D, 0.0000D, 1.0000D, 0.5625D, 1.0000D));
            main.add(new AABB(0.0000D, 0.4375D, 0.8750D, 0.8125D, 1.0000D, 1.0000D));
            main.add(new AABB(0.0000D, 0.6250D, 0.4375D, 0.5625D, 1.0000D, 0.6875D));
            main.add(new AABB(0.0000D, 0.6250D, 0.8125D, 0.8125D, 1.0000D, 1.0000D));
            main.add(new AABB(0.0000D, 0.6875D, 0.7500D, 0.8125D, 1.0000D, 1.0000D));
            main.add(new AABB(0.0000D, 0.7500D, 0.4375D, 0.5625D, 1.0000D, 1.0000D));
            main.add(new AABB(0.1875D, 0.4375D, 0.3750D, 0.5625D, 1.0000D, 0.7500D));
            main.add(new AABB(0.0000D, 0.7500D, 0.6875D, 0.8125D, 1.0000D, 1.0000D));
            main.add(new AABB(0.0000D, 0.8125D, 0.6250D, 0.8125D, 1.0000D, 1.0000D));
            main.add(new AABB(0.0000D, 0.8750D, 0.5625D, 0.8125D, 1.0000D, 1.0000D));
            main.add(new AABB(0.0000D, 0.9375D, 0.5000D, 0.8125D, 1.0000D, 1.0000D));
            main.add(new AABB(0.9375D, 0.9375D, 0.0000D, 1.0000D, 1.0000D, 0.6250D));
        }
        if (bX == 3 && bY == 0 && bZ == 1) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.0625D, 1.0000D));
            main.add(new AABB(0.0000D, 0.4375D, 0.0000D, 0.8125D, 1.0000D, 1.0000D));
            main.add(new AABB(0.0000D, 0.4375D, 0.0000D, 1.0000D, 0.5625D, 1.0000D));
            main.add(new AABB(0.9375D, 0.4375D, 0.0000D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 3 && bY == 0 && bZ == 2) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.0625D, 1.0000D));
            main.add(new AABB(0.0000D, 0.4375D, 0.0000D, 1.0000D, 0.5625D, 1.0000D));
            main.add(new AABB(0.0000D, 0.4375D, 0.0000D, 0.8125D, 1.0000D, 0.2500D));
            main.add(new AABB(0.0000D, 0.6250D, 0.0000D, 0.8125D, 1.0000D, 0.3125D));
            main.add(new AABB(0.0000D, 0.6250D, 0.4375D, 0.5625D, 1.0000D, 0.6875D));
            main.add(new AABB(0.0000D, 0.6875D, 0.0000D, 0.8125D, 1.0000D, 0.3750D));
            main.add(new AABB(0.0000D, 0.7500D, 0.0000D, 0.5625D, 1.0000D, 0.6875D));
            main.add(new AABB(0.1875D, 0.4375D, 0.3750D, 0.5625D, 1.0000D, 0.7500D));
            main.add(new AABB(0.0000D, 0.7500D, 0.0000D, 0.8125D, 1.0000D, 0.4375D));
            main.add(new AABB(0.0000D, 0.8125D, 0.0000D, 0.8125D, 1.0000D, 0.5000D));
            main.add(new AABB(0.0000D, 0.8750D, 0.0000D, 0.8125D, 1.0000D, 0.5625D));
            main.add(new AABB(0.0000D, 0.9375D, 0.0000D, 0.8125D, 1.0000D, 0.6250D));
            main.add(new AABB(0.9375D, 0.4375D, 0.0000D, 1.0000D, 1.0000D, 0.1250D));
        }
        if (bX == 3 && bY == 1 && bZ == 0) {
            main.add(new AABB(0.0000D, 0.0000D, 0.4375D, 0.8125D, 1.0000D, 1.0000D));
            main.add(new AABB(0.0000D, 0.0625D, 0.3750D, 0.8125D, 1.0000D, 1.0000D));
            main.add(new AABB(0.0000D, 0.1250D, 0.3125D, 0.8125D, 1.0000D, 1.0000D));
            main.add(new AABB(0.0000D, 0.1875D, 0.2500D, 0.8125D, 1.0000D, 1.0000D));
            main.add(new AABB(0.1875D, 0.0000D, 0.3750D, 0.5625D, 1.0000D, 1.0000D));
            main.add(new AABB(0.9375D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 0.6250D));
        }
        if (bX == 3 && bY == 1 && bZ == 1) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 0.8125D, 1.0000D, 1.0000D));
            main.add(new AABB(0.0000D, 0.1875D, 0.0000D, 1.0000D, 1.0000D, 1.0000D));
            main.add(new AABB(0.9375D, 0.0000D, 0.0000D, 1.0000D, 0.0625D, 1.0000D));
        }
        if (bX == 3 && bY == 1 && bZ == 2) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 0.8125D, 1.0000D, 0.6875D));
            main.add(new AABB(0.0000D, 0.0625D, 0.0000D, 0.8125D, 1.0000D, 0.7500D));
            main.add(new AABB(0.0000D, 0.1250D, 0.0000D, 0.8125D, 1.0000D, 0.8125D));
            main.add(new AABB(0.0000D, 0.1875D, 0.0000D, 0.8125D, 1.0000D, 0.8750D));
            main.add(new AABB(0.1875D, 0.0000D, 0.0000D, 0.5625D, 1.0000D, 0.7500D));
            main.add(new AABB(0.0000D, 0.1875D, 0.0000D, 1.0000D, 1.0000D, 0.1250D));
            main.add(new AABB(0.9375D, 0.0000D, 0.0000D, 1.0000D, 0.0625D, 0.1250D));
        }
        if (bX == 3 && bY == 2 && bZ == 0) {
            main.add(new AABB(0.0000D, 0.0000D, 0.2500D, 0.8125D, 0.3125D, 1.0000D));
            main.add(new AABB(0.0000D, 0.0000D, 0.3125D, 0.8125D, 0.3750D, 1.0000D));
            main.add(new AABB(0.0000D, 0.0000D, 0.3750D, 0.8125D, 0.4375D, 1.0000D));
            main.add(new AABB(0.0000D, 0.0000D, 0.4375D, 0.8125D, 0.5000D, 1.0000D));
            main.add(new AABB(0.0000D, 0.0000D, 0.5000D, 0.8125D, 0.5625D, 1.0000D));
            main.add(new AABB(0.0000D, 0.0000D, 0.5625D, 0.8125D, 0.6250D, 1.0000D));
            main.add(new AABB(0.0000D, 0.0000D, 0.6250D, 0.8125D, 0.6875D, 1.0000D));
            main.add(new AABB(0.0000D, 0.0000D, 0.6875D, 0.8125D, 0.7500D, 1.0000D));
            main.add(new AABB(0.0000D, 0.0000D, 0.7500D, 0.8125D, 0.8125D, 1.0000D));
            main.add(new AABB(0.0000D, 0.0000D, 0.8125D, 0.8125D, 0.8750D, 1.0000D));
            main.add(new AABB(0.0000D, 0.0000D, 0.8750D, 0.8125D, 0.9375D, 1.0000D));
            main.add(new AABB(0.0000D, 0.0000D, 0.9375D, 0.8125D, 1.0000D, 1.0000D));
            main.add(new AABB(0.9375D, 0.0000D, 0.0000D, 1.0000D, 0.0625D, 0.6250D));
        }
        if (bX == 3 && bY == 2 && bZ == 1) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 0.8125D, 1.0000D, 1.0000D));
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.3125D, 1.0000D));
        }
        if (bX == 3 && bY == 2 && bZ == 2) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 0.8125D, 0.3125D, 0.8750D));
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 0.8125D, 0.3750D, 0.8125D));
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 0.8125D, 0.4375D, 0.7500D));
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 0.8125D, 0.5000D, 0.6875D));
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 0.8125D, 0.5625D, 0.6250D));
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 0.8125D, 0.6250D, 0.5625D));
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 0.8125D, 0.6875D, 0.5000D));
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 0.8125D, 0.7500D, 0.4375D));
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 0.8125D, 0.8125D, 0.3750D));
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 0.8125D, 0.8750D, 0.3125D));
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 0.8125D, 0.9375D, 0.2500D));
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 0.8125D, 1.0000D, 0.1875D));
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.3125D, 0.1250D));
        }
        if (bX == 4 && bY == 0 && bZ == 0) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.5625D, 0.1250D));
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.0625D, 1.0000D));
            main.add(new AABB(0.0000D, 0.4375D, 0.0000D, 1.0000D, 0.5625D, 1.0000D));
            main.add(new AABB(0.0000D, 0.9375D, 0.0000D, 1.0000D, 1.0000D, 0.6250D));
            main.add(new AABB(0.0625D, 0.4375D, 0.1250D, 0.3125D, 1.0000D, 0.5000D));
            main.add(new AABB(0.6875D, 0.4375D, 0.1250D, 0.9375D, 1.0000D, 0.5000D));
            main.add(new AABB(0.9375D, 0.0000D, 0.0000D, 1.0000D, 0.5625D, 1.0000D));
        }
        if (bX == 4 && bY == 0 && bZ == 1) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.0625D, 1.0000D));
            main.add(new AABB(0.0000D, 0.4375D, 0.0000D, 1.0000D, 1.0000D, 1.0000D));
            main.add(new AABB(0.9375D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 4 && bY == 0 && bZ == 2) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.0625D, 1.0000D));
            main.add(new AABB(0.0000D, 0.4375D, 0.0000D, 1.0000D, 0.5625D, 1.0000D));
            main.add(new AABB(0.0000D, 0.4375D, 0.0000D, 1.0000D, 1.0000D, 0.1250D));
            main.add(new AABB(0.0000D, 0.9375D, 0.5625D, 1.0000D, 1.0000D, 1.0000D));
            main.add(new AABB(0.2500D, 0.4375D, 0.1875D, 0.7500D, 0.6875D, 0.6875D));
            main.add(new AABB(0.3125D, 0.4375D, 0.2500D, 0.6875D, 1.0000D, 0.6250D));
            main.add(new AABB(0.9375D, 0.0000D, 0.0000D, 1.0000D, 0.5625D, 1.0000D));
        }
        if (bX == 4 && bY == 1 && bZ == 0) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 1.0000D, 0.6250D));
            main.add(new AABB(0.1875D, 0.4375D, 0.7500D, 0.8125D, 1.0000D, 1.0000D));
        }
        if (bX == 4 && bY == 1 && bZ == 1) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.0625D, 1.0000D));
            main.add(new AABB(0.0000D, 0.1875D, 0.0000D, 0.0625D, 1.0000D, 1.0000D));
            main.add(new AABB(0.0000D, 0.3125D, 0.1250D, 0.9375D, 1.0000D, 1.0000D));
            main.add(new AABB(0.1875D, 0.4375D, 0.0000D, 0.8125D, 1.0000D, 1.0000D));
            main.add(new AABB(0.0000D, 0.4375D, 0.2500D, 1.0000D, 1.0000D, 0.8750D));
        }
        if (bX == 4 && bY == 1 && bZ == 2) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.0625D, 0.1250D));
            main.add(new AABB(0.0000D, 0.0000D, 0.5625D, 1.0000D, 1.0000D, 0.6875D));
            main.add(new AABB(0.0000D, 0.0000D, 0.5625D, 0.1250D, 1.0000D, 1.0000D));
            main.add(new AABB(0.0000D, 0.1875D, 0.0000D, 0.0625D, 1.0000D, 0.1250D));
            main.add(new AABB(0.0000D, 0.0000D, 0.5625D, 1.0000D, 0.0625D, 1.0000D));
            main.add(new AABB(0.2500D, 0.5000D, 0.0000D, 0.7500D, 1.0000D, 0.1250D));
            main.add(new AABB(0.2500D, 0.5000D, 0.1875D, 0.7500D, 1.0000D, 0.6875D));
            main.add(new AABB(0.3125D, 0.0000D, 0.2500D, 0.6875D, 1.0000D, 0.6875D));
            main.add(new AABB(0.3125D, 0.5625D, 0.0000D, 0.6875D, 0.9375D, 0.6875D));
            main.add(new AABB(0.8750D, 0.0000D, 0.5625D, 1.0000D, 1.0000D, 1.0000D));
        }
        if (bX == 4 && bY == 2 && bZ == 0) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 1.0000D, 0.0625D, 0.6250D));
            main.add(new AABB(0.1875D, 0.0000D, 0.7500D, 0.8125D, 0.0625D, 1.0000D));
        }
        if (bX == 4 && bY == 2 && bZ == 1) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 0.0625D, 0.3125D, 1.0000D));
            main.add(new AABB(0.0000D, 0.0000D, 0.1250D, 0.9375D, 0.1875D, 1.0000D));
            main.add(new AABB(0.1875D, 0.0000D, 0.0000D, 0.8125D, 0.0625D, 1.0000D));
            main.add(new AABB(0.0000D, 0.0000D, 0.2500D, 1.0000D, 0.0625D, 0.8750D));
        }
        if (bX == 4 && bY == 2 && bZ == 2) {
            main.add(new AABB(0.0000D, 0.0000D, 0.0000D, 0.0625D, 0.3125D, 0.1250D));
            main.add(new AABB(0.0000D, 0.0000D, 0.5625D, 1.0000D, 0.5625D, 0.6875D));
            main.add(new AABB(0.0000D, 0.0000D, 0.5625D, 0.1250D, 0.5625D, 1.0000D));
            main.add(new AABB(0.0000D, 0.4375D, 0.5625D, 1.0000D, 0.5625D, 1.0000D));
            main.add(new AABB(0.8750D, 0.0000D, 0.5625D, 1.0000D, 0.5625D, 1.0000D));
        }

        return main;
    }
}

package mctmods.immersivetechnology.common.util.compat.groovyscript;

import com.immersiveconvergence.api.fluid.ICPipes;

import com.cleanroommc.groovyscript.api.GroovyLog;
import com.cleanroommc.groovyscript.registry.VirtualizedRegistry;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import java.util.Collections;

@SuppressWarnings("unused")
public class PressurizedFluid extends VirtualizedRegistry<Fluid> {

    public PressurizedFluid() {
        super(Collections.singletonList("pressurizedFluid"));
    }

    @Override
    public void onReload() {
        removeScripted().forEach(ICPipes::removeNormallyPressurized);
        restoreFromBackup().forEach(ICPipes::addNormallyPressurized);
    }

    public void add(FluidStack fluid) {
        if (fluid == null || fluid.getFluid() == null) {
            GroovyLog.msg("Error adding Immersive Technology pressurized fluid").add("the fluid does not exist").error().post();
            return;
        }
        if (ICPipes.isNormallyPressurized(fluid.getFluid())) { return; }
        ICPipes.addNormallyPressurized(fluid.getFluid());
        addScripted(fluid.getFluid());
    }

    public void remove(FluidStack fluid) {
        if (fluid == null || fluid.getFluid() == null) {
            GroovyLog.msg("Error removing Immersive Technology pressurized fluid").add("the fluid does not exist").error().post();
            return;
        }
        if (!ICPipes.isNormallyPressurized(fluid.getFluid())) { return; }
        ICPipes.removeNormallyPressurized(fluid.getFluid());
        addBackup(fluid.getFluid());
    }
}

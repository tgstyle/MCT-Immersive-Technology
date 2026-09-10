package mctmods.immersivetechnology.common.util.compat.groovyscript;

import com.cleanroommc.groovyscript.api.GroovyPlugin;
import com.cleanroommc.groovyscript.api.IGroovyContainer;
import com.cleanroommc.groovyscript.compat.mods.GroovyContainer;
import com.cleanroommc.groovyscript.compat.mods.GroovyPropertyContainer;
import mctmods.immersivetechnology.ImmersiveTechnology;

public class ITGroovyPlugin implements GroovyPlugin {

    @Override public String getModId() { return ImmersiveTechnology.MODID; }

    @Override public String getContainerName() { return ImmersiveTechnology.NAME; }

    @Override public IGroovyContainer.Priority getOverridePriority() { return IGroovyContainer.Priority.OVERRIDE; }

    @Override public void onCompatLoaded(GroovyContainer<?> container) {}

    @Override public GroovyPropertyContainer createGroovyPropertyContainer() { return new ITGroovyProperties(); }
}

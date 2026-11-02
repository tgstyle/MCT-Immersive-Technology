package mctmods.immersivetechnology.core.integration.groovyscript;

import mctmods.immersivetechnology.ImmersiveTechnology;

import com.cleanroommc.groovyscript.api.GroovyPlugin;
import com.cleanroommc.groovyscript.api.IGroovyContainer;
import com.cleanroommc.groovyscript.compat.mods.GroovyContainer;
import com.cleanroommc.groovyscript.compat.mods.GroovyPropertyContainer;
import javax.annotation.Nonnull;

public class ITGroovyPlugin implements GroovyPlugin {

    @Override @Nonnull public String getModId() { return ImmersiveTechnology.MODID; }

    @Override @Nonnull public String getContainerName() { return ImmersiveTechnology.NAME; }

    @Override @Nonnull public IGroovyContainer.Priority getOverridePriority() { return IGroovyContainer.Priority.OVERRIDE; }

    @Override public void onCompatLoaded(GroovyContainer<?> container) {}

    @Override public GroovyPropertyContainer createGroovyPropertyContainer() { return new ITGroovyProperties(); }
}

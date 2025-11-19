package mctmods.immersivetechnology.core;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.appender.RollingRandomAccessFileAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.layout.PatternSelector;
import org.apache.logging.log4j.core.appender.rolling.CompositeTriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.TriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.SizeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.RolloverStrategy;

import org.spongepowered.asm.mixin.Mixins;

import java.lang.reflect.Field;
import java.util.Arrays;

@Mod(modid = "mct_mixin", name = "MCT Mixin", version = "1.0", acceptedMinecraftVersions = "[1.12.2]")
public class MCTMixin {
    public static final Logger LOGGER = LogManager.getLogger("MCT_Mixin");

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        Mixins.addConfiguration("mixins.immersiveengineering.json");
        LOGGER.info("Loaded config: enableAdditionsLogging={}, enablePotentialsLogging={}, enableDevSided={}", MCTMixinConfig.mixinSettings.enableAdditionsLogging, MCTMixinConfig.mixinSettings.enablePotentialsLogging, MCTMixinConfig.mixinSettings.enableDevSided);
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();
        org.apache.logging.log4j.core.Appender app = config.getAppenders().get("File");
        if (app instanceof RollingRandomAccessFileAppender) {
            RollingRandomAccessFileAppender oldApp = (RollingRandomAccessFileAppender) app;
            PatternLayout oldLayout = (PatternLayout) oldApp.getLayout();
            String oldPattern = oldLayout.getConversionPattern();
            String newPattern = oldPattern.replace("%msg%n", "%msg %ex{short}%n");
            PatternSelector patternSelector = null;
            try { Field f = PatternLayout.class.getDeclaredField("patternSelector"); f.setAccessible(true); patternSelector = (PatternSelector) f.get(oldLayout); } catch (Exception e) { LOGGER.error("Error getting patternSelector", e); }
            PatternLayout.Builder layoutBuilder = PatternLayout.newBuilder();
            layoutBuilder.withPattern(newPattern);
            layoutBuilder.withPatternSelector(patternSelector);
            layoutBuilder.withRegexReplacement(null);
            layoutBuilder.withCharset(oldLayout.getCharset());
            layoutBuilder.withAlwaysWriteExceptions(true);
            layoutBuilder.withNoConsoleNoAnsi(false);
            layoutBuilder.withConfiguration(config);
            PatternLayout newLayout = layoutBuilder.build();
            RolloverStrategy strategy = oldApp.getManager().getRolloverStrategy();
            TriggeringPolicy oldPolicy = oldApp.getManager().getTriggeringPolicy();
            TriggeringPolicy sizePolicy = SizeBasedTriggeringPolicy.createPolicy("195 MB");
            TriggeringPolicy newPolicy;
            if (oldPolicy instanceof CompositeTriggeringPolicy) { CompositeTriggeringPolicy composite = (CompositeTriggeringPolicy) oldPolicy; TriggeringPolicy[] policies = composite.getTriggeringPolicies(); TriggeringPolicy[] newPolicies = Arrays.copyOf(policies, policies.length + 1); newPolicies[policies.length] = sizePolicy; newPolicy = CompositeTriggeringPolicy.createPolicy(newPolicies); }
            else { newPolicy = CompositeTriggeringPolicy.createPolicy(oldPolicy, sizePolicy); }
            boolean appendB = true;
            int bufferSizeI = 262144;
            boolean immediateFlushB = true;
            RollingRandomAccessFileAppender.Builder<?> appBuilder = RollingRandomAccessFileAppender.newBuilder();
            appBuilder.setName(oldApp.getName());
            appBuilder.withFileName(oldApp.getFileName());
            appBuilder.withFilePattern(oldApp.getFilePattern());
            appBuilder.withAppend(appendB);
            appBuilder.withImmediateFlush(immediateFlushB);
            appBuilder.withBufferSize(bufferSizeI);
            appBuilder.withPolicy(newPolicy);
            appBuilder.withStrategy(strategy);
            appBuilder.setLayout(newLayout);
            appBuilder.setFilter(oldApp.getFilter());
            appBuilder.setIgnoreExceptions(oldApp.ignoreExceptions());
            appBuilder.setConfiguration(config);
            RollingRandomAccessFileAppender newApp = appBuilder.build();
            oldApp.stop();
            config.getAppenders().remove("File");
            config.addAppender(newApp);
            newApp.start();
            ctx.updateLoggers();
        }
    }
}

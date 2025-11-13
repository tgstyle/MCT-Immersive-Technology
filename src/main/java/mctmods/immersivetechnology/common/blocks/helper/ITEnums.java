package mctmods.immersivetechnology.common.blocks.helper;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class ITEnums {
    public enum IOSideConfig implements StringRepresentable {
        NONE("none"),
        INPUT("in"),
        OUTPUT("out");

        public static final ITEnums.IOSideConfig[] VALUES = values();
        final String texture;

        IOSideConfig(String texture) {
            this.texture = texture;
        }

        public @NotNull String getSerializedName() {
            return this.toString().toLowerCase(Locale.ENGLISH);
        }

        public String getTextureName() {
            return this.texture;
        }

        public static ITEnums.IOSideConfig next(ITEnums.IOSideConfig current) {
            return current == INPUT ? OUTPUT : (current == OUTPUT ? NONE : INPUT);
        }
    }
}

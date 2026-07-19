package mctmods.immersivetechnology.common.blocks.helper;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class Enums {
    public enum IOSideConfig implements StringRepresentable {
        NONE("none"),
        INPUT("in"),
        OUTPUT("out");

        public static final Enums.IOSideConfig[] VALUES = values();
        final String texture;

        IOSideConfig(String texture) { this.texture = texture; }

        @NotNull public String getSerializedName() { return this.toString().toLowerCase(Locale.ENGLISH); }

        public String getTextureName() { return this.texture; }

        public static Enums.IOSideConfig next(Enums.IOSideConfig current) { return current == INPUT ? OUTPUT : (current == OUTPUT ? NONE : INPUT); }
    }
}

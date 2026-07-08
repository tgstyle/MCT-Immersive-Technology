package mctmods.immersivetechnology.mixin.common;

import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(StructureTemplate.class)
public interface IStructureTemplateAccessorMixin {
    @Accessor("palettes")
    List<StructureTemplate.Palette> it$getPalettes();
}

package mctmods.immersivetechnology.common.data.generators;

import mctmods.immersivetechnology.common.data.models.ITTRSRModelBuilder;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.registration.ITMultiblockProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.client.model.generators.ModelProvider;
import net.minecraftforge.client.model.generators.loaders.CompositeModelBuilder;
import net.minecraftforge.client.model.generators.loaders.ObjModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Supplier;

public class ITComplexItemModelProvider extends ModelProvider<ITTRSRModelBuilder> {
    public ITComplexItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, ITLib.MODID, ITEM_FOLDER, ITTRSRModelBuilder::new, existingFileHelper);
    }

    @Override @NotNull public String getName() { return getClass().getSimpleName(); }

    @Override protected void registerModels() {
        generateMultiblockModel("advanced_coke_oven", "stone", ITMultiblockProvider.ADVANCED_COKE_OVEN.block(), new Vector3f(4.0f, -3.5f, -1.0f), 0.1875f, 0.0625f, 0.0625f);
        generateMultiblockModel("alternator", "metal", ITMultiblockProvider.ALTERNATOR.block(), new Vector3f(5.5f, -3.5f, -2.0f), 0.1875f, 0.0625f, 0.0625f);
        generateMultiblockModel("boiler_liquid", "metal", ITMultiblockProvider.BOILER_LIQUID.block(), new Vector3f(-2.0f, -0.525f, 0.5f), 0.1875f, 0.0625f, 0.0625f);
        generateMultiblockModel("boiler_solid", "metal", ITMultiblockProvider.BOILER_SOLID.block(), new Vector3f(-2.0f, -0.525f, 0.5f), 0.1875f, 0.0625f, 0.0625f);
        generateMultiblockModel("boiler_tank", "metal", ITMultiblockProvider.BOILER_TANK.block(), new Vector3f(-3.2f, 0.5f, 0.5f), 0.1875f, 0.0625f, 0.0625f);
        generateMultiblockModel("cooling_tower", "stone", ITMultiblockProvider.COOLING_TOWER.block(), new Vector3f(-3.0f, -2.9f, -1.5f), 0.075f, 0.0375f, 0.0375f);
        generateMultiblockModel("distiller", "metal", ITMultiblockProvider.DISTILLER.block(), new Vector3f(0.5f, -0.5f, -0.5f), 0.1875f, 0.0625f, 0.0625f);
        generateMultiblockModel("electrolytic_crucible_battery", "metal", ITMultiblockProvider.ELECTROLYTIC_CRUCIBLE_BATTERY.block(), new Vector3f(-4.5f, -0.5f, -0.5f), 0.11f, 0.0625f, 0.0625f);
        generateMultiblockModel("gas_turbine", "metal", ITMultiblockProvider.GAS_TURBINE.block(), new Vector3f(-4.2f, 2.4f, -3.5f), 0.13f, 0.05f, 0.05f);
        generateMultiblockModel("heat_exchanger", "metal", ITMultiblockProvider.HEAT_EXCHANGER.block(), new Vector3f(-2.5f, 2.2f, -3.5f), 0.1875f, 0.05f, 0.05f);
        generateMultiblockModel("solar_melter", "metal", ITMultiblockProvider.SOLAR_MELTER.block(), new Vector3f(1.8f, -7.0f, -1.5f), 0.06f, 0.03f, 0.03f);
        generateMultiblockModel("solar_reflector", "metal", ITMultiblockProvider.SOLAR_REFLECTOR.block(), new Vector3f(4.5f, -1.8f, -1.5f), 0.1875f, 0.0625f, 0.0625f);
        generateMultiblockModel("solar_tower", "metal", ITMultiblockProvider.SOLAR_TOWER.block(), new Vector3f(1.8f, -7.0f, -1.5f), 0.06f, 0.03f, 0.03f);
        generateMultiblockModel("steam_turbine", "metal", ITMultiblockProvider.STEAM_TURBINE.block(), new Vector3f(6.5f, -4.5f, -5.5f), 0.11f, 0.04f, 0.04f);
        generateMultiblockModel("steel_sheetmetal_tank", "metal", ITMultiblockProvider.STEEL_SHEETMETAL_TANK.block(), new Vector3f(0f, -4.0f, 0.5f), 0.1875f, 0.0625f, 0.0625f);
    }

    private ITTRSRModelBuilder createObjModel(String modelPath, String jsonName) {
        return getBuilder(jsonName).customLoader(ObjModelBuilder::begin).modelLocation(modLoc("models/" + modelPath)).flipV(true).end();
    }

    private ITTRSRModelBuilder createTransformedObjModel(String modelPath, String jsonName, @Nullable Vector3f translation) {
        ITTRSRModelBuilder builder = createObjModel(modelPath, jsonName);
        if (translation != null) { builder.rootTransforms().translation(translation.x(), translation.y(), translation.z()).end(); }
        return builder;
    }

    private ITTRSRModelBuilder createSolarReflectorModel(Supplier<? extends ItemLike> block) {
        String base = "multiblock/metal/obj/solar_reflector/";
        ITTRSRModelBuilder reflectorModel = createTransformedObjModel(base + "solar_reflector.obj", "solar_reflector_reflector", null);
        ITTRSRModelBuilder supportModel = createTransformedObjModel(base + "solar_reflector_support.obj", "solar_reflector_support", new Vector3f(1.6f, 0.0f, 1.6f));
        ITTRSRModelBuilder mirrorModel = createTransformedObjModel(base + "solar_reflector_mirror.obj", "solar_reflector_mirror", new Vector3f(1.6f, 0.0f, 1.6f));

        return getBuilder(name(block.get())).customLoader(CompositeModelBuilder::begin)
                .child("reflector", reflectorModel)
                .child("support", supportModel)
                .child("mirror", mirrorModel)
                .end();
    }

    private ITTRSRModelBuilder createBoilerSolidModel(Supplier<? extends ItemLike> block, String objPath) {
        return getBuilder(name(block.get())).customLoader(ObjModelBuilder::begin)
                .modelLocation(modLoc("models/" + objPath))
                .flipV(true)
                .end()
                .texture("cube_front", modLoc("multiblock/metal/boiler_solid"));
    }

    private void applyStandardTransforms(ITTRSRModelBuilder model, Vector3f guiTrans, float guiScale, float groundScale, float fixedScale) {
        ModelBuilder<?>.TransformsBuilder trans = model.transforms();
        doTransform(trans, ItemDisplayContext.FIRST_PERSON_LEFT_HAND, new Vector3f(-1.75F, 2.5F, 1.25F), new Vector3f(0, 225, 0), 0.03125F);
        doTransform(trans, ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, new Vector3f(-1.75F, 2.5F, 1.75F), new Vector3f(0, 225, 0), 0.03125F);
        doTransform(trans, ItemDisplayContext.THIRD_PERSON_LEFT_HAND, new Vector3f(-0.75F, 0, -1.25F), new Vector3f(0, 90, 0), 0.03125F);
        doTransform(trans, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, new Vector3f(1.0F, 0, -1.75F), new Vector3f(0, 270, 0), 0.03125F);
        doTransform(trans, ItemDisplayContext.HEAD, new Vector3f(0, 8, -8), null, 0.2F);
        doTransform(trans, ItemDisplayContext.GUI, guiTrans, new Vector3f(30, 225, 0), guiScale);
        doTransform(trans, ItemDisplayContext.GROUND, new Vector3f(-1.5F, 3, -1.5F), null, groundScale);
        doTransform(trans, ItemDisplayContext.FIXED, new Vector3f(-1, -8, -2), null, fixedScale);
    }

    private void doTransform(ModelBuilder<?>.TransformsBuilder transform, ItemDisplayContext type, @Nullable Vector3f translation, @Nullable Vector3f rotationAngle, float scale) {
        ModelBuilder<?>.TransformsBuilder.TransformVecBuilder trans = transform.transform(type);
        if (translation != null) { trans.translation(translation.x(), translation.y(), translation.z()); }
        if (rotationAngle != null) { trans.rotation(rotationAngle.x(), rotationAngle.y(), rotationAngle.z()); }
        trans.scale(scale);
        trans.end();
    }

    private ITTRSRModelBuilder obj(Supplier<? extends ItemLike> item, String model) { return obj(item.get(), model); }

    private ITTRSRModelBuilder obj(ItemLike item, String model) {
        return getBuilder(name(item)).customLoader(ObjModelBuilder::begin).modelLocation(modLoc("models/" + model)).flipV(true).end();
    }

    private String name(ItemLike item) { return Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(item.asItem())).getPath(); }

    private void generateMultiblockModel(String id, String type, Supplier<? extends ItemLike> block, Vector3f guiTrans, float guiScale, float groundScale, float fixedScale) {
        ITTRSRModelBuilder model;
        String objPath = "multiblock/" + type + "/obj/" + id + "/" + id + ".obj";

        if ("solar_reflector".equals(id)) { model = createSolarReflectorModel(block); }
        else if ("boiler_solid".equals(id)) { model = createBoilerSolidModel(block, objPath); }
        else { model = obj(block, objPath); }

        applyStandardTransforms(model, guiTrans, guiScale, groundScale, fixedScale);
    }
}

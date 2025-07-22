package mctmods.immersivetechnology.common.data.generators;

import mctmods.immersivetechnology.common.data.TRSRModelBuilder;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.registration.ITBlocks;
import mctmods.immersivetechnology.core.registration.ITMultiblockProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.client.model.generators.ModelProvider;
import net.minecraftforge.client.model.generators.loaders.ObjModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Supplier;

public class ITComplexItemModelProvider extends ModelProvider<TRSRModelBuilder> {
    public ITComplexItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper)  { super(output, ITLib.MODID, ITEM_FOLDER, TRSRModelBuilder::new, existingFileHelper); }

    @Override
    public @NotNull String getName()
    {
        return getClass().getSimpleName();
    }

    static final ResourceLocation ITEM_GENERATED = new ResourceLocation("minecraft", "item/generated");

    @Override
    protected void registerModels() {
        generateMultiblockModel("boiler", ITMultiblockProvider.BOILER.block());
        // generateMultiblockModel("distiller", ITMultiblockProvider.DISTILLER.block());
        generateMultiblockModel("alternator", ITMultiblockProvider.ALTERNATOR.block());
        generateMultiblockModel("coke_oven_advanced", ITMultiblockProvider.ADV_COKE_OVEN.block());
        generateMultiblockModel("steam_turbine", ITMultiblockProvider.STEAM_TURBINE.block());
        generateMultiblockModel("gas_turbine", ITMultiblockProvider.GAS_TURBINE.block());
        // generateMultiblockModel("solar_tower", ITMultiblockProvider.SOLAR_TOWER.block());
        generateBlockModel(ITBlocks.MetalDevices.COKE_OVEN_PREHEATER);
    }

    private void doTransform(ModelBuilder<?>.TransformsBuilder transform, ItemDisplayContext type, @Nullable Vector3f translation, @Nullable Vector3f rotationAngle, float scale){
        ModelBuilder<?>.TransformsBuilder.TransformVecBuilder trans = transform.transform(type);
        if(translation != null) { trans.translation(translation.x(), translation.y(), translation.z()); }
        if(rotationAngle != null) { trans.rotation(rotationAngle.x(), rotationAngle.y(), rotationAngle.z()); }
        trans.scale(scale);
        trans.end();
    }

    private TRSRModelBuilder obj(Supplier<? extends ItemLike> item, String model){ return obj(item.get(), model); }

    private TRSRModelBuilder obj(ItemLike item, String model){ return getBuilder(name(item)).customLoader(ObjModelBuilder::begin).modelLocation(modLoc("models/" + model)).flipV(true).end(); }

    private String name(ItemLike item){ return Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(item.asItem())).getPath(); }

    private void generateMultiblockModel(String id, Supplier<? extends ItemLike> block) {
        TRSRModelBuilder model = obj(block, "block/multiblock/obj/"+ id + "/" + id + ".obj");

        ModelBuilder<?>.TransformsBuilder trans = model.transforms();
        doTransform(trans, ItemDisplayContext.FIRST_PERSON_LEFT_HAND, new Vector3f(-1.75F, 2.5F, 1.25F), new Vector3f(0, 225, 0), 0.03125F);
        doTransform(trans, ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, new Vector3f(-1.75F, 2.5F, 1.75F), new Vector3f(0, 225, 0), 0.03125F);
        doTransform(trans, ItemDisplayContext.THIRD_PERSON_LEFT_HAND, new Vector3f(-0.75F, 0, -1.25F), new Vector3f(0, 90, 0), 0.03125F);
        doTransform(trans, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, new Vector3f(1.0F, 0, -1.75F), new Vector3f(0, 270, 0), 0.03125F);
        doTransform(trans, ItemDisplayContext.HEAD, new Vector3f(0, 8, -8), null, 0.2F);
        doTransform(trans, ItemDisplayContext.GUI, new Vector3f(6, -6, 0), new Vector3f(30, 225, 0), 0.1875F);
        doTransform(trans, ItemDisplayContext.GROUND, new Vector3f(-1.5F, 3, -1.5F), null, 0.0625F);
        doTransform(trans, ItemDisplayContext.FIXED, new Vector3f(-1, -8, -2), null, 0.0625F);
    }

    private void generateBlockModel(Supplier<? extends ItemLike> block) {
        TRSRModelBuilder model = obj(block, "block/" + "coke_oven_preheater" + ".obj");

        ModelBuilder<?>.TransformsBuilder trans = model.transforms();
        doTransform(trans, ItemDisplayContext.FIRST_PERSON_LEFT_HAND, new Vector3f(-1.75F, 2.5F, 1.25F), new Vector3f(0, 225, 0), 0.03125F);
        doTransform(trans, ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, new Vector3f(-1.75F, 2.5F, 1.75F), new Vector3f(0, 225, 0), 0.03125F);
        doTransform(trans, ItemDisplayContext.THIRD_PERSON_LEFT_HAND, new Vector3f(-0.75F, 0, -1.25F), new Vector3f(0, 90, 0), 0.03125F);
        doTransform(trans, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, new Vector3f(1.0F, 0, -1.75F), new Vector3f(0, 270, 0), 0.03125F);
        doTransform(trans, ItemDisplayContext.HEAD, new Vector3f(0, 8, -8), null, 0.2F);
        doTransform(trans, ItemDisplayContext.GUI, new Vector3f(6, -6, 0), new Vector3f(30, 225, 0), 0.1875F);
        doTransform(trans, ItemDisplayContext.GROUND, new Vector3f(-1.5F, 3, -1.5F), null, 0.0625F);
        doTransform(trans, ItemDisplayContext.FIXED, new Vector3f(-1, -8, -2), null, 0.0625F);
    }
}

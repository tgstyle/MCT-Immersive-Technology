package mctmods.immersivetechnology.common.data.models;

import com.google.common.base.Preconditions;
import com.google.gson.*;
import com.mojang.math.Transformation;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransform.Deserializer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraftforge.common.util.TransformationHelper;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

public class TransformationMap {
    private final Map<ItemDisplayContext, ItemTransform> transforms = new EnumMap<>(ItemDisplayContext.class);

    public static Vector3f toXYZDegrees(Quaternionf q) {
        float iSq = q.x * q.x;
        float jSq = q.y * q.y;
        float kSq = q.z * q.z;
        float angleX = (float)Math.atan2(2 * (q.w * q.x - q.y * q.z), 1 - 2 * (iSq + jSq));
        float sinOfY = 2 * (q.w * q.y + q.x * q.z);
        float angleY = Math.abs(sinOfY) >= 0.999999f ? Math.copySign(Mth.HALF_PI, sinOfY) : (float)Math.asin(sinOfY);
        float angleZ = (float)Math.atan2(2 * (q.w * q.z - q.y * q.x), 1 - 2 * (jSq + kSq));
        Preconditions.checkState(Float.isFinite(angleX) && Float.isFinite(angleY) && Float.isFinite(angleZ), "Invalid quaternion: %s", q);
        Vector3f result = new Vector3f(angleX, angleY, angleZ);
        result.mul(180 / Mth.PI);
        return result;
    }

    @SuppressWarnings("unused")
    public void addFromJson(String json) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Transformation.class, new TransformationHelper.Deserializer())
                .registerTypeAdapter(ItemTransform.class, new ItemTransform.Deserializer())
                .create();
        JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
        Optional<String> type = Optional.ofNullable(obj.remove("type")).map(JsonElement::getAsString);
        boolean vanilla = type.map("vanilla"::equals).orElse(false);
        Map<ItemDisplayContext, Transformation> tempTransforms = new EnumMap<>(ItemDisplayContext.class);
        for (ItemDisplayContext perspective : ItemDisplayContext.values()) {
            String key = perspective.getSerializedName();
            JsonObject forType = obj.getAsJsonObject(key);
            if (forType != null) { obj.remove(key); }
            else {
                key = alternateName(perspective);
                forType = obj.getAsJsonObject(key);
                if (forType != null) { obj.remove(key); }
            }
            Transformation transform;
            if (forType != null) {
                if (vanilla) {
                    ItemTransform vanillaTransform = gson.fromJson(forType, ItemTransform.class);
                    transform = fromItemTransform(vanillaTransform, false);
                } else {
                    transform = readMatrix(forType, gson);
                    if (type.map("no_corner_offset"::equals).orElse(false)) { transform = transform.blockCornerToCenter(); }
                }
            } else { transform = Transformation.identity(); }
            tempTransforms.put(perspective, transform);
        }
        Transformation baseTransform = obj.size() > 0 ? readMatrix(obj, gson) : Transformation.identity();
        for (Entry<ItemDisplayContext, Transformation> e : tempTransforms.entrySet()) {
            Transformation composed = composeForgeLike(e.getValue(), baseTransform);
            if (!composed.isIdentity()) {
                Vector3f translation = new Vector3f(composed.getTranslation());
                translation.mul(16);
                this.transforms.put(e.getKey(), new ItemTransform(
                        toXYZDegrees(composed.getLeftRotation()),
                        translation,
                        composed.getScale(),
                        toXYZDegrees(composed.getRightRotation())
                ));
            }
        }
    }

    private static Transformation composeForgeLike(Transformation a, Transformation b) {
        if (a.isIdentity()) { return b; }
        if (b.isIdentity()) { return a; }
        Matrix4f m = new Matrix4f(a.getMatrix());
        m.mul(b.getMatrix());
        return new Transformation(m);
    }

    private Transformation readMatrix(JsonObject json, Gson gson) {
        if (!json.has("origin")) { json.addProperty("origin", "corner"); }
        return gson.fromJson(json, Transformation.class);
    }

    private String alternateName(ItemDisplayContext type) { return type.name().toLowerCase(Locale.US); }

    public JsonObject toJson() {
        JsonObject ret = new JsonObject();
        for (Entry<ItemDisplayContext, ItemTransform> entry : transforms.entrySet()) { add(ret, entry.getKey(), entry.getValue()); }
        return ret;
    }

    private void add(JsonObject main, ItemDisplayContext type, ItemTransform trsr) {
        JsonObject result = new JsonObject();
        if (!trsr.translation.equals(Deserializer.DEFAULT_TRANSLATION)) { result.add("translation", toJson(trsr.translation)); }
        if (!trsr.rotation.equals(Deserializer.DEFAULT_ROTATION)) { result.add("rotation", toJson(trsr.rotation)); }
        if (!trsr.scale.equals(Deserializer.DEFAULT_SCALE)) { result.add("scale", toJson(trsr.scale)); }
        if (!trsr.rightRotation.equals(Deserializer.DEFAULT_ROTATION)) { result.add("right_rotation", toJson(trsr.rightRotation)); }
        main.add(type.getSerializedName(), result);
    }

    private static JsonArray toJson(Vector3f v) {
        JsonArray ret = new JsonArray();
        ret.add(v.x());
        ret.add(v.y());
        ret.add(v.z());
        return ret;
    }

    private static Transformation fromItemTransform(ItemTransform transform, boolean leftHand) {
        Vector3f translate = new Vector3f(transform.translation);
        if (leftHand) { translate.setComponent(0, -translate.x()); }

        float rx = transform.rotation.x();
        float ry = transform.rotation.y();
        float rz = transform.rotation.z();
        if (leftHand) {
            ry = -ry;
            rz = -rz;
        }
        Quaternionf leftRotation = new Quaternionf().rotateXYZ(Mth.DEG_TO_RAD * rx, Mth.DEG_TO_RAD * ry, Mth.DEG_TO_RAD * rz);

        rx = transform.rightRotation.x();
        ry = transform.rightRotation.y() * (leftHand ? -1.0F : 1.0F);
        rz = transform.rightRotation.z() * (leftHand ? -1.0F : 1.0F);
        Quaternionf rightRotation = new Quaternionf().rotateXYZ(Mth.DEG_TO_RAD * rx, Mth.DEG_TO_RAD * ry, Mth.DEG_TO_RAD * rz);

        return new Transformation(translate, leftRotation, new Vector3f(transform.scale), rightRotation);
    }

}

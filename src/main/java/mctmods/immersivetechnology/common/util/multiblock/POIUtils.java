package mctmods.immersivetechnology.common.util.multiblock;

import blusunrize.immersiveengineering.api.multiblocks.blocks.util.RelativeBlockFace;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import mctmods.immersivetechnology.core.lib.ITLib;

import java.io.InputStream;
import java.io.InputStreamReader;

public class POIUtils {
    public static MultiblockData loadMultiblockData(String multiblockName) {
        MultiblockData data = null;
        try {
            InputStream is = POIUtils.class.getResourceAsStream("/assets/immersivetechnology/multiblocks/" + multiblockName + ".json");
            if (is != null) {
                JsonReader reader = new JsonReader(new InputStreamReader(is));
                Gson gson = new Gson();
                data = gson.fromJson(reader, MultiblockData.class);
                reader.close();
                for (PoIJSONSchema poi : data.pointsOfInterest) {
                    if (poi.facingString != null) { poi.relativeFace = RelativeBlockFace.valueOf(poi.facingString.toUpperCase()); }
                    else { poi.relativeFace = null; }
                }
            } else {ITLib.IT_LOGGER.error("{} JSON resource not found at /assets/immersivetechnology/multiblocks/{}.json", multiblockName, multiblockName); }
        } catch (Exception e) {ITLib.IT_LOGGER.error("Error loading {} from JSON", multiblockName, e); }
        return data;
    }
}

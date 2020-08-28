package mctmods.immersivetechnology.common.util.multiblock;

import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.common.util.ITLogger;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.oredict.OreDictionary;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

public class MultiblockUtils {

    public static MultiblockJSONSchema Load(String path) {
        MultiblockJSONSchema data;
        try {
            InputStreamReader stream = new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(String.format("assets/%s/%s",ImmersiveTechnology.MODID,path)));
            JsonReader reader = new JsonReader(stream);
            try {
                data = new Gson().fromJson(reader, MultiblockJSONSchema.class);
            } catch (JsonSyntaxException i) {
                ITLogger.error(String.format("Syntax error in file %s", path));
                throw i;
            }
        } catch (Exception e) {
            ITLogger.error(String.format("Couldn't load file %s", path));
            return null;
        }
        return data;
    }

    public static IngredientStack[] GetMaterials(MultiblockJSONSchema data) {
        ArrayList<IngredientStack> ingredients = new ArrayList<IngredientStack>();
        for(BlockJSONSchema blockData : data.palette) {
            long count = 0;
            for(String row : data.structure) {
                count += row.chars().filter(ch -> ch == blockData.character).count();
            }
            if(count == 0) continue;
            if(blockData.mod.equals("ore")) ingredients.add(new IngredientStack(blockData.name,(int)count));
            else {
                Item item = Item.getByNameOrId(blockData.mod + ":" + blockData.name);
                if(item == null) {
                    ITLogger.error(String.format("Invalid item %s:%s",blockData.mod, blockData.name));
                    continue;
                }
                ItemStack itemstack = new ItemStack(item, (int)count, blockData.meta);
                ingredients.add(new IngredientStack(itemstack));
            }
        }
        return ingredients.toArray(new IngredientStack[0]);
    }

    public static HashMap<Character, IRefComparable> GetPalette(MultiblockJSONSchema data) {
        HashMap<Character, IRefComparable> palette = new HashMap<Character, IRefComparable>();
        for(BlockJSONSchema blockData : data.palette) {

            if(blockData.mod.equals("ore")) {
                NonNullList<ItemStack> ores = OreDictionary.getOres(blockData.name);
                if(ores.isEmpty()) {
                    ITLogger.error(String.format("Empty oreDictEntry %s", blockData.name));
                    continue;
                }
                palette.put(blockData.character, new OreDictRef(blockData.name));
            }
            else {
                Item item = Item.getByNameOrId(blockData.mod + ":" + blockData.name);
                if(item == null) {
                    ITLogger.error(String.format("Invalid item %s:%s",blockData.mod, blockData.name));
                    continue;
                }
                palette.put(blockData.character, new ItemStackRef(new ItemStack(item, 1, blockData.meta)));
            }
        }
        return palette;
    }

    public static IRefComparable[][][] GetStructure(MultiblockJSONSchema data, int width, int length, int height) {
        HashMap<Character, IRefComparable> palette = GetPalette(data);
        IRefComparable[][][] structure = new IRefComparable[height][length][width];
        for(int rowIndex = 0; rowIndex < data.structure.length; rowIndex++) {
            char[] characters = data.structure[rowIndex].toCharArray();
            for(int x = 0; x < characters.length; x++) {
                if(characters[x] == ' ') {
                    structure[Math.floorDiv(rowIndex, length)][rowIndex % length][x] = AirRef.instance;
                    continue;
                }
                ItemStack itemstack = palette.get(characters[x]).toItemStack();
                if(itemstack == null) throw new IllegalArgumentException(String.format("Invalid palette entry %s", characters[x]));
                structure[Math.floorDiv(rowIndex, length)][rowIndex % length][x] = palette.get(characters[x]);
            }
        }
        return structure;
    }

    public static ItemStack GetItemStack(int position, ItemStack[][][] source) {
        int length = source[0].length;
        int width = source[0][0].length;
        return source   [position / (length * width)]
                        [(position - (position % width)) / width % length]
                        [position % width];
    }

    public static ItemStack[][][] Convert(IRefComparable[][][] source) {
        int height = source.length;
        int length = source[0].length;
        int width = source[0][0].length;
        ItemStack[][][] toReturn = new ItemStack[height][length][width];
        for(int y = 0; y < height; y++) {
            for(int z = 0; z < length; z++) {
                for(int x = 0; x < width; x++) {
                    toReturn[y][z][x] = source[y][z][x].toItemStack();
                }
            }
        }
        return toReturn;
    }

}

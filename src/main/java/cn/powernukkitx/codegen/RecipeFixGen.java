package cn.powernukkitx.codegen;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;

public class RecipeFixGen {
    static Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().setNumberToNumberStrategy(JsonReader::nextInt).setObjectToNumberStrategy(JsonReader::nextInt).create();
    static List<JsonObject> shapelessCrafting;
    static List<JsonObject> shapedCrafting;

    public static void main(String[] args) {
        loadShapelessCraft();
        loadShapeCraft();

        try (var recipe = RecipeFixGen.class.getClassLoader().getResourceAsStream("recipes.json")) {
            Map map = gson.fromJson(new InputStreamReader(recipe), Map.class);
            JsonObject jsonTree = gson.toJsonTree(map).getAsJsonObject();
            JsonArray recipes = jsonTree.getAsJsonArray("recipes");

            //load recipes
            for (var r : recipes) {
                JsonObject obj = r.getAsJsonObject();
                int type = obj.get("type").getAsInt();
                switch (type) {
                    case 0, 5, 8 -> {
                        String block = obj.get("block").toString();
                        fixShapeLessRecipe(obj, block);
                    }
                    case 1 -> {
                        fixShapeLessRecipe(obj);
                    }
                    case 3 -> {
                        String craftingBlock = (String) recipe.get("block");
                        Map<String, Object> resultMap = (Map<String, Object>) recipe.get("output");
                        ItemDescriptor resultItem = parseRecipeItem(resultMap);
                        if (resultItem == null) {
                            yield null;
                        }
                        Map<String, Object> inputMap = (Map<String, Object>) recipe.get("input");
                        ItemDescriptor inputItem = parseRecipeItem(inputMap);
                        if (inputItem == null) {
                            yield null;
                        }
                        Item result = resultItem.toItem();
                        Item input = inputItem.toItem();
                        Recipe furnaceRecipe = switch (craftingBlock) {
                            case "furnace" -> new FurnaceRecipe(result, input);
                            case "blast_furnace" -> new BlastFurnaceRecipe(result, input);
                            case "smoker" -> new SmokerRecipe(result, input);
                            case "campfire" -> new CampfireRecipe(result, input);
                            case "soul_campfire" -> new SoulCampfireRecipe(result, input);
                            default -> throw new IllegalStateException("Unexpected value: " + craftingBlock);
                        };
                        var xp = furnaceXpConfig.getDouble(input.getId() + ":" + input.getDamage());
                        if (xp != 0) {
                            this.setRecipeXp(furnaceRecipe, xp);
                        }
                        yield furnaceRecipe;
                    }
                    default -> throw new IllegalStateException("Unexpected value: " + recipe);
                }
                if (re == null) {
                    if (type != 9) {//todo trim smithing recipe
                        log.warn("Load recipe {} with null!", recipe.toString().substring(0, 60));
                    }
                    continue;
                }
                this.register(re);
            }

            Path path = Path.of("build/recipes.json");
            Files.deleteIfExists(path);
            Files.writeString(path, gson.toJson(jsonTree), StandardCharsets.UTF_8, StandardOpenOption.CREATE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static void fixShapeLessRecipe(JsonObject org, String block) {
        f1:
        for (var b : shapelessCrafting) {
            String asString = b.get("block").getAsString();
            JsonArray a1 = b.get("input").getAsJsonArray();
            JsonArray a2 = org.get("input").getAsJsonArray();
            JsonArray o1 = b.get("output").getAsJsonArray();
            JsonArray o2 = org.get("output").getAsJsonArray();
            if (asString.equals(block) && a1.size() == a2.size() && o1.size() == o2.size()) {
                if (setShapeLessAux(a1, a2)) {
                    continue f1;
                }
                if (setShapeLessAux(o1, o2)) {
                    continue f1;
                }
            }
        }
    }

    static void fixShapeRecipe(JsonObject org, String block) {
        f1:
        for (var b : shapelessCrafting) {
            String asString = b.get("block").getAsString();
            JsonObject a1 = b.get("input").getAsJsonObject();
            JsonObject a2 = org.get("input").getAsJsonObject();
            JsonArray o1 = b.get("output").getAsJsonArray();
            JsonArray o2 = org.get("output").getAsJsonArray();
            if (asString.equals(block) && a1.size() == a2.size() && o1.size() == o2.size()) {
                if (setShapeLessAux(a1, a2)) {
                    continue f1;
                }
                if (setShapeLessAux(o1, o2)) {
                    continue f1;
                }
            }
        }
    }

    private static boolean setShapeAux(JsonObject array1, JsonObject array2) {
        for(var e : array1.entrySet()){
            for(var e2: array2.entrySet()){
                String id1 = e.getValue().getAsJsonObject().get("name").getAsString();
                String id2 = e2.getValue().getAsJsonObject().get("itemId").getAsString();
                if (id1.equals(id2)){
                    JsonObject obj1 = e.getValue().getAsJsonObject();
                    JsonObject obj2 = e2.getValue().getAsJsonObject();
                    if (obj1.asMap().containsKey("meta")) {
                        obj2.addProperty("auxValue", asJsonObject1.get("meta").getAsNumber());
                    }
                    array2.set(i, asJsonObject);
                }
                else {

                }
            }
        }
        return false;
    }

    private static boolean setShapeLessAux(JsonArray array1, JsonArray array2) {
        for (int i = 0; i < array1.size(); i++) {
            String id1 = array1.get(i).getAsJsonObject().get("name").getAsString();
            String id2 = array2.get(i).getAsJsonObject().get("itemId").getAsString();
            if (!id1.equals(id2)) return true;
            else {
                JsonObject asJsonObject = array2.get(i).getAsJsonObject();
                JsonObject asJsonObject1 = array1.get(i).getAsJsonObject();
                if (asJsonObject1.asMap().containsKey("meta")) {
                    asJsonObject.addProperty("auxValue", asJsonObject1.get("meta").getAsNumber());
                }
                array2.set(i, asJsonObject);
            }
        }
        return false;
    }

    static void loadShapelessCraft() {
        try (var recipe = RecipeFixGen.class.getClassLoader().getResourceAsStream("vanilla_recipes/shapeless_crafting.json")) {
            assert recipe != null;
            shapelessCrafting = gson.fromJson(new InputStreamReader(recipe), new TypeToken<List<JsonObject>>() {
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static void loadShapeCraft() {
        try (var recipe = RecipeFixGen.class.getClassLoader().getResourceAsStream("vanilla_recipes/shaped_crafting.json")) {
            assert recipe != null;
            shapedCrafting = gson.fromJson(new InputStreamReader(recipe), new TypeToken<List<JsonObject>>() {
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

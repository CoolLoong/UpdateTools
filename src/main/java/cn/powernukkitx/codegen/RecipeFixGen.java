package cn.powernukkitx.codegen;

import com.google.gson.*;
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
import java.util.Optional;

public class RecipeFixGen {
    static Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().setNumberToNumberStrategy(JsonReader::nextInt).setObjectToNumberStrategy(JsonReader::nextInt).create();
    static List<JsonObject> shapelessCrafting;
    static List<JsonObject> shapedCrafting;
    static List<JsonObject> smeltingCrafting;

    public static void main(String[] args) {
        loadShapelessCraft();
        loadShapeCraft();
        loadSmeltingCrafting();

        try (var recipe = RecipeFixGen.class.getClassLoader().getResourceAsStream("recipes.json")) {
            Map map = gson.fromJson(new InputStreamReader(recipe), Map.class);
            JsonObject jsonTree = gson.toJsonTree(map).getAsJsonObject();
            JsonArray recipes = jsonTree.getAsJsonArray("recipes");

            //load recipes
            for (var r : recipes) {
                JsonObject obj = r.getAsJsonObject();
                int type = obj.get("type").getAsInt();
                JsonElement jsonElement = obj.get("block");
                if (jsonElement != null) {
                    String block = jsonElement.getAsString();
                    switch (type) {
                        case 0, 5 -> fixShapeLessRecipe(obj, block);
                        case 1 -> fixShapeRecipe(obj, block);
                        case 3 -> fixSmeltRecipe(obj, block);
                        default -> {
                        }
                    }
                }
            }

            Path path = Path.of("build/recipes.json");
            Files.deleteIfExists(path);
            String json = gson.toJson(jsonTree);
            Files.writeString(path,json , StandardCharsets.UTF_8, StandardOpenOption.CREATE);
            Files.writeString(Path.of("src/main/resources/recipes.json"), json, StandardCharsets.UTF_8, StandardOpenOption.WRITE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static void fixSmeltRecipe(JsonObject org, String block) {
        f1:
        for (var b : smeltingCrafting) {
            String asString = b.get("block").getAsString();
            JsonObject a1 = b.get("input").getAsJsonObject();
            JsonObject a2 = org.get("input").getAsJsonObject();
            JsonObject o1 = b.get("output").getAsJsonObject();
            JsonObject o2 = org.get("output").getAsJsonObject();
            if (asString.equals(block)) {
                if (setSmeltingAux(a1, a2)) {
                    continue f1;
                }
                org.add("input", a2);
                if (setSmeltingAux(o1, o2)) {
                    continue f1;
                }
                org.add("output", o2);
            }
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
        for (var b : shapedCrafting) {
            String asString = b.get("block").getAsString();
            JsonObject a1 = b.get("input").getAsJsonObject();
            JsonObject a2 = org.get("input").getAsJsonObject();
            JsonArray o1 = b.get("output").getAsJsonArray();
            JsonArray o2 = org.get("output").getAsJsonArray();
            if (asString.equals(block) && a1.size() == a2.size() && o1.size() == o2.size()) {
                if (setShapeAux(a1, a2)) {
                    continue f1;
                }
                if (setShapeLessAux(o1, o2)) {
                    continue f1;
                }
            }
        }
    }

    private static String getElementId(JsonObject element) {
        JsonElement jsonElement = element.get("itemId");
        if (jsonElement == null) {
            jsonElement = element.get("name");
        }
        if (jsonElement == null) {
            jsonElement = element.get("id");
        }
        return Optional.ofNullable(jsonElement).map(JsonElement::getAsString).orElse(null);
    }

    private static boolean setSmeltingAux(JsonObject array1, JsonObject array2) {
        String id1 = array1.getAsJsonObject().get("name").getAsString();
        String id2 = getElementId(array2.getAsJsonObject());
        if (id2 == null) return true;
        if (!id1.equals(id2)) {
            return true;
        } else {
            if (array1.asMap().containsKey("meta") && array2.asMap().containsKey("damage")) {
                array2.addProperty("damage", array1.get("meta").getAsNumber());
            }

            if (array1.asMap().containsKey("block_states")) {
                array2.addProperty("block_states", array1.get("block_states").getAsString());
            }
        }
        return false;
    }

    private static boolean setShapeAux(JsonObject array1, JsonObject array2) {
        for (var e : array1.entrySet()) {
            JsonElement jsonElement = array2.get(e.getKey());
            if (jsonElement != null) {
                JsonObject o1 = e.getValue().getAsJsonObject();
                if (o1.get("tag") != null) return true;
                JsonObject o2 = jsonElement.getAsJsonObject();
                String id1 = o1.getAsJsonObject().get("name").getAsString();
                String id2 = getElementId(o2.getAsJsonObject());
                if (id2 == null) return true;
                if (!id1.equals(id2)) {
                    return true;
                } else {
                    if (o1.asMap().containsKey("meta") && array2.asMap().containsKey("auxValue")) {
                        o2.addProperty("auxValue", o1.get("meta").getAsNumber());
                    }
                    if (o1.asMap().containsKey("block_states")) {
                        o2.addProperty("block_states", o1.get("block_states").getAsString());
                    }
                    array2.add(e.getKey(), o2);
                }
            }
        }
        return false;
    }

    private static boolean setShapeLessAux(JsonArray array1, JsonArray array2) {
        for (int i = 0; i < array1.size(); i++) {
            if (array1.get(i).getAsJsonObject().get("tag") != null) return true;
            String id1 = array1.get(i).getAsJsonObject().get("name").getAsString();
            String id2 = getElementId(array2.get(i).getAsJsonObject());
            if (id2 == null) return true;
            if (!id1.equals(id2)) return true;
            else {
                JsonObject asJsonObject = array2.get(i).getAsJsonObject();
                JsonObject asJsonObject1 = array1.get(i).getAsJsonObject();
                if (asJsonObject1.asMap().containsKey("meta") && asJsonObject.asMap().containsKey("auxValue")) {
                    asJsonObject.addProperty("auxValue", asJsonObject1.get("meta").getAsNumber());
                }
                if (asJsonObject1.asMap().containsKey("block_states")) {
                    asJsonObject.addProperty("block_states", asJsonObject1.get("block_states").getAsString());
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

    static void loadSmeltingCrafting() {
        try (var recipe = RecipeFixGen.class.getClassLoader().getResourceAsStream("vanilla_recipes/smelting.json")) {
            assert recipe != null;
            smeltingCrafting = gson.fromJson(new InputStreamReader(recipe), new TypeToken<List<JsonObject>>() {
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

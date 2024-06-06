package cn.powernukkitx.codegen;

import cn.powernukkitx.codegen.util.DownloadUtil;
import com.google.gson.Gson;

import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Allay Project 12/18/2023
 *
 * @author Cool_Loong
 */
public class DownloadResource {
    public static void main(String[] args) throws IOException {
        DownloadUtil.download("https://github.com/CloudburstMC/Data/raw/master/block_palette.nbt",
                "src/main/resources/block_palette.nbt");
        DownloadUtil.download("https://github.com/CloudburstMC/Data/raw/master/biome_definitions.dat",
                "src/main/resources/biome_definitions.nbt");
        DownloadUtil.download("https://github.com/CloudburstMC/Data/raw/master/entity_identifiers.dat",
                "src/main/resources/entity_identifiers.nbt");
        DownloadUtil.download("https://github.com/CloudburstMC/Data/raw/master/runtime_item_states.json",
                "src/main/resources/runtime_item_states.json");
        DownloadUtil.download("https://github.com/CloudburstMC/Data/raw/master/creative_items.json",
                "src/main/resources/creative_items.json");
        
        DownloadUtil.download("https://github.com/pmmp/BedrockData/raw/master/recipes/potion_type.json",
                "src/main/resources/vanilla_recipes/potion_type.json");
        DownloadUtil.download("https://github.com/pmmp/BedrockData/raw/master/recipes/shaped_crafting.json",
                "src/main/resources/vanilla_recipes/shaped_crafting.json");
        DownloadUtil.download("https://github.com/pmmp/BedrockData/raw/master/recipes/shapeless_crafting.json",
                "src/main/resources/vanilla_recipes/shapeless_crafting.json");
        DownloadUtil.download("https://github.com/pmmp/BedrockData/raw/master/recipes/smithing.json",
                "src/main/resources/vanilla_recipes/smithing.json");
        DownloadUtil.download("https://github.com/pmmp/BedrockData/raw/master/recipes/shapeless_shulker_box.json",
                "src/main/resources/vanilla_recipes/shapeless_shulker_box.json");
        DownloadUtil.download("https://github.com/pmmp/BedrockData/raw/master/recipes/smelting.json",
                "src/main/resources/vanilla_recipes/smelting.json");
        DownloadUtil.download("https://github.com/pmmp/BedrockData/raw/master/recipes/special_hardcoded.json",
                "src/main/resources/vanilla_recipes/special_hardcoded.json");

        updateRecipes();
    }

    private static void updateRecipes() throws IOException {
        Path p = Path.of("src/main/resources/vanilla_recipes/shaped_crafting.json");
        //todo These are wrong recipes, their `result` should not have `data`, if this bug is fixed, please remove it
        Map<String, String> errorRecipes = Map.of(
                "minecraft:dispenser", "CgAAAxAAZmFjaW5nX2RpcmVjdGlvbgAAAAABDQB0cmlnZ2VyZWRfYml0AAA=",
                "minecraft:dropper", "CgAAAxAAZmFjaW5nX2RpcmVjdGlvbgAAAAABDQB0cmlnZ2VyZWRfYml0AAA=",
                "minecraft:piston", "CgAAAxAAZmFjaW5nX2RpcmVjdGlvbgAAAAAA",
                "minecraft:sticky_piston", "CgAAAxAAZmFjaW5nX2RpcmVjdGlvbgAAAAAA"
        );
        Gson gson = new Gson();
        List<Map<String, Object>> data = gson.fromJson(new FileReader(p.toFile()), List.class);
        for (var fix : data) {
            List<Map<String, Object>> output = (List<Map<String, Object>>) fix.get("output");
            Map<String, Object> o = output.get(0);
            var name = o.get("name").toString();
            if (errorRecipes.containsKey(name)) {
                o.put("block_states", errorRecipes.get(name));
            }
        }
        Files.writeString(p, gson.toJson(data), StandardCharsets.UTF_8);
    }
}

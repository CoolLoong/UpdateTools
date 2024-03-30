package cn.powernukkitx.codegen;

import cn.powernukkitx.codegen.util.DownloadUtil;

/**
 * Allay Project 12/18/2023
 *
 * @author Cool_Loong
 */
public class DownloadResource {
    static final String version = "1_20_70";

    public static void main(String[] args) {
        DownloadUtil.download("https://github.com/CloudburstMC/Data/raw/master/block_palette.nbt",
                "src/main/resources/block_palette.nbt");
        DownloadUtil.download("https://github.com/CloudburstMC/Data/raw/master/biome_definitions.dat",
                "src/main/resources/biome_definitions.nbt");
        DownloadUtil.download("https://github.com/CloudburstMC/Data/raw/master/entity_identifiers.dat",
                "src/main/resources/entity_identifiers.nbt");
        DownloadUtil.download("https://github.com/CloudburstMC/Data/raw/master/runtime_item_states.json",
                "src/main/resources/runtime_item_states.json");
        DownloadUtil.download("https://github.com/GeyserMC/Geyser/raw/master/core/src/main/resources/bedrock/creative_items.%s.json".formatted(version),
                "src/main/resources/creative_items.json");
    }
}

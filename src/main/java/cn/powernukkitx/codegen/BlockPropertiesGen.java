package cn.powernukkitx.codegen;

import cn.powernukkitx.codegen.util.DownloadUtil;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.nbt.NbtUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class BlockPropertiesGen {
    public static void main(String[] args) throws IOException {
        File file = new File("src/main/resources/block_attributes.nbt");
        InputStream stream;
        if (file.exists()) {
            stream = new FileInputStream(file);
        } else {
            stream = DownloadUtil.downloadAsStream("https://github.com/AllayMC/BedrockData/raw/main/%s/block_attributes.nbt".formatted(args[0]));
        }
        try (var reader = NbtUtils.createGZIPReader(stream)) {
            NbtMap nbtMap = (NbtMap) reader.readTag();
            Path path = Path.of("build/block_attributes.txt");
            Files.deleteIfExists(path);
            Files.writeString(path, nbtMap.toSNBT(4), StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
        }
    }
}

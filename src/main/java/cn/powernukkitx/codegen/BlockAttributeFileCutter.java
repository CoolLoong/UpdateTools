package cn.powernukkitx.codegen;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.SneakyThrows;
import org.cloudburstmc.nbt.NBTInputStream;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.nbt.NbtType;
import org.cloudburstmc.nbt.NbtUtils;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

/**
 * Author: daoge_cmd <br>
 * Date: 2023/5/27 <br>
 * PowerNukkitX Project <br>
 */
public class BlockAttributeFileCutter {
    static final Path OUTPUT = Path.of("./target/block_color.json");

    @SneakyThrows
    public static void main(String[] args) {
        File file = new File("src/main/resources/block_attributes.nbt");
        NBTInputStream gzipReader = NbtUtils.createGZIPReader(new FileInputStream(file));
        var nbt = (NbtMap) gzipReader.readTag();
        var ext = new HashMap<Integer, Color>();
        for (var block : nbt.getList("block", NbtType.COMPOUND)) {
            var hash = block.getInt("blockStateHash");
            var color = block.getCompound("color");
            var r = color.getInt("r");
            var g = color.getInt("g");
            var b = color.getInt("b");
            var a = block.getFloat("translucency") >= 1 ? 0 : 255;
            ext.put(hash, new Color(r, g, b, a));
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        if (!Files.exists(OUTPUT))
            Files.createFile(OUTPUT);
        Files.writeString(OUTPUT, gson.toJson(ext));
        System.out.println("OK!");
    }

    record Color(int r, int g, int b, int a) {
    }
}

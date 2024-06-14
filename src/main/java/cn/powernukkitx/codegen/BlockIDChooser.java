package cn.powernukkitx.codegen;

import cn.powernukkitx.codegen.util.Identifier;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.nbt.NbtType;
import org.cloudburstmc.nbt.NbtUtils;

import java.io.*;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Predicate;

public class BlockIDChooser {
    private static final TreeMap<String, NbtMap> BLOCKID = new TreeMap<>();

    public static void main(String[] args) throws FileNotFoundException {
        File file = new File("src/main/resources/block_palette.nbt");
        InputStream stream = new FileInputStream(file);
        try (stream) {
            NbtMap reader = (NbtMap) NbtUtils.createGZIPReader(stream).readTag();
            reader.getList("blocks", NbtType.COMPOUND).forEach((block) -> {
                BLOCKID.put(block.getString("name"), block);
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Set<String> stringSet = new LinkedHashSet<>();
        for (var s : BLOCKID.keySet()) {
            if (predicate().test(s)) {
                Identifier identifier = new Identifier(s);
                stringSet.add(identifier.path().toUpperCase());
                System.out.println(identifier.path().toUpperCase());
            }
        }
        System.out.println("[%s]".formatted(String.join(", ", stringSet)));
    }

    static Predicate<String> predicate() {
        return (s) -> s.endsWith("slab") && !s.contains("double");
    }
}

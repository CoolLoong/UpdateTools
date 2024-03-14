package cn.powernukkitx.codegen;

import cn.powernukkitx.codegen.util.DownloadUtil;
import cn.powernukkitx.codegen.util.Identifier;
import cn.powernukkitx.codegen.util.StringUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import lombok.SneakyThrows;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.nbt.NbtType;
import org.cloudburstmc.nbt.NbtUtils;

import javax.lang.model.element.Modifier;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * CodeGen about ItemID and runtime_item_states.json
 */
public class ItemGen {
    private static final String JAVA_DOC = """
            @author Cool_Loong
            """;
    private static final Map<String, Map<String, Object>> ITEM_ID = new TreeMap<>();
    private static final TreeMap<String, NbtMap> BLOCKID = new TreeMap<>();

    public static void main(String[] args) throws FileNotFoundException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        File file1 = new File("src/main/resources/block_palette.nbt");
        InputStream stream1 = new FileInputStream(file1);
        try (stream1) {
            NbtMap reader = (NbtMap) NbtUtils.createGZIPReader(stream1).readTag();
            reader.getList("blocks", NbtType.COMPOUND).forEach((block) -> {
                BLOCKID.put(block.getString("name"), block);
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        File file2 = new File("src/main/resources/runtime_item_states.json");
        InputStream stream2 = new FileInputStream(file2);
        try (stream2) {
            List<Map<String, Object>> map = gson.fromJson(new InputStreamReader(stream2), List.class);
            map.stream().filter(v -> !v.get("name").toString().contains("item.") && !BLOCKID.containsKey(v.get("name").toString()))
                    .forEach(item -> ITEM_ID.put(item.get("name").toString(), item));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        generateItemID();
        generateItemRegisterBlock();
        generateItemClass();
        System.out.println("OK!");
    }

    @SneakyThrows
    public static void generateItemID() {
        TypeSpec.Builder codeBuilder = TypeSpec.interfaceBuilder("ItemID")
                .addJavadoc(JAVA_DOC)
                .addModifiers(Modifier.PUBLIC);
        for (var entry : ITEM_ID.entrySet()) {
            var split = StringUtil.fastTwoPartSplit(
                    StringUtil.fastTwoPartSplit(entry.getKey(), ":", "")[1],
                    ".", "");
            var valueName = split[0].isBlank() ? split[1].toUpperCase() : split[0].toUpperCase() + "_" + split[1].toUpperCase();
            codeBuilder.addField(
                    FieldSpec.builder(String.class, valueName, Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                            .initializer("$S", entry.getKey()).build()
            );
        }
        var javaFile = JavaFile.builder("", codeBuilder.build()).build();
        javaFile.writeToPath(Path.of("build"));
    }

    @SneakyThrows
    public static void generateItemRegisterBlock() {
        List<String> result = new ArrayList<>();
        for (var k : ITEM_ID.keySet()) {
            String template = "register(%s, Item%s.class);";
            Identifier identifier = new Identifier(k);
            result.add(template.formatted(identifier.path().toUpperCase(), convertToCamelCase(identifier.path())));
        }
        Path path = Path.of("build/item_init_block.txt");
        Files.deleteIfExists(path);
        Files.writeString(path, String.join("\n", result), StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
    }

    @SneakyThrows
    public static void generateItemClass() {
        File file = new File("build/itemclasses/");
        if (!file.exists()) {
            file.mkdir();
        }
        String template = """
                package cn.nukkit.item;
                
                public class %s extends Item {
                     public %s() {
                         super(%s);
                     }
                }""";
        for (var k : ITEM_ID.keySet()) {
            Identifier identifier = new Identifier(k);
            String classNameFile = "Item%s.java".formatted(convertToCamelCase(identifier.path()));
            Path path = Path.of("build/itemclasses").resolve(classNameFile);
            Files.deleteIfExists(path);
            String className = classNameFile.replace(".java", "");
            String result = template.formatted(className, className, identifier.path().toUpperCase());
            Files.writeString(path, result, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
        }
    }

    public static String convertToCamelCase(String input) {
        StringBuilder result = new StringBuilder();
        boolean makeUpperCase = true;

        for (char character : input.toCharArray()) {
            if (character == '_') {
                makeUpperCase = true;
            } else {
                if (makeUpperCase) {
                    result.append(Character.toUpperCase(character));
                    makeUpperCase = false;
                } else {
                    result.append(character);
                }
            }
        }

        return result.toString();
    }

    record ItemEntry(String name, int id) {
    }
}

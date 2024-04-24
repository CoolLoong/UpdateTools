package cn.powernukkitx.codegen;

import cn.powernukkitx.codegen.util.Identifier;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
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
 * Allay Project 2023/3/26
 *
 * @author daoge_cmd | Cool_Loong
 */
public class BlockIDGen {
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

        File file2 = new File("src/main/resources/block_property_types.json");
        InputStream stream2 = new FileInputStream(file2);
        try (stream2) {
            BLOCK_PROPERTY_INFO = GSON.toJsonTree(GSON.fromJson(new InputStreamReader(stream2), Map.class)).getAsJsonObject();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        generateBlockID();
        generateBlockRegisterCodeBlock();
        generateBlockClass();
        System.out.println("OK!");
    }

    private static final Gson GSON = new Gson();
    private static JsonObject BLOCK_PROPERTY_INFO;
    private static final TreeMap<String, NbtMap> BLOCKID = new TreeMap<>();
    private static final String JAVA_DOC = """
            @author Cool_Loong
            """;

    @SneakyThrows
    public static void generateBlockID() {
        TypeSpec.Builder codeBuilder = TypeSpec.interfaceBuilder("BlockID")
                .addJavadoc(JAVA_DOC)
                .addModifiers(Modifier.PUBLIC);
        for (var identifier : BLOCKID.keySet()) {
            codeBuilder.addField(FieldSpec
                    .builder(String.class, new Identifier(identifier).path().toUpperCase(), Modifier.FINAL, Modifier.PUBLIC, Modifier.STATIC)
                    .initializer("$S", identifier)
                    .build());
        }
        var javaFile = JavaFile.builder("org.allaymc.dependence", codeBuilder.build()).build();
        Path path = Path.of("build/BlockID.java");
        Files.deleteIfExists(path);
        Files.writeString(path, javaFile.toString(), StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
    }

    @SneakyThrows
    public static void generateBlockRegisterCodeBlock() {
        List<String> result = new ArrayList<>();
        for (var k : BLOCKID.keySet()) {
            String template = "register(%s, Block%s.class);";
            Identifier identifier = new Identifier(k);
            result.add(template.formatted(identifier.path().toUpperCase(), convertToCamelCase(identifier.path())));
        }
        Path path = Path.of("build/block_init_block.txt");
        Files.deleteIfExists(path);
        Files.writeString(path, String.join("\n", result), StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
    }

    @SneakyThrows
    public static void generateBlockClass() {
        File file = new File("build/blockclasses/");
        if (!file.exists()) {
            file.mkdir();
        }
        String template = """
                package cn.nukkit.block;
                                
                import cn.nukkit.block.property.CommonBlockProperties;
                import org.jetbrains.annotations.NotNull;
                                
                public class %s extends Block {
                     public static final BlockProperties PROPERTIES = new BlockProperties(%s);
                     
                     @Override
                     @NotNull
                     public BlockProperties getProperties() {
                        return PROPERTIES;
                     }
                     
                     public %s() {
                         super(PROPERTIES.getDefaultState());
                     }
                     
                     public %s(BlockState blockstate) {
                         super(blockstate);
                     }
                }""";
        for (var entry : BLOCKID.entrySet()) {
            Identifier identifier = new Identifier(entry.getKey());
            String classNameFile = "Block%s.java".formatted(convertToCamelCase(identifier.path()));
            Path path = Path.of("build/blockclasses").resolve(classNameFile);
            Files.deleteIfExists(path);
            String className = classNameFile.replace(".java", "");

            Map<String, Object> specialBlockTypes = GSON.fromJson(BLOCK_PROPERTY_INFO.get("specialBlockTypes"), Map.class);
            StringBuilder builder = new StringBuilder(identifier.path().toUpperCase());
            List<Map.Entry<String, Object>> states = entry.getValue().getCompound("states").entrySet().stream().toList();
            if (!states.isEmpty()) builder.append(", ");
            for (int i = 0; i < states.size(); i++) {
                Map.Entry<String, Object> e = states.get(i);
                String propertyTmp = e.getKey().startsWith("minecraft:") ? e.getKey().replace("minecraft:", "MINECRAFT_") : e.getKey();
                String property = propertyTmp.toUpperCase();
                if (specialBlockTypes.containsKey(identifier.toString())) {
                    Map<String, Object> map = (Map<String, Object>) specialBlockTypes.get(identifier.toString());
                    if (map.containsKey(e.getKey())) {
                        builder.append("CommonBlockProperties.").append(map.get(e.getKey()).toString().toUpperCase());
                    } else {
                        builder.append("CommonBlockProperties.").append(property);
                    }
                } else {
                    builder.append("CommonBlockProperties.").append(property);
                }
                if (i != states.size() - 1) {
                    builder.append(", ");
                }
            }
            String result = template.formatted(className, builder.toString(), className, className);
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
}

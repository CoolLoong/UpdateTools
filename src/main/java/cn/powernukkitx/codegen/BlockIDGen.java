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
import java.util.*;

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
            if (skipBlockSet.contains(identifier.toString())) continue;
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

    public static final Set<String> skipBlockSet = Set.of(
            "minecraft:camera",
            "minecraft:chemical_heat",
            "minecraft:chemistry_table",
            "minecraft:colored_torch_bp",
            "minecraft:colored_torch_rg",
            "minecraft:element_0",
            "minecraft:element_1",
            "minecraft:element_10",
            "minecraft:element_100",
            "minecraft:element_101",
            "minecraft:element_102",
            "minecraft:element_103",
            "minecraft:element_104",
            "minecraft:element_105",
            "minecraft:element_106",
            "minecraft:element_107",
            "minecraft:element_108",
            "minecraft:element_109",
            "minecraft:element_11",
            "minecraft:element_110",
            "minecraft:element_111",
            "minecraft:element_112",
            "minecraft:element_113",
            "minecraft:element_114",
            "minecraft:element_115",
            "minecraft:element_116",
            "minecraft:element_117",
            "minecraft:element_118",
            "minecraft:element_12",
            "minecraft:element_13",
            "minecraft:element_14",
            "minecraft:element_15",
            "minecraft:element_16",
            "minecraft:element_17",
            "minecraft:element_18",
            "minecraft:element_19",
            "minecraft:element_2",
            "minecraft:element_20",
            "minecraft:element_21",
            "minecraft:element_22",
            "minecraft:element_23",
            "minecraft:element_24",
            "minecraft:element_25",
            "minecraft:element_26",
            "minecraft:element_27",
            "minecraft:element_28",
            "minecraft:element_29",
            "minecraft:element_3",
            "minecraft:element_30",
            "minecraft:element_31",
            "minecraft:element_32",
            "minecraft:element_33",
            "minecraft:element_34",
            "minecraft:element_35",
            "minecraft:element_36",
            "minecraft:element_37",
            "minecraft:element_38",
            "minecraft:element_39",
            "minecraft:element_4",
            "minecraft:element_40",
            "minecraft:element_41",
            "minecraft:element_42",
            "minecraft:element_43",
            "minecraft:element_44",
            "minecraft:element_45",
            "minecraft:element_46",
            "minecraft:element_47",
            "minecraft:element_48",
            "minecraft:element_49",
            "minecraft:element_5",
            "minecraft:element_50",
            "minecraft:element_51",
            "minecraft:element_52",
            "minecraft:element_53",
            "minecraft:element_54",
            "minecraft:element_55",
            "minecraft:element_56",
            "minecraft:element_57",
            "minecraft:element_58",
            "minecraft:element_59",
            "minecraft:element_6",
            "minecraft:element_60",
            "minecraft:element_61",
            "minecraft:element_62",
            "minecraft:element_63",
            "minecraft:element_64",
            "minecraft:element_65",
            "minecraft:element_66",
            "minecraft:element_67",
            "minecraft:element_68",
            "minecraft:element_69",
            "minecraft:element_7",
            "minecraft:element_70",
            "minecraft:element_71",
            "minecraft:element_72",
            "minecraft:element_73",
            "minecraft:element_74",
            "minecraft:element_75",
            "minecraft:element_76",
            "minecraft:element_77",
            "minecraft:element_78",
            "minecraft:element_79",
            "minecraft:element_8",
            "minecraft:element_80",
            "minecraft:element_81",
            "minecraft:element_82",
            "minecraft:element_83",
            "minecraft:element_84",
            "minecraft:element_85",
            "minecraft:element_86",
            "minecraft:element_87",
            "minecraft:element_88",
            "minecraft:element_89",
            "minecraft:element_9",
            "minecraft:element_90",
            "minecraft:element_91",
            "minecraft:element_92",
            "minecraft:element_93",
            "minecraft:element_94",
            "minecraft:element_95",
            "minecraft:element_96",
            "minecraft:element_97",
            "minecraft:element_98",
            "minecraft:element_99",
            "minecraft:hard_black_stained_glass",
            "minecraft:hard_black_stained_glass_pane",
            "minecraft:hard_blue_stained_glass",
            "minecraft:hard_blue_stained_glass_pane",
            "minecraft:hard_brown_stained_glass",
            "minecraft:hard_brown_stained_glass_pane",
            "minecraft:hard_cyan_stained_glass",
            "minecraft:hard_cyan_stained_glass_pane",
            "minecraft:hard_glass",
            "minecraft:hard_glass_pane",
            "minecraft:hard_gray_stained_glass",
            "minecraft:hard_gray_stained_glass_pane",
            "minecraft:hard_green_stained_glass",
            "minecraft:hard_green_stained_glass_pane",
            "minecraft:hard_light_blue_stained_glass",
            "minecraft:hard_light_blue_stained_glass_pane",
            "minecraft:hard_light_gray_stained_glass",
            "minecraft:hard_light_gray_stained_glass_pane",
            "minecraft:hard_lime_stained_glass",
            "minecraft:hard_lime_stained_glass_pane",
            "minecraft:hard_magenta_stained_glass",
            "minecraft:hard_magenta_stained_glass_pane",
            "minecraft:hard_orange_stained_glass",
            "minecraft:hard_orange_stained_glass_pane",
            "minecraft:hard_pink_stained_glass",
            "minecraft:hard_pink_stained_glass_pane",
            "minecraft:hard_purple_stained_glass",
            "minecraft:hard_purple_stained_glass_pane",
            "minecraft:hard_red_stained_glass",
            "minecraft:hard_red_stained_glass_pane",
            "minecraft:hard_white_stained_glass",
            "minecraft:hard_white_stained_glass_pane",
            "minecraft:hard_yellow_stained_glass",
            "minecraft:hard_yellow_stained_glass_pane",
            "minecraft:underwater_torch"
    );
}

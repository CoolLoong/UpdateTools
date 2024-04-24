package cn.powernukkitx.codegen;

import cn.powernukkitx.codegen.util.DownloadUtil;
import cn.powernukkitx.codegen.util.Identifier;
import cn.powernukkitx.codegen.util.StringUtil;
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
import java.util.TreeMap;

import static cn.powernukkitx.codegen.ItemGen.convertToCamelCase;

public class EntityGen {
    private static TreeMap<Integer, NbtMap> idlist;

    public static void main(String[] args) throws FileNotFoundException {
        File file = new File("src/main/resources/entity_identifiers.nbt");
        InputStream stream;
        if (file.exists()) {
            stream = new FileInputStream(file);
        } else {
            stream = DownloadUtil.downloadAsStream("https://github.com/AllayMC/BedrockData/raw/main/%s/entity_identifiers.nbt".formatted(args[0]));
        }
        try (stream) {
            NbtMap reader = (NbtMap) NbtUtils.createNetworkReader(stream).readTag();
            idlist = new TreeMap<>();
            reader.getList("idlist", NbtType.COMPOUND).forEach(n -> idlist.put(n.getInt("rid"), n));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        generateEntityID();
        generateEntityRegisterCodeBlock();
        System.out.println("OK!");
    }

    @SneakyThrows
    public static void generateEntityID() {
        TypeSpec.Builder codeBuilder = TypeSpec.interfaceBuilder("EntityID")
                .addModifiers(Modifier.PUBLIC);
        for (var entry : idlist.values()) {
            var split = StringUtil.fastTwoPartSplit(
                    StringUtil.fastTwoPartSplit(entry.getString("id"), ":", "")[1],
                    ".", "");
            var valueName = split[0].isBlank() ? split[1].toUpperCase() : split[0].toUpperCase() + "_" + split[1].toUpperCase();
            codeBuilder.addField(
                    FieldSpec.builder(String.class, valueName, Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                            .initializer("$S", entry.getString("id")).build()
            );
        }
        var javaFile = JavaFile.builder("", codeBuilder.build()).build();
        javaFile.writeToPath(Path.of("build"));
    }

    @SneakyThrows
    public static void generateEntityRegisterCodeBlock() {
        List<String> result = new ArrayList<>();
        for (var k : idlist.values()) {
            String template = "register(new EntityDefinition(%s,\"%s\",%s,%s,%s), Entity%s.class);";
            Identifier identifier = new Identifier(k.getString("id"));
            result.add(template.formatted(identifier.path().toUpperCase(), k.getString("bid"), k.getInt("rid"), k.getBoolean("hasspawnegg"),
                    k.getBoolean("summonable"),
                    convertToCamelCase(identifier.path())));
        }
        Path path = Path.of("build/entity_init_block.txt");
        Files.deleteIfExists(path);
        Files.writeString(path, String.join("\n", result), StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
    }
}

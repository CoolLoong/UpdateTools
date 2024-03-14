package cn.powernukkitx.codegen;

import cn.powernukkitx.codegen.util.DownloadUtil;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import lombok.SneakyThrows;

import javax.lang.model.element.Modifier;
import java.io.*;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;

public class BiomeGen {
    private static Map<String, JsonElement> biomeMap;

    public static void main(String[] args) throws IOException {
        File file2 = new File("src/main/resources/biome_id_and_type.json");
        InputStream stream2;
        if (file2.exists()) {
            stream2 = new FileInputStream(file2);
        } else {
            stream2 = DownloadUtil.downloadAsStream("https://github.com/AllayMC/BedrockData/raw/main/%s/biome_id_and_type.json".formatted(args[0]));
        }
        var t = (JsonObject) new Gson().toJsonTree(new Gson().fromJson(new InputStreamReader(stream2), Map.class));
        stream2.close();
        biomeMap = t.asMap();
        generateBiomeID();
        System.out.println("OK!");
    }

    @SneakyThrows
    public static void generateBiomeID() {
        TypeSpec.Builder codeBuilder = TypeSpec.interfaceBuilder("BiomeID")
                .addModifiers(Modifier.PUBLIC);
        TreeMap<Integer, String> integerStringTreeMap = new TreeMap<>();
        for (var entry : biomeMap.entrySet()) {
            var valueName = entry.getKey().toUpperCase();
            integerStringTreeMap.put(entry.getValue().getAsJsonObject().get("id").getAsInt(), valueName);
        }
        for (var e : integerStringTreeMap.entrySet()) {
            codeBuilder.addField(
                    FieldSpec.builder(int.class, e.getValue(), Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                            .initializer("$L", e.getKey()).build()
            );
        }
        var javaFile = JavaFile.builder("", codeBuilder.build()).build();
        javaFile.writeToPath(Path.of("target"));
    }
}

package cn.powernukkitx.codegen;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

public class BdsLangExport {
    static final String TARGET = "C:\\Users\\admin\\Downloads\\bedrock-server-1.20.51.01\\resource_packs\\vanilla\\texts";
    static Pattern pattern = Pattern.compile("%[0-9]");

    public static void main(String[] args) throws IOException, URISyntaxException {
        var langs = new File("src/main/resources/language").listFiles();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        for (var file : Objects.requireNonNull(langs)) {
            if (file.getName().length() == 3 && pathMapping(file.getName(), TARGET) != null) {
                var path = pathMapping(file.getName(), TARGET);
                if (!path.toFile().exists()) continue;
                Path output = file.toPath().resolve("lang.json");
                TreeMap<String, String> olds;
                try (FileReader fileReader = new FileReader(output.toFile())) {
                    olds = new TreeMap<>(gson.fromJson(fileReader, Map.class));
                }
                TreeMap<String, String> news = new TreeMap<>();
                for (var line : Files.readAllLines(path)) {//bds lang
                    if (line.startsWith("commands")) {
                        line = line.replaceAll("\\$.", "").transform(s -> {
                            AtomicInteger ds = new AtomicInteger();
                            s = pattern.matcher(s).replaceAll(matchResult -> {
                                int number = Integer.parseInt(String.valueOf(matchResult.group().charAt(1)));
                                ds.getAndIncrement();
                                return "{%" + (number - 1) + "}";
                            });
                            var array = s.toCharArray();
                            StringBuilder builder = new StringBuilder(String.valueOf(array[0]));
                            for (int i = 1; i < array.length; ) {
                                if (array[i] == '%' && array[i - 1] != '{') {
                                    char c = array[i + 1];
                                    if (c == 's' || c == 'd') {
                                        builder.append('{').append('%').append(ds.getAndIncrement()).append('}');
                                        i += 2;
                                    } else if (c == '.') {
                                        builder.append('{').append('%').append(ds.getAndIncrement()).append('}');
                                        i += 4;
                                    } else {
                                        i++;
                                    }
                                } else {
                                    builder.append(array[i]);
                                    i++;
                                }
                            }
                            var str = builder.toString().replace("\u00A0", " ");
                            var last = str.indexOf('#');
                            return str.substring(0, last == -1 ? str.length() : last);
                        });
                        String[] split = line.trim().split("=");
                        news.put(split[0], split[1]);
                    }
                }
                olds.putAll(news);
                Files.delete(output);
                Files.writeString(output, gson.toJson(olds), StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
            }
        }
        System.out.println("success update lang");
    }

    public static Path pathMapping(String fileName, String targetFile) {
        return switch (fileName) {
            case "bra" -> Path.of(targetFile).resolve("pt_BR.lang");
            case "chs" -> Path.of(targetFile).resolve("zh_CN.lang");
            case "cht" -> Path.of(targetFile).resolve("zh_TW.lang");
            case "cze" -> Path.of(targetFile).resolve("cs_CZ.lang");
            case "deu" -> Path.of(targetFile).resolve("de_DE.lang");
            case "fin" -> Path.of(targetFile).resolve("fi_FI.lang");
            case "eng" -> Path.of(targetFile).resolve("en_US.lang");
            case "fra" -> Path.of(targetFile).resolve("fr_FR.lang");
            case "idn" -> Path.of(targetFile).resolve("id_ID.lang");
            case "jpn" -> Path.of(targetFile).resolve("ja_JP.lang");
            case "kor" -> Path.of(targetFile).resolve("ko_KR.lang");
            case "ltu" -> Path.of(targetFile).resolve("en_US.lang");
            case "pol" -> Path.of(targetFile).resolve("pl_PL.lang");
            case "rus" -> Path.of(targetFile).resolve("ru_RU.lang");
            case "spa" -> Path.of(targetFile).resolve("es_ES.lang");
            case "tur" -> Path.of(targetFile).resolve("tr_TR.lang");
            case "ukr" -> Path.of(targetFile).resolve("uk_UA.lang");
            case "vie" -> Path.of(targetFile).resolve("en_US.lang");
            default -> null;
        };
    }
}

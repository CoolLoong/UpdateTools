package cn.powernukkitx.codegen;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.function.Predicate;

public class PickBlockClass {
    static File file = new File("build/blockclasses");

    public static void main(String[] args) throws IOException {
        EndWithPicker("CoralFan");
        EndWithPicker("Sapling");
        FunctionalPicker("flower", s -> switch (s) {
            case "BlockOrangeTulip", "BlockPinkTulip", "BlockWhiteTulip", "BlockOxeyeDaisy", "BlockBlueOrchid",
                 "BlockAzureBluet", "BlockRedTulip" -> true;
            default -> false;
        });
    }

    public static void EndWithPicker(String pattern) {
        File targetBlock = new File("build/" + pattern);
        if (!targetBlock.exists()) targetBlock.mkdirs();
        for (var f : Objects.requireNonNull(file.listFiles())) {
            if (f.getName().replace(".java", "").endsWith(pattern)) {
                try {
                    Files.copy(f.toPath(), targetBlock.toPath().resolve(f.getName()), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        for (var f : Objects.requireNonNull(targetBlock.listFiles())) {
            System.out.println("register(%s, %s);".formatted(convertFileNameToId(f.getName()), f.getName().replace(".java", ".class")));
        }
    }

    public static void FunctionalPicker(String folder, Predicate<String> predicate) {
        File targetBlock = new File("build/" + folder);
        if (!targetBlock.exists()) targetBlock.mkdirs();
        for (var f : Objects.requireNonNull(file.listFiles())) {
            if (predicate.test(f.getName().replace(".java", ""))) {
                try {
                    Files.copy(f.toPath(), targetBlock.toPath().resolve(f.getName()), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        for (var f : Objects.requireNonNull(targetBlock.listFiles())) {
            System.out.println("register(%s, %s);".formatted(convertFileNameToId(f.getName()), f.getName().replace(".java", ".class")));
        }
    }

    public static String convertFileNameToId(String fileName) {
        String block = fileName.replace("Block", "").replace(".java", "");
        StringBuilder builder = new StringBuilder();
        boolean f = true;
        for (char c : block.toCharArray()) {
            if (!f && Character.isUpperCase(c)) {
                builder.append("_");
            }
            f = false;
            builder.append(Character.toUpperCase(c));
        }
        return builder.toString();
    }
}

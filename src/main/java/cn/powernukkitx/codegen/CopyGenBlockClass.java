package cn.powernukkitx.codegen;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

public class CopyGenBlockClass {
    static final Path projectPath = Path.of("C:\\Users\\15425\\IdeaProjects\\PowerNukkitX\\src\\main\\java\\cn\\nukkit");
    static final Path targetPath1 = projectPath.resolve("block");
    static final Path targetPath2 = projectPath.resolve("item");

    public static void main(String[] args) {
        copyItem();
        copyBlock();
    }

    public static void copyItem() {
        File file2 = new File("build/itemclasses");
        for (var f : Objects.requireNonNull(file2.listFiles())) {
            Path targetFile = targetPath2.resolve(f.getName());
            if (f.getName().endsWith(".java") && !targetFile.toFile().exists()) {
                try {
                    Files.copy(f.toPath(), targetFile);
                    System.out.println("Success copy new item class for " + f.getName());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static void copyBlock() {
        File file1 = new File("build/blockclasses");
        for (var f : Objects.requireNonNull(file1.listFiles())) {
            Path targetFile = targetPath1.resolve(f.getName());
            if (f.getName().endsWith(".java") && !targetFile.toFile().exists()) {
                try {
                    Files.copy(f.toPath(), targetFile);
                    System.out.println("Success copy new block class for " + f.getName());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}

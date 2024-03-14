package cn.powernukkitx.codegen;

import cn.powernukkitx.codegen.util.DownloadUtil;
import cn.powernukkitx.codegen.util.HashUtil;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.nbt.NbtMapBuilder;
import org.cloudburstmc.nbt.NbtType;
import org.cloudburstmc.nbt.NbtUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class RuntimeBlockStateGen {
    public static void main(String[] args) throws IOException {
        File file = new File("src/main/resources/canonical_block_states.nbt");
        InputStream stream = new FileInputStream(file);
        StringBuilder stringBuilder = new StringBuilder("# WARNING! Don't edit this file! It's automatically regenerated!");
        stringBuilder.append('\n');

        try (var reader = NbtUtils.createNetworkReader(stream)) {
            NbtMap map = (NbtMap) reader.readTag();
            var blocks = map.getList("blocks", NbtType.COMPOUND);
            int runtimeId = 0;
            for (var b : blocks) {
                StringBuilder bString = new StringBuilder(b.getString("name"));
                NbtMap states = b.getCompound("states");
                for (var key : states.keySet()) {
                    bString.append(';').append(key).append('=').append(states.get(key).toString());
                }
                stringBuilder.append(bString).append('\n');
                NbtMapBuilder builder = NbtMapBuilder.from(b);
                builder.remove("version");
                NbtMap build = builder.build();
                int blockHash = HashUtil.fnv1a_32_nbt(build);
                stringBuilder.append("blockHash=").append(blockHash).append('\n');
                stringBuilder.append("runtimeId=").append(runtimeId).append("\n\n");
                runtimeId++;
            }
            Path path = Path.of("target/runtime_block_states.txt");
            Files.deleteIfExists(path);
            Files.writeString(path, stringBuilder, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
        }
    }
}

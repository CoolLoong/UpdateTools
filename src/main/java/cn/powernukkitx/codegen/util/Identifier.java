package cn.powernukkitx.codegen.util;


import static cn.powernukkitx.codegen.util.IdentifierUtils.isNamespaceValid;
import static cn.powernukkitx.codegen.util.IdentifierUtils.isPathValid;
import static cn.powernukkitx.codegen.util.StringUtil.fastTwoPartSplit;

/**
 * A simple logging class that implements the Identified interface
 * <p>
 * Allay Project 2023/3/4
 *
 * @author daoge_cmd
 */
public record Identifier(String namespace, String path) {
    public static final String NAMESPACE_SEPARATOR = ":";
    public static final String DEFAULT_NAMESPACE = "minecraft";

    public Identifier(String[] id) {
        this(id[0], id[1]);
    }

    public Identifier(String id) {
        this(fastTwoPartSplit(id, NAMESPACE_SEPARATOR, DEFAULT_NAMESPACE));
    }

    public Identifier(String namespace, String path) {
        this.namespace = namespace.isEmpty() ? DEFAULT_NAMESPACE : namespace;
        this.path = path;
        if (!isNamespaceValid(this.namespace))
            throw new InvalidIdentifierException("Non [a-z0-9_.-] character in namespace of location: " + this.namespace + NAMESPACE_SEPARATOR + this.path);
        if (!isPathValid(this.path))
            throw new InvalidIdentifierException("Non [a-z0-9/._-] character in path of location: " + this.namespace + NAMESPACE_SEPARATOR + this.path);
    }

    public String toString() {
        return this.namespace + NAMESPACE_SEPARATOR + this.path;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof Identifier lv) {
            return this.namespace.equals(lv.namespace) && this.path.equals(lv.path);
        }
        return false;
    }

    public int hashCode() {
        return 31 * this.namespace.hashCode() + this.path.hashCode();
    }
}

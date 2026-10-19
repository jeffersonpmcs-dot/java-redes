package br.edu.ifsc.lages.rca.fileserver.util;
import java.io.IOException;
import java.nio.file.*;
public final class SafeFiles {
    private SafeFiles() {}
    public static Path resolve(Path root, String name) throws IOException {
        if (name == null || name.isBlank() || name.equals(".") || name.equals("..")
                || name.matches(".*[\\\\/:*?\"<>|].*") || name.endsWith(".") || name.endsWith(" "))
            throw new IOException("Nome de arquivo inválido.");
        Path base = root.toRealPath();
        Path target = base.resolve(name).normalize();
        if (!target.getParent().equals(base) || Files.isSymbolicLink(target))
            throw new IOException("Caminho fora da pasta permitida.");
        return target;
    }
}

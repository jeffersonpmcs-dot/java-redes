package br.edu.ifsc.lages.rca.fileserver.util;
import java.nio.file.Path;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;
class SafeFilesTest {
    @TempDir Path pasta;
    @Test void rejeitaTravessiaECaminhosAbsolutos() {
        for(String nome:new String[]{"../fora.txt", "..\\fora.txt", "C:\\segredo.txt", "/etc/passwd", "..", "arquivo:stream", "arquivo."})
            assertThrows(IOException.class,()->SafeFiles.resolve(pasta,nome),nome);
    }
    @Test void aceitaArquivoNaPasta() throws Exception {
        assertEquals(pasta.toRealPath().resolve("exemplo.txt"),SafeFiles.resolve(pasta,"exemplo.txt"));
    }
    @Test void hashConhecidoEVazio() throws Exception {
        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",FileUtil.calculateSHA256(new byte[0]));
        assertArrayEquals(new byte[0],FileUtil.decodeBase64(FileUtil.encodeBase64(new byte[0])));
    }
}

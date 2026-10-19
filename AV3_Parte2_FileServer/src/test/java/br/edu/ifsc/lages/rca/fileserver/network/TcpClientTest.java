package br.edu.ifsc.lages.rca.fileserver.network;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class TcpClientTest {
    @TempDir Path pasta;

    private void baixarComResposta(String resposta, boolean sucesso) throws Exception {
        try (ServerSocket servidor = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<?> tarefa = executor.submit(() -> {
                try (Socket socket = servidor.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                     PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)) {
                    socket.setSoTimeout(3000);
                    assertNotNull(in.readLine());
                    out.println(resposta);
                } catch (IOException e) { throw new UncheckedIOException(e); }
            });
            TcpClient cliente = new TcpClient();
            try {
                cliente.conectar(servidor.getInetAddress().getHostAddress(), servidor.getLocalPort());
                if (sucesso) {
                    cliente.baixarArquivo("vazio.txt", pasta.toFile());
                    assertEquals(0, Files.size(pasta.resolve("vazio.txt")));
                } else {
                    assertThrows(Exception.class, () -> cliente.baixarArquivo("vazio.txt", pasta.toFile()));
                    assertFalse(Files.exists(pasta.resolve("vazio.txt")));
                }
                tarefa.get(5, TimeUnit.SECONDS);
            } finally {
                cliente.desconectar();
                servidor.close();
                executor.shutdownNow();
            }
        }
    }

    @Test void baixaArquivoVazioComHashValido() throws Exception {
        baixarComResposta("{\"cmd\":\"get resp\",\"value\":\"\",\"hash\":\"e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855\"}", true);
    }

    @Test void hashIncorretoNaoGravaArquivo() throws Exception {
        baixarComResposta("{\"cmd\":\"get resp\",\"value\":\"b2s=\",\"hash\":\"incorreto\"}", false);
    }

    @Test void arquivoAusenteNaoGravaArquivo() throws Exception {
        baixarComResposta("{\"cmd\":\"get resp\"}", false);
    }
}

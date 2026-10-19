package br.edu.ifsc.lages.rca.fileserver.network;

import br.edu.ifsc.lages.rca.fileserver.model.FileMessage;
import br.edu.ifsc.lages.rca.fileserver.util.FileUtil;
import br.edu.ifsc.lages.rca.fileserver.util.SafeFiles;
import com.google.gson.Gson;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class TcpServer {

    private int porta;
    private File diretorioServidor;
    private Gson gson;

    public TcpServer(int porta) {
        this.porta = porta;
        this.gson = new Gson();

        this.diretorioServidor = new File("server_files");
        if (!diretorioServidor.exists()) {
            diretorioServidor.mkdir();
        }
    }

    public void iniciar() {
        try (ServerSocket serverSocket = new ServerSocket(porta)) {
            System.out.println("[Servidor] Rodando na porta " + porta + "...");
            System.out.println("[Servidor] Pasta de arquivos: " + diretorioServidor.getAbsolutePath());

            while (true) {
                // Fica aguardando um cliente se conectar
                Socket clienteSocket = serverSocket.accept();
                System.out.println("[Servidor] Cliente conectado: " + clienteSocket.getInetAddress().getHostAddress());

                processarCliente(clienteSocket);
            }
        } catch (IOException e) {
            System.err.println("[Servidor] Erro de rede: " + e.getMessage());
        }
    }

    // =========================================================================
    // =========================================================================
    private void processarCliente(Socket socket) {
        new Thread(() -> {
            try (
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                    PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)
            ) {
                String jsonRequisicao;

                // O LAÇO: Continua lendo os comandos enquanto o cliente não desconectar
                while ((jsonRequisicao = in.readLine()) != null) {

                    FileMessage requisicao = gson.fromJson(jsonRequisicao, FileMessage.class);
                    FileMessage resposta = new FileMessage();

                    switch (requisicao.getCmd()) {
                        case "list req":
                            resposta.setCmd("list resp");
                            resposta.setFiles(listarArquivos());
                            break;

                        case "put req":
                            resposta.setCmd("put resp");
                            resposta.setFile(requisicao.getFile());
                            boolean salvo = salvarArquivo(requisicao);
                            resposta.setStatus(salvo ? "ok" : "fail");
                            break;

                        case "get req":
                            resposta.setCmd("get resp");
                            resposta.setFile(requisicao.getFile());
                            prepararArquivoParaEnvio(requisicao.getFile(), resposta);
                            break;

                        default:
                            System.out.println("[Servidor] Comando desconhecido ignorado.");
                            resposta.setStatus("fail");
                    }

                    // Envia a resposta de volta ao cliente
                    String jsonResposta = gson.toJson(resposta);
                    out.println(jsonResposta);
                }

            } catch (Exception e) {
                System.out.println("[Servidor] Cliente encerrou a conexão ou erro: " + e.getMessage());
            } finally {
                try {
                    socket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }).start(); // Dá o play na Thread
    }

    // =========================================================================
    // =========================================================================
    private List<String> listarArquivos() {
        List<String> nomes = new ArrayList<>();
        File[] arquivos = diretorioServidor.listFiles();
        if (arquivos != null) {
            for (File f : arquivos) {
                if (f.isFile() && !Files.isSymbolicLink(f.toPath())) nomes.add(f.getName());
            }
        }
        return nomes;
    }

    private boolean salvarArquivo(FileMessage req) {
        try {
            if (req.getValue() == null || req.getValue().length() > 7_000_000) return false;
            byte[] bytesArquivo = FileUtil.decodeBase64(req.getValue());
            if (bytesArquivo.length > 5 * 1024 * 1024) return false;
            String hashCalculado = FileUtil.calculateSHA256(bytesArquivo);

            if (hashCalculado.equals(req.getHash())) {
                File arquivoDestino = SafeFiles.resolve(diretorioServidor.toPath(), req.getFile()).toFile();
                Files.write(arquivoDestino.toPath(), bytesArquivo);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    private void prepararArquivoParaEnvio(String nomeArquivo, FileMessage resp) {
        try {
            File arquivo = SafeFiles.resolve(diretorioServidor.toPath(), nomeArquivo).toFile();
            if (arquivo.isFile()) {
                if (arquivo.length() > 5 * 1024 * 1024) throw new IOException("Limite de 5 MiB.");
                byte[] bytesArquivo = Files.readAllBytes(arquivo.toPath());
                resp.setValue(FileUtil.encodeBase64(bytesArquivo));
                resp.setHash(FileUtil.calculateSHA256(bytesArquivo));
            }
        } catch (Exception e) {
            System.err.println("[Servidor] Erro ao ler arquivo para envio.");
        }
    }
}

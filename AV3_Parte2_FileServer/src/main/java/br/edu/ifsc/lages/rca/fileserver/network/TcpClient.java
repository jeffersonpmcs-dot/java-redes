package br.edu.ifsc.lages.rca.fileserver.network;

import br.edu.ifsc.lages.rca.fileserver.model.FileMessage;
import br.edu.ifsc.lages.rca.fileserver.util.FileUtil;
import com.google.gson.Gson;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public class TcpClient {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Gson gson;

    public TcpClient() {
        this.gson = new Gson();
    }

    // =========================================================================
    // 1. GERENCIAMENTO DA CONEXÃO
    // =========================================================================
    public void conectar(String ip, int porta) throws IOException {
        this.socket = new Socket();
        this.socket.connect(new java.net.InetSocketAddress(ip, porta), 3000);
        this.socket.setSoTimeout(10000);
        // Configura leitura e escrita garantindo o padrão UTF-8 exigido no edital
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        this.out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
    }

    public void desconectar() {
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            System.err.println("[Cliente] Erro ao fechar conexão: " + e.getMessage());
        }
    }

    // =========================================================================
    // 2. NÚCLEO DE COMUNICAÇÃO JSON
    // =========================================================================
    private FileMessage enviarPedido(FileMessage request) throws IOException {
        String jsonRequest = gson.toJson(request);

        // O println já insere a quebra de linha (\n) para destravar a leitura do servidor
        out.println(jsonRequest);

        // Aguarda a resposta do servidor
        String jsonResponse = in.readLine();
        if (jsonResponse == null) {
            throw new IOException("A conexão com o servidor foi perdida.");
        }

        return gson.fromJson(jsonResponse, FileMessage.class);
    }

    // =========================================================================
    // 3. COMANDOS DO SISTEMA (API do Cliente)
    // =========================================================================
    public List<String> listarArquivos() throws Exception {
        FileMessage req = new FileMessage();
        req.setCmd("list req");

        FileMessage resp = enviarPedido(req);
        return resp.getFiles();
    }

    public void enviarArquivo(File arquivo) throws Exception {
        if (arquivo.length() > 5 * 1024 * 1024) throw new IOException("Limite de 5 MiB por arquivo.");
        byte[] bytesArquivo = Files.readAllBytes(arquivo.toPath());

        FileMessage req = new FileMessage();
        req.setCmd("put req");
        req.setFile(arquivo.getName());
        req.setValue(FileUtil.encodeBase64(bytesArquivo));
        req.setHash(FileUtil.calculateSHA256(bytesArquivo)); // Hash gerado antes de viajar

        FileMessage resp = enviarPedido(req);

        if (!"ok".equals(resp.getStatus())) {
            throw new Exception("O servidor recusou o armazenamento do arquivo.");
        }
    }

    public void baixarArquivo(String nomeArquivo, File pastaDestino) throws Exception {
        FileMessage req = new FileMessage();
        req.setCmd("get req");
        req.setFile(nomeArquivo);

        FileMessage resp = enviarPedido(req);

        if (resp.getValue() != null) {
            byte[] bytesRecebidos = FileUtil.decodeBase64(resp.getValue());
            String hashCalculado = FileUtil.calculateSHA256(bytesRecebidos);

            // Validação de Segurança Criptográfica
            if (!hashCalculado.equals(resp.getHash())) {
                throw new Exception("Alerta de Segurança: O Hash não confere. Arquivo corrompido!");
            }

            File arquivoSalvo = br.edu.ifsc.lages.rca.fileserver.util.SafeFiles.resolve(pastaDestino.toPath(), nomeArquivo).toFile();
            Files.write(arquivoSalvo.toPath(), bytesRecebidos);
        } else {
            throw new Exception("O arquivo não existe no servidor ou está vazio.");
        }
    }
}

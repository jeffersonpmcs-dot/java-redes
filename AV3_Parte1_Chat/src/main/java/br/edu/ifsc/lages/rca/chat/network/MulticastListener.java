package br.edu.ifsc.lages.rca.chat.network;

import br.edu.ifsc.lages.rca.chat.model.ChatMessage;
import com.google.gson.Gson;

import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;

public class MulticastListener implements Runnable {

    private MulticastSocket socket;
    private Gson gson;
    private MessageCallback callback;

    public interface MessageCallback {
        void onMessageReceived(ChatMessage message);
    }

    // O construtor recebe o socket (que já conectou no grupo) e a tela que vai receber os textos
    public MulticastListener(MulticastSocket socket, MessageCallback callback) {
        this.socket = socket;
        this.callback = callback;
        this.gson = new Gson();
    }

    @Override
    public void run() {
        // Um buffer (balde) de 4KB para guardar os bytes que chegam da rede
        byte[] buffer = new byte[4096];

        // Laço infinito: A Thread fica viva trabalhando enquanto o socket estiver aberto
        while (socket != null && !socket.isClosed()) {
            try {
                // Prepara o "envelope" vazio para receber os dados
                DatagramPacket pacote = new DatagramPacket(buffer, buffer.length);

                // A execução PARA AQUI (bloqueia) até que alguém mande uma mensagem no grupo
                socket.receive(pacote);

                // Pega os bytes que chegaram e transforma em texto String (descartando os espaços vazios do buffer)
                String jsonRecebido = new String(pacote.getData(), 0, pacote.getLength(), StandardCharsets.UTF_8).trim();

                System.out.println("<<< [JSON RECEBIDO] " + jsonRecebido);

                // Desempacota o JSON de volta para o objeto Java
                ChatMessage mensagem = gson.fromJson(jsonRecebido, ChatMessage.class);

                // Dispara a mensagem para a interface gráfica exibir na tela
                if (callback != null && mensagem != null && mensagem.getUsername() != null
                        && mensagem.getMessage() != null && mensagem.getDate() != null && mensagem.getTime() != null) {
                    callback.onMessageReceived(mensagem);
                }

            } catch (Exception e) {
                // Se o usuário clicar em "Sair" e fechar o socket, o receive() gera uma exceção.
                if (socket.isClosed()) break;
                System.err.println("[Rede] Mensagem inválida descartada.");
            }
        }
    }
}

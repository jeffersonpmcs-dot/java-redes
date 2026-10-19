package br.edu.ifsc.lages.rca.chat.network;

import br.edu.ifsc.lages.rca.chat.model.ChatMessage;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;

public class MulticastNetwork {

    private MulticastSocket socket;
    private InetAddress grupo;
    private int porta;
    private Gson gson;

    // Construtor: Prepara o terreno quando a classe é instanciada
    public MulticastNetwork() {
        this.gson = new Gson();
    }

    // =========================================================================
    // 1. ENTRAR NO GRUPO (Sintonizar a rádio)
    // =========================================================================
    public void conectar(String ipGrupo, int porta) throws IOException {
        this.porta = porta;
        // O IP de Multicast precisa ser um endereço reservado (entre 224.0.0.0 e 239.255.255.255)
        this.grupo = InetAddress.getByName(ipGrupo);
        if (!grupo.isMulticastAddress() || porta < 1 || porta > 65535)
            throw new IOException("Informe um endereço multicast e porta de 1 a 65535.");

        // Cria o socket já atrelado à porta que o usuário escolheu na tela
        this.socket = new MulticastSocket(porta);

        // Avisa o roteador: "Quero receber as mensagens destinadas a este IP de grupo"
        try { this.socket.joinGroup(grupo); }
        catch (IOException e) { this.socket.close(); throw e; }
    }

    // =========================================================================
    // 2. ENVIAR MENSAGEM (O locutor falando)
    // =========================================================================
    public void enviarMensagem(ChatMessage mensagem) throws IOException {
        // 1. Transforma o objeto Java naquele texto JSON que o professor exigiu
        String json = gson.toJson(mensagem);

        // 2. Converte o texto para bytes (pois a rede só entende bytes)
        byte[] bytesParaEnviar = json.getBytes(StandardCharsets.UTF_8);
        if (bytesParaEnviar.length > 4096) throw new IOException("Mensagem excede o limite de 4096 bytes JSON.");

        // 3. Empacota os bytes num "envelope" (DatagramPacket) com o endereço de destino
        DatagramPacket pacote = new DatagramPacket(bytesParaEnviar, bytesParaEnviar.length, grupo, porta);

        // 4. Dispara na rede!
        socket.send(pacote);
    }

    // =========================================================================
    // 3. DESCONECTAR (Desligar a rádio)
    // =========================================================================
    public void desconectar() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.leaveGroup(grupo); // Sai do grupo educadamente
                socket.close(); // Libera a porta do sistema operacional
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // =========================================================================
    // 4. OBTENÇÃO DO SOCKET PARA A THREAD DE LEITURA
    // =========================================================================
    // Precisamos deste método para que a Thread separada consiga escutar a mesma porta
    public MulticastSocket getSocket() {
        return socket;
    }
}

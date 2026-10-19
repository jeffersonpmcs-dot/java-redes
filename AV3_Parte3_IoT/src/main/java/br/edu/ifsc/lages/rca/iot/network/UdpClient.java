package br.edu.ifsc.lages.rca.iot.network;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

public class UdpClient {

    // Método direto: envia um pacote UDP e espera a resposta com limite de tempo
    public static String enviarComando(String ip, int porta, String jsonStr) throws Exception {
        // O bloco try-with-resources garante que a porta do seu PC feche logo após o uso
        try (DatagramSocket socket = new DatagramSocket()) {

            // Timeout de 3 segundos. Se a filial cair ou a internet falhar, o cliente não trava.
            socket.setSoTimeout(3000);

            // 1. Prepara e envia o pacote
            byte[] dadosEnvio = jsonStr.getBytes(StandardCharsets.UTF_8);
            InetAddress endereco = InetAddress.getByName(ip);
            socket.connect(endereco, porta);
            DatagramPacket pacoteEnvio = new DatagramPacket(dadosEnvio, dadosEnvio.length, endereco, porta);
            socket.send(pacoteEnvio);

            // 2. Aguarda a resposta do servidor da filial
            byte[] bufferRecepcao = new byte[8192];
            DatagramPacket pacoteRecebido = new DatagramPacket(bufferRecepcao, bufferRecepcao.length);
            socket.receive(pacoteRecebido);

            // 3. Converte os bytes recebidos de volta para String e devolve para a tela
            return new String(pacoteRecebido.getData(), 0, pacoteRecebido.getLength(), StandardCharsets.UTF_8).trim();
        }
    }
}

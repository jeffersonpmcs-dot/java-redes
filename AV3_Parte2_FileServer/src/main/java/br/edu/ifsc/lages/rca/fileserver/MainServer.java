package br.edu.ifsc.lages.rca.fileserver;

import br.edu.ifsc.lages.rca.fileserver.network.TcpServer;

public class MainServer {
    public static void main(String[] args) {
        // Inicializa o servidor na porta 6789
        TcpServer servidor = new TcpServer(6789);
        servidor.iniciar();
    }
}

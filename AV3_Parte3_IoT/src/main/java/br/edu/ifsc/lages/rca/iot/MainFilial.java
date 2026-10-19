package br.edu.ifsc.lages.rca.iot;

import br.edu.ifsc.lages.rca.iot.network.UdpServer;

public class MainFilial {
    public static void main(String[] args) {

        // 1. Verifica se o usuário esqueceu de digitar o nome da filial no terminal
        if (args == null || args.length == 0) {
            System.err.println("[Erro] Você precisa informar o nome da filial!");
            System.err.println("Exemplo de uso: java MainFilial Lages");
            return; // Encerra o programa
        }

        // 2. Pega a primeira palavra que foi digitada no terminal
        String nomeFilial = args[0];

        System.out.println("=========================================");
        System.out.println("[Sistema] Inicializando unidade: " + nomeFilial);
        System.out.println("=========================================");

        // 3. Passa a palavra digitada para o motor UdpServer
        try {
            UdpServer filial = new UdpServer(args.length > 1 ? args[1] : "config.json", nomeFilial);
            filial.iniciar();
        } catch (Exception e) {
            System.err.println("Falha ao iniciar o servidor: " + e.getMessage());
        }
    }
}

package br.edu.ifsc.lages.rca.iot.network;

import br.edu.ifsc.lages.rca.iot.model.ServerConfig;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.lang.reflect.Type;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UdpServer {

    private ServerConfig config;
    private Gson gson;

    // O nosso "Banco de Dados" em memória para guardar se a luz/ar está ligada(true) ou desligada(false)
    private Map<String, Boolean> estadoDispositivos;

    public UdpServer(String caminhoConfig, String nomeFilial) {
        this.gson = new Gson();
        this.estadoDispositivos = new HashMap<>();
        carregarConfiguracaoCentralizada(caminhoConfig, nomeFilial);
    }

    // =========================================================================
    // 1. CARREGAMENTO INICIAL (Agora com leitura unificada do Vetor JSON)
    // =========================================================================
    private void carregarConfiguracaoCentralizada(String caminho, String nomeFilial) {
        try (FileReader reader = new FileReader(caminho, StandardCharsets.UTF_8)) {
            // Define para o Gson que o arquivo contém uma Lista/Vetor de ServerConfig
            Type listaType = new TypeToken<ArrayList<ServerConfig>>(){}.getType();
            List<ServerConfig> listaConfigs = gson.fromJson(reader, listaType);

            // Procura dentro do arquivo único a configuração que bate com o nome desta filial
            for (ServerConfig c : listaConfigs) {
                // Compara ignorando letras maiúsculas e minúsculas
                if (nomeFilial.equalsIgnoreCase(c.getFilial())) {
                    this.config = c;
                    break;
                }
            }

            if (this.config == null) {
                throw new Exception("Filial '" + nomeFilial + "' não encontrada no arquivo " + caminho);
            }

            System.out.println("[Filial] Servidor inicializado para: " + nomeFilial);
            System.out.println("[Filial] Escutando na porta: " + config.getPort());

            // Cadastra todos os sensores/atuadores na memória, nascendo todos DESLIGADOS (false)
            if (config.getPort() < 1 || config.getPort() > 65535 || config.getAdminUser() == null
                    || config.getAdminPass() == null || config.getId() == null)
                throw new IllegalArgumentException("Campos obrigatórios ausentes ou porta inválida.");
            for (String id : config.getId()) {
                estadoDispositivos.put(id, false);
            }
        } catch (Exception e) {
            System.err.println("[Filial] Erro crítico ao processar configuração: " + e.getMessage());
            throw new IllegalArgumentException("Configuração inválida: " + caminho, e);
        }
    }

    // =========================================================================
    // 2. ABRIR PORTAS (Ouvido do UDP)
    // =========================================================================
    public void iniciar() {
        try (DatagramSocket socket = new DatagramSocket(config.getPort())) {
            System.out.println("[Filial] Servidor UDP ONLINE. Aguardando comandos da Matriz...");
            byte[] buffer = new byte[8192];

            while (true) {
                DatagramPacket pacoteRecebido = new DatagramPacket(buffer, buffer.length);
                socket.receive(pacoteRecebido); // Fica bloqueado aqui até chegar um pacote

                // Processa antes de reutilizar o buffer no próximo receive.
                processarPacote(socket, pacoteRecebido);
            }
        } catch (Exception e) {
            System.err.println("[Filial] Erro na rede UDP: " + e.getMessage());
        }
    }

    // =========================================================================
    // 3. PROCESSAMENTO E REGRAS DE NEGÓCIO (IoT)
    // =========================================================================
    private void processarPacote(DatagramSocket socket, DatagramPacket pacote) {
        try {
            String jsonStr = new String(pacote.getData(), 0, pacote.getLength(), StandardCharsets.UTF_8).trim();
            // Não registrar credenciais recebidas.

            JsonObject jsonRecebido = JsonParser.parseString(jsonStr).getAsJsonObject();

            if (!autenticar(jsonRecebido)) {
                System.out.println("[Filial] ALERTA: Tentativa de acesso negada (Credenciais inválidas).");
                return;
            }

            String cmd = jsonRecebido.get("cmd").getAsString();
            JsonObject resposta = new JsonObject();

            switch (cmd) {
                case "list_req":
                    resposta.addProperty("cmd", "list_resp");
                    JsonArray vetorIds = new JsonArray();
                    for (String id : config.getId()) {
                        vetorIds.add(id);
                    }
                    resposta.add("id", vetorIds);
                    break;

                case "get_status":
                    resposta.addProperty("cmd", "get_resp");
                    for (Map.Entry<String, Boolean> dispositivo : estadoDispositivos.entrySet()) {
                        resposta.addProperty(dispositivo.getKey(), dispositivo.getValue());
                    }
                    break;

                case "set_req":
                    resposta.addProperty("cmd", "set_resp");
                    String idAlvo = jsonRecebido.get("id").getAsString();
                    boolean novoValor = jsonRecebido.get("value").getAsBoolean();

                    if (estadoDispositivos.containsKey(idAlvo) && idAlvo.startsWith("actuator_")) {
                        estadoDispositivos.put(idAlvo, novoValor);
                        resposta.addProperty("id", idAlvo);
                        resposta.addProperty("value", novoValor);
                    } else {
                        resposta.addProperty("error", "Dispositivo invalido ou e um sensor (somente leitura).");
                    }
                    break;

                default:
                    System.out.println("[Filial] Comando desconhecido.");
                    return;
            }

            byte[] dadosResposta = gson.toJson(resposta).getBytes(StandardCharsets.UTF_8);
            DatagramPacket pacoteResposta = new DatagramPacket(
                    dadosResposta, dadosResposta.length,
                    pacote.getAddress(), pacote.getPort()
            );

            socket.send(pacoteResposta);
            System.out.println(">>> [Enviado para Matriz] " + gson.toJson(resposta));

        } catch (Exception e) {
            System.err.println("[Filial] Falha ao processar comando: " + e.getMessage());
        }
    }

    // =========================================================================
    // 4. SEGURANÇA
    // =========================================================================
    private boolean autenticar(JsonObject json) {
        if (json.has("user") && json.has("password")) {
            String user = json.get("user").getAsString();
            String pass = json.get("password").getAsString();

            return user.equals(config.getAdminUser()) && pass.equals(config.getAdminPass());
        }
        return false;
    }
}

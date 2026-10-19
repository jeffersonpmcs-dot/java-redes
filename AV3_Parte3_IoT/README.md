# Simulação IoT por UDP

Projeto acadêmico de Redes do IFSC, com Java, Maven, Gson, Swing e FlatLaf.

## Compilar e testar

Requisitos: JDK 23+ e Maven 3.9+. Abra um terminal nesta pasta:

```text
mvn clean verify
```

## Executar

Execute cada linha abaixo em um terminal separado, nesta pasta:

```text
java -cp target/AV3_Parte3_IoT-1.0-SNAPSHOT.jar br.edu.ifsc.lages.rca.iot.MainFilial Lages
java -cp target/AV3_Parte3_IoT-1.0-SNAPSHOT.jar br.edu.ifsc.lages.rca.iot.MainFilial Florianopolis
java -cp target/AV3_Parte3_IoT-1.0-SNAPSHOT.jar br.edu.ifsc.lages.rca.iot.MainMatriz
```

## Demonstração e funcionamento

1. Inicie as filiais Lages e Florianopolis em terminais separados e depois o painel da matriz.
2. Cadastre `127.0.0.1:51000` e `127.0.0.1:51001`, com usuário `admin` e senha fictícia `demo-only`.
3. Selecione um `actuator_` e alterne seu estado. Dispositivos `sensor_` são somente leitura.

Execute a partir da pasta do projeto para encontrar `config.json`. Para usar configuração própria, copie para `config.local.json` (ignorado pelo Git) e informe o caminho como segundo argumento de `MainFilial`, após o nome da filial.

O protocolo oferece `list_req`, `get_status` e `set_req`, incluindo `user` e `password`. O timeout é de 3 segundos por filial, e o intervalo de consulta aceita 1 a 3600 segundos. Consultas e comandos não bloqueiam a interface. O servidor processa os pequenos comandos sequencialmente para evitar compartilhamento de buffers entre threads.

É uma simulação: não há sensores físicos, os estados começam em `false` e são perdidos ao reiniciar. Credenciais são transmitidas sem criptografia, embora não sejam impressas no log. Credenciais inválidas e comandos desconhecidos não recebem resposta e aparecem como falha de comunicação no painel. Use somente em laboratório confiável.

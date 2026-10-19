# Servidor de arquivos TCP

Projeto acadêmico de Redes do IFSC, com Java, Maven, Gson, Swing e FlatLaf.

## Compilar e testar

Requisitos: JDK 23+ e Maven 3.9+. Abra um terminal nesta pasta:

```text
mvn clean verify
```

## Executar

Execute cada linha abaixo em um terminal separado, nesta pasta:

```text
java -cp target/AV3_Parte2_FileServer-1.0-SNAPSHOT.jar br.edu.ifsc.lages.rca.fileserver.MainServer
java -cp target/AV3_Parte2_FileServer-1.0-SNAPSHOT.jar br.edu.ifsc.lages.rca.fileserver.MainClient
```

## Demonstração e funcionamento

1. Inicie `MainServer` em um terminal e `MainClient` em outro.
2. Conecte a `127.0.0.1`, porta `6789`.
3. Envie um arquivo fictício, atualize a lista e baixe o arquivo em outra pasta.

O servidor cria `server_files/` no diretório de execução. Essa pasta não entra no Git. Há um exemplo fictício em `examples/exemplo.txt`.

O protocolo envia um JSON por linha: `list req`, `put req` e `get req`, com respostas correspondentes. Arquivos são codificados em Base64 e verificados com SHA-256. Arquivos vazios são aceitos; o limite de arquivo é 5 MiB. Nomes com caminhos, travessia ou links simbólicos são rejeitados. Envios com o mesmo nome substituem o arquivo do servidor.

Operações de rede executam fora da thread da interface. A conexão tem timeout de 3 segundos e a leitura de 10 segundos. O servidor não tem autenticação ou TLS, e a leitura de mensagens JSON ainda não impõe limite incremental de memória: use somente em laboratório confiável.

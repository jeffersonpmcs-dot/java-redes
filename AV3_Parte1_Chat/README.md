# Chat multicast

Projeto acadêmico de Redes do IFSC, com Java, Maven, Gson, Swing e FlatLaf.

## Compilar e testar

Requisitos: JDK 23+ e Maven 3.9+. Abra um terminal nesta pasta:

```text
mvn clean verify
```

## Executar

Execute cada linha abaixo em um terminal separado, nesta pasta:

```text
java -cp target/AV3_Parte1_Chat-1.0-SNAPSHOT.jar br.edu.ifsc.lages.rca.chat.MainChat
```

## Demonstração e funcionamento

1. Abra duas instâncias do chat.
2. Informe nomes diferentes e mantenha grupo `230.0.0.0` e porta `6789` em ambas.
3. Conecte e envie uma mensagem. As duas instâncias devem recebê-la.

O protocolo usa `date`, `time`, `username` e `message`. As mensagens JSON têm limite de 4096 bytes UTF-8. Pacotes inválidos são descartados sem encerrar a escuta.

Multicast depende de suporte da rede e das permissões do firewall. Não há confirmação de entrega, histórico persistente, autenticação ou criptografia. O teste automatizado verifica a tolerância a pacotes inválidos por UDP local; não substitui uma verificação multicast entre máquinas.

# Redes em Java: chat, arquivos e simulação IoT

Três projetos acadêmicos desenvolvidos na disciplina de Redes do 3º período do IFSC, reunidos para demonstrar comunicação por sockets, protocolos de aplicação em JSON e interfaces Swing.

| Projeto | Comunicação | Demonstração |
|---|---|---|
| [Chat multicast](AV3_Parte1_Chat/README.md) | UDP multicast | Conversa em grupo sem servidor central |
| [Servidor de arquivos](AV3_Parte2_FileServer/README.md) | TCP | Upload, download e integridade SHA-256 |
| [Simulação IoT](AV3_Parte3_IoT/README.md) | UDP unicast | Consulta de filiais e controle de atuadores |

## Como começar

Instale JDK 23 ou superior e Maven 3.9 ou superior. Na raiz deste repositório, execute `mvn clean verify` para testar e empacotar os três projetos. Também é possível executar esse comando na pasta de um projeto individual. O empacotamento inclui as dependências no JAR; os comandos de execução estão nos READMEs individuais. Não é necessário usar uma IDE específica. O primeiro build precisa de internet para baixar dependências.

## Organização

Cada projeto separa interface (`gui`), comunicação (`network`) e modelos (`model`). São exercícios acadêmicos de protocolos próprios, destinados a demonstrações locais ou em rede de laboratório confiável. Não oferecem TLS, autorização robusta ou proteção completa contra clientes maliciosos. SHA-256 verifica integridade; não criptografa nem autentica arquivos.

## Conteúdo do repositório

Versione código, testes, `pom.xml`, documentação e dados fictícios. As pastas `target/`, `.idea/`, `server_files/` e configurações locais são ignoradas. Arquivos pessoais existentes em `server_files/` devem permanecer somente na máquina local.

## Contexto e autoria

Autoria: [Jefferson Machado (@jeffersonpmcs-dot)](https://github.com/jeffersonpmcs-dot).

Projetos desenvolvidos no contexto acadêmico da disciplina de Redes do 3º período do IFSC. O repositório apresenta os trabalhos e as melhorias posteriores de validação, testes e documentação.

O repositório ainda não possui uma licença de reutilização definida.

## Testes e integração contínua

```text
mvn clean verify
```

O GitHub Actions executa esse comando no Windows e no Linux a cada push e pull request. A primeira execução na plataforma foi concluída com sucesso nos dois ambientes: [ver execução aprovada](https://github.com/jeffersonpmcs-dot/java-redes/actions/runs/39260077479).

### Resultado da validação — 19/10/2026

Compilação, testes e empacotamento concluídos com sucesso nos três projetos, com JDK 25 e compatibilidade de compilação Java 23.

| Projeto | Testes automatizados | Resultado |
|---|---:|---|
| Chat multicast | 1 | Aprovado |
| Servidor de arquivos | 6 | Aprovados |
| Simulação IoT | 3 | Aprovados |
| **Total** | **10** | **Nenhuma falha ou teste ignorado** |

Os testes cobrem mensagens inválidas no chat, caminhos de arquivo indevidos, integridade SHA-256, download de arquivo vazio, rejeição de hash incorreto, arquivo ausente e validação de argumentos e configurações do IoT.

Também foram realizadas verificações locais com comunicação real:

- **TCP:** upload, listagem, download, hash incorreto, tentativa de acesso fora da pasta permitida, arquivo vazio e comando desconhecido.
- **UDP:** consulta de estados, alteração de atuador, rejeição de alteração de sensor, 60 consultas simultâneas, JSON inválido e credenciais incorretas.

Os 10 testes automatizados passaram no Windows e no Linux no GitHub Actions. As verificações adicionais de comunicação TCP e UDP foram realizadas localmente no Windows. A validação não incluiu multicast entre computadores nem uma sessão manual completa das interfaces.

## Repositório publicado

O projeto está disponível em [jeffersonpmcs-dot/java-redes](https://github.com/jeffersonpmcs-dot/java-redes), na branch `main`.

Para obter uma cópia e executar os testes:

```text
git clone https://github.com/jeffersonpmcs-dot/java-redes.git
cd java-redes
mvn clean verify
```

Os comandos para iniciar cada aplicação estão nos READMEs individuais. Novos envios e pull requests executam o workflow **Testes Java**, disponível na aba [Actions](https://github.com/jeffersonpmcs-dot/java-redes/actions).

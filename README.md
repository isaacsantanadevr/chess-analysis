# Chess Analysis

Projeto pessoal para análise de partidas de xadrez usando **Stockfish**, com backend em **Java + Spring Boot** e frontend em **React**.

A ideia do projeto é simples: receber uma partida em PGN, analisar os lances com o Stockfish e apresentar o resultado de uma forma mais clara para o jogador, sem depender apenas de avaliações técnicas do motor.

O desenvolvimento está dividido em duas partes. A primeira é a aplicação funcional de análise. A segunda será focada em cloud, Kubernetes e evolução da arquitetura.

## Parte 1: Analisador de partidas

A primeira parte já está funcional.

O usuário envia uma partida em formato PGN e o sistema analisa os movimentos utilizando o Stockfish.

Para cada lance, a aplicação mostra informações como:

- o movimento realizado;
- a classificação do lance;
- o melhor lance encontrado pelo Stockfish;
- a avaliação da posição antes do movimento;
- a avaliação da posição depois do movimento;
- uma explicação mais simples do que aconteceu na posição.

Os lances podem ser classificados como:

- **Melhor**
- **Bom**
- **Imprecisão**
- **Erro**
- **Erro grave**

A classificação é feita a partir da comparação entre a avaliação da posição antes e depois do lance.

Quando a avaliação é feita em centipawns, o sistema calcula quanto a posição piorou para o jogador que realizou o movimento. Casos envolvendo mate possuem um tratamento separado, já que uma simples diferença de centipawns não representa corretamente esse tipo de posição.

### Como funciona

De forma simplificada:

```text
Usuário envia o PGN
        ↓
Frontend React
        ↓
Backend Spring Boot
        ↓
Stockfish analisa as posições
        ↓
Backend classifica os lances
        ↓
Resultados são armazenados
        ↓
Frontend apresenta a análise

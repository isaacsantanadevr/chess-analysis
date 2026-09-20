# Avaliação dos lances

## Representação

`StockfishResult` mantém `bestMov` e a lista `variation`. O score usa:

| Campo | CP | MATE |
| --- | --- | --- |
| `scoreType` | `CP` | `MATE` |
| `centipawns` | Inteiro na perspectiva branca | `null` |
| `evaluation` | `centipawns / 100.0` | `null` |
| `mate` | `null` | Distância em lances, positiva para brancas, negativa para pretas |
| `mateWinner` | `null` | `WHITE` ou `BLACK`, inclusive em mate zero |

Por exemplo, `score cp 125` com pretas a jogar vira `centipawns: -125`, `evaluation: -1.25`. `score mate -3` com pretas a jogar vira `mate: 3`, `mateWinner: WHITE`. `score mate 0` indica que o lado a jogar já sofreu mate; `mateWinner` evita perder essa informação no zero.

O parser usa a última avaliação exata da linha principal. Linhas `info string`, estatísticas sem score, outras variantes e `lowerbound`/`upperbound` não substituem esse resultado. Ausência de score exato ou encerramento antes de `bestmove` causa erro, sem inventar uma avaliação zero. A codificação UCI distingue centipawns e distância de mate no [código oficial do Stockfish](https://github.com/official-stockfish/Stockfish/blob/master/src/uci.cpp).

## Comparação

O resultado da posição de índice `i` avalia o tabuleiro **após** o lance `i`. Para avaliar esse lance, usamos o resultado `i - 1` como `beforeResult`. O melhor lance usado na classificação é `beforeResult.bestMov`, não o `bestMov` da posição resultante.

Entre dois scores CP:

- Brancas: `max(0, antes.centipawns - depois.centipawns)`.
- Pretas: `max(0, depois.centipawns - antes.centipawns)`.

O lado que jogou vem do FEN anterior. Valores negativos são limitados a zero para evitar perda negativa por oscilação da busca.

| Regra, em ordem de prioridade | Categoria |
| --- | --- |
| Movimento UCI igual ao melhor movimento anterior | `BEST` |
| Perda de 0 a 49 cp | `GOOD` |
| Perda de 50 a 99 cp | `INACCURACY` |
| Perda de 100 a 199 cp | `MISTAKE` |
| Perda de 200 cp ou mais | `BLUNDER` |

Quando qualquer score é mate, `centipawnLoss` é `null`. Preservada a prioridade de `BEST`, perder um mate a favor do jogador ou passar a sofrer um mate antes não detectado recebe `BLUNDER`. Preservar o mesmo vencedor, escapar de mate ou encontrar mate favorável recebe `GOOD`. Não comparamos distâncias de mate como se fossem peões.

As regras são heurísticas locais. A classificação depende da profundidade e das duas buscas independentes. Não há alteração da profundidade, buscas adicionais por lance, classificação de outras plataformas ou garantia de que um mate antes detectado continue visível na busca seguinte. `BEST` pode apresentar perda numérica positiva por essas oscilações.

## Persistência e interface

A V2 adiciona os scores antes/depois, melhor movimento anterior, perda e classificação a `move_analyses`. O movimento realizado já está em `moves.uci`; sua relação existente é preservada. A avaliação inicial tipada fica em `analysis_jobs`. Os novos campos são nulos nos registros legados; não é possível reconstruir os mates perdidos pelo parser antigo.

O painel mostra scores da perspectiva branca, lances em SAN quando conversíveis, classificação recebida do backend e perda em cp. Para mate mostra `+M3`, `−M3` ou mate zero com indicação do vencedor. A barra chega a 100% ou 0% para mate. O melhor lance antes do movimento e o melhor lance na posição atual aparecem separados. O layout, cores de tema e componentes existentes são mantidos.

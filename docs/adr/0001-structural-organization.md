# ADR 0001 — Reorganização estrutural conservadora

Status: adotada nesta reorganização.

## Contexto

O backend Maven estava na raiz, enquanto o frontend já tinha diretório próprio e organização por responsabilidade. É necessário preparar a evolução arquitetural preservando o comportamento atual.

## Decisão

- Mover o projeto Maven e seus scripts para `backend/`, preservando um único módulo.
- Separar controller, orquestração de análise, parsing PGN e integração com engine por packages, modificando apenas declarações de package e imports.
- Manter os DTOs de análise juntos e o resultado Stockfish junto à integração existente.
- Preservar integralmente o frontend, cuja organização já é adequada a esta etapa.
- Reservar diretórios de infraestrutura com `.gitkeep`, sem configuração executável.

## Consequências

Os comandos Maven devem ser executados em `backend/`. Os nomes completos das classes movidas mudam de package; nomes de classes, métodos, endpoints e propriedades JSON são preservados. A aplicação permanece síncrona e dependente do executável Stockfish local.

## Arquitetura planejada

`chess-core`, `chess-api` e `analysis-worker` são divisões futuras, não módulos implementados. A extração fica para outra tarefa para evitar introduzir mudanças de dependências, concorrência ou execução nesta etapa.

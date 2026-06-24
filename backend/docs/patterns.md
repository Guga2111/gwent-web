# Gwent Engine — Patterns

## 1. Engine completamente isolada da API

A `gwent-engine` é um módulo Java puro, sem dependências de framework (sem Spring, sem WebSocket, sem HTTP).
Qualquer camada de transporte (REST, WebSocket, CLI, mobile) é responsabilidade de quem consome a engine.

**Consequência:** a engine deve funcionar standalone. Se quiser rodar via terminal, funciona. Via web, funciona. Via mobile, funciona.

---

## 2. Separação de responsabilidades por camada

```
domain/     → Tipos imutáveis (records, enums). Representam conceitos do jogo.
state/      → Containers mutáveis do estado atual da partida. Sem lógica de regras.
command/    → Intenção do jogador. Dados puros, sem lógica.
exception/  → Hierarquia de erros do domínio.
core/       → Toda a lógica e regras do jogo. Único lugar que muta o estado.
```

---

## 3. State é dumb — lógica vai no core

Classes em `state/` (GameState, PlayerState, Board, BoardRow) são containers de dados.
Elas podem ter getters, setters simples e operações de coleção (add, remove, clear).
**Nunca** tomam decisões de regra do jogo.

```java
// CORRETO — state só armazena
playerState.removeFromHand(card);
playerState.addToGraveyard(card);

// ERRADO — decisão de regra não pertence ao state
playerState.playCard(card); // quem decide se pode jogar é o core
```

---

## 4. Command = ação iniciada pelo jogador

Apenas ações explícitas do jogador viram commands. Transitions automáticas da engine (coin flip, draw inicial, fim de rodada) são métodos do core, não commands.

| Command | Fase |
|---|---|
| `PlayCardCommand(Card, RowType)` | PLAY |
| `PassCommand()` | PLAY |
| `MulliganCommand(Card)` | REDRAW |
| `UseLeaderCommand()` | PLAY |

Commands são records imutáveis — dados puros, sem lógica.

---

## 5. GameEngine.execute() retorna void

O `GameEngine` muta o `GameState` diretamente. O caller lê o estado após a execução.
Não há retorno de estado imutável porque o projeto não tem requisito de undo/replay na engine.

```java
void execute(GameState state, GameCommand command)
```

---

## 6. Sealed interface + pattern matching exaustivo

`GameCommand` é sealed. O compilador garante que todo novo command seja tratado no `GameEngine`.

```java
switch (command) {
    case PlayCardCommand c  -> ...
    case PassCommand c      -> ...
    case MulliganCommand c  -> ...
    case UseLeaderCommand c -> ...
}
```

---

## 7. Hierarquia de exceções tipadas

Nenhum `IllegalStateException` genérico. Toda exceção é uma subclasse de `GwentException`.

```
GwentException
├── exception/command/InvalidCommandException   → jogador fez algo inválido
│   ├── WrongTurnException
│   ├── CardNotInHandException
│   ├── InvalidRowException
│   ├── LeaderAlreadyUsedException
│   └── PlayerAlreadyPassedException
└── exception/state/InvalidStateTransitionException → engine violou invariante
    ├── InvalidPhaseTransitionException
    ├── TurnNotSetException
    ├── RoundLimitExceededException
    └── PlayerAlreadyEliminatedException
```

---

## 8. Enums sobre booleans para legibilidade

Prefira enums quando o valor representa um estado com semântica de domínio.

```java
// CORRETO
Turn currentTurn; // Turn.PLAYER_1 ou Turn.PLAYER_2

// EVITAR
boolean isPlayer1Turn;
```

---

## 9. Testes cobrem happy path, sad path e edge cases

Todo comportamento tem ao menos três cenários testados. Use nomes descritivos no padrão `shouldDoSomethingWhenCondition`.

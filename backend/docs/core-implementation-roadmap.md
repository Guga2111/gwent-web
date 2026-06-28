#depo Gwent Engine — Core Implementation Roadmap

## Decisões de design consolidadas

### GameEngine
- Classe stateless — recebe `GameState` como parâmetro sempre
- `execute(GameState, GameCommand)` faz dispatch via sealed switch
- Validação inline no início de cada handler privado
- Transições engine-initiated (não são commands): `resolveCoinFlip`, `drawInitialCards`, `resolveRound`, `startNewRound`

### Weather
- `FROST`, `FOG`, `RAIN`, `CLEAR_WEATHER` entram no `Ability` enum
- `Card` constructor ganha validação cruzada: `cardType == WEATHER` ↔ ability é uma das 4 weather values
- `core` atualiza `boardRow.setWeatherActive()` + `board.addWeatherCard()` na mesma operação
- `ScoreCalculator` lê `boardRow.isWeatherActive()` — nunca a lista de cartas

### Scoring (ordem de aplicação por linha)
1. `basePower`
2. `TIGHT_BOND` — força × count de cartas com mesmo nome na mesma linha
3. Weather — se ativo, cards não-herói → força = 1 (sobrescreve TIGHT_BOND)
4. `MORALE_BOOST` — +1 para todas as outras cartas da linha
5. `COMMANDERS_HORN` — ×2 para todos os units da linha

Heroes (`CardType.HERO`) são imunes a weather e SCORCH.

### Round flow
- Após `PassCommand`: se oponente também passou → `resolveRound()`, senão não troca turno (oponente continua jogando)
- `resolveRound()`: calcula score de ambos → compara → `loseLife()` no perdedor (empate = ambos perdem) → se eliminado → `GAME_OVER`, senão → `startNewRound()`
- `startNewRound()`: `clearRows()` em ambos → `clearWeatherCards()` → `resetPassed()` em ambos → perdedor da rodada joga primeiro → comprar cartas → `nextRound()` → `setPhase(REDRAW)`

### MEDIC
- `GameState` ganha campo `pendingAbility` (dado puro, sem lógica)
- Engine seta `pendingAbility = MEDIC_CHOICE` ao processar MEDIC — não troca turno ainda
- `ResolveMedicCommand(Card chosen)` resolve a escolha, limpa `pendingAbility`, troca turno
- Timeout de 30s e envio de carta aleatória em caso de expiração: responsabilidade da API, não da engine

---

## Ordem de implementação

### Passo 1 — `domain/Ability`
Adicionar os valores de clima: `FROST`, `FOG`, `RAIN`, `CLEAR_WEATHER`.

### Passo 2 — `domain/Card`
No canonical constructor, adicionar validação cruzada:
- Se `cardType == WEATHER` → `ability` obrigatório e deve ser uma das 4 weather values
- Se `ability` é uma weather value → `cardType` deve ser `WEATHER`

### Passo 3 — `command/ResolveMedicCommand`
Novo record: `public record ResolveMedicCommand(Card card) implements GameCommand {}`
Adicionar ao sealed `GameCommand`.

### Passo 4 — `state/GameState`
Adicionar campo `pendingAbility` nullable. Getter + setter simples.
Candidato: enum `PendingAbility { MEDIC_CHOICE }` em `domain/` ou `core/`.

### Passo 5 — `core/ScoreCalculator`
Classe package-private. Calcular score de um `PlayerState` considerando `weatherActive` e os modificadores de linha.
Implementar antes do GameEngine — é isolada e testável de forma independente.

### Passo 6 — `core/AbilityResolver`
Classe package-private. Lógica `onPlay` por ability (SPY, MEDIC, MUSTER, SCORCH, AGILE...).
SCORCH depende do ScoreCalculator — implementar depois do passo 5.

### Passo 7 — `core/GameEngine`
Por último, pois orquestra tudo.
Cada handler delega ao `AbilityResolver` e ao `ScoreCalculator`.
Implementar handlers na ordem: `handleMulligan` → `handlePass` → `handleUseLeader` → `handlePlayCard`.

---

## Validações por command (referência)

| Command | Validações |
|---|---|
| `PlayCardCommand` | `phase == PLAY`, turno correto, card in hand, `targetRow` compatível com `card.rowType()` (ou `AGILE`) |
| `PassCommand` | `phase == PLAY`, turno correto, player não passou ainda |
| `MulliganCommand` | `phase == REDRAW`, card in hand |
| `UseLeaderCommand` | `phase == PLAY`, turno correto, leader não usado |
| `ResolveMedicCommand` | `pendingAbility == MEDIC_CHOICE`, card in graveyard, card não é SPECIAL/WEATHER/LEADER |

---

## Estrutura de pastas esperada ao final

```
core/
  GameEngine.java
  ScoreCalculator.java      (package-private)
  AbilityResolver.java      (package-private)
```

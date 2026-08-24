# Proximos Passos

## Prioridade 1 — Engine (leader ability pendente)

### Leader abilities implementadas (18/18)

Todas implementadas no engine (`LeaderAbilityResolver`), API (`GameModelMapper`) e frontend (`LeaderOverlay`, `RevealedCardsOverlay`).


## Prioridade 2 — Faction Passives (engine)

Cada faccao tem uma habilidade passiva que se aplica automaticamente no fim/inicio de round, sem interacao do jogador (exceto Scoia'tael).

### Engine hooks necessarios

- **`resolveRound()`** — apos calcular scores e determinar loser, antes de `startNewRound()`
- **`startNewRound()`** — antes e depois de `clearRows()`

### Criar `FactionPassiveResolver` em `gwent-engine/core/`

Seguir o padrao de `LeaderAbilityResolver`. Faccao derivada de `player.getLeader().faction()`.

### Passivas por faccao

| Faccao | Passiva | Hook | Logica |
|---|---|---|---|
| **Northern Realms** | Comprar 1 carta ao vencer um round | Apos determinar winner em `resolveRound` | Se NR ganhou e deck nao vazio → `winner.drawCard()` |
| **Nilfgaard** | Vencer empates (se scores iguais, Nilfgaard nao perde vida) | Dentro do branch `else` (tie) em `resolveRound` | Se um jogador e Nilfgaard e o outro nao: apenas o nao-Nilfgaard perde vida. Mirror match: empate normal |
| **Monsters** | Manter 1 unidade aleatoria no campo ao fim do round | Antes/depois de `clearRows()` em `startNewRound` | Escolher 1 UNIT (nao HERO) aleatorio das rows do Monsters player; remover da row antes de `clearRows()`; re-adicionar apos a limpeza. Guardar `(Card, RowType)` pois AGILE precisa da row real |
| **Scoia'tael** | Escolher quem comeca cada round | Requer `PendingAbility.SCOIA_ROUND_START` | Apos fim de round (antes de `startNewRound`), se um jogador e Scoia'tael: setar pending ability; frontend exibe prompt "voce quer comecar ou dar a vez ao oponente?"; resolver com `RESOLVE_FACTION` command |
| **Skellige** | Ressuscitar 2 unidades do cemiterio no inicio de cada round | Apos `clearRows()` em `startNewRound` | Escolher ate 2 UNIT (nao HERO) aleatorios do cemiterio e adicionar a mao do jogador Skellige |

### Observacoes de implementacao

- `BoardRow.addCard()` valida `rowType` — guardar a row real de cartas AGILE ao extrair
- Scoia'tael e a unica passiva que requer interacao (novo `PendingAbility` + frontend overlay simples com 2 botoes)
- Testar com mirror match Nilfgaard vs Nilfgaard para garantir tie normal

### Arquivos a modificar

- `gwent-engine/core/GwentEngine.java` — hooks em `resolveRound` e `startNewRound`
- `gwent-engine/core/FactionPassiveResolver.java` — novo arquivo
- `gwent-engine/domain/PendingAbility.java` — adicionar `SCOIA_ROUND_START` (se implementar Scoia'tael)
- `gwent-api/.../GameModelMapper.java` — expor pending ability no DTO (ja existe campo `pendingAbility`)
- Frontend — overlay simples para Scoia'tael (se implementar)

---

## Prioridade 3 — Polish

1. **Countdown visual nos overlays** — mostrar timer regressivo no MulliganOverlay (30s) e MedicOverlay (30s). O backend ja aplica timeout automatico, mas o frontend nao exibe contagem
2. **Tooltips de habilidades nas cartas**
3. **Animacoes de jogada/pontuacao**
4. **Leaderboard e estatisticas** — requer modelo de dados de historico de partidas

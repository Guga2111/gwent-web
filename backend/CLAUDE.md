# CLAUDE.md — Gwent Web Backend

## Leitura obrigatória antes de qualquer tarefa

Antes de implementar, sugerir mudanças ou revisar código, leia obrigatoriamente:

- [`docs/patterns.md`](docs/patterns.md) — padrões arquiteturais e de código do projeto
- [`docs/antipatterns.md`](docs/antipatterns.md) — o que nunca fazer neste projeto

Toda sugestão ou implementação deve estar alinhada com esses documentos.

---

## Contexto do projeto

Implementação do jogo de cartas Gwent (Witcher 3) como engine Java + API REST/WebSocket.

**Módulos:**
- `gwent-engine` — lógica pura do jogo, Java 25, zero dependências de framework
- `gwent-api` — Spring Boot 3.4, camada de transporte (REST + WebSocket)

**Stack:** Java 25, Maven multi-módulo, JUnit 5

---

## Estrutura da gwent-engine

```
domain/     → records e enums imutáveis (Card, Faction, CardType, RowType, Ability, GamePhase, Turn)
state/      → containers mutáveis (GameState, PlayerState, Board, BoardRow)
command/    → intenção do jogador (GameCommand sealed, PlayCardCommand, PassCommand, MulliganCommand, UseLeaderCommand)
exception/  → hierarquia tipada (GwentException → command/ e state/)
core/       → (a implementar) lógica e regras do jogo, GameEngine
```

---

## Convenções

- State classes são dumb containers — lógica vai no `core`
- Commands são records imutáveis — dados puros, sem lógica
- `GameEngine.execute(GameState, GameCommand)` retorna void
- Toda exceção estende `GwentException`, nunca usar `IllegalStateException` genérico
- Testes cobrem happy path, sad path e edge cases
- Claude `não deve colocar sua marca d'agua (watermark) nos commits e nos prs` "🤖 Generated with Claude Code"
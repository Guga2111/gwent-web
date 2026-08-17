# Proximos Passos

## Prioridade 1 — Seguranca (bloqueia producao)

1. **WebSocket ChannelInterceptor** — validar JWT no CONNECT e restringir SUBSCRIBE ao topico do proprio jogador

## Prioridade 2 — Completar o loop de jogo

2. **MedicOverlay no frontend** — a engine ja seta `pendingAbility = MEDIC_CHOICE` mas o front nao tem UI para o jogador escolher qual carta restaurar do cemiterio

### Ja implementados nesta branch

- **Broadcast de erros via WebSocket** — backend lanca excecoes para `/topic/games/{gameId}/{userId}/errors`; frontend ja assina o topico correto
- **Surrender funcional** — `api/game.ts` tem `surrender()`, `ControlBar.tsx` recebe `onSurrender`, `Game.tsx` passa o handler
- **Reconexao WebSocket** — STOMP `reconnectDelay: 5000` + `getGameState()` no `onConnect`

## Prioridade 3 — Conteudo

6. **Catalogo de cartas + seed** — substituir `makePresetDeck()` por cartas persistidas no banco, separadas por faccao
7. **Deck builder (DeckForge)** — pagina stub, precisa de backend (CRUD de decks) + frontend
8. **Selecao de faccao/deck ao criar jogo** — hoje todos jogam com o mesmo deck Northern Realms

## Prioridade 4 — Engine (lider abilities restantes)

### Leader abilities implementadas (17/18)

- SIEGE_MASTER (Northern Realms)
- WHITE_FLAME (Nilfgaard)
- DESTROYER_OF_WORLDS (Monsters)
- DAISY_OF_THE_VALLEY (Scoia'tael)
- KING_BRAN (Skellige)
- BRINGER_OF_DEATH (Monsters)
- INVADER_OF_THE_NORTH (Nilfgaard)
- LORD_COMMANDER (Northern Realms)
- QUEEN_OF_DOL_BLATHANNA (Scoia'tael)
- EMPEROR_OF_NILFGAARD (Nilfgaard)
- KING_OF_THE_WILD_HUNT (Monsters)
- RELENTLESS (Nilfgaard)
- KING_OF_TEMERIA (Northern Realms)
- COMMANDER_OF_THE_RED_RIDERS (Monsters)
- CLAN_AN_CRAITE (Skellige)
- NORTH_COMMANDER (Northern Realms)
- HOPE_OF_THE_AEN_SEIDHE (Scoia'tael)

### Leader abilities pendentes (1)

- PUREBLOOD_ELF — requer troca de turno mid-pending (complexidade alta)


## Prioridade 5 — Polish

11. **Tooltips de habilidades nas cartas**
12. **Animacoes de jogada/pontuacao**
13. **Leaderboard e estatisticas**
14. **Countdown visual nos overlays** — mostrar timer regressivo no MulliganOverlay (30s) e MedicOverlay (30s). O backend ja aplica timeout automatico, mas o frontend nao exibe contagem

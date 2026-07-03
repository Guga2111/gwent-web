# Proximos Passos

## Prioridade 1 — Seguranca (bloqueia producao)

1. **WebSocket ChannelInterceptor** — validar JWT no CONNECT e restringir SUBSCRIBE ao topico do proprio jogador
2. **Broadcast de erros via WebSocket** — hoje o backend lanca excecoes mas nao envia para `/topic/games/{gameId}/errors`. O frontend ja escuta esse topico mas nunca recebe nada

## Prioridade 2 — Completar o loop de jogo

3. **MedicOverlay no frontend** — a engine ja seta `pendingAbility = MEDIC_CHOICE` mas o front nao tem UI para o jogador escolher qual carta restaurar do cemiterio
4. **Surrender funcional** — o botao existe mas faz `onClick={() => {}}`
5. **Reconexao WebSocket** — se o jogador desconecta, perde a sessao. Precisa de retry + recarregar estado via `GET /api/games/{gameId}`

## Prioridade 3 — Conteudo

6. **Catalogo de cartas + seed** — substituir `makePresetDeck()` por cartas persistidas no banco, separadas por faccao
7. **Deck builder (DeckForge)** — pagina stub, precisa de backend (CRUD de decks) + frontend
8. **Selecao de faccao/deck ao criar jogo** — hoje todos jogam com o mesmo deck Northern Realms

## Prioridade 4 — Engine (lider abilities restantes)

9. **11 leader abilities faltando** — varias precisam de um sistema de `pendingAbility` com player choice (o framework ja existe, falta expandir)
10. **Board modifier system** — necessario para NORTH_COMMANDER, HOPE_OF_THE_AEN_SEIDHE

### Leader abilities implementadas (5/16)

- SIEGE_MASTER (Northern Realms)
- WHITE_FLAME (Nilfgaard)
- DESTROYER_OF_WORLDS (Monsters)
- DAISY_OF_THE_VALLEY (Scoia'tael)
- KING_BRAN (Skellige)

### Leader abilities pendentes (11)

- NORTH_COMMANDER — requer board modifier system
- KING_OF_TEMERIA — requer player choice / pendingAbility
- LORD_COMMANDER — destroi unidade siege inimiga se score da row >= 10
- EMPEROR_OF_NILFGAARD — ver 3 cartas do oponente (responsabilidade da API)
- INVADER_OF_THE_NORTH — cancela leader ability do oponente
- RELENTLESS — puxa carta do cemiterio do oponente (requer player choice)
- BRINGER_OF_DEATH — pega carta weather do deck
- COMMANDER_OF_THE_RED_RIDERS — pega qualquer carta, depois descarta (requer player choice)
- KING_OF_THE_WILD_HUNT — restaura carta do cemiterio (requer pendingAbility)
- QUEEN_OF_DOL_BLATHANNA — destroi unidade melee mais forte se score >= 10
- PUREBLOOD_ELF / HOPE_OF_THE_AEN_SEIDHE — requer board modifier system
- CLAN_AN_CRAITE — restaura 2 cartas do cemiterio (requer dois pendingAbility)

## Prioridade 5 — Polish

11. **Tooltips de habilidades nas cartas**
12. **Animacoes de jogada/pontuacao**
13. **Leaderboard e estatisticas**
14. **Countdown visual nos overlays** — mostrar timer regressivo no MulliganOverlay (30s) e MedicOverlay (30s). O backend ja aplica timeout automatico, mas o frontend nao exibe contagem

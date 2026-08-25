# Proximos Passos — Pre-Demo

## Critico (bloqueia o demo)

### 1. Disconnection handling
Nao ha reconnect nem auto-forfeit. Se o WebSocket de um jogador cai no meio do jogo, o jogo trava. No minimo, adicionar timeout que declara forfeit do jogador desconectado.

### 2. Frontend Dockerfile + docker-compose
O backend ja tem config Docker de producao, mas o frontend nao tem nada. Criar Dockerfile (Bun build -> nginx serve) e um docker-compose combinado.

### 3. BERSERKER ability e no-op
A ability existe no enum e nos cards, mas `AbilityResolver` nunca a trata. Implementar ou remover os cards do catalogo para nao confundir jogadores.

---

## Alta Prioridade (prejudica a experiencia do demo)

### 4. Imagens de cards Skellige em falta
Todos os 12 cards Skellige renderizam como placeholders. Adicionar a arte ou esconder a faccao no deck building por agora.

### 5. Countdown timers nos overlays
Mulligan, Medic e Leader overlays tem timeouts de 30s no backend que auto-resolvem, mas o jogador nao ve contagem regressiva. Auto-resolucao surpresa vai frustrar testers.

### 6. Taverna sempre mostra "Reinos do Norte"
O hub ignora a faccao do deck ativo do jogador. Menor mas parece bug.

### 7. UX de matchmaking manual
Jogadores precisam partilhar um UUID para comecar um jogo. No minimo, documentar claramente ou adicionar uma fila de matchmaking simples.

---

## Nice to Have (polish)

8. 3 imagens de cards Scoiatael em falta
9. Paginas Shop e Leaderboard sao stubs ("Coming Soon") — ok se claramente rotuladas
10. Sem tutorial/ajuda — jogadores novos nao vao saber as regras
11. Sem sons ambiente ou SFX de cards

---

## O que ja esta solido

- Ciclo de jogo completo funciona end-to-end (coin flip -> mulligan -> play -> rounds -> game over)
- Todas as abilities principais a funcionar (SPY, MEDIC, SCORCH, MUSTER, TIGHT_BOND, MORALE_BOOST, HORN, weather)
- Todas as 5 passivas de faccao implementadas e testadas
- 18/18 leader abilities feitas
- ~77 cards em 5 faccoes + neutral
- Deck building com validacao (22-40 cards, max copias, etc.)
- Game-over overlay, round-end overlay, todos os overlays de escolha presentes
- Cobertura de testes no backend solida (11 ficheiros de teste, ~4k linhas)
- Auth (JWT), CORS, DB migration tudo configurado

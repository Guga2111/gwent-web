# Gwent Frontend — Patterns

## 1. Estado do jogo como fonte única de verdade

O `GameStateDto` recebido via WebSocket é a única fonte de verdade do estado da partida.
Nenhum componente mantém estado local que duplique ou derive dados do game state.

```tsx
// CORRETO — lê direto do store
const gameState = useGameStore((s) => s.gameState)
const myHand = gameState?.player1.hand

// ERRADO — cópia local que dessincroniza
const [hand, setHand] = useState(gameState?.player1.hand)
```

---

## 2. Separação clara entre camadas

```
api/        → Comunicação HTTP (Axios). Não conhece componentes.
hooks/      → Lógica reutilizável (useWebSocket). Conecta API/stores aos componentes.
stores/     → Estado global (Zustand). Sem side effects nem chamadas de rede.
pages/      → Telas roteadas (Login, Lobby, Game). Compõem componentes.
components/ → Peças visuais. Recebem dados via props ou leem do store.
types/      → DTOs e interfaces. Espelham o contrato do backend.
styles/     → CSS global e tema medieval.
```

---

## 3. Componentes recebem dados, não buscam

Componentes de board (BoardRow, Card, PlayerPanel) recebem dados via props.
Somente pages e hooks acessam stores diretamente.

```tsx
// CORRETO — componente puro
function BoardRow({ row, onCardClick }: BoardRowProps) { ... }

// ERRADO — componente acoplado ao store
function BoardRow() {
  const row = useGameStore(s => s.gameState?.player1.close) // acoplamento
}
```

Exceção: componentes de topo da página (Game.tsx) podem ler do store e distribuir via props.

---

## 4. Commands via WebSocket, queries via HTTP

- **Ações do jogador** (play card, pass, mulligan) → STOMP publish para `/app/games/{id}/command`
- **Operações de sessão** (create game, join, register, login) → HTTP via Axios

Nunca misturar os dois canais para a mesma operação.

---

## 5. Tema medieval via CSS variables

Todas as cores, fontes e espaçamentos do tema estão definidos em `styles/index.css` como CSS custom properties.
Componentes referenciam variáveis, nunca valores hardcoded.

```css
/* CORRETO */
color: var(--gold-light);
font-family: var(--font-heading);

/* ERRADO */
color: #f6dd97;
font-family: 'Cinzel', serif;
```

---

## 6. Identidade do jogador resolvida no frontend

O frontend determina "quem sou eu" comparando o email do JWT com os `playerId` do `GameStateDto`.
Isso define qual lado do board é "meu" e qual é "oponente".

```tsx
const myTurn = gameState.player1.playerId === user.email ? 'PLAYER_1' : 'PLAYER_2'
const myState = myTurn === 'PLAYER_1' ? gameState.player1 : gameState.player2
const opponentState = myTurn === 'PLAYER_1' ? gameState.player2 : gameState.player1
```

---

## 7. Layout do board com CSS Grid/Flexbox, não libs externas

O tabuleiro usa CSS nativo (grid para o layout 3-column, flex para linhas de cartas).
Sem dependência de libs de drag-and-drop ou layout engines para o MVP.

---

## 8. Oponente nunca vê dados privados

A mão do oponente renderiza apenas card backs (quantidade de cartas visível, conteúdo oculto).
O backend já envia dados filtrados, mas o frontend reforça essa separação visual.

---

## 9. Proxy reverso para desenvolvimento

Vite proxies `/api`, `/authenticate` e `/ws` para o backend (`localhost:8080`).
O frontend nunca hardcoda URLs absolutas do backend — tudo é relativo.
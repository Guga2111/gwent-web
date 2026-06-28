# Gwent Frontend — Anti-Patterns

## 1. Duplicar estado do game no componente

O estado da partida vem do WebSocket e vive no Zustand store.
Nunca copiar para `useState` local — causa dessincronização.

```tsx
// ERRADO — state local diverge quando chega novo update do WS
const [score, setScore] = useState(gameState?.player1.totalScore)

// CORRETO — lê sempre do store
const score = useGameStore(s => s.gameState?.player1.totalScore)
```

---

## 2. Hardcodar valores de tema

Cores, fontes e sombras do tema medieval estão em CSS variables.
Usar valores hex/rgb diretos nos componentes quebra a consistência visual e dificulta mudanças de tema.

```tsx
// ERRADO
<div style={{ color: '#f6dd97', fontFamily: "'Cinzel', serif" }}>

// CORRETO
<div style={{ color: 'var(--gold-light)', fontFamily: 'var(--font-heading)' }}>
```

---

## 3. Chamar API dentro de componentes visuais

Componentes de `components/` são visuais — recebem dados via props, não fazem fetch.
Chamadas de API ficam em `hooks/` ou `pages/`.

```tsx
// ERRADO — componente visual fazendo fetch
function Card({ cardId }) {
  const [data, setData] = useState(null)
  useEffect(() => { fetchCard(cardId).then(setData) }, []) // NÃO
}

// CORRETO — page busca e passa via props
function Game() {
  const gameState = useGameStore(s => s.gameState)
  return <Card card={gameState.player1.hand[0]} />
}
```

---

## 4. Lógica de regras do jogo no frontend

O frontend não valida regras do Gwent. Quem decide se uma jogada é válida é a engine no backend.
O frontend envia o command e espera o novo estado via WebSocket.

```tsx
// ERRADO — frontend decidindo se pode jogar
if (card.row === 'SIEGE' && !siegeRow.isFull) { sendCommand(...) }

// CORRETO — envia e deixa o backend validar
sendCommand({ commandType: 'PLAY_CARD', cardId: card.id, row: card.row })
// se inválido, backend retorna erro via WS ou HTTP
```

---

## 5. Usar libs de UI genéricas para o board

O tabuleiro do Gwent tem layout único (3 rows por lado, cartas em fan, leader cards, weather slots).
Nenhuma lib de componentes genérica (Material UI, Shadcn, Ant Design) resolve isso — é CSS custom.

Libs de UI genéricas servem para formulários e modais, não para o game board.

---

## 6. Polling para atualizações de estado

O estado da partida chega via WebSocket (STOMP subscription).
Nunca fazer polling HTTP para buscar o game state.

```tsx
// ERRADO
useEffect(() => {
  const interval = setInterval(() => fetchGameState(gameId), 1000) // NÃO
  return () => clearInterval(interval)
}, [])

// CORRETO — já implementado em useWebSocket
stompClient.subscribe(`/topic/games/${gameId}`, (msg) => {
  setGameState(JSON.parse(msg.body))
})
```

---

## 7. Expor dados do oponente no DOM

Mesmo que o backend envie dados filtrados, nunca renderizar informações privadas do oponente
(cartas na mão, estratégia) em elementos ocultos no DOM. Inspetor de elementos revela tudo.

```tsx
// ERRADO — dados expostos no DOM, apenas visualmente ocultos
<div style={{ display: 'none' }}>{opponentHand.map(c => c.name)}</div>

// CORRETO — renderiza apenas card backs
{Array.from({ length: opponentHandSize }).map((_, i) => <CardBack key={i} />)}
```

---

## 8. Over-engineering de animações no MVP

Animações sofisticadas (card flip 3D, particle effects, smooth transitions entre rounds)
são futuras. No MVP, foco em funcionalidade — transições simples com CSS transitions bastam.

---

## 9. Misturar idiomas na UI sem sistema de i18n

A UI do design é em português. Se for internacionalizar depois, usar um sistema (i18next) desde o início.
Para o MVP, manter tudo em português consistentemente — não misturar strings PT e EN na interface.

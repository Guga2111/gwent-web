# Gwent Frontend — Documentation

## Design References

Protótipos criados no Claude Design. Arquivos fonte em:

```
~/Downloads/Gwent Online Hub Design/
├── Hub Gwent.dc.html              — Hub completo (6 telas + matchmaking modal)
├── Hub Gwent-print-h3rsnt.dc.html — Versão print do Hub
├── Tabuleiro.dc.html              — Tabuleiro de jogo
├── support.js                     — Runtime do Claude Design
└── uploads/
    ├── SCR-20260627-oqvw.png          — Screenshot referência do Gwent (Witcher 3)
    ├── pasted-1782428786039-0.png     — Screenshot referência do Gwent (Witcher 3)
    └── pasted-1782429520620-0.png     — Screenshot referência do Gwent (Witcher 3)
```

---

## Design System — Tema Medieval

Extraído dos protótipos e fixado em `src/styles/index.css`.

### Fontes

| Uso | Fonte | Variável |
|---|---|---|
| Títulos decorativos (logo, nomes de tela) | Cinzel Decorative 700/900 | `--font-display` |
| Headings, labels, scores | Cinzel 500-700 | `--font-heading` |
| Corpo, descrições, flavor text | IM Fell English (italic) | `--font-body` |
| UI funcional (botões, inputs, stats) | Barlow 400-700 | `--font-ui` |

### Paleta de cores

| Token | Hex | Uso |
|---|---|---|
| `--bg-darkest` | `#0d0a07` | Fundo geral da aplicação |
| `--bg-dark` | `#1a1410` | Painéis, sidebars |
| `--bg-medium` | `#2a2118` | Inputs, áreas internas |
| `--bg-card` | `#332a20` | Cards de UI, containers |
| `--gold-light` | `#f6dd97` | Destaques, títulos, scores |
| `--gold` | `#caa057` | Labels, bordas ativas, text secondary gold |
| `--gold-dark` | `#a07830` | Botões primários, backgrounds de CTA |
| `--text-primary` | `#e8dcc8` | Texto principal |
| `--text-secondary` | `#b0a08a` | Texto complementar |
| `--text-muted` | `#7a6c5a` | Texto desabilitado, hints |
| `--red` | `#c44` | Oponente, erros, derrotas |
| `--green` | `#5a8a3c` | Vitórias, validações |
| `--blue` | `#4a7a9c` | Facção do Norte, ranks |

### Animações (do protótipo)

- `gw-flicker` — pulsação sutil de luz ambiente (taverna)
- `gw-glow` — brilho pulsante no botão principal (CTA)
- `tb-turn` — highlight do painel do jogador quando é sua vez
- `tb-pulse` — pulsação de indicadores

---

## Telas do Protótipo

### Hub (Hub Gwent.dc.html)

6 telas navegáveis pelo bottom nav + 1 modal overlay:

#### 1. Taverna (Home) `isHome`
- **Centro:** Saudação italic, escudo da facção, modo de jogo (Ranqueada), botão "PROCURAR OPONENTE" com glow
- **Canto inferior esquerdo:** Preview do baralho ativo (fan de 5 cartas + leader card + nome + botão editar)
- **Canto inferior direito:** "Encomendas do Taverneiro" — 3 quests diárias com progresso (parchment card)
- **Footer:** Link "trocar tapas numa amistosa" (match privado)

#### 2. Forja de Baralhos `isDeck`
- **Layout:** Grid 2 colunas — sidebar esquerda (336px) com baralho atual + grid direita com coleção
- **Baralho atual:** Leader card, lista de unidades com contagem, total (25/25), botões "Novo baralho" e "Salvar"
- **Coleção:** Filtros por facção/tipo (tabs pill), grid 6 colunas com cards mostrando arte, power, custo

#### 3. Mercador (Shop) `isShop`
- **Featured banner:** Destaque de expansão (barril temático com preço em coroas)
- **Prateleira:** Grid 3 colunas com itens — barris de cartas, cosméticos, expansões
- **Cada item:** Arte, tag, nome, descrição, botão de compra com preço

#### 4. Salão da Glória (Leaderboard) `isRank`
- **Filtros:** Continente / Conhecidos / por facção (tabs pill)
- **Tabela:** Posto, crest/avatar, nome, facção, PR (pontos), sequência de vitórias
- **Header:** Nome da temporada + tempo restante

#### 5. Brasão (Profile) `isProfile`
- **Banner topo:** Avatar shield, nome, título, rank, nível (circular progress)
- **Grid 2 colunas:**
  - Esquerda: Feitos (6 stat boxes: partidas, %vitórias, recorde, cartas, baralhos, facção principal) + Maestria das Facções (barras de progresso)
  - Direita: Últimos Duelos (W/L boxes) + Títulos Conquistados (badges com ícones)

#### 6. Ajustes (Settings) `isSettings`
- **Seções:** Áudio (sliders), Imagem & Tela (quality picker, toggles), Jogabilidade (toggles), Conta (idioma, gerenciar conta, logout)
- **Componentes:** Sliders com knob dourado, toggles, radio group visual

#### 7. Modal de Matchmaking (overlay)
- **Estado 1 — Buscando:** Spinner ring animado, texto "Procurando adversário...", botão cancelar
- **Estado 2 — Encontrado:** "VS" entre dois shields de facção, nomes dos jogadores, botão "Iniciar Duelo"

### Top HUD (todas as telas do Hub)
- **Esquerda:** Avatar shield com nível, nome, título, rank + PR
- **Centro:** Logo "GWENT" em Cinzel Decorative
- **Direita:** Coroas (moeda gold) + Sucata (moeda blue) + ícone configurações

### Bottom Nav (todas as telas do Hub)
- 5 botões: Taverna, Forja de Baralhos, Mercador, Salão da Glória, Brasão
- Cada botão: ícone SVG em medallion circular + label em Cinzel
- Estado ativo: glow dourado + texto gold-light

### Town Crier Ticker
- Barra horizontal entre o conteúdo e o bottom nav
- Ícone de megafone + texto em IM Fell English italic
- Anuncia eventos do servidor (torneios, manutenções)

---

### Tabuleiro (Tabuleiro.dc.html)

Layout 3 colunas, tela cheia (100vh):

#### Left Rail (222px)
- **Leader card oponente** (topo): Card 70x92 com leve rotação (-4deg), nome, ícone estrela
- **Painel oponente:** Avatar circular, nome, facção, cards na mão (contagem), gems de rounds won, score circular (gold badge 48px)
- **Weather zone:** Box com 3 slots (Frost/Fog/Rain), ícones SVG, texto do efeito ativo
- **Botão PASSAR:** Estilo taverna, label "PASSAR", hint "[ESPAÇO] segure"
- **Painel jogador:** Mesmo layout do oponente, com gold glow pulsante quando é sua vez (`tb-turn`)
- **Leader card jogador** (fundo): Card 70x92 com rotação (+4deg)

#### Center Board
- **Mão do oponente** (topo): Cards face-down em fan horizontal (rotações individuais, overlap)
- **3 rows do oponente** (siege → ranged → close, de cima para baixo):
  - Score badge circular (left absolute, -14px)
  - Horn slot (34x56)
  - Cards area (flex, gap 6px, cards 56x82 com power gem + art background)
  - Right cap (46x56, slot vazio)
- **Divider central:** Linha horizontal dourada + medalha circular com ícone de espadas cruzadas
- **3 rows do jogador** (close → ranged → siege, de cima para baixo): Mesmo layout, scores com gem diferente
- **Mão do jogador** (fundo): Cards face-up em fan com hover lift, label "Sua mão · N", cada card mostra power + nome

#### Right Rail (96px)
- **Oponente** (topo): Label, deck stack (50x64, contagem no badge), graveyard stack (ícone cemitério)
- **Jogador** (fundo): Deck stack, graveyard stack, label "Você"

#### Control Bar (bottom overlay)
- Botões: Desistir (flag icon), Ampliar (zoom icon), Selecionar (enter icon)
- Gradiente fade from bottom, pointer-events: none no container (auto nos botões)

---

## Implementation Plan — MVP

### Phase 1: Auth + Navigation Shell

**Objetivo:** Login funcional e navegação entre telas.

1. **Login/Register page** — Form funcional com tema medieval (já scaffolded)
2. **Lobby page** — Create game + Join game por ID (já scaffolded)
3. **Router + ProtectedRoute** — Redirect para /login se sem JWT (já scaffolded)

**Backend endpoints usados:**
- `POST /authenticate` (login → JWT)
- `POST /api/auth/register` (registro)
- `POST /api/games` (criar partida)
- `POST /api/games/{id}/join` (entrar na partida)

### Phase 2: Game Board — Layout Estático

**Objetivo:** Renderizar o tabuleiro com dados mockados, sem interação.

1. **Board layout** — 3 colunas (left rail, center, right rail) em CSS Grid
2. **BoardRow component** — Score badge + horn slot + cards area + right cap
3. **Card component** — Versão simplificada: strength gem + nome + cor por tipo (hero/normal)
4. **CardBack component** — Verso de carta para mão do oponente
5. **PlayerPanel component** — Avatar, nome, facção, score circular, rounds won gems
6. **LeaderCard component** — Card do líder com rotação
7. **Hand component** — Fan de cartas (player face-up, opponent face-down)

### Phase 3: Game Board — Interação Real

**Objetivo:** Partida jogável end-to-end via WebSocket.

1. **Conectar WebSocket** — `useWebSocket` hook subscribes to `/topic/games/{id}` (já scaffolded)
2. **Identity resolution** — Determinar "meu lado" comparando email do JWT com `playerId`
3. **Play card** — Click na carta da mão → `sendCommand({ commandType: 'PLAY_CARD', ... })`
4. **Pass** — Click no botão PASSAR → `sendCommand({ commandType: 'PASS' })`
5. **Leader ability** — Click no leader card → `sendCommand({ commandType: 'USE_LEADER' })`
6. **Mulligan phase** — UI para selecionar cartas para redraw no início do jogo
7. **Round transitions** — Mostrar resultado do round, scores reset, novo round
8. **Game over** — Tela de vitória/derrota

### Phase 4: Polish

**Objetivo:** Aproximar do protótipo visual.

1. **Weather effects** — Indicador visual no left rail
2. **Turn indicator** — Glow pulsante no painel do jogador ativo
3. **Score animations** — Transições suaves nos números
4. **Card hover** — Lift effect na mão do jogador
5. **Responsive** — Garantir que funciona em telas 1280px+

### Fora do MVP (futuro)

- Hub completo (Taverna com quests, Forja de Baralhos, Mercador, Salão da Glória, Brasão, Ajustes)
- Matchmaking automático (modal de busca com spinner)
- Top HUD com currencies e perfil
- Bottom nav com 5 tabs
- Town crier ticker
- Card art real (imagens)
- Animações avançadas (flip 3D, particles)
- Sound effects
- i18n
- Mobile/tablet support

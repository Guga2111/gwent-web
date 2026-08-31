import { useState, useEffect } from 'react'
import { Pencil } from 'lucide-react'
import { createGame, joinGame } from '@/api/game'
import { getUserDecks } from '@/api/deck'
import { getCardsByFaction } from '@/api/catalog'
import type { CatalogCardDto } from '@/types/deck'
import { getCardArtUrl } from '@/components/board/card/cardArt'
import MesaPrivadaModal from '@/components/hub/MesaPrivadaModal'
import MatchmakingModal from '@/components/hub/MatchmakingModal'
import DeckPickerModal from '@/components/hub/DeckPickerModal'
import type { DeckDto } from '@/types/deck'
import { getFactionConfig } from '@/utils/factionConfig'
import { useHubStore } from '@/stores/hubStore'
import { useMatchmakingStore } from '@/stores/matchmakingStore'
import { joinMatchmakingQueue, leaveMatchmakingQueue } from '@/api/matchmaking'

const deckFan = [
  { rot: -2, x: 66, y: 10, z: 1 },
  { rot: 4, x: 104, y: 8, z: 2 },
  { rot: 9, x: 146, y: 10, z: 3 },
  { rot: 14, x: 190, y: 16, z: 4 },
  { rot: 18, x: 232, y: 24, z: 5 },
]

export default function Taverna() {
  const [modalOpen, setModalOpen] = useState(false)
  const [pickerOpen, setPickerOpen] = useState(false)
  const [matchmakingOpen, setMatchmakingOpen] = useState(false)
  const setTab = useHubStore((s) => s.setTab)
  const [decks, setDecks] = useState<DeckDto[]>([])
  const [activeDeck, setActiveDeck] = useState<DeckDto | null>(null)
  const [leaderCard, setLeaderCard] = useState<CatalogCardDto | null>(null)
  const config = getFactionConfig(activeDeck?.faction ?? null)
  const matchmakingError = useMatchmakingStore((s) => s.error)

  function selectDeck(deck: DeckDto) {
    setActiveDeck(deck)
    useHubStore.getState().setActiveDeck(deck)
    setLeaderCard(null)
    getCardsByFaction(deck.faction)
      .then((cards) => setLeaderCard(cards.find(c => c.id === deck.leaderId) ?? null))
      .catch(() => {})
  }

  useEffect(() => {
    getUserDecks().then((fetched) => {
      setDecks(fetched)
      if (fetched.length > 0) selectDeck(fetched[0])
    }).catch(() => {})
  }, [])

  useEffect(() => {
    return () => { useMatchmakingStore.getState().reset() }
  }, [])

  async function handleCreateGame(deckId: string): Promise<string> {
    const { gameId } = await createGame(deckId)
    return gameId
  }

  async function handleJoinGame(code: string, deckId: string): Promise<void> {
    await joinGame(code, deckId)
  }

  async function handleSearchOpponent() {
    if (!activeDeck) return
    try {
      const matchedGameId = await joinMatchmakingQueue(activeDeck.id)
      if (matchedGameId) {
        useMatchmakingStore.getState().setFound(matchedGameId)
      } else {
        useMatchmakingStore.getState().setSearching()
      }
      setMatchmakingOpen(true)
    } catch (err: any) {
      if (err?.response?.status === 409) {
        useMatchmakingStore.getState().setSearching()
        setMatchmakingOpen(true)
      }
    }
  }

  async function handleCancelMatchmaking() {
    await leaveMatchmakingQueue().catch(() => {})
    useMatchmakingStore.getState().reset()
    setMatchmakingOpen(false)
  }

  return (
    <div className="absolute inset-0 flex flex-col items-center justify-center text-center gap-[5px]">
      
      {/* Faction shield */}
      <div className="taverna-shield-outer relative my-1.5 flex items-center justify-center">
        <div
          className="flex items-center justify-center"
          style={{
            width: 76,
            height: 92,
            clipPath: 'polygon(0 0, 100% 0, 100% 64%, 50% 100%, 0 64%)',
            background: `linear-gradient(180deg, var(${config.secondaryVar}), color-mix(in srgb, var(${config.secondaryVar}) 60%, black) 70%)`,
          }}
        >
          {/* Sword icon */}
          <div className="relative" style={{ width: 14, height: 52, marginTop: -4 }}>
            <div
              className="absolute left-1/2 top-0 -translate-x-1/2 rounded-sm"
              style={{ width: 5, height: 38, background: 'linear-gradient(180deg, var(--gold-light), var(--gold))' }}
            />
            <div
              className="absolute left-1/2 top-2 -translate-x-1/2 rounded-sm bg-[var(--gold-light)]"
              style={{ width: 22, height: 4 }}
            />
            <div
              className="absolute left-1/2 top-px -translate-x-1/2 rounded-full bg-[var(--gold-light)]"
              style={{ width: 9, height: 9 }}
            />
          </div>
        </div>
      </div>

      {/* Faction label */}
      <div
        className="relative text-[11px] tracking-[3px] uppercase font-bold"
        style={{ color: activeDeck ? `var(${config.accentColor})` : 'var(--text-muted)' }}
      >
        {activeDeck ? config.label : 'Sem baralho'}
      </div>

      {/* Mode banner */}
      <div className="taverna-mode-banner relative my-2.5 px-[30px] py-1.5">
        <span className="font-heading font-bold text-[11.5px] tracking-[3px] text-[var(--bg-darkest)]">
          PARTIDA RANQUEADA
        </span>
      </div>

      {/* Primary CTA */}
      <button
        className="taverna-cta-btn relative mt-1.5 px-14 py-5 rounded-[11px] border-none cursor-pointer"
        style={!activeDeck ? { opacity: 0.5, cursor: 'not-allowed' } : undefined}
        onClick={handleSearchOpponent}
      >
        {/* Corner diamonds */}
        {[
          { top: 7, left: 7 },
          { top: 7, right: 7 },
          { bottom: 7, left: 7 },
          { bottom: 7, right: 7 },
        ].map((pos, i) => (
          <div
            key={i}
            className="taverna-diamond absolute w-2 h-2 bg-[var(--bg-darkest)]"
            style={pos}
          />
        ))}
        <div className="flex items-center gap-[15px] text-[var(--bg-darkest)]">
          <svg viewBox="0 0 24 24" className="w-[30px] h-[30px]" fill="none" stroke="currentColor" strokeWidth={1.8} strokeLinecap="round" strokeLinejoin="round">
            <polyline points="14.5 17.5 3 6 3 3 6 3 17.5 14.5" />
            <line x1="13" y1="19" x2="19" y2="13" />
            <line x1="16" y1="16" x2="20" y2="20" />
            <line x1="19" y1="21" x2="21" y2="19" />
            <polyline points="14.5 6.5 18 3 21 3 21 6 17.5 9.5" />
            <line x1="5" y1="14" x2="9" y2="18" />
            <line x1="7" y1="17" x2="4" y2="20" />
            <line x1="3" y1="19" x2="5" y2="21" />
          </svg>
          <span className="font-heading taverna-cta-label text-[25px] font-bold tracking-[1.5px]">
            PROCURAR OPONENTE
          </span>
        </div>
      </button>

      {/* Matchmaking error */}
      {matchmakingError && (
        <p className="font-body italic text-[12.5px] text-[var(--red)]">{matchmakingError}</p>
      )}

      {/* Win streak + amistosa link */}
      <div className="relative flex items-center gap-4 mt-3.5">
        <span className="font-body italic text-[13.5px] text-[var(--text-muted)]">
          sequência de - vitórias
        </span>
        <span className="w-1 h-1 rounded-full bg-[var(--border-gold)]" />
        <span className="font-body italic text-[13.5px] text-[var(--text-muted)]">
          320 PR até o Ouro
        </span>
        <span className="w-1 h-1 rounded-full bg-[var(--border-gold)]" />
        <button
          onClick={() => setModalOpen(true)}
          className="font-body bg-transparent border-none cursor-pointer italic text-[13.5px] underline underline-offset-[3px] text-[var(--blue)]"
        >
          trocar tapas numa amistosa
        </button>
      </div>

      {/* Bottom left: Active deck preview */}
      <div className="absolute left-6 bottom-6 text-left">
        <div className="flex items-center gap-[9px] mb-[9px]">
          <span className="font-heading font-semibold text-[10.5px] tracking-[2.5px] uppercase text-[var(--gold)]">
            Seu baralho
          </span>
          <button
            onClick={() => setPickerOpen(true)}
            className="font-body italic text-[12.5px] text-[var(--text-muted)] bg-transparent border-none cursor-pointer p-0 hover:text-[var(--text-primary)] transition-colors"
          >
            {activeDeck
              ? `${activeDeck.name} · ${activeDeck.cards.reduce((sum, e) => sum + e.quantity, 0)}`
              : 'Nenhum baralho'}
          </button>
          <button
            onClick={() => setTab?.('deck')}
            className="taverna-edit-btn flex items-center gap-[5px] px-[11px] py-1 rounded-[5px] text-[11px] font-semibold tracking-[.5px] border-none cursor-pointer text-[var(--gold-light)]"
          >
            <Pencil size={12} strokeWidth={2} />
            Editar
          </button>
        </div>
        <div className="relative h-[140px] w-[330px]">
          {/* Card fan */}
          {deckFan.map((card, i) => (
            <div
              key={i}
              className="taverna-card-fan-card absolute flex items-center justify-center rounded-[7px] border-2 border-[var(--gold)]"
              style={{
                bottom: card.y,
                left: card.x,
                background: `linear-gradient(160deg, var(${config.secondaryVar}), color-mix(in srgb, var(${config.secondaryVar}) 60%, black) 70%)`,
                transform: `rotate(${card.rot}deg)`,
                zIndex: card.z,
              }}
            >
              <div className="taverna-card-fan-diamond w-[13px] h-[13px] rotate-45" />
            </div>
          ))}
          {/* Leader card */}
          <div className="taverna-leader-card absolute left-0 bottom-0 rounded-[8px] p-[3px] z-[6]">
            <div
              className="relative w-full h-full rounded-[6px] overflow-hidden flex items-start justify-center"
              style={{ background: `linear-gradient(180deg, var(${config.secondaryVar}), color-mix(in srgb, var(${config.secondaryVar}) 60%, black) 70%)` }}
            >
              {leaderCard && (
                <img
                  src={getCardArtUrl(leaderCard.id, leaderCard.faction)}
                  alt=""
                  onError={(e) => { (e.target as HTMLImageElement).style.display = 'none' }}
                  className="card-art-img"
                />
              )}
              <div className="taverna-leader-overlay absolute left-0 right-0 bottom-0 px-1.5 py-[5px] text-center">
                <div className="font-body italic text-[8px] text-[var(--gold)]">
                  {config.label}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Bottom right: Quests */}
      <div className="taverna-quests-card absolute right-6 bottom-[10px] w-[316px] text-left px-[19px] pt-[17px] pb-[14px] rounded-sm text-[var(--parchment-text)]">
        {/* Seal */}
        <div className="taverna-quest-seal absolute -top-[13px] left-1/2 -translate-x-1/2 w-[34px] h-[34px] flex items-center justify-center">
          <div className="taverna-quest-seal__icon w-[10px] h-[10px] rotate-45" />
        </div>

        <div className="font-heading font-bold text-[12.5px] tracking-[1.5px] uppercase text-center text-[var(--parchment-heading)] mt-1 mb-[11px]">
          Encomendas do Taverneiro
        </div>

        <div className="flex flex-col gap-[10px]">
          {[
            { text: 'Vença 3 duelos ranqueados', progress: 'I / III', pct: '33%', reward: '50' },
            { text: 'Baixe 10 unidades na mesa', progress: 'VI / X', pct: '60%', reward: '25' },
            { text: 'Despache um jogador de Monstros', progress: '0 / I', pct: '0%', reward: '100' },
          ].map((quest, i) => (
            <div key={i}>
              {i > 0 && <div className="parchment-separator h-px mb-[10px]" />}
              <div className="flex justify-between items-baseline">
                <span className="font-body text-sm text-[var(--parchment-text)]">
                  {quest.text}
                </span>
                <span className="font-heading font-bold text-[11.5px] text-[var(--parchment-accent)]">
                  {quest.progress}
                </span>
              </div>
              <div className="flex items-center gap-[7px] mt-1">
                <div className="taverna-quest-track flex-1 h-[5px] rounded-[3px] overflow-hidden">
                  <div className="taverna-quest-fill h-full" style={{ width: quest.pct }} />
                </div>
                <span className="text-[10.5px] font-bold text-[var(--parchment-accent)]">
                  &#x2B26; {quest.reward}
                </span>
              </div>
            </div>
          ))}
        </div>

        <div className="font-body italic text-[10.5px] text-right text-[var(--parchment-muted)] mt-2.5">
          novas encomendas ao raiar do dia · 06:42
        </div>
      </div>

      <DeckPickerModal
        open={pickerOpen}
        decks={decks}
        activeDeckId={activeDeck?.id ?? null}
        onSelect={(deck) => { selectDeck(deck); setPickerOpen(false) }}
        onClose={() => setPickerOpen(false)}
      />

      <MesaPrivadaModal
        open={modalOpen}
        onClose={() => setModalOpen(false)}
        onCreateGame={handleCreateGame}
        onJoinGame={handleJoinGame}
        defaultDeckId={activeDeck?.id ?? null}
      />

      <MatchmakingModal open={matchmakingOpen} onCancel={handleCancelMatchmaking} />
    </div>
  )
}

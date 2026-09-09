import { useState } from 'react'
import { ArrowLeftRight, Pencil } from 'lucide-react'
import { createGame, joinGame } from '@/api/game'
import LeaderHubCard from '@/components/hub/tavern/LeaderHubCard'
import MesaPrivadaModal from '@/components/hub/tavern/MesaPrivadaModal'
import MatchmakingModal from '@/components/hub/tavern/MatchmakingModal'
import DeckPickerModal from '@/components/hub/tavern/DeckPickerModal'
import ModeToggle from '@/components/hub/tavern/ModeToggle'
import { getFactionConfig } from '@/utils/factionConfig'
import CardBack from '@/components/board/card/CardBack'
import { useHubStore } from '@/stores/hubStore'
import { useMatchmakingStore } from '@/stores/matchmakingStore'
import { useTavernaData } from '@/hooks/useTavernaData'
import { useMatchmaking } from '@/hooks/useMatchmaking'
import { Button } from '@/components/ui/button'
import { Tooltip, TooltipTrigger, TooltipContent } from '@/components/ui/tooltip'
import QuestsCard from '@/components/hub/tavern/QuestsCard'

const deckFan = [
  { rot: -10, x: 92, y: 2, z: 1 },
  { rot: -5, x: 130, y: 10, z: 2 },
  { rot: 0, x: 168, y: 14, z: 3 },
  { rot: 5, x: 206, y: 12, z: 4 },
  { rot: 10, x: 244, y: 6, z: 5 },
]

export default function Taverna() {
  const [modalOpen, setModalOpen] = useState(false)
  const [pickerOpen, setPickerOpen] = useState(false)
  const [gameMode, setGameMode] = useState<'matchmaking' | 'friendly'>('matchmaking')
  const setActiveTab = useHubStore((s) => s.setActiveTab)
  const matchmakingError = useMatchmakingStore((s) => s.error)

  const { decks, activeDeck, leaderCard, selectDeck } = useTavernaData()
  const { matchmakingOpen, searchOpponent, cancelMatchmaking } = useMatchmaking(activeDeck?.id ?? null)

  const config = getFactionConfig(activeDeck?.faction ?? null)

  async function handleCreateGame(deckId: string): Promise<string> {
    const { gameId } = await createGame(deckId)
    return gameId
  }

  async function handleJoinGame(code: string, deckId: string): Promise<void> {
    await joinGame(code, deckId)
  }

  return (
    <div className="absolute inset-0 flex flex-col items-center justify-center text-center gap-[5px]">

      {/* Game mode tabs */}
      <ModeToggle
        items={[
          { value: 'matchmaking', label: 'MATCHMAKING' },
          { value: 'friendly', label: 'AMISTOSA' },
        ]}
        value={gameMode}
        onChange={setGameMode}
      />

      {/* Faction shield */}
      <div className="taverna-shield-outer relative mt-10 mb-1.5 flex items-center justify-center">
        <div
          className="flex items-center justify-center w-[76px] h-[92px] [clip-path:polygon(0_0,100%_0,100%_64%,50%_100%,0_64%)]"
          style={{ background: `linear-gradient(180deg, var(${config.secondaryVar}), color-mix(in srgb, var(${config.secondaryVar}) 60%, black) 70%)` }}
        >
          {/* Faction icon */}
          <div className="relative w-3.5 h-[52px] -mt-1">
            {/* TODO: Add the faction symbol here */}
            {config.Emblem && (
              <div className="card-back-circle" style={{ color: config.tokens.primary }}>
                <config.Emblem />
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Faction label */}
      <div
        className={`relative text-[11px] tracking-[3px] uppercase font-bold ${activeDeck ? `text-${config.accentColor}` : 'text-text-muted'}`}
      >
        {activeDeck ? config.label : 'Sem baralho'}
      </div>

      {/* Mode banner */}
      <div className="taverna-mode-banner relative my-2.5 px-[30px] py-1.5">
        <span className="font-heading font-bold text-[11.5px] tracking-[3px] text-bg-darkest">
          {gameMode === 'matchmaking' ? 'MATCHMAKING' : 'PARTIDA AMISTOSA'}
        </span>
      </div>

      {/* Primary CTA */}
      <Button
        className="relative mt-1.5 px-12 py-9 rounded-xl min-w-113"
        disabled={!activeDeck}
        onClick={gameMode === 'friendly' ? () => setModalOpen(true) : searchOpponent}
        variant="cta"
      >
        <div className="flex items-center gap-[15px] text-bg-darkest">
          <span className="font-heading taverna-cta-label text-[25px] font-bold tracking-[1.5px]">
            {gameMode === 'friendly' ? 'CRIAR PARTIDA' : 'PROCURAR OPONENTE'}
          </span>
        </div>
      </Button>

      {/* Matchmaking error */}
      {matchmakingError && (
        <p className="font-bodytext-[12.5px] text-red">{matchmakingError}</p>
      )}



      {/* Bottom left: Active deck preview */}
      <div className="absolute left-6 bottom-6 text-left">
        <span className="font-heading font-semibold text-[10.5px] tracking-[2.5px] uppercase text-gold">
          Seu baralho
        </span>
        <div className="flex items-center gap-2 mt-1.5 mb-1">
          <Tooltip>
            <TooltipTrigger asChild>
              <Button
                variant="outline"
                className="flex items-center gap-2 px-3 py-1.5 rounded-lg min-w-40"
                onClick={() => setPickerOpen(true)}
              >
                <ArrowLeftRight data-icon="inline-start" />
                {activeDeck && (
                  <div
                    className={`size-2.5 rounded-full flex-shrink-0 bg-${config.accentColor}`}
                  />
                )}
                <span className="text-[12.5px] font-semibold tracking-wide">
                  {activeDeck
                    ? `${activeDeck.name} · ${activeDeck.cards.reduce((sum, e) => sum + e.quantity, 0)}`
                    : 'Nenhum baralho'}
                </span>
              </Button>
            </TooltipTrigger>
            <TooltipContent side="top">Trocar baralho</TooltipContent>
          </Tooltip>
          <Button
            onClick={() => setActiveTab('deck')}
            className="flex items-center gap-2 px-3 py-1.5 rounded-lg"
            variant="outline"
          >
            <Pencil data-icon="inline-start" />
            Editar
          </Button>
        </div>
        <div className="relative h-[160px] w-[390px]">
          {/* Card fan */}
          {deckFan.map((card, i) => (
            <div
              key={i}
              className="taverna-card-fan-card absolute"
              style={{
                bottom: card.y,
                left: card.x,
                transform: `rotate(${card.rot}deg)`,
                zIndex: card.z,
              }}
            >
              <CardBack faction={activeDeck?.faction ?? undefined} />
            </div>
          ))}
          <LeaderHubCard leader={leaderCard} />
        </div>
      </div>

      {/* Bottom right: Quests */}
      <QuestsCard />

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

      <MatchmakingModal open={matchmakingOpen} onCancel={cancelMatchmaking} />
    </div>
  )
}

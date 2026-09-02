import { useEffect } from "react";
import { createPortal } from "react-dom";
import { useParams, useNavigate } from "react-router-dom";
import { useGameStore } from "@/stores/gameStore";
import { useAuthStore } from "@/stores/authStore";
import { useWebSocket } from "@/hooks/useWebSocket";
import { surrender } from "@/api/game";

import { useCardSelection } from "@/hooks/useCardSelection";
import { useFlyingCard } from "@/hooks/useFlyingCard";
import { useGameActions } from "@/hooks/useGameActions";
import { useRevealedCards } from "@/hooks/useRevealedCards";
import { useErrorAutoDismiss } from "@/hooks/useErrorAutoDismiss";
import { useTurnCountdown } from "@/hooks/useTurnCountdown";

import PlayerPanel from "@/components/board/rail/PlayerPanel";
import LeaderCard from "@/components/board/card/LeaderCard";
import WeatherZone from "@/components/board/rail/WeatherZone";
import PassButton from "@/components/board/rail/PassButton";
import BoardRow from "@/components/board/row/BoardRow";
import Hand from "@/components/board/row/Hand";
import DeckStack from "@/components/board/rail/DeckStack";
import GraveyardStack from "@/components/board/rail/GraveyardStack";
import CentralDivider from "@/components/board/row/CentralDivider";
import ControlBar from "@/components/board/controls/ControlBar";
import CardDetailPanel from "@/components/board/card/CardDetailPanel";
import MulliganOverlay from "@/components/board/overlays/MulliganOverlay";
import RoundEndOverlay from "@/components/board/overlays/RoundEndOverlay";
import GameOverOverlay from "@/components/board/overlays/GameOverOverlay";
import MedicOverlay from "@/components/board/overlays/MedicOverlay";
import LeaderOverlay from "@/components/board/overlays/LeaderOverlay";
import RevealedCardsOverlay from "@/components/board/overlays/RevealedCardsOverlay";
import ScoiataelOverlay from "@/components/board/overlays/ScoiataelOverlay";
import TurnCountdown from "@/components/board/overlays/TurnCountdown";
import DisconnectedBanner from "@/components/board/overlays/DisconnectedBanner";
import FlyingCard from "@/components/board/card/FlyingCard";

export default function Game() {
  const { gameId } = useParams<{ gameId: string }>();
  const navigate = useNavigate();

  const gameState = useGameStore((s) => s.gameState);
  const connected = useGameStore((s) => s.connected);
  const error = useGameStore((s) => s.error);
  const setGameId = useGameStore((s) => s.setGameId);
  const setError = useGameStore((s) => s.setError);
  const opponentConnected = useGameStore((s) => s.opponentConnected);
  const forfeitDeadlineUtc = useGameStore((s) => s.forfeitDeadlineUtc);
  const reset = useGameStore((s) => s.reset);
  const playerId = useAuthStore((s) => s.user)?.email;

  const { sendCommand } = useWebSocket(gameId ?? null);

  useEffect(() => {
    if (!gameId) return;
    setGameId(gameId);
    return () => reset();
  }, [gameId]);

  useErrorAutoDismiss();

  const me = gameState?.me;
  const opponent = gameState?.opponent;

  const {
    selectedCardId, selectedCard, inspectedBoardCard, inspectedLeader,
    selectHandCard, inspectHandCard, inspectBoardCard, inspectLeader,
    clearSelection, clearInspectedBoardCard, clearInspectedLeader,
  } = useCardSelection(me?.hand ?? []);

  const { flyingCard, landedCardId, launchCard, handleFlightComplete } =
    useFlyingCard(me, opponent, error);

  const {
    isMyTurn, canInteract, playCard, playWeatherCard, confirmPlay,
    pass, useLeader, canPlayOnRow,
  } = useGameActions(sendCommand, gameState, selectedCard, selectedCardId, clearSelection, launchCard);

  const { showRevealedCards, dismissRevealedCards } = useRevealedCards(gameState?.revealedCards);
  const { remainingPct, remainingSeconds, isUrgent } = useTurnCountdown(gameState?.turnDeadlineUtc ?? null);

  const isWeatherCard = selectedCard?.cardType === 'WEATHER';

  if (!connected || !gameState || !me || !opponent) {
    return (
      <div className="flex flex-col items-center justify-center h-screen gap-4 bg-bg-darkest">
        <p
          className="text-text-secondary font-heading"
        >
          {!connected ? "Conectando..." : "Aguardando estado do jogo..."}
        </p>
        <button
          onClick={() => navigate("/hub")}
          className="text-sm underline text-text-muted bg-transparent border-none cursor-pointer"
        >
          Voltar à Taverna
        </button>
      </div>
    );
  }

  return (
    <div className="flex h-screen game-table">
      {/* Left Rail */}
      <div className="flex flex-col bg-(--bg-dark)/90 border-r border-(--border-subtle) overflow-hidden w-84">
        <LeaderCard
          leader={opponent.leader}
          leaderUsed={opponent.leaderUsed}
          onClick={() => inspectLeader(opponent.leader, 'opponent')}
          side="top"
        />
        <PlayerPanel player={opponent} isActive={!isMyTurn} side="top" />
        <div className="flex-1" />
        <WeatherZone
          weatherEffects={gameState.weatherCards.map((c) => c.ability ?? "")}
          isTargeting={isMyTurn && !!isWeatherCard}
          targetAbility={isWeatherCard ? selectedCard!.ability : null}
          onSlotClick={playWeatherCard}
        />
        <PassButton
          onClick={pass}
          disabled={!isMyTurn || me.passed}
        />
        <div className="flex-1" />
        <PlayerPanel player={me} isActive={isMyTurn} side="bottom" />
        <LeaderCard
          leader={me.leader}
          leaderUsed={me.leaderUsed}
          onClick={() => inspectLeader(me.leader, 'player')}
          side="bottom"
        />
      </div>

      {/* Center Board */}
      <div className="flex flex-col relative overflow-hidden px-10 flex-1">
          {/* Error notification */}
          {error && (
            <div
              className="absolute top-3 left-1/2 -translate-x-1/2 z-20 rounded-md px-4.5 py-2 text-[13px] text-white pointer-events-none border border-[rgba(255,100,100,0.4)] font-ui"
              style={{
                backgroundColor: "rgba(204, 68, 68, 0.92)",
              }}
            >
              {error}
            </div>
          )}

          <DisconnectedBanner opponentConnected={opponentConnected} forfeitDeadlineUtc={forfeitDeadlineUtc} />

          {/* Opponent hand (face-down) */}
          <Hand
            opponentHandSize={opponent.handSize}
            isPlayer={false}
            interactive={false}
            faction={opponent.leader.faction}
          />

          {/* Board mat */}
          <div className="board-mat flex flex-col flex-1 min-h-0">
            <BoardRow row={opponent.siegeRow} rowLabel="Cerco" rowType="SIEGE" side="opponent" interactive={false} onInspectCard={inspectBoardCard} />
            <BoardRow row={opponent.rangedRow} rowLabel="Distância" rowType="RANGED" side="opponent" interactive={false} onInspectCard={inspectBoardCard} />
            <BoardRow row={opponent.meleeRow} rowLabel="Corpo" rowType="MELEE" side="opponent" interactive={false} onInspectCard={inspectBoardCard} />

            <CentralDivider turnRemainingPct={remainingPct} isMyTurn={isMyTurn} isUrgent={isUrgent} />

            <BoardRow
              row={me.meleeRow} rowLabel="Corpo" rowType="MELEE" side="player"
              interactive={canInteract} isPlacementTarget={canPlayOnRow("MELEE")}
              onRowClick={canPlayOnRow("MELEE") ? () => playCard("MELEE") : undefined}
              onInspectCard={inspectBoardCard} suppressEnterCardId={landedCardId}
            />
            <BoardRow
              row={me.rangedRow} rowLabel="Distância" rowType="RANGED" side="player"
              interactive={canInteract} isPlacementTarget={canPlayOnRow("RANGED")}
              onRowClick={canPlayOnRow("RANGED") ? () => playCard("RANGED") : undefined}
              onInspectCard={inspectBoardCard} suppressEnterCardId={landedCardId}
            />
            <BoardRow
              row={me.siegeRow} rowLabel="Cerco" rowType="SIEGE" side="player"
              interactive={canInteract} isPlacementTarget={canPlayOnRow("SIEGE")}
              onRowClick={canPlayOnRow("SIEGE") ? () => playCard("SIEGE") : undefined}
              onInspectCard={inspectBoardCard} suppressEnterCardId={landedCardId}
            />
          </div>

          <TurnCountdown remainingSeconds={remainingSeconds} isMyTurn={isMyTurn} />

          {/* Player hand */}
          <Hand
            cards={me.hand}
            isPlayer
            interactive={canInteract}
            selectedCardId={selectedCardId ?? undefined}
            departingCardId={flyingCard?.card.id}
            onCardClick={(cardId) => {
              if (isMyTurn) {
                selectHandCard(cardId);
              } else {
                const card = me.hand.find((c) => c.id === cardId) ?? null;
                if (card) inspectHandCard(card);
              }
            }}
          />

          {/* Phase overlays */}
          {gameState.pendingAbility === "SCOIATAEL_FIRST_PLAYER_CHOICE" && isMyTurn && (
            <ScoiataelOverlay
              abilityDeadlineUtc={gameState.abilityDeadlineUtc}
              onChoose={(goFirst) => {
                sendCommand({
                  commandType: "RESOLVE_SCOIATAEL",
                  playerId,
                  chosenPlayerId: goFirst ? me.playerId : opponent.playerId,
                });
              }}
            />
          )}
          {gameState.phase === "REDRAW" && !me.mulliganConfirmed && (
            <MulliganOverlay
              hand={me.hand}
              mulligansRemaining={me.mulligansRemaining}
              abilityDeadlineUtc={gameState.abilityDeadlineUtc}
              onConfirm={(cardIds) =>
                sendCommand({
                  commandType: "CONFIRM_MULLIGAN",
                  playerId,
                  cardIds,
                })
              }
            />
          )}
          {gameState.phase === "ROUND_END" && (
            <RoundEndOverlay
              round={gameState.currentRound}
              myScore={me.score}
              opponentScore={opponent.score}
            />
          )}
          {gameState.pendingAbility === "MEDIC_CHOICE" && isMyTurn && (
            <MedicOverlay
              graveyard={me.graveyard}
              abilityDeadlineUtc={gameState.abilityDeadlineUtc}
              onSelectCard={(cardId) =>
                sendCommand({ commandType: "RESOLVE_MEDIC", playerId, cardId })
              }
            />
          )}
          {gameState.pendingAbility === "LEADER_GRAVEYARD_PICK" && isMyTurn && (
            <LeaderOverlay
              pendingType="LEADER_GRAVEYARD_PICK"
              cards={me.graveyard}
              abilityDeadlineUtc={gameState.abilityDeadlineUtc}
              onSelectCard={(cardId) =>
                sendCommand({ commandType: "RESOLVE_LEADER", playerId, cardId })
              }
            />
          )}
          {gameState.pendingAbility === "LEADER_OPPONENT_GRAVEYARD_PICK" &&
            isMyTurn && (
              <LeaderOverlay
                pendingType="LEADER_OPPONENT_GRAVEYARD_PICK"
                cards={opponent.graveyard}
                abilityDeadlineUtc={gameState.abilityDeadlineUtc}
                onSelectCard={(cardId) =>
                  sendCommand({
                    commandType: "RESOLVE_LEADER",
                    playerId,
                    cardId,
                  })
                }
              />
            )}
          {gameState.pendingAbility === "LEADER_DECK_PICK" &&
            isMyTurn &&
            gameState.deckCards && (
              <LeaderOverlay
                pendingType="LEADER_DECK_PICK"
                cards={gameState.deckCards}
                abilityDeadlineUtc={gameState.abilityDeadlineUtc}
                onSelectCard={(cardId) =>
                  sendCommand({
                    commandType: "RESOLVE_LEADER",
                    playerId,
                    cardId,
                  })
                }
              />
            )}
          {gameState.pendingAbility === "LEADER_HAND_DISCARD" && isMyTurn && (
            <LeaderOverlay
              pendingType="LEADER_HAND_DISCARD"
              cards={me.hand}
              abilityDeadlineUtc={gameState.abilityDeadlineUtc}
              onSelectCard={(cardId) =>
                sendCommand({ commandType: "RESOLVE_LEADER", playerId, cardId })
              }
            />
          )}
          {showRevealedCards &&
            gameState.revealedCards &&
            gameState.revealedCards.length > 0 && (
              <RevealedCardsOverlay
                cards={gameState.revealedCards}
                onDismiss={dismissRevealedCards}
              />
            )}
          {gameState.phase === "GAME_OVER" && (
            <GameOverOverlay
              myState={me}
              opponentState={opponent}
              winner={gameState.winner}
              disconnectForfeit={gameState.disconnectForfeit}
              onBack={() => navigate("/hub")}
            />
          )}
        </div>

        {/* Right Rail */}
        <div className="flex flex-col items-center bg-(--bg-dark)/90 border-l border-(--border-subtle) py-3 gap-3 w-70 overflow-hidden">
          <div className="flex items-center gap-16 py-11">
            <GraveyardStack count={opponent.graveyard.length} />
            <DeckStack count={opponent.deckSize} label="Deck" faction={opponent.leader.faction} />
          </div>

          <div className="flex-1 flex flex-col items-center justify-center px-3 min-h-0">
            {selectedCard ? (
              <CardDetailPanel
                card={selectedCard}
                onClose={clearSelection}
              />
            ) : inspectedLeader ? (
              <CardDetailPanel
                card={inspectedLeader.card}
                onClose={clearInspectedLeader}
                action={
                  inspectedLeader.side === 'player'
                    ? me.leaderUsed
                      ? { label: 'Ja Utilizado', onClick: () => {}, disabled: true }
                      : {
                          label: 'Usar Habilidade',
                          onClick: () => {
                            useLeader();
                            clearInspectedLeader();
                          },
                          disabled: !isMyTurn,
                        }
                    : undefined
                }
              />
            ) : inspectedBoardCard ? (
              <CardDetailPanel
                card={inspectedBoardCard}
                onClose={clearInspectedBoardCard}
              />
            ) : (
              <ControlBar
                onSurrender={() => gameId && surrender(gameId).catch(() => setError("Falha ao desistir. Tente novamente."))}
                selectedCardId={selectedCardId}
                onConfirmPlay={confirmPlay}
              />
            )}
          </div>

          <div className="flex items-center gap-16 py-11">
            <GraveyardStack count={me.graveyard.length} />
            <DeckStack count={me.deckSize} label="Deck" faction={me.leader.faction} />
          </div>
        </div>

        {/* Flying card overlay */}
        {flyingCard &&
          createPortal(
            <FlyingCard
              card={flyingCard.card}
              fromRect={flyingCard.fromRect}
              toRect={flyingCard.toRect}
              onComplete={handleFlightComplete}
            />,
            document.body
          )}
    </div>
  );
}

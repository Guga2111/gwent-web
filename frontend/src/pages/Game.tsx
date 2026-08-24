import { useEffect, useRef, useState } from "react";
import { createPortal } from "react-dom";
import { useParams, useNavigate } from "react-router-dom";
import { useGameStore } from "@/stores/gameStore";
import { useAuthStore } from "@/stores/authStore";
import { useWebSocket } from "@/hooks/useWebSocket";
import { surrender } from "@/api/game";
import type { CardDto, RowType } from "@/types/game";

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
import FlyingCard from "@/components/board/card/FlyingCard";

export default function Game() {
  const { gameId } = useParams<{ gameId: string }>();
  const navigate = useNavigate();

  const gameState = useGameStore((s) => s.gameState);
  const connected = useGameStore((s) => s.connected);
  const error = useGameStore((s) => s.error);
  const setGameId = useGameStore((s) => s.setGameId);
  const setError = useGameStore((s) => s.setError);
  const reset = useGameStore((s) => s.reset);
  const user = useAuthStore((s) => s.user);

  const [selectedCardId, setSelectedCardId] = useState<string | null>(null);
  const [inspectedBoardCard, setInspectedBoardCard] = useState<CardDto | null>(null);
  const [inspectedLeader, setInspectedLeader] = useState<{ card: CardDto; side: 'player' | 'opponent' } | null>(null);
  const [showRevealedCards, setShowRevealedCards] = useState(false);

  // Flying card animation state
  const [flyingCard, setFlyingCard] = useState<{
    card: CardDto;
    fromRect: DOMRect;
    toRect: DOMRect;
    targetRow: RowType;
  } | null>(null);
  const [landedCardId, setLandedCardId] = useState<string | null>(null);
  const flyingAnimDone = useRef(false);
  const flyingCardInRow = useRef(false);

  // Show revealed cards overlay when backend sends them (EMPEROR_OF_NILFGAARD)
  useEffect(() => {
    if (gameState?.revealedCards && gameState.revealedCards.length > 0) {
      setShowRevealedCards(true);
    }
  }, [gameState?.revealedCards]);

  const { sendCommand } = useWebSocket(gameId ?? null);

  useEffect(() => {
    if (!gameId) return;
    setGameId(gameId);
    return () => reset();
  }, [gameId]);

  // Auto-dismiss errors after 3s
  useEffect(() => {
    if (!error) return;
    const t = setTimeout(() => setError(null), 3000);
    return () => clearTimeout(t);
  }, [error, setError]);

  const me = gameState?.me;
  const opponent = gameState?.opponent;
  const isMyTurn = gameState
    ? gameState.currentTurn === gameState.myTurn
    : false;

  const playerId = user?.email;

  const selectedCard = me?.hand.find((c) => c.id === selectedCardId) ?? null;
  const isWeatherCard = selectedCard?.cardType === 'WEATHER';

  // Clear other inspections when a hand card is selected
  useEffect(() => {
    if (selectedCardId) {
      setInspectedBoardCard(null);
      setInspectedLeader(null);
    }
  }, [selectedCardId]);

  const handleBoardCardClick = (card: CardDto) => {
    if (selectedCardId) return; // hand selection takes priority
    setInspectedLeader(null);
    setInspectedBoardCard((prev) => (prev?.id === card.id ? null : card));
  };

  const handleLeaderClick = (card: CardDto, side: 'player' | 'opponent') => {
    setSelectedCardId(null);
    setInspectedBoardCard(null);
    setInspectedLeader((prev) =>
      prev?.card.id === card.id ? null : { card, side }
    );
  };

  const handlePlayWeatherCard = () => {
    if (!selectedCardId || !isMyTurn) return;
    sendCommand({
      commandType: "PLAY_CARD",
      playerId,
      cardId: selectedCardId,
      targetRow: "MELEE",
    });
    setSelectedCardId(null);
  };

  const handleConfirmPlay = () => {
    if (!selectedCard || !isMyTurn) return;
    if (selectedCard.cardType === "WEATHER") {
      handlePlayWeatherCard();
      return;
    }
    if (selectedCard.ability === "AGILE") return; // user must click a row
    if (selectedCard.rowType) handlePlayCard(selectedCard.rowType);
  };

  const canPlayOnRow = (row: RowType): boolean => {
    if (!selectedCard || !isMyTurn) return false;
    if (selectedCard.ability === "AGILE")
      return row === "MELEE" || row === "RANGED";
    return selectedCard.rowType === row;
  };

  const handlePlayCard = (targetRow: RowType) => {
    if (!selectedCardId || !isMyTurn) return;

    const card = me?.hand.find((c) => c.id === selectedCardId);
    const isSpy = card?.ability === 'SPY';
    const cardEl = document.querySelector(`[data-card-id="${selectedCardId}"]`);
    const rowEl = document.querySelector(
      `[data-row-type="${targetRow}"][data-row-side="${isSpy ? 'opponent' : 'player'}"]`
    );

    if (card && cardEl && rowEl) {
      const fromRect = cardEl.getBoundingClientRect();
      const toRect = rowEl.getBoundingClientRect();
      flyingAnimDone.current = false;
      flyingCardInRow.current = false;
      setFlyingCard({ card, fromRect, toRect, targetRow });
    }

    sendCommand({
      commandType: "PLAY_CARD",
      playerId,
      cardId: selectedCardId,
      targetRow,
    });
    setSelectedCardId(null);
  };

  // Check if the flying card has arrived in row data (WS update)
  useEffect(() => {
    if (!flyingCard || !me || !opponent) return;
    const isSpy = flyingCard.card.ability === 'SPY';
    const source = isSpy ? opponent : me;
    const rowMap: Record<RowType, CardDto[]> = {
      MELEE: source.meleeRow.cards ?? [],
      RANGED: source.rangedRow.cards ?? [],
      SIEGE: source.siegeRow.cards ?? [],
    };
    const inRow = rowMap[flyingCard.targetRow].some(
      (c) => c.id === flyingCard.card.id
    );
    if (inRow) {
      flyingCardInRow.current = true;
      if (flyingAnimDone.current) {
        clearFlyingCard();
      }
    }
  }, [me?.meleeRow, me?.rangedRow, me?.siegeRow, opponent?.meleeRow, opponent?.rangedRow, opponent?.siegeRow, flyingCard]);

  // Also clear on error (backend rejected command) — card stays in hand
  useEffect(() => {
    if (!flyingCard) return;
    if (error) {
      setFlyingCard(null);
    }
  }, [error, flyingCard]);

  const handleFlightComplete = () => {
    flyingAnimDone.current = true;
    if (flyingCardInRow.current) {
      clearFlyingCard();
    }
  };

  const clearFlyingCard = () => {
    const cardId = flyingCard?.card.id ?? null;
    setFlyingCard(null);
    if (cardId) {
      setLandedCardId(cardId);
      setTimeout(() => setLandedCardId(null), 100);
    }
  };

  if (!connected || !gameState || !me || !opponent) {
    return (
      <div className="flex flex-col items-center justify-center h-screen gap-4 bg-[var(--bg-darkest)]">
        <p
          className="text-[var(--text-secondary)]"
          style={{ fontFamily: "var(--font-heading)" }}
        >
          {!connected ? "Conectando..." : "Aguardando estado do jogo..."}
        </p>
        <button
          onClick={() => navigate("/hub")}
          className="text-sm underline text-[var(--text-muted)] bg-transparent border-none cursor-pointer"
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
          onClick={() => handleLeaderClick(opponent.leader, 'opponent')}
          side="top"
        />
        <PlayerPanel player={opponent} isActive={!isMyTurn} side="top" />
        <div className="flex-1" />
        <WeatherZone
          weatherEffects={gameState.weatherCards.map((c) => c.ability ?? "")}
          isTargeting={isMyTurn && !!isWeatherCard}
          targetAbility={isWeatherCard ? selectedCard!.ability : null}
          onSlotClick={handlePlayWeatherCard}
        />
        <PassButton
          onClick={() => sendCommand({ commandType: "PASS", playerId })}
          disabled={!isMyTurn || me.passed}
        />
        <div className="flex-1" />
        <PlayerPanel player={me} isActive={isMyTurn} side="bottom" />
        <LeaderCard
          leader={me.leader}
          leaderUsed={me.leaderUsed}
          onClick={() => handleLeaderClick(me.leader, 'player')}
          side="bottom"
        />
      </div>

      {/* Center Board */}
      <div className="flex flex-col relative overflow-hidden px-10 flex-1">
          {/* Error notification */}
          {error && (
            <div
              className="absolute top-3 left-1/2 -translate-x-1/2 z-20 rounded-md px-4.5 py-2 text-[13px] text-white pointer-events-none border border-[rgba(255,100,100,0.4)]"
              style={{
                fontFamily: "var(--font-ui)",
                backgroundColor: "rgba(204, 68, 68, 0.92)",
              }}
            >
              {error}
            </div>
          )}

          {/* Opponent hand (face-down) */}
          <Hand
            opponentHandSize={opponent.handSize}
            isPlayer={false}
            interactive={false}
            faction={opponent.leader.faction}
          />

          {/* Board mat — leather playing surface */}
          <div className="board-mat flex flex-col flex-1 min-h-0">
            {/* Opponent rows: siege, ranged, melee (top to bottom) */}
            <BoardRow
              row={opponent.siegeRow}
              rowLabel="Cerco"
              rowType="SIEGE"
              side="opponent"
              interactive={false}
              onInspectCard={handleBoardCardClick}
            />
            <BoardRow
              row={opponent.rangedRow}
              rowLabel="Distância"
              rowType="RANGED"
              side="opponent"
              interactive={false}
              onInspectCard={handleBoardCardClick}
            />
            <BoardRow
              row={opponent.meleeRow}
              rowLabel="Corpo"
              rowType="MELEE"
              side="opponent"
              interactive={false}
              onInspectCard={handleBoardCardClick}
            />

            <CentralDivider />

            {/* Player rows: melee, ranged, siege (top to bottom) */}
            <BoardRow
              row={me.meleeRow}
              rowLabel="Corpo"
              rowType="MELEE"
              side="player"
              interactive={isMyTurn}
              isPlacementTarget={canPlayOnRow("MELEE")}
              onRowClick={
                canPlayOnRow("MELEE") ? () => handlePlayCard("MELEE") : undefined
              }
              onInspectCard={handleBoardCardClick}
              suppressEnterCardId={landedCardId}
            />
            <BoardRow
              row={me.rangedRow}
              rowLabel="Distância"
              rowType="RANGED"
              side="player"
              interactive={isMyTurn}
              isPlacementTarget={canPlayOnRow("RANGED")}
              onRowClick={
                canPlayOnRow("RANGED")
                  ? () => handlePlayCard("RANGED")
                  : undefined
              }
              onInspectCard={handleBoardCardClick}
              suppressEnterCardId={landedCardId}
            />
            <BoardRow
              row={me.siegeRow}
              rowLabel="Cerco"
              rowType="SIEGE"
              side="player"
              interactive={isMyTurn}
              isPlacementTarget={canPlayOnRow("SIEGE")}
              onRowClick={
                canPlayOnRow("SIEGE") ? () => handlePlayCard("SIEGE") : undefined
              }
              onInspectCard={handleBoardCardClick}
              suppressEnterCardId={landedCardId}
            />
          </div>

          {/* Player hand */}
          <Hand
            cards={me.hand}
            isPlayer
            interactive={isMyTurn}
            selectedCardId={selectedCardId ?? undefined}
            departingCardId={flyingCard?.card.id}
            onCardClick={(cardId) => {
              if (isMyTurn) {
                setSelectedCardId((prev) => (prev === cardId ? null : cardId));
              } else {
                const card = me.hand.find((c) => c.id === cardId) ?? null;
                if (card) {
                  setInspectedBoardCard((prev) =>
                    prev?.id === card.id ? null : card
                  );
                }
              }
            }}
          />

          {/* Phase overlays */}
          {gameState.phase === "REDRAW" && !me.mulliganConfirmed && (
            <MulliganOverlay
              hand={me.hand}
              mulligansRemaining={me.mulligansRemaining}
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
              onSelectCard={(cardId) =>
                sendCommand({ commandType: "RESOLVE_MEDIC", playerId, cardId })
              }
            />
          )}
          {gameState.pendingAbility === "LEADER_GRAVEYARD_PICK" && isMyTurn && (
            <LeaderOverlay
              pendingType="LEADER_GRAVEYARD_PICK"
              cards={me.graveyard}
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
                onDismiss={() => setShowRevealedCards(false)}
              />
            )}
          {gameState.phase === "GAME_OVER" && (
            <GameOverOverlay
              myState={me}
              opponentState={opponent}
              winner={gameState.winner}
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
                onClose={() => setSelectedCardId(null)}
              />
            ) : inspectedLeader ? (
              <CardDetailPanel
                card={inspectedLeader.card}
                onClose={() => setInspectedLeader(null)}
                action={
                  inspectedLeader.side === 'player'
                    ? me.leaderUsed
                      ? { label: 'Ja Utilizado', onClick: () => {}, disabled: true }
                      : {
                          label: 'Usar Habilidade',
                          onClick: () => {
                            sendCommand({ commandType: "USE_LEADER", playerId });
                            setInspectedLeader(null);
                          },
                          disabled: !isMyTurn,
                        }
                    : undefined
                }
              />
            ) : inspectedBoardCard ? (
              <CardDetailPanel
                card={inspectedBoardCard}
                onClose={() => setInspectedBoardCard(null)}
              />
            ) : (
              <ControlBar
                onSurrender={() => gameId && surrender(gameId).catch(() => {})}
                selectedCardId={selectedCardId}
                onConfirmPlay={handleConfirmPlay}
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

import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { useGameStore } from "@/stores/gameStore";
import { useAuthStore } from "@/stores/authStore";
import { useWebSocket } from "@/hooks/useWebSocket";
import { surrender } from "@/api/game";
import type { RowType } from "@/types/game";

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
import MulliganOverlay from "@/components/board/overlays/MulliganOverlay";
import RoundEndOverlay from "@/components/board/overlays/RoundEndOverlay";
import GameOverOverlay from "@/components/board/overlays/GameOverOverlay";
import MedicOverlay from "@/components/board/overlays/MedicOverlay";
import LeaderOverlay from "@/components/board/overlays/LeaderOverlay";
import RevealedCardsOverlay from "@/components/board/overlays/RevealedCardsOverlay";

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
  const [showRevealedCards, setShowRevealedCards] = useState(false);

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

  const canPlayOnRow = (row: RowType): boolean => {
    if (!selectedCard || !isMyTurn) return false;
    if (selectedCard.ability === "AGILE")
      return row === "MELEE" || row === "RANGED";
    return selectedCard.rowType === row;
  };

  const handlePlayCard = (targetRow: RowType) => {
    if (!selectedCardId || !isMyTurn) return;
    sendCommand({
      commandType: "PLAY_CARD",
      playerId,
      cardId: selectedCardId,
      targetRow,
    });
    setSelectedCardId(null);
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
    <div className="grid grid-cols-[220px_1fr_96px] h-screen bg-[var(--bg-darkest)]">
      {/* Left Rail */}
      <div className="flex flex-col bg-[var(--bg-dark)] border-r border-[var(--border-subtle)] overflow-hidden">
        <LeaderCard
          leaderUsed={opponent.leaderUsed}
          disabled
          onClick={() => {}}
          side="top"
        />
        <PlayerPanel player={opponent} isActive={!isMyTurn} side="top" />
        <div className="flex-1" />
        <WeatherZone
          weatherEffects={gameState.weatherCards.map((c) => c.ability ?? "")}
        />
        <PassButton
          onClick={() => sendCommand({ commandType: "PASS", playerId })}
          disabled={!isMyTurn || me.passed}
        />
        <div className="flex-1" />
        <PlayerPanel player={me} isActive={isMyTurn} side="bottom" />
        <LeaderCard
          leaderUsed={me.leaderUsed}
          disabled={!isMyTurn || me.leaderUsed}
          onClick={() => sendCommand({ commandType: "USE_LEADER", playerId })}
          side="bottom"
        />
      </div>

      {/* Center Board */}
      <div className="flex flex-col relative overflow-hidden px-10">
          {/* Error notification */}
          {error && (
            <div
              className="absolute top-3 left-1/2 -translate-x-1/2 z-20 rounded-md px-[18px] py-2 text-[13px] text-white pointer-events-none border border-[rgba(255,100,100,0.4)]"
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
          />

          {/* Opponent rows: siege, ranged, melee (top to bottom) */}
          <BoardRow
            row={opponent.siegeRow}
            rowLabel="Cerco"
            rowType="SIEGE"
            side="opponent"
            interactive={false}
          />
          <BoardRow
            row={opponent.rangedRow}
            rowLabel="Distância"
            rowType="RANGED"
            side="opponent"
            interactive={false}
          />
          <BoardRow
            row={opponent.meleeRow}
            rowLabel="Corpo"
            rowType="MELEE"
            side="opponent"
            interactive={false}
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
          />

          {/* Player hand */}
          <Hand
            cards={me.hand}
            isPlayer
            interactive={isMyTurn}
            selectedCardId={selectedCardId ?? undefined}
            onCardClick={(cardId) =>
              setSelectedCardId((prev) => (prev === cardId ? null : cardId))
            }
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
        <div className="flex flex-col items-center bg-[var(--bg-dark)] border-l border-[var(--border-subtle)] py-3 gap-3">
          <DeckStack count={opponent.deckSize} label="Deck" />
          <GraveyardStack count={opponent.graveyard.length} />
          <div className="flex-1 flex flex-col items-center justify-center">
            <ControlBar
              onSurrender={() => gameId && surrender(gameId).catch(() => {})}
            />
          </div>
          <GraveyardStack count={me.graveyard.length} />
          <DeckStack count={me.deckSize} label="Deck" />
        </div>
    </div>
  );
}

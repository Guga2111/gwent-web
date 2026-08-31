package com.gwent.api.game;

import com.gwent.api.game.dto.*;
import com.gwent.api.game.exception.CardNotFoundException;
import com.gwent.engine.command.*;
import com.gwent.engine.domain.*;
import com.gwent.engine.state.BoardRow;
import com.gwent.engine.state.GameState;
import com.gwent.engine.state.PlayerState;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.gwent.api.shared.TestDataFactory.*;
import static org.junit.jupiter.api.Assertions.*;

class GameModelMapperTest {

    private final GameModelMapper mapper = new GameModelMapper();

    // ── toGameStateDto ──

    @Test
    void shouldMapCorrectly_forPlayer1Perspective() {
        UUID gameId = UUID.randomUUID();
        SessionContext ctx = makeSessionContext("p1@test.com", "p2@test.com");

        GameStateDto dto = mapper.toGameStateDto(gameId, ctx, Turn.PLAYER_1, 10, 5, null, null, false);

        assertEquals(gameId, dto.gameId());
        assertEquals("PLAYER_1", dto.myTurn());
        assertEquals("p1@test.com", dto.me().playerId());
        assertEquals("p2@test.com", dto.opponent().playerId());
    }

    @Test
    void shouldMapCorrectly_forPlayer2Perspective() {
        UUID gameId = UUID.randomUUID();
        SessionContext ctx = makeSessionContext("p1@test.com", "p2@test.com");

        GameStateDto dto = mapper.toGameStateDto(gameId, ctx, Turn.PLAYER_2, 5, 10, null, null, false);

        assertEquals("PLAYER_2", dto.myTurn());
        assertEquals("p2@test.com", dto.me().playerId());
        assertEquals("p1@test.com", dto.opponent().playerId());
    }

    @Test
    void shouldNullifyTurnDeadline_whenNotInPlayPhase() {
        UUID gameId = UUID.randomUUID();
        SessionContext ctx = makeSessionContext("p1@test.com", "p2@test.com");
        // GameState starts in COIN_FLIP phase

        GameStateDto dto = mapper.toGameStateDto(gameId, ctx, Turn.PLAYER_1, 0, 0, 999L, null, false);

        assertNull(dto.turnDeadlineUtc());
    }

    @Test
    void shouldNullifyTurnDeadline_whenPendingAbility() {
        UUID gameId = UUID.randomUUID();
        SessionContext ctx = makeSessionContext("p1@test.com", "p2@test.com");
        ctx.gameState().setPhase(GamePhase.REDRAW);
        ctx.gameState().setPhase(GamePhase.PLAY);
        ctx.gameState().setPendingAbility(PendingAbility.MEDIC_CHOICE);

        GameStateDto dto = mapper.toGameStateDto(gameId, ctx, Turn.PLAYER_1, 0, 0, 999L, 1000L, false);

        assertNull(dto.turnDeadlineUtc());
    }

    @Test
    void shouldNullifyAbilityDeadline_whenGameOver() {
        UUID gameId = UUID.randomUUID();
        SessionContext ctx = makeSessionContext("p1@test.com", "p2@test.com");
        ctx.gameState().setPhase(GamePhase.REDRAW);
        ctx.gameState().setPhase(GamePhase.PLAY);
        ctx.gameState().finishGame(Turn.PLAYER_1, com.gwent.engine.domain.EndReason.SURRENDER);

        GameStateDto dto = mapper.toGameStateDto(gameId, ctx, Turn.PLAYER_1, 0, 0, null, 999L, false);

        assertNull(dto.abilityDeadlineUtc());
    }

    @Test
    void shouldMapWinner_toCorrectPlayerId() {
        UUID gameId = UUID.randomUUID();
        SessionContext ctx = makeSessionContext("p1@test.com", "p2@test.com");
        // Must transition through valid phases to reach GAME_OVER
        ctx.gameState().setPhase(GamePhase.REDRAW);
        ctx.gameState().setPhase(GamePhase.PLAY);
        ctx.gameState().finishGame(Turn.PLAYER_1, com.gwent.engine.domain.EndReason.SURRENDER);

        GameStateDto dto = mapper.toGameStateDto(gameId, ctx, Turn.PLAYER_1, 0, 0, null, null, false);

        assertEquals("p1@test.com", dto.winner());
    }

    @Test
    void shouldSetWinnerNull_whenNoWinner() {
        UUID gameId = UUID.randomUUID();
        SessionContext ctx = makeSessionContext("p1@test.com", "p2@test.com");

        GameStateDto dto = mapper.toGameStateDto(gameId, ctx, Turn.PLAYER_1, 0, 0, null, null, false);

        assertNull(dto.winner());
    }

    // ── toCommand ──

    @Test
    void shouldMapPassCommand() {
        CommandRequestDto request = new CommandRequestDto(null, CommandType.PASS, null, null, null, null);
        SessionContext ctx = makeSessionContext("p1@test.com", "p2@test.com");

        GameCommand cmd = mapper.toCommand(request, Turn.PLAYER_1, ctx.gameState(), ctx);

        assertInstanceOf(PassCommand.class, cmd);
    }

    @Test
    void shouldMapPlayCardCommand() {
        SessionContext ctx = makeSessionContext("p1@test.com", "p2@test.com");
        // Add a card to hand
        Card handCard = makeCard("play_card", Faction.NORTHERN_REALMS);
        ctx.gameState().getPlayer1().addToHand(handCard);

        CommandRequestDto request = new CommandRequestDto(null, CommandType.PLAY_CARD, "PLAY_CARD", "MELEE", null, null);
        GameCommand cmd = mapper.toCommand(request, Turn.PLAYER_1, ctx.gameState(), ctx);

        assertInstanceOf(PlayCardCommand.class, cmd);
    }

    @Test
    void shouldMapMulliganCommand() {
        SessionContext ctx = makeSessionContext("p1@test.com", "p2@test.com");
        Card handCard = makeCard("mulligan_card", Faction.NORTHERN_REALMS);
        ctx.gameState().getPlayer1().addToHand(handCard);

        CommandRequestDto request = new CommandRequestDto(null, CommandType.MULLIGAN, "MULLIGAN_CARD", null, null, null);
        GameCommand cmd = mapper.toCommand(request, Turn.PLAYER_1, ctx.gameState(), ctx);

        assertInstanceOf(MulliganCommand.class, cmd);
    }

    @Test
    void shouldMapConfirmMulligan_withCards() {
        SessionContext ctx = makeSessionContext("p1@test.com", "p2@test.com");
        Card c1 = makeCard("swap_1", Faction.NORTHERN_REALMS);
        Card c2 = makeCard("swap_2", Faction.NORTHERN_REALMS);
        ctx.gameState().getPlayer1().addToHand(c1);
        ctx.gameState().getPlayer1().addToHand(c2);

        CommandRequestDto request = new CommandRequestDto(null, CommandType.CONFIRM_MULLIGAN, null, null,
                List.of("SWAP_1", "SWAP_2"), null);
        GameCommand cmd = mapper.toCommand(request, Turn.PLAYER_1, ctx.gameState(), ctx);

        assertInstanceOf(ConfirmMulliganCommand.class, cmd);
    }

    @Test
    void shouldMapConfirmMulligan_withNullCardIds() {
        SessionContext ctx = makeSessionContext("p1@test.com", "p2@test.com");

        CommandRequestDto request = new CommandRequestDto(null, CommandType.CONFIRM_MULLIGAN, null, null, null, null);
        GameCommand cmd = mapper.toCommand(request, Turn.PLAYER_1, ctx.gameState(), ctx);

        assertInstanceOf(ConfirmMulliganCommand.class, cmd);
    }

    @Test
    void shouldMapUseLeaderCommand() {
        CommandRequestDto request = new CommandRequestDto(null, CommandType.USE_LEADER, null, null, null, null);
        SessionContext ctx = makeSessionContext("p1@test.com", "p2@test.com");

        GameCommand cmd = mapper.toCommand(request, Turn.PLAYER_1, ctx.gameState(), ctx);

        assertInstanceOf(UseLeaderCommand.class, cmd);
    }

    @Test
    void shouldMapResolveMedicCommand() {
        SessionContext ctx = makeSessionContext("p1@test.com", "p2@test.com");
        Card graveyardCard = makeCard("medic_target", Faction.NORTHERN_REALMS);
        ctx.gameState().getPlayer1().addToGraveyard(graveyardCard);

        CommandRequestDto request = new CommandRequestDto(null, CommandType.RESOLVE_MEDIC, "MEDIC_TARGET", null, null, null);
        GameCommand cmd = mapper.toCommand(request, Turn.PLAYER_1, ctx.gameState(), ctx);

        assertInstanceOf(ResolveMedicCommand.class, cmd);
    }

    @Test
    void shouldMapResolveLeader_graveyardPick() {
        SessionContext ctx = makeSessionContext("p1@test.com", "p2@test.com");
        ctx.gameState().setPendingAbility(PendingAbility.LEADER_GRAVEYARD_PICK);
        Card graveyardCard = makeCard("graveyard_pick", Faction.NORTHERN_REALMS);
        ctx.gameState().getPlayer1().addToGraveyard(graveyardCard);

        CommandRequestDto request = new CommandRequestDto(null, CommandType.RESOLVE_LEADER, "GRAVEYARD_PICK", null, null, null);
        GameCommand cmd = mapper.toCommand(request, Turn.PLAYER_1, ctx.gameState(), ctx);

        assertInstanceOf(ResolveLeaderCommand.class, cmd);
    }

    @Test
    void shouldMapResolveLeader_deckPick() {
        SessionContext ctx = makeSessionContext("p1@test.com", "p2@test.com");
        ctx.gameState().setPendingAbility(PendingAbility.LEADER_DECK_PICK);
        // The deck already has cards from makeSessionContext

        String firstDeckCardId = ctx.gameState().getPlayer1().getDeck().getFirst().id();
        CommandRequestDto request = new CommandRequestDto(null, CommandType.RESOLVE_LEADER, firstDeckCardId, null, null, null);
        GameCommand cmd = mapper.toCommand(request, Turn.PLAYER_1, ctx.gameState(), ctx);

        assertInstanceOf(ResolveLeaderCommand.class, cmd);
    }

    @Test
    void shouldMapResolveScoiataelCommand() {
        SessionContext ctx = makeSessionContext("p1@test.com", "p2@test.com");

        CommandRequestDto request = new CommandRequestDto(null, CommandType.RESOLVE_SCOIATAEL, null, null, null, "p1@test.com");
        GameCommand cmd = mapper.toCommand(request, Turn.PLAYER_1, ctx.gameState(), ctx);

        assertInstanceOf(ResolveScoiataelCommand.class, cmd);
    }

    @Test
    void shouldThrow_whenScoiataelUnknownPlayerId() {
        SessionContext ctx = makeSessionContext("p1@test.com", "p2@test.com");

        CommandRequestDto request = new CommandRequestDto(null, CommandType.RESOLVE_SCOIATAEL, null, null, null, "unknown@test.com");

        assertThrows(IllegalArgumentException.class,
                () -> mapper.toCommand(request, Turn.PLAYER_1, ctx.gameState(), ctx));
    }

    // ── findCard ──

    @Test
    void shouldThrowCardNotFoundException_whenNotInList() {
        SessionContext ctx = makeSessionContext("p1@test.com", "p2@test.com");

        CommandRequestDto request = new CommandRequestDto(null, CommandType.RESOLVE_MEDIC, "nonexistent", null, null, null);

        assertThrows(CardNotFoundException.class,
                () -> mapper.toCommand(request, Turn.PLAYER_1, ctx.gameState(), ctx));
    }

    // ── toCardDto ──

    @Test
    void shouldMapAllCardFields() {
        Card card = new Card("test_card", "Test Card", Faction.NORTHERN_REALMS, CardType.UNIT,
                Ability.MEDIC, null, RowType.MELEE, 5);

        CardDto dto = mapper.toCardDto(card);

        assertEquals("TEST_CARD", dto.id());
        assertEquals("Test Card", dto.name());
        assertEquals(5, dto.basePower());
        assertNull(dto.currentPower());
        assertEquals("UNIT", dto.cardType());
        assertEquals("MELEE", dto.rowType());
        assertEquals("MEDIC", dto.ability());
        assertEquals("NORTHERN_REALMS", dto.faction());
        assertNull(dto.leaderAbility());
    }

    @Test
    void shouldHandleNullRowTypeAndAbility() {
        Card card = new Card("leader", "Leader", Faction.NORTHERN_REALMS, CardType.LEADER,
                null, LeaderAbility.SIEGE_MASTER, null, null);

        CardDto dto = mapper.toCardDto(card);

        assertNull(dto.rowType());
        assertNull(dto.ability());
        assertEquals("SIEGE_MASTER", dto.leaderAbility());
    }

    // ── toBoardRowDto ──

    @Test
    void shouldMapRowWithCardsAndFlags() {
        BoardRow row = new BoardRow(RowType.MELEE);
        Card card = makeCard("row_card", Faction.NORTHERN_REALMS);
        row.addCard(card);
        row.setHornActive(true);
        row.setWeatherActive(true);

        BoardRowDto dto = mapper.toBoardRowDto(row);

        assertEquals(1, dto.cards().size());
        assertTrue(dto.hornActive());
        assertTrue(dto.weatherActive());
    }
}

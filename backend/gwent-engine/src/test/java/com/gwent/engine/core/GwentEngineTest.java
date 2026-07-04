package com.gwent.engine.core;

import com.gwent.engine.command.*;
import com.gwent.engine.domain.*;
import com.gwent.engine.exception.command.*;
import com.gwent.engine.state.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GwentEngineTest {

    private GwentEngine engine;

    @BeforeEach
    void setUp() {
        engine = new GwentEngine();
    }

    // =========================================================
    // resolveCoinFlip
    // =========================================================

    @Test
    void shouldSetFirstPlayerAndTransitionToRedraw() {
        GameState state = new GameState(makePlayer(), makePlayer());

        engine.resolveCoinFlip(state, Turn.PLAYER_2);

        assertEquals(Turn.PLAYER_2, state.getCurrentTurn());
        assertEquals(GamePhase.REDRAW, state.getPhase());
    }

    // =========================================================
    // drawInitialCards
    // =========================================================

    @Test
    void shouldDrawCorrectNumberOfCardsForBothPlayers() {
        PlayerState p1 = new PlayerState(makeLeader(), List.of(
                makeUnit("u1", "A", 5, RowType.MELEE),
                makeUnit("u2", "B", 3, RowType.MELEE)
        ));
        PlayerState p2 = new PlayerState(makeLeader(), List.of(
                makeUnit("u3", "C", 4, RowType.RANGED)
        ));
        GameState state = new GameState(p1, p2);

        engine.drawInitialCards(state, 2);

        assertEquals(2, p1.getHand().size());
        assertEquals(1, p2.getHand().size()); // only 1 card in deck
    }

    // =========================================================
    // startPlay
    // =========================================================

    @Test
    void shouldTransitionToPlayPhase() {
        GameState state = makeRedrawState(makePlayer(), makePlayer());
        engine.startPlay(state);

        assertEquals(GamePhase.PLAY, state.getPhase());
    }

    // =========================================================
    // handlePlayCard — happy paths
    // =========================================================

    @Test
    void shouldPlaceCardOnBoardAndRemoveFromHand() {
        Card unit = makeUnit("u1", "Soldier", 5, RowType.MELEE);
        PlayerState p1 = playerWithHand(unit);
        GameState state = makePlayState(p1, makePlayer());

        engine.execute(state, new PlayCardCommand(unit, RowType.MELEE));

        assertTrue(p1.getHand().isEmpty());
        assertTrue(p1.getMeleeRow().getCards().contains(unit));
    }

    @Test
    void shouldSwitchTurnAfterPlayingCard() {
        Card unit = makeUnit("u1", "Soldier", 5, RowType.MELEE);
        GameState state = makePlayState(playerWithHand(unit), makePlayer());

        engine.execute(state, new PlayCardCommand(unit, RowType.MELEE));

        assertEquals(Turn.PLAYER_2, state.getCurrentTurn());
    }

    @Test
    void shouldNotSwitchTurnWhenOpponentHasPassed() {
        Card unit = makeUnit("u1", "Soldier", 5, RowType.MELEE);
        PlayerState p2 = makePlayer();
        p2.pass();
        GameState state = makePlayState(playerWithHand(unit), p2);

        engine.execute(state, new PlayCardCommand(unit, RowType.MELEE));

        assertEquals(Turn.PLAYER_1, state.getCurrentTurn());
    }

    @Test
    void shouldPlaceSpyOnOpponentRowAndDrawTwoCards() {
        Card spy = makeUnit("spy", "Spy", 4, RowType.MELEE, Ability.SPY);
        Card draw1 = makeUnit("d1", "Draw1", 3, RowType.MELEE);
        Card draw2 = makeUnit("d2", "Draw2", 3, RowType.MELEE);
        PlayerState p1 = new PlayerState(makeLeader(), List.of(draw1, draw2));
        p1.addToHand(spy);
        PlayerState p2 = makePlayer();
        GameState state = makePlayState(p1, p2);

        engine.execute(state, new PlayCardCommand(spy, RowType.MELEE));

        assertTrue(p2.getMeleeRow().getCards().contains(spy));
        assertFalse(p1.getMeleeRow().getCards().contains(spy));
        assertEquals(2, p1.getHand().size());
    }

    @Test
    void shouldActivateFrostOnBothMeleeRowsWhenFrostPlayed() {
        Card frost = makeWeatherCard("frost", Ability.FROST);
        GameState state = makePlayState(playerWithHand(frost), makePlayer());

        engine.execute(state, new PlayCardCommand(frost, RowType.MELEE));

        assertTrue(state.getPlayer1().getMeleeRow().isWeatherActive());
        assertTrue(state.getPlayer2().getMeleeRow().isWeatherActive());
        assertFalse(state.getPlayer1().getRangedRow().isWeatherActive());
        assertFalse(state.getPlayer1().getSiegeRow().isWeatherActive());
    }

    @Test
    void shouldActivateFogOnBothRangedRowsWhenFogPlayed() {
        Card fog = makeWeatherCard("fog", Ability.FOG);
        GameState state = makePlayState(playerWithHand(fog), makePlayer());

        engine.execute(state, new PlayCardCommand(fog, RowType.RANGED));

        assertTrue(state.getPlayer1().getRangedRow().isWeatherActive());
        assertTrue(state.getPlayer2().getRangedRow().isWeatherActive());
        assertFalse(state.getPlayer1().getMeleeRow().isWeatherActive());
    }

    @Test
    void shouldActivateRainOnBothSiegeRowsWhenRainPlayed() {
        Card rain = makeWeatherCard("rain", Ability.RAIN);
        GameState state = makePlayState(playerWithHand(rain), makePlayer());

        engine.execute(state, new PlayCardCommand(rain, RowType.SIEGE));

        assertTrue(state.getPlayer1().getSiegeRow().isWeatherActive());
        assertTrue(state.getPlayer2().getSiegeRow().isWeatherActive());
    }

    @Test
    void shouldClearAllWeatherWhenClearWeatherPlayed() {
        PlayerState p1 = makePlayer();
        PlayerState p2 = makePlayer();
        p1.getMeleeRow().setWeatherActive(true);
        p2.getRangedRow().setWeatherActive(true);
        Card clearWeather = makeWeatherCard("clear", Ability.CLEAR_WEATHER);
        p1.addToHand(clearWeather);
        GameState state = makePlayState(p1, p2);

        engine.execute(state, new PlayCardCommand(clearWeather, RowType.MELEE));

        assertFalse(p1.getMeleeRow().isWeatherActive());
        assertFalse(p2.getRangedRow().isWeatherActive());
        assertTrue(p1.getGraveyard().contains(clearWeather));
    }

    @Test
    void shouldSetPendingAbilityAndNotSwitchTurnWhenMedicPlayed() {
        Card medic = makeUnit("medic", "Medic", 5, RowType.MELEE, Ability.MEDIC);
        PlayerState p1 = playerWithHand(medic);
        p1.addToGraveyard(makeUnit("dead", "Dead Unit", 4, RowType.MELEE));
        GameState state = makePlayState(p1, makePlayer());

        engine.execute(state, new PlayCardCommand(medic, RowType.MELEE));

        assertEquals(PendingAbility.MEDIC_CHOICE, state.getPendingAbility());
        assertEquals(Turn.PLAYER_1, state.getCurrentTurn());
    }

    @Test
    void shouldAddWeatherCardToBoardActiveWeatherList() {
        Card frost = makeWeatherCard("frost", Ability.FROST);
        GameState state = makePlayState(playerWithHand(frost), makePlayer());

        engine.execute(state, new PlayCardCommand(frost, RowType.MELEE));

        assertTrue(state.getBoard().getActiveWeatherCards().contains(frost));
    }

    // =========================================================
    // handlePlayCard — sad paths
    // =========================================================

    @Test
    void shouldThrowWhenPlayingCardInWrongPhase() {
        Card unit = makeUnit("u1", "Soldier", 5, RowType.MELEE);
        GameState state = makeRedrawState(playerWithHand(unit), makePlayer());

        assertThrows(InvalidPhaseCommandException.class, () ->
                engine.execute(state, new PlayCardCommand(unit, RowType.MELEE)));
    }

    @Test
    void shouldThrowWhenPassedPlayerTriesToPlayCard() {
        Card unit = makeUnit("u1", "Soldier", 5, RowType.MELEE);
        PlayerState p1 = playerWithHand(unit);
        p1.pass();
        GameState state = makePlayState(p1, makePlayer());

        assertThrows(PlayerAlreadyPassedException.class, () ->
                engine.execute(state, new PlayCardCommand(unit, RowType.MELEE)));
    }

    @Test
    void shouldThrowWhenCardNotInHand() {
        Card unit = makeUnit("u1", "Soldier", 5, RowType.MELEE);
        GameState state = makePlayState(makePlayer(), makePlayer());

        assertThrows(CardNotInHandException.class, () ->
                engine.execute(state, new PlayCardCommand(unit, RowType.MELEE)));
    }

    @Test
    void shouldThrowWhenCardPlacedOnIncompatibleRow() {
        Card rangedUnit = makeUnit("r1", "Archer", 4, RowType.RANGED);
        GameState state = makePlayState(playerWithHand(rangedUnit), makePlayer());

        assertThrows(InvalidRowException.class, () ->
                engine.execute(state, new PlayCardCommand(rangedUnit, RowType.MELEE)));
    }

    // =========================================================
    // handlePass — happy paths
    // =========================================================

    @Test
    void shouldMarkPlayerAsPassedAndSwitchTurn() {
        GameState state = makePlayState(makePlayer(), makePlayer());

        engine.execute(state, new PassCommand());

        assertTrue(state.getPlayer1().isPassed());
        assertEquals(Turn.PLAYER_2, state.getCurrentTurn());
    }

    @Test
    void shouldStartNewRoundWhenBothPlayersPass() {
        GameState state = makePlayState(makePlayer(), makePlayer());

        engine.execute(state, new PassCommand()); // p1 passes → p2's turn
        engine.execute(state, new PassCommand()); // p2 passes → round ends

        // tie → both lose 1 life, nobody eliminated → startNewRound → PLAY (no redraw between rounds)
        assertEquals(GamePhase.PLAY, state.getPhase());
        assertEquals(2, state.getCurrentRound());
        assertEquals(1, state.getPlayer1().getLives());
        assertEquals(1, state.getPlayer2().getLives());
    }

    @Test
    void shouldSetGameOverWhenPlayerIsEliminated() {
        Card strong = makeUnit("strong", "Strong", 10, RowType.MELEE);
        PlayerState p1 = makePlayer();
        p1.loseLife(); // 1 life remaining
        PlayerState p2 = playerWithHand(strong);
        GameState state = makePlayState(p1, p2);

        engine.execute(state, new PassCommand());                             // p1 passes
        engine.execute(state, new PlayCardCommand(strong, RowType.MELEE));   // p2 plays strong card
        engine.execute(state, new PassCommand());                             // p2 passes → p1 loses last life

        assertEquals(GamePhase.GAME_OVER, state.getPhase());
        assertTrue(state.getPlayer1().isEliminated());
    }

    @Test
    void shouldSetRoundLoserAsFirstPlayerInNextRound() {
        Card strong = makeUnit("strong", "Strong", 10, RowType.MELEE);
        PlayerState p2 = playerWithHand(strong);
        GameState state = makePlayState(makePlayer(), p2);

        engine.execute(state, new PassCommand());                             // p1 passes
        engine.execute(state, new PlayCardCommand(strong, RowType.MELEE));   // p2 plays
        engine.execute(state, new PassCommand());                             // p2 passes → p1 lost → p1 goes first

        assertEquals(Turn.PLAYER_1, state.getCurrentTurn());
    }

    @Test
    void shouldClearRowsAndResetPassedAtRoundStart() {
        Card unit = makeUnit("u1", "Unit", 5, RowType.MELEE);
        PlayerState p1 = playerWithHand(unit);
        GameState state = makePlayState(p1, makePlayer());

        engine.execute(state, new PlayCardCommand(unit, RowType.MELEE));
        engine.execute(state, new PassCommand()); // p2 passes
        engine.execute(state, new PassCommand()); // p1 passes → round ends

        assertTrue(p1.getMeleeRow().getCards().isEmpty());
        assertFalse(p1.isPassed());
        assertFalse(state.getPlayer2().isPassed());
    }

    @Test
    void shouldClearWeatherAtRoundStart() {
        Card frost = makeWeatherCard("frost", Ability.FROST);
        PlayerState p1 = playerWithHand(frost);
        GameState state = makePlayState(p1, makePlayer());

        engine.execute(state, new PlayCardCommand(frost, RowType.MELEE));
        engine.execute(state, new PassCommand()); // p2 passes
        engine.execute(state, new PassCommand()); // p1 passes → new round

        assertFalse(state.getPlayer1().getMeleeRow().isWeatherActive());
        assertFalse(state.getPlayer2().getMeleeRow().isWeatherActive());
        assertTrue(state.getBoard().getActiveWeatherCards().isEmpty());
    }

    @Test
    void shouldNotDrawCardsAtNewRound() {
        Card u1 = makeUnit("u1", "A", 5, RowType.MELEE);
        Card u2 = makeUnit("u2", "B", 3, RowType.MELEE);
        Card u3 = makeUnit("u3", "C", 4, RowType.RANGED);
        Card u4 = makeUnit("u4", "D", 2, RowType.RANGED);
        PlayerState p1 = new PlayerState(makeLeader(), List.of(u1, u2));
        PlayerState p2 = new PlayerState(makeLeader(), List.of(u3, u4));
        GameState state = makePlayState(p1, p2);

        engine.execute(state, new PassCommand());
        engine.execute(state, new PassCommand());

        assertEquals(0, p1.getHand().size());
        assertEquals(0, p2.getHand().size());
    }

    // =========================================================
    // handlePass — sad paths
    // =========================================================

    @Test
    void shouldThrowWhenPassingInWrongPhase() {
        GameState state = makeRedrawState(makePlayer(), makePlayer());

        assertThrows(InvalidPhaseCommandException.class, () ->
                engine.execute(state, new PassCommand()));
    }

    @Test
    void shouldThrowWhenAlreadyPassedPlayerPassesAgain() {
        PlayerState p1 = makePlayer();
        p1.pass();
        GameState state = makePlayState(p1, makePlayer());

        assertThrows(PlayerAlreadyPassedException.class, () ->
                engine.execute(state, new PassCommand()));
    }

    // =========================================================
    // handleMulligan — happy paths
    // =========================================================

    @Test
    void shouldSwapHandCardWithCardFromDeck() {
        Card toReturn = makeUnit("old", "Old Card", 3, RowType.MELEE);
        Card inDeck = makeUnit("new", "New Card", 5, RowType.MELEE);
        PlayerState p1 = new PlayerState(makeLeader(), List.of(inDeck));
        p1.addToHand(toReturn);
        GameState state = makeRedrawState(p1, makePlayer());

        engine.execute(state, new MulliganCommand(Turn.PLAYER_1, toReturn));

        assertTrue(p1.getHand().contains(inDeck));
        assertFalse(p1.getHand().contains(toReturn));
        assertTrue(p1.getDeck().contains(toReturn));
    }

    @Test
    void shouldGetSameCardBackWhenDeckWasEmptyBeforeMulligan() {
        // When deck is empty, returned card goes to deck and is immediately drawn back
        Card toReturn = makeUnit("card", "Card", 3, RowType.MELEE);
        PlayerState p1 = new PlayerState(makeLeader(), List.of());
        p1.addToHand(toReturn);
        GameState state = makeRedrawState(p1, makePlayer());

        engine.execute(state, new MulliganCommand(Turn.PLAYER_1, toReturn));

        assertTrue(p1.getHand().contains(toReturn));
    }

    @Test
    void shouldAllowBothPlayersToMulliganIndependently() {
        Card p1Card = makeUnit("p1c", "P1Card", 3, RowType.MELEE);
        Card p1Deck = makeUnit("p1d", "P1Deck", 5, RowType.MELEE);
        Card p2Card = makeUnit("p2c", "P2Card", 4, RowType.RANGED);
        Card p2Deck = makeUnit("p2d", "P2Deck", 6, RowType.RANGED);
        PlayerState p1 = new PlayerState(makeLeader(), List.of(p1Deck));
        p1.addToHand(p1Card);
        PlayerState p2 = new PlayerState(makeLeader(), List.of(p2Deck));
        p2.addToHand(p2Card);
        GameState state = makeRedrawState(p1, p2);

        engine.execute(state, new MulliganCommand(Turn.PLAYER_1, p1Card));
        engine.execute(state, new MulliganCommand(Turn.PLAYER_2, p2Card));

        assertTrue(p1.getHand().contains(p1Deck));
        assertTrue(p2.getHand().contains(p2Deck));
    }

    // =========================================================
    // handleMulligan — sad paths
    // =========================================================

    @Test
    void shouldThrowWhenMulliganInWrongPhase() {
        Card card = makeUnit("u1", "Card", 3, RowType.MELEE);
        GameState state = makePlayState(playerWithHand(card), makePlayer());

        assertThrows(InvalidPhaseCommandException.class, () ->
                engine.execute(state, new MulliganCommand(Turn.PLAYER_1, card)));
    }

    @Test
    void shouldThrowWhenMulliganCardNotInHand() {
        Card card = makeUnit("u1", "Card", 3, RowType.MELEE);
        GameState state = makeRedrawState(makePlayer(), makePlayer());

        assertThrows(CardNotInHandException.class, () ->
                engine.execute(state, new MulliganCommand(Turn.PLAYER_1, card)));
    }

    @Test
    void shouldThrowWhenPlayerExceedsMulliganLimit() {
        Card c1 = makeUnit("c1", "Card1", 3, RowType.MELEE);
        Card c2 = makeUnit("c2", "Card2", 4, RowType.MELEE);
        Card c3 = makeUnit("c3", "Card3", 5, RowType.MELEE);
        Card d1 = makeUnit("d1", "Deck1", 3, RowType.MELEE);
        Card d2 = makeUnit("d2", "Deck2", 4, RowType.MELEE);
        Card d3 = makeUnit("d3", "Deck3", 5, RowType.MELEE);
        PlayerState p1 = new PlayerState(makeLeader(), List.of(d1, d2, d3));
        p1.addToHand(c1);
        p1.addToHand(c2);
        p1.addToHand(c3);
        GameState state = makeRedrawState(p1, makePlayer());

        engine.execute(state, new MulliganCommand(Turn.PLAYER_1, c1)); // 1st mulligan
        engine.execute(state, new MulliganCommand(Turn.PLAYER_1, c2)); // 2nd mulligan
        // 3rd mulligan → no mulligans remaining
        assertThrows(NoMulligansRemainingException.class, () ->
                engine.execute(state, new MulliganCommand(Turn.PLAYER_1, c3)));
    }

    @Test
    void shouldAllowUpToTwoMulligans() {
        Card c1 = makeUnit("c1", "Card1", 3, RowType.MELEE);
        Card c2 = makeUnit("c2", "Card2", 4, RowType.MELEE);
        Card d1 = makeUnit("d1", "Deck1", 6, RowType.MELEE);
        Card d2 = makeUnit("d2", "Deck2", 7, RowType.MELEE);
        PlayerState p1 = new PlayerState(makeLeader(), List.of(d1, d2));
        p1.addToHand(c1);
        p1.addToHand(c2);
        GameState state = makeRedrawState(p1, makePlayer());

        assertDoesNotThrow(() -> engine.execute(state, new MulliganCommand(Turn.PLAYER_1, c1)));
        assertDoesNotThrow(() -> engine.execute(state, new MulliganCommand(Turn.PLAYER_1, c2)));
        assertEquals(0, p1.getMulligansRemaining());
    }

    @Test
    void shouldThrowWhenMulliganAfterConfirming() {
        Card card = makeUnit("u1", "Card", 3, RowType.MELEE);
        PlayerState p1 = playerWithHand(card);
        GameState state = makeRedrawState(p1, makePlayer());

        engine.execute(state, new ConfirmMulliganCommand(Turn.PLAYER_1, List.of()));

        assertThrows(PlayerAlreadyConfirmedMulliganException.class, () ->
                engine.execute(state, new MulliganCommand(Turn.PLAYER_1, card)));
    }

    // =========================================================
    // handleConfirmMulligan — happy paths
    // =========================================================

    @Test
    void shouldTransitionToPlayWhenBothPlayersConfirm() {
        GameState state = makeRedrawState(makePlayer(), makePlayer());

        engine.execute(state, new ConfirmMulliganCommand(Turn.PLAYER_1, List.of()));
        engine.execute(state, new ConfirmMulliganCommand(Turn.PLAYER_2, List.of()));

        assertEquals(GamePhase.PLAY, state.getPhase());
    }

    @Test
    void shouldStayInRedrawWhenOnlyOnePlayerConfirms() {
        GameState state = makeRedrawState(makePlayer(), makePlayer());

        engine.execute(state, new ConfirmMulliganCommand(Turn.PLAYER_1, List.of()));

        assertEquals(GamePhase.REDRAW, state.getPhase());
    }

    @Test
    void shouldTransitionToPlayWhenSecondPlayerConfirmsAfterMulliganing() {
        Card card = makeUnit("u1", "Card", 3, RowType.MELEE);
        Card deck = makeUnit("d1", "Deck", 5, RowType.MELEE);
        PlayerState p1 = new PlayerState(makeLeader(), List.of(deck));
        p1.addToHand(card);
        GameState state = makeRedrawState(p1, makePlayer());

        engine.execute(state, new MulliganCommand(Turn.PLAYER_1, card));
        engine.execute(state, new ConfirmMulliganCommand(Turn.PLAYER_1, List.of()));
        engine.execute(state, new ConfirmMulliganCommand(Turn.PLAYER_2, List.of()));

        assertEquals(GamePhase.PLAY, state.getPhase());
    }

    @Test
    void shouldMarkPlayerAsConfirmed() {
        GameState state = makeRedrawState(makePlayer(), makePlayer());

        engine.execute(state, new ConfirmMulliganCommand(Turn.PLAYER_2, List.of()));

        assertTrue(state.getPlayer2().isMulliganConfirmed());
        assertFalse(state.getPlayer1().isMulliganConfirmed());
    }

    // =========================================================
    // handleConfirmMulligan — sad paths
    // =========================================================

    @Test
    void shouldThrowWhenConfirmMulliganInWrongPhase() {
        GameState state = makePlayState(makePlayer(), makePlayer());

        assertThrows(InvalidPhaseCommandException.class, () ->
                engine.execute(state, new ConfirmMulliganCommand(Turn.PLAYER_1, List.of())));
    }

    // =========================================================
    // handleUseLeader — happy paths
    // =========================================================

    @Test
    void shouldMarkLeaderAsUsedAndSwitchTurn() {
        GameState state = makePlayState(makePlayer(), makePlayer());

        engine.execute(state, new UseLeaderCommand());

        assertTrue(state.getPlayer1().isLeaderUsed());
        assertEquals(Turn.PLAYER_2, state.getCurrentTurn());
    }

    // =========================================================
    // handleUseLeader — sad paths
    // =========================================================

    @Test
    void shouldThrowWhenUseLeaderInWrongPhase() {
        GameState state = makeRedrawState(makePlayer(), makePlayer());

        assertThrows(InvalidPhaseCommandException.class, () ->
                engine.execute(state, new UseLeaderCommand()));
    }

    @Test
    void shouldThrowWhenLeaderAlreadyUsed() {
        PlayerState p1 = makePlayer();
        p1.useLeader();
        GameState state = makePlayState(p1, makePlayer());

        assertThrows(LeaderAlreadyUsedException.class, () ->
                engine.execute(state, new UseLeaderCommand()));
    }

    // =========================================================
    // handleResolveMedic — happy paths
    // =========================================================

    @Test
    void shouldReviveCardFromGraveyardToBoard() {
        Card medic = makeUnit("medic", "Medic", 5, RowType.MELEE, Ability.MEDIC);
        Card revived = makeUnit("revived", "Revived", 4, RowType.MELEE);
        PlayerState p1 = playerWithHand(medic);
        p1.addToGraveyard(revived);
        GameState state = makePlayState(p1, makePlayer());

        engine.execute(state, new PlayCardCommand(medic, RowType.MELEE));
        engine.execute(state, new ResolveMedicCommand(revived));

        assertTrue(p1.getMeleeRow().getCards().contains(revived));
        assertFalse(p1.getGraveyard().contains(revived));
        assertNull(state.getPendingAbility());
        assertEquals(Turn.PLAYER_2, state.getCurrentTurn());
    }

    @Test
    void shouldChainMedicWhenRevivedCardIsAlsoAMedic() {
        Card medic1 = makeUnit("medic1", "Medic", 5, RowType.MELEE, Ability.MEDIC);
        Card medic2 = makeUnit("medic2", "Medic", 5, RowType.MELEE, Ability.MEDIC);
        Card anotherUnit = makeUnit("unit", "Unit", 3, RowType.MELEE);
        PlayerState p1 = playerWithHand(medic1);
        p1.addToGraveyard(medic2);
        p1.addToGraveyard(anotherUnit);
        GameState state = makePlayState(p1, makePlayer());

        engine.execute(state, new PlayCardCommand(medic1, RowType.MELEE));
        engine.execute(state, new ResolveMedicCommand(medic2));

        assertEquals(PendingAbility.MEDIC_CHOICE, state.getPendingAbility());
        assertEquals(Turn.PLAYER_1, state.getCurrentTurn()); // turn not switched yet
    }

    // =========================================================
    // handleResolveMedic — sad paths
    // =========================================================

    @Test
    void shouldThrowWhenResolveMedicWithNoPendingAbility() {
        Card card = makeUnit("u1", "Card", 4, RowType.MELEE);
        GameState state = makePlayState(makePlayer(), makePlayer());

        assertThrows(InvalidPhaseCommandException.class, () ->
                engine.execute(state, new ResolveMedicCommand(card)));
    }

    @Test
    void shouldThrowWhenRevivedCardNotInGraveyard() {
        Card medic = makeUnit("medic", "Medic", 5, RowType.MELEE, Ability.MEDIC);
        Card notInGraveyard = makeUnit("ghost", "Ghost", 4, RowType.MELEE);
        Card validTarget = makeUnit("valid", "Valid", 3, RowType.MELEE);
        PlayerState p1 = playerWithHand(medic);
        p1.addToGraveyard(validTarget);
        GameState state = makePlayState(p1, makePlayer());

        engine.execute(state, new PlayCardCommand(medic, RowType.MELEE));

        assertThrows(CardNotInGraveyardException.class, () ->
                engine.execute(state, new ResolveMedicCommand(notInGraveyard)));
    }

    @Test
    void shouldThrowWhenTryingToReviveSpecialCard() {
        Card medic = makeUnit("medic", "Medic", 5, RowType.MELEE, Ability.MEDIC);
        Card special = new Card("spec", "Special", Faction.NEUTRAL, CardType.SPECIAL, null, null, null, null);
        Card validTarget = makeUnit("valid", "Valid", 3, RowType.MELEE);
        PlayerState p1 = playerWithHand(medic);
        p1.addToGraveyard(special);
        p1.addToGraveyard(validTarget);
        GameState state = makePlayState(p1, makePlayer());

        engine.execute(state, new PlayCardCommand(medic, RowType.MELEE));

        assertThrows(InvalidRowException.class, () ->
                engine.execute(state, new ResolveMedicCommand(special)));
    }

    @Test
    void shouldThrowWhenTryingToReviveHeroCard() {
        Card medic = makeUnit("medic", "Medic", 5, RowType.MELEE, Ability.MEDIC);
        Card hero = new Card("hero", "Geralt", Faction.NEUTRAL, CardType.HERO, null, null, RowType.MELEE, 15);
        PlayerState p1 = playerWithHand(medic);
        p1.addToGraveyard(hero);
        // Also add a unit so medic triggers pendingAbility
        Card unit = makeUnit("unit", "Unit", 3, RowType.MELEE);
        p1.addToGraveyard(unit);
        GameState state = makePlayState(p1, makePlayer());

        engine.execute(state, new PlayCardCommand(medic, RowType.MELEE));

        assertThrows(InvalidRowException.class, () ->
                engine.execute(state, new ResolveMedicCommand(hero)));
    }

    @Test
    void shouldSkipMedicWhenGraveyardHasNoUnitCards() {
        Card medic = makeUnit("medic", "Medic", 5, RowType.MELEE, Ability.MEDIC);
        PlayerState p1 = playerWithHand(medic);
        // Only hero in graveyard — medic should skip
        Card hero = new Card("hero", "Geralt", Faction.NEUTRAL, CardType.HERO, null, null, RowType.MELEE, 15);
        p1.addToGraveyard(hero);
        GameState state = makePlayState(p1, makePlayer());

        engine.execute(state, new PlayCardCommand(medic, RowType.MELEE));

        assertNull(state.getPendingAbility());
        assertEquals(Turn.PLAYER_2, state.getCurrentTurn()); // turn switched normally
    }

    @Test
    void shouldThrowWhenTryingToReviveLeaderCard() {
        Card medic = makeUnit("medic", "Medic", 5, RowType.MELEE, Ability.MEDIC);
        Card leader = makeLeader();
        Card validTarget = makeUnit("valid", "Valid", 3, RowType.MELEE);
        PlayerState p1 = playerWithHand(medic);
        p1.addToGraveyard(leader);
        p1.addToGraveyard(validTarget);
        GameState state = makePlayState(p1, makePlayer());

        engine.execute(state, new PlayCardCommand(medic, RowType.MELEE));

        assertThrows(InvalidRowException.class, () ->
                engine.execute(state, new ResolveMedicCommand(leader)));
    }

    // =========================================================
    // surrender — happy paths
    // =========================================================

    @Test
    void shouldSetGameOverWhenPlayerSurrenders() {
        GameState state = makePlayState(makePlayer(), makePlayer());

        engine.surrender(state, Turn.PLAYER_1);

        assertEquals(GamePhase.GAME_OVER, state.getPhase());
    }

    @Test
    void shouldSetOpponentAsWinnerWhenPlayerSurrenders() {
        GameState state = makePlayState(makePlayer(), makePlayer());

        engine.surrender(state, Turn.PLAYER_1);

        assertEquals(Turn.PLAYER_2, state.getWinner());
    }

    @Test
    void shouldSetSurrenderAsEndReason() {
        GameState state = makePlayState(makePlayer(), makePlayer());

        engine.surrender(state, Turn.PLAYER_2);

        assertEquals(Turn.PLAYER_1, state.getWinner());
        assertEquals(EndReason.SURRENDER, state.getEndReason());
    }

    // =========================================================
    // resolveRound — winner and endReason
    // =========================================================

    @Test
    void shouldSetWinnerWhenPlayerIsEliminatedByScore() {
        Card strong = makeUnit("strong", "Strong", 10, RowType.MELEE);
        PlayerState p1 = makePlayer();
        p1.loseLife(); // 1 life remaining
        PlayerState p2 = playerWithHand(strong);
        GameState state = makePlayState(p1, p2);

        engine.execute(state, new PassCommand());
        engine.execute(state, new PlayCardCommand(strong, RowType.MELEE));
        engine.execute(state, new PassCommand());

        assertEquals(Turn.PLAYER_2, state.getWinner());
        assertEquals(EndReason.NORMAL, state.getEndReason());
    }

    @Test
    void shouldSetNullWinnerOnTieElimination() {
        PlayerState p1 = makePlayer();
        PlayerState p2 = makePlayer();
        p1.loseLife(); // 1 life remaining
        p2.loseLife(); // 1 life remaining
        GameState state = makePlayState(p1, p2);

        engine.execute(state, new PassCommand());
        engine.execute(state, new PassCommand()); // tie → both lose last life

        assertEquals(GamePhase.GAME_OVER, state.getPhase());
        assertNull(state.getWinner());
        assertEquals(EndReason.NORMAL, state.getEndReason());
    }

    @Test
    void shouldNotSetWinnerWhenRoundEndsWithoutElimination() {
        GameState state = makePlayState(makePlayer(), makePlayer());

        engine.execute(state, new PassCommand());
        engine.execute(state, new PassCommand());

        assertEquals(GamePhase.PLAY, state.getPhase()); // new round started
        assertNull(state.getWinner());
        assertNull(state.getEndReason());
    }

    // =========================================================
    // Helpers
    // =========================================================

    private Card makeLeader() {
        return new Card("foltest", "Foltest", Faction.NORTHERN_REALMS, CardType.LEADER,
                null, LeaderAbility.SIEGE_MASTER, null, null);
    }

    private Card makeUnit(String id, String name, int power, RowType rowType) {
        return new Card(id, name, Faction.NEUTRAL, CardType.UNIT, null, null, rowType, power);
    }

    private Card makeUnit(String id, String name, int power, RowType rowType, Ability ability) {
        return new Card(id, name, Faction.NEUTRAL, CardType.UNIT, ability, null, rowType, power);
    }

    private Card makeWeatherCard(String id, Ability ability) {
        return new Card(id, ability.name(), Faction.NEUTRAL, CardType.WEATHER, ability, null, null, null);
    }

    private PlayerState makePlayer() {
        return new PlayerState(makeLeader(), List.of());
    }

    private PlayerState playerWithHand(Card... cards) {
        PlayerState player = new PlayerState(makeLeader(), List.of());
        for (Card card : cards) player.addToHand(card);
        return player;
    }

    private GameState makeRedrawState(PlayerState p1, PlayerState p2) {
        GameState state = new GameState(p1, p2);
        engine.resolveCoinFlip(state, Turn.PLAYER_1); // sets turn, mulligansRemaining=2, phase=REDRAW
        return state;
    }

    private GameState makePlayState(PlayerState p1, PlayerState p2) {
        GameState state = new GameState(p1, p2);
        state.setCurrentTurn(Turn.PLAYER_1);
        state.setPhase(GamePhase.REDRAW);
        state.setPhase(GamePhase.PLAY);
        return state;
    }
}

package com.gwent.engine.core;

import com.gwent.engine.command.ResolveLeaderCommand;
import com.gwent.engine.command.UseLeaderCommand;
import com.gwent.engine.domain.*;
import com.gwent.engine.exception.command.CardNotInGraveyardException;
import com.gwent.engine.exception.command.CardNotInHandException;
import com.gwent.engine.exception.command.DeckInsufficientCardsException;
import com.gwent.engine.exception.command.InvalidRowException;
import com.gwent.engine.state.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LeaderAbilityResolverTest {

    private GwentEngine engine;

    @BeforeEach
    void setUp() {
        engine = new GwentEngine();
    }

    // =========================================================
    // SIEGE_MASTER (Northern Realms) — horn on siege row
    // =========================================================

    @Test
    void shouldActivateHornOnSiegeRowWhenSiegeMasterUsed() {
        PlayerState p1 = playerWithLeader(LeaderAbility.SIEGE_MASTER);
        GameState state = makePlayState(p1, playerWithLeader(LeaderAbility.SIEGE_MASTER));

        engine.execute(state, new UseLeaderCommand());

        assertTrue(p1.getSiegeRow().isHornActive());
    }

    @Test
    void shouldNotActivateHornOnOtherRowsWhenSiegeMasterUsed() {
        PlayerState p1 = playerWithLeader(LeaderAbility.SIEGE_MASTER);
        GameState state = makePlayState(p1, playerWithLeader(LeaderAbility.SIEGE_MASTER));

        engine.execute(state, new UseLeaderCommand());

        assertFalse(p1.getMeleeRow().isHornActive());
        assertFalse(p1.getRangedRow().isHornActive());
    }

    // =========================================================
    // WHITE_FLAME (Nilfgaard) — cancel opponent's leader
    // =========================================================

    @Test
    void shouldMarkOpponentLeaderAsUsedWhenWhiteFlameUsed() {
        PlayerState p1 = playerWithLeader(LeaderAbility.WHITE_FLAME);
        PlayerState p2 = playerWithLeader(LeaderAbility.SIEGE_MASTER);
        GameState state = makePlayState(p1, p2);

        engine.execute(state, new UseLeaderCommand());

        assertTrue(p2.isLeaderUsed());
    }

    @Test
    void shouldDoNothingWhenOpponentLeaderAlreadyUsedForWhiteFlame() {
        PlayerState p1 = playerWithLeader(LeaderAbility.WHITE_FLAME);
        PlayerState p2 = playerWithLeader(LeaderAbility.SIEGE_MASTER);
        p2.useLeader();
        GameState state = makePlayState(p1, p2);

        assertDoesNotThrow(() -> engine.execute(state, new UseLeaderCommand()));
        assertTrue(p2.isLeaderUsed());
    }

    // =========================================================
    // DAISY_OF_THE_VALLEY (Scoia'tael) — draw first card from deck
    // =========================================================

    @Test
    void shouldDrawFirstCardFromDeckWhenDaisyOfTheValleyUsed() {
        Card deckCard = makeUnit("d1", "DeckUnit", 5, RowType.MELEE);
        PlayerState p1 = new PlayerState(makeLeader(LeaderAbility.DAISY_OF_THE_VALLEY), List.of(deckCard));
        GameState state = makePlayState(p1, playerWithLeader(LeaderAbility.DAISY_OF_THE_VALLEY));

        engine.execute(state, new UseLeaderCommand());

        assertTrue(p1.getHand().contains(deckCard));
        assertTrue(p1.getDeck().isEmpty());
    }

    @Test
    void shouldThrowWhenDeckEmptyForDaisyOfTheValley() {
        PlayerState p1 = playerWithLeader(LeaderAbility.DAISY_OF_THE_VALLEY);
        GameState state = makePlayState(p1, playerWithLeader(LeaderAbility.DAISY_OF_THE_VALLEY));

        assertThrows(DeckInsufficientCardsException.class, () ->
                engine.execute(state, new UseLeaderCommand()));
    }

    // =========================================================
    // BRINGER_OF_DEATH (Monsters) — graveyard to hand pending
    // =========================================================

    @Test
    void shouldSetGraveyardToHandPendingWhenBringerOfDeathUsed() {
        PlayerState p1 = playerWithLeader(LeaderAbility.BRINGER_OF_DEATH);
        p1.addToGraveyard(makeUnit("g1", "Ghost", 5, RowType.MELEE));
        GameState state = makePlayState(p1, playerWithLeader(LeaderAbility.BRINGER_OF_DEATH));

        engine.execute(state, new UseLeaderCommand());

        assertEquals(PendingAbility.LEADER_GRAVEYARD_TO_HAND, state.getPendingAbility());
        assertEquals(LeaderAbility.BRINGER_OF_DEATH, state.getPendingLeaderAbility());
    }

    @Test
    void shouldMoveCardFromGraveyardToHandWhenBringerOfDeathResolved() {
        PlayerState p1 = playerWithLeader(LeaderAbility.BRINGER_OF_DEATH);
        Card unit = makeUnit("g1", "Ghost", 5, RowType.MELEE);
        p1.addToGraveyard(unit);
        GameState state = makePlayState(p1, playerWithLeader(LeaderAbility.BRINGER_OF_DEATH));

        engine.execute(state, new UseLeaderCommand());
        engine.execute(state, new ResolveLeaderCommand(unit));

        assertTrue(p1.getHand().contains(unit));
        assertFalse(p1.getGraveyard().contains(unit));
        assertNull(state.getPendingAbility());
    }

    @Test
    void shouldDoNothingWhenGraveyardHasNoUnitsForBringerOfDeath() {
        PlayerState p1 = playerWithLeader(LeaderAbility.BRINGER_OF_DEATH);
        GameState state = makePlayState(p1, playerWithLeader(LeaderAbility.BRINGER_OF_DEATH));

        engine.execute(state, new UseLeaderCommand());

        assertNull(state.getPendingAbility());
    }

    @Test
    void shouldThrowWhenResolvingBringerOfDeathWithCardNotInGraveyard() {
        PlayerState p1 = playerWithLeader(LeaderAbility.BRINGER_OF_DEATH);
        Card inGraveyard = makeUnit("g1", "InGrave", 5, RowType.MELEE);
        Card notInGraveyard = makeUnit("n1", "NotInGrave", 5, RowType.MELEE);
        p1.addToGraveyard(inGraveyard);
        GameState state = makePlayState(p1, playerWithLeader(LeaderAbility.BRINGER_OF_DEATH));

        engine.execute(state, new UseLeaderCommand());

        assertThrows(CardNotInGraveyardException.class, () ->
                engine.execute(state, new ResolveLeaderCommand(notInGraveyard)));
    }

    @Test
    void shouldThrowWhenResolvingBringerOfDeathWithNonUnitCard() {
        PlayerState p1 = playerWithLeader(LeaderAbility.BRINGER_OF_DEATH);
        Card hero = makeHero("h1", "Hero", 10, RowType.MELEE);
        Card unit = makeUnit("u1", "Unit", 3, RowType.MELEE);
        p1.addToGraveyard(hero);
        p1.addToGraveyard(unit);
        GameState state = makePlayState(p1, playerWithLeader(LeaderAbility.BRINGER_OF_DEATH));

        engine.execute(state, new UseLeaderCommand());

        assertThrows(InvalidRowException.class, () ->
                engine.execute(state, new ResolveLeaderCommand(hero)));
    }

    // =========================================================
    // DESTROYER_OF_WORLDS (Monsters) — discard pending + draw
    // =========================================================

    @Test
    void shouldSetHandDiscardPendingWhenDestroyerOfWorldsUsed() {
        Card c1 = makeUnit("c1", "Card1", 3, RowType.MELEE);
        Card c2 = makeUnit("c2", "Card2", 4, RowType.MELEE);
        PlayerState p1 = playerWithLeader(LeaderAbility.DESTROYER_OF_WORLDS);
        p1.addToHand(c1);
        p1.addToHand(c2);
        GameState state = makePlayState(p1, playerWithLeader(LeaderAbility.DESTROYER_OF_WORLDS));

        engine.execute(state, new UseLeaderCommand());

        assertEquals(PendingAbility.LEADER_HAND_DISCARD, state.getPendingAbility());
        assertEquals(LeaderAbility.DESTROYER_OF_WORLDS, state.getPendingLeaderAbility());
        assertEquals(2, state.getPendingAbilityCount());
    }

    @Test
    void shouldDiscardCardsSequentiallyAndDrawOneAfterAllDiscards() {
        Card c1 = makeUnit("c1", "Card1", 3, RowType.MELEE);
        Card c2 = makeUnit("c2", "Card2", 4, RowType.MELEE);
        Card keep = makeUnit("keep", "Keep", 5, RowType.MELEE);
        Card deckCard = makeUnit("deck", "FromDeck", 6, RowType.RANGED);
        PlayerState p1 = new PlayerState(makeLeader(LeaderAbility.DESTROYER_OF_WORLDS), List.of(deckCard));
        p1.addToHand(c1);
        p1.addToHand(c2);
        p1.addToHand(keep);
        GameState state = makePlayState(p1, playerWithLeader(LeaderAbility.DESTROYER_OF_WORLDS));

        engine.execute(state, new UseLeaderCommand());

        // first discard
        engine.execute(state, new ResolveLeaderCommand(c1));
        assertTrue(p1.getGraveyard().contains(c1));
        assertEquals(PendingAbility.LEADER_HAND_DISCARD, state.getPendingAbility());
        assertEquals(1, state.getPendingAbilityCount());

        // second discard — draws 1 from deck after
        engine.execute(state, new ResolveLeaderCommand(c2));
        assertTrue(p1.getGraveyard().contains(c2));
        assertNull(state.getPendingAbility());
        assertTrue(p1.getHand().contains(keep));
        assertTrue(p1.getHand().contains(deckCard));
    }

    @Test
    void shouldSetPendingCountTo1WhenHandHasOnlyOneCard() {
        Card onlyCard = makeUnit("u1", "Only", 3, RowType.MELEE);
        PlayerState p1 = playerWithLeader(LeaderAbility.DESTROYER_OF_WORLDS);
        p1.addToHand(onlyCard);
        GameState state = makePlayState(p1, playerWithLeader(LeaderAbility.DESTROYER_OF_WORLDS));

        engine.execute(state, new UseLeaderCommand());

        assertEquals(1, state.getPendingAbilityCount());
    }

    @Test
    void shouldDoNothingWhenHandIsEmptyForDestroyerOfWorlds() {
        PlayerState p1 = playerWithLeader(LeaderAbility.DESTROYER_OF_WORLDS);
        GameState state = makePlayState(p1, playerWithLeader(LeaderAbility.DESTROYER_OF_WORLDS));

        engine.execute(state, new UseLeaderCommand());

        assertNull(state.getPendingAbility());
    }

    @Test
    void shouldNotDrawWhenDeckIsEmptyAfterDiscards() {
        Card c1 = makeUnit("c1", "Card1", 3, RowType.MELEE);
        Card c2 = makeUnit("c2", "Card2", 4, RowType.MELEE);
        PlayerState p1 = playerWithLeader(LeaderAbility.DESTROYER_OF_WORLDS);
        p1.addToHand(c1);
        p1.addToHand(c2);
        GameState state = makePlayState(p1, playerWithLeader(LeaderAbility.DESTROYER_OF_WORLDS));

        engine.execute(state, new UseLeaderCommand());
        engine.execute(state, new ResolveLeaderCommand(c1));
        engine.execute(state, new ResolveLeaderCommand(c2));

        assertEquals(2, p1.getGraveyard().size());
        assertTrue(p1.getHand().isEmpty());
        assertNull(state.getPendingAbility());
    }

    @Test
    void shouldThrowWhenDiscardingCardNotInHand() {
        Card c1 = makeUnit("c1", "Card1", 3, RowType.MELEE);
        Card notInHand = makeUnit("n1", "NotInHand", 5, RowType.MELEE);
        PlayerState p1 = playerWithLeader(LeaderAbility.DESTROYER_OF_WORLDS);
        p1.addToHand(c1);
        GameState state = makePlayState(p1, playerWithLeader(LeaderAbility.DESTROYER_OF_WORLDS));

        engine.execute(state, new UseLeaderCommand());

        assertThrows(CardNotInHandException.class, () ->
                engine.execute(state, new ResolveLeaderCommand(notInHand)));
    }

    // =========================================================
    // INVADER_OF_THE_NORTH (Nilfgaard) — revive 1 from graveyard for both
    // =========================================================

    @Test
    void shouldReviveUnitFromGraveyardForBothPlayers() {
        PlayerState p1 = playerWithLeader(LeaderAbility.INVADER_OF_THE_NORTH);
        PlayerState p2 = playerWithLeader(LeaderAbility.INVADER_OF_THE_NORTH);
        Card g1 = makeUnit("g1", "Ghost1", 5, RowType.MELEE);
        Card g2 = makeUnit("g2", "Ghost2", 4, RowType.RANGED);
        p1.addToGraveyard(g1);
        p2.addToGraveyard(g2);
        GameState state = makePlayState(p1, p2);

        engine.execute(state, new UseLeaderCommand());

        assertTrue(p1.getHand().contains(g1));
        assertFalse(p1.getGraveyard().contains(g1));
        assertTrue(p2.getHand().contains(g2));
        assertFalse(p2.getGraveyard().contains(g2));
    }

    @Test
    void shouldThrowWhenNoUnitsInGraveyardForInvaderOfTheNorth() {
        PlayerState p1 = playerWithLeader(LeaderAbility.INVADER_OF_THE_NORTH);
        PlayerState p2 = playerWithLeader(LeaderAbility.INVADER_OF_THE_NORTH);
        GameState state = makePlayState(p1, p2);

        assertThrows(CardNotInGraveyardException.class, () ->
                engine.execute(state, new UseLeaderCommand()));
    }

    @Test
    void shouldThrowWhenOnlyOnePlayerHasUnitsInGraveyardForInvaderOfTheNorth() {
        PlayerState p1 = playerWithLeader(LeaderAbility.INVADER_OF_THE_NORTH);
        PlayerState p2 = playerWithLeader(LeaderAbility.INVADER_OF_THE_NORTH);
        p1.addToGraveyard(makeUnit("g1", "Ghost", 5, RowType.MELEE));
        // p2 has no units in graveyard
        GameState state = makePlayState(p1, p2);

        assertThrows(CardNotInGraveyardException.class, () ->
                engine.execute(state, new UseLeaderCommand()));
    }

    // =========================================================
    // LORD_COMMANDER (Northern Realms) — clear all weather
    // =========================================================

    @Test
    void shouldClearAllWeatherFromAllRowsForBothPlayers() {
        PlayerState p1 = playerWithLeader(LeaderAbility.LORD_COMMANDER);
        PlayerState p2 = playerWithLeader(LeaderAbility.LORD_COMMANDER);
        p1.getMeleeRow().setWeatherActive(true);
        p1.getRangedRow().setWeatherActive(true);
        p1.getSiegeRow().setWeatherActive(true);
        p2.getMeleeRow().setWeatherActive(true);
        p2.getRangedRow().setWeatherActive(true);
        p2.getSiegeRow().setWeatherActive(true);
        GameState state = makePlayState(p1, p2);

        engine.execute(state, new UseLeaderCommand());

        assertFalse(p1.getMeleeRow().isWeatherActive());
        assertFalse(p1.getRangedRow().isWeatherActive());
        assertFalse(p1.getSiegeRow().isWeatherActive());
        assertFalse(p2.getMeleeRow().isWeatherActive());
        assertFalse(p2.getRangedRow().isWeatherActive());
        assertFalse(p2.getSiegeRow().isWeatherActive());
    }

    @Test
    void shouldNotThrowWhenNoWeatherActiveForLordCommander() {
        PlayerState p1 = playerWithLeader(LeaderAbility.LORD_COMMANDER);
        GameState state = makePlayState(p1, playerWithLeader(LeaderAbility.LORD_COMMANDER));

        assertDoesNotThrow(() -> engine.execute(state, new UseLeaderCommand()));
    }

    // =========================================================
    // QUEEN_OF_DOL_BLATHANNA (Scoia'tael) — destroy strongest in melee row
    // =========================================================

    @Test
    void shouldDestroyStrongestUnitInMeleeRowWhenScoreAtLeast10() {
        PlayerState p1 = playerWithLeader(LeaderAbility.QUEEN_OF_DOL_BLATHANNA);
        PlayerState p2 = playerWithLeader(LeaderAbility.QUEEN_OF_DOL_BLATHANNA);
        Card strong = makeUnit("s1", "Strong", 8, RowType.MELEE);
        Card weak = makeUnit("w1", "Weak", 3, RowType.MELEE);
        p2.getMeleeRow().addCard(strong);
        p2.getMeleeRow().addCard(weak);
        GameState state = makePlayState(p1, p2);

        engine.execute(state, new UseLeaderCommand());

        assertFalse(p2.getMeleeRow().getCards().contains(strong));
        assertTrue(p2.getMeleeRow().getCards().contains(weak));
        assertTrue(p2.getGraveyard().contains(strong));
    }

    @Test
    void shouldNotDestroyWhenMeleeRowScoreBelow10() {
        PlayerState p1 = playerWithLeader(LeaderAbility.QUEEN_OF_DOL_BLATHANNA);
        PlayerState p2 = playerWithLeader(LeaderAbility.QUEEN_OF_DOL_BLATHANNA);
        Card unit = makeUnit("u1", "Unit", 5, RowType.MELEE);
        p2.getMeleeRow().addCard(unit);
        GameState state = makePlayState(p1, p2);

        engine.execute(state, new UseLeaderCommand());

        assertTrue(p2.getMeleeRow().getCards().contains(unit));
    }

    @Test
    void shouldNotDestroyHeroCardsInMeleeRow() {
        PlayerState p1 = playerWithLeader(LeaderAbility.QUEEN_OF_DOL_BLATHANNA);
        PlayerState p2 = playerWithLeader(LeaderAbility.QUEEN_OF_DOL_BLATHANNA);
        Card hero = makeHero("h1", "Hero", 10, RowType.MELEE);
        p2.getMeleeRow().addCard(hero);
        GameState state = makePlayState(p1, p2);

        engine.execute(state, new UseLeaderCommand());

        assertTrue(p2.getMeleeRow().getCards().contains(hero));
    }

    // =========================================================
    // EMPEROR_OF_NILFGAARD (Nilfgaard) — reveal cards
    // =========================================================

    @Test
    void shouldRevealThreeRandomCardsFromOpponentHand() {
        PlayerState p1 = playerWithLeader(LeaderAbility.EMPEROR_OF_NILFGAARD);
        PlayerState p2 = playerWithLeader(LeaderAbility.EMPEROR_OF_NILFGAARD);
        for (int i = 0; i < 5; i++) {
            p2.addToHand(makeUnit("u" + i, "Unit" + i, 3 + i, RowType.MELEE));
        }
        GameState state = makePlayState(p1, p2);

        engine.execute(state, new UseLeaderCommand());

        assertNotNull(state.getRevealedCards());
        assertEquals(3, state.getRevealedCards().size());
        for (Card revealed : state.getRevealedCards()) {
            assertTrue(p2.getHand().contains(revealed));
        }
    }

    @Test
    void shouldRevealAllCardsWhenOpponentHasLessThanThree() {
        PlayerState p1 = playerWithLeader(LeaderAbility.EMPEROR_OF_NILFGAARD);
        PlayerState p2 = playerWithLeader(LeaderAbility.EMPEROR_OF_NILFGAARD);
        Card c1 = makeUnit("u1", "Unit1", 3, RowType.MELEE);
        Card c2 = makeUnit("u2", "Unit2", 4, RowType.MELEE);
        p2.addToHand(c1);
        p2.addToHand(c2);
        GameState state = makePlayState(p1, p2);

        engine.execute(state, new UseLeaderCommand());

        assertNotNull(state.getRevealedCards());
        assertEquals(2, state.getRevealedCards().size());
    }

    @Test
    void shouldSetEmptyRevealedCardsWhenOpponentHandIsEmpty() {
        PlayerState p1 = playerWithLeader(LeaderAbility.EMPEROR_OF_NILFGAARD);
        PlayerState p2 = playerWithLeader(LeaderAbility.EMPEROR_OF_NILFGAARD);
        GameState state = makePlayState(p1, p2);

        engine.execute(state, new UseLeaderCommand());

        assertNotNull(state.getRevealedCards());
        assertTrue(state.getRevealedCards().isEmpty());
    }

    // =========================================================
    // RELENTLESS (Nilfgaard) — pick from opponent graveyard
    // =========================================================

    @Test
    void shouldSetPendingAbilityWhenRelentlessUsedWithUnitsInOpponentGraveyard() {
        PlayerState p1 = playerWithLeader(LeaderAbility.RELENTLESS);
        PlayerState p2 = playerWithLeader(LeaderAbility.RELENTLESS);
        p2.addToGraveyard(makeUnit("g1", "Ghost", 5, RowType.MELEE));
        GameState state = makePlayState(p1, p2);

        engine.execute(state, new UseLeaderCommand());

        assertEquals(PendingAbility.LEADER_OPPONENT_GRAVEYARD_PICK, state.getPendingAbility());
        assertEquals(Turn.PLAYER_1, state.getCurrentTurn());
    }

    @Test
    void shouldPlaceOpponentGraveyardUnitOnCurrentPlayerSide() {
        PlayerState p1 = playerWithLeader(LeaderAbility.RELENTLESS);
        PlayerState p2 = playerWithLeader(LeaderAbility.RELENTLESS);
        Card unit = makeUnit("g1", "Ghost", 5, RowType.RANGED);
        p2.addToGraveyard(unit);
        GameState state = makePlayState(p1, p2);

        engine.execute(state, new UseLeaderCommand());
        engine.execute(state, new ResolveLeaderCommand(unit));

        assertTrue(p1.getRangedRow().getCards().contains(unit));
        assertFalse(p2.getGraveyard().contains(unit));
        assertNull(state.getPendingAbility());
    }

    @Test
    void shouldDoNothingWhenOpponentGraveyardHasNoUnitsForRelentless() {
        PlayerState p1 = playerWithLeader(LeaderAbility.RELENTLESS);
        PlayerState p2 = playerWithLeader(LeaderAbility.RELENTLESS);
        GameState state = makePlayState(p1, p2);

        engine.execute(state, new UseLeaderCommand());

        assertNull(state.getPendingAbility());
        assertEquals(Turn.PLAYER_2, state.getCurrentTurn());
    }

    @Test
    void shouldThrowWhenResolvingOpponentGraveyardPickWithCardNotInGraveyard() {
        PlayerState p1 = playerWithLeader(LeaderAbility.RELENTLESS);
        PlayerState p2 = playerWithLeader(LeaderAbility.RELENTLESS);
        Card inGraveyard = makeUnit("g1", "InGrave", 5, RowType.MELEE);
        Card notThere = makeUnit("n1", "NotThere", 5, RowType.MELEE);
        p2.addToGraveyard(inGraveyard);
        GameState state = makePlayState(p1, p2);

        engine.execute(state, new UseLeaderCommand());

        assertThrows(CardNotInGraveyardException.class, () ->
                engine.execute(state, new ResolveLeaderCommand(notThere)));
    }

    // =========================================================
    // KING_OF_THE_WILD_HUNT (Monsters) — pick weather from deck
    // =========================================================

    @Test
    void shouldPickFirstWeatherCardFromDeckWhenKingOfTheWildHuntUsed() {
        Card weather = makeWeatherCard("frost", Ability.FROST);
        Card unit = makeUnit("u1", "Unit", 5, RowType.MELEE);
        PlayerState p1 = new PlayerState(makeLeader(LeaderAbility.KING_OF_THE_WILD_HUNT), List.of(unit, weather));
        GameState state = makePlayState(p1, playerWithLeader(LeaderAbility.KING_OF_THE_WILD_HUNT));

        engine.execute(state, new UseLeaderCommand());

        assertTrue(p1.getHand().contains(weather));
        assertFalse(p1.getDeck().contains(weather));
        assertTrue(p1.getDeck().contains(unit));
    }

    @Test
    void shouldDoNothingWhenNoWeatherInDeckForKingOfTheWildHunt() {
        Card unit = makeUnit("u1", "Unit", 5, RowType.MELEE);
        PlayerState p1 = new PlayerState(makeLeader(LeaderAbility.KING_OF_THE_WILD_HUNT), List.of(unit));
        GameState state = makePlayState(p1, playerWithLeader(LeaderAbility.KING_OF_THE_WILD_HUNT));

        engine.execute(state, new UseLeaderCommand());

        assertTrue(p1.getHand().isEmpty());
        assertEquals(1, p1.getDeck().size());
    }

    // =========================================================
    // KING_OF_TEMERIA (Northern Realms) — pick FOG from deck
    // =========================================================

    @Test
    void shouldPickFogCardFromDeckWhenKingOfTemeriaUsed() {
        Card fog = makeWeatherCard("fog", Ability.FOG);
        Card frost = makeWeatherCard("frost", Ability.FROST);
        PlayerState p1 = new PlayerState(makeLeader(LeaderAbility.KING_OF_TEMERIA), List.of(frost, fog));
        GameState state = makePlayState(p1, playerWithLeader(LeaderAbility.KING_OF_TEMERIA));

        engine.execute(state, new UseLeaderCommand());

        assertTrue(p1.getHand().contains(fog));
        assertFalse(p1.getHand().contains(frost));
        assertFalse(p1.getDeck().contains(fog));
    }

    @Test
    void shouldDoNothingWhenNoFogInDeckForKingOfTemeria() {
        Card frost = makeWeatherCard("frost", Ability.FROST);
        PlayerState p1 = new PlayerState(makeLeader(LeaderAbility.KING_OF_TEMERIA), List.of(frost));
        GameState state = makePlayState(p1, playerWithLeader(LeaderAbility.KING_OF_TEMERIA));

        engine.execute(state, new UseLeaderCommand());

        assertTrue(p1.getHand().isEmpty());
        assertEquals(1, p1.getDeck().size());
    }

    // =========================================================
    // COMMANDER_OF_THE_RED_RIDERS (Monsters) — horn on melee row
    // =========================================================

    @Test
    void shouldActivateHornOnMeleeRowWhenCommanderOfTheRedRidersUsed() {
        PlayerState p1 = playerWithLeader(LeaderAbility.COMMANDER_OF_THE_RED_RIDERS);
        GameState state = makePlayState(p1, playerWithLeader(LeaderAbility.COMMANDER_OF_THE_RED_RIDERS));

        engine.execute(state, new UseLeaderCommand());

        assertTrue(p1.getMeleeRow().isHornActive());
    }

    @Test
    void shouldNotActivateHornOnOtherRowsWhenCommanderOfTheRedRidersUsed() {
        PlayerState p1 = playerWithLeader(LeaderAbility.COMMANDER_OF_THE_RED_RIDERS);
        GameState state = makePlayState(p1, playerWithLeader(LeaderAbility.COMMANDER_OF_THE_RED_RIDERS));

        engine.execute(state, new UseLeaderCommand());

        assertFalse(p1.getRangedRow().isHornActive());
        assertFalse(p1.getSiegeRow().isHornActive());
    }

    // =========================================================
    // CLAN_AN_CRAITE (Skellige) — all graveyard to deck for both + shuffle
    // =========================================================

    @Test
    void shouldMoveAllGraveyardCardsToDeckForBothPlayers() {
        PlayerState p1 = playerWithLeader(LeaderAbility.CLAN_AN_CRAITE);
        PlayerState p2 = playerWithLeader(LeaderAbility.CLAN_AN_CRAITE);
        Card g1 = makeUnit("g1", "Ghost1", 5, RowType.MELEE);
        Card g2 = makeUnit("g2", "Ghost2", 4, RowType.RANGED);
        Card g3 = makeUnit("g3", "Ghost3", 3, RowType.SIEGE);
        p1.addToGraveyard(g1);
        p1.addToGraveyard(g2);
        p2.addToGraveyard(g3);
        GameState state = makePlayState(p1, p2);

        engine.execute(state, new UseLeaderCommand());

        assertTrue(p1.getGraveyard().isEmpty());
        assertTrue(p2.getGraveyard().isEmpty());
        assertTrue(p1.getDeck().contains(g1));
        assertTrue(p1.getDeck().contains(g2));
        assertTrue(p2.getDeck().contains(g3));
    }

    @Test
    void shouldDoNothingWhenBothGraveyardsEmptyForClanAnCraite() {
        PlayerState p1 = playerWithLeader(LeaderAbility.CLAN_AN_CRAITE);
        PlayerState p2 = playerWithLeader(LeaderAbility.CLAN_AN_CRAITE);
        GameState state = makePlayState(p1, p2);

        assertDoesNotThrow(() -> engine.execute(state, new UseLeaderCommand()));
        assertTrue(p1.getDeck().isEmpty());
        assertTrue(p2.getDeck().isEmpty());
    }

    // =========================================================
    // PUREBLOOD_ELF (Scoia'tael) — pick frost and play via board
    // =========================================================

    @Test
    void shouldPickFrostFromDeckAndPlayViaBoardWhenPurebloodElfUsed() {
        Card frost = makeWeatherCard("frost", Ability.FROST);
        PlayerState p1 = new PlayerState(makeLeader(LeaderAbility.PUREBLOOD_ELF), List.of(frost));
        PlayerState p2 = playerWithLeader(LeaderAbility.PUREBLOOD_ELF);
        GameState state = makePlayState(p1, p2);

        engine.execute(state, new UseLeaderCommand());

        assertFalse(p1.getDeck().contains(frost));
        assertFalse(p1.getHand().contains(frost));
        assertTrue(state.getBoard().getActiveWeatherCards().contains(frost));
    }

    @Test
    void shouldDoNothingWhenNoFrostInDeckForPurebloodElf() {
        Card fog = makeWeatherCard("fog", Ability.FOG);
        PlayerState p1 = new PlayerState(makeLeader(LeaderAbility.PUREBLOOD_ELF), List.of(fog));
        GameState state = makePlayState(p1, playerWithLeader(LeaderAbility.PUREBLOOD_ELF));

        engine.execute(state, new UseLeaderCommand());

        assertTrue(p1.getDeck().contains(fog));
        assertTrue(state.getBoard().getActiveWeatherCards().isEmpty());
    }

    // =========================================================
    // KING_BRAN (Skellige) — graveyard to deck
    // =========================================================

    @Test
    void shouldMoveAllGraveyardCardsBackToDeckWhenKingBranUsed() {
        Card g1 = makeUnit("g1", "Ghost1", 3, RowType.MELEE);
        Card g2 = makeUnit("g2", "Ghost2", 4, RowType.RANGED);
        PlayerState p1 = playerWithLeader(LeaderAbility.KING_BRAN);
        p1.addToGraveyard(g1);
        p1.addToGraveyard(g2);
        GameState state = makePlayState(p1, playerWithLeader(LeaderAbility.KING_BRAN));

        engine.execute(state, new UseLeaderCommand());

        assertTrue(p1.getGraveyard().isEmpty());
        assertTrue(p1.getDeck().contains(g1));
        assertTrue(p1.getDeck().contains(g2));
    }

    @Test
    void shouldDoNothingWhenGraveyardIsEmptyForKingBran() {
        PlayerState p1 = playerWithLeader(LeaderAbility.KING_BRAN);
        GameState state = makePlayState(p1, playerWithLeader(LeaderAbility.KING_BRAN));

        assertDoesNotThrow(() -> engine.execute(state, new UseLeaderCommand()));
        assertTrue(p1.getGraveyard().isEmpty());
        assertTrue(p1.getDeck().isEmpty());
    }

    // =========================================================
    // SON_OF_MEDELL (Northern Realms) — destroy strongest in ranged row
    // =========================================================

    @Test
    void shouldDestroyStrongestUnitInRangedRowWhenScoreAtLeast10() {
        PlayerState p1 = playerWithLeader(LeaderAbility.SON_OF_MEDELL);
        PlayerState p2 = playerWithLeader(LeaderAbility.SON_OF_MEDELL);
        Card strong = makeUnit("s1", "Strong", 7, RowType.RANGED);
        Card weak = makeUnit("w1", "Weak", 4, RowType.RANGED);
        p2.getRangedRow().addCard(strong);
        p2.getRangedRow().addCard(weak);
        GameState state = makePlayState(p1, p2);

        engine.execute(state, new UseLeaderCommand());

        assertFalse(p2.getRangedRow().getCards().contains(strong));
        assertTrue(p2.getRangedRow().getCards().contains(weak));
        assertTrue(p2.getGraveyard().contains(strong));
    }

    @Test
    void shouldNotDestroyWhenRangedRowScoreBelow10ForSonOfMedell() {
        PlayerState p1 = playerWithLeader(LeaderAbility.SON_OF_MEDELL);
        PlayerState p2 = playerWithLeader(LeaderAbility.SON_OF_MEDELL);
        Card unit = makeUnit("u1", "Unit", 5, RowType.RANGED);
        p2.getRangedRow().addCard(unit);
        GameState state = makePlayState(p1, p2);

        engine.execute(state, new UseLeaderCommand());

        assertTrue(p2.getRangedRow().getCards().contains(unit));
        assertTrue(p2.getGraveyard().isEmpty());
    }

    @Test
    void shouldNotDestroyHeroCardsInRangedRowForSonOfMedell() {
        PlayerState p1 = playerWithLeader(LeaderAbility.SON_OF_MEDELL);
        PlayerState p2 = playerWithLeader(LeaderAbility.SON_OF_MEDELL);
        Card hero = makeHero("h1", "Hero", 10, RowType.RANGED);
        p2.getRangedRow().addCard(hero);
        GameState state = makePlayState(p1, p2);

        engine.execute(state, new UseLeaderCommand());

        assertTrue(p2.getRangedRow().getCards().contains(hero));
        assertTrue(p2.getGraveyard().isEmpty());
    }

    // =========================================================
    // STEEL_FORGED (Northern Realms) — destroy strongest in siege row
    // =========================================================

    @Test
    void shouldDestroyStrongestUnitInSiegeRowWhenScoreAtLeast10() {
        PlayerState p1 = playerWithLeader(LeaderAbility.STEEL_FORGED);
        PlayerState p2 = playerWithLeader(LeaderAbility.STEEL_FORGED);
        Card strong = makeUnit("s1", "Strong", 7, RowType.SIEGE);
        Card weak = makeUnit("w1", "Weak", 3, RowType.SIEGE);
        p2.getSiegeRow().addCard(strong);
        p2.getSiegeRow().addCard(weak);
        GameState state = makePlayState(p1, p2);

        engine.execute(state, new UseLeaderCommand());

        assertFalse(p2.getSiegeRow().getCards().contains(strong));
        assertTrue(p2.getSiegeRow().getCards().contains(weak));
        assertTrue(p2.getGraveyard().contains(strong));
    }

    @Test
    void shouldNotDestroyWhenSiegeRowScoreBelow10ForSteelForged() {
        PlayerState p1 = playerWithLeader(LeaderAbility.STEEL_FORGED);
        PlayerState p2 = playerWithLeader(LeaderAbility.STEEL_FORGED);
        Card unit = makeUnit("u1", "Unit", 5, RowType.SIEGE);
        p2.getSiegeRow().addCard(unit);
        GameState state = makePlayState(p1, p2);

        engine.execute(state, new UseLeaderCommand());

        assertTrue(p2.getSiegeRow().getCards().contains(unit));
        assertTrue(p2.getGraveyard().isEmpty());
    }

    @Test
    void shouldNotDestroyHeroCardsInSiegeRowForSteelForged() {
        PlayerState p1 = playerWithLeader(LeaderAbility.STEEL_FORGED);
        PlayerState p2 = playerWithLeader(LeaderAbility.STEEL_FORGED);
        Card hero = makeHero("h1", "Hero", 10, RowType.SIEGE);
        p2.getSiegeRow().addCard(hero);
        GameState state = makePlayState(p1, p2);

        engine.execute(state, new UseLeaderCommand());

        assertTrue(p2.getSiegeRow().getCards().contains(hero));
        assertTrue(p2.getGraveyard().isEmpty());
    }

    // =========================================================
    // IMPERIAL_MAJESTY (Nilfgaard) — pick rain and play via board
    // =========================================================

    @Test
    void shouldPickRainFromDeckAndPlayViaBoardWhenImperialMajestyUsed() {
        Card rain = makeWeatherCard("rain", Ability.RAIN);
        PlayerState p1 = new PlayerState(makeLeader(LeaderAbility.IMPERIAL_MAJESTY), List.of(rain));
        PlayerState p2 = playerWithLeader(LeaderAbility.IMPERIAL_MAJESTY);
        GameState state = makePlayState(p1, p2);

        engine.execute(state, new UseLeaderCommand());

        assertFalse(p1.getDeck().contains(rain));
        assertTrue(state.getBoard().getActiveWeatherCards().contains(rain));
    }

    @Test
    void shouldDoNothingWhenNoRainInDeckForImperialMajesty() {
        Card frost = makeWeatherCard("frost", Ability.FROST);
        PlayerState p1 = new PlayerState(makeLeader(LeaderAbility.IMPERIAL_MAJESTY), List.of(frost));
        GameState state = makePlayState(p1, playerWithLeader(LeaderAbility.IMPERIAL_MAJESTY));

        engine.execute(state, new UseLeaderCommand());

        assertTrue(p1.getDeck().contains(frost));
        assertTrue(state.getBoard().getActiveWeatherCards().isEmpty());
    }

    // =========================================================
    // THE_BEATIFUL (Scoia'tael) — horn on ranged row
    // =========================================================

    @Test
    void shouldActivateHornOnRangedRowWhenTheBeatifulUsed() {
        PlayerState p1 = playerWithLeader(LeaderAbility.THE_BEATIFUL);
        GameState state = makePlayState(p1, playerWithLeader(LeaderAbility.THE_BEATIFUL));

        engine.execute(state, new UseLeaderCommand());

        assertTrue(p1.getRangedRow().isHornActive());
    }

    @Test
    void shouldNotActivateHornOnOtherRowsWhenTheBeatifulUsed() {
        PlayerState p1 = playerWithLeader(LeaderAbility.THE_BEATIFUL);
        GameState state = makePlayState(p1, playerWithLeader(LeaderAbility.THE_BEATIFUL));

        engine.execute(state, new UseLeaderCommand());

        assertFalse(p1.getMeleeRow().isHornActive());
        assertFalse(p1.getSiegeRow().isHornActive());
    }

    // =========================================================
    // HOPE_OF_THE_AEN_SEIDHE (Scoia'tael) — move agile to best row
    // =========================================================

    @Test
    void shouldMoveAgileCardsToMeleeWhenMeleeHasHigherNonAgileScore() {
        PlayerState p1 = playerWithLeader(LeaderAbility.HOPE_OF_THE_AEN_SEIDHE);
        Card meleeUnit = makeUnit("m1", "MeleeUnit", 8, RowType.MELEE);
        Card rangedUnit = makeUnit("r1", "RangedUnit", 3, RowType.RANGED);
        Card agile = makeAgileUnit("a1", "Agile", 5);
        p1.getMeleeRow().addCard(meleeUnit);
        p1.getRangedRow().addCard(rangedUnit);
        p1.getRangedRow().addCard(agile);
        GameState state = makePlayState(p1, playerWithLeader(LeaderAbility.HOPE_OF_THE_AEN_SEIDHE));

        engine.execute(state, new UseLeaderCommand());

        assertTrue(p1.getMeleeRow().getCards().contains(agile));
        assertFalse(p1.getRangedRow().getCards().contains(agile));
    }

    @Test
    void shouldMoveAgileCardsToRangedWhenRangedHasHigherNonAgileScore() {
        PlayerState p1 = playerWithLeader(LeaderAbility.HOPE_OF_THE_AEN_SEIDHE);
        Card meleeUnit = makeUnit("m1", "MeleeUnit", 3, RowType.MELEE);
        Card rangedUnit = makeUnit("r1", "RangedUnit", 8, RowType.RANGED);
        Card agile = makeAgileUnit("a1", "Agile", 5);
        p1.getMeleeRow().addCard(meleeUnit);
        p1.getMeleeRow().addCard(agile);
        p1.getRangedRow().addCard(rangedUnit);
        GameState state = makePlayState(p1, playerWithLeader(LeaderAbility.HOPE_OF_THE_AEN_SEIDHE));

        engine.execute(state, new UseLeaderCommand());

        assertTrue(p1.getRangedRow().getCards().contains(agile));
        assertFalse(p1.getMeleeRow().getCards().contains(agile));
    }

    @Test
    void shouldNotMoveAgileCardsWhenNonAgileScoresAreTied() {
        PlayerState p1 = playerWithLeader(LeaderAbility.HOPE_OF_THE_AEN_SEIDHE);
        Card meleeUnit = makeUnit("m1", "MeleeUnit", 5, RowType.MELEE);
        Card rangedUnit = makeUnit("r1", "RangedUnit", 5, RowType.RANGED);
        Card agileInMelee = makeAgileUnit("a1", "Agile", 3);
        p1.getMeleeRow().addCard(meleeUnit);
        p1.getMeleeRow().addCard(agileInMelee);
        p1.getRangedRow().addCard(rangedUnit);
        GameState state = makePlayState(p1, playerWithLeader(LeaderAbility.HOPE_OF_THE_AEN_SEIDHE));

        engine.execute(state, new UseLeaderCommand());

        assertTrue(p1.getMeleeRow().getCards().contains(agileInMelee));
        assertFalse(p1.getRangedRow().getCards().contains(agileInMelee));
    }

    // =========================================================
    // Helpers
    // =========================================================

    private Card makeLeader(LeaderAbility ability) {
        return new Card("leader", "Leader", Faction.NEUTRAL, CardType.LEADER, null, ability, null, null);
    }

    private Card makeUnit(String id, String name, int power, RowType rowType) {
        return new Card(id, name, Faction.NEUTRAL, CardType.UNIT, null, null, rowType, power);
    }

    private Card makeHero(String id, String name, int power, RowType rowType) {
        return new Card(id, name, Faction.NEUTRAL, CardType.HERO, null, null, rowType, power);
    }

    private Card makeWeatherCard(String id, Ability ability) {
        return new Card(id, ability.name(), Faction.NEUTRAL, CardType.WEATHER, ability, null, null, null);
    }

    private Card makeAgileUnit(String id, String name, int power) {
        return new Card(id, name, Faction.NEUTRAL, CardType.UNIT, Ability.AGILE, null, RowType.MELEE, power);
    }

    private PlayerState playerWithLeader(LeaderAbility ability) {
        return new PlayerState(makeLeader(ability), List.of());
    }

    private GameState makePlayState(PlayerState p1, PlayerState p2) {
        GameState state = new GameState(p1, p2);
        state.setCurrentTurn(Turn.PLAYER_1);
        state.setPhase(GamePhase.REDRAW);
        state.setPhase(GamePhase.PLAY);
        return state;
    }
}

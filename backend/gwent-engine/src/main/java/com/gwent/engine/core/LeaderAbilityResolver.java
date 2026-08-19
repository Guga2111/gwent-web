package com.gwent.engine.core;

import com.gwent.engine.domain.*;
import com.gwent.engine.exception.command.CardNotInGraveyardException;
import com.gwent.engine.exception.command.DeckInsufficientCardsException;
import com.gwent.engine.state.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

class LeaderAbilityResolver {

    private final ScoreCalculator scoreCalculator;

    LeaderAbilityResolver(ScoreCalculator scoreCalculator) {
        this.scoreCalculator = scoreCalculator;
    }

    void resolve(GameState state, LeaderAbility ability) {
        switch (ability) {

            // --- Northern Realms ---
            case SIEGE_MASTER                -> handleSiegeMaster(state);
            case SON_OF_MEDELL             -> handleDestroyStrongestInRow(state, RowType.RANGED);
            case KING_OF_TEMERIA             -> handleKingOfTemeria(state);
            case LORD_COMMANDER              -> handleLordCommander(state);
            case STEEL_FORGED -> handleDestroyStrongestInRow(state, RowType.SIEGE);

            // --- Nilfgaard ---
            case WHITE_FLAME                 -> handleTheWhiteFlame(state);
            case EMPEROR_OF_NILFGAARD        -> handleEmperorOfNilfgaard(state);
            case INVADER_OF_THE_NORTH        -> handleInvaderOfTheNorth(state);
            case RELENTLESS                  -> handleRelentless(state);
            case IMPERIAL_MAJESTY            -> handleImperialMajesty(state);

            // --- Monsters ---
            case DESTROYER_OF_WORLDS         -> handleDestroyerOfWorlds(state);
            case BRINGER_OF_DEATH            -> handleBringerOfDeath(state);
            case COMMANDER_OF_THE_RED_RIDERS -> handleCommanderOfTheRedRiders(state);
            case KING_OF_THE_WILD_HUNT       -> handlePickWeatherFromDeck(state);
            // TODO - Implement the handle function logic
            case TREACHEROUS -> handleTreacherous(state);

            // --- Scoia'tael ---
            case DAISY_OF_THE_VALLEY         -> handleDaiseOfTheValley(state);
            case QUEEN_OF_DOL_BLATHANNA      -> handleDestroyStrongestInRow(state, RowType.MELEE);
            case HOPE_OF_THE_AEN_SEIDHE      -> handleHopeOfTheAenSeidhe(state);
            case PUREBLOOD_ELF               -> handlePurebloodElf(state);
            case THE_BEATIFUL               -> handleTheBeatiful(state);

            // --- Skellige ---
            // KING_BRAN - TODO: `UNIT Cards only lost half of their strength in bad weather conditions` (HIGH COMPLEXITY)
            case KING_BRAN                   -> handleKingBran(state);
            case CLAN_AN_CRAITE              -> handleClanAnCraite(state);
        }
    }


    // Northern Realms: Commanders horn in siege row - TODO REVIEW THIS
    private void handleSiegeMaster(GameState state) {
        PlayerState current = state.getCurrentPlayer();
        current.getSiegeRow().setHornActive(true);
    }

    // Northern Realms: Clear all weather in the game - TODO REVIEW THIS
    private void handleLordCommander(GameState state) {
        for (RowType row : RowType.values()) {
            state.getPlayer1().getRow(row).setWeatherActive(false);
            state.getPlayer2().getRow(row).setWeatherActive(false);
        }
    }

    // Northern Realms: Pick impenetrable fog card and play it - TODO REVIEW THIS (remains the instantly play card action... but i think it should be delegated to the engine)
    private void handleKingOfTemeria(GameState state) {
        PlayerState current = state.getCurrentPlayer();
        current.getDeck().stream()
                .filter(c -> c.cardType() == CardType.WEATHER && c.ability() == Ability.FOG)
                .findFirst()
                .ifPresent(c -> {
                    current.removeFromDeck(c);
                    current.addToHand(c);
                });
    }

    // Nilfgaard / Scoia'tael / Monsters: move first weather card from deck to hand
    private void handlePickWeatherFromDeck(GameState state) {
        PlayerState current = state.getCurrentPlayer();
        current.getDeck().stream()
                .filter(c -> c.cardType() == CardType.WEATHER)
                .findFirst()
                .ifPresent(c -> {
                    current.removeFromDeck(c);
                    current.addToHand(c);
                });
    }

    // Nilfgaard: reveal up to 3 random cards from opponent's hand
    private void handleEmperorOfNilfgaard(GameState state) {
        PlayerState opponent = state.getOpponent();
        List<Card> hand = new ArrayList<>(opponent.getHand());
        Collections.shuffle(hand);
        int count = Math.min(3, hand.size());
        state.setRevealedCards(hand.subList(0, count));
    }

    // Nilfgaard: revive 1 random card from the graveyard from both players and add it to the players hands - TODO: REVIEW THIS
    private void handleInvaderOfTheNorth(GameState state) {
        List<PlayerState> players = List.of(state.getPlayer1(), state.getPlayer2());

        for (PlayerState player : players) {
            Optional<Card> card = player.getGraveyard().stream()
                    .filter(c -> c.cardType() == CardType.UNIT)
                    .findFirst();

            if (card.isEmpty()) throw new CardNotInGraveyardException();

            player.removeFromGraveyard(card.get());
            player.addToHand(card.get());
        }
    }

    // Nilfgaard: cancel opponent's leader ability (mark as used)
    private void handleTheWhiteFlame(GameState state) {
        PlayerState opponent = state.getOpponent();
        if (!opponent.isLeaderUsed()) {
            opponent.useLeader();
        }
    }

    // Nilfgaard: pick 1 unit from opponent's graveyard, play on your side
    private void handleRelentless(GameState state) {
        PlayerState opponent = state.getOpponent();
        boolean hasUnits = opponent.getGraveyard().stream()
                .anyMatch(c -> c.cardType() == CardType.UNIT);
        if (!hasUnits) return;
        state.setPendingAbility(PendingAbility.LEADER_OPPONENT_GRAVEYARD_PICK);
        state.setPendingLeaderAbility(LeaderAbility.RELENTLESS);
    }

    // Nilfgaard: pick 1 torrential card and play it instantly (rain) - TODO: REVIEW THIS (view the logic for playing instantly)
    private void handleImperialMajesty(GameState state) {
        PlayerState current = state.getCurrentPlayer();
        current.getDeck().stream()
                .filter(c -> c.cardType() == CardType.WEATHER && c.ability() == Ability.RAIN)
                .findFirst()
                .ifPresent(c -> {
                    current.removeFromDeck(c);
                    state.getBoard().addWeatherCard(c);
                });
    }

    // Monsters: discard 2 cards from hand and draw 1 from deck
    private void handleDestroyerOfWorlds(GameState state) {
        PlayerState current = state.getCurrentPlayer();
        if (current.getHand().isEmpty()) return;
        int count = Math.min(2, current.getHand().size());
        state.setPendingAbility(PendingAbility.LEADER_HAND_DISCARD);
        state.setPendingLeaderAbility(LeaderAbility.DESTROYER_OF_WORLDS);
        state.setPendingAbilityCount(count);
    }

    //  Monsters: Doubles meelee strength by two (like commanders horn)
    private void handleCommanderOfTheRedRiders(GameState state) {
        PlayerState current = state.getCurrentPlayer();
        current.getMeleeRow().setHornActive(true);
    }

    // Monsters: Restores a card from your discard pile
    private void handleBringerOfDeath(GameState state) {
        PlayerState current = state.getCurrentPlayer();
        boolean hasUnits = current.getGraveyard().stream()
                .anyMatch(c -> c.cardType() == CardType.UNIT);
        if (!hasUnits) return;
        state.setPendingAbility(PendingAbility.LEADER_GRAVEYARD_TO_HAND);
        state.setPendingLeaderAbility(LeaderAbility.BRINGER_OF_DEATH);
    }

    // Monsters: doubles all spy cards strength
    private void handleTreacherous(GameState state) {
        // TODO ... ... ...
    }

    // LORD_COMMANDER (siege) / QUEEN_OF_DOL_BLATHANNA (melee): destroy strongest non-HERO unit if row score >= 10
    private void handleDestroyStrongestInRow(GameState state, RowType rowType) {
        PlayerState opponent = state.getOpponent();
        BoardRow row = opponent.getRow(rowType);

        int rowScore = scoreCalculator.calculate(row);
        if (rowScore < 10) return;

        Card strongest = null;
        int maxPower = -1;
        for (Card card : row.getCards()) {
            if (card.cardType() == CardType.HERO) continue;
            if (card.cardType() != CardType.UNIT) continue;
            int power = scoreCalculator.calculateCardPower(card, row);
            if (power > maxPower) {
                maxPower = power;
                strongest = card;
            }
        }

        if (strongest != null) {
            row.removeCard(strongest);
            opponent.addToGraveyard(strongest);
        }
    }

    // Scoia'tel: draw a card in the beggening of the battle (simply draw one card) - TODO: Review this
    private void handleDaiseOfTheValley(GameState state) {
        PlayerState current = state.getCurrentPlayer();
        Card card = current.getDeck().stream()
                .findFirst()
                .orElseThrow(DeckInsufficientCardsException::new);
        current.removeFromDeck(card);
        current.addToHand(card);
    }

    // Scoia'tael: pick a bitting frost from deck and play it
    private void handlePurebloodElf(GameState state) {
        PlayerState current = state.getCurrentPlayer();
        current.getDeck().stream()
                .filter(c -> c.cardType() == CardType.WEATHER && c.ability() == Ability.FROST)
                .findFirst()
                .ifPresent(c -> {
                    current.removeFromDeck(c);
                    state.getBoard().addWeatherCard(c);
                });
    }

    // Scoia'tael: move agile units to the most maximezed points row - TODO: Review this
    private void handleHopeOfTheAenSeidhe(GameState state) {
        // 0. Pegar o player que ta fazendo a açao
        PlayerState current = state.getCurrentPlayer();
        // 1. Coletar as cartas que nao sao agile
        List<Card> meleeNotAgileCards = current.getMeleeRow().getCards().stream()
                .filter(c -> c.ability() != Ability.AGILE)
                .toList();

        List<Card> rangedNotAgileCards = current.getRangedRow().getCards().stream()
                .filter(c -> c.ability() != Ability.AGILE)
                .toList();

        // 3. Somar os pontos de melee e ranged sem considerar as cartas com tipo agile
        int meleeNotAgilePoints = meleeNotAgileCards.stream()
                .mapToInt(c -> scoreCalculator.calculateCardPower(c, current.getMeleeRow()))
                .sum();

        int rangedNotAgilePoints = rangedNotAgileCards.stream()
                .mapToInt(c -> scoreCalculator.calculateCardPower(c, current.getRangedRow()))
                .sum();
        // 4. Verificar qual tem a maior pontuaçao
        // 5. Mover as cartas do tipo AGILE para a qual tiver a maior pontuacao
        if (meleeNotAgilePoints > rangedNotAgilePoints) {
            List<Card> agileCardsToAdd = current.getRangedRow().getCards().stream()
                    .filter(c -> c.ability() == Ability.AGILE)
                    .toList();

            for (Card card : agileCardsToAdd) {
                current.getMeleeRow().addCard(card);
                current.getRangedRow().removeCard(card);
            }
        } else if (rangedNotAgilePoints > meleeNotAgilePoints) {
            List<Card> agileCardsToAdd = current.getMeleeRow().getCards().stream()
                    .filter(c -> c.ability() == Ability.AGILE)
                    .toList();

            for (Card card : agileCardsToAdd) {
                current.getRangedRow().addCard(card);
                current.getMeleeRow().removeCard(card);
            }
        }
    }

    // Scoia'tael: doubles the strength of ranged cards
    private void handleTheBeatiful(GameState state) {
        PlayerState current = state.getCurrentPlayer();
        current.getRangedRow().setHornActive(true);
    }

    // Skellige: move all graveyard cards back to deck
    private void handleKingBran(GameState state) {
        PlayerState current = state.getCurrentPlayer();
        new ArrayList<>(current.getGraveyard()).forEach(c -> {
            current.removeFromGraveyard(c);
            current.returnToDeck(c);
        });
    }

    // Skellige: Move all players graveyard cards back to deck shuffled - TODO: REVIEW THIS
    private void handleClanAnCraite(GameState state) {
        List<PlayerState> players = List.of(state.getPlayer1(), state.getPlayer2());

        for (PlayerState player : players) {
            new ArrayList<>(player.getGraveyard()).forEach(c -> {
                player.removeFromGraveyard(c);
                player.returnToDeck(c);
            });
            player.shuffleDeck();
        }
    }

}

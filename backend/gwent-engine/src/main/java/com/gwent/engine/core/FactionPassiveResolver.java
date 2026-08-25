package com.gwent.engine.core;

import com.gwent.engine.domain.*;
import com.gwent.engine.state.GameState;
import com.gwent.engine.state.PlayerState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

class FactionPassiveResolver {

    record KeptCard(Card card, RowType row) {}

    Turn resolveNilfgaardTie(GameState state) {
        Faction p1Faction = state.getPlayer1().getLeader().faction();
        Faction p2Faction = state.getPlayer2().getLeader().faction();

        if (p1Faction == Faction.NILFGAARD && p2Faction == Faction.NILFGAARD) {
            state.getPlayer1().loseLife();
            state.getPlayer2().loseLife();
            return state.getCurrentTurn();
        }

        if (p1Faction == Faction.NILFGAARD) {
            state.getPlayer2().loseLife();
            return Turn.PLAYER_2;
        }

        state.getPlayer1().loseLife();
        return Turn.PLAYER_1;
    }

    void resolveNorthernRealmsBonus(PlayerState winner) {
        if (winner.getLeader().faction() != Faction.NORTHERN_REALMS) return;
        if (winner.isDeckEmpty()) return;
        winner.drawCard();
    }

    KeptCard resolveMonsterKeepCard(PlayerState player) {
        if (player.getLeader().faction() != Faction.MONSTER) return null;

        List<CardOnRow> candidates = Arrays.stream(RowType.values())
                .flatMap(row -> player.getRow(row).getCards().stream()
                        .filter(card -> card.cardType().equals(CardType.UNIT))
                        .map(card -> new CardOnRow(card, row)))
                .toList();

        if (candidates.isEmpty()) return null;

        CardOnRow picked = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
        player.getRow(picked.row()).removeCard(picked.card());
        return new KeptCard(picked.card(), picked.row());
    }

    void resolveSkelligeRound3(PlayerState player) {
        if (player.getLeader().faction() != Faction.SKELLIGE) return;

        List<Card> graveyardUnits = player.getGraveyard().stream()
                .filter(c -> c.cardType() == CardType.UNIT)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

        int count = Math.min(2, graveyardUnits.size());
        for (int i = 0; i < count; i++) {
            int idx = ThreadLocalRandom.current().nextInt(graveyardUnits.size() - i) + i;
            Card card = graveyardUnits.get(idx);
            graveyardUnits.set(idx, graveyardUnits.get(i));
            player.removeFromGraveyard(card);
            player.getRow(card.rowType()).addCard(card);
        }
    }

    boolean hasScoiataelAdvantage(GameState state) {
        Faction p1 = state.getPlayer1().getLeader().faction();
        Faction p2 = state.getPlayer2().getLeader().faction();
        return (p1 == Faction.SCOIATAEL) != (p2 == Faction.SCOIATAEL);
    }

    Turn getScoiataelPlayer(GameState state) {
        if (state.getPlayer1().getLeader().faction() == Faction.SCOIATAEL) return Turn.PLAYER_1;
        return Turn.PLAYER_2;
    }

    private record CardOnRow(Card card, RowType row) {}
}

package com.gwent.engine.core;

import com.gwent.engine.domain.Ability;
import com.gwent.engine.domain.Card;
import com.gwent.engine.domain.CardType;
import com.gwent.engine.state.BoardRow;
import com.gwent.engine.state.PlayerState;

import java.util.Map;
import java.util.stream.Collectors;

public class ScoreCalculator {

    public ScoreCalculator () {}

    public int calculate (PlayerState player) {
        return calculate(player.getMeleeRow())
                + calculate(player.getRangedRow())
                + calculate(player.getSiegeRow());
    }

    int calculate (BoardRow row) {

        int total = 0;

        Map<String, Long> countByName = row.getCards().stream()
                .collect(Collectors.groupingBy(Card::name, Collectors.counting()));

        long countMoraleBoost = row.getCards().stream()
                .filter(c -> Ability.MORALE_BOOST.equals(c.ability()))
                .count();

        for (Card card : row.getCards()) {

            int currentCardPower = 0;
            long moraleBonus = Ability.MORALE_BOOST.equals(card.ability())
                    ? countMoraleBoost - 1
                    : countMoraleBoost;

            currentCardPower += card.basePower();
            if (Ability.TIGHT_BOND.equals(card.ability())) currentCardPower *= countByName.get(card.name());
            if (card.cardType() == CardType.UNIT) currentCardPower += row.getLeaderBonusPower();
            if (row.isWeatherActive() && card.cardType().equals(CardType.UNIT)) currentCardPower = 1;
            currentCardPower += (int) moraleBonus;
            if (row.isHornActive()) currentCardPower *= 2;

            total += currentCardPower;
        }

        return total;
    }

    public int calculateCardPower (Card card, BoardRow row) {
        Map<String, Long> countByName = row.getCards().stream()
                .collect(Collectors.groupingBy(Card::name, Collectors.counting()));

        long countMoraleBoost = row.getCards().stream()
                .filter(c -> Ability.MORALE_BOOST.equals(c.ability()))
                .count();

        long moraleBonus = Ability.MORALE_BOOST.equals(card.ability())
                ? countMoraleBoost - 1
                : countMoraleBoost;

        int power = card.basePower();
        if (Ability.TIGHT_BOND.equals(card.ability())) power *= countByName.get(card.name());
        if (card.cardType() == CardType.UNIT) power += row.getLeaderBonusPower();
        if (row.isWeatherActive() && card.cardType() == CardType.UNIT) power = 1;
        power += (int) moraleBonus;
        if (row.isHornActive()) power *= 2;

        return power;
    }
}

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
        return calculate(player, false);
    }

    public int calculate (PlayerState player, boolean treacherousActive) {
        boolean kingBran = player.isKingBranActive();
        return calculate(player.getMeleeRow(), kingBran, treacherousActive)
                + calculate(player.getRangedRow(), kingBran, treacherousActive)
                + calculate(player.getSiegeRow(), kingBran, treacherousActive);
    }

    int calculate (BoardRow row) {
        return calculate(row, false, false);
    }

    int calculate (BoardRow row, boolean kingBranActive) {
        return calculate(row, kingBranActive, false);
    }

    int calculate (BoardRow row, boolean kingBranActive, boolean treacherousActive) {

        int total = 0;

        Map<String, Long> countByName = row.getCards().stream()
                .collect(Collectors.groupingBy(Card::name, Collectors.counting()));

        long countMoraleBoost = row.getCards().stream()
                .filter(c -> c.hasAbility(Ability.MORALE_BOOST))
                .count();

        for (Card card : row.getCards()) {

            int currentCardPower = 0;
            long moraleBonus = card.hasAbility(Ability.MORALE_BOOST)
                    ? countMoraleBoost - 1
                    : countMoraleBoost;

            currentCardPower += card.basePower();
            if (card.hasAbility(Ability.TIGHT_BOND)) currentCardPower *= countByName.get(card.name());

            if (card.cardType() == CardType.UNIT) {
                if (row.isWeatherActive()) {
                    currentCardPower = kingBranActive
                            ? Math.max(1, (int) Math.ceil(card.basePower() / 2.0))
                            : 1;
                }
                currentCardPower += (int) moraleBonus;
                if (row.isHornActive()) currentCardPower *= 2;
                if (treacherousActive && card.hasAbility(Ability.SPY)) currentCardPower *= 2;
            }

            total += currentCardPower;
        }

        return total;
    }

    public int calculateCardPower (Card card, BoardRow row) {
        return calculateCardPower(card, row, false);
    }

    public int calculateCardPower (Card card, BoardRow row, boolean kingBranActive) {
        Map<String, Long> countByName = row.getCards().stream()
                .collect(Collectors.groupingBy(Card::name, Collectors.counting()));

        long countMoraleBoost = row.getCards().stream()
                .filter(c -> c.hasAbility(Ability.MORALE_BOOST))
                .count();

        long moraleBonus = card.hasAbility(Ability.MORALE_BOOST)
                ? countMoraleBoost - 1
                : countMoraleBoost;

        int power = card.basePower();
        if (card.hasAbility(Ability.TIGHT_BOND)) power *= countByName.get(card.name());

        if (card.cardType() == CardType.UNIT) {
            if (row.isWeatherActive()) {
                power = kingBranActive
                        ? Math.max(1, (int) Math.ceil(card.basePower() / 2.0))
                        : 1;
            }
            power += (int) moraleBonus;
            if (row.isHornActive()) power *= 2;
        }

        return power;
    }
}

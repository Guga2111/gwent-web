package com.gwent.engine.state;

import com.gwent.engine.domain.Ability;
import com.gwent.engine.domain.Card;
import com.gwent.engine.domain.RowType;
import com.gwent.engine.exception.command.InvalidRowException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BoardRow {
    private List<Card> cards = new ArrayList<>();
    private final RowType rowType;
    private boolean hornActive;
    private boolean weatherActive;
    private int leaderBonusPower;

    public BoardRow (RowType rowType) {
        this.rowType = rowType;
        this.hornActive = false;
        this.weatherActive = false;
    }

    public List<Card> getCards () {
        return Collections.unmodifiableList(cards);
    }

    public RowType getRowType () {
        return rowType;
    }

    public boolean isHornActive() {
        return hornActive;
    }

    public void setHornActive (boolean hornActive) {
        this.hornActive = hornActive;
    }

    public boolean isWeatherActive() {
        return weatherActive;
    }

    public void setWeatherActive (boolean weatherActive) {
        this.weatherActive = weatherActive;
    }

    public int getLeaderBonusPower() {
        return leaderBonusPower;
    }

    public void setLeaderBonusPower(int leaderBonusPower) {
        this.leaderBonusPower = leaderBonusPower;
    }

    public void addCard (Card card) {

        if (card.ability() == Ability.AGILE) {
            if (rowType != RowType.MELEE && rowType != RowType.RANGED)
                throw new InvalidRowException();
        } else if (card.rowType() != rowType) {
            throw new InvalidRowException();
        }

        this.cards.add(card);
    }

    public void removeCard (Card card) {
        this.cards.remove(card);
    }

    public void clear () {
        cards.clear();
        leaderBonusPower = 0;
    }
}

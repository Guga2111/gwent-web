package com.gwent.engine.state;

import com.gwent.engine.domain.Card;
import com.gwent.engine.domain.RowType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BoardRow {
    private List<Card> cards = new ArrayList<>();
    private final RowType rowType;
    private boolean hornActive;
    private boolean weatherActive;

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

    public void addCard (Card card) {

        if (card.rowType() != rowType) {
            throw new IllegalStateException("The Card row: [" + card.rowType() + "] should be the same from the BoardRow: [" + rowType + "]");
        }

        this.cards.add(card);
    }

    public void removeCard (Card card) {
        this.cards.remove(card);
    }

    public void clear () {
        cards.clear();
    }
}

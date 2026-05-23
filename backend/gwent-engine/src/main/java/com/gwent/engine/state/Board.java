package com.gwent.engine.state;

import com.gwent.engine.domain.Card;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Board {
    private final PlayerState player1;
    private final PlayerState player2;
    private List<Card> activeWeatherCards = new ArrayList<>();

    public Board (PlayerState player1, PlayerState player2) {
        this.player1 = player1;
        this.player2 = player2;
    }

    public PlayerState getPlayer1 () {
        return player1;
    }

    public PlayerState getPlayer2 () {
        return player2;
    }

    public List<Card> getActiveWeatherCards () {
        return Collections.unmodifiableList(activeWeatherCards);
    }

    public void addWeatherCard (Card card) {
        activeWeatherCards.add(card);
    }

    public void clearWeatherCards () {
        activeWeatherCards.clear();
    }
}

package com.gwent.api.deck;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class DeckCardEntry {

    private String cardId;

    private int quantity;

    public DeckCardEntry(String cardId, int quantity) {
        this.cardId = cardId;
        this.quantity = quantity;
    }
}

package com.gwent.engine.domain;

public enum LeaderAbility {
    // Northern Realms
    SIEGE_MASTER,          // Foltest - doubles siege strength
    SON_OF_MEDELL,       // Foltest - destroy enemy ranged if 10+ strength
    KING_OF_TEMERIA,       // Foltest - pick a fog card from deck and play it
    LORD_COMMANDER,        // Foltest - clear weather effects
    STEEL_FORGED,         // Foltest - destroy enemy siege if 10+ strength

    // Nilfgaard
    EMPEROR_OF_NILFGAARD,  // Emhyr - look at 3 opponent cards
    INVADER_OF_THE_NORTH,  // Emhyr - revive card from discard
    RELENTLESS,            // Emhyr - draw from opponent discard pile
    WHITE_FLAME,           // Emhyr - cancel opponent leader ability
    IMPERIAL_MAJESTY,           // Emhyr - pick a torrential card and play it instantly

    // Monsters
    BRINGER_OF_DEATH,      // Eredin - restore a card from your discard pile to your hand.
    COMMANDER_OF_THE_RED_RIDERS, // Eredin - doubles meelee strength by *2
    DESTROYER_OF_WORLDS,   // Eredin - discard 2 cards and draw 1
    KING_OF_THE_WILD_HUNT, // Eredin - pick any weather card from your deck and play it instantly.
    TREACHEROUS,           // Eredin - doubles strength of all spy cards


    // Scoia'tael
    QUEEN_OF_DOL_BLATHANNA, // Francesca - destroy enemy close combat if 10+ strength
    DAISY_OF_THE_VALLEY,    // Francesca - raw an extra card at the beginning of the battle.
    PUREBLOOD_ELF,          // Francesca - pick a Biting Frost card from your deck and play it instantly.
    THE_BEATIFUL,           // Francesca - double the strength of ranged cards *2 (if does not have a commanders horn)
    HOPE_OF_THE_AEN_SEIDHE, // Francesca - move agile units to whichever valid row maximizes their strength (don't move units already in optimal row).

    // Skellige
    KING_BRAN,             // Crach - shuffle graveyard back into deck
    CLAN_AN_CRAITE          // Crach - shuffle graveyard back into deck
}

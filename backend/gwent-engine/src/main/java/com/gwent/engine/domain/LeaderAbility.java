package com.gwent.engine.domain;

public enum LeaderAbility {
    // Northern Realms
    SIEGE_MASTER,          // Foltest - clears weather from siege row
    NORTH_COMMANDER,       // Foltest - +1 to siege units
    KING_OF_TEMERIA,       // Foltest - pick a fog card from deck
    LORD_COMMANDER,        // Foltest - destroy enemy siege if 10+ strength

    // Nilfgaard
    EMPEROR_OF_NILFGAARD,  // Emhyr - look at 3 opponent cards
    INVADER_OF_THE_NORTH,  // Emhyr - cancel opponent leader ability
    RELENTLESS,            // Emhyr - draw from opponent discard pile
    WHITE_FLAME,           // Emhyr - pick a weather card from deck

    // Monsters
    BRINGER_OF_DEATH,      // Eredin - pick a weather card from deck
    COMMANDER_OF_THE_RED_RIDERS, // Eredin - pick any card from deck (then discard)
    DESTROYER_OF_WORLDS,   // Eredin - discard 2 cards and draw 1
    KING_OF_THE_WILD_HUNT, // Eredin - restore card from graveyard

    // Scoia'tael
    QUEEN_OF_DOL_BLATHANNA, // Francesca - destroy enemy close if 10+ strength
    DAISY_OF_THE_VALLEY,    // Francesca - pick a weather card from deck
    PUREBLOOD_ELF,          // Francesca - pick any card (then opponent does too)
    HOPE_OF_THE_AEN_SEIDHE, // Francesca - +1 to agile units

    // Skellige
    KING_BRAN,             // Crach - shuffle graveyard back into deck
    CLAN_AN_CRAITE          // Crach - restore 2 cards from graveyard
}

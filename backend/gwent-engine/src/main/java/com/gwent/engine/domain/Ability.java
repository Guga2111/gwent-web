package com.gwent.engine.domain;

public enum Ability {
    SPY,
    MEDIC,
    TIGHT_BOND,
    MORALE_BOOST,
    MUSTER,
    BERSERKER,
    SCORCH,
    AGILE,
    DUMMY,
    COMMANDERS_HORN,
    FROST,
    FOG,
    RAIN,
    MARDROEME,
    CLEAR_WEATHER,
    SKELLIGE_STORM,
    KAMBI;

    public boolean isWeather() {
        return this == FROST || this == FOG || this == RAIN || this == CLEAR_WEATHER || this == SKELLIGE_STORM;
    }
}

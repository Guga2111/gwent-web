package com.gwent.engine.domain;

import java.util.Map;
import java.util.Set;

import static java.util.Map.entry;

public final class MusterGroups {

    private MusterGroups() {}

    private static final Set<String> ARACHAS = Set.of("Arachas", "Arachas Behemoth");
    private static final Set<String> CERYS = Set.of("Cerys an Craite", "Shield Maiden");
    private static final Set<String> CRONES = Set.of("Crone: Brewess", "Crone: Whispess", "Crone: Weavess");
    private static final Set<String> VAMPIRES = Set.of(
            "Vampire: Katakan", "Vampire: Bruxa", "Vampire: Ekimmara",
            "Vampire: Fleder", "Vampire: Garkain");

    private static final Map<String, Set<String>> GROUPS = Map.ofEntries(
            entry("Arachas", ARACHAS),
            entry("Arachas Behemoth", ARACHAS),
            entry("Cerys an Craite", CERYS),
            entry("Shield Maiden", CERYS),
            entry("Crone: Brewess", CRONES),
            entry("Crone: Whispess", CRONES),
            entry("Crone: Weavess", CRONES),
            entry("Vampire: Katakan", VAMPIRES),
            entry("Vampire: Bruxa", VAMPIRES),
            entry("Vampire: Ekimmara", VAMPIRES),
            entry("Vampire: Fleder", VAMPIRES),
            entry("Vampire: Garkain", VAMPIRES)
    );

    public static Set<String> getMusterNames(String cardName) {
        return GROUPS.getOrDefault(cardName, Set.of(cardName));
    }
}

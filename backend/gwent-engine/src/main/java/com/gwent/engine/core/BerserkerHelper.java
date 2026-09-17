package com.gwent.engine.core;

import com.gwent.engine.domain.Ability;
import com.gwent.engine.domain.Card;
import com.gwent.engine.domain.CardType;
import com.gwent.engine.domain.Faction;
import com.gwent.engine.state.BoardRow;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class BerserkerHelper {

    record TransformEntry(String originalName, int originalPower, String transformedName, Ability transformedAbility, int transformedPower) {}

    private static final String TRANSFORMED_SUFFIX = "_TRANSFORMED";

    private static final List<TransformEntry> ENTRIES = List.of(
            new TransformEntry("Vildkaarl", 8, "Transformed Vildkaarl", Ability.MORALE_BOOST, 14),
            new TransformEntry("Berserker Marauder", 4, "Transformed Marauder", null, 8)
    );

    private static final Map<String, TransformEntry> BY_ORIGINAL_NAME =
            ENTRIES.stream().collect(Collectors.toMap(TransformEntry::originalName, e -> e));

    private static final Map<String, TransformEntry> BY_TRANSFORMED_NAME =
            ENTRIES.stream().collect(Collectors.toMap(TransformEntry::transformedName, e -> e));

    private BerserkerHelper() {}

    static void transformBerserkers(BoardRow row) {
        List<Card> berserkers = row.getCards().stream()
                .filter(c -> c.ability() == Ability.BERSERKER)
                .toList();

        for (Card berserker : berserkers) {
            TransformEntry entry = BY_ORIGINAL_NAME.get(berserker.name());
            if (entry == null) continue;

            row.removeCard(berserker);
            row.addCard(new Card(
                    berserker.id() + TRANSFORMED_SUFFIX, entry.transformedName(),
                    Faction.SKELLIGE, CardType.UNIT,
                    entry.transformedAbility(), null,
                    berserker.rowType(), entry.transformedPower()
            ));
        }
    }

    static Card revertIfTransformed(Card card) {
        if (!card.id().endsWith(TRANSFORMED_SUFFIX)) return card;

        TransformEntry entry = BY_TRANSFORMED_NAME.get(card.name());
        if (entry == null) return card;

        String originalId = card.id().substring(0, card.id().length() - TRANSFORMED_SUFFIX.length());
        return new Card(
                originalId, entry.originalName(),
                Faction.SKELLIGE, CardType.UNIT,
                Ability.BERSERKER, null,
                card.rowType(), entry.originalPower()
        );
    }
}

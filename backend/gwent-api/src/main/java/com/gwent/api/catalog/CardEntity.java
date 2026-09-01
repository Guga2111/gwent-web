package com.gwent.api.catalog;

import com.gwent.engine.domain.*;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "card_catalog")
@Getter
@Setter
@NoArgsConstructor
public class CardEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Faction faction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardType cardType;

    @Enumerated(EnumType.STRING)
    private Ability ability;

    @Enumerated(EnumType.STRING)
    private LeaderAbility leaderAbility;

    @Enumerated(EnumType.STRING)
    private RowType rowType;

    private Integer basePower;

    @Column(nullable = false)
    private int deckCopies;
}
package com.gwent.api.catalog;

import com.gwent.engine.domain.*;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CardCatalogLoader implements ApplicationRunner {

    private final CardCatalogRepository repository;

    public CardCatalogLoader(CardCatalogRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (repository.count() > 0) return;
        repository.saveAll(allCards());
    }

    private List<CardEntity> allCards() {
        return List.of(

            // ── NORTHERN REALMS ──────────────────────────────────────────────

            // Leader
            card("NR_LEADER_FOLTEST", "Foltest: Lord Commander of the North",
                    Faction.NORTHERN_REALMS, CardType.LEADER,
                    null, LeaderAbility.SIEGE_MASTER, null, null, 0),

            // Heroes
            card("NR_HERO_GERALT",        "Geralt of Rivia",       Faction.NORTHERN_REALMS, CardType.HERO, null,                null, RowType.MELEE,  15, 1),
            card("NR_HERO_CIRI",          "Ciri",                  Faction.NORTHERN_REALMS, CardType.HERO, null,                null, RowType.MELEE,  15, 1),
            card("NR_HERO_TRISS",         "Triss Merigold",        Faction.NORTHERN_REALMS, CardType.HERO, null,                null, RowType.MELEE,   7, 1),
            card("NR_HERO_YENNEFER",      "Yennefer of Vengerberg",Faction.NORTHERN_REALMS, CardType.HERO, Ability.MEDIC,       null, RowType.RANGED,  7, 1),
            card("NR_HERO_PHILIPPA",      "Philippa Eilhart",      Faction.NORTHERN_REALMS, CardType.HERO, Ability.SPY,         null, RowType.RANGED, 10, 1),
            card("NR_HERO_VERNON_ROCHE",  "Vernon Roche",          Faction.NORTHERN_REALMS, CardType.HERO, null,                null, RowType.MELEE,  10, 1),

            // Units
            card("NR_DIJKSTRA",       "Dijkstra",                       Faction.NORTHERN_REALMS, CardType.UNIT, Ability.SPY,          null, RowType.RANGED, 4, 1),
            card("NR_PRINCE_STENNIS", "Prince Stennis",                 Faction.NORTHERN_REALMS, CardType.UNIT, Ability.SPY,          null, RowType.MELEE,  5, 1),
            card("NR_BLUE_STRIPES",   "Blue Stripes Commando",          Faction.NORTHERN_REALMS, CardType.UNIT, Ability.TIGHT_BOND,   null, RowType.MELEE,  4, 3),
            card("NR_POOR_INFANTRY",  "Poor Fucking Infantry",          Faction.NORTHERN_REALMS, CardType.UNIT, Ability.TIGHT_BOND,   null, RowType.MELEE,  1, 3),
            card("NR_CRINFRID",       "Crinfrid Reavers Dragon Hunter", Faction.NORTHERN_REALMS, CardType.UNIT, Ability.TIGHT_BOND,   null, RowType.RANGED, 5, 3),
            card("NR_KAEDWENI_SIEGE", "Kaedweni Siege Expert",          Faction.NORTHERN_REALMS, CardType.UNIT, Ability.MORALE_BOOST, null, RowType.SIEGE,  1, 2),
            card("NR_BALLISTA",       "Ballista",                       Faction.NORTHERN_REALMS, CardType.UNIT, null,                 null, RowType.SIEGE,  6, 1),
            card("NR_CATAPULT",       "Catapult",                       Faction.NORTHERN_REALMS, CardType.UNIT, Ability.TIGHT_BOND,   null, RowType.SIEGE,  8, 2),
            card("NR_SIEGE_TOWER",    "Siege Tower",                    Faction.NORTHERN_REALMS, CardType.UNIT, null,                 null, RowType.SIEGE,  6, 1),

            // ── NILFGAARD ────────────────────────────────────────────────────

            // Leader
            card("NG_LEADER_EMHYR", "Emhyr var Emreis: The White Flame",
                    Faction.NILFGAARD, CardType.LEADER,
                    null, LeaderAbility.WHITE_FLAME, null, null, 0),

            // Heroes
            card("NG_HERO_MORVRAN", "Morvran Voorhis",   Faction.NILFGAARD, CardType.HERO, null,          null, RowType.RANGED, 10, 1),
            card("NG_HERO_MENNO",   "Menno Coehoorn",    Faction.NILFGAARD, CardType.HERO, null,          null, RowType.MELEE,  10, 1),
            card("NG_HERO_LETHO",   "Letho of Gulet",    Faction.NILFGAARD, CardType.HERO, null,          null, RowType.MELEE,  10, 1),
            card("NG_HERO_TIBOR",   "Tibor Eggebracht",  Faction.NILFGAARD, CardType.HERO, null,          null, RowType.RANGED, 10, 1),
            card("NG_HERO_STEFAN",  "Stefan Skellen",    Faction.NILFGAARD, CardType.HERO, Ability.SPY,   null, RowType.RANGED,  6, 1),

            // Units
            card("NG_CYNTHIA",       "Cynthia",                           Faction.NILFGAARD, CardType.UNIT, Ability.SPY,          null, RowType.RANGED,  5, 1),
            card("NG_RENUALD",       "Renuald aep Matsen",                Faction.NILFGAARD, CardType.UNIT, Ability.SPY,          null, RowType.RANGED,  5, 1),
            card("NG_FRINGILLA",     "Fringilla Vigo",                    Faction.NILFGAARD, CardType.UNIT, Ability.MORALE_BOOST, null, RowType.RANGED,  5, 1),
            card("NG_IMPERA",        "Impera Brigade Guard",              Faction.NILFGAARD, CardType.UNIT, Ability.TIGHT_BOND,   null, RowType.MELEE,   3, 4),
            card("NG_NAUSICAA",      "Nausicaa Cavalry Rider",            Faction.NILFGAARD, CardType.UNIT, Ability.TIGHT_BOND,   null, RowType.MELEE,   2, 3),
            card("NG_BLACK_ARCHER",  "Black Infantry Archer",             Faction.NILFGAARD, CardType.UNIT, Ability.TIGHT_BOND,   null, RowType.RANGED,  3, 3),
            card("NG_SIEGE_ENGINEER","Siege Engineer",                    Faction.NILFGAARD, CardType.UNIT, null,                 null, RowType.SIEGE,   6, 1),
            card("NG_ZERRIKANIAN",   "Heavy Zerrikanian Fire Scorpion",   Faction.NILFGAARD, CardType.UNIT, null,                 null, RowType.SIEGE,  10, 1),

            // ── MONSTERS ─────────────────────────────────────────────────────

            // Leader
            card("MO_LEADER_EREDIN", "Eredin Breacc Glas: Destroyer of Worlds",
                    Faction.MONSTER, CardType.LEADER,
                    null, LeaderAbility.DESTROYER_OF_WORLDS, null, null, 0),

            // Heroes
            card("MO_HERO_IMLERITH",  "Imlerith",  Faction.MONSTER, CardType.HERO, null,        null, RowType.MELEE,  10, 1),
            card("MO_HERO_GELS",      "Ge'els",    Faction.MONSTER, CardType.HERO, Ability.SPY, null, RowType.RANGED,  6, 1),
            card("MO_HERO_CARANTHIR", "Caranthir", Faction.MONSTER, CardType.HERO, null,        null, RowType.RANGED,  8, 1),
            card("MO_HERO_NITHRAL",   "Nithral",   Faction.MONSTER, CardType.HERO, null,        null, RowType.MELEE,   6, 1),

            // Units
            card("MO_NEKKER",           "Nekker",           Faction.MONSTER, CardType.UNIT, Ability.MUSTER, null, RowType.MELEE,  2, 3),
            card("MO_ARACHAS",          "Arachas",          Faction.MONSTER, CardType.UNIT, Ability.MUSTER, null, RowType.MELEE,  4, 3),
            card("MO_GHOUL",            "Ghoul",            Faction.MONSTER, CardType.UNIT, Ability.MEDIC,  null, RowType.MELEE,  6, 1),
            card("MO_EARTH_ELEMENTAL",  "Earth Elemental",  Faction.MONSTER, CardType.UNIT, Ability.MUSTER, null, RowType.SIEGE,  6, 1),
            card("MO_GOLEM",            "Golem",            Faction.MONSTER, CardType.UNIT, null,           null, RowType.SIEGE,  6, 1),
            card("MO_WYVERN",           "Wyvern",           Faction.MONSTER, CardType.UNIT, null,           null, RowType.RANGED, 7, 2),
            card("MO_CRONE_BREWESS",    "Crone: Brewess",   Faction.MONSTER, CardType.UNIT, Ability.MUSTER, null, RowType.MELEE,  5, 1),
            card("MO_CRONE_WHISPESS",   "Crone: Whispess",  Faction.MONSTER, CardType.UNIT, Ability.MUSTER, null, RowType.RANGED, 5, 1),
            card("MO_CRONE_WEAVESS",    "Crone: Weavess",   Faction.MONSTER, CardType.UNIT, Ability.MUSTER, null, RowType.MELEE,  5, 1),
            card("MO_VAMPIRE_KATAKAN",  "Vampire: Katakan", Faction.MONSTER, CardType.UNIT, Ability.MUSTER, null, RowType.RANGED, 5, 1),
            card("MO_FOGLET",           "Foglet",           Faction.MONSTER, CardType.UNIT, null,           null, RowType.RANGED, 2, 2),

            // ── SCOIA'TAEL ───────────────────────────────────────────────────

            // Leader
            card("ST_LEADER_FRANCESCA", "Francesca Findabair: The Beautiful",
                    Faction.SCOIATAEL, CardType.LEADER,
                    null, LeaderAbility.DAISY_OF_THE_VALLEY, null, null, 0),

            // Heroes
            card("ST_HERO_ISENGRIM", "Isengrim Faoiltiarna",    Faction.SCOIATAEL, CardType.HERO, Ability.MORALE_BOOST, null, RowType.MELEE,  10, 1),
            card("ST_HERO_MILVA",    "Milva",                   Faction.SCOIATAEL, CardType.HERO, Ability.MORALE_BOOST, null, RowType.RANGED,  7, 1),
            card("ST_HERO_IDA",      "Ida Emean aep Sivney",    Faction.SCOIATAEL, CardType.HERO, null,                 null, RowType.RANGED,  6, 1),
            card("ST_HERO_SASKIA",   "Saskia",                  Faction.SCOIATAEL, CardType.HERO, Ability.MORALE_BOOST, null, RowType.MELEE,  10, 1),

            // Units
            card("ST_YAEVINN",        "Yaevinn",                  Faction.SCOIATAEL, CardType.UNIT, Ability.SPY,          null, RowType.RANGED, 6, 1),
            card("ST_DENNIS",         "Dennis Cranmer",            Faction.SCOIATAEL, CardType.UNIT, null,                 null, RowType.MELEE,  6, 1),
            card("ST_VRIHEDD",        "Vrihedd Brigade Veteran",   Faction.SCOIATAEL, CardType.UNIT, Ability.AGILE,        null, RowType.RANGED, 5, 3),
            card("ST_MAHAKAM",        "Mahakam Defender",          Faction.SCOIATAEL, CardType.UNIT, Ability.TIGHT_BOND,   null, RowType.MELEE,  5, 4),
            card("ST_DOL_BLATHANNA", "Dol Blathanna Protector",   Faction.SCOIATAEL, CardType.UNIT, Ability.TIGHT_BOND,   null, RowType.SIEGE,  5, 3),
            card("ST_HAWKER_HEALER",  "Hawker Healer",             Faction.SCOIATAEL, CardType.UNIT, Ability.MEDIC,        null, RowType.RANGED, 2, 3),
            card("ST_HAWKER_SMUGGLER","Hawker Smuggler",           Faction.SCOIATAEL, CardType.UNIT, Ability.AGILE,        null, RowType.RANGED, 4, 3),
            card("ST_DOL_SCOUT",      "Dol Blathanna Scout",       Faction.SCOIATAEL, CardType.UNIT, Ability.MORALE_BOOST, null, RowType.RANGED, 6, 1),

            // ── SKELLIGE ─────────────────────────────────────────────────────

            // Leader
            card("SK_LEADER_BRAN", "King Bran",
                    Faction.SKELLIGE, CardType.LEADER,
                    null, LeaderAbility.KING_BRAN, null, null, 0),

            // Heroes
            card("SK_HERO_CRACH",   "Crach an Craite",  Faction.SKELLIGE, CardType.HERO, null,        null, RowType.MELEE,  10, 1),
            card("SK_HERO_CERYS",   "Cerys an Craite",  Faction.SKELLIGE, CardType.HERO, null,        null, RowType.MELEE,  10, 1),
            card("SK_HERO_HJALMAR", "Hjalmar an Craite",Faction.SKELLIGE, CardType.HERO, null,        null, RowType.MELEE,  12, 1),
            card("SK_HERO_BIRNA",   "Birna Bran",       Faction.SKELLIGE, CardType.HERO, Ability.SPY, null, RowType.RANGED,  8, 1),

            // Units
            card("SK_BERSERKER",    "Berserker",                Faction.SKELLIGE, CardType.UNIT, Ability.BERSERKER,  null, RowType.MELEE,  4, 3),
            card("SK_CLAN_AN_CRAITE","Clan an Craite Warrior",  Faction.SKELLIGE, CardType.UNIT, Ability.TIGHT_BOND, null, RowType.MELEE,  5, 4),
            card("SK_ASSAULT_TEAM", "Skellige Assault Team",    Faction.SKELLIGE, CardType.UNIT, Ability.TIGHT_BOND, null, RowType.RANGED, 3, 3),
            card("SK_PIRATE",       "Clan Dimun Pirate",        Faction.SKELLIGE, CardType.UNIT, null,               null, RowType.RANGED, 5, 2),
            card("SK_ARMORSMITH",   "Clan Tordarroch Armorsmith",Faction.SKELLIGE,CardType.UNIT, Ability.MEDIC,      null, RowType.RANGED, 5, 1),
            card("SK_BROKVAR",      "Clan Brokvar Hunter",      Faction.SKELLIGE, CardType.UNIT, Ability.TIGHT_BOND, null, RowType.RANGED, 5, 2),
            card("SK_SHIELD_MAIDEN","Shield Maiden",            Faction.SKELLIGE, CardType.UNIT, Ability.TIGHT_BOND, null, RowType.MELEE,  4, 3),

            // ── NEUTRAL ──────────────────────────────────────────────────────

            // Weather (deckCopies=0 — available for deck builder, not in default decks)
            card("NEUTRAL_FROST",         "Biting Frost",       Faction.NEUTRAL, CardType.WEATHER, Ability.FROST,         null, null, null, 0),
            card("NEUTRAL_FOG",           "Impenetrable Fog",   Faction.NEUTRAL, CardType.WEATHER, Ability.FOG,           null, null, null, 0),
            card("NEUTRAL_RAIN",          "Torrential Rain",    Faction.NEUTRAL, CardType.WEATHER, Ability.RAIN,          null, null, null, 0),
            card("NEUTRAL_CLEAR_WEATHER", "Clear Weather",      Faction.NEUTRAL, CardType.WEATHER, Ability.CLEAR_WEATHER, null, null, null, 0),

            // Neutral units/heroes (deckCopies=0 — for deck builder)
            card("NEUTRAL_VILLENTRETENMERTH", "Villentretenmerth",  Faction.NEUTRAL, CardType.HERO, Ability.SCORCH,       null, RowType.MELEE,  7, 0),
            card("NEUTRAL_ZOLTAN",            "Zoltan Chivay",      Faction.NEUTRAL, CardType.UNIT, null,                 null, RowType.MELEE,  5, 0),
            card("NEUTRAL_EMIEL_REGIS",       "Emiel Regis",        Faction.NEUTRAL, CardType.UNIT, Ability.MEDIC,        null, RowType.RANGED, 5, 0),
            card("NEUTRAL_DANDELION",         "Dandelion",          Faction.NEUTRAL, CardType.UNIT, Ability.MORALE_BOOST, null, RowType.RANGED, 2, 0)
        );
    }

    private CardEntity card(String id, String name, Faction faction, CardType cardType,
                            Ability ability, LeaderAbility leaderAbility,
                            RowType rowType, Integer basePower, int deckCopies) {
        CardEntity e = new CardEntity();
        e.setId(id);
        e.setName(name);
        e.setFaction(faction);
        e.setCardType(cardType);
        e.setAbility(ability);
        e.setLeaderAbility(leaderAbility);
        e.setRowType(rowType);
        e.setBasePower(basePower);
        e.setDeckCopies(deckCopies);
        return e;
    }
}

package com.gwent.api.catalog;

import com.gwent.engine.domain.*;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(1)
public class CardCatalogLoader implements ApplicationRunner {

    private final CardCatalogRepository repository;

    public CardCatalogLoader(CardCatalogRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(ApplicationArguments args) {
        long currentCount = repository.count();
        if (currentCount > 0 && currentCount == allCards().size()) return;
        repository.deleteAll();
        repository.saveAll(allCards());
    }

    private List<CardEntity> allCards() {
        return List.of(

            // ── NORTHERN REALMS ──────────────────────────────────────────────

            // Leaders
            card("NR_LEADER_KING_OF_TEMERIA",    "Foltest: King of Temeria",             Faction.NORTHERN_REALMS, CardType.LEADER, null, LeaderAbility.KING_OF_TEMERIA,   null, null, 0),
            card("NR_LEADER_LORD_COMMANDER",     "Foltest: Lord Commander of the North", Faction.NORTHERN_REALMS, CardType.LEADER, null, LeaderAbility.LORD_COMMANDER,    null, null, 0),
            card("NR_LEADER_SON_OF_MEDELL",      "Foltest: Son of Medell",               Faction.NORTHERN_REALMS, CardType.LEADER, null, LeaderAbility.SON_OF_MEDELL,     null, null, 0),
            card("NR_LEADER_SIEGEMASTER",        "Foltest: The Siegemaster",             Faction.NORTHERN_REALMS, CardType.LEADER, null, LeaderAbility.SIEGE_MASTER,      null, null, 0),
            card("NR_LEADER_STEEL_FORGED",       "Foltest: The Steel-Forged",            Faction.NORTHERN_REALMS, CardType.LEADER, null, LeaderAbility.STEEL_FORGED,      null, null, 0),

            // Heroes
            card("NR_HERO_PHILIPPA",      "Philippa Eilhart",      Faction.NORTHERN_REALMS, CardType.HERO, null,                null, RowType.RANGED, 10, 1),
            card("NR_HERO_VERNON_ROCHE",  "Vernon Roche",          Faction.NORTHERN_REALMS, CardType.HERO, null,                null, RowType.MELEE,  10, 1),
            card("NR_HERO_ESTERAD",       "Esterad Thyssen",       Faction.NORTHERN_REALMS, CardType.HERO, null,                null, RowType.MELEE,  10, 1),
            card("NR_HERO_JOHN_NATALIS",  "John Natalis",          Faction.NORTHERN_REALMS, CardType.HERO, null,                null, RowType.MELEE,  10, 1),

            // Units
            card("NR_DIJKSTRA",       "Dijkstra",                       Faction.NORTHERN_REALMS, CardType.UNIT, Ability.SPY,          null, RowType.MELEE,  4, 1),
            card("NR_PRINCE_STENNIS", "Prince Stennis",                 Faction.NORTHERN_REALMS, CardType.UNIT, Ability.SPY,          null, RowType.MELEE,  5, 1),
            card("NR_THALER",         "Thaler",                         Faction.NORTHERN_REALMS, CardType.UNIT, Ability.SPY,          null, RowType.SIEGE,  1, 1),
            card("NR_BLUE_STRIPES",   "Blue Stripes Commando",          Faction.NORTHERN_REALMS, CardType.UNIT, Ability.TIGHT_BOND,   null, RowType.MELEE,  4, 3),
            card("NR_POOR_INFANTRY",  "Poor Fucking Infantry",          Faction.NORTHERN_REALMS, CardType.UNIT, Ability.TIGHT_BOND,   null, RowType.MELEE,  1, 3),
            card("NR_CRINFRID",       "Crinfrid Reavers Dragon Hunter", Faction.NORTHERN_REALMS, CardType.UNIT, Ability.TIGHT_BOND,   null, RowType.RANGED, 5, 3),
            card("NR_KAEDWENI_SIEGE", "Kaedweni Siege Expert",          Faction.NORTHERN_REALMS, CardType.UNIT, Ability.MORALE_BOOST, null, RowType.SIEGE,  1, 3),
            card("NR_DUN_BANNER",     "Dun Banner Medic",               Faction.NORTHERN_REALMS, CardType.UNIT, Ability.MEDIC,        null, RowType.SIEGE,  5, 1),
            card("NR_DETHMOLD",       "Dethmold",                       Faction.NORTHERN_REALMS, CardType.UNIT, null,                 null, RowType.RANGED, 6, 1),
            card("NR_KEIRA_METZ",     "Keira Metz",                     Faction.NORTHERN_REALMS, CardType.UNIT, null,                 null, RowType.RANGED, 5, 1),
            card("NR_SABRINA",        "Sabrina Glevissig",              Faction.NORTHERN_REALMS, CardType.UNIT, null,                 null, RowType.RANGED, 4, 1),
            card("NR_SHELDON",        "Sheldon Skaggs",                 Faction.NORTHERN_REALMS, CardType.UNIT, null,                 null, RowType.RANGED, 4, 1),
            card("NR_SILE",           "Síle de Tansarville",            Faction.NORTHERN_REALMS, CardType.UNIT, null,                 null, RowType.RANGED, 5, 1),
            card("NR_VES",            "Ves",                            Faction.NORTHERN_REALMS, CardType.UNIT, null,                 null, RowType.MELEE,  5, 1),
            card("NR_SIEGFRIED",      "Siegfried of Denesle",            Faction.NORTHERN_REALMS, CardType.UNIT, null,                 null, RowType.MELEE,  5, 1),
            card("NR_YARPEN",         "Yarpen Zigrin",                  Faction.NORTHERN_REALMS, CardType.UNIT, null,                 null, RowType.MELEE,  2, 1),
            card("NR_REDANIAN",       "Redanian Foot Soldier",          Faction.NORTHERN_REALMS, CardType.UNIT, null,                 null, RowType.MELEE,  1, 2),
            card("NR_BALLISTA",       "Ballista",                       Faction.NORTHERN_REALMS, CardType.UNIT, null,                 null, RowType.SIEGE,  6, 1),
            card("NR_CATAPULT",       "Catapult",                       Faction.NORTHERN_REALMS, CardType.UNIT, Ability.TIGHT_BOND,   null, RowType.SIEGE,  8, 2),
            card("NR_TREBUCHET",      "Trebuchet",                      Faction.NORTHERN_REALMS, CardType.UNIT, null,                 null, RowType.SIEGE,  6, 2),
            card("NR_SIEGE_TOWER",    "Siege Tower",                    Faction.NORTHERN_REALMS, CardType.UNIT, null,                 null, RowType.SIEGE,  6, 1),

            // ── NILFGAARD ────────────────────────────────────────────────────

            // Leaders
            card("NG_LEADER_EMPEROR",       "Emhyr var Emreis: Emperor of Nilfgaard",   Faction.NILFGAARD, CardType.LEADER, null, LeaderAbility.EMPEROR_OF_NILFGAARD, null, null, 0),
            card("NG_LEADER_IMPERIAL",      "Emhyr var Emreis: His Imperial Majesty",   Faction.NILFGAARD, CardType.LEADER, null, LeaderAbility.IMPERIAL_MAJESTY,     null, null, 0),
            card("NG_LEADER_INVADER",       "Emhyr var Emreis: Invader of the North",   Faction.NILFGAARD, CardType.LEADER, null, LeaderAbility.INVADER_OF_THE_NORTH, null, null, 0),
            card("NG_LEADER_RELENTLESS",    "Emhyr var Emreis: The Relentless",         Faction.NILFGAARD, CardType.LEADER, null, LeaderAbility.RELENTLESS,           null, null, 0),
            card("NG_LEADER_WHITE_FLAME",   "Emhyr var Emreis: The White Flame",        Faction.NILFGAARD, CardType.LEADER, null, LeaderAbility.WHITE_FLAME,          null, null, 0),

            // Heroes
            card("NG_HERO_MORVRAN", "Morvran Voorhis",   Faction.NILFGAARD, CardType.HERO, null,           null, RowType.SIEGE,  10, 1),
            card("NG_HERO_MENNO",   "Menno Coehoorn",    Faction.NILFGAARD, CardType.HERO, Ability.MEDIC,  null, RowType.MELEE,  10, 1),
            card("NG_HERO_LETHO",   "Letho of Gulet",    Faction.NILFGAARD, CardType.HERO, null,           null, RowType.MELEE,  10, 1),
            card("NG_HERO_TIBOR",   "Tibor Eggebracht",  Faction.NILFGAARD, CardType.HERO, null,           null, RowType.RANGED, 10, 1),

            // Units — Spies
            card("NG_STEFAN_SKELLEN",  "Stefan Skellen",          Faction.NILFGAARD, CardType.UNIT, Ability.SPY,          null, RowType.MELEE,   9, 1),
            card("NG_SHILARD",         "Shilard Fitz-Oesterlen",  Faction.NILFGAARD, CardType.UNIT, Ability.SPY,          null, RowType.MELEE,   7, 1),
            card("NG_VATTIER",         "Vattier de Rideaux",      Faction.NILFGAARD, CardType.UNIT, Ability.SPY,          null, RowType.MELEE,   4, 1),

            // Units — Tight Bond
            card("NG_IMPERA",          "Impera Brigade Guard",    Faction.NILFGAARD, CardType.UNIT, Ability.TIGHT_BOND,   null, RowType.MELEE,   3, 4),
            card("NG_NAUSICAA",        "Nausicaa Cavalry Rider",  Faction.NILFGAARD, CardType.UNIT, Ability.TIGHT_BOND,   null, RowType.MELEE,   2, 3),
            card("NG_YOUNG_EMISSARY",  "Young Emissary",          Faction.NILFGAARD, CardType.UNIT, Ability.TIGHT_BOND,   null, RowType.MELEE,   5, 2),

            // Units — Medic
            card("NG_ETOLIAN",         "Etolian Auxiliary Archers",Faction.NILFGAARD, CardType.UNIT, Ability.MEDIC,        null, RowType.RANGED,  1, 2),
            card("NG_SIEGE_TECH",      "Siege Technician",         Faction.NILFGAARD, CardType.UNIT, Ability.MEDIC,        null, RowType.SIEGE,   0, 1),

            // Units — no ability
            card("NG_FRINGILLA",       "Fringilla Vigo",                  Faction.NILFGAARD, CardType.UNIT, null,  null, RowType.RANGED,  6, 1),
            card("NG_ASSIRE",          "Assire var Anahid",               Faction.NILFGAARD, CardType.UNIT, null,  null, RowType.RANGED,  6, 1),
            card("NG_CYNTHIA",         "Cynthia",                         Faction.NILFGAARD, CardType.UNIT, null,  null, RowType.RANGED,  4, 1),
            card("NG_RENUALD",         "Renuald aep Matsen",              Faction.NILFGAARD, CardType.UNIT, null,  null, RowType.RANGED,  5, 1),
            card("NG_BLACK_ARCHER",    "Black Infantry Archer",           Faction.NILFGAARD, CardType.UNIT, null,  null, RowType.RANGED, 10, 2),
            card("NG_VANHEMAR",        "Vanhemar",                        Faction.NILFGAARD, CardType.UNIT, null,  null, RowType.RANGED,  4, 1),
            card("NG_PUTTKAMMER",      "Puttkammer",                      Faction.NILFGAARD, CardType.UNIT, null,  null, RowType.RANGED,  3, 1),
            card("NG_SWEERS",          "Sweers",                          Faction.NILFGAARD, CardType.UNIT, null,  null, RowType.RANGED,  2, 1),
            card("NG_ALBRICH",         "Albrich",                         Faction.NILFGAARD, CardType.UNIT, null,  null, RowType.RANGED,  2, 1),
            card("NG_CAHIR",           "Cahir Mawr Dyffryn aep Ceallach", Faction.NILFGAARD, CardType.UNIT, null,  null, RowType.MELEE,   6, 1),
            card("NG_MORTEISEN",       "Morteisen",                       Faction.NILFGAARD, CardType.UNIT, null,  null, RowType.MELEE,   3, 1),
            card("NG_RAINFARN",        "Rainfarn",                        Faction.NILFGAARD, CardType.UNIT, null,  null, RowType.MELEE,   4, 1),
            card("NG_VREEMDE",         "Vreemde",                         Faction.NILFGAARD, CardType.UNIT, null,  null, RowType.MELEE,   2, 1),
            card("NG_SIEGE_ENGINEER",  "Siege Engineer",                  Faction.NILFGAARD, CardType.UNIT, null,  null, RowType.SIEGE,   6, 1),
            card("NG_ZERRIKANIAN_H",   "Heavy Zerrikanian Fire Scorpion", Faction.NILFGAARD, CardType.UNIT, null,  null, RowType.SIEGE,  10, 1),
            card("NG_ZERRIKANIAN",     "Zerrikanian Fire Scorpion",       Faction.NILFGAARD, CardType.UNIT, null,  null, RowType.SIEGE,   5, 1),
            card("NG_ROTTEN_MANGONEL", "Rotten Mangonel",                 Faction.NILFGAARD, CardType.UNIT, null,  null, RowType.SIEGE,   3, 1),

            // ── MONSTERS ─────────────────────────────────────────────────────

            // Leaders
            card("MO_LEADER_TREACHEROUS",   "Eredin: The Treacherous",              Faction.MONSTER, CardType.LEADER, null, LeaderAbility.TREACHEROUS,                  null, null, 0),
            card("MO_LEADER_BRINGER",       "Eredin: Bringer of Death",             Faction.MONSTER, CardType.LEADER, null, LeaderAbility.BRINGER_OF_DEATH,             null, null, 0),
            card("MO_LEADER_COMMANDER",     "Eredin: Commander of the Red Riders",  Faction.MONSTER, CardType.LEADER, null, LeaderAbility.COMMANDER_OF_THE_RED_RIDERS,  null, null, 0),
            card("MO_LEADER_DESTROYER",     "Eredin: Destroyer of Worlds",          Faction.MONSTER, CardType.LEADER, null, LeaderAbility.DESTROYER_OF_WORLDS,          null, null, 0),
            card("MO_LEADER_WILD_HUNT",     "Eredin: King of the Wild Hunt",        Faction.MONSTER, CardType.LEADER, null, LeaderAbility.KING_OF_THE_WILD_HUNT,        null, null, 0),

            // Heroes
            card("MO_HERO_IMLERITH",  "Imlerith",  Faction.MONSTER, CardType.HERO, null,                null, RowType.MELEE,  10, 1),
            card("MO_HERO_DRAUG",     "Draug",     Faction.MONSTER, CardType.HERO, null,                null, RowType.MELEE,  10, 1),
            card("MO_HERO_LESHEN",    "Leshen",    Faction.MONSTER, CardType.HERO, null,                null, RowType.RANGED, 10, 1),
            card("MO_HERO_KAYRAN",    "Kayran",    Faction.MONSTER, CardType.HERO, Ability.AGILE, Ability.MORALE_BOOST, null, RowType.RANGED,  8, 1),

            // Units — Muster
            card("MO_NEKKER",           "Nekker",            Faction.MONSTER, CardType.UNIT, Ability.MUSTER, null, RowType.MELEE,  2, 3),
            card("MO_ARACHAS",          "Arachas",           Faction.MONSTER, CardType.UNIT, Ability.MUSTER, null, RowType.MELEE,  4, 3),
            card("MO_ARACHAS_BEHEMOTH", "Arachas Behemoth",  Faction.MONSTER, CardType.UNIT, Ability.MUSTER, null, RowType.SIEGE,  6, 1),
            card("MO_GHOUL",            "Ghoul",             Faction.MONSTER, CardType.UNIT, Ability.MUSTER, null, RowType.MELEE,  1, 3),
            card("MO_CRONE_BREWESS",    "Crone: Brewess",    Faction.MONSTER, CardType.UNIT, Ability.MUSTER, null, RowType.MELEE,  6, 1),
            card("MO_CRONE_WHISPESS",   "Crone: Whispess",   Faction.MONSTER, CardType.UNIT, Ability.MUSTER, null, RowType.MELEE,  6, 1),
            card("MO_CRONE_WEAVESS",    "Crone: Weavess",    Faction.MONSTER, CardType.UNIT, Ability.MUSTER, null, RowType.MELEE,  6, 1),
            card("MO_VAMPIRE_KATAKAN",  "Vampire: Katakan",  Faction.MONSTER, CardType.UNIT, Ability.MUSTER, null, RowType.MELEE,  5, 1),
            card("MO_VAMPIRE_BRUXA",    "Vampire: Bruxa",    Faction.MONSTER, CardType.UNIT, Ability.MUSTER, null, RowType.MELEE,  4, 1),
            card("MO_VAMPIRE_EKIMMARA", "Vampire: Ekimmara", Faction.MONSTER, CardType.UNIT, Ability.MUSTER, null, RowType.MELEE,  4, 1),
            card("MO_VAMPIRE_FLEDER",   "Vampire: Fleder",   Faction.MONSTER, CardType.UNIT, Ability.MUSTER, null, RowType.MELEE,  4, 1),
            card("MO_VAMPIRE_GARKAIN",  "Vampire: Garkain",  Faction.MONSTER, CardType.UNIT, Ability.MUSTER, null, RowType.MELEE,  4, 1),

            // Units — Agile
            card("MO_HARPY",            "Harpy",             Faction.MONSTER, CardType.UNIT, Ability.AGILE,  null, RowType.RANGED, 2, 1),
            card("MO_CELAENO_HARPY",    "Celaeno Harpy",     Faction.MONSTER, CardType.UNIT, Ability.AGILE,  null, RowType.RANGED, 2, 1),

            // Units — Scorch
            card("MO_TOAD",             "Toad",              Faction.MONSTER, CardType.UNIT, Ability.SCORCH, null, RowType.RANGED, 7, 1),

            // Units — no ability
            card("MO_EARTH_ELEMENTAL",  "Earth Elemental",   Faction.MONSTER, CardType.UNIT, null,           null, RowType.SIEGE,  6, 1),
            card("MO_FIRE_ELEMENTAL",   "Fire Elemental",    Faction.MONSTER, CardType.UNIT, null,           null, RowType.SIEGE,  6, 1),
            card("MO_ICE_GIANT",        "Ice Giant",         Faction.MONSTER, CardType.UNIT, null,           null, RowType.SIEGE,  5, 1),
            card("MO_WYVERN",           "Wyvern",            Faction.MONSTER, CardType.UNIT, null,           null, RowType.RANGED, 2, 1),
            card("MO_FOGLET",           "Foglet",            Faction.MONSTER, CardType.UNIT, null,           null, RowType.MELEE,  2, 1),
            card("MO_COCKATRICE",       "Cockatrice",        Faction.MONSTER, CardType.UNIT, null,           null, RowType.RANGED, 2, 1),
            card("MO_ENDREGA",          "Endrega",           Faction.MONSTER, CardType.UNIT, null,           null, RowType.RANGED, 2, 1),
            card("MO_GARGOYLE",         "Gargoyle",          Faction.MONSTER, CardType.UNIT, null,           null, RowType.RANGED, 2, 1),
            card("MO_GRAVE_HAG",        "Grave Hag",         Faction.MONSTER, CardType.UNIT, null,           null, RowType.RANGED, 5, 1),
            card("MO_BOTCHLING",        "Botchling",         Faction.MONSTER, CardType.UNIT, null,           null, RowType.MELEE,  4, 1),
            card("MO_FIEND",            "Fiend",             Faction.MONSTER, CardType.UNIT, null,           null, RowType.MELEE,  6, 1),
            card("MO_FORKTAIL",         "Forktail",          Faction.MONSTER, CardType.UNIT, null,           null, RowType.MELEE,  5, 1),
            card("MO_FRIGHTENER",       "Frightener",        Faction.MONSTER, CardType.UNIT, null,           null, RowType.MELEE,  5, 1),
            card("MO_GRIFFIN",           "Griffin",           Faction.MONSTER, CardType.UNIT, null,           null, RowType.MELEE,  5, 1),
            card("MO_PLAGUE_MAIDEN",    "Plague Maiden",     Faction.MONSTER, CardType.UNIT, null,           null, RowType.MELEE,  5, 1),
            card("MO_WEREWOLF",         "Werewolf",          Faction.MONSTER, CardType.UNIT, null,           null, RowType.MELEE,  5, 1),

            // ── SCOIA'TAEL ───────────────────────────────────────────────────

            // Leaders
            card("ST_LEADER_DAISY",         "Francesca Findabair: Daisy of the Valley",      Faction.SCOIATAEL, CardType.LEADER, null, LeaderAbility.DAISY_OF_THE_VALLEY,    null, null, 0),
            card("ST_LEADER_HOPE",          "Francesca Findabair: Hope of the Aen Seidhe",   Faction.SCOIATAEL, CardType.LEADER, null, LeaderAbility.HOPE_OF_THE_AEN_SEIDHE, null, null, 0),
            card("ST_LEADER_PUREBLOOD",     "Francesca Findabair: Pureblood Elf",            Faction.SCOIATAEL, CardType.LEADER, null, LeaderAbility.PUREBLOOD_ELF,           null, null, 0),
            card("ST_LEADER_QUEEN",         "Francesca Findabair: Queen of Dol Blathanna",   Faction.SCOIATAEL, CardType.LEADER, null, LeaderAbility.QUEEN_OF_DOL_BLATHANNA, null, null, 0),
            card("ST_LEADER_BEAUTIFUL",     "Francesca Findabair: The Beautiful",             Faction.SCOIATAEL, CardType.LEADER, null, LeaderAbility.THE_BEATIFUL,            null, null, 0),

            // Heroes
            card("ST_HERO_ISENGRIM", "Isengrim Faoiltiarna",    Faction.SCOIATAEL, CardType.HERO, Ability.MORALE_BOOST, null, RowType.MELEE,  10, 1),
            card("ST_HERO_SASKIA",   "Saesenthessis",           Faction.SCOIATAEL, CardType.HERO, null,                 null, RowType.RANGED, 10, 1),
            card("ST_HERO_EITHNE",   "Eithné",                  Faction.SCOIATAEL, CardType.HERO, null,                 null, RowType.RANGED, 10, 1),
            card("ST_HERO_IORVETH",  "Iorveth",                 Faction.SCOIATAEL, CardType.HERO, null,                 null, RowType.RANGED, 10, 1),

            // Units — Agile
            card("ST_YAEVINN",        "Yaevinn",                   Faction.SCOIATAEL, CardType.UNIT, Ability.AGILE,  null, RowType.RANGED, 6, 1),
            card("ST_VRIHEDD",        "Vrihedd Brigade Veteran",   Faction.SCOIATAEL, CardType.UNIT, Ability.AGILE,  null, RowType.RANGED, 5, 2),
            card("ST_DOL_SCOUT",      "Dol Blathanna Scout",       Faction.SCOIATAEL, CardType.UNIT, Ability.AGILE,  null, RowType.RANGED, 6, 3),
            card("ST_BARCLAY",        "Barclay Els",               Faction.SCOIATAEL, CardType.UNIT, Ability.AGILE,  null, RowType.RANGED, 6, 1),
            card("ST_CIARAN",         "Ciaran aep Easnillien",     Faction.SCOIATAEL, CardType.UNIT, Ability.AGILE,  null, RowType.RANGED, 3, 1),
            card("ST_FILAVANDREL",    "Filavandrel aen Fidhail",   Faction.SCOIATAEL, CardType.UNIT, Ability.AGILE,  null, RowType.RANGED, 6, 1),

            // Units — Muster
            card("ST_DWARVEN_SKIRMISHER", "Dwarven Skirmisher",    Faction.SCOIATAEL, CardType.UNIT, Ability.MUSTER, null, RowType.MELEE,  3, 3),
            card("ST_ELVEN_SKIRMISHER",   "Elven Skirmisher",      Faction.SCOIATAEL, CardType.UNIT, Ability.MUSTER, null, RowType.RANGED, 2, 3),
            card("ST_HAVEKAR_SMUGGLER",   "Havekar Smuggler",      Faction.SCOIATAEL, CardType.UNIT, Ability.MUSTER, null, RowType.MELEE,  5, 3),

            // Units — Medic
            card("ST_HAVEKAR_HEALER", "Havekar Healer",            Faction.SCOIATAEL, CardType.UNIT, Ability.MEDIC,  null, RowType.RANGED, 0, 3),

            // Units — Scorch
            card("ST_SCHIRRU",        "Schirrú",                   Faction.SCOIATAEL, CardType.UNIT, Ability.SCORCH, null, RowType.SIEGE,  8, 1),

            // Units — Morale Boost
            card("ST_MILVA",          "Milva",                     Faction.SCOIATAEL, CardType.UNIT, Ability.MORALE_BOOST, null, RowType.RANGED, 10, 1),

            // Units — no ability
            card("ST_DENNIS",         "Dennis Cranmer",            Faction.SCOIATAEL, CardType.UNIT, null,           null, RowType.MELEE,  6, 1),
            card("ST_MAHAKAM",        "Mahakaman Defender",        Faction.SCOIATAEL, CardType.UNIT, null,           null, RowType.MELEE,  5, 5),
            card("ST_IDA",            "Ida Emean aep Sivney",      Faction.SCOIATAEL, CardType.UNIT, null,           null, RowType.RANGED, 6, 1),
            card("ST_DOL_ARCHER",     "Dol Blathanna Archer",      Faction.SCOIATAEL, CardType.UNIT, null,           null, RowType.RANGED, 4, 1),
            card("ST_VRIHEDD_RECRUIT","Vrihedd Brigade Recruit",   Faction.SCOIATAEL, CardType.UNIT, null,           null, RowType.RANGED, 4, 1),
            card("ST_TORUVIEL",       "Toruviel",                  Faction.SCOIATAEL, CardType.UNIT, null,           null, RowType.RANGED, 2, 1),
            card("ST_RIORDAIN",       "Riordain",                  Faction.SCOIATAEL, CardType.UNIT, null,           null, RowType.RANGED, 1, 1),

            // ── SKELLIGE ─────────────────────────────────────────────────────

            // Leaders
            card("SK_LEADER_BRAN", "King Bran",
                    Faction.SKELLIGE, CardType.LEADER,
                    null, LeaderAbility.KING_BRAN, null, null, 0),
            card("SK_LEADER_CRACH", "Crach an Craite",
                    Faction.SKELLIGE, CardType.LEADER,
                    null, LeaderAbility.CLAN_AN_CRAITE, null, null, 0),

            // Heroes
            card("SK_HERO_CERYS",   "Cerys an Craite",  Faction.SKELLIGE, CardType.HERO, Ability.MUSTER, null, RowType.MELEE,  10, 1),
            card("SK_HERO_HJALMAR", "Hjalmar an Craite",Faction.SKELLIGE, CardType.HERO, null,           null, RowType.RANGED, 10, 1),
            card("SK_HERO_ERMION",  "Ermion",           Faction.SKELLIGE, CardType.HERO, Ability.MARDROEME, null, RowType.RANGED, 8, 1),
            card("SK_HERO_HEMDALL", "Hemdall",          Faction.SKELLIGE, CardType.HERO, null,           null, RowType.MELEE,  11, 0),
            card("SK_HERO_OLAF",    "Olaf",             Faction.SKELLIGE, CardType.UNIT, Ability.AGILE, Ability.MORALE_BOOST, null, RowType.MELEE, 12, 1),

            // Units — existing (fixed)
            card("SK_HERO_BIRNA",   "Birna Bran",                Faction.SKELLIGE, CardType.UNIT, Ability.MEDIC,       null, RowType.MELEE,   2, 1),
            card("SK_VILDKAARL",    "Vildkaarl",                 Faction.SKELLIGE, CardType.UNIT, Ability.BERSERKER,   null, RowType.MELEE,   8, 1),
            card("SK_BERSERKER",    "Berserker Marauder",        Faction.SKELLIGE, CardType.UNIT, Ability.BERSERKER,   null, RowType.MELEE,   4, 3),
            card("SK_CLAN_AN_CRAITE","Clan an Craite Warrior",   Faction.SKELLIGE, CardType.UNIT, Ability.TIGHT_BOND,  null, RowType.MELEE,   6, 3),
            card("SK_PIRATE",       "Clan Dimun Pirate",         Faction.SKELLIGE, CardType.UNIT, Ability.SCORCH,      null, RowType.RANGED,  6, 1),
            card("SK_ARMORSMITH",   "Clan Tordarroch Armorsmith",Faction.SKELLIGE, CardType.UNIT, null,                null, RowType.MELEE,   4, 1),
            card("SK_BROKVAR",      "Clan Brokvar Archer",       Faction.SKELLIGE, CardType.UNIT, null,                null, RowType.RANGED,  6, 3),
            card("SK_SHIELD_MAIDEN","Shield Maiden",             Faction.SKELLIGE, CardType.UNIT, Ability.TIGHT_BOND, Ability.MUSTER, null, RowType.MELEE,   4, 3),

            // Units — new (no new abilities)
            card("SK_BLUEBOY",     "Blueboy Lugos",          Faction.SKELLIGE, CardType.UNIT, null,                null, RowType.MELEE, 6, 1),
            card("SK_MADMAN",      "Madman Lugos",           Faction.SKELLIGE, CardType.UNIT, null,                null, RowType.MELEE, 6, 1),
            card("SK_SVANRIGE",    "Svanrige",               Faction.SKELLIGE, CardType.UNIT, null,                null, RowType.MELEE, 4, 1),
            card("SK_UDALRYK",     "Udalryk",                Faction.SKELLIGE, CardType.UNIT, null,                null, RowType.MELEE, 4, 1),
            card("SK_DONAR",       "Donar an Hindar",        Faction.SKELLIGE, CardType.UNIT, null,                null, RowType.MELEE, 4, 1),
            card("SK_SKALD",       "Clan Heymaey Skald",     Faction.SKELLIGE, CardType.UNIT, null,                null, RowType.MELEE, 4, 1),
            card("SK_HOLGER",      "Holger Blackhand",        Faction.SKELLIGE, CardType.UNIT, null,                null, RowType.SIEGE, 4, 1),
            card("SK_DRAIG",       "Draig Bon-Dhu",          Faction.SKELLIGE, CardType.UNIT, Ability.COMMANDERS_HORN, null, RowType.SIEGE, 2, 1),

            // Units — new (existing abilities)
            card("SK_WAR_LONGSHIP",   "War Longship",     Faction.SKELLIGE, CardType.UNIT, Ability.TIGHT_BOND, null, RowType.SIEGE,  6, 3),
            card("SK_LIGHT_LONGSHIP", "Light Longship",   Faction.SKELLIGE, CardType.UNIT, Ability.MUSTER,     null, RowType.RANGED, 4, 3),
            card("SK_YOUNG_BERSERKER","Young Berserker",  Faction.SKELLIGE, CardType.UNIT, Ability.BERSERKER,  null, RowType.RANGED, 2, 3),

            // Units — new (new abilities)
            card("SK_KAMBI",          "Kambi",            Faction.SKELLIGE, CardType.UNIT, Ability.KAMBI,       null, RowType.MELEE,  0, 1),

            // Weather
            card("SK_SKELLIGE_STORM", "Skellige Storm",   Faction.SKELLIGE, CardType.WEATHER, Ability.SKELLIGE_STORM, null, null, null, 3),

            // ── NEUTRAL ──────────────────────────────────────────────────────

            // Weather
            card("NEUTRAL_FROST",         "Biting Frost",       Faction.NEUTRAL, CardType.WEATHER, Ability.FROST,         null, null, null, 3),
            card("NEUTRAL_FOG",           "Impenetrable Fog",   Faction.NEUTRAL, CardType.WEATHER, Ability.FOG,           null, null, null, 3),
            card("NEUTRAL_RAIN",          "Torrential Rain",    Faction.NEUTRAL, CardType.WEATHER, Ability.RAIN,          null, null, null, 2),
            card("NEUTRAL_CLEAR_WEATHER", "Clear Weather",      Faction.NEUTRAL, CardType.WEATHER, Ability.CLEAR_WEATHER, null, null, null, 2),

            // Special
            card("NEUTRAL_DUMMY",            "Decoy",             Faction.NEUTRAL, CardType.SPECIAL, Ability.DUMMY,            null, null, null, 3),
            card("NEUTRAL_COMMANDERS_HORN",  "Commander's Horn",  Faction.NEUTRAL, CardType.SPECIAL, Ability.COMMANDERS_HORN,  null, null, null, 3),
            card("NEUTRAL_SCORCH",           "Scorch",            Faction.NEUTRAL, CardType.SPECIAL, Ability.SCORCH,           null, null, null, 3),
            card("NEUTRAL_MARDROEME",        "Mardroeme",         Faction.SKELLIGE, CardType.SPECIAL, Ability.MARDROEME,        null, null, null, 3),

            // Neutral heroes
            card("NEUTRAL_HERO_GERALT",       "Geralt of Rivia",        Faction.NEUTRAL, CardType.HERO, null,                 null, RowType.MELEE,  15, 1),
            card("NEUTRAL_HERO_CIRI",         "Cirilla Fiona Elen Riannon", Faction.NEUTRAL, CardType.HERO, null,             null, RowType.MELEE,  15, 1),
            card("NEUTRAL_HERO_TRISS",        "Triss Merigold",         Faction.NEUTRAL, CardType.HERO, null,                 null, RowType.MELEE,   7, 1),
            card("NEUTRAL_HERO_YENNEFER",     "Yennefer of Vengerberg", Faction.NEUTRAL, CardType.HERO, Ability.MEDIC,        null, RowType.RANGED,  7, 1),
            card("NEUTRAL_HERO_AVALLACH",    "Avallac'h",              Faction.NEUTRAL, CardType.HERO, Ability.SPY,          null, RowType.MELEE,   0, 1),

            // Neutral units
            card("NEUTRAL_VILLENTRETENMERTH", "Villentretenmerth",      Faction.NEUTRAL, CardType.UNIT, Ability.SCORCH,          null, RowType.MELEE,  7, 1),
            card("NEUTRAL_ZOLTAN",            "Zoltan Chivay",          Faction.NEUTRAL, CardType.UNIT, null,                    null, RowType.MELEE,  5, 1),
            card("NEUTRAL_VESEMIR",           "Vesemir",                Faction.NEUTRAL, CardType.UNIT, null,                    null, RowType.MELEE,  6, 1),
            card("NEUTRAL_EMIEL_REGIS",       "Emiel Regis",            Faction.NEUTRAL, CardType.UNIT, null,                    null, RowType.MELEE,  5, 1),
            card("NEUTRAL_DANDELION",         "Dandelion",              Faction.NEUTRAL, CardType.UNIT, Ability.COMMANDERS_HORN, null, RowType.MELEE,  2, 1),
            card("NEUTRAL_OLGIERD",           "Olgierd von Everec",     Faction.NEUTRAL, CardType.UNIT, Ability.AGILE, Ability.MORALE_BOOST, null, RowType.MELEE, 6, 1),
            card("NEUTRAL_GAUNTER",           "Gaunter O'Dimm",         Faction.NEUTRAL, CardType.UNIT, Ability.MUSTER,          null, RowType.SIEGE,  2, 1),
            card("NEUTRAL_GAUNTER_DARKNESS",  "Gaunter O'Dimm: Darkness", Faction.NEUTRAL, CardType.UNIT, Ability.MUSTER,        null, RowType.RANGED, 4, 3)
        );
    }

    private CardEntity card(String id, String name, Faction faction, CardType cardType,
                            Ability ability, LeaderAbility leaderAbility,
                            RowType rowType, Integer basePower, int deckCopies) {
        return card(id, name, faction, cardType, ability, null, leaderAbility, rowType, basePower, deckCopies);
    }

    private CardEntity card(String id, String name, Faction faction, CardType cardType,
                            Ability ability, Ability secondAbility, LeaderAbility leaderAbility,
                            RowType rowType, Integer basePower, int deckCopies) {
        CardEntity e = new CardEntity();
        e.setId(id);
        e.setName(name);
        e.setFaction(faction);
        e.setCardType(cardType);
        e.setAbility(ability);
        e.setSecondAbility(secondAbility);
        e.setLeaderAbility(leaderAbility);
        e.setRowType(rowType);
        e.setBasePower(basePower);
        e.setDeckCopies(deckCopies);
        return e;
    }
}

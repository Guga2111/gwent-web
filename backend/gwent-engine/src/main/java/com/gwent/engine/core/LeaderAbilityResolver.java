package com.gwent.engine.core;

import com.gwent.engine.domain.*;
import com.gwent.engine.state.*;

import java.util.ArrayList;
import java.util.List;

class LeaderAbilityResolver {

    void resolve(GameState state, LeaderAbility ability) {
        switch (ability) {

            // --- Northern Realms ---
            case SIEGE_MASTER        -> handleSiegeMaster(state);
            // TODO: NORTH_COMMANDER (+1 a todas as unidades de cerco — requer sistema de modificadores de board)
            // TODO: KING_OF_TEMERIA (escolhe carta FOG do deck — requer player choice / pendingAbility)
            // TODO: LORD_COMMANDER (destrói unidade de cerco inimiga se score da linha >= 10)

            // --- Nilfgaard ---
            case WHITE_FLAME         -> handlePickWeatherFromDeck(state);
            // TODO: EMPEROR_OF_NILFGAARD (ver 3 cartas do oponente — sem mudança de estado, responsabilidade da API)
            // TODO: INVADER_OF_THE_NORTH (cancela habilidade de leader do oponente)
            // TODO: RELENTLESS (compra carta do graveyard do oponente — requer player choice)

            // --- Monsters ---
            case DESTROYER_OF_WORLDS -> handleDestroyerOfWorlds(state);
            // TODO: BRINGER_OF_DEATH (escolhe carta de clima do deck — igual a WHITE_FLAME)
            // TODO: COMMANDER_OF_THE_RED_RIDERS (escolhe qualquer carta do deck e depois descarta — requer player choice)
            // TODO: KING_OF_THE_WILD_HUNT (restaura carta do graveyard — requer pendingAbility)

            // --- Scoia'tael ---
            case DAISY_OF_THE_VALLEY -> handlePickWeatherFromDeck(state);
            // TODO: QUEEN_OF_DOL_BLATHANNA (destrói a unidade mais forte de corpo a corpo se score >= 10)
            // TODO: PUREBLOOD_ELF (escolhe qualquer carta, depois oponente também — requer player choice dos dois)
            // TODO: HOPE_OF_THE_AEN_SEIDHE (+1 a unidades ágeis — requer sistema de modificadores de board)

            // --- Skellige ---
            case KING_BRAN           -> handleKingBran(state);
            // TODO: CLAN_AN_CRAITE (restaura 2 cartas do graveyard — requer pendingAbility duas vezes)

            default -> {} // no-op para abilities não implementadas
        }
    }

    // Northern Realms: limpa o clima da linha de cerco de ambos os jogadores
    private void handleSiegeMaster(GameState state) {
        state.getPlayer1().getSiegeRow().setWeatherActive(false);
        state.getPlayer2().getSiegeRow().setWeatherActive(false);
        // TODO: remover cartas RAIN de board.activeWeatherCards quando Board ganhar removeWeatherCardsByAbility()
    }

    // Nilfgaard / Scoia'tael: move a primeira carta de clima encontrada no deck para a mão
    // TODO: substituir por player choice via pendingAbility quando houver múltiplas opções
    private void handlePickWeatherFromDeck(GameState state) {
        PlayerState current = state.getCurrentPlayer();
        current.getDeck().stream()
                .filter(c -> c.cardType() == CardType.WEATHER)
                .findFirst()
                .ifPresent(c -> {
                    current.removeFromDeck(c);
                    current.addToHand(c);
                });
    }

    // Monsters: descarta até 2 cartas da mão e compra 1 do deck
    // TODO: substituir descarte das primeiras cartas por player choice
    private void handleDestroyerOfWorlds(GameState state) {
        PlayerState current = state.getCurrentPlayer();
        List<Card> toDiscard = new ArrayList<>(current.getHand()).stream()
                .limit(2)
                .toList();
        toDiscard.forEach(c -> {
            current.removeFromHand(c);
            current.addToGraveyard(c);
        });
        if (!current.isDeckEmpty()) current.drawCard();
    }

    // Skellige: move todas as cartas do graveyard de volta para o deck
    private void handleKingBran(GameState state) {
        PlayerState current = state.getCurrentPlayer();
        new ArrayList<>(current.getGraveyard()).forEach(c -> {
            current.removeFromGraveyard(c);
            current.returnToDeck(c);
        });
    }
}

package com.gwent.api.game.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gwent.api.game.*;
import com.gwent.api.game.dto.*;

import com.gwent.api.game.exception.GameNotFoundException;
import com.gwent.api.game.exception.GameNotWaitingException;
import com.gwent.engine.domain.GamePhase;
import com.gwent.engine.domain.Turn;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class GamePersistenceService {

    private final GameRepository gameRepository;
    private final ObjectMapper objectMapper;

    public GamePersistenceService (GameRepository gameRepository, ObjectMapper objectMapper) {
        this.gameRepository = gameRepository;
        this.objectMapper = objectMapper;
    }

    public CreateGameDto createGame (String userId, UUID deckId) {
        UUID gameId = UUID.randomUUID();

        Game game = new Game();
        game.setId(gameId);
        game.setPlayer1Id(userId);
        game.setPlayer1DeckId(deckId);
        game.setStatus(GameStatus.WAITING);
        gameRepository.save(game);

        return new CreateGameDto(gameId, userId);
    }

    public Game getGameValidatingWaitingStatus (UUID gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException(gameId));

        if (game.getStatus() != GameStatus.WAITING) throw new GameNotWaitingException(gameId);

        return game;
    }

    public void saveJoinedGame (UUID gameId, String userId, UUID deckId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException(gameId));

        game.setPlayer2Id(userId);
        game.setPlayer2DeckId(deckId);
        gameRepository.save(game);
    }

    public void persist(UUID gameId, SessionContext ctx, GameStateDto dto) {
        try {
            GameStatus status = ctx.gameState().getPhase() == GamePhase.GAME_OVER
                    ? GameStatus.FINISHED : GameStatus.IN_PROGRESS;
            Game game = gameRepository.findById(gameId).orElse(new Game());
            game.setId(gameId);
            game.setStateJson(objectMapper.writeValueAsString(dto));
            game.setStatus(status);
            gameRepository.save(game);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize game state", e);
        }
    }

    public GameStateDto getPersistedState(UUID gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException(gameId));
        if (game.getStateJson() == null) throw new GameNotFoundException(gameId);
        try {
            return objectMapper.readValue(game.getStateJson(), GameStateDto.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize game state", e);
        }
    }

    public Optional<ActiveGameDto> findActiveGameForPlayer(String userId) {
        return gameRepository.findActiveGameForPlayer(GameStatus.IN_PROGRESS, userId)
                .map(g -> new ActiveGameDto(g.getId()));
    }
}

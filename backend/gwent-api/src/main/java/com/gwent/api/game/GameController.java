package com.gwent.api.game;

import com.gwent.api.game.dto.CommandRequestDto;
import com.gwent.api.game.dto.CreateGameDto;
import com.gwent.api.game.dto.GameStateDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameSessionService gameSessionService;

    public GameController(GameSessionService gameSessionService) {
        this.gameSessionService = gameSessionService;
    }

    @PostMapping
    public ResponseEntity<CreateGameDto> createGame() {
        return ResponseEntity.status(HttpStatus.CREATED).body(gameSessionService.createSession());
    }

    @PostMapping("/{gameId}/join")
    public ResponseEntity<GameStateDto> joinGame(@PathVariable UUID gameId) {
        return ResponseEntity.ok(gameSessionService.joinSession(gameId));
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<GameStateDto> getGame(@PathVariable UUID gameId) {
        return ResponseEntity.ok(gameSessionService.getSession(gameId));
    }

    @MessageMapping("/games/{gameId}/command")
    public void handleCommand(@DestinationVariable UUID gameId, CommandRequestDto request) {
        gameSessionService.execute(gameId, request);
    }
}

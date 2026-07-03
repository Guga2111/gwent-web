package com.gwent.api.game;

import com.gwent.api.game.dto.CommandRequestDto;
import com.gwent.api.game.dto.CreateGameDto;
import com.gwent.api.game.dto.ErrorDto;
import com.gwent.api.game.dto.GameStateDto;
import com.gwent.api.game.exception.CardNotFoundException;
import com.gwent.api.game.exception.GameNotFoundException;
import com.gwent.api.game.exception.PlayerNotInGameException;
import com.gwent.engine.exception.GwentException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameSessionService gameSessionService;
    private final SimpMessagingTemplate messagingTemplate;

    public GameController(GameSessionService gameSessionService, SimpMessagingTemplate messagingTemplate) {
        this.gameSessionService = gameSessionService;
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping
    public ResponseEntity<CreateGameDto> createGame(Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(gameSessionService.createSession(principal.getName()));
    }

    @PostMapping("/{gameId}/join")
    public ResponseEntity<GameStateDto> joinGame(@PathVariable UUID gameId, Principal principal) {
        return ResponseEntity.ok(gameSessionService.joinSession(gameId, principal.getName()));
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<GameStateDto> getGame(@PathVariable UUID gameId, Principal principal) {
        return ResponseEntity.ok(gameSessionService.getSession(gameId, principal.getName()));
    }

    @PostMapping("/{gameId}/surrender")
    public ResponseEntity<Void> surrender(@PathVariable UUID gameId, Principal principal) {
        gameSessionService.surrender(gameId, principal.getName());
        return ResponseEntity.ok().build();
    }

    @MessageMapping("/games/{gameId}/command")
    public void handleCommand(@DestinationVariable UUID gameId, CommandRequestDto request, Principal principal) {
        String userId = principal != null ? principal.getName() : request.playerId();
        try {
            gameSessionService.execute(gameId, userId, request);
        } catch (GwentException e) {
            sendErrorToPlayer(gameId, userId, "GAME_RULE_VIOLATION", e.getMessage());
        } catch (GameNotFoundException e) {
            sendErrorToPlayer(gameId, userId, "GAME_NOT_FOUND", e.getMessage());
        } catch (PlayerNotInGameException e) {
            sendErrorToPlayer(gameId, userId, "PLAYER_NOT_IN_GAME", e.getMessage());
        } catch (CardNotFoundException e) {
            sendErrorToPlayer(gameId, userId, "CARD_NOT_FOUND", e.getMessage());
        }
    }

    private void sendErrorToPlayer(UUID gameId, String userId, String error, String message) {
        messagingTemplate.convertAndSend(
                "/topic/games/" + gameId + "/" + userId + "/errors",
                new ErrorDto(error, message)
        );
    }
}

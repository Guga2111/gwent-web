package com.gwent.api.matchmaking;

import com.gwent.api.matchmaking.dto.JoinQueueRequest;
import com.gwent.api.matchmaking.dto.MatchFoundDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/matchmaking")
public class MatchmakingController {

    private final MatchmakingService matchmakingService;

    public MatchmakingController(MatchmakingService matchmakingService) {
        this.matchmakingService = matchmakingService;
    }

    @PostMapping("/join")
    public ResponseEntity<?> joinQueue(@RequestBody JoinQueueRequest req, Principal principal) {
        try {
            Optional<UUID> matchedGameId = matchmakingService.joinQueue(principal.getName(), req.deckId());
            return matchedGameId
                    .map(gameId -> ResponseEntity.ok((Object) new MatchFoundDto(gameId)))
                    .orElse(ResponseEntity.accepted().build());
        } catch (AlreadyInQueueException e) {
            return ResponseEntity.status(409).build();
        }
    }

    @PostMapping("/leave")
    public ResponseEntity<Void> leaveQueue(Principal principal) {
        matchmakingService.leaveQueue(principal.getName());
        return ResponseEntity.ok().build();
    }
}

package com.gwent.api.game.service;

import com.gwent.api.game.SessionContext;
import com.gwent.api.game.dto.GameStateDto;
import com.gwent.api.game.dto.PresenceDto;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GameBroadcastService {

    private final SimpMessagingTemplate messagingTemplate;

    public GameBroadcastService (SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void broadcastState (UUID gameId, SessionContext ctx, GameStateDto p1Dto, GameStateDto p2Dto) {
        messagingTemplate.convertAndSend("/topic/games/" + gameId + "/" + ctx.player1Id(), p1Dto);
        messagingTemplate.convertAndSend("/topic/games/" + gameId + "/" + ctx.player2Id(), p2Dto);
    }

    public void broadcastPresence (UUID gameId, String playerEmail, boolean isConnected, Long deadline) {
        messagingTemplate.convertAndSend("/topic/games/" + gameId + "/presence",
                new PresenceDto(playerEmail, isConnected, deadline));
    }
}

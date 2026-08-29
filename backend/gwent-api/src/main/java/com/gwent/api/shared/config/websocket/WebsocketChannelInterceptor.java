package com.gwent.api.shared.config.websocket;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.gwent.api.game.GameSessionService;
import com.gwent.api.security.SecurityConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class WebsocketChannelInterceptor implements ChannelInterceptor {
    @Value("${jwt.secret}")
    private String jwtSecret;

    private final WebSocketSessionRegistry registry;
    private final GameSessionService gameSessionService;

    public WebsocketChannelInterceptor (WebSocketSessionRegistry registry, @Lazy GameSessionService gameSessionService) {
        this.registry = registry;
        this.gameSessionService = gameSessionService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && accessor.getCommand() != null) {
            StompCommand command = accessor.getCommand();

            if (StompCommand.CONNECT.equals(command)) {
                handleConnect(accessor);
            } else if (StompCommand.SUBSCRIBE.equals(command)) {
                handleSubscribe(accessor);
            }
        }
        
        return message;
    }

    private void handleConnect(StompHeaderAccessor accessor) {
        String authHeader = accessor.getFirstNativeHeader(SecurityConstants.AUTHORIZATION);
        
        if (authHeader == null || !authHeader.startsWith(SecurityConstants.BEARER)) {
            throw new MessageDeliveryException("Not existing or invalid Authorization Header on CONNECT");
        }

        String token = authHeader.replace(SecurityConstants.BEARER, "");

        try {
            DecodedJWT jwt = JWT.require(Algorithm.HMAC512(jwtSecret))
                    .build()
                    .verify(token);

            String email = jwt.getSubject();
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(email, null, List.of());

            accessor.setUser(authentication);
            
        } catch (JWTVerificationException e) {
            throw new MessageDeliveryException("Expired or invalid JWT");
        }
    }

    private void handleSubscribe(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();

        if (destination != null && destination.startsWith("/topic/games/")) {
            Authentication user = (Authentication) accessor.getUser();

            if (user == null || !isAuthorizedForTopic(user, destination)) {
                throw new MessageDeliveryException("Denied access to player topic");
            }
            String[] segments = destination.substring("/topic/games/".length()).split("/");
            if (segments.length >= 2 && !"presence".equals(segments[1])) {
                UUID gameId = UUID.fromString(segments[0]);
                Integer count = registry.registerSession(accessor.getSessionId(), gameId, user.getName());
                if (count == 1) {
                    gameSessionService.cancelDisconnectForfeit(gameId, user.getName());
                }
            }
        }
    }

    private boolean isAuthorizedForTopic(Authentication user, String destination) {
        // pattern: /topic/games/{gameId}/{playerEmail} or /topic/games/{gameId}/{playerEmail}/errors
        // or /topic/games/{gameId}/presence
        String[] segments = destination.substring("/topic/games/".length()).split("/");
        if (segments.length < 2) return false;

        String playerSegment = segments[1];
        if ("presence".equals(playerSegment)) return true;
        return user.getName().equals(playerSegment);
    }
}
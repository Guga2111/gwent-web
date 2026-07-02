package com.gwent.api.shared.config.websocket;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.gwent.api.security.SecurityConstants;
import org.springframework.beans.factory.annotation.Value;
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

@Component
public class WebsocketChannelInterceptor implements ChannelInterceptor {

    @Value("${jwt.secret}")
    private String jwtSecret;

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
        }
    }

    private boolean isAuthorizedForTopic(Authentication user, String destination) {
        // pattern: /topic/games/{gameId}/{playerEmail}
        String[] segments = destination.substring("/topic/games/".length()).split("/");
        if (segments.length < 2) return false;
        String targetPlayerEmail = segments[1];
        return user.getName().equals(targetPlayerEmail);
    }
}
package com.gwent.api.security;

import com.gwent.api.security.manager.CustomAuthManager;
import com.gwent.api.user.User;
import com.gwent.api.user.UserNotFoundException;
import com.gwent.api.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static com.gwent.api.shared.TestDataFactory.makeUser;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomAuthManagerTest {

    @Mock
    private UserService userService;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private CustomAuthManager customAuthManager;

    // ── authenticate ──

    @Test
    void shouldReturnAuth_whenCredentialsValid() {
        User user = makeUser("user@test.com", "tester");
        user.setPassword("hashed-pass");
        when(userService.getUser("user@test.com")).thenReturn(user);
        when(passwordEncoder.matches("raw-pass", "hashed-pass")).thenReturn(true);

        Authentication auth = new UsernamePasswordAuthenticationToken("user@test.com", "raw-pass");
        Authentication result = customAuthManager.authenticate(auth);

        assertNotNull(result);
        assertTrue(result.isAuthenticated());
    }

    @Test
    void shouldThrowBadCredentials_whenPasswordWrong() {
        User user = makeUser("user@test.com", "tester");
        user.setPassword("hashed-pass");
        when(userService.getUser("user@test.com")).thenReturn(user);
        when(passwordEncoder.matches("wrong-pass", "hashed-pass")).thenReturn(false);

        Authentication auth = new UsernamePasswordAuthenticationToken("user@test.com", "wrong-pass");

        assertThrows(BadCredentialsException.class, () -> customAuthManager.authenticate(auth));
    }

    @Test
    void shouldPropagateUserNotFoundException() {
        when(userService.getUser("noone@test.com")).thenThrow(new UserNotFoundException("noone@test.com"));

        Authentication auth = new UsernamePasswordAuthenticationToken("noone@test.com", "pass");

        assertThrows(UserNotFoundException.class, () -> customAuthManager.authenticate(auth));
    }

    @Test
    void shouldReturnAuthWithEmailAsPrincipal() {
        User user = makeUser("principal@test.com", "tester");
        user.setPassword("hashed");
        when(userService.getUser("principal@test.com")).thenReturn(user);
        when(passwordEncoder.matches("pass", "hashed")).thenReturn(true);

        Authentication auth = new UsernamePasswordAuthenticationToken("principal@test.com", "pass");
        Authentication result = customAuthManager.authenticate(auth);

        assertEquals("principal@test.com", result.getPrincipal());
    }
}

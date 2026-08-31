package com.gwent.api.user;

import com.gwent.api.user.dto.UserMeDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static com.gwent.api.shared.TestDataFactory.makeUser;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    // ── getUser ──

    @Test
    void shouldReturnUser_whenEmailExists() {
        User user = makeUser("test@test.com", "tester");
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));

        User result = userService.getUser("test@test.com");

        assertEquals("test@test.com", result.getEmail());
        assertEquals("tester", result.getUsername());
    }

    @Test
    void shouldThrowUserNotFoundException_whenEmailNotFound() {
        when(userRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUser("missing@test.com"));
    }

    // ── registerUser ──

    @Test
    void shouldEncodePasswordAndSaveUser() {
        when(passwordEncoder.encode("raw-password")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.registerUser("new@test.com", "newuser", "raw-password");

        verify(passwordEncoder).encode("raw-password");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldReturnSavedUser_withCorrectFields() {
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.registerUser("new@test.com", "newuser", "pass");

        assertEquals("new@test.com", result.getEmail());
        assertEquals("newuser", result.getUsername());
        assertEquals("encoded", result.getPassword());
    }

    // ── getMe ──

    @Test
    void shouldReturnUserMeDto_whenUserExists() {
        User user = makeUser("me@test.com", "myself");
        when(userRepository.findByEmail("me@test.com")).thenReturn(Optional.of(user));

        UserMeDto dto = userService.getMe("me@test.com");

        assertEquals("me@test.com", dto.email());
        assertEquals("myself", dto.username());
        assertFalse(dto.hasSeenTutorial());
    }

    @Test
    void shouldThrowUserNotFoundException_whenGetMeUserNotFound() {
        when(userRepository.findByEmail("ghost@test.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getMe("ghost@test.com"));
    }

    // ── markTutorialSeen ──

    @Test
    void shouldSetHasSeenTutorialTrue_andSave() {
        User user = makeUser("tutorial@test.com", "learner");
        when(userRepository.findByEmail("tutorial@test.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.markTutorialSeen("tutorial@test.com");

        assertTrue(user.isHasSeenTutorial());
        verify(userRepository).save(user);
    }
}

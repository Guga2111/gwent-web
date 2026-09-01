package com.gwent.api.user;

import com.gwent.api.user.dto.UserMeDto;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
    }

    public User registerUser(String email, String username, String password) {
        User user = new User();
        user.setEmail(email);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        return userRepository.save(user);
    }

    public UserMeDto getMe(String email) {
        User user = getUser(email);
        return new UserMeDto(user.getEmail(), user.getUsername(), user.isHasSeenTutorial());
    }

    public void markTutorialSeen(String email) {
        User user = getUser(email);
        user.setHasSeenTutorial(true);
        userRepository.save(user);
    }
}

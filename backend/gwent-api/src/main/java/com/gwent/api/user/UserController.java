package com.gwent.api.user;

import com.gwent.api.user.dto.UserMeDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/users")
class UserController {

    private final UserService userService;

    UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserMeDto> getMe(Principal principal) {
        return ResponseEntity.ok(userService.getMe(principal.getName()));
    }

    @PostMapping("/me/tutorial-seen")
    public ResponseEntity<Void> markTutorialSeen(Principal principal) {
        userService.markTutorialSeen(principal.getName());
        return ResponseEntity.noContent().build();
    }
}

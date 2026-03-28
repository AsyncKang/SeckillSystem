package com.seckill.user.controller;

import com.seckill.user.entity.User;
import com.seckill.user.service.JwtService;
import com.seckill.user.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtService jwtService;

    @PostMapping("/register")
    public TokenResponse register(@Valid @RequestBody RegisterRequest req) {
        String token = userService.register(req.getUsername(), req.getPassword(), req.getPhone());
        return new TokenResponse(token);
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest req) {
        String token = userService.login(req.getUsername(), req.getPassword());
        return new TokenResponse(token);
    }

    @GetMapping("/me")
    public UserInfoResponse me(@RequestHeader(value = "Authorization", required = false) String auth) {
        if (auth == null || !auth.startsWith("Bearer ")) {
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.UNAUTHORIZED, "未登录");
        }
        Long userId = jwtService.getUserIdFromToken(auth.substring(7));
        User user = userService.getById(userId);
        return new UserInfoResponse(user.getId(), user.getUsername(), user.getAdmin());
    }

    @Data
    public static class RegisterRequest {
        @NotBlank
        private String username;
        @NotBlank
        private String password;
        private String phone;
    }

    @Data
    public static class LoginRequest {
        @NotBlank
        private String username;
        @NotBlank
        private String password;
    }

    @Data
    public static class TokenResponse {
        private final String token;
    }

    @Data
    public static class UserInfoResponse {
        private final Long id;
        private final String username;
        private final Boolean admin;
    }
}

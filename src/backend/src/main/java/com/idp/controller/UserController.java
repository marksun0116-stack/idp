package com.idp.controller;

import com.idp.dto.LoginResponse;
import com.idp.dto.RegisterUserResponse;
import com.idp.dto.UserAuthRequest;
import com.idp.dto.UserMeResponse;
import com.idp.model.AppUser;
import com.idp.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterUserResponse register(@Valid @RequestBody UserAuthRequest request) {
        AppUser user = userService.register(request);
        return new RegisterUserResponse("registered", user.getUsername());
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody UserAuthRequest request) {
        return new LoginResponse(userService.login(request));
    }

    @GetMapping("/me")
    public UserMeResponse me(Authentication authentication) {
        return new UserMeResponse(authentication.getName(), authentication.getName());
    }
}

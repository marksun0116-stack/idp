package com.idp.service;

import com.idp.dto.UserAuthRequest;
import com.idp.exception.UserAuthException;
import com.idp.exception.UserConflictException;
import com.idp.model.AppUser;
import com.idp.model.AuthToken;
import com.idp.repository.AppUserRepository;
import com.idp.repository.AuthTokenRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Locale;

@Service
public class UserService {
    private final AppUserRepository userRepository;
    private final AuthTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    public UserService(AppUserRepository userRepository, AuthTokenRepository tokenRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AppUser register(UserAuthRequest request) {
        String username = normalize(request.username());
        if (userRepository.existsByUsername(username)) {
            throw new UserConflictException("Username is already registered");
        }
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        return userRepository.save(user);
    }

    @Transactional
    public String login(UserAuthRequest request) {
        String username = normalize(request.username());
        AppUser user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UserAuthException("Invalid username or password"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UserAuthException("Invalid username or password");
        }
        AuthToken authToken = new AuthToken();
        authToken.setUser(user);
        authToken.setToken(generateToken());
        return tokenRepository.save(authToken).getToken();
    }

    private String normalize(String username) {
        return username.trim().toLowerCase(Locale.US);
    }

    private String generateToken() {
        byte[] bytes = new byte[36];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}

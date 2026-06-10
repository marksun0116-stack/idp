package com.idp.controller;

import com.idp.dto.PublicProfileRequest;
import com.idp.dto.PublicProfileResponse;
import com.idp.dto.PublicProfileUpdateResponse;
import com.idp.model.PublicProfile;
import com.idp.service.PublicProfileService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile/public")
public class ProfileController {
    private final PublicProfileService publicProfileService;

    public ProfileController(PublicProfileService publicProfileService) {
        this.publicProfileService = publicProfileService;
    }

    @GetMapping
    public PublicProfileResponse get(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return null;
        }
        return publicProfileService.getOwned(authentication.getName());
    }

    @PutMapping
    public PublicProfileUpdateResponse upsert(@Valid @RequestBody PublicProfileRequest request, Authentication authentication) {
        PublicProfile profile = publicProfileService.upsert(authentication.getName(), request);
        return new PublicProfileUpdateResponse(profile.getHandle(), profile.getUpdatedAt());
    }
}

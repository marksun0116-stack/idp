package com.idp.controller;

import com.idp.dto.PublicProfileResponse;
import com.idp.service.PublicProfileService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/investors")
public class PublicProfileController {
    private final PublicProfileService publicProfileService;

    public PublicProfileController(PublicProfileService publicProfileService) {
        this.publicProfileService = publicProfileService;
    }

    @GetMapping("/{handle}")
    public PublicProfileResponse get(@PathVariable("handle") String handle) {
        return publicProfileService.getPublic(handle);
    }
}

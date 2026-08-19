package com.geolink.findme.presentation.controller;

import com.geolink.findme.business.service.UserService;
import com.geolink.findme.presentation.dto.UpdateProfileRequest;
import com.geolink.findme.presentation.dto.UserProfileResponse;
import com.geolink.findme.presentation.mapper.UserWebMapper;
import com.geolink.findme.security.CurrentUserProvider;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final CurrentUserProvider currentUserProvider;
    private final UserWebMapper mapper;

    public UserController(UserService userService, CurrentUserProvider currentUserProvider, UserWebMapper mapper) {
        this.userService = userService;
        this.currentUserProvider = currentUserProvider;
        this.mapper = mapper;
    }

    @GetMapping("/me")
    public UserProfileResponse me() {
        var user = userService.getMyProfile(currentUserProvider.requireCurrentUserId());
        return mapper.toProfileResponse(user);
    }

    @PutMapping("/me")
    public UserProfileResponse updateMe(@Valid @RequestBody UpdateProfileRequest request) {
        var user = userService.updateMyProfile(currentUserProvider.requireCurrentUserId(), request.prenom(), request.nom());
        return mapper.toProfileResponse(user);
    }
}

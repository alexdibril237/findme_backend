package com.geolink.findme.auth.web.controller;

import com.geolink.findme.auth.application.usecase.GetMyProfileUseCase;
import com.geolink.findme.auth.application.usecase.UpdateMyProfileUseCase;
import com.geolink.findme.auth.infrastructure.security.CurrentUserProvider;
import com.geolink.findme.auth.web.dto.UpdateProfileRequest;
import com.geolink.findme.auth.web.dto.UserProfileResponse;
import com.geolink.findme.auth.web.mapper.UserWebMapper;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final GetMyProfileUseCase getMyProfileUseCase;
    private final UpdateMyProfileUseCase updateMyProfileUseCase;
    private final CurrentUserProvider currentUserProvider;
    private final UserWebMapper mapper;

    public UserController(GetMyProfileUseCase getMyProfileUseCase, UpdateMyProfileUseCase updateMyProfileUseCase,
                           CurrentUserProvider currentUserProvider, UserWebMapper mapper) {
        this.getMyProfileUseCase = getMyProfileUseCase;
        this.updateMyProfileUseCase = updateMyProfileUseCase;
        this.currentUserProvider = currentUserProvider;
        this.mapper = mapper;
    }

    @GetMapping("/me")
    public UserProfileResponse me() {
        var user = getMyProfileUseCase.execute(currentUserProvider.requireCurrentUserId());
        return mapper.toProfileResponse(user);
    }

    @PutMapping("/me")
    public UserProfileResponse updateMe(@Valid @RequestBody UpdateProfileRequest request) {
        var user = updateMyProfileUseCase.execute(new UpdateMyProfileUseCase.Command(
                currentUserProvider.requireCurrentUserId(), request.prenom(), request.nom()));
        return mapper.toProfileResponse(user);
    }
}

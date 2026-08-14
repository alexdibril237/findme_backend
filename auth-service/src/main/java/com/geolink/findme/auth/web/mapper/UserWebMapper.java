package com.geolink.findme.auth.web.mapper;

import com.geolink.findme.auth.application.usecase.AuthResult;
import com.geolink.findme.auth.domain.model.User;
import com.geolink.findme.auth.web.dto.AuthResponse;
import com.geolink.findme.auth.web.dto.UserProfileResponse;
import org.springframework.stereotype.Component;

@Component
public class UserWebMapper {

    public UserProfileResponse toProfileResponse(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getEmail().value(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole().name(),
                user.getStatus().name(),
                user.getCreatedAt(),
                user.getLastLoginAt()
        );
    }

    public AuthResponse toAuthResponse(AuthResult result) {
        return new AuthResponse(result.accessToken(), result.refreshToken(), toProfileResponse(result.user()));
    }
}

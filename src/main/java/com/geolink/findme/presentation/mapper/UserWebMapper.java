package com.geolink.findme.presentation.mapper;

import com.geolink.findme.business.service.AuthResult;
import com.geolink.findme.data.entity.User;
import com.geolink.findme.presentation.dto.AuthResponse;
import com.geolink.findme.presentation.dto.UserProfileResponse;
import org.springframework.stereotype.Component;

@Component
public class UserWebMapper {

    public UserProfileResponse toProfileResponse(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
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

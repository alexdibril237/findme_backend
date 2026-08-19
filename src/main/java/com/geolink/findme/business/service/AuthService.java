package com.geolink.findme.business.service;

public interface AuthService {

    AuthResult signup(String rawEmail, String rawPassword, String firstName, String lastName);

    AuthResult signin(String rawEmail, String rawPassword);

    AuthResult refresh(String rawRefreshToken);

    void logout(String rawRefreshToken);

    void forgotPassword(String rawEmail);

    void resetPassword(String rawToken, String newRawPassword);
}

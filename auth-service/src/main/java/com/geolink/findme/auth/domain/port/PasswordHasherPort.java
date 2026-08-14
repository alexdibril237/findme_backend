package com.geolink.findme.auth.domain.port;

public interface PasswordHasherPort {

    String hash(String rawPassword);

    boolean matches(String rawPassword, String hash);
}
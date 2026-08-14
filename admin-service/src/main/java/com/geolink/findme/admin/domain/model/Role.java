package com.geolink.findme.admin.domain.model;

/** Reflète les rôles émis par auth-service dans le JWT (claim {@code role}). */
public enum Role {
    USER,
    ADMIN,
    SUPPORT_AGENT
}

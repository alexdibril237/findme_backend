package com.geolink.findme.auth.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate racine du domaine auth. Pure Java : aucune dépendance à Spring ou JPA,
 * les règles métier (changement de mot de passe, activation/désactivation) vivent ici.
 */
public class User {

    private final UUID id;
    private Email email;
    private String passwordHash;
    private String firstName;
    private String lastName;
    private Role role;
    private AccountStatus status;
    private final Instant createdAt;
    private Instant updatedAt;
    private Instant lastLoginAt;

    private User(UUID id, Email email, String passwordHash, String firstName, String lastName,
                 Role role, AccountStatus status, Instant createdAt, Instant updatedAt, Instant lastLoginAt) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.lastLoginAt = lastLoginAt;
    }

    public static User createNew(Email email, String passwordHash, String firstName, String lastName) {
        Instant now = Instant.now();
        return new User(UUID.randomUUID(), email, passwordHash, firstName, lastName,
                Role.USER, AccountStatus.ACTIVE, now, now, null);
    }

    public static User rehydrate(UUID id, Email email, String passwordHash, String firstName, String lastName,
                                  Role role, AccountStatus status, Instant createdAt, Instant updatedAt,
                                  Instant lastLoginAt) {
        return new User(id, email, passwordHash, firstName, lastName, role, status, createdAt, updatedAt, lastLoginAt);
    }

    public boolean isActive() {
        return status == AccountStatus.ACTIVE;
    }

    public void changePassword(String newPasswordHash) {
        this.passwordHash = Objects.requireNonNull(newPasswordHash);
        this.updatedAt = Instant.now();
    }

    public void updateProfile(String firstName, String lastName) {
        this.firstName = Objects.requireNonNull(firstName);
        this.lastName = Objects.requireNonNull(lastName);
        this.updatedAt = Instant.now();
    }

    public void recordLogin() {
        this.lastLoginAt = Instant.now();
    }

    public void changeRole(Role newRole) {
        this.role = Objects.requireNonNull(newRole);
        this.updatedAt = Instant.now();
    }

    public void disable() {
        this.status = AccountStatus.DISABLED;
        this.updatedAt = Instant.now();
    }

    public void enable() {
        this.status = AccountStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public Email getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Role getRole() {
        return role;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }
}
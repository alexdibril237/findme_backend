package com.geolink.findme.admin.infrastructure.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/** Miroir minimal du JSON produit par Spring Data {@code Page<T>} côté auth-service/address-service. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PageEnvelope<T>(
        List<T> content,
        long totalElements,
        int totalPages,
        int number,
        int size
) {
}
